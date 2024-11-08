package org.clas.ftdata;

import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;

/**
 *
 * @author devita
 */
public class FTCalEvent {
    
    private int run = 0;

    private List<FTCalADC> ADCs = null;
    private List<FTCalHit> hits = null;
    private List<FTCalCluster> clusters = null;

    private double startTime = 0;
    private double triggerPID = 0;
    
    private Particle generated;
    
    public FTCalEvent(int run) {
        this.run = run;
    }

    public int getRun() {
        return run;
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

    public double getTriggerPID() {
        return triggerPID;
    }

    public void setTriggerPID(double triggerPID) {
        this.triggerPID = triggerPID;
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
        if(trues.size()==this.ADCs.size()) {
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
            h.setToTrue(ADCs.get(h.adcIndex()).truth());
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
