package org.qft.ml.structure;

// A simple point of space described by coordinates
public class point {
    // Let's let default values be 0 for now.
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public point(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // return midpoint in 3 dimensions
    public point getMidpoint(point a, point b){
        double midX = ( a.getX() + b.getX() ) /2;
        double midY = ( a.getY() + b.getY() ) /2;
        double midZ = ( a.getZ() + b.getZ() ) /2;
        return new point(midX,midY,midZ);
    }
}
