import uproot
import awkward as ak
import numpy as np
import pandas as pd
import os


def extract_collection(tree, prefix, fields):
    aux_name = f"Analysis{prefix}AuxDyn"

    branches = [
        f"{aux_name}.{field}"
        for field in fields
        if f"{aux_name}.{field}" in tree.keys()
    ]

    if not branches:
        return {}

    arrays = tree.arrays(branches, library="ak")

    return {
        field: arrays[f"{aux_name}.{field}"]
        for field in fields
        if f"{aux_name}.{field}" in arrays.fields
    }


def flatten_collection_to_dataframe(event_numbers, collection):
    """
    Flatten jagged awkward arrays into a pandas DataFrame.
    Returns None if there are no rows.
    """

    if not collection:
        return None

    reference = next(iter(collection.values()))

    # Total number of particles
    total = int(ak.sum(ak.num(reference)))
    if total == 0:
        return None

    # Broadcast event numbers
    event_broadcasted, _ = ak.broadcast_arrays(event_numbers, reference)

    data = {
        "eventNumber": ak.to_numpy(ak.flatten(event_broadcasted))
    }

    for field, arr in collection.items():
        data[field] = ak.to_numpy(
            ak.flatten(ak.fill_none(arr, np.nan))
        )

    df = pd.DataFrame(data)

    # Compute Cartesian momenta if possible
    if {"pt", "eta", "phi"}.issubset(df.columns):
        df["px"] = df["pt"] * np.cos(df["phi"])
        df["py"] = df["pt"] * np.sin(df["phi"])
        df["pz"] = df["pt"] * np.sinh(df["eta"])

    if df.empty:
        return None

    return df


def parse_physlite_to_parquet(
        filename,
        output_dir="parquet_output",
        max_events=None):

    os.makedirs(output_dir, exist_ok=True)

    file = uproot.open(filename)
    tree = file["CollectionTree"]

    # Event numbers
    if "EventInfo_eventNumber" in tree.keys():
        event_numbers = tree["EventInfo_eventNumber"].array(library="ak")
    else:
        event_numbers = ak.Array(np.arange(tree.num_entries))

    if max_events:
        event_numbers = event_numbers[:max_events]

    collections = {
        "TruthParticles": ["pt", "eta", "phi", "m", "pdgId", "status"],
        "Electrons": ["pt", "eta", "phi", "m", "charge"],
        "Muons": ["pt", "eta", "phi", "m", "charge"],
        "Jets": ["pt", "eta", "phi", "m"]
    }



    for name, fields in collections.items():
        print(f"Processing {name}...")

        collection = extract_collection(tree, name, fields)

        if max_events:
            collection = {k: v[:max_events] for k, v in collection.items()}

        df = flatten_collection_to_dataframe(event_numbers, collection)

        if df is None:
            print(f"⚠ Skipping {name}: no rows")
            continue

        output_path = os.path.join(output_dir, f"{name}.parquet")

        df.to_parquet(
            output_path,
            engine="pyarrow",
            compression="snappy",
            index=False
        )

        print(f"✔ Wrote {output_path} ({len(df)} rows)")


if __name__ == "__main__":
    parse_physlite_to_parquet(
        filename=r"C:\Datasets\data16_13TeV_Run_00296939\DAOD_PHYSLITE.37019878._000004.pool.root.1",
        output_dir=r"C:\Datasets\parquet_output",
        max_events=1000
    )


    # Need to fix the above - as we're getting empty parquet files back.