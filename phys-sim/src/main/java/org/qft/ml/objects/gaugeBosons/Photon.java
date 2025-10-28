package org.qft.ml.objects.gaugeBosons;

import org.qft.ml.objects.elementary_particle;

public class Photon extends elementary_particle {
    public Photon() {

    }

    @Override
    public double getMass() {
        return 0; // eV/c^2
    }

    @Override
    public double getCharge() {
        return 0;
    }

    @Override
    public double getSpin() {
        return 1;
    }

}
