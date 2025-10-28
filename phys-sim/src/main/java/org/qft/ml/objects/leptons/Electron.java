package org.qft.ml.objects.leptons;

import org.qft.ml.objects.elementary_particle;

public class Electron extends elementary_particle {
    public Electron(){

    }

    @Override
    public double getMass() {
        return 0.511; // MeV/c^2
    }

    @Override
    public double getCharge() {
        return -1;
    }

    @Override
    public double getSpin() {
        return (double) 1/2;
    }
}
