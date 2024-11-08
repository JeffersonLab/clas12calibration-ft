package org.clas.ftdata;

import org.clas.viewer.FTCalConstants;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author devita
 */
public class FTCalHit implements Comparable<FTCalHit> {
    
    private int     component;
    private double  charge;
    private double  rawTime;
    private Point3D position;
    private double  path;
    private int     adcId;
    private int     clusterId;

    // original values
    private double  energy0;
    private double  time0;
    private double  chargeToEnergy0;
    private double  tOffset0;
    private double  tWalk0;

    // calibrated values
    private double  energy;
    private double  time;
    private double  chargeToEnergy;
    private double  tOffset;
    private double  tWalk;

    public FTCalHit(int component, double energy, double time, double x, double y, double z, int adcId, int clusterId) {
        this.component = component;
        this.energy0   = energy;
        this.time0     = time;
        this.energy    = energy;
        this.time      = time;
        this.position  = new Point3D(x, y, z);
        this.path      = this.position.distance(FTCalConstants.VERTEX);
        this.adcId     = adcId;
        this.clusterId = clusterId;
    }

    public int component() {
        return component;
    }

    public double charge() {
        return charge;
    }

    public void charge(double charge) {
        this.charge = charge;
        if(this.charge>0) {
            this.chargeToEnergy  = this.energy/this.charge;
            this.chargeToEnergy0 = this.energy0/this.charge;
        }
    }

    public double rawTime() {
        return rawTime;
    }

    public void rawTime(double rawTime) {
        this.rawTime = rawTime;
    }

    public Point3D position() {
        return position;
    }

    public double path() {
        return path;
    }

    public int adcIndex() {
        return adcId;
    }

    public int clusterIndex() {
        return clusterId;
    }
    
    public double energy(boolean calib) {
        if(calib) 
            return energy;
        else
            return energy0;
    }

    public double time(boolean calib) {
        if(calib)
            return time;
        else
            return time0;
    }

    public double getZeroOffsetTime(boolean calib) {
        return this.time+this.tOffset;
    }

    public double getZeroTwTime(boolean calib) {
        return this.time+this.tWalk;
    }

    public void updateEnergy(CalibrationConstants charge2energy) {
        if(charge2energy!=null) {
            double c2e = 1*FTCalConstants.EMIPS*1E-3
	    	          /charge2energy.getDoubleValue("mips_charge",    1,1,component);    
            chargeToEnergy = c2e;
            energy = charge*c2e;
        }
    }
    
    public void updateTime(CalibrationConstants timeoffsets, CalibrationConstants timewalk) {
        if(timeoffsets!=null) 
            tOffset = timeoffsets.getDoubleValue("offset", 1,1,component);
        if(timewalk!=null) {
            double amp = timewalk.getDoubleValue("A", 1,1,component);
            double lam = timewalk.getDoubleValue("L", 1,1,component);
            tWalk = amp*Math.exp(-charge*lam);
        }
        time = rawTime -(FTCalConstants.CRYSTALLENGTH-FTCalConstants.SHOWERDEPTH)/FTCalConstants.LIGHTSPEED - tOffset - tWalk;
    }

    
    @Override
    public int compareTo(FTCalHit arg0) {
        if(this.energy(true)<arg0.energy(true)) {
                return 1;
        } else {
                return -1;
        }
    }
    
    public void update(CalibrationConstants charge2energy, CalibrationConstants timeoffsets, CalibrationConstants timewalk) {
        this.updateEnergy(charge2energy);
        this.updateTime(timeoffsets, timewalk);
    }

    public void setToTrue(FTCalTrue t) {
        if(t!=null) {
//            System.out.println(energy + " " + t.energy());
//            System.out.println(time + " " + t.time());
            this.energy   = t.energy();
            this.time     = t.time();
            this.position = t.position();
        }
        else {
            this.energy = 0;
        }
    }
    
    @Override
    public String toString() {
        String s = String.format("Hit component: %d  energy: %7.3f  charge: %7.3f  time: %7.3f  rawtime: %7.3f  toffset: %7.3f  twalk: %7.3f  path: %7.3f  adcIndex: %d  clusterId: %d",
                   this.component(), this.energy(true), this.charge(), this.time(true), this.rawTime(), this.tOffset, this.tWalk, this.path(), this.adcIndex(), this.clusterIndex());
        return s;
    }
    
}
