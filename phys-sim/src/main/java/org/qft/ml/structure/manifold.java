package org.qft.ml.structure;

import java.util.*;

public class manifold {

    frame mainFrame;
    List<frame> listOfFrames;
    // Each list of frames corresponds to exactly one frame evolving over time
    // We'll describe the manifold as a hashset containing these lists of frames
    HashSet<List<frame>> sheetOfFrames;
    public manifold(){
        this.mainFrame = new frame();
        this.listOfFrames = new ArrayList<>();
        this.listOfFrames.add(this.mainFrame);
        this.sheetOfFrames = new HashSet<>();
        this.sheetOfFrames.add(this.listOfFrames);
    }

    public List<frame> getListOfFrames() {
        return listOfFrames;
    }

    public frame getMainFrame() {
        return mainFrame;
    }

    public HashSet<List<frame>> getSheetOfFrames(){
        return sheetOfFrames;
    }
}
