import unittest

import numpy as np

import EventObject

def main():
    filename = "C:\Datasets\data16_13TeV_Run_00296939\DAOD_PHYSLITE.37019878._000004.pool.root.1"
    #testElectrons(filename)
    #testILOCElectrons(filename)
    testDepthLimit1EventObject(filename)


# Note the behavior for each of the below calls to object.
def testElectrons(f):
    eo = EventObject.EventObject(f)
    electrons = eo.getElectrons()
    print(electrons.describe())
    print("--------------------------------\n")
    print(eo.electrons.describe())
    print("--------------------------------\n")
    print(eo.getElectrons())


def testDepthLimit1EventObject(f):
    eo = EventObject.EventObject(f)
    eo_slice = eo.getEvents()
    print("eo_slice event shape:")
    print(eo_slice.shape)       # with this particular file, 146115 rows, 4 columns
    print(eo_slice.iloc[:, ])

    flattened_event=np.array(eo_slice.iloc[:, ])
    print("Success! We now have a flattened event array:")
    print(flattened_event[0])

    # this works!!!
    #print("Here is every event:")
    #for event in flattened_event:
        #print(event)


def testILOCElectrons(f):
    eo = EventObject.EventObject(f)
    electrons = eo.getElectrons()

    #1105 rows, 3 columns
    print(electrons.shape)
    print(electrons.iloc[:, 0])     # get the pt column

    pt_electrons=np.array(electrons.iloc[:, 0])
    print("Success! We now have pt array:")
    print(pt_electrons.shape)

    print("Success! We now have a flattened pt array:")
    flattened_pt = pt_electrons.flatten()
    print(flattened_pt[0])

    print("Here's the whole flattened momentum array: ")

    for pt in flattened_pt:
        print(pt)


if __name__ == '__main__':
    main()
