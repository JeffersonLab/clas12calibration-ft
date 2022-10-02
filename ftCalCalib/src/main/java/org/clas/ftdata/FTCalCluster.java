package org.clas.ftdata;

import java.util.ArrayList;
import java.util.Collections;
import org.clas.viewer.FTCalConstants;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author devita
 */
public class FTCalCluster extends ArrayList<FTCalHit> {
    
    private int     id;
    private int     seed;
    private int     charge;
    
    // original values
    private double  energy0;
    private double  energyR0;
    private double  time0;
    private Point3D position0;
    private double  path0;
    private double  vertexTime0;
   
    // calibrated values
    private double  energy;
    private double  energyR;
    private double  time;
    private Point3D position;
    private double  path;
    private double  vertexTime;
    

    public FTCalCluster(int id, int charge, double energy, double energyR, double time, double x, double y, double z) {
        this.id = id;
        this.charge      = charge;
        this.energy0     = energy;
        this.energyR0    = energyR;
        this.time0       = time;
        this.position0   = new Point3D(x, y, z);
        this.path0       = this.position0.distance(FTCalConstants.VERTEX);
        this.vertexTime0 = this.time0 -this.path0/PhysicsConstants.speedOfLight();
        this.energy      = energy;
        this.energyR     = energyR;
        this.time        = time;
        this.position    = new Point3D(x, y, z);
        this.path        = this.position.distance(FTCalConstants.VERTEX);
        this.vertexTime  = this.time -this.path/PhysicsConstants.speedOfLight();
    }

    public int id() {
        return id;
    }

    public int seed() {
        return seed;
    }
    
    public int charge() {
        return charge;
    }

    public double energy(boolean calib) {
        if(calib)
            return energy;
        else
            return energy0;
    }

    public double energyR(boolean calib) {
        if(calib)
            return energyR;
        else
            return energyR0;
    }

    public Point3D position(boolean calib) {
        if(calib)
            return position;
        else
            return position0;
    }

    public double path(boolean calib) {
        if(calib)
            return path;
        else
            return path0;
    }

    public double time(boolean calib) {
        if(calib)
            return time;
        else
            return time0;
    }
    
    public double vertexTime(boolean calib) {
        if(calib)
            return vertexTime;
        else
            return vertexTime0;
    }
    
    public int pid() {
        if(this.charge()==0)
            return 22;
        else
            return 11;
    }
    
    private Vector3D momentum(boolean calib) {
        return position(calib).vectorFrom(FTCalConstants.VERTEX).asUnit().multiply(this.energy(calib));
    }
    
    public Particle toParticle(boolean calib) {
        Vector3D p = this.momentum(calib);
        Point3D  v = FTCalConstants.VERTEX;
        Particle particle = new Particle(this.pid(), p.x(), p.y(), p.z(), v.x(), v.y(), v.z());
        particle.setProperty("seed", this.seed());
        return particle;
    }

    public void update(CalibrationConstants energyCorrection) {
        if(!this.isEmpty()) {
            this.seed       = this.getSeed();
            this.energyR    = this.getEnergyR();
            this.position   = this.getCentroid();
            this.time       = this.getTime();
            this.energy     = this.getEnergyCorr(energyCorrection);
            this.path       = this.getPath();
            this.vertexTime = this.time -this.path/PhysicsConstants.speedOfLight();
        }
    }
    
    private double getEnergyR() {
        double energy = 0;
        for(int i=0; i<this.size(); i++) {
            energy += this.get(i).energy(true);
        }
        return energy;
    }

    private double getEnergyCorr(CalibrationConstants energyCorrection) {
        if(energyCorrection != null) {
            double energyCorr = (energyCorrection.getDoubleValue("c0",1,1,seed)
                              +  energyCorrection.getDoubleValue("c1",1,1,seed)*energyR
                              +  energyCorrection.getDoubleValue("c2",1,1,seed)*energyR*energyR
                              +  energyCorrection.getDoubleValue("c3",1,1,seed)*energyR*energyR*energyR
                              +  energyCorrection.getDoubleValue("c4",1,1,seed)*energyR*energyR*energyR*energyR
                                )*1E-3;
            return energyR + energyCorr;
        }
        else
            return energy;
    }
    
    private double getPath() {
        return this.position.distance(FTCalConstants.VERTEX);
    }

    private int getSeed() {
        if(!this.isEmpty()){
            Collections.sort(this);
            return this.get(0).component();
        }
        else {
            return 0;
        }
    }

    private double getTime() {
        // returns energy weighted time 
        double clusterTime    = 0;
        for(int i=0; i<this.size(); i++) {
            FTCalHit hit = this.get(i);
            clusterTime += hit.energy(true)*hit.time(true);              
        }
        clusterTime /= this.energyR;
        return clusterTime;
    }
        
    private Point3D getCentroid() {
        double energy = this.energyR;
        double wtot = 0;
        double x    = 0;
        double y    = 0;
        double z    = 0;
        for(int i=0; i<this.size(); i++) {
            FTCalHit hit = this.get(i);
            // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//				double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
            double wi = Math.max(0., (3.45+Math.log(hit.energy(true)/energy)));
            Point3D position = hit.position();
            wtot     += wi;
            x += wi*position.x();
            y += wi*position.y();
            z += wi*position.z();
        }
        x /= wtot;
        y /= wtot;
        z /= wtot;
        Point3D centroid  = new Point3D(x, y, z);
        return centroid;            
    }    

    @Override
    public String  toString(){
        StringBuilder str = new StringBuilder();

        str.append(String.format("Cluster: %4d\n",   this.id()));            
        str.append(String.format("\tSize: %4d",      this.size()));
        str.append(String.format("\tE: %7.3f",       this.energy(true))); 
        str.append(String.format("\tERec: %7.3f",    this.energyR(true))); 
        str.append(String.format("\tTime: %7.3f",    this.time(true))); 
        str.append(String.format("\tPath: %7.3f",    this.path(true))); 
        str.append(String.format("\tx: %7.3f",       this.position(true).x())); 
        str.append(String.format("\ty: %7.3f",       this.position(true).y())); 
        str.append(String.format("\tz: %7.3f\n",     this.position(true).z())); 
        for(int j = 0; j< this.size(); j++) {
            str.append(String.format("\thit #%d\t", j));
            str.append(this.get(j).toString());
            str.append("\n");
        }
        return str.toString();
    }
    
}
