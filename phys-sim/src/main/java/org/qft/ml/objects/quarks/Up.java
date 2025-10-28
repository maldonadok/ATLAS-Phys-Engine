package org.qft.ml.objects.quarks;

import org.qft.ml.objects.elementary_particle;

public class Up extends elementary_particle {
    public Up() {
    }
    @Override
    public double getMass() {
        return 2.2; // MeV/c^2
    }

    @Override
    public double getCharge() {
        return (double) 2/3;
    }

    @Override
    public double getSpin() {
        return (double) 1/2;
    }
}
