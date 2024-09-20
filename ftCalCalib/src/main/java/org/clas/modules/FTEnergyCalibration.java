package org.clas.modules;

import org.clas.ftdata.FTCalCluster;
import org.clas.ftdata.FTCalEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTAdjustFit;
import org.clas.viewer.FTCalConstants;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
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
    
    public FTEnergyCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "pi0mass:pi0mass_error:factor:factor_error:mips_charge:",3, ccdb, gConstants);
        this.setCCDBTable("/calibration/ft/ftcal/charge_to_energy");
        this.setCols(PhysicsConstants.massPionNeutral()*1E3*0.95, PhysicsConstants.massPionNeutral()*1E3*1.05);
        this.setReference(PhysicsConstants.massPionNeutral()*1E3);
        this.getCalibrationTable().addConstraint(3, this.getReference()-0.8, this.getReference()+0.8);
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
            // initialize data group
            H1F hpi0 = new H1F("hpi0_" + key, 100,50., 200.);
            hpi0.setTitleX("M (MeV)");
            hpi0.setTitleY("Counts");
            hpi0.setTitle("Component " + key);
            H1F hpi0_calib = new H1F("hpi0_calib_" + key, 75, 50., 200.);
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
            H1F hcal = new H1F("hcal_" + key, 60, 0, 2);
            hcal.setTitleX("Correction Factor");
            hcal.setTitleY("Counts");
            hcal.setTitle("Component " + key);
            F1D fcal = new F1D("fcal_" + key, "[amp]*gaus(x,[mean],[sigma])+[p0]+[p1]*x", 0.9, 1.1);
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
    }

    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("hpi0_" + key).getIntegral();
    }

    @Override
    public void loadConstants(IndexedTable table) {
        for(int i=0; i<table.getRowCount(); i++) {
            int sector = Integer.valueOf((String)table.getValueAt(i, 0));
            int layer  = Integer.valueOf((String)table.getValueAt(i, 1));
            int comp   = Integer.valueOf((String)table.getValueAt(i, 2));
            double cmips = table.getDoubleValue("mips_charge", sector, layer, comp);
            this.getPreviousCalibrationTable().addEntry(sector, layer, comp);
            for(int j=3; j<this.getPreviousCalibrationTable().getColumnCount(); j++) {
                this.getPreviousCalibrationTable().setDoubleValue(0.0, this.getPreviousCalibrationTable().getColumnName(j), sector, layer, comp);
            }                
            this.getPreviousCalibrationTable().setDoubleValue(cmips, "mips_charge", sector, layer, comp);
        }
        this.getPreviousCalibrationTable().fireTableDataChanged();    
    }
    
    @Override
    public void processEvent(FTCalEvent event) {
        // get current calibration constants
        int run = event.getRun();
        if(run<=0) {
            return;
        }
        
        // loop over FTCAL reconstructed cluster
        if (!event.getClusters().isEmpty()) {
            
            List<Particle> photons0 = new ArrayList<>();
            List<Particle> photons  = new ArrayList<>();

            // start from clusters
            for (FTCalCluster c : event.getClusters()) {
                
                if(c.charge()==0 && c.size()>FTCalConstants.CLUSTERSIZE) {
                        
                    if(c.energyR(true)*1000>FTCalConstants.CLUSTERTHR)  {
                        photons.add(c.toParticle(true));
                        photons0.add(c.toParticle(false));
                    }
                    
                }
            }
                
            if(photons.size()>=2) {
                for (int i1 = 0; i1 < photons.size(); i1++) {
                    for (int i2 = i1 + 1; i2 < photons.size(); i2++) {

                        int key1 = (int) photons.get(i1).getProperty("seed");
                        int key2 = (int) photons.get(i2).getProperty("seed");

                        // get calibrated mass
                        Particle gamma1 = photons.get(i1);
                        Particle gamma2 = photons.get(i2);
                        Particle pi0 = new Particle();
                        pi0.copy(gamma1);
                        pi0.combine(gamma2, +1);
                        double invmass = pi0.mass()*1E3;
                        double angle = Math.toDegrees(Math.acos(gamma1.cosTheta(gamma2)));
                        
                        Particle pi0Org = new Particle();
                        pi0Org.copy(photons0.get(i1));
                        pi0Org.combine(photons0.get(i2), +1);
                        double invmassOrg = pi0Org.mass()*1E3;
                                
                        if(angle>FTCalConstants.PI0MINANGLE) {
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0sum").fill(invmassOrg);
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0sum").fill(invmassOrg);
                            this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0sum_calib").fill(invmass);
                            this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0sum_calib").fill(invmass);
                        }
                        this.getDataGroup().getItem(1, 1, key1).getH2F("hmassangle").fill(invmass, angle);
                        this.getDataGroup().getItem(1, 1, key2).getH2F("hmassangle").fill(invmass, angle);

                        if(angle>FTCalConstants.PI0MINANGLE) {
                            double ecal1 = Math.pow(this.getReference(),2)/(2*gamma2.p()*1E3*(1-Math.cos(Math.toRadians(angle))));
                            double ecal2 = Math.pow(this.getReference(),2)/(2*gamma1.p()*1E3*(1-Math.cos(Math.toRadians(angle))));
                            double theta1 = Math.toDegrees(gamma1.theta());
                            double theta2 = Math.toDegrees(gamma2.theta());

                            if(ecal1>0 && 
                               theta2>FTCalConstants.THETAMIN && 
                               theta2<FTCalConstants.THETAMAX) {
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0_" + key1).fill(invmassOrg);
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hpi0_calib_" + key1).fill(invmass);
                                this.getDataGroup().getItem(1, 1, key1).getH2F("hcal2d_" + key1).fill(ecal1,gamma1.p()*1E3);
                                this.getDataGroup().getItem(1, 1, key1).getH1F("hcal_" + key1).fill(ecal1/gamma1.p()/1E3);
                            }
                            if(ecal2>0 && 
                               theta1>FTCalConstants.THETAMIN && 
                               theta1<FTCalConstants.THETAMAX) {
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0_" + key2).fill(invmassOrg);
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hpi0_calib_" + key2).fill(invmass);
                                this.getDataGroup().getItem(1, 1, key2).getH2F("hcal2d_" + key2).fill(ecal2,gamma2.p());
                                this.getDataGroup().getItem(1, 1, key2).getH1F("hcal_" + key2).fill(ecal2/gamma2.p());
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
            if(Math.abs(fcalib.getParameter(4)/fcalib.getParameter(0))>1) {
                fcalib = new F1D("fcal_" + key, "[amp]*gaus(x,[mean],[sigma])+[p0]+[p1]*x+[p2]*x*x", 0.9, 1.1);
                this.initCalibGaussFitPar(fcalib, hcalib, -1);
                for(int i=0; i<hcalib.getFunction().getNPars(); i++) {
                    fcalib.setParameter(i, hcalib.getFunction().getParameter(i));
                    DataFitter.fit(fcalib,hcalib,"LQ");
                }
            }
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
        double rangeMin = (hMean - (2.0*hRMS)); 
        double rangeMax = (hMean + (1.5*hRMS));  
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
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            if(this.getCalibrationTable().hasEntry(1, 1, key)) 
                return this.getCalibrationTable().getDoubleValue("pi0mass", 1, 1, key);
            else {
                F1D fpi0 = this.getDataGroup().getItem(1,1,key).getF1D("fpi0_calib_" + key);
                return fpi0.getParameter(1);
            }
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

    @Override
    public void updateTable() {
        
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hcalib = this.getDataGroup().getItem(1,1,key).getH1F("hcal_" + key);
            F1D fpi0 = this.getDataGroup().getItem(1,1,key).getF1D("fpi0_calib_" + key);
            F1D fcalib = this.getDataGroup().getItem(1,1,key).getF1D("fcal_" + key);
            
            double pi0Mass     = 0;
            double pi0Mass_err = 0;
            double factorE     = 0;
            double factorE_err = 0;
            double cmips       = FTCalConstants.CHARGEMIPS;
            if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                cmips = this.getPreviousCalibrationTable().getDoubleValue("mips_charge", 1, 1, key);
            }                        
            cmips=cmips/this.getConstantScale();
            
            pi0Mass     = fpi0.getParameter(1);
            pi0Mass_err = fpi0.parameter(1).error();
            factorE     = fcalib.getParameter(1);
            factorE_err = fcalib.parameter(1).error();

            // update charge2energy only if max bin content is >= threshold
            if(hcalib.getMax()>=3) cmips = cmips/factorE;
            else                   cmips = FTCalConstants.DEFAULTEMIPS;

            double pi0MassDiff = pi0Mass - this.getReference();
            if(Math.abs(pi0MassDiff)<1) this.getDataGroup().getItem(1,1,key).getH1F("hemass").fill(pi0Mass);
            if(factorE>0) this.getDataGroup().getItem(1,1,key).getH1F("hefactors").fill(factorE);
            this.getDataGroup().getItem(1,1,key).getH1F("heconstants").fill(cmips);

            getCalibrationTable().setDoubleValue(pi0Mass,     "pi0mass",        1, 1, key);
            getCalibrationTable().setDoubleValue(pi0Mass_err, "pi0mass_error",  1, 1, key);
            getCalibrationTable().setDoubleValue(factorE,     "factor",         1, 1, key);
            getCalibrationTable().setDoubleValue(factorE_err, "factor_error",   1, 1, key);
            getCalibrationTable().setDoubleValue(cmips,       "mips_charge",    1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }
}
