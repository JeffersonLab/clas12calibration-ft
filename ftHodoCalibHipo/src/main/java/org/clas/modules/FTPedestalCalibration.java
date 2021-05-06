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
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.DataFitter;
import org.clas.viewer.FTAdjustFit;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;


/**
 *
 * @author devita
 */
public class FTPedestalCalibration extends FTCalibrationModule {
    private int sector = 3;
    private int layer = 1;
    private int component = 1;
    //private int runNumber = 1;
    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    private IndexedTable                 prevCalibIT = null;
    private IndexedTable            TranslationTable = null;
    private int EventTimeline = 100;

    public FTPedestalCalibration(FTDetector d, String name) {
        super(d, name, "pedestal:pedestal_width", "pedestal",3);
    }

    
    @Override
    public void resetEventListener() {
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                int numcomp=21;
                if (ssec%2==1)
                    numcomp=10;
                H1F hgPedestal = new H1F("hgPedestal_" + ssec + "_" + llay, 500, 0., numcomp);
                hgPedestal.setTitleX("component");
                hgPedestal.setTitleY("pedestal");
                hgPedestal.setTitle("fitted pedestal for Sec: " + ssec + " Layer: " + llay);
                hgPedestal.setBinContent(0,400);
                
                GraphErrors  gPedestal = new GraphErrors("gPedestal_"+ ssec + "_" + llay);
                gPedestal.setTitle("Fitted pedestal for Sec: "+ ssec + " Layer: " + llay); //  title
                gPedestal.setTitleX("component"); // X axis title
                gPedestal.setTitleY("Pedestal");   // Y axis title
                gPedestal.setMarkerColor(4); // color from 0-9 for given palette
                gPedestal.setMarkerSize(3);  // size in points on the screen
                gPedestal.setMarkerStyle(2); // Style can be 1 or 2
                gPedestal.addPoint(0.2, 0., 0., 0.);
                gPedestal.addPoint(1.2, 1., 0., 0.);
                
                GraphErrors  gPedestalCal = new GraphErrors("gPedestalCal_"+ ssec + "_" + llay);
                gPedestalCal.setTitle("Fitted pedestal for Sec: "+ ssec + " Layer: " + llay); //  title
                gPedestalCal.setTitleX("component"); // X axis title
                gPedestalCal.setTitleY("Pedestal");   // Y axis title
                gPedestalCal.setMarkerColor(2); // color from 0-9 for given palette
                gPedestalCal.setMarkerSize(3);  // size in points on the screen
                gPedestalCal.setMarkerStyle(1); // Style can be 1 or 2
                gPedestalCal.addPoint(0., 0., 0., 0.);
                gPedestalCal.addPoint(1., 1., 0., 0.);

                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    // initializa calibration constant table
                    this.getCalibrationTable().addEntry(ssec, llay, key);
                    H1F hPedestal = new H1F("hPedestal_" + ssec + "_" + llay + "_" + key, 500, 0., 500);
                    hPedestal.setTitleX("Pedestal");
                    hPedestal.setTitleY("Counts");
                    hPedestal.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    hPedestal.setFillColor(3);

                    H1F hPedestalTimeline = new H1F("hPedestalTimeline_" + ssec + "_" + llay + "_" + key, 200, 0., 500);
                    hPedestalTimeline.setTitleX("Pedestal");
                    hPedestalTimeline.setTitleY("Counts");
                    hPedestalTimeline.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    hPedestalTimeline.setFillColor(3);
                    
                    H1F hgPedestalTimeline = new H1F("hgPedestalTimeline_" + ssec + "_" + llay+ "_" + key, 500, 0., 100);
                    hgPedestalTimeline.setTitleX("Entry");
                    hgPedestalTimeline.setTitleY("pedestal");
                    hgPedestalTimeline.setTitle("Pedestal time line for Sec: " + ssec + " Layer: " + llay +  " Component: " + key);
                    hgPedestalTimeline.setBinContent(0,400);
                    
                    GraphErrors  gPedestalTimeline = new GraphErrors("gPedestalTimeline_"+ ssec + "_" + llay+ "_" + key);
                    gPedestalTimeline.setTitle("Pedestal timeline for Sec: "+ ssec + " Layer: " + llay+ " Component: " + key); //  title
                    gPedestalTimeline.setTitleX("Entry"); // X axis title
                    gPedestalTimeline.setTitleY("Pedestal");   // Y axis title
                    gPedestalTimeline.setMarkerColor(4); // color from 0-9 for given palette
                    gPedestalTimeline.setMarkerSize(3);  // size in points on the screen
                    gPedestalTimeline.setMarkerStyle(2); // Style can be 1 or 2
                    gPedestalTimeline.addPoint(0.2, 0., 0., 0.);
                    gPedestalTimeline.addPoint(1.2, 1., 0., 0.);
                    

                    F1D fPedestal = new F1D("fPedestal_" + ssec + "_" + llay + "_" + key, "[amp]*gaus(x,[mean],[sigma])", 0, 500.0);
                    fPedestal.setParameter(0, 0.0);
                    fPedestal.setParameter(1, 200.0);
                    fPedestal.setParameter(2, 30);
                    fPedestal.setLineColor(2);
                    fPedestal.setLineWidth(3);
             
                    //System.out.println("dame:" + this.getPreviousCalibrationTable().hasEntry(ssec,llay,key));
                    if(this.getPreviousCalibrationTable().hasEntry(ssec,llay,key)) {
                        double ped = this.getPreviousCalibrationTable().getDoubleValue("pedestal", ssec,llay,key);
                        this.getDataGroup().getItem(ssec,llay,key).getGraph("gPedestal_"+ ssec + "_" + llay).addPoint(key+0.2, ped, 0, 0);
                    }
                    
                    DataGroup dg = new DataGroup(4, 1);
                    dg.addDataSet(hPedestal,          0);
                    dg.addDataSet(hgPedestalTimeline, 1);
                    dg.addDataSet(gPedestalTimeline,  1);
                    dg.addDataSet(hPedestalTimeline,  3);
                    dg.addDataSet(fPedestal,          0);
                    dg.addDataSet(gPedestal,          2);
                    dg.addDataSet(gPedestalCal,       2);
                    dg.addDataSet(hgPedestal,         2);
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
        return (int) this.getDataGroup().getItem(isec, ilay, icomp).getH1F("hPedestal_" + isec + "_" + ilay + "_" + icomp).getEntries();
    }
    
    public void processEvent(DataEvent event) {
        if (event.hasBank("RUN::config")){
            DataBank runCONFIG = event.getBank("RUN::config");
            this.runNumber=runCONFIG.getInt("run", 0);
        }
        if (event.hasBank("FTHODO::adc")) {
            DataBank adcFTHODO = event.getBank("FTHODO::adc");
            for (int loop = 0; loop < adcFTHODO.rows(); loop++) {
                int sect = adcFTHODO.getByte("sector", loop);
                int lay = adcFTHODO.getByte("layer", loop);
                int comp = adcFTHODO.getShort("component", loop);
                double chargeFromADC = ((double) adcFTHODO.getInt("ADC", loop))*(LSB*nsPerSample/50);
                if (chargeFromADC>10){
                    int ped = adcFTHODO.getShort("ped", loop);
                    this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestal_" + sect + "_" + lay + "_" + comp).fill(ped);
                    this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).fill(ped);
                    
                    if (getNEvents(sect, lay, comp)%EventTimeline == 0 && getNEvents(sect, lay, comp)>0 && getNEvents(sect, lay, comp)/EventTimeline < 100){
                        double xpoint=getNEvents(sect, lay, comp)/EventTimeline*1.0;
                        int bin = this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).getMaximumBin();
                        double ypoint=this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).getAxis().getBinCenter(bin);
                        this.getDataGroup().getItem(sect, lay, comp).getGraph("gPedestalTimeline_"+ sect + "_" + lay +"_"+comp).addPoint(xpoint, ypoint, 0.0 ,0.0);
                        this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).reset();
                    }
                    else if (getNEvents(sect, lay, comp)%EventTimeline == 0  && getNEvents(sect, lay, comp)/EventTimeline > 100){
                        int ipoint = getNEvents(sect, lay, comp)/EventTimeline%100;
                        double xpoint=ipoint*1.0;
                        int bin = this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).getMaximumBin();
                        double ypoint=this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).getAxis().getBinCenter(bin);
                        this.getDataGroup().getItem(sect, lay, comp).getGraph("gPedestalTimeline_"+ sect + "_" + lay +"_"+comp).setPoint(ipoint, xpoint, ypoint);
                        this.getDataGroup().getItem(sect, lay, comp).getH1F("hPedestalTimeline_" + sect + "_" + lay + "_" + comp).reset();
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
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gPedestalCal_"+ ssec + "_" + llay).reset();
                }
            }
        }
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    H1F hPedestal = this.getDataGroup().getItem(ssec,llay,key).getH1F("hPedestal_" + ssec + "_" + llay + "_" + key);
                    F1D fPedestal = this.getDataGroup().getItem(ssec,llay,key).getF1D("fPedestal_" + ssec + "_" + llay + "_" + key);
                    this.initGausFitPar(fPedestal,hPedestal);
                    DataFitter.fit(fPedestal,hPedestal,"LQ");                    
//                    double chisq=fcharge.getChiSquare()/fcharge.getNDF();
//                    double ampl2tot = fcharge.getParameter(0)/hcharge.getIntegral();
//                    double fitwidth = fcharge.getParameter(2);
//                    boolean ToSetToFitValues= (chisq>0.91 && ampl2tot>0.017  && fitwidth>30 )? true : false;
//                    double mipsen=(llay==1) ? 1.2 : 2.65;
//                    this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineColor(2);
//                    this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineStyle(1);
//                    if (!ToSetToFitValues){
//                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setParameter(1,800.0);
//                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setParameter(2,100.0);
//                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineColor(4);
//                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineStyle(3);
//                    }

                    double pedest= this.getDataGroup().getItem(ssec, llay, key).getF1D("fPedestal_" + ssec + "_" + llay + "_" + key).getParameter(1);
                    double pedesterr= this.getDataGroup().getItem(ssec, llay, key).getF1D("fPedestal_" + ssec + "_" + llay + "_" + key).getParameter(2);
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gPedestalCal_"+ ssec + "_" + llay).addPoint(key, pedest, 0, pedesterr);

                    getCalibrationTable().setDoubleValue(pedest, "pedestal", ssec, llay, key);
                    getCalibrationTable().setDoubleValue(pedesterr, "pedestal_width", ssec, llay, key);
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }

    private void initGausFitPar(F1D fPedestal, H1F hPedestal) {
        double ampl= hPedestal.getBinContent(hPedestal.getMaximumBin());
        double mean = hPedestal.getAxis().getBinCenter(hPedestal.getMaximumBin());
        double gamma = 2.0;
        double rangeMin=mean-3*gamma;
        double rangeMax=mean+3*gamma;
        double pm = (mean*3.)/50.0;
        fPedestal.setRange(rangeMin, rangeMax);
        fPedestal.setParameter(0, ampl);
        fPedestal.setParameter(1, mean);
        fPedestal.setParameter(2, gamma);
        fPedestal.setParLimits(0, ampl*0.9, ampl * 1.2);
        fPedestal.setParLimits(1, mean-pm,mean+pm);
        fPedestal.setParLimits(2, gamma/10, gamma*5);
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
            this.getCanvas().divide(3, 2);
            this.getCanvas().cd(0);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hPedestal_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("fPedestal_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hPedestal_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("fPedestal_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgPedestalTimeline_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestalTimeline_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgPedestalTimeline_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestalTimeline_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgPedestal_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestal_"+ sector + "_" + 1), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestalCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgPedestal_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestal_"+ sector + "_" + 2), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestalCal_"+ sector + "_" + 2), "same");
            this.getCanvas().update();
        }
        else {
            System.out.println(" ERROR: can not find the data group");
        }
    }
    
    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        //System.out.println("Well. it's really working " + col + "  " + row);
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
            this.getCanvas().divide(3, 2);
            this.getCanvas().cd(0);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hPedestal_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("fPedestal_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hPedestal_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("fPedestal_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgPedestalTimeline_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestalTimeline_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgPedestalTimeline_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestalTimeline_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgPedestal_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestal_"+ sector + "_" + 1), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gPedestalCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgPedestal_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestal_"+ sector + "_" + 2), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gPedestalCal_"+ sector + "_" + 2), "same");
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
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gPedestal_"+ ssec + "_" + llay).reset();
                }
            }
        }
        System.out.println("Loading calibration values for module " + this.getName() + " from CCDB");
        setConstantsManager(ccdb);
        this.prevCalibIT = getConstantsManager().getConstants(this.runNumber, "/daq/fadc/fthodo");
        this.TranslationTable = getConstantsManager().getConstants(this.runNumber, "/daq/tt/fthodo");
        for (int slot=3;slot<20;slot++){
            if (slot==11 || slot==12)
                continue;
            for (int chan=0; chan<16; chan++) {
                if (!this.TranslationTable.hasEntry(72, slot, chan))
                    continue;
                int sector = this.TranslationTable.getIntValue("sector", 72, slot, chan);
                int layer = this.TranslationTable.getIntValue("layer", 72, slot, chan);
                int component = this.TranslationTable.getIntValue("component", 72, slot, chan);
                this.getPreviousCalibrationTable().addEntry(sector, layer, component);
                this.getPreviousCalibrationTable().setDoubleValue(this.prevCalibIT.getDoubleValue("pedestal", 72, slot, chan),"pedestal",sector, layer, component);
                double ped = this.getPreviousCalibrationTable().getDoubleValue("pedestal", sector, layer, component);
                //System.out.println(slot+" "+chan+" "+sector+" "+layer+" "+component+" "+ ped );
                this.getDataGroup().getItem(sector, layer, component).getGraph("gPedestal_"+ sector + "_" + layer).addPoint(component+0.2, ped, 0, 0);
            }
        }
    }
    
    
    @Override
    public void adjustFit() {
        System.out.println("Adjusting Charge fit for Sector " + this.sector +" Layer " + this.layer +" Component " + this.component);
        H1F histtofit =  this.getDataGroup().getItem(this.sector,this.layer,this.component).getH1F("hPedestal_" + sector + "_" + layer + "_" + component);
        F1D ftofit = this.getDataGroup().getItem(this.sector,this.layer,this.component).getF1D("fPedestal_" + sector + "_" + layer + "_" + component);
        FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ", "energy");
        cfit.setCalibTable(getCalibrationTable());
        cfit.setSecLayComp(this.sector,this.layer,this.component);
        cfit.setGraphToUpdate(this.getDataGroup().getItem(this.sector,this.layer,this.component).getGraph("g_PedestalCal_"+ this.sector + "_" + this.layer));
    }

}
