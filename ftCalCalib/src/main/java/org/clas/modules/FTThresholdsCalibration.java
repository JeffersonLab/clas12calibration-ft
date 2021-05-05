/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
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
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTThresholdsCalibration extends FTCalibrationModule {

    IndexedTable charge2energy = null;

    public FTThresholdsCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "threshold:threshold_error",3, ccdb, gConstants);
        this.getCalibrationTable().addConstraint(3, 5, 40);                
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 200, 0.0, 100.0);
        htsum.setTitleX("Threshold (MeV)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Thresholds Distribution");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 200, 0.0, 100.0);
        htsum_calib.setTitleX("Threshold (MeV)");
        htsum_calib.setTitleY("counts");
        htsum_calib.setTitle("Thresholds Distribution");
        htsum_calib.setFillColor(44);
        GraphErrors  gthresholds = new GraphErrors("gthresholds");
        gthresholds.setTitle("Thresholds"); //  title
        gthresholds.setTitleX("Crystal ID"); // X axis title
        gthresholds.setTitleY("Thresholds (MeV)");   // Y axis title
        gthresholds.setMarkerColor(5); // color from 0-9 for given palette
        gthresholds.setMarkerSize(5);  // size in points on the screen
//        gpedestals.setMarkerStyle(1); // Style can be 1 or 2
        gthresholds.addPoint(0., 0., 0., 0.);
        gthresholds.addPoint(1., 1., 0., 0.);
        H1F hthresholds = new H1F("hthresholds", 100, 0., 60.);
        hthresholds.setTitleX("Thresholds (MeV)");
        hthresholds.setTitleY("Crystals");
        hthresholds.setTitle("Thresholds");
        hthresholds.setFillColor(44);
        hthresholds.setLineColor(24);
        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);

            // initialize data group
            H1F hthrs = new H1F("hthrs_" + key, 100, 0.0, 100.0);
            hthrs.setTitleX("Threshold (MeV)");
            hthrs.setTitleY("Counts");
            hthrs.setTitle("Component " + key);
            F1D fthrs = new F1D("fthrs_" + key, "[amp]*gaus(x,[mean],[sigma])", 0, 30);
            fthrs.setParameter(0, 0.0);
            fthrs.setParameter(1, 0.0);
            fthrs.setParameter(2, 2.0);
            fthrs.setLineColor(24);
            fthrs.setLineWidth(2);
//            fped.setLineColor(2);
//            fped.setLineStyle(1);
            DataGroup dg = new DataGroup(2, 2);
            dg.addDataSet(htsum      , 0);
            dg.addDataSet(gthresholds, 1);
            dg.addDataSet(hthrs,       2);
            dg.addDataSet(fthrs,       2);
            dg.addDataSet(hthresholds, 3);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return (int) this.getDataGroup().getItem(1, 1, icomp).getH1F("hthrs_" + icomp).getEntries();
    }

    public double getThreshold(int isec, int ilay, int icomp) {
        return this.getCalibrationTable().getDoubleValue("threshold", isec, ilay, icomp);
    }

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
        if (event.hasBank("FTCAL::adc")) {
            DataBank adcFTCAL = event.getBank("FTCAL::adc");
            for (int loop = 0; loop < adcFTCAL.rows(); loop++) {
                int    component    = adcFTCAL.getInt("component", loop);
                int    adc          = adcFTCAL.getInt("ADC", loop);
                double ped          = adcFTCAL.getShort("ped", loop);                
                if(adc>0) {
                    double c2e        = (this.getConstants().LSB*this.getConstants().nsPerSample/50)*this.getConstants().eMips/this.getConstants().chargeMips;
                    if(this.getConstantsManager()!=null) c2e = 1*charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
						                *charge2energy.getDoubleValue("mips_energy", 1,1,component)
						                /charge2energy.getDoubleValue("mips_charge", 1,1,component);
                    if(this.getPreviousCalibrationTable().hasEntry(1,1,component)) {
                        c2e = charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
			     *charge2energy.getDoubleValue("mips_energy", 1,1,component)
			     /this.getPreviousCalibrationTable().getDoubleValue("charge2energy", 1, 1, component);
                    }
                    this.getDataGroup().getItem(1,1,component).getH1F("htsum").fill(adc*c2e);
                    this.getDataGroup().getItem(1,1,component).getH1F("hthrs_"+component).fill(adc*c2e);
//                    if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
//                        double offset = this.getPreviousCalibrationTable().getDoubleValue("pedestal", 1, 1, key);
//                        this.getDataGroup().getItem(1,1,key).getH1F("hpsum_calib").fill(ped+200-offset);
//                        this.getDataGroup().getItem(1,1,key).getH1F("hped_calib_"+key).fill(ped+200-offset);                        
//                    }                            
                } 
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");
//        this.getCanvas().getPad(0).getAxisY().setLog(true);
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gthresholds").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("hthresholds").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hthrs = this.getDataGroup().getItem(1,1,key).getH1F("hthrs_" + key);
            F1D fthrs = this.getDataGroup().getItem(1,1,key).getF1D("fthrs_" + key);
            this.initTimeGaussFitPar(fthrs,hthrs);
            DataFitter.fit(fthrs,hthrs,"LQ");
            
        
            double mean   = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).getParameter(1);
            double sigma  = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).getParameter(2);
            double emean  = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).parameter(1).error();
            double esigma = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).parameter(2).error();
            double thrs   = mean-1.177*sigma;
            double ethrs  = Math.sqrt(emean*emean+1.177*1.177*esigma*esigma);
            this.getDataGroup().getItem(1,1,key).getGraph("gthresholds").addPoint(key, mean, 0, emean);
            this.getDataGroup().getItem(1,1,key).getH1F("hthresholds").fill(mean);
            getCalibrationTable().setDoubleValue(mean,  "threshold",       1, 1, key);
            getCalibrationTable().setDoubleValue(emean, "threshold_error", 1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    private void initTimeGaussFitPar(F1D fped, H1F hped) {
        double hAmp  = hped.getBinContent(hped.getMaximumBin());
        double hMean = hped.getAxis().getBinCenter(hped.getMaximumBin());
        double hRMS  = hped.getRMS(); //ns
        double rangeMin = (hMean - (3.0*hRMS)); 
        double rangeMax = (hMean + (0.1*hRMS));  
        fped.setRange(rangeMin, rangeMax);
        fped.setParameter(0, hAmp);
        fped.setParLimits(0, hAmp*0.8, hAmp*1.2);
        fped.setParameter(1, hMean);
        fped.setParLimits(1, hMean-hRMS, hMean+hRMS);
        fped.setParameter(2, 0.2);
        fped.setParLimits(2, 0.1*hRMS, 2*hRMS);
    }    

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 3);
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
            double thrs = this.getThreshold(sector, layer, key);
            if (thrs > 0) {
                col = palette.getColor3D(thrs, 80, false);
            }
        }
//        col = new Color(100, 0, 0);
        return col;
    }
   
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
