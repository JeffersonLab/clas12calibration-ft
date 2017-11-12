package org.clas.ftcal.led;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clas.detector.CodaEventDecoder;
import org.clas.detector.DetectorDataDgtz;
import org.clas.detector.DetectorEventDecoder;
import org.clas.ftcal.tools.FTCalDetector;
import org.clas.ft.tools.FTParameter;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas; 
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;


public class FTCalLedModule extends JPanel implements DetectorListener,CalibrationConstantsListener,ActionListener,ChangeListener{

    // panels and canvases
    ColorPalette palette = new ColorPalette();
    JSplitPane     splitPane       = new JSplitPane();
    JPanel         detectorPanel   = new JPanel();
    JPanel         radioPane       = new JPanel();
    ButtonGroup    radioGroup      = new ButtonGroup();
    JComboBox      radioList       = new JComboBox();
    JTabbedPane    tabbedPane      = new JTabbedPane();
    EmbeddedCanvas canvasEvent     = new EmbeddedCanvas();
    EmbeddedCanvas canvasNoise     = new EmbeddedCanvas();
    EmbeddedCanvas canvasCharge    = new EmbeddedCanvas();
    EmbeddedCanvas canvasAmpli     = new EmbeddedCanvas();
    EmbeddedCanvas canvasWidth     = new EmbeddedCanvas();
    EmbeddedCanvas canvasTime      = new EmbeddedCanvas();
    FTCalDetector  detector        = new FTCalDetector("FTCAL");
    List<FTParameter>        parameters = new ArrayList<FTParameter>();
    ConstantsManager         ccdb = new ConstantsManager();
    CalibrationConstantsView summaryTable = null; 
    CalibrationConstants     calib = null;
        
    // histograms, functions and graphs
    DetectorCollection<H1F> H_fADC = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_WAVE = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_WAVE_PED = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_WAVE_PUL = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_PED  = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_NOISE = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_fADC   = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_fADC_NORM = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_fADC_RANGES = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_CHARGE = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_VMAX   = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_WIDTH   = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_TCROSS  = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_LED_THALF   = new DetectorCollection<H1F>();
    H1F H_fADC_N   = null;
    H1F H_WMAX     = null;
    H1F H_TCROSS   = null;
    H1F H_LED_N = null;
    
    DetectorCollection<GraphErrors> G_LED_CHARGE        = new DetectorCollection<GraphErrors>();
    DetectorCollection<GraphErrors> G_LED_CHARGE_SELECT = new DetectorCollection<GraphErrors>();
    DetectorCollection<F1D> F_LED_CHARGE_SELECT_MEAN    = new DetectorCollection<F1D>();
    DetectorCollection<F1D> F_LED_CHARGE_SELECT_LOW     = new DetectorCollection<F1D>();
    DetectorCollection<F1D> F_LED_CHARGE_SELECT_HIGH    = new DetectorCollection<F1D>();
    DetectorCollection<GraphErrors> G_LED_AMPLI         = new DetectorCollection<GraphErrors>();
    DetectorCollection<GraphErrors> G_LED_AMPLI_SELECT  = new DetectorCollection<GraphErrors>();
    DetectorCollection<F1D> F_LED_AMPLI_SELECT_MEAN     = new DetectorCollection<F1D>();
    DetectorCollection<F1D> F_LED_AMPLI_SELECT_LOW      = new DetectorCollection<F1D>();
    DetectorCollection<F1D> F_LED_AMPLI_SELECT_HIGH     = new DetectorCollection<F1D>();
    DetectorCollection<GraphErrors> G_LED_WIDTH         = new DetectorCollection<GraphErrors>();

    double[] ledCharge;
    double[] ledCharge2;
    double[] ledAmpli;
    double[] ledAmpli2;
    double[] ledWidth;
    double[] ledWidth2;
    int[]    ledEvent;    
    int[]    ledNEvents;
    int nLedEvents=50;
    int nLedSkip=10;
    double deltaCharge = 10;
    double deltaAmpli  = 5;

    DetectorCollection<GraphErrors> G_PULSE_ANALYSIS = new DetectorCollection<GraphErrors>();
    
    double[] crystalID; 
    double[] pedestalMEAN;
    double[] noiseRMS;
    double[] cosmicCharge;
    double[] timeCross;
    double[] timeHalf;
    double[] fullWidthHM;
    int[] crystalPointers;    
    
    // decoded related information
    int nProcessed = 0;
    CodaEventDecoder             decoder = new CodaEventDecoder();
    DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();
    public int runNumber = 0;

    // analysis parameters
//    int threshold = 50; // 10 fADC value <-> ~ 5mV
//    int ped_i1 = 1;
//    int ped_i2 = 20;
//    int pul_i1 = 25;
//    int pul_i2 = 65;
    double nsPerSample=4;
    double LSB = 0.4884;
    double crystal_size = 15;
    private final int nCrystalX = 22;
    private final int nCrystalY = nCrystalX;

    // control variables
    private int plotSelect = 0;  // 0 - waveforms, 1 - noise
    private int keySelect = 8;
    private boolean debugFlag=true;
    private int timerUpdate = 1000;

    
    
    public FTCalLedModule(){
        
        this.initCCDB();
        this.initDetector();
        this.initParameters();
        this.initHistograms();
        this.initPanel();
    }

    private void initPanel() {
        
        this.setLayout(new BorderLayout());

        // the last tab will contain the summmary table
        summaryTable = new CalibrationConstantsView();
        this.initTable();
       
        // create Tabbed Panel
        tabbedPane.add("Event Viewer",this.canvasEvent);
        tabbedPane.add("Noise"       ,this.canvasNoise);
        tabbedPane.add("Charge"      ,this.canvasCharge);
        tabbedPane.add("Amplitude"   ,this.canvasAmpli);
        tabbedPane.add("Width"       ,this.canvasWidth);
        tabbedPane.add("Time"        ,this.canvasTime);
        tabbedPane.add("Summary"     ,summaryTable);
        tabbedPane.addChangeListener(this);
        this.initCanvas();
        this.plotHistograms();
        
        JPanel canvasPane = new JPanel();

        canvasPane.setLayout(new BorderLayout());
        canvasPane.add(tabbedPane, BorderLayout.CENTER);
//        canvasPane.add(buttonPane, BorderLayout.PAGE_END);
    
        this.detectorPanel.setLayout(new BorderLayout());
        this.detectorPanel.add(this.detector, BorderLayout.CENTER);
        this.detector.add(this.radioPane, BorderLayout.PAGE_START);
        this.detectorPanel.add(this.detectorDecoder.getFadcPanel(), BorderLayout.PAGE_END);
        
        splitPane.setLeftComponent(this.detectorPanel);
        splitPane.setRightComponent(canvasPane);
        splitPane.setDividerLocation(0.4);        
        splitPane.setResizeWeight(0.4);
 
        this.add(splitPane, BorderLayout.CENTER);

    }

    private void initParameters() {
        
        this.addParameters("Event", "Status", "Occupancy", "Pedestal (Counts)", "Noise (mV)", "Charge Mean (pC)", "Charge Sigma (pC)", 
                           "Amplitude Mean (mV)", "Amplitude Sigma (mV)", "Width (ns)", "Time (ns)");
        this.parameters.get(0).setRanges(0.0,0.0,1.0,1.0,true);
        this.parameters.get(1).setRanges(0.75,1.05,1.0,2.0,true);
        this.parameters.get(2).setRanges(0.0,5000.0,1.0,5000.0,false);
        this.parameters.get(3).setRanges(130.0,250.0,1.0,500.0,false);
        this.parameters.get(4).setRanges(0.75,1.05,1.0,2.0,false); 
        this.parameters.get(5).setRanges(500.0,1500.0,1.0,2000.0,false);        
        this.parameters.get(6).setRanges(0.0,deltaCharge,1.0,deltaCharge,false);
        this.parameters.get(7).setRanges(800.0,1200.0,1.0,2400.0,false);
        this.parameters.get(8).setRanges(0.0,deltaAmpli,1.0,deltaAmpli,false);
        this.parameters.get(9).setRanges(0.0,100.0,1.0,100.0,false);
        this.parameters.get(10).setRanges(0.0,200.0,1.0,200.0,false);
        
    }

    private void initCanvas() {
        GStyle.getAxisAttributesX().setTitleFontSize(16);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(16);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        // event canvas
        this.canvasEvent.setGridX(false);
        this.canvasEvent.setGridY(false);
        this.canvasEvent.setStatBoxFontSize(12);
        this.canvasEvent.initTimer(timerUpdate);
        // noise
        this.canvasNoise.divide(2, 2);
        this.canvasNoise.cd(0);
        this.canvasNoise.setGridX(false);
        this.canvasNoise.setGridY(false);
        this.canvasNoise.setStatBoxFontSize(12);
        this.canvasNoise.cd(1);
        this.canvasNoise.setGridX(false);
        this.canvasNoise.setGridY(false);
        this.canvasNoise.setStatBoxFontSize(12);
        this.canvasNoise.cd(2);
        this.canvasNoise.setGridX(false);
        this.canvasNoise.setGridY(false);
        this.canvasNoise.setStatBoxFontSize(12);
        this.canvasNoise.cd(3);
        this.canvasNoise.setGridX(false);
        this.canvasNoise.setGridY(false);
        this.canvasNoise.setStatBoxFontSize(12);
        this.canvasNoise.initTimer(timerUpdate);
        // charge
        this.canvasCharge.divide(2, 2);
        this.canvasCharge.cd(0);
        this.canvasCharge.setGridX(false);
        this.canvasCharge.setGridY(false);
        this.canvasCharge.setStatBoxFontSize(12);
        this.canvasCharge.cd(1);
        this.canvasCharge.setGridX(false);
        this.canvasCharge.setGridY(false);
        this.canvasCharge.setStatBoxFontSize(12);
        this.canvasCharge.cd(2);
        this.canvasCharge.setGridX(false);
        this.canvasCharge.setGridY(false);
        this.canvasCharge.setStatBoxFontSize(12);
        this.canvasCharge.cd(3);
        this.canvasCharge.setGridX(false);
        this.canvasCharge.setGridY(false);
        this.canvasCharge.setStatBoxFontSize(12);
        this.canvasCharge.initTimer(timerUpdate);
        // amplitude
        this.canvasAmpli.divide(2, 2);
        this.canvasAmpli.cd(0);
        this.canvasAmpli.setGridX(false);
        this.canvasAmpli.setGridY(false);
        this.canvasAmpli.setStatBoxFontSize(12);
        this.canvasAmpli.cd(1);
        this.canvasAmpli.setGridX(false);
        this.canvasAmpli.setGridY(false);
        this.canvasAmpli.setStatBoxFontSize(12);
        this.canvasAmpli.cd(2);
        this.canvasAmpli.setGridX(false);
        this.canvasAmpli.setGridY(false);
        this.canvasAmpli.setStatBoxFontSize(12);
        this.canvasAmpli.cd(3);
        this.canvasAmpli.setGridX(false);
        this.canvasAmpli.setGridY(false);
        this.canvasAmpli.setStatBoxFontSize(12);
        this.canvasAmpli.initTimer(timerUpdate);
        // Width
        this.canvasWidth.divide(2, 2);
        this.canvasWidth.cd(0);
        this.canvasWidth.setGridX(false);
        this.canvasWidth.setGridY(false);
        this.canvasWidth.setStatBoxFontSize(12);
        this.canvasWidth.cd(1);
        this.canvasWidth.setGridX(false);
        this.canvasWidth.setGridY(false);
        this.canvasWidth.setStatBoxFontSize(12);
        this.canvasWidth.cd(2);
        this.canvasWidth.setGridX(false);
        this.canvasWidth.setGridY(false);
        this.canvasWidth.setStatBoxFontSize(12);
        this.canvasWidth.cd(3);
        this.canvasWidth.setGridX(false);
        this.canvasWidth.setGridY(false);
        this.canvasWidth.setStatBoxFontSize(12);
        this.canvasWidth.initTimer(timerUpdate);
        // time
        this.canvasTime.divide(2, 2);
        this.canvasTime.cd(0);
        this.canvasTime.setGridX(false);
        this.canvasTime.setGridY(false);
        this.canvasTime.setStatBoxFontSize(12);
        this.canvasTime.cd(1);
        this.canvasTime.setGridX(false);
        this.canvasTime.setGridY(false);
        this.canvasTime.setStatBoxFontSize(12);
        this.canvasTime.cd(2);
        this.canvasTime.setGridX(false);
        this.canvasTime.setGridY(false);
        this.canvasTime.setStatBoxFontSize(12);
        this.canvasTime.cd(3);
        this.canvasTime.setGridX(false);
        this.canvasTime.setGridY(false);
        this.canvasTime.setStatBoxFontSize(12);
        this.canvasTime.initTimer(timerUpdate);
    }
    
    private void initTable() {
        calib = new CalibrationConstants(3,"Pedestal (Counts)/F:Noise (mV)/F:Charge Mean (pC)/F:Charge Sigma (pC)/F:Amp. Mean (mV)/F:Amp. Sigma (mV)/F");
        calib.setName("Summary");
	calib.setPrecision(3);
        calib.addConstraint(3, this.parameters.get(3).getMin(),this.parameters.get(3).getMax()); 
        calib.addConstraint(4, this.parameters.get(4).getMin(),this.parameters.get(4).getMax()); 
        calib.addConstraint(5, this.parameters.get(5).getMin(),this.parameters.get(5).getMax()); 
        calib.addConstraint(6, this.parameters.get(6).getMin(),this.parameters.get(6).getMax()); 
        calib.addConstraint(7, this.parameters.get(7).getMin(),this.parameters.get(7).getMax()); 
        calib.addConstraint(8, this.parameters.get(8).getMin(),this.parameters.get(8).getMax()); 
        for (int component : this.detector.getDetectorComponents()) calib.addEntry(1, 1, component);
        calib.fireTableDataChanged();
        this.resetTable();
        summaryTable.addConstants(calib,this);
    }
    
    private void initCCDB() {
            System.out.println("\nInitializing connection to CCDB"); 
            ccdb.init(Arrays.asList(new String[]{
                    "/daq/fadc/ftcal",
                    "/daq/tt/ftcal"}));
            this.detectorDecoder.getFadcPanel().init(ccdb,11,"/daq/fadc/ftcal", 70,3,1); 
            this.detectorDecoder.getFadcPanel().setTET(200);
     }

    private void initDetector() {
        detector.getView().addDetectorListener(this);
        for(String layer : detector.getView().getLayerNames()){
            detector.getView().setDetectorListener(layer,this);
         }
        detector.updateBox();
    }

    private void initHistograms() {
        for (int component : this.detector.getDetectorComponents()) {
                int ix = this.detector.getIdX(component);
                int iy = this.detector.getIdY(component);
                String title = "Crystal " + component + " (" + ix + "," + iy + ")";
                H_fADC.add(1, 1, component, new H1F("fADC_" + component, title, 100, 0.0, 100.0));
                H_PED.add(1, 1, component, new H1F("Pedestal_" + component, title, 120, 130., 250.0));
                H_PED.get(1, 1, component).setFillColor(2);
                H_PED.get(1, 1, component).setTitleX("Pedestal (fADC counts)");
                H_PED.get(1, 1, component).setTitleY("Counts");   
                H_PED.get(1, 1, component).setOptStat(1111);                     
                H_NOISE.add(1, 1, component, new H1F("Noise_" + component, title, 200, 0.0, 10.0));
                H_NOISE.get(1, 1, component).setFillColor(4);
                H_NOISE.get(1, 1, component).setTitleX("RMS (mV)");
                H_NOISE.get(1, 1, component).setTitleY("Counts");  
                H_NOISE.get(1, 1, component).setOptStat(1111);                     
                H_WAVE.add(1, 1, component, new H1F("Wave_" + component, title, 100, 0.0, 100.0));
                H_WAVE.get(1, 1, component).setFillColor(25);
                H_WAVE.get(1, 1, component).setTitleX("fADC Sample");
                H_WAVE.get(1, 1, component).setTitleY("fADC Counts");
                H_WAVE.get(1, 1, component).setOptStat(1111);                     
                H_WAVE_PED.add(1, 1, component, new H1F("Wave_PED_" + component, title, 100, 0.0, 100.0));
                H_WAVE_PED.get(1, 1, component).setFillColor(47);
                H_WAVE_PED.get(1, 1, component).setTitleX("fADC Sample");
                H_WAVE_PED.get(1, 1, component).setTitleY("fADC Counts");
                H_WAVE_PED.get(1, 1, component).setOptStat(1111);                     
                H_WAVE_PUL.add(1, 1, component, new H1F("Wave_PUL_" + component, title, 100, 0.0, 100.0));
                H_WAVE_PUL.get(1, 1, component).setFillColor(46);
                H_WAVE_PUL.get(1, 1, component).setTitleX("fADC Sample");
                H_WAVE_PUL.get(1, 1, component).setTitleY("fADC Counts");
                H_WAVE_PUL.get(1, 1, component).setOptStat(1111);                     
                H_LED_fADC.add(1, 1, component, new H1F("FADC_" + component, title, 100, 0.0, 100.0));
                H_LED_fADC.get(1, 1, component).setFillColor(3);
                H_LED_fADC.get(1, 1, component).setTitleX("fADC Sample");
                H_LED_fADC.get(1, 1, component).setTitleY("fADC Counts");
                H_LED_fADC.get(1, 1, component).setOptStat(1111);                     
                H_LED_fADC_NORM.add(1, 1, component, new H1F("fADC_" + component, title, 100, 0.0, 100.0));
                H_LED_fADC_NORM.get(1, 1, component).setFillColor(23);
                H_LED_fADC_NORM.get(1, 1, component).setTitleX("fADC Sample");
                H_LED_fADC_NORM.get(1, 1, component).setTitleY("fADC Counts");
                H_LED_fADC_NORM.get(1, 1, component).setOptStat(1111);   
                H_LED_fADC_RANGES.add(1, 1, component, new H1F("fADC_" + component, title, 100, 0.0, 100.0));
                H_LED_fADC_RANGES.get(1, 1, component).setFillColor(3);
                H_LED_fADC_RANGES.get(1, 1, component).setTitleX("fADC Sample");
                H_LED_fADC_RANGES.get(1, 1, component).setTitleY("fADC Counts");
                H_LED_fADC_RANGES.get(1, 1, component).setOptStat(1111);   
                H_LED_CHARGE.add(1, 1, component, new H1F("Charge_" + component, title, 300, 500.0, 2000.0));
                H_LED_CHARGE.get(1, 1, component).setFillColor(2);
                H_LED_CHARGE.get(1, 1, component).setTitleX("Charge (pC)");
                H_LED_CHARGE.get(1, 1, component).setTitleY("Counts");
                H_LED_CHARGE.get(1, 1, component).setOptStat(1111);                     
                H_LED_VMAX.add(1, 1, component, new H1F("VMax_" + component, title, 300, 0.0, 1500.0));
                H_LED_VMAX.get(1, 1, component).setFillColor(2);
                H_LED_VMAX.get(1, 1, component).setTitleX("Amplitude (mV)");
                H_LED_VMAX.get(1, 1, component).setTitleY("Counts");
                H_LED_VMAX.get(1, 1, component).setOptStat(1111);                     
                H_LED_WIDTH.add(1, 1, component, new H1F("Width_" + component, title, 300, 0.0, 100.0));
                H_LED_WIDTH.get(1, 1, component).setFillColor(2);
                H_LED_WIDTH.get(1, 1, component).setTitleX("FWHM (ns)");
                H_LED_WIDTH.get(1, 1, component).setTitleY("Counts");
                H_LED_WIDTH.get(1, 1, component).setOptStat(1111);                     
                H_LED_TCROSS.add(1, 1, component, new H1F("T_TRIG_" + component, title, 200, 60.0, 160.0));
                H_LED_TCROSS.get(1, 1, component).setFillColor(5);
                H_LED_TCROSS.get(1, 1, component).setTitleX("Time (ns)");
                H_LED_TCROSS.get(1, 1, component).setTitleY("Counts");
                H_LED_TCROSS.get(1, 1, component).setOptStat(1111);                     
                H_LED_THALF.add(1, 1, component, new H1F("T_TRIG_" + component, title, 200, 60.0, 160.0));
                H_LED_THALF.get(1, 1, component).setFillColor(5);
                H_LED_THALF.get(1, 1, component).setTitleX("Time (ns)");
                H_LED_THALF.get(1, 1, component).setTitleY("Counts"); 
                H_LED_THALF.get(1, 1, component).setOptStat(1111);                     
                // graphs
                G_LED_CHARGE.add(1, 1,component, new GraphErrors());
                G_LED_CHARGE.get(1, 1, component).setTitle(title); //  title
                G_LED_CHARGE.get(1, 1, component).setTitleX("Event");             // X axis title
                G_LED_CHARGE.get(1, 1, component).setTitleY("LED Charge (pC)");   // Y axis title
                G_LED_CHARGE.get(1, 1, component).setMarkerColor(1); // color from 0-9 for given palette
                G_LED_CHARGE.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_LED_CHARGE.get(1, 1, component).setMarkerStyle(4); // Style can be 1 or 2 
                G_LED_CHARGE_SELECT.add(1, 1,component, new GraphErrors());
                G_LED_CHARGE_SELECT.get(1, 1, component).setTitle(title); //  title
                G_LED_CHARGE_SELECT.get(1, 1, component).setTitleX("Event");             // X axis title
                G_LED_CHARGE_SELECT.get(1, 1, component).setTitleY("LED Charge (pC)");   // Y axis title
                G_LED_CHARGE_SELECT.get(1, 1, component).setMarkerColor(2); // color from 0-9 for given palette
                G_LED_CHARGE_SELECT.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_LED_CHARGE_SELECT.get(1, 1, component).setMarkerStyle(0); // Style can be 1 or 2 
                F_LED_CHARGE_SELECT_MEAN.add(1, 1, component, new F1D("Charge Mean","[mean]",   0.0, 1.0));
                F_LED_CHARGE_SELECT_MEAN.get(1, 1, component).setParameter(0, 0.0);
                F_LED_CHARGE_SELECT_MEAN.get(1, 1, component).setLineWidth(1);
                F_LED_CHARGE_SELECT_MEAN.get(1, 1, component).setLineColor(1);
                F_LED_CHARGE_SELECT_LOW.add(1, 1, component, new F1D("Charge Low Limit","[low]", 0.0, 1.0));
                F_LED_CHARGE_SELECT_LOW.get(1, 1, component).setParameter(0, -deltaCharge);
                F_LED_CHARGE_SELECT_LOW.get(1, 1, component).setLineWidth(1);
                F_LED_CHARGE_SELECT_LOW.get(1, 1, component).setLineColor(4);
                F_LED_CHARGE_SELECT_HIGH.add(1, 1, component, new F1D("Charge High Limit","[high]", 0.0, 1.0));
                F_LED_CHARGE_SELECT_HIGH.get(1, 1, component).setParameter(0, deltaCharge);
                F_LED_CHARGE_SELECT_HIGH.get(1, 1, component).setLineWidth(1);
                F_LED_CHARGE_SELECT_HIGH.get(1, 1, component).setLineColor(4);
                G_LED_AMPLI.add(1, 1,component, new GraphErrors());
                G_LED_AMPLI.get(1, 1, component).setTitle(title); //  title
                G_LED_AMPLI.get(1, 1, component).setTitleX("Event");             // X axis title
                G_LED_AMPLI.get(1, 1, component).setTitleY("LED Amplitude (mV)");   // Y axis title
                G_LED_AMPLI.get(1, 1, component).setMarkerColor(1); // color from 0-9 for given palette
                G_LED_AMPLI.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_LED_AMPLI.get(1, 1, component).setMarkerStyle(0); // Style can be 1 or 2 
                G_LED_AMPLI_SELECT.add(1, 1,component, new GraphErrors());
                G_LED_AMPLI_SELECT.get(1, 1, component).setTitle(title); //  title
                G_LED_AMPLI_SELECT.get(1, 1, component).setTitleX("Event");             // X axis title
                G_LED_AMPLI_SELECT.get(1, 1, component).setTitleY("LED Amplitude (mV)");   // Y axis title
                G_LED_AMPLI_SELECT.get(1, 1, component).setMarkerColor(2); // color from 0-9 for given palette
                G_LED_AMPLI_SELECT.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_LED_AMPLI_SELECT.get(1, 1, component).setMarkerStyle(0); // Style can be 1 or 2 
                F_LED_AMPLI_SELECT_MEAN.add(1, 1, component, new F1D("Amplitude Mean","[mean]",   0.0, 1.0));
                F_LED_AMPLI_SELECT_MEAN.get(1, 1, component).setParameter(0, 0.0);
                F_LED_AMPLI_SELECT_MEAN.get(1, 1, component).setLineWidth(1);
                F_LED_AMPLI_SELECT_MEAN.get(1, 1, component).setLineColor(1);
                F_LED_AMPLI_SELECT_LOW.add(1, 1, component, new F1D("Amplitude Low Limit","[low]", 0.0, 1.0));
                F_LED_AMPLI_SELECT_LOW.get(1, 1, component).setParameter(0, -deltaCharge);
                F_LED_AMPLI_SELECT_LOW.get(1, 1, component).setLineWidth(1);
                F_LED_AMPLI_SELECT_LOW.get(1, 1, component).setLineColor(4);
                F_LED_AMPLI_SELECT_HIGH.add(1, 1, component, new F1D("Amplitude High Limit","[high]", 0.0, 1.0));
                F_LED_AMPLI_SELECT_HIGH.get(1, 1, component).setParameter(0, deltaCharge);
                F_LED_AMPLI_SELECT_HIGH.get(1, 1, component).setLineWidth(1);
                F_LED_AMPLI_SELECT_HIGH.get(1, 1, component).setLineColor(4);
                G_LED_WIDTH.add(1, 1,component, new GraphErrors());
                G_LED_WIDTH.get(1, 1, component).setTitle(title); //  title
                G_LED_WIDTH.get(1, 1, component).setTitleX("Event");             // X axis title
                G_LED_WIDTH.get(1, 1, component).setTitleY("LED FWHM (ns)");   // Y axis title
                G_LED_WIDTH.get(1, 1, component).setMarkerColor(1); // color from 0-9 for given palette
                G_LED_WIDTH.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_LED_WIDTH.get(1, 1, component).setMarkerStyle(0); // Style can be 1 or 2 
                G_PULSE_ANALYSIS.add(1, 1,component, new GraphErrors());
                G_PULSE_ANALYSIS.get(1, 1, component).setTitle(title); //  title
                G_PULSE_ANALYSIS.get(1, 1, component).setTitleX("Event");             // X axis title
                G_PULSE_ANALYSIS.get(1, 1, component).setTitleY("LED Amplitude (mV)");   // Y axis title
                G_PULSE_ANALYSIS.get(1, 1, component).setMarkerColor(1); // color from 0-9 for given palette
                G_PULSE_ANALYSIS.get(1, 1, component).setMarkerSize(5); // size in points on the screen
                G_PULSE_ANALYSIS.get(1, 1, component).setMarkerStyle(0); // Style can be 1 or 2 
        }
        H_fADC_N   = new H1F("fADC"  , 505, 0, 505);
        H_WMAX     = new H1F("WMAX"  , 505, 0, 505);
        H_TCROSS   = new H1F("TCROSS", 505, 0, 505);
        H_LED_N    = new H1F("EVENT" , 505, 0, 505);

        crystalID       = new double[332];
        pedestalMEAN    = new double[332];
        noiseRMS        = new double[332];
        timeCross       = new double[332];
        timeHalf        = new double[332];
        fullWidthHM     = new double[332];
        crystalPointers = new int[484];
        ledCharge  = new double[484];
        ledCharge2 = new double[484];
        ledAmpli   = new double[484];
        ledAmpli2  = new double[484];
        ledWidth   = new double[484];
        ledWidth2  = new double[484];
        ledEvent   = new int[484];
        ledNEvents = new int[484];                
        int ipointer=0;
        for(int i=0; i<484; i++) {
            if(this.detector.hasComponent(i)) {
                crystalPointers[i]=ipointer;
                crystalID[ipointer]=i;
                ipointer++;
            }
            else {
                crystalPointers[i]=-1;
            }
            ledEvent[i]   = -1;
            ledNEvents[i] =  0;
        }


    }
    
    public void plotHistograms() {
        // event viewer
        canvasEvent.draw(H_WAVE.get(1, 1, keySelect));
        if(debugFlag) {
            canvasEvent.draw(H_WAVE_PED.get(1, 1, keySelect),"same");
            canvasEvent.draw(H_WAVE_PUL.get(1, 1, keySelect),"same");
            if(G_PULSE_ANALYSIS.hasEntry(1, 1, keySelect)) {
                if(G_PULSE_ANALYSIS.get(1, 1, keySelect).getDataSize(1)>0) canvasEvent.draw(G_PULSE_ANALYSIS.get(1, 1, keySelect),"same");
            }
        } 
 // noise
        for(int key=0; key<crystalPointers.length; key++) {
            if(crystalPointers[key]>=0) {
                pedestalMEAN[crystalPointers[key]] = H_PED.get(1, 1,key).getMean();
                noiseRMS[crystalPointers[key]]     = H_NOISE.get(1, 1, key).getMean();
                timeCross[crystalPointers[key]]    = H_LED_TCROSS.get(1, 1, key).getMean();
                timeHalf[crystalPointers[key]]     = H_LED_THALF.get(1, 1, key).getMean();
                fullWidthHM[crystalPointers[key]]  = H_LED_WIDTH.get(1, 1, key).getMean();
            }
        }
        GraphErrors  G_PED = new GraphErrors("Mean",crystalID,pedestalMEAN);
        G_PED.setTitle(" "); //  title
        G_PED.setTitleX("Crystal ID"); // X axis title
        G_PED.setTitleY("Pedestal (Counts)");   // Y axis title
        G_PED.setMarkerColor(2); // color from 0-9 for given palette
        G_PED.setMarkerSize(5); // size in points on the screen
        G_PED.setMarkerStyle(0); // Style can be 1 or 2
        GraphErrors  G_NOISE = new GraphErrors("RMS",crystalID,noiseRMS);
        G_NOISE.setTitle(" "); //  title
        G_NOISE.setTitleX("Crystal ID"); // X axis title
        G_NOISE.setTitleY("Noise RMS (mV)");   // Y axis title
        G_NOISE.setMarkerColor(4); // color from 0-9 for given palette
        G_NOISE.setMarkerSize(5); // size in points on the screen
        G_NOISE.setMarkerStyle(0); // Style can be 1 or 2
        canvasNoise.cd(0);
        canvasNoise.draw(G_PED);
        canvasNoise.cd(1);
        canvasNoise.draw(G_NOISE);
        if (H_NOISE.hasEntry(1, 1, keySelect)) {
            H1F hnoise = H_NOISE.get(1, 1, keySelect);
            H1F hped   = H_PED.get(1, 1, keySelect);
            canvasNoise.cd(2);
            canvasNoise.draw(hped,"S");
            canvasNoise.cd(3);
            canvasNoise.draw(hnoise,"S");
        }
        // Charge
        canvasCharge.cd(0);
        if (H_LED_fADC.hasEntry(1, 1, keySelect)) {
            H1F hfADC  = H_LED_fADC_NORM.get(1, 1, keySelect);
            H1F hfADCR = H_LED_fADC_RANGES.get(1, 1, keySelect);
            if(H_LED_N.getBinContent(keySelect)>0) {
                for(int i=0; i<hfADC.getData().length; i++) {
                    hfADC.setBinContent(i, H_LED_fADC.get(1, 1, keySelect).getBinContent(i)/H_LED_N.getBinContent(keySelect));
//                    if((i>ped_i1 && i<=ped_i2) || (i>pul_i1 && i<=pul_i2)) hfADCR.setBinContent(i, H_LED_fADC.get(1, 1, keySelect).getBinContent(i)/H_LED_N.getBinContent(keySelect));
                }
            }
            canvasCharge.draw(hfADC);               
            canvasCharge.draw(hfADCR, "same");               
        }
        canvasCharge.cd(1);
        canvasCharge.draw(G_LED_CHARGE.get(1, 1, keySelect));
        if(G_LED_CHARGE_SELECT.get(1, 1, keySelect).getDataSize(1)>0) canvasCharge.draw(G_LED_CHARGE_SELECT.get(1, 1, keySelect),"same");
        canvasCharge.cd(2);
        if(H_LED_CHARGE.hasEntry(1, 1, keySelect)) {
            H1F hled = H_LED_CHARGE.get(1, 1,keySelect);
            canvasCharge.draw(hled,"S");
        } 
        canvasCharge.cd(3);
        canvasCharge.draw(G_LED_CHARGE_SELECT.get(1, 1, keySelect));
        canvasCharge.draw(F_LED_CHARGE_SELECT_MEAN.get(1, 1, keySelect),"same");
        canvasCharge.draw(F_LED_CHARGE_SELECT_LOW.get(1, 1, keySelect),"same");
        canvasCharge.draw(F_LED_CHARGE_SELECT_HIGH.get(1, 1, keySelect),"same");
        // Amplitude
        canvasAmpli.cd(0);
        if (H_LED_fADC.hasEntry(1, 1, keySelect)) {
            H1F hfADC = H_LED_fADC_NORM.get(1, 1, keySelect);
            if(H_LED_N.getBinContent(keySelect)>0) {
                for(int i=0; i<hfADC.getData().length; i++) {
                    hfADC.setBinContent(i, H_LED_fADC.get(1, 1, keySelect).getBinContent(i)/H_LED_N.getBinContent(keySelect));
                }
            }            
            canvasAmpli.draw(hfADC);               
        }
        canvasAmpli.cd(1);
        canvasAmpli.draw(G_LED_AMPLI.get(1, 1, keySelect));
        if(G_LED_AMPLI_SELECT.get(1, 1, keySelect).getDataSize(1)>0) canvasAmpli.draw(G_LED_AMPLI_SELECT.get(1, 1, keySelect),"same");
        canvasAmpli.cd(2);
        if(H_LED_VMAX.hasEntry(1, 1, keySelect)) {
            H1F hled = H_LED_VMAX.get(1, 1,keySelect);
            canvasAmpli.draw(hled,"S");
        }
        canvasAmpli.cd(3);
        canvasAmpli.draw(G_LED_AMPLI_SELECT.get(1, 1, keySelect));
        canvasAmpli.draw(F_LED_AMPLI_SELECT_MEAN.get(1, 1, keySelect),"same");
        canvasAmpli.draw(F_LED_AMPLI_SELECT_LOW.get(1, 1, keySelect),"same");
        canvasAmpli.draw(F_LED_AMPLI_SELECT_HIGH.get(1, 1, keySelect),"same");
        // Width
        GraphErrors  G_WIDTH = new GraphErrors("FWHM",crystalID,fullWidthHM);
        G_WIDTH.setTitle(" "); //  title
        G_WIDTH.setTitleX("Crystal ID"); // X axis title
        G_WIDTH.setTitleY("FWHM (ns)");   // Y axis title
        G_WIDTH.setMarkerColor(2); // color from 0-9 for given palette
        G_WIDTH.setMarkerSize(5); // size in points on the screen
        G_WIDTH.setMarkerStyle(0); // Style can be 1 or 2
        canvasWidth.cd(0);
        if (H_LED_fADC.hasEntry(1, 1, keySelect)) {
            H1F hfADC = H_LED_fADC_NORM.get(1, 1, keySelect);
            if(H_LED_N.getBinContent(keySelect)>0) {
                for(int i=0; i<hfADC.getData().length; i++) {
                    hfADC.setBinContent(i, H_LED_fADC.get(1, 1, keySelect).getBinContent(i)/H_LED_N.getBinContent(keySelect));
                }
            }            canvasWidth.draw(hfADC);               
        }
        canvasWidth.cd(1); 
        canvasWidth.draw(G_WIDTH);
        canvasWidth.cd(2);
        if(H_LED_WIDTH.hasEntry(1, 1, keySelect)) {
            H1F hwidth = H_LED_WIDTH.get(1, 1, keySelect);
            canvasWidth.draw(hwidth,"S");
        }        
        canvasWidth.cd(3);
        canvasWidth.draw(G_LED_WIDTH.get(1, 1, keySelect));
        // Time
        GraphErrors  G_TCROSS = new GraphErrors("TCross",crystalID,timeCross);
        G_TCROSS.setTitle(" "); //  title
        G_TCROSS.setTitleX("Crystal ID"); // X axis title
        G_TCROSS.setTitleY("Trigger Time (ns)");   // Y axis title
        G_TCROSS.setMarkerColor(5); // color from 0-9 for given palette
        G_TCROSS.setMarkerSize(5);  // size in points on the screen
        G_TCROSS.setMarkerStyle(0); // Style can be 1 or 2
        GraphErrors  G_THALF = new GraphErrors("Time Mode-7",crystalID,timeHalf);
        G_THALF.setTitle(" "); //  title
        G_THALF.setTitleX("Crystal ID"); // X axis title
        G_THALF.setTitleY("Mode-7 Time (ns)");   // Y axis title
        G_THALF.setMarkerColor(5); // color from 0-9 for given palette
        G_THALF.setMarkerSize(5);  // size in points on the screen
        G_THALF.setMarkerStyle(0); // Style can be 1 or 2
        canvasTime.cd(0);
        canvasTime.draw(G_TCROSS);
        canvasTime.cd(1);
        canvasTime.draw(G_THALF);
        canvasTime.cd(2);
        if(H_LED_TCROSS.hasEntry(1, 1, keySelect)) {
            H1F htime = H_LED_TCROSS.get(1, 1, keySelect);
            canvasTime.draw(htime,"S");
        }
        canvasTime.cd(3);
        if(H_LED_THALF.hasEntry(1, 1, keySelect)) {
            H1F htime = H_LED_THALF.get(1, 1, keySelect);
            canvasTime.draw(htime,"S");            
//            if(htime.getEntries()>0) {
//                initTimeGaussFitPar(keySelect,htime);
//                DataFitter.fit(myTimeGauss.get(1, 1, keySelect),htime,"NQ");
//                canvasTime.draw(myTimeGauss.get(1, 1, keySelect),"sameS");
//            }
        }
        this.updateTable();
        
    }

    public void resetModule() { 
        for (int component = 0; component < 505; component++) {
            if(H_fADC.hasEntry(1, 1, component)) {
                H_fADC.get(1, 1, component).reset();
                H_PED.get(1, 1, component).reset();
                H_NOISE.get(1, 1, component).reset();
                H_LED_fADC.get(1, 1, component).reset();
                H_LED_fADC_NORM.get(1, 1, component).reset();
                H_LED_fADC_RANGES.get(1, 1, component).reset();
                H_LED_CHARGE.get(1, 1, component).reset();
                H_LED_VMAX.get(1, 1, component).reset();
                H_LED_TCROSS.get(1, 1, component).reset();
                H_LED_THALF.get(1, 1, component).reset();
                H_LED_WIDTH.get(1, 1, component).reset();                
                G_LED_CHARGE.get(1, 1, component).reset();
                G_LED_CHARGE_SELECT.get(1, 1, component).reset();
                G_LED_AMPLI.get(1, 1, component).reset();
                G_LED_AMPLI_SELECT.get(1, 1, component).reset();
                G_LED_WIDTH.get(1, 1, component).reset();
            }
            H_fADC_N.reset();
            H_LED_N.reset();
        }
        for (int i = 0; i < 484; i++) {
            ledCharge[i] = 0;
            ledCharge2[i] = 0;
            ledAmpli[i] = 0;
            ledAmpli2[i] = 0;
            ledWidth[i] = 0;
            ledWidth2[i] = 0;
            ledEvent[i] = -1;
            ledNEvents[i] = 0;
        }
        this.plotHistograms();
        this.resetTable();
    }
    
    public void resetTable() {
        for (int j=3; j<this.calib.getColumnCount(); j++) {
            for (int component : this.detector.getDetectorComponents()) { 
                this.calib.setDoubleValue(-1.,this.calib.getColumnName(j),1, 1, component);
            }
        }
        this.calib.fireTableDataChanged();
    }
    
    public void resetWave(int component) { 
        if(H_WAVE.hasEntry(1, 1, component)) {
            H_WAVE.get(1, 1, component).reset();
            H_WAVE_PED.get(1, 1, component).reset();
            H_WAVE_PUL.get(1, 1, component).reset();
        }
    }
                
    @Override
    public void processShape(DetectorShape2D desc) {
        int sector    = desc.getDescriptor().getSector();
        int layer     = desc.getDescriptor().getLayer();
        keySelect = desc.getDescriptor().getComponent();
        this.plotHistograms();
    }

    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        this.keySelect = component;
        this.plotHistograms();
    }

    public void dataEventAction(DataEvent event) {
        // TODO Auto-generated method stub

        nProcessed++;
        if(event instanceof EvioDataEvent){
            try {
                    List<DetectorDataDgtz>  dataList = decoder.getDataEntries((EvioDataEvent) event); 
                    this.runNumber = decoder.getRunNumber();
                    detectorDecoder.translate(dataList);
                    detectorDecoder.fitPulses(dataList);

        //                System.out.println(dataList.size());


            //    System.out.println("event #: " + nProcessed);
        //        List<DetectorCounter> counters = decoder.getDetectorCounters(DetectorType.FTCAL);
            List<DetectorDataDgtz>  counters = new ArrayList<DetectorDataDgtz>();
            for(DetectorDataDgtz entry : dataList){
                if(entry.getDescriptor().getType()==DetectorType.FTCAL){
                    if(entry.getADCSize()>0){
                        counters.add(entry);
                    }
                }
            }     
    //        this.detectorView.getView().fill(dataList,"");
    //        System.out.println(counters.size());
    //        FTCALled.MyADCFitter fadcFitter = new FTCALled.MyADCFitter();
            H_WMAX.reset();
            H_TCROSS.reset();
            double tPMTCross=0;
            double tPMTHalf=0;
//            G_PULSE_ANALYSIS.clear();
            for (DetectorDataDgtz counter : counters) {
                int key = counter.getDescriptor().getComponent();
    //                        System.out.println(counters.size() + " " + key + " " + counter.getDescriptor().getComponent());
    //                        System.out.println(counter);
                short pulse[] = counter.getADCData(0).getPulseArray();
                int ped_i1 = counter.getADCData(0).getPedistalMinBin();
                int ped_i2 = counter.getADCData(0).getPedistalMaxBin();
                int pul_i1 = counter.getADCData(0).getPulseMinBin();
                int pul_i2 = counter.getADCData(0).getPulseMaxBin();
                H_fADC_N.fill(key);
                this.resetWave(key);
                for (int i = 0; i < Math.min(pulse.length, H_fADC.get(1, 1, key).getAxis().getNBins()); i++) {
                    H_fADC.get(1, 1, key).fill(i, pulse[i] - counter.getADCData(0).getPedestal() + 10.0);
                    H_WAVE.get(1, 1, key).fill(i, pulse[i]);
                    if(i> ped_i1 && i<=ped_i2) H_WAVE_PED.get(1, 1, key).fill(i, pulse[i]);
                    if(i>=pul_i1 && i<=pul_i2) H_WAVE_PUL.get(1, 1, key).fill(i, pulse[i]);                    
                }
                H_WMAX.fill(key,counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal());
                H_TCROSS.fill(key,counter.getADCData(0).getTimeCourse());
                H_PED.get(1, 1, key).fill(counter.getADCData(0).getPedestal());
                H_NOISE.get(1, 1, key).fill(counter.getADCData(0).getRMS()*LSB);
                // save relevant info in pulse-analysis graph for debugging
                G_PULSE_ANALYSIS.get(1, 1, key).reset();
                G_PULSE_ANALYSIS.get(1, 1, key).addPoint(ped_i1+1.5,counter.getADCData(0).getPedestal(),0,0);
                G_PULSE_ANALYSIS.get(1, 1, key).addPoint(ped_i2+0.5,counter.getADCData(0).getPedestal(),0,0);            
                G_PULSE_ANALYSIS.get(1, 1, key).addPoint(pul_i1+0.5,counter.getADCData(0).getPulseValue(pul_i1),0,0);
                G_PULSE_ANALYSIS.get(1, 1, key).addPoint(pul_i2+0.5,counter.getADCData(0).getPulseValue(pul_i2),0,0);            
                if(counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal()>this.detectorDecoder.getFadcPanel().tet) {
                    H_LED_N.fill(key);
                    for (int i = 0; i < Math.min(pulse.length, H_LED_fADC.get(1, 1, key).getAxis().getNBins()); i++) {
                        H_LED_fADC.get(1, 1, key).fill(i, pulse[i]-counter.getADCData(0).getPedestal() + 10.0);                
                    }
                    H_LED_CHARGE.get(1, 1, key).fill(counter.getADCData(0).getADC()*LSB*nsPerSample/50);
                    H_LED_VMAX.get(1, 1, key).fill((counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal())*LSB);
                    H_LED_TCROSS.get(1, 1, key).fill(counter.getADCData(0).getTimeCourse()*nsPerSample-tPMTCross);
                    H_LED_THALF.get(1, 1, key).fill(counter.getADCData(0).getTime()-tPMTHalf); 
                    H_LED_WIDTH.get(1, 1, key).fill(counter.getADCData(0).getFWHM()*nsPerSample); 
                    G_PULSE_ANALYSIS.get(1, 1, key).addPoint(counter.getADCData(0).getThresholdCrossing()+0.5,counter.getADCData(0).getPulseValue(counter.getADCData(0).getThresholdCrossing()),0,0);
                    G_PULSE_ANALYSIS.get(1, 1, key).addPoint(counter.getADCData(0).getTime()/nsPerSample+0.5,counter.getADCData(0).getHeight()/2,0,0);
                    G_PULSE_ANALYSIS.get(1, 1, key).addPoint(counter.getADCData(0).getPosition()+0.5,counter.getADCData(0).getHeight(),0,0);
//                    G_PULSE.addPoint(counter.getADCData(0).getTimeF()/nsPerSample,counter.getADCData(0).getHalf_Max(),0,0);

                    // fill info for time dependence evaluation
                    if(this.detector.hasComponent(key)) {
                        double ledCH = counter.getADCData(0).getADC()*LSB*nsPerSample/50;
                        double ledAM = (counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal())*LSB;
                        double ledFW = (counter.getADCData(0).getFWHM()*nsPerSample);
                        if(ledEvent[key]==-1) {
                            ledEvent[key] = nProcessed;
                        }
                        if(ledEvent[key]!=-1) {
                            if(nProcessed>ledEvent[key]+nLedEvents && nProcessed<=ledEvent[key]+2*nLedEvents) {
                                ledCharge[key]  = ledCharge[key]/ledNEvents[key];
                                ledCharge2[key] = ledCharge2[key]/ledNEvents[key]; 
                                ledAmpli[key]  = ledAmpli[key]/ledNEvents[key];
                                ledAmpli2[key] = ledAmpli2[key]/ledNEvents[key]; 
                                ledWidth[key]  = ledWidth[key]/ledNEvents[key];
                                ledWidth2[key] = ledWidth2[key]/ledNEvents[key]; 
                                double ledX  = ledEvent[key]+nLedEvents/2.;
                                double ledY  = ledCharge[key];
                                double ledEX = nLedEvents;
                                double ledEY = sqrt(ledCharge2[key]-ledCharge[key]*ledCharge[key])/sqrt(ledNEvents[key]);
                                G_LED_CHARGE.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY);
                                if(G_LED_CHARGE.get(1, 1, key).getDataSize(1)>nLedSkip) {
                                    G_LED_CHARGE_SELECT.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY);
                                    int npoints = G_LED_CHARGE_SELECT.get(1, 1, key).getDataSize(0);
                                    double mean = G_LED_CHARGE_SELECT.get(1, 1, key).getVectorY().getMean();
                                    double lowX = G_LED_CHARGE_SELECT.get(1, 1, key).getDataX(0)-G_LED_CHARGE_SELECT.get(1, 1, key).getDataEX(0);
                                    double higX = G_LED_CHARGE_SELECT.get(1, 1, key).getDataX(npoints-1)+G_LED_CHARGE_SELECT.get(1, 1, key).getDataEX(npoints-1);
                                    deltaCharge = mean*0.01;
                                    F_LED_CHARGE_SELECT_MEAN.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_CHARGE_SELECT_MEAN.get(1, 1, key).setParameter(0, mean);
                                    F_LED_CHARGE_SELECT_LOW.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_CHARGE_SELECT_LOW.get(1, 1, key).setParameter(0,  mean-deltaCharge);
                                    F_LED_CHARGE_SELECT_HIGH.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_CHARGE_SELECT_HIGH.get(1, 1, key).setParameter(0, mean+deltaCharge);
                                } 
                                ledY  = ledAmpli[key];
                                ledEY = sqrt(ledAmpli2[key]-ledAmpli[key]*ledAmpli[key])/sqrt(ledNEvents[key]);
                                G_LED_AMPLI.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY);
                                if(G_LED_AMPLI.get(1, 1, key).getDataSize(1)>nLedSkip) {
                                    G_LED_AMPLI_SELECT.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY);
                                    int npoints = G_LED_AMPLI_SELECT.get(1, 1, key).getDataSize(0);
                                    double mean = G_LED_AMPLI_SELECT.get(1, 1, key).getVectorY().getMean();
                                    double lowX = G_LED_AMPLI_SELECT.get(1, 1, key).getDataX(0)-G_LED_AMPLI_SELECT.get(1, 1, key).getDataEX(0);
                                    double higX = G_LED_AMPLI_SELECT.get(1, 1, key).getDataX(npoints-1)+G_LED_AMPLI_SELECT.get(1, 1, key).getDataEX(npoints-1);
                                    deltaAmpli  = mean*0.01;
                                    F_LED_AMPLI_SELECT_MEAN.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_AMPLI_SELECT_MEAN.get(1, 1, key).setParameter(0, mean);
                                    F_LED_AMPLI_SELECT_LOW.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_AMPLI_SELECT_LOW.get(1, 1, key).setParameter(0,  mean-deltaAmpli);
                                    F_LED_AMPLI_SELECT_HIGH.get(1, 1, key).setRange(lowX, higX);
                                    F_LED_AMPLI_SELECT_HIGH.get(1, 1, key).setParameter(0, mean+deltaAmpli);
                                } 
                                ledY  = ledWidth[key];
                                ledEY = sqrt(ledWidth2[key]-ledWidth[key]*ledWidth[key])/sqrt(ledNEvents[key]);
                                G_LED_WIDTH.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY);
//                                if(G_LED_WIDTH.get(1, 1, key).getDataSize(1)>nLedSkip) G_LED_WIDTH_SELECT.get(1, 1, key).addPoint(ledX,ledY,ledEX,ledEY); 
                                ledEvent[key]   = nProcessed;
                                ledCharge[key]  = ledCH;
                                ledCharge2[key] = ledCH*ledCH;
                                ledAmpli[key]   = ledAM;
                                ledAmpli2[key]  = ledAM*ledAM;
                                ledWidth[key]   = ledFW;
                                ledWidth2[key]  = ledFW*ledFW;
                                ledNEvents[key] = 1;
                            }
                            else if(nProcessed<=ledEvent[key]+nLedEvents) {
                                ledCharge[key]  += ledCH;
                                ledCharge2[key] += ledCH*ledCH;
                                ledAmpli[key]   += ledAM;
                                ledAmpli2[key]  += ledAM*ledAM;
                                ledWidth[key]   += ledFW;
                                ledWidth2[key]  += ledFW*ledFW;
                                ledNEvents[key]++;
                            }
                        }
                    }
                }
//                G_PULSE_ANALYSIS.add(1, 1, key, G_PULSE);
            }
            this.canvasEvent.update();

            this.detector.repaint();
        } 
        catch (Exception e) {
                e.printStackTrace();
            }
        }
    


    }
    
    private void drawWave(EmbeddedCanvas canvas) {
        canvas.cd(0);
        canvas.draw(H_WAVE.get(1, 1, keySelect));
        if(debugFlag) {
            canvas.draw(H_WAVE_PED.get(1, 1, keySelect),"same");
            canvas.draw(H_WAVE_PUL.get(1, 1, keySelect),"same");
//            if(G_PULSE_ANALYSIS.hasEntry(1, 1, keySelect)) {
//                if(G_PULSE_ANALYSIS.get(1, 1, keySelect).getDataSize(1)>0) canvas.draw(G_PULSE_ANALYSIS.get(1, 1, keySelect),"same");
//            }
        } 
        canvas.update();
    }
    
    
    private boolean getComponentStatus(int key) {
        boolean componentStatus = false;
        if(H_WMAX.getBinContent(key)>this.detectorDecoder.getFadcPanel().tet) {
            componentStatus= true;
        }
        return componentStatus;
    }
    
    private double getParameterValue(int index, int key) {
        double value = -1;
        if(this.detector.hasComponent(key)) {
            switch (index) {
                case 0: 
                {
                    break;
                }
                case 1: 
                {
                    value = this.H_NOISE.get(1, 1, key).getMean();
                    break;
                }  
                case 2:
                {
                    value = (int) this.H_LED_N.getBinContent(key);
                    break;
                }
                case 3:
                {
                    value = H_PED.get(1, 1, key).getMean();
                    break;
                }
                case 4:
                {
                    value = H_NOISE.get(1, 1, key).getMean();
                    break;
                }
                case 5:
                {
                    if(this.G_LED_CHARGE_SELECT.get(1, 1, key).getDataSize(1)>0) value = G_LED_CHARGE_SELECT.get(1, 1, key).getVectorY().getMean();
                    break;
                }
                case 6:
                {
                    if(this.G_LED_CHARGE_SELECT.get(1, 1, key).getDataSize(1)>0) value = G_LED_CHARGE_SELECT.get(1, 1, key).getVectorY().getRMS();
                    break;
                }
                case 7:
                {
                    if(this.G_LED_AMPLI_SELECT.get(1, 1, key).getDataSize(1)>0) value = G_LED_AMPLI_SELECT.get(1, 1, key).getVectorY().getMean();
                    break;
                }
                case 8:
                {
                    if(this.G_LED_AMPLI_SELECT.get(1, 1, key).getDataSize(1)>0) value = G_LED_AMPLI_SELECT.get(1, 1, key).getVectorY().getRMS();
                    break;
                }
                case 9:
                {
                    value = this.H_LED_WIDTH.get(1, 1, key).getMean();
                    break;
                }
                case 10:
                {
                    value = this.H_LED_THALF.get(1, 1, key).getMean();
                    break;
                }
                default: value =-1;
                    break;
            }
        }
        return value;
    }

    @Override
    public void update(DetectorShape2D shape) {

        int sector = shape.getDescriptor().getSector();
        int layer = shape.getDescriptor().getLayer();
        int component = shape.getDescriptor().getComponent();
        //shape.setColor(200, 200, 200);
        
        Color col = new Color(100, 100, 100);
        if(this.H_fADC.get(sector, layer, component).getEntries()>0) {
            if(this.plotSelect==0) {
                if(H_WMAX.getBinContent(component)>this.detectorDecoder.getFadcPanel().tet) {
                    if(H_TCROSS.getBinContent(component)>0) {
                        col = new Color(140, 0, 200);
                    }
                    else {
                        col = new Color(200, 0, 200);
                    }
                }
            }
            else if(plotSelect==1) {
                col = this.parameters.get(1).getColor(this.getParameterValue(1,component));
            }
            else if(plotSelect==41) {
                col = palette.getColor3D(this.getParameterValue(4,component), 1200., false); 
            }
            else {
                col = this.parameters.get(plotSelect).getColor(this.getParameterValue(plotSelect,component));
            }
        }
        shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
//        System.out.println("FTCALViewerModule ACTION = " + e.getActionCommand());
        for(int i=0; i<this.parameters.size(); i++) {
            if(e.getActionCommand() == this.parameters.get(i).getName()) {
                this.plotSelect=i;
                this.detector.getView().getColorAxis().setRange(0, this.parameters.get(i).getLimit());
                this.detector.getView().repaint();
                break;
            }
        }
        if(e.getActionCommand() == "comboBoxChanged") {
            for(int i=0; i<this.parameters.size(); i++) {
                String item = (String) this.radioList.getSelectedItem();
                if(item == this.parameters.get(i).getName()) {
                    this.plotSelect=i;
                    this.detector.getView().getColorAxis().setRange(0, this.parameters.get(i).getLimit());
                    this.detector.getView().repaint();
                    break;
                }
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
//        System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " with index " + index);
//        plotSelect = index;
//        this.updateTable();
//        this.detector.getView().repaint();
    }

    public void updateTable() {
        for(int key : this.detector.getDetectorComponents()) {
            if(H_PED.get(1, 1, key).getEntries()>0) {
                calib.setDoubleValue(H_PED.get(1, 1, key).getMean(),"Pedestal (Counts)",1, 1, key);
            }
            if(H_NOISE.get(1, 1, key).getEntries()>0) {
                calib.setDoubleValue(H_NOISE.get(1, 1, key).getMean(),"Noise (mV)",1, 1, key);
            }
            if(G_LED_CHARGE_SELECT.get(1, 1, key).getDataSize(1)>0) {
                calib.setDoubleValue(G_LED_CHARGE_SELECT.get(1, 1, key).getVectorY().getMean(),"Charge Mean (pC)",1, 1, key);
                calib.setDoubleValue(G_LED_CHARGE_SELECT.get(1, 1, key).getVectorY().getRMS(),"Charge Sigma (pC)",1, 1, key);
            }
            if(G_LED_AMPLI_SELECT.get(1, 1, key).getDataSize(1)>0) {               
                calib.setDoubleValue(G_LED_AMPLI_SELECT.get(1, 1, key).getVectorY().getMean(),"Amp. Mean (mV)",1, 1, key);
                calib.setDoubleValue(G_LED_AMPLI_SELECT.get(1, 1, key).getVectorY().getRMS(),"Amp. Sigma (mV)",1, 1, key);
            }
        }
        calib.fireTableDataChanged();
//        summaryTable.show();
    }
    
    private void addParameters(String... fields){
        for(String item : fields){
            this.parameters.add(new FTParameter(item));
        }
//        this.setRadioButtons();
        this.setRadioList();
    }
    
    
    private void setRadioButtons() {
        this.radioPane.setLayout(new FlowLayout());
        for (FTParameter par : this.parameters) {
            String item = par.getName();
//            System.out.println(item);
            // add buttons named as "fields" to the button group and panel
            JRadioButton b = new JRadioButton(item);
            if(radioGroup.getButtonCount()==0) b.setSelected(true);
            b.addActionListener(this);
            this.radioPane.add(b);
            radioGroup.add(b);
        }
    }

    private void setRadioList() {
        this.radioPane.setLayout(new FlowLayout());
        for (FTParameter par : this.parameters) {
            String item = par.getName();
//            System.out.println(item);
            // add buttons named as "fields" to the button group and panel
            this.radioList.addItem(item);
        }
        this.radioList.setSelectedIndex(0);
        this.radioList.addActionListener(this);
        this.radioPane.add(this.radioList);
    }

    public void setTimerUpdate(int timerUpdate) {
        this.timerUpdate = timerUpdate;
    }
    
    public void saveTable(String name) {

       try {
            // Open the output file
            File outputFile = new File(name);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);

            for (int i = 0; i < calib.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < calib.getColumnCount(); j++) {
                    line = line + calib.getValueAt(i, j);
                    if (j < calib.getColumnCount() - 1) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            outputBw.close();
            System.out.println("Constants saved to'" + name);
        } catch (IOException ex) {
            System.out.println(
                    "Error writing file '"
                    + name + "'");
            // Or we could just do this: 
            ex.printStackTrace();
        }

    }

}
