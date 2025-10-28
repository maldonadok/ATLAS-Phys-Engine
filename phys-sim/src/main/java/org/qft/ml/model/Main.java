package org.qft.ml.model;

import org.qft.ml.objects.elementary_particle;
import org.qft.ml.objects.quarks.Up;
import org.qft.ml.structure.frame;
import org.qft.ml.structure.point;
import org.qft.ml.structure.manifold;
import org.qft.ml.structure.spatial_metric;


public class Main {
    public static void main(String[] args) {

        //initialTesting();
        manifoldTesting();


    }

    private static void manifoldTesting() {

    }

    private static void initialTesting() {
        elementary_particle ep = new elementary_particle();
        ep.setColor("green");
        System.out.println("No issues initializing elementary particle.");

        System.out.println("Color charge of particle - " + ep.getColor());

        Up newUp = new Up();
        System.out.println("Mass of an Up quark - " + newUp.getMass() );

        frame f = new frame(true);

        point p = new point(0,0,0);
        System.out.println("This is our X coordinate: "+p.getX());
        System.out.println("This is our Y coordinate: "+p.getY());
        System.out.println("This is our Z coordinate: "+p.getZ());

        p.setX(2);
        p.setY(2);
        p.setZ(2);

        System.out.println("This is our X coordinate: "+p.getX());
        System.out.println("This is our Y coordinate: "+p.getY());
        System.out.println("This is our Z coordinate: "+p.getZ());
    }
}