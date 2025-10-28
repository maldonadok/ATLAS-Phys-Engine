import pandas as pd
import awkward as ak  # nested, variable sized data
import uproot  # use of root files
from matplotlib import pyplot as plt


# Rewritten TT analysis from the CERN data intro using Pandas

def main():
    filename = "C:\Datasets\data16_13TeV_Run_00296939\DAOD_PHYSLITE.37019878._000004.pool.root.1"
    ttAnalysis(filename)


def ttAnalysis(filename):
    electron_event = None
    muon_event = None
    jet_event = None
    tree = uproot.open({filename: "CollectionTree"})

    electrons = ak.to_dataframe(
        ak.zip(  # Zips multiple arrays into one single structure
            {
                "pt": tree["AnalysisElectronsAuxDyn.pt"].array(),
                "eta": tree["AnalysisElectronsAuxDyn.eta"].array(),
                "phi": tree["AnalysisElectronsAuxDyn.phi"].array(),
            }
        )
    )

    muons = ak.to_dataframe(
        ak.zip(
            {
                "pt": tree["AnalysisMuonsAuxDyn.pt"].array(),
                "eta": tree["AnalysisMuonsAuxDyn.eta"].array(),
                "phi": tree["AnalysisMuonsAuxDyn.phi"].array(),
            }
        )
    )

    jets = ak.to_dataframe(
        ak.zip(
            {
                "pt": tree["AnalysisJetsAuxDyn.pt"].array(),
                "eta": tree["AnalysisJetsAuxDyn.eta"].array(),
                "phi": tree["AnalysisJetsAuxDyn.phi"].array(),
                "mass": tree["AnalysisJetsAuxDyn.m"].array(),
            }
        )
    )

    print(electrons.describe())
    print("--------------------------------\n")

    # Checking that we can successfully flatten for analysis
    flattened_electrons_pt = electrons.to_numpy().flatten()
    plt.hist(flattened_electrons_pt, bins=100)
    plt.title('$p_T$ distribution of all $e$')
    plt.xlabel('$p_T$')
    plt.ylabel('Number of electrons')
    plt.show()

    #electrons["pt"].plot()
    #electrons.plot.scatter(x="pt", y="phi", alpha=0.5)
    #electrons.plot.area(subplots=True)
    #plt.show()

    # For the jets portion in the tree branch = BTagging_AntiKt4EMPFlowAuxDyn.DL1dv01_pb
    # PHYSLITE files have exactly one btagging value for each jet in the branch

    btag_prob = ak.to_dataframe(
        tree["BTagging_AntiKt4EMPFlowAuxDyn.DL1dv01_pb"].array()
    )
    print("btag_prob:")
    print(btag_prob.describe())

    print("--------------------------------\n")

    print("jets:")
    print(jets.describe())
    print("--------------------------------\n")


# Checking this is obviously different from using the Awkward library
    if jets["pt"].count() == btag_prob["values"].count():
        print("Their counts are equal!")
        # We then want to attach each value to jets, since their counts are 1:1
        jets["btag_prob"] = btag_prob       # This works!
        print(jets.describe())

    else:
        print("Something went wrong buddy. See the stack trace :/")

    # creating the events DF is essentially making a DF of all 3 df's
    # try this:

    dfs = []
    dfs.extend([electrons, muons, jets])
    events = pd.concat(dfs)
    print("Describing events")
    print(events.describe())
    print("Event head")
    print(events.head())


if __name__ == "__main__":
    main()