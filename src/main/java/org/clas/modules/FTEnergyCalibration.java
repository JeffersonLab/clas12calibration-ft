/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public class FTEnergyCalibration extends FTCalibrationModule {

    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    double clusterEnergyThr = 500.0;// Vertical selection
    int    clusterSizeThr   = 9;// Vertical selection
//    double singleChThr = 0.00;// Single channel selection MeV
//    double signalThr =0.0;
//    double simSignalThr=0.00;// Threshold used for simulated events in MeV
//    double startTime   = 124.25;//ns
//    double ftcalDistance =1898; //mm
//    double timeshift =0;// ns
//    double crystal_size = 15.3;//mm
    double charge2e = 15.3/6.005; //MeV
//    double crystal_length = 200;//mm                                                                                            
//    double shower_depth = 65;                                                                                                   
//    double light_speed = 150; //cm/ns     
//    double c = 29.97; //cm/ns     

    public FTEnergyCalibration(FTDetector d, String name) {
        super(d, name, "offset:offset_error:resolution");
    }

    @Override
    public void resetEventListener() {

        H1F hpi0sum = new H1F("hpi0sum", 100,0., 300.);
        hpi0sum.setTitleX("M (MeV)");
        hpi0sum.setTitleY("Counts");
        hpi0sum.setTitle("2#gamma invariant mass");
        hpi0sum.setFillColor(3);
        H1F hpi0sum_calib = new H1F("hpi0sum_calib", 100,0., 300.);
        hpi0sum_calib.setTitleX("M (MeV)");
        hpi0sum_calib.setTitleY("counts");
        hpi0sum_calib.setTitle("2#gamma invariant mass");
        hpi0sum_calib.setFillColor(44);
        H2F hmassangle = new H2F("hmassangle", 100, 0., 300., 100, 0., 6.);
        hmassangle.setTitleX("M (MeV)");
        hmassangle.setTitleY("Angle (deg)");
        hmassangle.setTitle("Angle vs. Mass");
        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);

            // initialize data group
            H1F hpi0 = new H1F("hpi0_" + key, 100,0., 300.);
            hpi0.setTitleX("M (MeV)");
            hpi0.setTitleY("Counts");
            hpi0.setTitle("Component " + key);
            H1F hpi0_calib = new H1F("hpi0_calib_" + key, 100,0., 300.);
            hpi0_calib.setTitleX("M (MeV)");
            hpi0_calib.setTitleY("Counts");
            hpi0_calib.setTitle("Component " + key);
            H2F hcal2d = new H2F("hcal2d_" + key, 100, 0, 5000, 100, 0, 5000);
            hcal2d.setTitleX("Calculated Energy (MeV)");
            hcal2d.setTitleY("Measured Energy (MeV)");
            hcal2d.setTitle("Component " + key);
            H1F hcal = new H1F("hcal_" + key, 100, 0, 2);
            hcal.setTitleX("Correction Factor");
            hcal.setTitleY("Counts");
            hcal.setTitle("Component " + key);
            H1F htime_calib = new H1F("htime_calib_" + key, 300, -15.0, 15.0);
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
//            ftime.setLineColor(2);
//            ftime.setLineStyle(1);
            DataGroup dg = new DataGroup(3, 2);
            dg.addDataSet(hpi0sum      , 0);
            dg.addDataSet(hpi0sum_calib, 0);
            dg.addDataSet(hmassangle,    1);
            dg.addDataSet(hpi0,          3);
            dg.addDataSet(hpi0_calib,    3);
            dg.addDataSet(hcal2d,        4);
            dg.addDataSet(hcal,          5);
//            dg.addDataSet(ftime,       3);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(1, 1, icomp).getH1F("hpi0_" + icomp).getEntries();
    }

    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        if (event.hasBank("FTCAL::clusters") && event.hasBank("FTCAL::adc")) {
            ArrayList<Particle> ftParticles = new ArrayList();
            DataBank clusterFTCAL = event.getBank("FTCAL::clusters");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            DataBank adcFTCAL     = event.getBank("FTCAL::adc");
            for (int loop = 0; loop < clusterFTCAL.rows(); loop++) {
                int key = getDetector().getComponent(clusterFTCAL.getFloat("x", loop), clusterFTCAL.getFloat("y", loop));
                int    size    = clusterFTCAL.getShort("size", loop);
                double x       = clusterFTCAL.getFloat("x", loop);
                double y       = clusterFTCAL.getFloat("y", loop);
                double z       = clusterFTCAL.getFloat("z", loop);
                double energy  = 1e3 * clusterFTCAL.getFloat("energy", loop);
                double energyR = 1e3 * clusterFTCAL.getFloat("recEnergy", loop);
                double path    = Math.sqrt(x*x+y*y+z*z);
                double energySeed=0;
                for(int k=0; k<adcFTCAL.rows(); k++) {
                    double energyK = ((double) adcFTCAL.getInt("ADC", k))*(LSB*nsPerSample/50)*charge2e;
                    if(key == adcFTCAL.getInt("component", k) && energyK>energySeed) energySeed = energyK;
                }
                Particle recParticle = new Particle(22, energy*x/path, energy*y/path, energy*z/path, 0,0,0);
                recParticle.setProperty("key",(double) key);
                recParticle.setProperty("energySeed",energySeed);
                if(energyR>this.clusterEnergyThr && size>this.clusterSizeThr) ftParticles.add(recParticle);
            }
            if(ftParticles.size()>=2) {
                for (int i1 = 0; i1 < ftParticles.size(); i1++) {
                    for (int i2 = i1 + 1; i2 < ftParticles.size(); i2++) {
                        int key1 = (int) ftParticles.get(i1).getProperty("key");
                        int key2 = (int) ftParticles.get(i2).getProperty("key");
                        Particle partGamma1 = ftParticles.get(i1);
                        Particle partGamma2 = ftParticles.get(i2);
                        Particle partPi0 = new Particle();
                        partPi0.copy(partGamma1);
                        partPi0.combine(partGamma2, +1);
                        double invmass = Math.sqrt(partPi0.mass2());
                        double x = (partGamma1.p() - partGamma2.p()) / (partGamma1.p() + partGamma2.p());
                        double angle = Math.toDegrees(Math.acos(partGamma1.cosTheta(partGamma2)));
                        this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0sum").fill(invmass);
                        this.getDataGroup().getItem(1, 1, key1).getH2F("hmassangle").fill(invmass, angle);
                        this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0sum").fill(invmass);
                        this.getDataGroup().getItem(1, 1, key2).getH2F("hmassangle").fill(invmass, angle);
                        if(angle>1.) {
                            double ecal1 = Math.pow(PhysicsConstants.massPionNeutral()*1.0E3,2)/(2*partGamma2.p()*(1-Math.cos(Math.toRadians(angle))))-(partGamma1.p()-partGamma1.getProperty("energySeed"));
                            double ecal2 = Math.pow(PhysicsConstants.massPionNeutral()*1.0E3,2)/(2*partGamma1.p()*(1-Math.cos(Math.toRadians(angle))))-(partGamma2.p()-partGamma2.getProperty("energySeed"));
//                            ecal1=partPi0.mass2()/(2*partGamma1.p()*partGamma2.p()*(1-partGamma1.cosTheta(partGamma2)));
//                            System.out.println(ecal1 + " " + partGamma1.p()+ " " + partGamma1.getProperty("energySeed"));
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0_" + key1).fill(invmass);
                            this.getDataGroup().getItem(1, 1, key1).getH2F("hcal2d_" + key1).fill(ecal1,partGamma1.getProperty("energySeed"));
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hcal_" + key1).fill(partGamma1.getProperty("energySeed")/ecal1);
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0_" + key2).fill(invmass);
                            this.getDataGroup().getItem(1, 1, key2).getH2F("hcal2d_" + key2).fill(ecal2,partGamma2.getProperty("energySeed"));
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hcal_" + key2).fill(partGamma2.getProperty("energySeed")/ecal2);
                        }
                    }
                }
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");
//        for (int key : this.getDetector().getDetectorComponents()) {
//            this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").reset();
//        }
//        for (int key : this.getDetector().getDetectorComponents()) {
//            H1F htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_" + key);
//            F1D ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_" + key);
//            this.initTimeGaussFitPar(ftime,htime);
//            DataFitter.fit(ftime,htime,"LQ");
//            
//            this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").addPoint(key, ftime.getParameter(1), 0, ftime.parameter(1).error());
//        
//            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(1),      "offset",       1, 1, key);
//            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(1).error(), "offset_error", 1, 1, key);
//            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(2),      "resolution" ,  1, 1, key);
//        }
//        getCalibrationTable().fireTableDataChanged();
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.8*hRMS)); 
        double rangeMax = (hMean + (0.2*hRMS));  
        double pm = (hMean*3.)/100.0;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+(pm));
        ftime.setParameter(2, 0.2);
        ftime.setParLimits(2, 0.1*hRMS, 0.8*hRMS);
    }    

    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        if (this.getDetector().hasComponent(key)) {
            int nent = this.getNEvents(sector, layer, key);
            if (nent > 0) {
                col = palette.getColor3D(nent, this.getnProcessed(), true);
            }
        }
//        col = new Color(100, 0, 0);
        return col;
    }
    
//    @Override
//    public void processShape(DetectorShape2D dsd) {
//        // plot histos for the specific component
//        int sector = dsd.getDescriptor().getSector();
//        int layer  = dsd.getDescriptor().getLayer();
//        int paddle = dsd.getDescriptor().getComponent();
//        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
//        IndexedList<DataGroup> group = this.getDataGroup();        
//        
//        if(group.hasItem(sector,layer,paddle)==true){
//            this.getCanvas().clear();
//            this.getCanvas().divide(2, 2);
//            this.getCanvas().cd(0);
//            this.getCanvas().draw(this.getDataGroup().getItem(1,1,paddle).getH1F("htsum"));
//            this.getCanvas().cd(1);
//            this.getCanvas().draw(this.getDataGroup().getItem(1,1,paddle).getGraph("gtoffsets"));
//            this.getCanvas().cd(2);
//            this.getCanvas().draw(this.getDataGroup().getItem(1,1,paddle).getH1F("htime_wide_" + paddle));
//            this.getCanvas().cd(3);
//            this.getCanvas().draw(this.getDataGroup().getItem(1,1,paddle).getH1F("htime_" + paddle));
//            this.getCanvas().draw(this.getDataGroup().getItem(1,1,paddle).getF1D("ftime_" + paddle),"same");
////            this.getCanvas().draw(this.getDataGroup().getItem(sector,layer,paddle));
////            this.getCanvas().update();
//        } else {
//            System.out.println(" ERROR: can not find the data group");
//        }
         
//    }
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
