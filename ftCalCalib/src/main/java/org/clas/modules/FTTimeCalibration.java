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
public class FTTimeCalibration extends FTCalibrationModule {

    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    double clusterThr = 50.0;// Vertical selection
    double singleChThr = 0.00;// Single channel selection MeV
    double signalThr =100.0;
    double ftcalDistance =1898; //mm
    double timeshift =0;// ns
    double crystal_size = 15.3;//mm
    double charge2e = 15.3/6.005; //MeV
    double crystal_length = 200;//mm                                                                                            
    double shower_depth = 65;                                                                                                   
    double light_speed = 150; //cm/ns     
    double c = 29.97; //cm/ns     

    double startTime   = 124.25;//ns
    double trigger     = 11;//ns
    double triggerPhase = 0;
    
    public FTTimeCalibration(FTDetector d, String name) {
        super(d, name, "offset:offset_error:resolution",3);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 330, -500.0, 50.0);
        htsum.setTitleX("Time Offset (ns)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Global Time Offset");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 200, -20.0, 20.0);
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

            // initialize data group
            H1F htime_wide = new H1F("htime_wide_" + key, 330, -500.0, 50.0);
            htime_wide.setTitleX("Time (ns)");
            htime_wide.setTitleY("Counts");
            htime_wide.setTitle("Component " + key);
            H1F htime = new H1F("htime_" + key, 300, this.getRange()[0], this.getRange()[1]);
            htime.setTitleX("Time (ns)");
            htime.setTitleY("Counts");
            htime.setTitle("Component " + key);
            H1F htime_calib = new H1F("htime_calib_" + key, 300, -30., 30.);
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
        this.startTime = -100000;
        this.triggerPhase=0;
        // get trigger phase
        if(event.hasBank("RUN::config")) {
            DataBank recConfig = event.getBank("RUN::config");
            long timestamp = recConfig.getLong("timestamp",0);    
            int phase_offset = 1;
            triggerPhase=((timestamp%6)+phase_offset)%6; // TI derived phase correction due to TDC and FADC clock differences
        }
        
        // get start time
        if(event.hasBank("REC::Event")) {
            DataBank recEvent = event.getBank("REC::Event");
            this.startTime = recEvent.getFloat("STTime", 0);
//            if(this.startTime>-1000)System.out.println(this.startTime);
        }
        if(event.hasBank("REC::Particle")) {
            DataBank recPart = event.getBank("REC::Particle");
            this.trigger = recPart.getInt("pid", 0);
//            System.out.println(this.startTime);
        }
        if (event.hasBank("FTCAL::adc") && this.startTime>-1000 /*&& trigger==11*/) {
            DataBank adcFTCAL = event.getBank("FTCAL::adc");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            for (int loop = 0; loop < adcFTCAL.rows(); loop++) {
                int    key    = adcFTCAL.getInt("component", loop);
                int    adc    = adcFTCAL.getInt("ADC", loop);
                double time   = adcFTCAL.getFloat("time", loop);                
                double charge =((double) adc)*(LSB*nsPerSample/50)*charge2e;
                double radius = Math.sqrt(Math.pow(this.getDetector().getIdX(key)-0.5,2.0)+Math.pow(this.getDetector().getIdY(key)-0.5,2.0))*this.crystal_size;//meters
                double path   = Math.sqrt(Math.pow(this.ftcalDistance+shower_depth,2)+Math.pow(radius,2));
                double tof    = (path/10/c); //ns
                double timec  = (time + 0*this.triggerPhase*4 -(this.startTime + (crystal_length-shower_depth)/light_speed + tof))-this.timeshift;
                if(charge>signalThr) {
                    this.getDataGroup().getItem(1,1,key).getH1F("htsum").fill(timec);
                    this.getDataGroup().getItem(1,1,key).getH1F("htime_wide_"+key).fill(timec);
                    this.getDataGroup().getItem(1,1,key).getH1F("htime_"+key).fill(timec);
                    if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                        double offset = this.getPreviousCalibrationTable().getDoubleValue("offset", 1, 1, key);
                        this.getDataGroup().getItem(1,1,key).getH1F("htsum_calib").fill(timec-offset);
                        this.getDataGroup().getItem(1,1,key).getH1F("htime_calib_"+key).fill(timec-offset);                        
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
