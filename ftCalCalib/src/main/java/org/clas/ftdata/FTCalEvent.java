package org.clas.ftdata;

import java.util.List;
import org.clas.viewer.FTCalConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author devita
 */
public class FTCalEvent {
    
    private int run = 0;
    private long trigger = 0;
    private double torus = 0;
    
    private List<FTCalADC> ADCs = null;
    private List<FTCalHit> hits = null;
    private List<FTCalCluster> clusters = null;

    private double startTime = 0;
    private int    triggerPID = 0;
    private double triggerZ = FTCalConstants.Z0;
    
    private Particle generated;
    
    public FTCalEvent(int run) {
        this.run = run;
    }

    public int getRun() {
        return run;
    }

    public boolean isTriggerBitSet(int bit) {
        long mask = (1L << bit);
        return (this.trigger & mask) != 0L;
    }

    public long getTrigger() {
        return trigger;
    }

    public void setTrigger(long trigger) {
        this.trigger = trigger;
    }

    public void setTorus(double torus) {
        this.torus = torus;
    }

    public List<FTCalADC> getADCs() {
        return ADCs;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public int getTriggerPID() {
        return triggerPID;
    }

    public void setTriggerPID(int triggerPID) {
        this.triggerPID = triggerPID;
    }

    public double getTriggerZ() {
        return triggerZ;
    }

    public void setTriggerZ(double z) {
        this.triggerZ = z;
    }

    public Point3D getVertex() {
        return FTCalConstants.VERTEXMODE ? new Point3D(0,0,triggerZ) : FTCalConstants.TARGET;
    }

    public boolean isGoodTriggerParticle() {
        return Math.abs(triggerZ-FTCalConstants.TARGET.z())<FTCalConstants.ZLENGTH &&
               (triggerPID*Math.signum(torus)==-11 || triggerPID*Math.signum(torus)==211  || triggerPID*Math.signum(torus)==-211);
    }
    
    public Particle getGenerated() {
        return generated;
    }

    public void setGenerated(Particle generated) {
        this.generated = generated;
    }

    public void setADCs(List<FTCalADC> ADCs) {
        this.ADCs = ADCs;
    }

    public List<FTCalHit> getHits() {
        return hits;
    }

    public void setHits(List<FTCalHit> hits) {
        this.hits = hits;
    }

    public List<FTCalCluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<FTCalCluster> clusters) {
        this.clusters = clusters;
    }
    
    public void addTruesToADCs(List<FTCalTrue> trues) {
        if(trues.size()<=this.ADCs.size()) {
            for(int i=0; i<this.ADCs.size(); i++)
                this.ADCs.get(i).addTrue(trues.get(i));
        }
    }
    
    public void linkHitsToADCs() {

        if(ADCs.isEmpty() || hits.isEmpty())
            return;
                
        for(FTCalHit h : hits) {
            int id = h.adcIndex();
            h.charge(ADCs.get(id).charge());
            h.rawTime(ADCs.get(id).time());
        }        
    }
    
    public void linkHitsToClusters() {
        
        if(clusters.isEmpty() || hits.isEmpty())
            return;
        
        for(FTCalCluster c : clusters) c.clear();
        
        for(FTCalCluster c : clusters) {
            for(FTCalHit h : hits) {
                if(c.id() == h.clusterIndex())
                    c.add(h);
            }
        }        
    }
    
    public void updateHits(CalibrationConstants charge2energy, CalibrationConstants timeoffsets, CalibrationConstants timewalk) {
        for(FTCalHit h : hits) {
//            System.out.println(h.toString());
            h.update(charge2energy, timeoffsets, timewalk);
//            System.out.println(h.toString());
        }
    }

    public void seHitsToTrue() {
        for(FTCalHit h : hits) {
                h.setToTrue(ADCs.get(h.adcIndex()).trueInfo());
        }
    }

    public void updateClusters(CalibrationConstants energycorr) {
        for(FTCalCluster c : clusters) {
//            System.out.println(c.toString());
            c.update(energycorr);
//            System.out.println(c.toString());
        }
    }

    
}
