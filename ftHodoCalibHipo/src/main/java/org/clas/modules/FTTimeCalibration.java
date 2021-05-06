/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.jlab.utils.groups.IndexedList;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTAdjustFit;
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
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.utils.groups.IndexedTable;
/**
 *
 * @author devita
 */
public class FTTimeCalibration extends FTCalibrationModule {
private int sector = 3;
    private int layer = 1;
    private int component = 1;
    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    double c = 29.97; //cm/ns
    double startTime   = 124.25;//ns
    double trigger = 11;//ns
    //private int runNumber = 1;
    private IndexedTable                 prevCalibIT = null;

    public FTTimeCalibration(FTDetector d, String name) {
        super(d, name, "time_offset:offset_error:time_rms", "time_offset:time_rms",3);
    }

    @Override
    public void resetEventListener() {
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                int numcomp=21;
                if (ssec%2==1)
                    numcomp=10;
                H1F hgToffsets = new H1F("hgToffsets_" + ssec + "_" + llay, 800, 0., numcomp);
                hgToffsets.setTitleX("component");
                hgToffsets.setTitleY("offset [ns]");
                hgToffsets.setTitle("Time offsets for Sec: " + ssec + " Layer: " + llay);
                hgToffsets.setBinContent(1,60);
                //hgToffsets.setBinContent(2,10);
                
                GraphErrors  gToffsets = new GraphErrors("gToffsets_"+ ssec + "_" + llay);
                gToffsets.setTitle("Time offsets for Sec: "+ ssec + " Layer: " + llay); //  title
                gToffsets.setTitleX("component"); // X axis title
                gToffsets.setTitleY("time [ns]");   // Y axis title
                gToffsets.setMarkerColor(4); // color from 0-9 for given palette
                gToffsets.setMarkerSize(3);  // size in points on the screen
                gToffsets.setMarkerStyle(2); // Style can be 1 or 2
                gToffsets.addPoint(0.2, 20., 0., 0.);
                gToffsets.addPoint(1.2, 60., 0., 0.);
                
                GraphErrors  gToffsetsCal = new GraphErrors("gToffsetsCal_"+ ssec + "_" + llay);
                gToffsetsCal.setTitle("Time offsets for Sec: "+ ssec + " Layer: " + llay); //  title
                gToffsetsCal.setTitleX("component"); // X axis title
                gToffsetsCal.setTitleY("time [ns]");   // Y axis title
                gToffsetsCal.setMarkerColor(2); // color from 0-9 for given palette
                gToffsetsCal.setMarkerSize(3);  // size in points on the screen
                gToffsetsCal.setMarkerStyle(1); // Style can be 1 or 2
                gToffsetsCal.addPoint(0., 20., 0., 0.);
                gToffsetsCal.addPoint(1., 60., 0., 0.);

                H2F htime_calSect = new H2F("htime_calSect_" + ssec + "_" + llay, 50, -12., 12., numcomp+1, 0, numcomp+1);
                htime_calSect.setTitleX("Time [ns]");
                htime_calSect.setTitleY("Component");
                htime_calSect.setTitle("Calibrated Sector: " + ssec + " Layer: " + llay );
                
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    // initializa calibration constant table
                    this.getCalibrationTable().addEntry(ssec, llay, key);
                    H1F htime_wide = new H1F("htime_wide_" + ssec + "_" + llay + "_" + key, 200, -300.0, 300.0);
                    htime_wide.setTitleX("Time [ns]");
                    htime_wide.setTitleY("Counts");
                    htime_wide.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    htime_wide.setFillColor(3);

                    H1F htime_small = new H1F("htime_small_" + ssec + "_" + llay + "_" + key, 2400, -300,300);
                    htime_small.setTitleX("Time [ns]");
                    htime_small.setTitleY("Counts");
                    htime_small.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    htime_small.setFillColor(3);

                    H1F htime = new H1F("htime_" + ssec + "_" + llay + "_" + key, 240, -30,30);
                    htime.setTitleX("Time [ns]");
                    htime.setTitleY("Counts");
                    htime.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    htime.setFillColor(3);
                    
                    
                    //F1D ftime = new F1D("ftime_" + ssec + "_" + llay + "_" + key, "[amp]*gaus(x,[mean],[sigma])", -10., 10.);
                    F1D ftime = new F1D("ftime_" + ssec + "_" + llay + "_" + key, "[amp]*gaus(x,[mean],[sigma])", 30., 45.);
                    ftime.setParameter(0, 0.0);
                    ftime.setParameter(1, 0.0);
                    ftime.setParameter(2, 2.0);
                    ftime.setLineColor(2);
                    ftime.setLineWidth(3);
                    
                    DataGroup dg = new DataGroup(4, 1);
                    dg.addDataSet(htime_wide,       1);
                    dg.addDataSet(htime,            0);
                    dg.addDataSet(htime_small,      1);
                    dg.addDataSet(ftime,            0);
                    dg.addDataSet(hgToffsets,       2);
                    dg.addDataSet(gToffsets,        2);
                    dg.addDataSet(gToffsetsCal,     2);
                    dg.addDataSet(htime_calSect,    3);

                    this.getDataGroup().add(dg, ssec, llay, key);
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }
    
    
    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }
    
    public int getNEvents(int isec, int ilay, int icomp) {
        return (int) this.getDataGroup().getItem(isec, ilay, icomp).getH1F("htime_small_" + isec + "_" + ilay + "_" + icomp).getEntries();
    }
 
    public void processEvent(DataEvent event) {
        this.startTime = -100000;

        if (event.hasBank("RUN::config")){
            DataBank runCONFIG = event.getBank("RUN::config");
            this.runNumber=runCONFIG.getInt("run", 0);
        }
        
        if(event.hasBank("REC::Event")) {
            DataBank recEvent = event.getBank("REC::Event");
            this.startTime = recEvent.getFloat("startTime", 0);
        }
        if(event.hasBank("REC::Particle")) {
            DataBank recPart = event.getBank("REC::Particle");
            this.trigger = recPart.getInt("pid", 0);
        }
        
        
        if (event.hasBank("FTHODO::hits") && event.hasBank("FTHODO::adc") && this.startTime>-1000 && trigger==11) {
            DataBank hitsFTHODO = event.getBank("FTHODO::hits");
            DataBank adcFTHODO = event.getBank("FTHODO::adc");
            for (int loop1 = 0; loop1 < hitsFTHODO.rows()-1; loop1++) {
                int clusterID1 = hitsFTHODO.getShort("clusterID", loop1);
                int hitID1 = hitsFTHODO.getShort("hitID", loop1);
                int sect1 = hitsFTHODO.getByte("sector", loop1);
                int lay1 = hitsFTHODO.getByte("layer", loop1);
                int comp1 = hitsFTHODO.getShort("component", loop1);
                double charge1FromADC = ((double) adcFTHODO.getInt("ADC", hitID1))*(LSB*nsPerSample/50);
                double time1   = adcFTHODO.getFloat("time", hitID1);
                double radius1 = Math.sqrt(Math.pow(hitsFTHODO.getFloat("x",loop1),2.0)+Math.pow(hitsFTHODO.getFloat("y",loop1),2.0));//cm
                double path1   = Math.sqrt(Math.pow(hitsFTHODO.getFloat("z",loop1),2)+Math.pow(radius1,2));
                double tof1    = (path1/c); //ns
                double timediff1  = (time1 -(this.startTime + tof1));
                for (int loop2 = loop1+1; loop2 < hitsFTHODO.rows(); loop2++) {
                    int clusterID2 = hitsFTHODO.getShort("clusterID", loop2);
                    int hitID2 = hitsFTHODO.getShort("hitID", loop2);
                    int sect2 = hitsFTHODO.getByte("sector", loop2);
                    int lay2 = hitsFTHODO.getByte("layer", loop2);
                    int comp2 = hitsFTHODO.getShort("component", loop2);
                    double charge2FromADC = ((double) adcFTHODO.getInt("ADC", hitID2))*(LSB*nsPerSample/50);
                    double time2   = adcFTHODO.getFloat("time", hitID2);
                    double radius2 = Math.sqrt(Math.pow(hitsFTHODO.getFloat("x",loop2),2.0)+Math.pow(hitsFTHODO.getFloat("y",loop2),2.0));//cm
                    double path2   = Math.sqrt(Math.pow(hitsFTHODO.getFloat("z",loop2),2)+Math.pow(radius2,2));
                    double tof2    = (path2/c); //ns
                    double timediff2  = (time2 -(this.startTime + tof2));
                    if (clusterID1==clusterID2){
                        this.getDataGroup().getItem(sect1,lay1,comp1).getH1F("htime_wide_" + sect1 + "_" + lay1 + "_" + comp1).fill(timediff1);
                        this.getDataGroup().getItem(sect1,lay1,comp1).getH1F("htime_small_" + sect1 + "_" + lay1 + "_" + comp1).fill(timediff1);
                        this.getDataGroup().getItem(sect2,lay2,comp2).getH1F("htime_wide_" + sect2 + "_" + lay2 + "_" + comp2).fill(timediff2);
                        this.getDataGroup().getItem(sect2,lay2,comp2).getH1F("htime_small_" + sect2 + "_" + lay2 + "_" + comp2).fill(timediff2);
//                        if(this.getPreviousCalibrationTable().hasEntry(sect1,lay1,comp1)) {
//                            double offset = this.getPreviousCalibrationTable().getDoubleValue("time_offset", sect1,lay1,comp1);
//                            double rms = this.getPreviousCalibrationTable().getDoubleValue("time_rms", sect1,lay1,comp1);
//                            this.getDataGroup().getItem(sect1,lay1,comp1).getH1F("htime_calibPre_"+ sect1 + "_" + lay1 + "_" + comp1).fill(timediff1-offset);
//                            this.getDataGroup().getItem(sect1,lay1,comp1).getGraph("gToffsets"+ sect1 + "_" + lay1).addPoint(comp1, offset, 0, rms);
//                        }
//                        if(this.getPreviousCalibrationTable().hasEntry(sect2,lay2,comp2)) {
//                            double offset = this.getPreviousCalibrationTable().getDoubleValue("time_offset", sect2,lay2,comp2);
//                            double rms = this.getPreviousCalibrationTable().getDoubleValue("time_rms", sect2,lay2,comp2);
//                            this.getDataGroup().getItem(sect2,lay2,comp2).getH1F("htime_calibPre_"+ sect2 + "_" + lay2 + "_" + comp2).fill(timediff2-offset);
//                            this.getDataGroup().getItem(sect2,lay2,comp2).getGraph("gToffsets"+ sect2 + "_" + lay2).addPoint(comp2, offset, 0, rms);
//                        }
                        break;
                    }
                }
            }
        }
    }
            
    public void analyze() {
        System.out.println("Analyzing");
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gToffsetsCal_"+ ssec + "_" + llay).reset();
                }
            }
        }
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    H1F htime_small = this.getDataGroup().getItem(ssec,llay,key).getH1F("htime_small_"+ ssec + "_" + llay+ "_" + key);
                    H1F htime = this.getDataGroup().getItem(ssec,llay,key).getH1F("htime_"+ ssec + "_" + llay+ "_" + key);
                    F1D ftime = this.getDataGroup().getItem(ssec,llay,key).getF1D("ftime_"+ ssec + "_" + llay+ "_" + key);
                    this.initTimeGaussFitPar(ftime,htime_small, htime);
                    DataFitter.fit(ftime,htime,"LQ");
                    double chisq=ftime.getChiSquare()/ftime.getNDF();
                    double integral = htime.getIntegral();
                    double fitwidth = ftime.getParameter(2);
                    double ampl = ftime.getParameter(0);
                    boolean ToSetToFitValues= (chisq<6.0 && integral>20  && fitwidth>0.4 && ampl>5 )? true : false;
                    this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setLineColor(2);
                    this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setLineStyle(1);
                    if (!ToSetToFitValues){
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setParameter(1,-100.0);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setParameter(2,50.0);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setLineColor(4);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay + "_" + key).setLineStyle(3);
                    }
                    double newOffset=ftime.getParameter(1);
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gToffsetsCal_"+ ssec + "_" + llay).addPoint(key, ftime.getParameter(1), 0, ftime.parameter(1).error()+ftime.getParameter(2));
                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay+ "_" + key).getParameter(1),      "time_offset",       ssec, llay, key);
                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay+ "_" + key).parameter(1).error(), "offset_error", ssec, llay, key);
                    getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(ssec, llay, key).getF1D("ftime_" + ssec + "_" + llay+ "_" + key).getParameter(2),      "time_rms" ,  ssec, llay, key);

                    for (int bin=0; bin<htime.getAxis().getNBins(); bin++) {
                        double bincontent =  htime.getBinContent(bin);
                        double bincent = htime.getDataX(bin);
                        this.getDataGroup().getItem(ssec,llay,key).getH2F("htime_calSect_" + ssec + "_" + llay).fill(bincent-newOffset,key,bincontent);
                    }
                }
            }
        }
        
        getCalibrationTable().fireTableDataChanged();
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime_small, H1F htime) {
        //System.out.println("Here");
        double maxbin=htime_small.getXaxis().getBinCenter(htime_small.getMaximumBin())-htime_small.getXaxis().getBinWidth(5)/2.0;
        //System.out.println("Here1:"+maxbin);
        if (maxbin>250 || maxbin<-250)
            maxbin=0.0;
        htime.set(120, maxbin-15.0,maxbin+15.0);
        
        for(int bin = 0; bin < htime.getXaxis().getNBins(); bin++){
            double xval=htime.getXaxis().getBinCenter(bin);
            int binval=htime_small.getXaxis().getBin(xval);
            double bincont=htime_small.getBinContent(binval);
            htime.setBinContent(bin,bincont);
            //System.out.println(" bin: "+bin+"Max: "+maxbin+" xval: "+xval+" binval: "+binval+" bincon: "+bincont);
        }
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        //double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hMean = htime.getMean();
        //double hRMS  = 2; //ns
        double hRMS  = htime.getRMS(); //ns
        double rangeMin = (hMean - (0.6*hRMS));
        double rangeMax = (hMean + (0.6*hRMS));
        double pm = 0.4*hRMS;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.85, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+pm);
        ftime.setParameter(2, 0.9);
        ftime.setParLimits(2, 0.05*hRMS, 0.8*hRMS);
    }
    
    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        if (this.getDetector().hasComponent(sector,layer,key)) {
            int nent = this.getNEvents(sector, layer, key);
            if (nent > 0) {
                col = palette.getColor3D(nent, this.getnProcessed(), true);
            }
        }
//        col = new Color(100, 0, 0);
        return col;
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
        // plot histos for the specific component
        this.sector = dsd.getDescriptor().getSector();
        this.layer  = dsd.getDescriptor().getLayer();
        this.component = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + component);
        IndexedList<DataGroup> group = this.getDataGroup();
        if(group.hasItem(sector,layer,component)==true){
            this.getCanvas().clear();
            this.getCanvas().divide(4, 2);
            this.getCanvas().cd(0);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("htime_small_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("htime_small_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("htime_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("ftime_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("htime_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("ftime_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH2F("htime_calSect_" + sector + "_" + 1));
            this.getCanvas().cd(6);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH2F("htime_calSect_" + sector + "_" + 2));
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgToffsets_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gToffsets_"+ sector + "_" + 1),"same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gToffsetsCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(7);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgToffsets_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gToffsets_"+ sector + "_" + 2),"same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gToffsetsCal_"+ sector + "_" + 2), "same");
            this.getCanvas().update();
        }
        else {
            System.out.println(" ERROR: can not find the data group");
        }
    }
    
    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's really working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = this.getDataGroup();
        
        this.sector    = Integer.parseInt(str_sector);
        this.layer     = Integer.parseInt(str_layer);
        this.component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            this.getCanvas().clear();
            this.getCanvas().divide(4, 2);
            this.getCanvas().cd(0);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("htime_small_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("htime_small_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("htime_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("ftime_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("htime_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("ftime_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH2F("htime_calSect_" + sector + "_" + 1));
            this.getCanvas().cd(6);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH2F("htime_calSect_" + sector + "_" + 2));
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgToffsets_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gToffsets_"+ sector + "_" + 1),"same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gToffsetsCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(7);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgToffsets_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gToffsets_"+ sector + "_" + 2),"same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gToffsetsCal_"+ sector + "_" + 2), "same");
            this.getCanvas().update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
    }

    @Override
    public void timerUpdate() {
        this.analyze();
    }
    
    
    @Override
    public void loadConstants(ConstantsManager ccdb) {
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gToffsets_"+ ssec + "_" + llay).reset();
                }
            }
        }
        System.out.println("Loading calibration values for module " + this.getName() + " from CCDB");
        setConstantsManager(ccdb);
        this.prevCalibIT = getConstantsManager().getConstants(this.runNumber, "/calibration/ft/fthodo/time_offsets");
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    this.getPreviousCalibrationTable().addEntry(ssec, llay, key);
                    this.getPreviousCalibrationTable().setDoubleValue(this.prevCalibIT.getDoubleValue("time_offset", ssec, llay, key),"time_offset",ssec, llay, key);
                    this.getPreviousCalibrationTable().setDoubleValue(this.prevCalibIT.getDoubleValue("time_rms", ssec, llay, key),"time_rms",ssec, llay, key);
                    
                    double timeoffset = this.getPreviousCalibrationTable().getDoubleValue("time_offset", ssec,llay,key);
                    double timerms = this.getPreviousCalibrationTable().getDoubleValue("time_rms", ssec,llay,key);
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gToffsets_"+ ssec + "_" + llay).addPoint(key+0.2, timeoffset, 0, timerms);
                }
            }
        }
    }
    
    
     @Override
    public void adjustFit() {
        System.out.println("Adjusting Time fit for Sector " + this.sector +" Layer " + this.layer +" Component " + this.component);
        H1F histtofit =  this.getDataGroup().getItem(this.sector,this.layer,this.component).getH1F("htime_" + sector + "_" + layer + "_" + component);
        F1D ftofit = this.getDataGroup().getItem(this.sector,this.layer,this.component).getF1D("ftime_" + sector + "_" + layer + "_" + component);
        FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ", "time");
        cfit.setCalibTable(getCalibrationTable());
        cfit.setSecLayComp(this.sector,this.layer,this.component);
        cfit.setGraphToUpdate(this.getDataGroup().getItem(this.sector,this.layer,this.component).getGraph("gToffsetsCal_"+ this.sector + "_" + this.layer));
    }
    
    
    
    
}
