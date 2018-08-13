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
 * @author nicholas Zachariou
 */
public class FTHodoDetector extends FTDetector {
    String viewName;
    private DetectorCollection<ShapePoint> points = new DetectorCollection<ShapePoint>();
    DetectorCollection<Double> thresholds = new DetectorCollection<Double>();
    
    public FTHodoDetector(String name) {
        super(name);
        int sector;// sectors 1-8 for each layer; detector symmetry is fourfold; with elements 0-28 for each quarter.
        int component; //tile component;1-9 for odd sectors;1-20 for even
        int layer;// thick and thin
        
        double[] layerOffsetY = {-170, 170.0}; // y-offset to place thin and thick layer on same pane
        double[] tileSize = {15.0, 30.0, 15.0, 30.0, 30.0, 30.0, 30.0, 30.0, 15.0,         // size of tiles per quadrant
            30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0,
            30.0, 30.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0};
        double[] tileThickness = {7., 15.};
        //============================================================
        double[] xx = {-97.5, -75.0, -127.5, -105.0, -75.0,
            -135.0, -105.0, -75.0, -52.5,
            -45.0, -15.0, 15.0, 45.0, -45.0,
            -15.0, 15.0, 45.0, -45.0, -15.0,
            15.0, 45.0, -52.5, -37.5, -22.5,
            -7.5, 7.5, 22.5, 37.5, 52.5};
        double[] yy = {-127.5, -135.0, -97.5, -105.0, -105.0,
            -75.0, -75.0, -75.0, -52.5,
            -150.0, -150.0, -150.0, -150.0, -120.0,
            -120.0, -120.0, -120.0, -90.0, -90.0,
            -90.0, -90.0, -67.5, -67.5, -67.5,
            -67.5, -67.5, -67.5, -67.5, -67.5};
        //============================================================
        double xcenter = 0;
        double ycenter = 0;
        double zcenter;
        for (int layerI = 0; layerI < 2; layerI++) { // two layers: I==0 for thin and I==1 for thick
            layer = layerI + 1;
            for (int quadrant = 0; quadrant < 4; quadrant++) { // 4 symmetry sectors per layer (named quadrant) from 0-3
                for (int element = 0; element < 29; element++) {// 29 elements per symmetry sector;sector is odd for first 9 elements and even for the rest
                    if (element < 9) {
                        sector = quadrant * 2 + 1;
                        component = element + 1;    // component number for odd sector is 1-9
                    } else {
                        sector = quadrant * 2 + 2;
                        component = element + 1 - 9;// component number for even sector is 1-20
                    }
                    switch (quadrant) {// calculate the x-element of the center of each tile;
                        case 0:
                            xcenter = xx[element];
                            break;
                        case 1:
                            xcenter = -yy[element];
                            break;
                        case 2:
                            xcenter = -xx[element];
                            break;
                        case 3:
                            xcenter = yy[element];
                            break;
                        default:
                            break;
                    }
                    switch (quadrant) { // calculate the y-element of the center of each tile
                        case 0:
                            ycenter = yy[element] + layerOffsetY[layerI];
                            break;
                        case 1:
                            ycenter = xx[element] + layerOffsetY[layerI];
                            break;
                        case 2:
                            ycenter = -yy[element] + layerOffsetY[layerI];
                            break;
                        case 3:
                            ycenter = -xx[element] + layerOffsetY[layerI];
                            break;
                        default:
                            break;
                    }
                    if (layerI == 0) {
                        zcenter = -tileThickness[layerI] / 2.0;
                    } else {
                        zcenter = tileThickness[layerI] / 2.0;
                    }
                    double ix= xcenter;
                    double iy= ycenter-layerOffsetY[layerI];
                    points.add(sector, layer, component, new ShapePoint(ix,iy));
                    // Sectors 1-8
                    // (sect=1: upper left - clockwise);
                    // layers 1-2 (thin==1, thick==2);
                    // tiles (1-9 for odd and 1-20 for even sectors)
                    DetectorShape2D shape = new DetectorShape2D(DetectorType.FTHODO,
                                                                sector,
                                                                layer,
                                                                component);
                    
//                    DetectorShape2D shape2 = new DetectorShape2D(DetectorType.FTHODO,
//                                                                 sector,
//                                                                 layer,
//                                                                 component);
                    // defines the 2D bars dimensions using the element size
                    shape.createBarXY(tileSize[element], tileSize[element]);
//                    shape2.createBarXY(tileSize[element], tileThickness[layerI]);
                    // defines the placements of the 2D bar according to the
                    // xcenter and ycenter calculated above
                    shape.getShapePath().translateXYZ(xcenter, ycenter, zcenter);
                    shape.setColor(0, 0, 0, 0);
//                    shape2.setColor(0, 0, 0, 0);
//                    shape2.getShapePath().translateXYZ(xcenter, zcenter, 0);
//                    this.getView().addShape("Side", shape2);
                    this.getView().addShape("Front", shape);

                }
            }
        }
    }
    
    public void setThresholds(double threshold) {
        int sector,layer, component;
        for (int layerI = 0; layerI < 2; layerI++) { // two layers: I==0 for thin and I==1 for thick
            layer = layerI + 1;
            for (int quadrant = 0; quadrant < 4; quadrant++) { // 4 symmetry sectors per layer (named quadrant) from 0-3
                for (int element = 0; element < 29; element++) {// 29 elements per symmetry sector;sector is odd for first 9 elements and even for the rest
                    if (element < 9) {
                        sector = quadrant * 2 + 1;
                        component = element + 1;    // component number for odd sector is 1-9
                    } else {
                        sector = quadrant * 2 + 2;
                        component = element + 1 - 9;// component number for even sector is 1-20
                    }
                    thresholds.add(sector, layer, component, threshold);
                }
            }
        }
    }
    
    public DetectorCollection<Double> getThresholds() {
        return this.thresholds;
    }
    
    public double getIdX(int sector, int layer, int component) {
        return this.points.get(sector, layer, component).x();
    }
    
    public double getIdY(int sector, int layer, int component) {
        return this.points.get(sector, layer, component).y();
    }
    
    public String getComponentName(int sector, int layer, int component) {
        String title = "Sector: " + sector + " Layer: " + layer + " Component: "+component;
        return title;
    }

    public Set<Integer> getDetectorComponents(int sector, int layer) {
        return this.points.getComponents(sector, layer);
    }

    public boolean hasComponent(int sector, int layer, int component) {
        return this.points.hasEntry(sector, layer, component);
    }

    public int getNComponents(int sector, int layer) {
        return this.points.getComponents(sector, layer).size();
    }

    public int getComponentMaxCount(int sector, int layer) {
        int keyMax=0;
        for(int key : this.points.getComponents(sector, layer)) keyMax=key;
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
