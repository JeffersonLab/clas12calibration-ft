/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.util.Set;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.clas.view.DetectorShape2D;

/**
 *
 * @author devita
 */
public class FTHodoElectronics extends FTDetector {
    String viewName;
    private DetectorCollection<ShapePoint> points = new DetectorCollection<ShapePoint>();
    DetectorCollection<Double> thresholds = new DetectorCollection<Double>();
    
    FTHodoWire wireFTHodo = new FTHodoWire();
    
    public FTHodoElectronics(String name) {
        super(name);
        int nChannels = 16;
        int nMezz = 15;
        int sec;
        int com;
        int lay;
        int width = 10;
        for (int iMez = 0; iMez < nMezz; iMez++) {
            lay = 1;
            for (int iCh = 0; iCh < nChannels; iCh++) {
                if (iCh > 7) {
                    lay = 2;
                }
                if (iMez == 14 && wireFTHodo.isChannelEmpty(iCh)) {
                    continue;
                }
                com = wireFTHodo.getComp4ChMez(iCh, iMez);
                sec = wireFTHodo.getSect4ChMez(iCh, iMez);
                
                points.add(0, iMez, iCh, new ShapePoint(0.0,0.0));

                DetectorShape2D channel = new DetectorShape2D(DetectorType.FTHODO,
                                                              sec, lay, com);
                channel.createBarXY(width, width);
                channel.getShapePath().translateXYZ(2 * (iMez - 7) * width,
                                                    (width * iCh) + width * (lay - 1),
                                                    0.0);
                //viewChannels.setColor(0, 145, 0, 0);
                this.getView().addShape("Crate", channel);
            }
        }
    }
    public void setThresholds(double threshold) {
        int nChannels = 16;
        int nMezz = 15;
        for (int mezz = 0; mezz < nMezz; mezz++) {
            for (int chan = 0; chan < nChannels; chan++) {
                thresholds.add(0, mezz, chan, threshold);
            }
        }
    }
    
    public DetectorCollection<Double> getThresholds() {
        return this.thresholds;
    }
    
    public double getIdX(int sector, int layer, int component) {
        return this.points.get(0, layer, component).x();
    }
    
    public double getIdY(int sector, int layer, int component) {
        return this.points.get(0, layer, component).y();
    }
    
    public String getComponentName(int sector, int iMez, int iCh) {
        String title = "Mezannine:" + iMez + " Channel:" + iCh;
        return title;
    }
    
    public Set<Integer> getDetectorComponents(int sector, int layer) {
        return this.points.getComponents(0, layer);
    }
    
    public boolean hasComponent(int sector, int layer, int component) {
        return this.points.hasEntry(0, layer, component);
    }
    
    public int getNComponents(int sector, int layer) {
        return this.points.getComponents(0, layer).size();
    }
    
    public int getComponentMaxCount(int sector, int layer) {
        int keyMax=0;
        for(int key : this.points.getComponents(0, layer)) keyMax=key;
        return keyMax;
    }
    
    public class ShapePoint {
        private double x; // the x coordinate
        private double y; // the y coordinate
        
        public ShapePoint(double x, double y) {
            set(x, y);
        }
        private void set(double x, double y) {
            setX(x);
            setY(y);
        }
        private void setX(double x) {
            this.x = x;
        }
        private void setY(double y) {
            this.y = y;
        }
        public double x() {
            return x;
        }
        public double y() {
            return y;
        }
    }

}
