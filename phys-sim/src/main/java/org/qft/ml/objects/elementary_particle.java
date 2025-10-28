package org.qft.ml.objects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class elementary_particle extends matter{

    Set<String> colors = new HashSet<String>(Arrays.asList("red","green","blue","anti-red","anti-green","anti-blue"));
    double mass=0;
    double charge=0;
    double spin=0;
    String color;
    double p_i=0;
    double p_j=0;
    double p_k=0;

    public elementary_particle() {
        super(true);
    }

    public double getCharge() {
        return this.charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getSpin() {
        return spin;
    }

    public void setSpin(double spin) {
        this.spin = spin;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        if(!colors.contains(color)){
            // Doesn't contain correct color in list, ie not valid
            throw new IllegalArgumentException("Input color is not valid.");
        }
        else {
            this.color = color;
        }
    }

    public double getP_i() {
        return p_i;
    }

    public void setP_i(double p_i) {
        this.p_i = p_i;
    }

    public double getP_j() {
        return p_j;
    }

    public void setP_j(double p_j) {
        this.p_j = p_j;
    }

    public double getP_k() {
        return p_k;
    }

    public void setP_k(double p_k) {
        this.p_k = p_k;
    }

    public double getMass() {
        return this.mass;
    }
}
