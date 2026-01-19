package org.qft.ml.structure;

class Jet {
    final double x, y, z;
    final double pMag;
    Jet(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pMag = Math.sqrt(x*x + y*y + z*z);
    }
}
