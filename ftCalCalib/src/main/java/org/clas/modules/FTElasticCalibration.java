/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTAdjustFit;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author devita
 */
public class FTElasticCalibration extends FTCalibrationModule {

    // analysis realted info
    double ebeam=5302;//2217;//10604;//6424;//
    double seed = 1370*ebeam/2217;//1370;//6980;//1370*ebeam/; 
    double seedTotRatio = 0.58;
    IndexedTable charge2energy = null;
 

    public FTElasticCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "seed:seed_error:factor:factor_err:charge2energy:",3, ccdb, gConstants);
        this.initRange(0., ebeam*1.3);
    }

    @Override
    public void resetEventListener() {

        H1F hClusterSum = new H1F("hClusterSum", 100, this.getRange()[0], this.getRange()[1]);
        hClusterSum.setTitleX("E (MeV)");
        hClusterSum.setTitleY("Counts");
        hClusterSum.setTitle("Cluster energy");
        hClusterSum.setFillColor(3);
        H1F hClusterSum_calib = new H1F("hClusterSum_calib", 100, this.getRange()[0], this.getRange()[1]);
        hClusterSum_calib.setTitleX("E (MeV)");
        hClusterSum_calib.setTitleY("counts");
        hClusterSum_calib.setTitle("Cluster energy");
        hClusterSum_calib.setFillColor(44);
        H1F hClusterSum_theta = new H1F("hClusterSum_theta", 100, this.getRange()[0], this.getRange()[1]);
        hClusterSum_theta.setTitleX("E (MeV)");
        hClusterSum_theta.setTitleY("counts");
        hClusterSum_theta.setTitle("Cluster energy");
        hClusterSum_theta.setFillColor(55);
        GraphErrors  gefactors = new GraphErrors("gefactors");
        gefactors.setTitle("Correction Factor"); //  title
        gefactors.setTitleX("Crystal ID"); // X axis title
        gefactors.setTitleY("Correction Factor");   // Y axis title
        gefactors.setMarkerColor(3); // color from 0-9 for given palette
        gefactors.setMarkerSize(5);  // size in points on the screen
//        gtoffsets.setMarkerStyle(1); // Style can be 1 or 2
        gefactors.addPoint(0., 0., 0., 0.);
        gefactors.addPoint(1., 1., 0., 0.);
        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);

            // initialize data group
            H1F hCluster = new H1F("hCluster_" + key, 100, this.getRange()[0], this.getRange()[1]);
            hCluster.setTitleX("E (MeV)");
            hCluster.setTitleY("Counts");
            hCluster.setTitle("Component " + key);
            H1F hCluster_calib = new H1F("hCluster_calib_" + key, 100, this.getRange()[0], this.getRange()[1]);
            hCluster_calib.setTitleX("E (MeV)");
            hCluster_calib.setTitleY("Counts");
            hCluster_calib.setTitle("Component " + key);
            hCluster_calib.setFillColor(44);
            H1F hCluster_theta = new H1F("hCluster_theta_" + key, 100, this.getRange()[0], this.getRange()[1]);
            hCluster_theta.setTitleX("E (MeV)");
            hCluster_theta.setTitleY("Counts");
            hCluster_theta.setTitle("Component " + key); 
            hCluster_theta.setFillColor(55);
            H1F hSeed = new H1F("hSeed_" + key, 100, this.getRange()[1]*0.3, this.getRange()[1]*0.9);
            hSeed.setTitleX("E (MeV)");
            hSeed.setTitleY("Counts");
            hSeed.setTitle("Component " + key);
            H1F hSeed_calib = new H1F("hSeed_calib_" + key, 100, this.getRange()[1]*0.3, this.getRange()[1]*0.9);
            hSeed_calib.setTitleX("E (MeV)");
            hSeed_calib.setTitleY("Counts");
            hSeed_calib.setTitle("Component " + key);
            F1D fseed = new F1D("fseed_" + key, "[amp]*gaus(x,[mean],[sigma])", this.getRange()[1]*0.3, this.getRange()[1]*0.6);
            fseed.setParameter(0, 0.0);
            fseed.setParameter(1, 0.0);
            fseed.setParameter(2, 2.0);
            fseed.setLineColor(24);
            fseed.setLineWidth(2);
            fseed.setOptStat(1111);

            DataGroup dg = new DataGroup(2, 2);
            dg.addDataSet(hClusterSum      , 0);
            dg.addDataSet(hClusterSum_calib, 0);
            dg.addDataSet(hClusterSum_theta, 0);
            dg.addDataSet(hCluster,          1);
            dg.addDataSet(hCluster_calib,    1);
            dg.addDataSet(hCluster_theta,    1);
            dg.addDataSet(hSeed,             2);
            dg.addDataSet(hSeed_calib,       2);
            dg.addDataSet(fseed,             2);
            dg.addDataSet(gefactors,         3);
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
        return this.getDataGroup().getItem(1, 1, icomp).getH1F("hCluster_" + icomp).getEntries();
    }

    @Override
    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        int run = 0;
        if(event.hasBank("RUN::config")) {
            run = event.getBank("RUN::config").getInt("run", 0);            
        }
        else {
            return;
        }
        if(this.getConstantsManager()!=null) charge2energy = this.getConstantsManager().getConstants(run, "/calibration/ft/ftcal/charge_to_energy");
//        if (event.hasBank("FTCAL::adc")) {
//            DataBank adcFTCAL     = event.getBank("FTCAL::adc");
//            for(int k=0; k<adcFTCAL.rows(); k++) {
//                    int component  = adcFTCAL.getInt("component",k);
//                    double energyK = (double) adcFTCAL.getInt("ADC",k)*charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
//						                      *charge2energy.getDoubleValue("mips_energy", 1,1,component)
//						                      /charge2energy.getDoubleValue("mips_charge", 1,1,component);
//                    if(energyK>500) this.getDataGroup().getItem(1, 1, component).getH1F("hSeed_" + component).fill(energyK);                                              }
//        }
        if (event.hasBank("FTCAL::clusters") && event.hasBank("FTCAL::hits") && event.hasBank("FTCAL::adc")) {
            ArrayList<Particle> ftParticles = new ArrayList();
            DataBank clusterFTCAL = event.getBank("FTCAL::clusters");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            DataBank hitFTCAL     = event.getBank("FTCAL::hits");
            DataBank adcFTCAL     = event.getBank("FTCAL::adc");
            for (int loop = 0; loop < clusterFTCAL.rows(); loop++) {
                int    cluster = getDetector().getComponent(clusterFTCAL.getFloat("x", loop), clusterFTCAL.getFloat("y", loop));
                int    id      = clusterFTCAL.getShort("id", loop);
                int    size    = clusterFTCAL.getShort("size", loop);
                double x       = clusterFTCAL.getFloat("x", loop);
                double y       = clusterFTCAL.getFloat("y", loop);
                double z       = clusterFTCAL.getFloat("z", loop);
                double energy  = 1e3 *clusterFTCAL.getFloat("energy", loop);
                double energyR = 1e3 * clusterFTCAL.getFloat("recEnergy", loop);
                double maxEnergy  = 1e3 *clusterFTCAL.getFloat("maxEnergy", loop);
                double path    = Math.sqrt(x*x+y*y+z*z);
                double theta   = Math.atan(Math.sqrt(x*x+y*y)/path);
                double pela    = PhysicsConstants.massProton()*1000*ebeam/(2*ebeam*Math.pow(Math.sin(theta/2), 2)+PhysicsConstants.massProton()*1000);
                int    key = 0;
                double energySeed=0;
                double energyCalib=0;
                ArrayList<Integer> crystals = new ArrayList();
                for(int k=0; k<hitFTCAL.rows(); k++) {
                    int clusterID  = hitFTCAL.getShort("clusterID", k);
                    // select hits that belong to cluster 
                    if(clusterID == id) { 
                        int hitID = hitFTCAL.getShort("hitID", k);
                        crystals.add(hitID);
                    }                            
                }
                for(int k : crystals) {
                    int    component  = adcFTCAL.getInt("component",k);
                    double adc        = (double) adcFTCAL.getInt("ADC",k);
                    // start with nominal value, then use CCDB, then use previous iteration
                    double c2e        = (this.getConstants().LSB*this.getConstants().nsPerSample/50)*this.getConstants().eMips/this.getConstants().chargeMips;
                    if(this.getConstantsManager()!=null) c2e = 1*charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
						                *charge2energy.getDoubleValue("mips_energy", 1,1,component)
						                /charge2energy.getDoubleValue("mips_charge", 1,1,component);
                    if(this.getPreviousCalibrationTable().hasEntry(1,1,component)) {
                        c2e = charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
			     *charge2energy.getDoubleValue("mips_energy", 1,1,component)
			     /this.getPreviousCalibrationTable().getDoubleValue("charge2energy", 1, 1, component);
                    }                        
                    double energyK = adc*c2e;
                    if(energyK>energySeed) {
                        key = component;
                        energySeed = energyK;
                    }                    
                    energyCalib += energyK;
//                    System.out.println(key + " " + energyK + " " + c2e + " " + energySeed + " " + energy);
                }
                energyCalib = energyCalib+energy-energyR;
//                System.out.println(energy + " " + energyCalib);
                

                Particle recParticle = new Particle(22, energy*x/path, energy*y/path, energy*z/path, 0,0,0);
                recParticle.setProperty("key",(double) key);
                recParticle.setProperty("energySeed",energySeed);
                recParticle.setProperty("energyCalib",energyCalib);
                recParticle.setProperty("elastic",pela);
                double ratio = this.seedTotRatio;
                if(this.isThisCrystalOnTheEdge(key)) ratio=0.65;
                if(energyR>this.getConstants().clusterThr && 
                   size>this.getConstants().clusterSize && 
                   energySeed>ratio*energyCalib &&     
                   key==cluster && 
                   energyCalib>3000) ftParticles.add(recParticle);
//                System.out.println(energyR + " " + size + " " + pela + " " + ftParticles.size());
            }
            if(ftParticles.size()>0) {
                for (int i = 0; i < ftParticles.size(); i++) {
                    int key = (int) ftParticles.get(i).getProperty("key");
                    Particle partElectron = ftParticles.get(i);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum").fill(partElectron.p());
                    this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum_theta").fill(ftParticles.get(i).getProperty("elastic"),0.1);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_" + key).fill(partElectron.p());
                    this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_theta_" + key).fill(ftParticles.get(i).getProperty("elastic"),0.1);
                    if(ftParticles.get(i).getProperty("energyCalib")>0) {
                        this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum_calib").fill(ftParticles.get(i).getProperty("energyCalib"));                        
                        this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_calib_" + key).fill(ftParticles.get(i).getProperty("energyCalib"));
                    }
                    this.getDataGroup().getItem(1, 1, key).getH1F("hSeed_" + key).fill(ftParticles.get(i).getProperty("energySeed"));
//                    System.out.println(" cluster " + ftParticles.get(i).getProperty("elastic"));
                }
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hseed = this.getDataGroup().getItem(1,1,key).getH1F("hSeed_" + key);
            F1D fseed = this.getDataGroup().getItem(1,1,key).getF1D("fseed_" + key);
            this.initSeedGaussFitPar(fseed,hseed);
            DataFitter.fit(fseed,hseed,"LQ");
            hseed.setFunction(null);
        }
    }


    @Override
    public void adjustFit() {
        System.out.println("Adjusting fit for component " + this.getSelectedKey());
        H1F hseed = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getH1F("hSeed_" + this.getSelectedKey());
        F1D fseed = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getF1D("fseed_" + this.getSelectedKey());
        FTAdjustFit cfit = new FTAdjustFit(hseed, fseed, "LRQ");
        this.getCanvas().update();
    }

    private void initSeedGaussFitPar(F1D fseed, H1F hseed) {
        double hAmp  = hseed.getBinContent(hseed.getMaximumBin());
        double hMean = hseed.getAxis().getBinCenter(hseed.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.01*hMean)); 
        double rangeMax = (hMean + (0.80*hMean));  
        double pm = hMean*0.2;
        fseed.setRange(rangeMin, rangeMax);
        fseed.setParameter(0, hAmp);
        fseed.setParLimits(0, hAmp*0.8, hAmp*1.2);
        fseed.setParameter(1, hMean);
        fseed.setParLimits(1, hMean-pm, hMean+pm);
        fseed.setParameter(2, pm);
        fseed.setParLimits(2, 10., 500.);
//        System.out.println(fseed.getRange().getMin() + " " + fseed.getRange().getMin() + " " + rangeMin + " " + rangeMax);
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
    
    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 2);
    }
    
    @Override
    public void timerUpdate() {
        this.analyze();
        this.updateTable();
    }
    
    @Override
    public void updateTable() {
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gefactors").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hseed = this.getDataGroup().getItem(1,1,key).getH1F("hSeed_" + key);
            F1D fseed = this.getDataGroup().getItem(1,1,key).getF1D("fseed_" + key);

            double seedE       = 0;
            double seedE_err   = 0;
            double factorE     = 0;
            double factorE_err = 0;
            double c2e         = this.getConstants().chargeMips;
            if(this.getConstantsManager()!=null) c2e = charge2energy.getDoubleValue("mips_charge", 1,1,key);
                if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                    c2e = this.getPreviousCalibrationTable().getDoubleValue("charge2energy", 1, 1, key);
                }                        
                    
             if(hseed.getEntries()>1000 && fseed.getParameter(1)>500) {
                seedE       = fseed.getParameter(1);
                seedE_err   = fseed.parameter(1).error();
                factorE     = this.seed/fseed.getParameter(1);
                factorE_err = factorE*seedE_err/seedE;
                c2e         = c2e/factorE;
            }
            else {
                c2e         = 4.0;
            }
            this.getDataGroup().getItem(1,1,key).getGraph("gefactors").addPoint(key, factorE, 0, factorE_err);
            getCalibrationTable().setDoubleValue(seedE,       "seed",            1, 1, key);
            getCalibrationTable().setDoubleValue(seedE_err,   "seed_error",      1, 1, key);
            getCalibrationTable().setDoubleValue(factorE,     "factor",          1, 1, key);
            getCalibrationTable().setDoubleValue(factorE_err, "factor_err" ,     1, 1, key);
            getCalibrationTable().setDoubleValue(c2e,         "charge2energy" ,  1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }
    
        public boolean isThisCrystalOnTheEdge(int id) {

        boolean crystalEdge=false;
        int iy = id / 22;
        int ix = id - iy * 22;

        double xcrystal = 1.0 * (22 - ix - 0.5);
        double ycrystal = 1.0 * (22 - iy - 0.5);
        double rcrystal = Math.sqrt(Math.pow(xcrystal - 1.0 * 11, 2.0) + Math.pow(ycrystal - 1.0 * 11, 2.0));
        if (rcrystal < 1.0 * 4.8 || rcrystal >1.0 * 10.11) {
            crystalEdge=true;
        }
        return crystalEdge;
    }
    
}
