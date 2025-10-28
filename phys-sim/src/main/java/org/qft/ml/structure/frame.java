package org.qft.ml.structure;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.qft.ml.objects.matter;
import java.util.HashMap;
import java.util.Map;

// Consists of 8 vertices
// By default let's initialize to the unit cube

public class frame {
    matter m = new matter(false);

    // if there is a matter object in frame, we want by default the coords to be as below:
    public point objectCenterInFrame = new point(0.5,0.5,0.5);

    ImmutablePair<point, matter> pair;

    point vertex1 = new point(0,0,0);
    point vertex2 = new point(0,1,0);
    point vertex3 = new point(1,1,0);
    point vertex4 = new point(1,0,0);
    point vertex5 = new point(1,0,1);
    point vertex6 = new point(0,0,1);
    point vertex7 = new point(0,1,1);
    point vertex8 = new point(1,1,1);

    Map<Integer, point> Vertices = new HashMap<Integer, point>();

    public frame() {
        this.Vertices.put(1, this.vertex1);
        this.Vertices.put(2, this.vertex2);
        this.Vertices.put(3, this.vertex3);
        this.Vertices.put(4, this.vertex4);
        this.Vertices.put(5, this.vertex5);
        this.Vertices.put(6, this.vertex6);
        this.Vertices.put(7, this.vertex7);
        this.Vertices.put(8, this.vertex8);
    }

    public point getObjectCenterInFrame() {
        return objectCenterInFrame;
    }

    public void setObjectCenterInFrame(point objectCenterInFrame) {
        this.objectCenterInFrame = objectCenterInFrame;
    }

    public frame(boolean e) {
        this.Vertices.put(1, this.vertex1);
        this.Vertices.put(2, this.vertex2);
        this.Vertices.put(3, this.vertex3);
        this.Vertices.put(4, this.vertex4);
        this.Vertices.put(5, this.vertex5);
        this.Vertices.put(6, this.vertex6);
        this.Vertices.put(7, this.vertex7);
        this.Vertices.put(8, this.vertex8);

        if (e){
            // If matter is in this frame, we put the default coordinates into the frame object,
            // then turn the exists value into true, then create a pair of the coordinates and the object
            this.Vertices.put(9, this.objectCenterInFrame);
            m.exists = true;
            pair = new ImmutablePair<>(this.objectCenterInFrame, this.m);
        }
        else{
            System.out.println("No matter object detected.");
        }
    }

    public boolean hasObject() {
        return m.exists;
    }

    public boolean isValidFace(){
        return false;
    }

    public boolean isPositiveValidSpace(){
        return false;
    }

}
