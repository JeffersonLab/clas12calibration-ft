package org.clas.modules;

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
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.groot.data.GraphErrors;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTEnergyCalibration extends FTCalibrationModule {

    IndexedTable charge2energy = null;
    IndexedTable energycorr = null;
    
    public FTEnergyCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "pi0mass:pi0mass_error:factor:factor_error:charge2energy:",3, ccdb, gConstants);
        this.setCols(PhysicsConstants.massPionNeutral()*1E3*0.95, PhysicsConstants.massPionNeutral()*1E3*1.05);
        this.setReference(PhysicsConstants.massPionNeutral()*1E3);
    }

    @Override
    public void resetEventListener() {
        
        H1F hpi0sum = new H1F("hpi0sum", 200, 50., 250.);
        hpi0sum.setTitleX("M (MeV)");
        hpi0sum.setTitleY("Counts");
        hpi0sum.setTitle("2#gamma invariant mass");
        hpi0sum.setFillColor(3);
        H1F hpi0sum_calib = new H1F("hpi0sum_calib", 200, 50., 250.);
        hpi0sum_calib.setTitleX("M (MeV)");
        hpi0sum_calib.setTitleY("counts");
        hpi0sum_calib.setTitle("2#gamma invariant mass");
        hpi0sum_calib.setFillColor(44);
        F1D fpi0sum_calib = new F1D("fpi0sum_calib", "[amp]*gaus(x,[mean],[sigma])+[p0]+[p1]*x+[p2]*x*x", 110., 150.);
        fpi0sum_calib.setParameter(0,   0.0);
        fpi0sum_calib.setParameter(1, 135.0);
        fpi0sum_calib.setParameter(2,   2.0);
        fpi0sum_calib.setLineColor(24);
        fpi0sum_calib.setLineWidth(2);
        fpi0sum_calib.setOptStat("1111");
        H2F hmassangle = new H2F("hmassangle", 100, 0., 200., 100, 0., 8.);
        hmassangle.setTitleX("M (MeV)");
        hmassangle.setTitleY("Angle (deg)");
        hmassangle.setTitle("Angle vs. Mass");
        GraphErrors  gefactors = new GraphErrors("gefactors");
        gefactors.setTitle("Correction Factor"); //  title
        gefactors.setTitleX("Crystal ID"); // X axis title
        gefactors.setTitleY("Correction Factor");   // Y axis title
        gefactors.setMarkerColor(3); // color from 0-9 for given palette
        gefactors.setMarkerSize(4);  // size in points on the screen
        gefactors.addPoint(0., 0., 0., 0.);
        gefactors.addPoint(1., 1., 0., 0.);
        H1F hemass = new H1F("hemass", 100, this.getReference()-1, this.getReference()+1); 
        hemass.setTitle("2#gamma invariant mass");
        hemass.setTitleX("M (MeV)");
        hemass.setTitleY("Counts");
        hemass.setFillColor(44);
        hemass.setLineColor(24);
        hemass.setOptStat("1111");
        H1F hefactors = new H1F("hefactors", 100, 0.8, 1.2);
        hefactors.setTitleX("Correction Factor");
        hefactors.setTitleY("Counts");
        hefactors.setFillColor(42);
        hefactors.setLineColor(22);
        hefactors.setOptStat("1111");
//        F1D ffactor = new F1D("ffactor", "[amp]*gaus(x,[mean],[sigma])", 100., 170.);
//        ffactor.setParameter(0,   0.0);
//        ffactor.setParameter(1,   1.0);
//        ffactor.setParameter(2,   0.1);
//        ffactor.setLineColor(2);
//        ffactor.setLineWidth(2);
//        ffactor.setOptStat("1111");
        H1F heconstants = new H1F("heconstants", 100, 0., 8.0);
        heconstants.setTitleX("Charge2Energy");
        heconstants.setTitleY("Counts");
        heconstants.setFillColor(43);
        heconstants.setLineColor(23);
        heconstants.setOptStat("1111");

        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);

            // initialize data group
            H1F hpi0 = new H1F("hpi0_" + key, 100,50., 200.);
            hpi0.setTitleX("M (MeV)");
            hpi0.setTitleY("Counts");
            hpi0.setTitle("Component " + key);
            H1F hpi0_calib = new H1F("hpi0_calib_" + key, 100,50., 200.);
            hpi0_calib.setTitleX("M (MeV)");
            hpi0_calib.setTitleY("Counts");
            hpi0_calib.setTitle("Component " + key);
            hpi0_calib.setFillColor(44);
            F1D fpi0_calib = new F1D("fpi0_calib_" + key, "[amp]*gaus(x,[mean],[sigma])+[p0]+[p1]*x", 100., 170.);
            fpi0_calib.setParameter(0,   0.0);
            fpi0_calib.setParameter(1, 135.0);
            fpi0_calib.setParameter(2,   2.0);
            fpi0_calib.setLineColor(24);
            fpi0_calib.setLineWidth(2);
            fpi0_calib.setOptStat("1111");
            H2F hcal2d = new H2F("hcal2d_" + key, 100, 0, 5000, 100, 0, 5000);
            hcal2d.setTitleX("Calculated Energy (MeV)");
            hcal2d.setTitleY("Measured Energy (MeV)");
            hcal2d.setTitle("Component " + key);
            F1D fcal2d = new F1D("fcal2d_" + key, "x", 500., 4500.);
            fcal2d.setLineColor(2);
            fcal2d.setLineWidth(1);
            H1F hcal = new H1F("hcal_" + key, 100, 0, 2);
            hcal.setTitleX("Correction Factor");
            hcal.setTitleY("Counts");
            hcal.setTitle("Component " + key);
            F1D fcal = new F1D("fcal_" + key, "[amp]*gaus(x,[mean],[sigma])+[p0]+[p1]*x", 100., 170.);
            fcal.setParameter(0, 0.0);
            fcal.setParameter(1, 1.0);
            fcal.setParameter(2, 2.0);
            fcal.setLineColor(24);
            fcal.setLineWidth(2);
            fcal.setOptStat("1111");
            DataGroup dg = new DataGroup(4, 2);
            dg.addDataSet(hpi0sum      , 0);
            dg.addDataSet(hpi0sum_calib, 0);
            dg.addDataSet(fpi0sum_calib, 0);
            dg.addDataSet(hmassangle,    1);
            dg.addDataSet(hmassangle,    1);
            dg.addDataSet(hemass,        2);
            dg.addDataSet(hefactors,     3);
//            dg.addDataSet(ffactor,       3);
            dg.addDataSet(hpi0,          4);
            dg.addDataSet(hpi0_calib,    4);          
            dg.addDataSet(fpi0_calib,    4);
            dg.addDataSet(hcal2d,        5);
            dg.addDataSet(fcal2d,        5);
            dg.addDataSet(hcal,          6);
            dg.addDataSet(fcal,          6);
            dg.addDataSet(heconstants,   7);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        this.getCalibrationTable().addConstraint(3, this.getReference()-0.8, this.getReference()+0.8);
        this.getCalibrationTable().fireTableDataChanged();
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
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("hpi0_" + key).getIntegral();
    }

    @Override
    public void processEvent(DataEvent event) {
        // get current calibration constants
        int run = 0;
        if(event.hasBank("RUN::config")) {
            run = event.getBank("RUN::config").getInt("run", 0);            
        }
        else {
            return;
        }
        if(this.getConstantsManager()!=null) charge2energy = this.getConstantsManager().getConstants(run, "/calibration/ft/ftcal/charge_to_energy");
        if(this.getConstantsManager()!=null) energycorr    = this.getConstantsManager().getConstants(run, "/calibration/ft/ftcal/energycorr");
        
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
                double z       = this.getConstants().crystal_distance+this.getConstants().shower_depth;//clusterFTCAL.getFloat("z", loop);
                double energy  = 1e3 * clusterFTCAL.getFloat("energy", loop);
                double energyR = 1e3 * clusterFTCAL.getFloat("recEnergy", loop);
                double path    = Math.sqrt(x*x+y*y+(z-this.getConstants().z0)*(z-this.getConstants().z0));
                int    key = 0;
                double energySeed=0;
                double energyCalib=0;
                // find hits associated to clusters
                ArrayList<Integer> crystals = new ArrayList();
                for(int k=0; k<hitFTCAL.rows(); k++) {
                    int clusterID  = hitFTCAL.getShort("clusterID", k);
                    // select hits that belong to cluster 
                    if(clusterID == id) { 
                        int hitID = hitFTCAL.getShort("hitID", k);
                        crystals.add(hitID);
                    }                            
                }
                // go to adc value to get reclibrated energy
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
                    c2e=c2e*this.getConstantScale();
                    double energyK = adc*c2e;
                    if(energyK>energySeed) {
                        key = component;
                        energySeed = energyK;
                    }                    
                    energyCalib += energyK;
//                    System.out.println(key + " " + energyK + " " + c2e + " " + energySeed + " " + energy);
                }
                double eR = energyCalib/1000;
                double  energyCorr = (energycorr.getDoubleValue("c0",1,1,key)
                                   +  energycorr.getDoubleValue("c1",1,1,key)*eR
                                   +  energycorr.getDoubleValue("c2",1,1,key)*eR*eR
                                   +  energycorr.getDoubleValue("c3",1,1,key)*eR*eR*eR
                                   +  energycorr.getDoubleValue("c4",1,1,key)*eR*eR*eR*eR
                    );
//                System.out.println(energyCorr + " " + (energy-energyR));
//                energyCalib = energyCalib+energy-energyR;
                energyCalib = energyCalib+energyCorr;
                        
                Particle recParticle = new Particle(22, energy*x/path, energy*y/path, energy*(z-this.getConstants().z0)/path, 0,0,0);
                recParticle.setProperty("key",(double) key);
                recParticle.setProperty("energySeed", energySeed);
                recParticle.setProperty("energyCalib",energyCalib);
                recParticle.setProperty("theta",Math.toDegrees(Math.acos((z-this.getConstants().z0)/path)));
                if(energyR>this.getConstants().clusterThr && size>this.getConstants().clusterSize && charge==0) ftParticles.add(recParticle);
            }
            if(ftParticles.size()>=2) {
                for (int i1 = 0; i1 < ftParticles.size(); i1++) {
                    for (int i2 = i1 + 1; i2 < ftParticles.size(); i2++) {
                        int key1 = (int) ftParticles.get(i1).getProperty("key");
                        int key2 = (int) ftParticles.get(i2).getProperty("key");
                        // get original mass
                        Particle partGamma1 = ftParticles.get(i1);
                        Particle partGamma2 = ftParticles.get(i2);
                        Particle partPi0 = new Particle();
                        partPi0.copy(partGamma1);
                        partPi0.combine(partGamma2, +1);
                        double invmass = Math.sqrt(partPi0.mass2());
                        double x = (partGamma1.p() - partGamma2.p()) / (partGamma1.p() + partGamma2.p());
                        double angle = Math.toDegrees(Math.acos(partGamma1.cosTheta(partGamma2)));
                        double invmassCalib = Math.pow(2*partGamma1.getProperty("energyCalib")*partGamma2.getProperty("energyCalib")*(1-Math.cos(Math.toRadians(angle))),0.5);
                        if(angle>this.getConstants().pi0MinAngle) {
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0sum").fill(invmass);
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0sum").fill(invmass);
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0sum_calib").fill(invmassCalib);
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0sum_calib").fill(invmassCalib);
                        }
                        this.getDataGroup().getItem(1, 1, key1).getH2F("hmassangle").fill(invmassCalib, angle);
                        this.getDataGroup().getItem(1, 1, key2).getH2F("hmassangle").fill(invmassCalib, angle);
                        if(angle>this.getConstants().pi0MinAngle) {
                            double ecal1 = Math.pow(this.getReference(),2)/(2*partGamma2.getProperty("energyCalib")*(1-Math.cos(Math.toRadians(angle))));
                            double ecal2 = Math.pow(this.getReference(),2)/(2*partGamma1.getProperty("energyCalib")*(1-Math.cos(Math.toRadians(angle))));
  //                          ecal1=Math.pow(PhysicsConstants.massPionNeutral()*1.0E3,2)/(2*partGamma1.p()*partGamma2.p()*(1-Math.cos(Math.toRadians(angle))));
//                            System.out.println(PhysicsConstants.massPionNeutral() + " " + ecal1 + " " + partGamma1.p()+ " " + partGamma1.getProperty("energySeed"));
                            if(ecal1>0 && 
                               partGamma2.getProperty("theta")>this.getConstants().thetaMin && 
                               partGamma2.getProperty("theta")<this.getConstants().thetaMax) {
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0_" + key1).fill(invmass);
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0_calib_" + key1).fill(invmassCalib);
                                this.getDataGroup().getItem(1, 1, key1).getH2F("hcal2d_" + key1).fill(ecal1,partGamma1.getProperty("energyCalib"));
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hcal_" + key1).fill(ecal1/partGamma1.getProperty("energyCalib"));
                            }
                            if(ecal2>0 && 
                               partGamma1.getProperty("theta")>this.getConstants().thetaMin && 
                               partGamma1.getProperty("theta")<this.getConstants().thetaMax) {
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0_" + key2).fill(invmass);
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0_calib_" + key2).fill(invmassCalib);
                                this.getDataGroup().getItem(1, 1, key2).getH2F("hcal2d_" + key2).fill(ecal2,partGamma2.getProperty("energyCalib"));
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hcal_" + key2).fill(ecal2/partGamma2.getProperty("energyCalib"));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void analyze() {
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getH1F("hemass").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("hefactors").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("heconstants").reset();
        }

        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hpi0sum = this.getDataGroup().getItem(1,1,key).getH1F("hpi0sum_calib");
            F1D fpi0sum = this.getDataGroup().getItem(1,1,key).getF1D("fpi0sum_calib");
            this.initCalibGaussFitPar(fpi0sum,hpi0sum,0);
            DataFitter.fit(fpi0sum,hpi0sum,"LQ");
            hpi0sum.setFunction(null);

            H1F hpi0 = this.getDataGroup().getItem(1,1,key).getH1F("hpi0_calib_" + key);
            F1D fpi0 = this.getDataGroup().getItem(1,1,key).getF1D("fpi0_calib_" + key);
            this.initCalibGaussFitPar(fpi0,hpi0,0);
            DataFitter.fit(fpi0,hpi0,"LQ");
            hpi0.setFunction(null);

            H1F hcalib = this.getDataGroup().getItem(1,1,key).getH1F("hcal_" + key);
            F1D fcalib = this.getDataGroup().getItem(1,1,key).getF1D("fcal_" + key);
            this.initCalibGaussFitPar(fcalib,hcalib,-1);
            DataFitter.fit(fcalib,hcalib,"LQ");
            hcalib.setFunction(null);
        }
    }

    @Override
    public void adjustFit() {
        System.out.println("Adjusting fit for component " + this.getSelectedKey());
        H1F hcal = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getH1F("hcal_" + this.getSelectedKey());
        F1D fcal = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getF1D("fcal_" + this.getSelectedKey());
        FTAdjustFit cfit = new FTAdjustFit(hcal, fcal, "LRQ");
        this.getCanvas().update();
    }

    private void initCalibGaussFitPar(F1D ftime, H1F htime, int mode) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = 1;
        if(mode>=0) hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = htime.getRMS();
        double rangeMin = (hMean - (1.0*hRMS)); 
        double rangeMax = (hMean + (1.0*hRMS));  
        double pm = (hMean*20.)/100.0;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.0, hAmp*1.2);
        if(mode>=0) ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+(pm));
        ftime.setParameter(2, 0.1*hRMS);
        ftime.setParLimits(2, 0.1*hRMS, 0.8*hRMS);
    }    

    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            F1D fpi0 = this.getDataGroup().getItem(1,1,key).getF1D("fpi0_calib_" + key);
            return fpi0.getParameter(1);
       }
        return 0;
    }
    
    @Override
    public void setCanvasBookData() {
        int[] pads = {4,5,6};
        this.getCanvasBook().setData(this.getDataGroup(), pads);
    }
    
    @Override
    public void setDrawOptions() {
        this.getCanvas().getPad(1).getAxisZ().setLog(true);
    }

    public void updateTable() {
        
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hcalib = this.getDataGroup().getItem(1,1,key).getH1F("hcal_" + key);
            F1D fpi0 = this.getDataGroup().getItem(1,1,key).getF1D("fpi0_calib_" + key);
            F1D fcalib = this.getDataGroup().getItem(1,1,key).getF1D("fcal_" + key);
            
            double pi0Mass     = 0;
            double pi0Mass_err = 0;
            double factorE     = 0;
            double factorE_err = 0;
            double c2e         = this.getConstants().chargeMips;
            if(this.getConstantsManager()!=null) c2e = charge2energy.getDoubleValue("mips_charge", 1,1,key);
            if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                c2e = this.getPreviousCalibrationTable().getDoubleValue("charge2energy", 1, 1, key);
            }                        
            c2e=c2e/this.getConstantScale();
            
            pi0Mass     = fpi0.getParameter(1);
            pi0Mass_err = fpi0.parameter(1).error();
            factorE     = fcalib.getParameter(1);
            factorE_err = fcalib.parameter(1).error();

            // update charge2energy only if max bin content is >= threshold
            if(hcalib.getMax()>=3) c2e = c2e/factorE;
            else                   c2e = this.getConstants().defaultC2E;

            double pi0MassDiff = pi0Mass - this.getReference();
            if(Math.abs(pi0MassDiff)<1) this.getDataGroup().getItem(1,1,key).getH1F("hemass").fill(pi0Mass);
            if(factorE>0) this.getDataGroup().getItem(1,1,key).getH1F("hefactors").fill(factorE);
            this.getDataGroup().getItem(1,1,key).getH1F("heconstants").fill(c2e);
//            H1F hefactors = this.getDataGroup().getItem(1,1,key).getH1F("hefactors");
//            F1D ffactor   = this.getDataGroup().getItem(1,1,key).getF1D("ffactor");
//            this.initCalibGaussFitPar(ffactor,hefactors);
//            DataFitter.fit(ffactor,hefactors,"LQ");
//            hefactors.setFunction(null);

            getCalibrationTable().setDoubleValue(pi0Mass,     "pi0mass",        1, 1, key);
            getCalibrationTable().setDoubleValue(pi0Mass_err, "pi0mass_error",  1, 1, key);
            getCalibrationTable().setDoubleValue(factorE,     "factor",         1, 1, key);
            getCalibrationTable().setDoubleValue(factorE_err, "factor_error",   1, 1, key);
            getCalibrationTable().setDoubleValue(c2e,         "charge2energy",  1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();

    }

    @Override
    public void timerUpdate() {
        this.analyze();            
        this.updateTable();
    }
}
