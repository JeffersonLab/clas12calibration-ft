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
import java.util.Objects;
import org.jlab.utils.groups.IndexedList;
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
import org.jlab.groot.data.H2F;
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
public class FTEnergyCalibration extends FTCalibrationModule {
    private int sector = 3;
    private int layer = 1;
    private int component = 1;
//    private int runNumber = 1;
    // analysis realted info
    double nsPerSample=4;
    double LSB = 0.4884;
    private IndexedTable                 prevCalibIT = null;

    public FTEnergyCalibration(FTDetector d, String name) {
        super(d, name, "mips_charge:mips_charge_error:mips_energy", "mips_charge:mips_energy",3);
    }

    
    @Override
    public void resetEventListener() {
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                int numcomp=21;
                if (ssec%2==1)
                    numcomp=10;
                H1F hgCharge = new H1F("hgCharge_" + ssec + "_" + llay, 500, 0., numcomp);
                hgCharge.setTitleX("component");
                hgCharge.setTitleY("charge [pC]");
                hgCharge.setTitle("fitted charge for Sec: " + ssec + " Layer: " + llay);
                hgCharge.setBinContent(0,500);
                
                GraphErrors  gCharge = new GraphErrors("gCharge_"+ ssec + "_" + llay);
                gCharge.setTitle("Fitted charge for Sec: "+ ssec + " Layer: " + llay); //  title
                gCharge.setTitleX("component"); // X axis title
                gCharge.setTitleY("Charge [pC]");   // Y axis title
                gCharge.setMarkerColor(4); // color from 0-9 for given palette
                gCharge.setMarkerSize(3);  // size in points on the screen
                gCharge.setMarkerStyle(2); // Style can be 1 or 2
                gCharge.addPoint(0.2, 0., 0., 0.);
                gCharge.addPoint(1.2, 1., 0., 0.);
                
                GraphErrors  gChargeCal = new GraphErrors("gChargeCal_"+ ssec + "_" + llay);
                gChargeCal.setTitle("Fitted charge for Sec: "+ ssec + " Layer: " + llay); //  title
                gChargeCal.setTitleX("component"); // X axis title
                gChargeCal.setTitleY("Charge [pC]");   // Y axis title
                gChargeCal.setMarkerColor(2); // color from 0-9 for given palette
                gChargeCal.setMarkerSize(3);  // size in points on the screen
                gChargeCal.setMarkerStyle(1); // Style can be 1 or 2
                gChargeCal.addPoint(0., 0., 0., 0.);
                gChargeCal.addPoint(1., 1., 0., 0.);

                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    // initializa calibration constant table
                    this.getCalibrationTable().addEntry(ssec, llay, key);
                    H1F hcharge = new H1F("hcharge_" + ssec + "_" + llay + "_" + key, 200, 0., 1500);
                    hcharge.setTitleX("Charge [pC]");
                    hcharge.setTitleY("Counts");
                    hcharge.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    hcharge.setFillColor(3);

                    H1F henergy = new H1F("henergy_" + ssec + "_" + llay + "_" + key, 150,0., 10);
                    henergy.setTitleX("Energy [MeV]");
                    henergy.setTitleY("Counts");
                    henergy.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    henergy.setFillColor(3);

                    H1F henergycalib = new H1F("henergycalib_" + ssec + "_" + llay + "_" + key, 200,0., 10);
                    henergycalib.setTitleX("Energy [MeV]");
                    henergycalib.setTitleY("Counts");
                    henergycalib.setTitle("Sector: " + ssec + " Layer: " + llay + " Component: "+ key);
                    henergycalib.setFillColor(2);
                    henergycalib.setLineColor(2);

                    F1D fcharge = new F1D("fcharge_" + ssec + "_" + llay + "_" + key, "[amp]*landau(x,[mean],[gamma])+[amp2]*exp(x*[scale])", 50, 500.0);
                    fcharge.setParameter(0, 0.0);
                    fcharge.setParameter(1, 180.0);
                    fcharge.setParameter(2, 25);
                    fcharge.setParameter(3, 0.0);
                    fcharge.setParameter(4, 0.0);
                    fcharge.setLineColor(2);
                    fcharge.setLineWidth(3);
             
                    //System.out.println("dame:" + this.getPreviousCalibrationTable().hasEntry(ssec,llay,key));
                    if(this.getPreviousCalibrationTable().hasEntry(ssec,llay,key)) {
                        double charge2cal = this.getPreviousCalibrationTable().getDoubleValue("mips_charge", ssec,llay,key);
                        this.getDataGroup().getItem(ssec,llay,key).getGraph("gCharge_"+ ssec + "_" + llay).addPoint(key+0.2, charge2cal, 0, 0);
                    }
                    
                    DataGroup dg = new DataGroup(3, 1);
                    dg.addDataSet(hcharge,       0);
                    dg.addDataSet(henergy,       1);
                    dg.addDataSet(henergycalib,  1);
                    dg.addDataSet(fcharge,       0);
                    dg.addDataSet(gCharge,       2);
                    dg.addDataSet(gChargeCal,    2);
                    dg.addDataSet(hgCharge,      2);
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
        return this.getDataGroup().getItem(isec, ilay, icomp).getH1F("hcharge_" + isec + "_" + ilay + "_" + icomp).getEntries();
    }
    
    public void processEvent(DataEvent event) {
        if (event.hasBank("RUN::config")){
            DataBank runCONFIG = event.getBank("RUN::config");
            this.runNumber=runCONFIG.getInt("run", 0);
        }
        
        if (event.hasBank("FTHODO::hits") && event.hasBank("FTHODO::adc")) {
            DataBank hitsFTHODO = event.getBank("FTHODO::hits");
            DataBank adcFTHODO = event.getBank("FTHODO::adc");
            for (int loop1 = 0; loop1 < hitsFTHODO.rows()-1; loop1++) {
                for (int loop2 = loop1+1; loop2 < hitsFTHODO.rows(); loop2++) {
                    int clusterID1 = hitsFTHODO.getShort("clusterID", loop1);
                    int clusterID2 = hitsFTHODO.getShort("clusterID", loop2);
                    int hitID1 = hitsFTHODO.getShort("hitID", loop1);
                    int hitID2 = hitsFTHODO.getShort("hitID", loop2);
                    int sect1 = hitsFTHODO.getByte("sector", loop1);
                    int lay1 = hitsFTHODO.getByte("layer", loop1);
                    int comp1 = hitsFTHODO.getShort("component", loop1);
                    int sect2 = hitsFTHODO.getByte("sector", loop2);
                    int lay2 = hitsFTHODO.getByte("layer", loop2);
                    int comp2 = hitsFTHODO.getShort("component", loop2);
                    double charge1FromADC = ((double) adcFTHODO.getInt("ADC", hitID1))*(LSB*nsPerSample/50);
                    double charge2FromADC = ((double) adcFTHODO.getInt("ADC", hitID2))*(LSB*nsPerSample/50);
                    if (clusterID1==clusterID2){
                        this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("hcharge_" + sect1 + "_" + lay1 + "_" + comp1).fill(charge1FromADC);
                        this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("hcharge_" + sect2 + "_" + lay2 + "_" + comp2).fill(charge2FromADC);
                        this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("henergy_" + sect1 + "_" + lay1 + "_" + comp1).fill(hitsFTHODO.getFloat("energy", loop1));
                        this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("henergy_" + sect2 + "_" + lay2 + "_" + comp2).fill(hitsFTHODO.getFloat("energy", loop2));
                        double mipsen1=(lay1==1) ? 1.2 : 2.65;
                        double mipsen2=(lay2==1) ? 1.2 : 2.65;
                        int bin1 =  this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("henergy_" + sect1 + "_" + lay1 + "_" + comp1).getMaximumBin();
                        double cont1 = this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("henergy_" + sect1 + "_" + lay1 + "_" + comp1).getBinContent(bin1);
                        int bin2 =  this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("henergy_" + sect2 + "_" + lay2 + "_" + comp2).getMaximumBin();
                        double cont2 = this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("henergy_" + sect2 + "_" + lay2 + "_" + comp2).getBinContent(bin2);
                        this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("henergycalib_" + sect1 + "_" + lay1 + "_" + comp1).reset();
                        this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("henergycalib_" + sect2 + "_" + lay2 + "_" + comp2).reset();                      
                        this.getDataGroup().getItem(sect1, lay1, comp1).getH1F("henergycalib_" + sect1 + "_" + lay1 + "_" + comp1).fill(mipsen1, cont1);
                        this.getDataGroup().getItem(sect2, lay2, comp2).getH1F("henergycalib_" + sect2 + "_" + lay2 + "_" + comp2).fill(mipsen2, cont2);
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
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gChargeCal_"+ ssec + "_" + llay).reset();
                }
            }
        }
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    H1F hcharge = this.getDataGroup().getItem(ssec,llay,key).getH1F("hcharge_" + ssec + "_" + llay + "_" + key);
                    F1D fcharge = this.getDataGroup().getItem(ssec,llay,key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key);
                    this.initLandauFitPar(fcharge,hcharge);
                    DataFitter.fit(fcharge,hcharge,"LQ");
                    double chisq=fcharge.getChiSquare()/fcharge.getNDF();
                    double ampl2tot = fcharge.getParameter(0)/hcharge.getIntegral();
                    double fitwidth = fcharge.getParameter(2);
                    boolean ToSetToFitValues= (chisq>0.89 && ampl2tot>0.014  && fitwidth>18 )? true : false;
                    double mipsen=(llay==1) ? 1.2 : 2.65;
                    this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineColor(2);
                    this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineStyle(1);
                    if (!ToSetToFitValues){
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setParameter(1,500.0);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setParameter(2,200.0);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineColor(4);
                        this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).setLineStyle(3);
                    }

                    double mipscharge= this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).getParameter(1);
                    double mipschargeerror= this.getDataGroup().getItem(ssec, llay, key).getF1D("fcharge_" + ssec + "_" + llay + "_" + key).getParameter(2);
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gChargeCal_"+ ssec + "_" + llay).addPoint(key, mipscharge, 0, fcharge.parameter(1).error()+mipschargeerror);

//                    for (int bin=0; bin<hcharge.getAxis().getNBins(); bin++) {
//                        double bincontent =  hcharge.getBinContent(bin);
//                        double bincent = hcharge.getDataX(bin);
//                        double newbincenter=mipsen*bincent/mipscharge;
//                        this.getDataGroup().getItem(ssec,llay,key).getH1F("henergycalib_"+ ssec + "_" + llay+ "_" + key).fill(newbincenter,bincontent);
//                    }
                    
                    getCalibrationTable().setDoubleValue(mipscharge, "mips_charge", ssec, llay, key);
                    getCalibrationTable().setDoubleValue(mipschargeerror, "mips_charge_error", ssec, llay, key);
                    getCalibrationTable().setDoubleValue(mipsen, "mips_energy", ssec, llay, key);                    
                }
            }
        }
        getCalibrationTable().fireTableDataChanged();
    }

    private void initLandauFitPar(F1D fcharge, H1F hcharge) {

        double mean= hcharge.getMean()-45.0;
        double ampl = hcharge.getBinContent(hcharge.getXaxis().getBin(mean)); //set as starting amplitude the value of the bin at mean
        double gamma = hcharge.getRMS()/4.0;
        double exp0 = hcharge.getBinContent(hcharge.getMaximumBin()); //set as starting amplitude of exponential the value of the bin at xmin
        double exp1=-0.001;
        double min=hcharge.getAxis().min();
        double max=hcharge.getAxis().max();
        if (hcharge.getMean()>320){
           fcharge.setRange(100, 800);
           fcharge.setParLimits(1, 250,800);
        }
        else {
          fcharge.setRange(50, 500);
          fcharge.setParLimits(1, 110,600);
        }
        fcharge.setParameter(0, ampl);
        fcharge.setParameter(1, mean);
        fcharge.setParameter(2, gamma);
        fcharge.setParameter(3, exp0);
        fcharge.setParameter(4, exp1);
        fcharge.setParLimits(0, 0, ampl * 100.0);
        fcharge.setParLimits(2, gamma/10, gamma*10);
        fcharge.setParLimits(3, exp0 * 0.005, exp0 * 100.0);
        fcharge.setParLimits(4, -1.0, 0);
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
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hcharge_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("fcharge_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hcharge_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("fcharge_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("henergy_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("henergycalib_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("henergy_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("henergycalib_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgCharge_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gCharge_"+ sector + "_" + 1), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gChargeCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgCharge_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gCharge_"+ sector + "_" + 2), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gChargeCal_"+ sector + "_" + 2), "same");
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
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hcharge_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getF1D("fcharge_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(3);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hcharge_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getF1D("fcharge_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(1);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("henergy_" + sector + "_" + 1 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("henergycalib_" + sector + "_" + 1 + "_" + component),"same");
            this.getCanvas().cd(4);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("henergy_" + sector + "_" + 2 + "_" + component));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("henergycalib_" + sector + "_" + 2 + "_" + component),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getH1F("hgCharge_"+ sector + "_" + 1));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gCharge_"+ sector + "_" + 1), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,1,component).getGraph("gChargeCal_"+ sector + "_" + 1), "same");
            this.getCanvas().cd(5);
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getH1F("hgCharge_"+ sector + "_" + 2));
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gCharge_"+ sector + "_" + 2), "same");
            this.getCanvas().draw(this.getDataGroup().getItem(sector,2,component).getGraph("gChargeCal_"+ sector + "_" + 2), "same");
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
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gCharge_"+ ssec + "_" + llay).reset();
                }
            }
        }
        System.out.println("Loading calibration values for module " + this.getName() + " from CCDB");
        setConstantsManager(ccdb);
        this.prevCalibIT = getConstantsManager().getConstants(this.runNumber, "/calibration/ft/fthodo/charge_to_energy");
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    this.getPreviousCalibrationTable().addEntry(ssec, llay, key);
                    this.getPreviousCalibrationTable().setDoubleValue(this.prevCalibIT.getDoubleValue("mips_charge", ssec, llay, key),"mips_charge",ssec, llay, key);
                    double charge2cal = this.getPreviousCalibrationTable().getDoubleValue("mips_charge", ssec,llay,key);
                    this.getDataGroup().getItem(ssec,llay,key).getGraph("gCharge_"+ ssec + "_" + llay).addPoint(key+0.2, charge2cal, 0, 0);
                }
            }
        }
    }
    
    @Override
    public void adjustFit() {
        System.out.println("Adjusting Charge fit for Sector " + this.sector +" Layer " + this.layer +" Component " + this.component);
        H1F histtofit =  this.getDataGroup().getItem(this.sector,this.layer,this.component).getH1F("hcharge_" + sector + "_" + layer + "_" + component);
        F1D ftofit = this.getDataGroup().getItem(this.sector,this.layer,this.component).getF1D("fcharge_" + sector + "_" + layer + "_" + component);
        FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ", "energy");
        cfit.setCalibTable(getCalibrationTable());
        cfit.setSecLayComp(this.sector,this.layer,this.component);
        cfit.setGraphToUpdate(this.getDataGroup().getItem(this.sector,this.layer,this.component).getGraph("gChargeCal_"+ this.sector + "_" + this.layer));
    }
    
    
    
    
    
    
    
    
    
    
    
}
