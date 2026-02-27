package org.qft.ml.model.objects;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChargedParticle {

    public static final int MAX_TRAIL_LENGTH = 40;
    public double x, y, z;
    public double vx, vy, vz;
    public double charge;

    public static final double c = 1.0; // visualization units

    public Deque<double[]> trail = new ArrayDeque<>();
    public Deque<double[]> spacetimeTrail = new ArrayDeque<>();

    public ChargedParticle(
            double x, double y, double z,
            double vx, double vy, double vz,
            double charge) {

        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.charge = charge;
    }

    public double gamma() {
        double v2 = vx*vx + vy*vy + vz*vz;
        double beta2 = v2 / (c*c);
        return 1.0 / Math.sqrt(1.0 - beta2);
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        z += vz * dt;
    }
}
