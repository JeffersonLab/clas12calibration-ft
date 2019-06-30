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

/**
 *
 * @author devita
 */
public class FTTimeCalibration extends FTCalibrationModule {   
    

    
    public FTTimeCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "offset:offset_error:resolution",3, ccdb, gConstants);
        this.setRange(25.,45.);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 330, -500.0, 50.0);
        htsum.setTitleX("Time Offset (ns)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Global Time Offset");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 800, -20.0, 20.0);
        htsum_calib.setTitleX("Time Offset (ns)");
        htsum_calib.setTitleY("counts");
        htsum_calib.setTitle("Global Time Offset");
        htsum_calib.setFillColor(44);
        GraphErrors  gtoffsets = new GraphErrors("gtoffsets");
        gtoffsets.setTitle("Timing Offsets"); //  title
        gtoffsets.setTitleX("Crystal ID"); // X axis title
        gtoffsets.setTitleY("Timing (ns)");   // Y axis title
        gtoffsets.setMarkerColor(5); // color from 0-9 for given palette
        gtoffsets.setMarkerSize(5);  // size in points on the screen
//        gtoffsets.setMarkerStyle(1); // Style can be 1 or 2
        gtoffsets.addPoint(0., 0., 0., 0.);
        gtoffsets.addPoint(1., 1., 0., 0.);

        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);
            this.getCalibrationTable().setDoubleValue(0.0, "offset",   1,1,key);

            // initialize data group
            H1F htime_wide = new H1F("htime_wide_" + key, 330, -400.0, 150.0);
            htime_wide.setTitleX("Time (ns)");
            htime_wide.setTitleY("Counts");
            htime_wide.setTitle("Component " + key);
            H1F htime = new H1F("htime_" + key, 400, this.getRange()[0], this.getRange()[1]);
            htime.setTitleX("Time (ns)");
            htime.setTitleY("Counts");
            htime.setTitle("Component " + key);
            H1F htime_calib = new H1F("htime_calib_" + key, 400, -20., 20.);
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
//            ftime.setLineColor(2);
//            ftime.setLineStyle(1);
            DataGroup dg = new DataGroup(3, 2);
            dg.addDataSet(htsum      , 0);
            dg.addDataSet(htsum_calib, 1);
            dg.addDataSet(gtoffsets,   2);
            dg.addDataSet(htime_wide,  3);
            dg.addDataSet(htime,       4);
            dg.addDataSet(ftime,       4);
            dg.addDataSet(htime_calib, 5);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(1, 1, icomp).getH1F("htime_" + icomp).getEntries();
    }

    public void processEvent(DataEvent event) {
        // loop over FTCAL reconstructed cluster
        double startTime = -100000;

        // get start time
        if(event.hasBank("REC::Event")) {
            DataBank recEvent = event.getBank("REC::Event");
            startTime = recEvent.getFloat("startTime", 0);
        }
        if(event.hasBank("REC::Particle")) {
            DataBank recPart = event.getBank("REC::Particle");
        }
        if (event.hasBank("FTCAL::adc") && startTime>-1000) {
            DataBank adcFTCAL = event.getBank("FTCAL::adc");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            for (int loop = 0; loop < adcFTCAL.rows(); loop++) {
                int    key    = adcFTCAL.getInt("component", loop);
                int    adc    = adcFTCAL.getInt("ADC", loop);
                double time   = adcFTCAL.getFloat("time", loop);                
                double charge =((double) adc)*(this.getConstants().LSB*this.getConstants().nsPerSample/50);
                double radius = Math.sqrt(Math.pow(this.getDetector().getIdX(key)-0.5,2.0)+Math.pow(this.getDetector().getIdY(key)-0.5,2.0))*this.getConstants().crystal_size;//meters
                double path   = Math.sqrt(Math.pow(this.getConstants().crystal_distance+this.getConstants().shower_depth,2)+Math.pow(radius,2));
                double tof    = (path/PhysicsConstants.speedOfLight()); //ns
                double timec  = (time -(startTime + (this.getConstants().crystal_length-this.getConstants().shower_depth)/this.getConstants().light_speed + tof));
                double twalk  = 0;
                if(charge>this.getConstants().chargeThr) {
                    if(this.getGlobalCalibration().containsKey("TimeWalk")) {
                        double amp = this.getGlobalCalibration().get("TimeWalk").getDoubleValue("A", 1,1,key);
                        double lam = this.getGlobalCalibration().get("TimeWalk").getDoubleValue("L", 1,1,key);
                        twalk = amp/Math.pow(charge,lam);
                    }
                    this.getDataGroup().getItem(1,1,key).getH1F("htsum").fill(timec-twalk);
                    this.getDataGroup().getItem(1,1,key).getH1F("htime_wide_"+key).fill(timec-twalk);
                    this.getDataGroup().getItem(1,1,key).getH1F("htime_"+key).fill(timec-twalk);
                    if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                        double offset = this.getPreviousCalibrationTable().getDoubleValue("offset", 1, 1, key);
                        this.getDataGroup().getItem(1,1,key).getH1F("htsum_calib").fill(timec-twalk-offset);
                        this.getDataGroup().getItem(1,1,key).getH1F("htime_calib_"+key).fill(timec-twalk-offset);                        
//                        System.out.println(key + " " + (time-twalk-offset-(this.getConstants().crystal_length-this.getConstants().shower_depth)/this.getConstants().light_speed) + " " + adc + " " + charge + " " + time + " " + twalk + " " + offset);
//                        if(event.hasBank("FTCAL::hits")) {event.getBank("FTCAL::adc").show();event.getBank("FTCAL::hits").show();}
                    }                            
                } 
            }
        }
    }

    public void analyze() {
//        System.out.println("Analyzing");
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_" + key);
            F1D ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_" + key);
            this.initTimeGaussFitPar(ftime,htime);
            DataFitter.fit(ftime,htime,"LQ");
            
            this.getDataGroup().getItem(1,1,key).getGraph("gtoffsets").addPoint(key, ftime.getParameter(1), 0, ftime.parameter(1).error());
        
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(1),      "offset",       1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(1).error(), "offset_error", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(2),      "resolution" ,  1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 4);
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.5*hRMS)); 
        double rangeMax = (hMean + (0.4*hRMS));  
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
    
    @Override
    public void drawDataGroup(int sector, int layer, int component) {
        if(this.getDataGroup().hasItem(sector,layer,component)==true){
            DataGroup dataGroup = this.getDataGroup().getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().divide(3,2);
            this.getCanvas().setGridX(false);
            this.getCanvas().setGridY(false);
            this.getCanvas().cd(0);
            this.getCanvas().draw(dataGroup.getH1F("htsum"));
            this.getCanvas().cd(1);
            this.getCanvas().draw(dataGroup.getH1F("htsum_calib"));
            this.getCanvas().cd(2);
            this.getCanvas().draw(dataGroup.getGraph("gtoffsets"));
            this.getCanvas().cd(3);
            this.getCanvas().draw(dataGroup.getH1F("htime_wide_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(dataGroup.getH1F("htime_" + component));
            this.getCanvas().cd(5);
            this.getCanvas().draw(dataGroup.getH1F("htime_calib_" + component));
        }
    }


    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
