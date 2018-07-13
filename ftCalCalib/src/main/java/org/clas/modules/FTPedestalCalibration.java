/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;

/**
 *
 * @author devita
 */
public class FTPedestalCalibration extends FTCalibrationModule {

    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    double clusterThr = 50.0;// Vertical selection
    double singleChThr = 0.00;// Single channel selection MeV
    double signalThr =0.0;
    double adcThrs=0.00;// Threshold used for simulated events in MeV
    double startTime   = 124.25;//ns
    double ftcalDistance =1898; //mm
    double timeshift =0;// ns
    double crystal_size = 15.3;//mm
    double charge2e = 15.3/6.005; //MeV
    double crystal_length = 200;//mm                                                                                            
    double shower_depth = 65;                                                                                                   
    double light_speed = 150; //cm/ns     
    double c = 29.97; //cm/ns     

    public FTPedestalCalibration(FTDetector d, String name) {
        super(d, name, "pedestal:pedestal_error:pedestal_sigma",3);
        this.getCalibrationTable().addConstraint(3, 140, 260);
        this.getCalibrationTable().addConstraint(5, 0, 1);                
    }

    @Override
    public void resetEventListener() {

        H1F hpsum = new H1F("hpsum", 200, 100.0, 300.0);
        hpsum.setTitleX("Pedestal (channel)");
        hpsum.setTitleY("Counts");
        hpsum.setTitle("Global Time Offset");
        hpsum.setFillColor(3);
        H1F hpsum_calib = new H1F("hpsum_calib", 200, 100.0, 300.0);
        hpsum_calib.setTitleX("Pedestal (channel)");
        hpsum_calib.setTitleY("counts");
        hpsum_calib.setTitle("Global Time Offset");
        hpsum_calib.setFillColor(44);
        GraphErrors  gpedestals = new GraphErrors("gpedestals");
        gpedestals.setTitle("Pedestals"); //  title
        gpedestals.setTitleX("Crystal ID"); // X axis title
        gpedestals.setTitleY("Pedestal (channel)");   // Y axis title
        gpedestals.setMarkerColor(5); // color from 0-9 for given palette
        gpedestals.setMarkerSize(5);  // size in points on the screen
//        gpedestals.setMarkerStyle(1); // Style can be 1 or 2
        gpedestals.addPoint(0., 0., 0., 0.);
        gpedestals.addPoint(1., 1., 0., 0.);

        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);

            // initialize data group
            H1F hped_wide = new H1F("hped_wide_" + key, 200, 100.0, 300.0);
            hped_wide.setTitleX("Pedestal (channel)");
            hped_wide.setTitleY("Counts");
            hped_wide.setTitle("Component " + key);
            H1F hped = new H1F("hped_" + key, 200, 100.0, 300.0);
            hped.setTitleX("Pedestal (channel)");
            hped.setTitleY("Counts");
            hped.setTitle("Component " + key);
            H1F hped_calib = new H1F("hped_calib_" + key, 200, 100.0, 300.0);
            hped_calib.setTitleX("Pedestal (channel)");
            hped_calib.setTitleY("Counts");
            hped_calib.setTitle("Component " + key);
            hped_calib.setFillColor(44);
            hped_calib.setLineColor(24);
            F1D fped = new F1D("fped_" + key, "[amp]*gaus(x,[mean],[sigma])", 195, 205);
            fped.setParameter(0, 0.0);
            fped.setParameter(1, 0.0);
            fped.setParameter(2, 2.0);
            fped.setLineColor(24);
            fped.setLineWidth(2);
//            fped.setLineColor(2);
//            fped.setLineStyle(1);
            DataGroup dg = new DataGroup(2, 2);
            dg.addDataSet(hpsum      , 0);
            dg.addDataSet(hpsum_calib, 0);
            dg.addDataSet(gpedestals,  1);
            dg.addDataSet(hped_wide,  2);
            dg.addDataSet(hped,       3);
            dg.addDataSet(hped_calib, 3);
            dg.addDataSet(fped,       3);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(1, 1, icomp).getH1F("hped_" + icomp).getEntries();
    }

    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        this.startTime = -100000;
        if(event.hasBank("REC::Event")) {
            DataBank recEvent = event.getBank("REC::Event");
            this.startTime = recEvent.getFloat("STTime", 0);
//            System.out.println(this.startTime);
        }
        if (event.hasBank("FTCAL::adc")) {
            DataBank adcFTCAL = event.getBank("FTCAL::adc");
            for (int loop = 0; loop < adcFTCAL.rows(); loop++) {
                int    key    = adcFTCAL.getInt("component", loop);
                int    adc    = adcFTCAL.getInt("ADC", loop);
                double ped    = adcFTCAL.getShort("ped", loop);                
                if(adc>adcThrs) {
                    this.getDataGroup().getItem(1,1,key).getH1F("hpsum").fill(ped);
                    this.getDataGroup().getItem(1,1,key).getH1F("hped_wide_"+key).fill(ped);
                    this.getDataGroup().getItem(1,1,key).getH1F("hped_"+key).fill(ped);
                    if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                        double offset = this.getPreviousCalibrationTable().getDoubleValue("pedestal", 1, 1, key);
                        this.getDataGroup().getItem(1,1,key).getH1F("hpsum_calib").fill(ped+200-offset);
                        this.getDataGroup().getItem(1,1,key).getH1F("hped_calib_"+key).fill(ped+200-offset);                        
                    }                            
                } 
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");
        this.getCanvas().getPad(0).getAxisY().setLog(true);
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gpedestals").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hped = this.getDataGroup().getItem(1,1,key).getH1F("hped_" + key);
            F1D fped = this.getDataGroup().getItem(1,1,key).getF1D("fped_" + key);
            this.initTimeGaussFitPar(fped,hped);
            DataFitter.fit(fped,hped,"LQ");
            
            this.getDataGroup().getItem(1,1,key).getGraph("gpedestals").addPoint(key, fped.getParameter(1), 0, fped.parameter(1).error());
        
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("fped_" + key).getParameter(1),      "pedestal",       1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("fped_" + key).parameter(1).error(), "pedestal_error", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("fped_" + key).getParameter(2),      "pedestal_sigma", 1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    private void initTimeGaussFitPar(F1D fped, H1F hped) {
        double hAmp  = hped.getBinContent(hped.getMaximumBin());
        double hMean = hped.getAxis().getBinCenter(hped.getMaximumBin());
        double hRMS  = hped.getRMS(); //ns
        double rangeMin = (hMean - (3*hRMS)); 
        double rangeMax = (hMean + (3*hRMS));  
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
            int nent = this.getNEvents(sector, layer, key);
            if (nent > 0) {
                col = palette.getColor3D(nent, this.getnProcessed(), true);
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
