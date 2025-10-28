package org.qft.ml.structure;

import java.util.List;

public class spatial_metric {
    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getDz() {
        return dz;
    }

    public void setDz(double dz) {
        this.dz = dz;
    }

    double dx = 1;
    double dy = 1;
    double dz = 1; 

    // Check def of christoffel here. Taking a covariant deriv esentially
    public boolean christoffel(frame check1, frame check2){
        return false;
    }
}
