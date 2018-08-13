/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.util.Set;
import org.jlab.detector.base.DetectorCollection;
import org.clas.view.DetectorPane2D;

/**
 *
 * @author devita
 */
public abstract class FTDetector extends DetectorPane2D {

    public FTDetector(String name) {
    }
    
    public abstract int getNComponents(int sector, int layer); // return the total number of components
    
    public abstract int getComponentMaxCount(int sector, int layer);

    public abstract Set<Integer> getDetectorComponents(int sector, int layer) ;
    
    public abstract DetectorCollection<Double> getThresholds();
    
    public abstract boolean hasComponent(int sector, int layer, int component);
    
    public abstract String getComponentName(int sector, int layer, int component);
    
    public abstract double getIdX(int sector, int layer, int component);

    public abstract double getIdY(int sector, int layer, int component);

}
