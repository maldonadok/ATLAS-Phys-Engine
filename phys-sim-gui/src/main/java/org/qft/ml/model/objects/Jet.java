package org.qft.ml.model.objects;

import java.awt.*;


public final class Jet {
    final double x, y, z;
    final double pMag;
    final int baseRadius;
    public static final double JET_MIN_RADIUS = 2;
    public static final double JET_MAX_RADIUS = 6;
    public static final double JET_P_SCALE = 0.03; // tune visually
    public static final Color JET_COLOR = new Color(20, 20, 20, 200);


    public Jet(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pMag = Math.sqrt(x*x + y*y + z*z);
        this.baseRadius = (int)Math.min(
                JET_MAX_RADIUS,
                Math.max(JET_MIN_RADIUS, pMag * JET_P_SCALE)
        );
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double momentumMag(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
}