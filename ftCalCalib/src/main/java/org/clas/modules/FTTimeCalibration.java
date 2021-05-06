package org.clas.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H2F;

/**
 *
 * @author devita
 */
public class FTTimeCalibration extends FTCalibrationModule {   
    

    
    public FTTimeCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "offset:offset_error:delta:resolution",3, ccdb, gConstants);
        this.getCalibrationTable().addConstraint(5, -0.1, 0.1);
        this.setRange(30.,40.);
        this.setRange(-10.,0.);
        this.setCols(-2, 2);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 275, -450.0, 100.0);
        htsum.setTitleX("Time Offset (ns)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Global Time Offset");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 300, -6.0, 6.0);
        htsum_calib.setTitleX("Time Offset (ns)");
        htsum_calib.setTitleY("Counts");
        htsum_calib.setTitle("Global Time Offset");
        htsum_calib.setFillColor(44);
        H1F htsum_cluster = new H1F("htsum_cluster" , 400, -2., 2.);
        htsum_cluster.setTitleX("Time (ns)");
        htsum_cluster.setTitleY("Counts");
        htsum_cluster.setTitle("Cluster Time");
        htsum_cluster.setFillColor(3);
        htsum_cluster.setLineColor(1);
        F1D fcluster = new F1D("fcluster", "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
        fcluster.setParameter(0, 0.0);
        fcluster.setParameter(1, 0.0);
        fcluster.setParameter(2, 2.0);
        fcluster.setLineWidth(2);
        fcluster.setOptStat("1111");
        H2F h2d_cluster = new H2F("h2d_cluster" , 100, 0., 12., 100, -2., 2.);
        h2d_cluster.setTitleX("Energy (GeV)");
        h2d_cluster.setTitleY("Time (ns)");
        h2d_cluster.setTitle("Cluster Time");
        GraphErrors  gtoffsets = new GraphErrors("gtoffsets");
        gtoffsets.setTitle("Timing Offsets"); //  title
        gtoffsets.setTitleX("Crystal ID"); // X axis title
        gtoffsets.setTitleY("Offset (ns)");   // Y axis title
        gtoffsets.setMarkerColor(5); // color from 0-9 for given palette
        gtoffsets.setMarkerSize(2);  // size in points on the screen
        gtoffsets.addPoint(0., 0., 0., 0.);
        gtoffsets.addPoint(1., 1., 0., 0.);
        GraphErrors  gtdeltas = new GraphErrors("gtdeltas");
        gtdeltas.setTitle("#Delta Offset (ns)"); //  title
        gtdeltas.setTitleX("Crystal ID"); // X axis title
        gtdeltas.setTitleY("#Delta Offset (ns)");   // Y axis title
        gtdeltas.setMarkerColor(3); // color from 0-9 for given palette
        gtdeltas.setMarkerSize(2);  // size in points on the screen
        gtdeltas.addPoint(0., 0., 0., 0.);
        gtdeltas.addPoint(1., 1., 0., 0.);
        H1F htoffsets = new H1F("htoffsets", 100, -2, 2);
        htoffsets.setTitle("Hit Time");
        htoffsets.setTitleX("#DeltaT (ns)");
        htoffsets.setTitleY("Counts");
        htoffsets.setFillColor(23);
        htoffsets.setLineColor(3);
        htoffsets.setOptStat("1111");

        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);
            this.getCalibrationTable().setDoubleValue(0.0, "offset",   1,1,key);

            // initialize data group
            H1F htime_wide = new H1F("htime_wide_" + key, 275, -400.0, 150.0);
            htime_wide.setTitleX("Time (ns)");
            htime_wide.setTitleY("Counts");
            htime_wide.setTitle("Component " + key);
            H1F htime = new H1F("htime_" + key, 500, this.getRange()[0], this.getRange()[1]);
            htime.setTitleX("Time (ns)");
            htime.setTitleY("Counts");
            htime.setTitle("Component " + key);
            H1F htime_calib = new H1F("htime_calib_" + key, 200, -4., 4.);
            htime_calib.setTitleX("Time (ns)");
            htime_calib.setTitleY("Counts");
            htime_calib.setTitle("Component " + key);
            htime_calib.setFillColor(44);
            htime_calib.setLineColor(24);
            F1D ftime = new F1D("ftime_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            ftime.setParameter(0, 0.0);
            ftime.setParameter(1, 0.0);
            ftime.setParameter(2, 2.0);
            ftime.setLineColor(24);
            ftime.setLineWidth(2);
            ftime.setOptStat("1111");
            F1D ftime_calib = new F1D("ftime_calib_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            ftime_calib.setParameter(0, 0.0);
            ftime_calib.setParameter(1, 0.0);
            ftime_calib.setParameter(2, 2.0);
            ftime_calib.setLineColor(24);
            ftime_calib.setLineWidth(2);
            ftime_calib.setOptStat("1111");
//            ftime.setLineColor(2);
//            ftime.setLineStyle(1);
            H1F htseed = new H1F("htseed_" + key, 200, -2., 2.);
            htseed.setTitleX("Time (ns)");
            htseed.setTitleY("Counts");
            htseed.setTitle("Cluster Time");
            htseed.setFillColor(3);
            htseed.setLineColor(1);
            F1D fseed = new F1D("fseed_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            fseed.setParameter(0, 0.0);
            fseed.setParameter(1, 0.0);
            fseed.setParameter(2, 2.0);
            fseed.setLineWidth(2);
            fseed.setOptStat("1111");
            DataGroup dg = new DataGroup(4, 2);
            dg.addDataSet(htsum,         0);
            dg.addDataSet(htsum_calib,   1);
            dg.addDataSet(htoffsets,     2);
            dg.addDataSet(htsum_cluster, 3);
            dg.addDataSet(fcluster,      3);
//            dg.addDataSet(htime_wide,    4);
            dg.addDataSet(htime,         4);
            dg.addDataSet(ftime,         4);
            dg.addDataSet(htime_calib,   5);
            dg.addDataSet(ftime_calib,   5);
//            dg.addDataSet(gtoffsets,     6);
            dg.addDataSet(htseed,        6);
            dg.addDataSet(fseed,         6);
            dg.addDataSet(h2d_cluster,   7);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("htime_" + key).getIntegral();
    }

    @Override
    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        double startTime = -100000;
        int   triggerPID = 0;
        // get start time
        if(event.hasBank("REC::Event") && event.hasBank("REC::Particle")) {
            DataBank recEvent = event.getBank("REC::Event");
            DataBank recPart = event.getBank("REC::Particle");
            startTime  = recEvent.getFloat("startTime", 0);
            if(recPart.getShort("status", 0)<-2000) 
                triggerPID = recPart.getInt("pid",0);
        }
        // loop over FTCAL reconstructed cluster
        if (event.hasBank("FT::particles") && event.hasBank("FTCAL::clusters") && event.hasBank("FTCAL::hits") && event.hasBank("FTCAL::adc")) {
            ArrayList<Particle> ftParticles = new ArrayList();
            DataBank particlesFT  = event.getBank("FT::particles");
            DataBank clusterFTCAL = event.getBank("FTCAL::clusters");
            DataBank hitFTCAL     = event.getBank("FTCAL::hits");
            DataBank adcFTCAL     = event.getBank("FTCAL::adc");
            // start from clusters
            for (int loop = 0; loop < clusterFTCAL.rows(); loop++) {
                int    cluster = getDetector().getComponent(clusterFTCAL.getFloat("x", loop), clusterFTCAL.getFloat("y", loop));
                int    id      = clusterFTCAL.getShort("id", loop);
                int    size    = clusterFTCAL.getShort("size", loop);
                int    charge  = particlesFT.getByte("charge", loop);
                double x       = clusterFTCAL.getFloat("x", loop);
                double y       = clusterFTCAL.getFloat("y", loop);
                double z       = clusterFTCAL.getFloat("z", loop);
                double time    = clusterFTCAL.getFloat("time", loop);
                double energy  = clusterFTCAL.getFloat("energy", loop);
                double energyR = clusterFTCAL.getFloat("recEnergy", loop);
                double path     = Math.sqrt(x*x+y*y+z*z);
                double theta = Math.toDegrees(Math.acos(z/path));
                // find hits associated to clusters
                if(energy>0.5 && energyR>0.3 && size > 3 && charge==0) {                            
                    double clusterTime = 0;
                    double seedTime = 0;
                    for(int k=0; k<hitFTCAL.rows(); k++) {
                        int clusterID  = hitFTCAL.getShort("clusterID", k);
                        // select hits that belong to cluster 
                        if(clusterID == id) { 
                            int    hitID     = hitFTCAL.getShort("hitID", k);
                            double hitEnergy = hitFTCAL.getFloat("energy", k);
                            int component    = adcFTCAL.getInt("component",hitID);
                            double hitTime   = adcFTCAL.getFloat("time", hitID);   
                            double hitCharge =((double) adcFTCAL.getInt("ADC", hitID))*(this.getConstants().LSB*this.getConstants().nsPerSample/50);
                            double radius    = Math.sqrt(Math.pow(this.getDetector().getIdX(component)-0.5,2.0)+Math.pow(this.getDetector().getIdY(component)-0.5,2.0))*this.getConstants().crystal_size;//meters
                            double hitPath   = Math.sqrt(Math.pow(this.getConstants().crystal_distance+this.getConstants().shower_depth,2)+Math.pow(radius,2));
                            double tof       = (hitPath/PhysicsConstants.speedOfLight()); //ns
                            double twalk  = 0;
                            double offset = 0;
                            if(this.getGlobalCalibration().containsKey("TimeWalk")) {
                                double amp = this.getGlobalCalibration().get("TimeWalk").getDoubleValue("A", 1,1,component);
                                double lam = this.getGlobalCalibration().get("TimeWalk").getDoubleValue("L", 1,1,component);
                                twalk = amp*Math.exp(-hitCharge*lam);
                            }
                            if(this.getPreviousCalibrationTable().hasEntry(1,1,component)) {
                                offset = this.getPreviousCalibrationTable().getDoubleValue("offset", 1, 1, component);
                            }
                            hitTime = hitTime - (this.getConstants().crystal_length-this.getConstants().shower_depth)/this.getConstants().light_speed - twalk - offset;
                            clusterTime += hitTime*hitEnergy;
                            double timec = (adcFTCAL.getFloat("time", hitID) -(startTime + (this.getConstants().crystal_length-this.getConstants().shower_depth)/this.getConstants().light_speed + tof));
    //                        System.out.println(component + " " + hitEnergy + " " + hitFTCAL.getFloat("time", k) + " " + hitTime);
                            this.getDataGroup().getItem(1,1,component).getH1F("htsum").fill(timec-twalk);
//                            this.getDataGroup().getItem(1,1,component).getH1F("htime_wide_"+component).fill(timec-twalk);
                            this.getDataGroup().getItem(1,1,component).getH1F("htime_"+component).fill(timec-twalk);
                            this.getDataGroup().getItem(1,1,component).getH1F("htsum_calib").fill(timec-twalk-offset);
                            this.getDataGroup().getItem(1,1,component).getH1F("htime_calib_"+component).fill(timec-twalk-offset); 
                            if(component==cluster) {
                                this.getDataGroup().getItem(1,1,component).getH1F("htseed_"+component).fill(timec-twalk-offset);                                
                            }
//                        System.out.println(key + " " + (time-twalk-offset-(this.getConstants().crystal_length-this.getConstants().shower_depth)/this.getConstants().light_speed) + " " + adc + " " + charge + " " + time + " " + twalk + " " + offset);
//                        if(event.hasBank("FTCAL::hits")) {event.getBank("FTCAL::adc").show();event.getBank("FTCAL::hits").show();}
                                               
                        }
                    }
                    clusterTime /= energyR;
                    if(theta>2.5 && theta<4.5) {
                        this.getDataGroup().getItem(1,1,cluster).getH1F("htsum_cluster").fill(clusterTime-path/PhysicsConstants.speedOfLight()-startTime);
                        this.getDataGroup().getItem(1,1,cluster).getH2F("h2d_cluster").fill(energy,clusterTime-path/PhysicsConstants.speedOfLight()-startTime);
    //                System.out.println(time + " " + clusterTime);
                    }
                }
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
        H1F htime = this.getDataGroup().getItem(1,1,8).getH1F("htsum_cluster");
        F1D ftime = this.getDataGroup().getItem(1,1,8).getF1D("fcluster");
        this.initTimeGaussFitPar(ftime,htime, 0.35);
        DataFitter.fit(ftime,htime,"LQ");
        for (int key : this.getDetector().getDetectorComponents()) {
//            this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("htoffsets").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_" + key);
            this.initTimeGaussFitPar(ftime,htime, 0.5);
            DataFitter.fit(ftime,htime,"LQ");

     //       this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").addPoint(key, ftime.getParameter(1), 0, ftime.parameter(1).error());
            
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_calib_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_calib_" + key);
            this.initTimeGaussFitPar(ftime,htime, 0.5);
            DataFitter.fit(ftime,htime,"LQ");
            
            double hoffset = ftime.getParameter(1);
            if(Math.abs(hoffset)<2) this.getDataGroup().getItem(1,1,key).getH1F("htoffsets").fill(hoffset);
            
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htseed_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("fseed_" + key);
            this.initTimeGaussFitPar(ftime,htime, 0.4);
            DataFitter.fit(ftime,htime,"LQ");
            
            double finalOffset = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(1);
            if(this.getDataGroup().getItem(1, 1, key).getF1D("fseed_" + key).getParameter(0)>10) {
                finalOffset = finalOffset
                            + this.getDataGroup().getItem(1, 1, key).getF1D("fseed_" + key).getParameter(1)
                            - this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1);
            }    
            getCalibrationTable().setDoubleValue(finalOffset, "offset", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(1).error(), "offset_error", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1), "delta" ,  1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(2), "resolution" ,  1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public void setCanvasBookData() {
        int[] pads = {4,5,6};
        this.getCanvasBook().setData(this.getDataGroup(), pads);   
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime, double range) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - range); 
        double rangeMax = (hMean + range);  
        double pm = (hMean*3.0)/100.0;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+(pm));
        ftime.setParameter(2, 0.2);
        ftime.setParLimits(2, 0.05*hRMS, 0.8*hRMS);
    }    

    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            return this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1);
       }
        return 0;
    }
    
    @Override
    public void drawDataGroup(int sector, int layer, int component) {
        if(this.getDataGroup().hasItem(sector,layer,component)==true){
            DataGroup dataGroup = this.getDataGroup().getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().divide(4,2);
            this.getCanvas().setGridX(false);
            this.getCanvas().setGridY(false);
            this.getCanvas().cd(0);
            this.getCanvas().draw(dataGroup.getH1F("htsum"));
            this.getCanvas().cd(1);
            this.getCanvas().draw(dataGroup.getH1F("htsum_calib"));
            this.getCanvas().cd(2);
//            this.getCanvas().draw(dataGroup.getGraph("gtoffsets"));
            this.getCanvas().draw(dataGroup.getH1F("htoffsets"));
            this.getCanvas().getPad(2).getAxisY().setLog(true);
            this.getCanvas().cd(3);
            this.getCanvas().draw(dataGroup.getH1F("htsum_cluster"));
//            this.getCanvas().cd(4);
//            this.getCanvas().draw(dataGroup.getH1F("htime_wide_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(dataGroup.getH1F("htime_" + component));
            this.getCanvas().cd(5);
            this.getCanvas().draw(dataGroup.getH1F("htime_calib_" + component));
            this.getCanvas().cd(6);
//            this.getCanvas().draw(dataGroup.getGraph("gtoffsets"));
            this.getCanvas().draw(dataGroup.getH1F("htseed_" + component));
            this.getCanvas().cd(7);
            this.getCanvas().getPad(7).getAxisZ().setLog(true);
            this.getCanvas().draw(dataGroup.getH2F("h2d_cluster"));
        }
    }


    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
