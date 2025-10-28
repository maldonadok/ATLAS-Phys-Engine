import sys

import EventObject as eo
import scipy.constants
import pandas as pd
import numpy as np


def main():
    filename = "C:\Datasets\data16_13TeV_Run_00296939\DAOD_PHYSLITE.37019878._000004.pool.root.1"
    event_object = eo.EventObject(filename)
    np.set_printoptions(threshold=sys.maxsize)
    electron_pt = np.array(event_object.getElectrons().iloc[:, 0])

    print(electron_pt)
 

    #electron_velocity_arr = getVelocity(event_object.getElectrons())
    #electron_pt = np.array(event_object.getElectrons().iloc[:, 0])
    #graphVelocityAndMomentum(electron_velocity_arr, electron_pt)






def getVelocity(df):
    e_mass=scipy.constants.electron_mass
    flattened_electron_pt = np.array(df.iloc[:, 0]).flatten()
    vel_arr = []
    for momentum in flattened_electron_pt:
        print(momentum/e_mass)
        vel_arr.append(momentum/e_mass)
    return vel_arr

def graphVelocityAndMomentum(electron_velocity_arr, electron_pt):
    pass

if __name__ == "__main__":
    main()