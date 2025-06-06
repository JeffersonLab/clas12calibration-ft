package org.clas.ftdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clas.viewer.FTCalDetector;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class FTCalDataProvider {
      
    private FTCalDetector detector;
    
    private CalibrationConstants charge2energy = null;
    private CalibrationConstants energycorr    = null;
    private CalibrationConstants timeoffsets   = null;
    private CalibrationConstants timewalk      = null;
    
    private boolean useMCTrue = false;
    
    public FTCalDataProvider(FTCalDetector detector, boolean mcTrue) {
        
        this.detector   = detector;
        this.useMCTrue  = mcTrue;
    }
    
    public void loadConstants(Map<String, CalibrationConstants> constants) {
        for(String key : constants.keySet()) {
            if(constants.get(key).getRowCount()==0) continue;
            if(key.equals("EnergyCalibration") && charge2energy == null)
                    charge2energy = constants.get(key);
            else if(key.equals("EnergyCorrection") && energycorr == null)
                    energycorr    = constants.get(key);
            else if(key.equals("TimeCalibration") && timeoffsets == null)
                    timeoffsets   = constants.get(key);
            else if(key.equals("TimeWalk") && timewalk == null)
                    timewalk      = constants.get(key);
        }
    }
    
    public FTCalEvent getEvent(DataEvent event) {    
        if(event.hasBank("RUN::config")) {
            int run = event.getBank("RUN::config").getInt("run", 0);            
            long trigger = event.getBank("RUN::config").getLong("trigger", 0);
            double torus = event.getBank("RUN::config").getFloat("torus", 0);
            
            FTCalEvent ftEvent = new FTCalEvent(run);
            ftEvent.setTrigger(trigger);
            ftEvent.setTorus(torus);
            
            if(event.hasBank("FTCAL::adc")) {
                ftEvent.setADCs(this.readADCs(event.getBank("FTCAL::adc")));
                if(event.hasBank("MC::True"))
                    ftEvent.addTruesToADCs(this.readMCTrueInfo(event.getBank("MC::True")));
            }
            
            if(event.hasBank("FTCAL::hits"))
                ftEvent.setHits(this.readHits(event.getBank("FTCAL::hits")));
            
            if(event.hasBank("FTCAL::clusters") && event.hasBank("FT::particles"));
                ftEvent.setClusters(this.readClusters(event.getBank("FTCAL::clusters"),event.getBank("FT::particles")));
            
            ftEvent.linkHitsToADCs();
            ftEvent.updateHits(charge2energy, timeoffsets, timewalk);
            if(this.useMCTrue) ftEvent.seHitsToTrue();
                
            ftEvent.linkHitsToClusters();
            ftEvent.updateClusters(energycorr);
    
            if(event.hasBank("REC::Event") && event.hasBank("REC::Particle")) {
                DataBank recEvent = event.getBank("REC::Event");
                DataBank recPart  = event.getBank("REC::Particle");
                ftEvent.setStartTime(recEvent.getFloat("startTime", 0));
                if(recPart.getShort("status", 0)<-2000) {
                    Particle part = new Particle(
                            recPart.getInt("pid", 0),
                            recPart.getFloat("px", 0),
                            recPart.getFloat("py", 0),
                            recPart.getFloat("pz", 0),
                            recPart.getFloat("vx", 0),
                            recPart.getFloat("vy", 0),
                            recPart.getFloat("vz", 0));
                    if(part.p()>0) {
                        ftEvent.setTriggerPID(recPart.getInt("pid",0));
                        ftEvent.setTriggerZ(recPart.getFloat("vz",0));}
                    
                }
            }
                
            if (event.hasBank("MC::Particle")) {
                DataBank genBank = event.getBank("MC::Particle");
                for (int loop = 0; loop < genBank.rows(); loop++) {
                    Particle genPart = new Particle(
                            genBank.getInt("pid", loop),
                            genBank.getFloat("px", loop),
                            genBank.getFloat("py", loop),
                            genBank.getFloat("pz", loop),
                            genBank.getFloat("vx", loop),
                            genBank.getFloat("vy", loop),
                            genBank.getFloat("vz", loop));
                    if (genPart.pid() == 22 || genPart.pid() == 11) {
                        if (ftEvent.getGenerated() == null) {
                            ftEvent.setGenerated(genPart);
                        }
                    }
                }
            }  
            return ftEvent;
        } 
        return null;
    }
    
    
    private List<FTCalADC> readADCs(DataBank bank) {

        List<FTCalADC> adcs = new ArrayList<>();
    
        for (int loop = 0; loop < bank.rows(); loop++) {
            int    component    = bank.getInt("component", loop);
            int    adc          = bank.getInt("ADC", loop);
            double time         = bank.getFloat("time", loop); 
            int    ped          = bank.getShort("ped", loop); 
            adcs.add(new FTCalADC(component, adc, time, ped));
        }   
        return adcs;
    }    
 
    private List<FTCalHit> readHits(DataBank bank) {

        List<FTCalHit> hits = new ArrayList<>();

        for(int i=0; i<bank.rows(); i++) {
            FTCalHit hit = new FTCalHit(detector.getComponent(bank.getByte("idx", i)-1, bank.getByte("idy", i)-1),
                                        bank.getFloat("energy", i),
                                        bank.getFloat("time", i),
                                        bank.getFloat("x", i),
                                        bank.getFloat("y", i),
                                        bank.getFloat("z", i),
                                        bank.getShort("hitID", i),
                                        bank.getShort("clusterID", i));
            hits.add(hit);
        }
        return hits;
    }
    
    private List<FTCalCluster> readClusters(DataBank bank, DataBank part) {
        
        List<FTCalCluster> clusters = new ArrayList<>();

        for(int i=0; i<bank.rows(); i++) {
            FTCalCluster cluster = new FTCalCluster(bank.getShort("id", i),
                                                    part.getByte("charge", i),
                                                    bank.getFloat("energy", i),
                                                    bank.getFloat("recEnergy", i),
                                                    bank.getFloat("time", i),
                                                    bank.getFloat("x", i),
                                                    bank.getFloat("y", i),
                                                    bank.getFloat("z", i));
            clusters.add(cluster);
        }
        return clusters;
    }
    
    private List<FTCalTrue> readMCTrueInfo(DataBank bank) {
           
        List<FTCalTrue> trues = new ArrayList<>();
        
        for(int i=0; i<bank.rows(); i++) {
            if(bank.getByte("detector", i)!=DetectorType.FTCAL.getDetectorId())
                continue;
            FTCalTrue t = new FTCalTrue(bank.getFloat("avgT", i),
                                        bank.getFloat("totEdep", i)/1000,
                                        new Vector3D(bank.getFloat("px", i)/1000,
                                                     bank.getFloat("py", i)/1000,
                                                     bank.getFloat("pz", i)/1000),
                                        new Point3D(bank.getFloat("avgX", i)/10,
                                                    bank.getFloat("avgY", i)/10,
                                                    bank.getFloat("avgZ", i)/10));
            trues.add(t);
        }
        return trues;
    }
    
}
