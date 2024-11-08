package org.clas.ftdata;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author devita
 */
public class FTCalTrue {
    
    private double time;
    private double energy;
    private Vector3D momentum;
    private Point3D  position;

    public FTCalTrue(double time, double energy, Vector3D momentum, Point3D position) {
        this.time = time;
        this.energy = energy;
        this.momentum = momentum;
        this.position = position;
    }
  
    public double energy() {
        return energy;
    }

    public double time() {
        return time;
    }

    public Vector3D momentum() {
        return momentum;
    }

    public Point3D position() {
        return position;
    }

}
