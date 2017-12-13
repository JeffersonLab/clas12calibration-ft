package org.clas.fthodo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import static java.lang.Math.*;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.clas.detector.CodaEventDecoder;
import org.clas.detector.DetectorDataDgtz;
import org.clas.detector.DetectorEventDecoder;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorPane2D;
import org.clas.view.DetectorShape2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.groot.data.H1F;
import org.jlab.groot.math.F1D;
import org.clas.ft.tools.FTAdjustFit;

public class FTHODOModule extends JPanel implements CalibrationConstantsListener, ActionListener, DetectorListener, ChangeListener {
    FTHodoWire wireFTHodo = new FTHodoWire();    
    CodaEventDecoder             decoder = new CodaEventDecoder();
    DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();
    //=================================
    //    PANELS, CANVASES ETC
    //=================================
    JSplitPane                 splitPane = new JSplitPane();
    JPanel                    canvasPane = new JPanel(new BorderLayout());
    JPanel                  detectorPane = new JPanel(new BorderLayout());
    JTabbedPane             detectorView = new JTabbedPane();
    ConstantsManager                ccdb = new ConstantsManager();
    CalibrationConstantsView canvasTable = new CalibrationConstantsView();
    CalibrationConstants       ConstantsTable = null;
    CalibrationConstants       CCDBTableNoise = null;
    CalibrationConstants       CCDBTableCharge2Energy = null;
    CalibrationConstants       CCDBTableCharge2EnergyMatchingTiles = null;
    CalibrationConstants       CCDBTablefACD = null;
    CalibrationConstants       FADC250ft3TablePED = null;
    CalibrationConstants       GAINandMIPSTableOverview = null;
    EmbeddedCanvas canvasEvent = new EmbeddedCanvas();
    EmbeddedCanvas canvasPed = new EmbeddedCanvas();
    EmbeddedCanvas canvasNoise = new EmbeddedCanvas();
    EmbeddedCanvas canvasNoiseAnalysis = new EmbeddedCanvas();
    EmbeddedCanvas canvasMIPsignal = new EmbeddedCanvas();
    EmbeddedCanvas canvasMIPAnalysis = new EmbeddedCanvas();
    EmbeddedCanvas canvasTime = new EmbeddedCanvas();
    EmbeddedCanvas canvasTimeAnalysis = new EmbeddedCanvas();
    ColorPalette palette = new ColorPalette();
    FTHodoHistograms histogramsFTHodo = new FTHodoHistograms();
    //=================================
    //           ARRAYS
    //=================================
    private double[][][] status;
    private double[][][] thrshNPE;
    private double[][][] pedMean;
    private double[][] pedValues4SlotChan;
    private double[][][] pedEvent;
    private double[][][] pedPreviousEvent;
    private double[][][] pedRMS;
    private double[][][] gain;
    private double[][][] errGain;
    private double[][][] gain_mV;
    private double[][][] errGain_mV;
    private double[][][] MIPgain;
    private double[][][] MIPerrgain;
    private double[][][] MIPgain_mV;
    private double[][][] MIPerrgain_mV;
    private double[][][] MIPMatchingTilesgain;
    private double[][][] MIPMatchingTileserrgain;
    private double[][][] MIPMatchingTilesgain_mV;
    private double[][][] MIPMatchingTileserrgain_mV;
    private double[][][] MIPS_pC_all;
    private double[][][] MIPS_pC_MatchingTiles;
    private double[][][] MIPS_maxV_all;
    private double[][][] MIPS_maxV_MatchingTiles;
    private double[][][] MIPSerr_pC_all;
    private double[][][] MIPSerr_pC_MatchingTiles;
    private double[][][] MIPSerr_maxV_all;
    private double[][][] MIPSerr_maxV_MatchingTiles;
    private double[][][] meanNPE;
    private double[][][] errNPE;
    private double[][][] sigNPE;
    private double[][][] meanNPE_mV;
    private double[][][] errNPE_mV;
    private double[][][] sigNPE_mV;
    double[][][] time_M3;
    double[][][] time_M7;
    double[][] dT_M3;
    double[][] dT_M7;
    // not ccdb constants
    private double[][][] npeEvent;
    private double[][][] vMax;
    private double[][][] vMaxEvent;
    private double[][][] qMax;
    private double[][][] ped_fadcCCDB;
    private double[][][] ped_noiseCCDB;
    private double[][][] pedrms_noiseCCDB;
    private double[][][] gain_pc_NoiseCCDB;
    private double[][][] gain_mV_NoiseCCDB;
    private double[][][] MIPS_pC_C2ECCDB;
    private double[][][] npeThreshold_NoiseCCDB;
    private int[][][] status_CCDB;
    //Indexed table to read in constants from ccdb
    IndexedTable geometryTable = null;
    IndexedTable calibrationfADC = null;
    IndexedTable calibrationNoiseTable = null;
    IndexedTable calibrationStatusTable = null;
    IndexedTable calibrationChargeToEnergyTable = null;
    IndexedTable calibrationTranslationTable = null;
    IndexedTable InverseTranslationTable = null;
    boolean debugging_mode = false;
    boolean setConstantsToCCDB = false;
    boolean pedMeanGood = false;
    int    runNumber       = 0;

    //=================================
    //           VARIABLES
    //=================================
    double tile_size = 15;
    int nDecodedProcessed = 0;
    private int secSel = 3;
    private int laySel = 1;
    private int comSel = 1;
    private int indexSel = 1;
    private boolean drawByElec = false;
    private boolean useGain_mV = true;
    private boolean matchingTiles = false;
    private boolean plotNPE = true;
    private boolean useGainCCDB = false;

    private int plotVoltageChargeBoth=1; //1==voltage 2 is Charge, 3 is both
    JPanel rBPaneGain;
    JPanel rBPaneMIP;
    JPanel rBPaneMIPgain;
    JToggleButton toggleMIPMatchingTilesBtn, toggleMIPGainMatchingTilesBtn, toggleMIPLEDBtn, toggleMIPGainLEDBtn;
    JToggleButton toggleNPEBtn;
    JToggleButton toggleGainCCDBBtn;
    JLabel LabelMIPMatchingTiles, LabelMIPGainMatchingTiles, LabelNPE, LabelMIPLED, LabelMIPGainLED, LabelGainCCDB;
    
    JComboBox MIP_ch_mv_chmvList;
    JComboBox NoiseAnalysis_ch_mv_List;
    JComboBox MIPAnalysis_ch_mv_List;
    JComboBox NoiseAnalysis_DetElec_List;
    JComboBox MIPAnalysis_DetElec_List;

    
    int previousTabSel = 0;
    private int tabSel = 0;
    private int tabSelDetectorView = 0;
    private int timerUpdate = 1000; //1 second
    // the following indices must correspond
    // to the order the canvased are added
    // to 'tabbedPane'
    final private int tabIndexEvent = 0;
    final private int tabIndexPed = 1;
    final private int tabIndexNoise = 2;
    final private int tabIndexNoiseAnalysis = 3;
    final private int tabIndexMIPsignal = 4;
    final private int tabIndexMIPAnalysis = 5;
    final private int tabIndexTime = 6;
    final private int tabIndexTimeAnalysis = 7;
    final private int tabIndexTable = 8;
    public String workDir         = null;


    public void initPanel() {
//        canvasTable.addListener(this);
        this.initCCDB();
        this.initConstants();
        this.initTable();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Event", this.canvasEvent);
        tabbedPane.add("Pedestal", this.canvasPed);
        tabbedPane.add("Noise Signal", this.canvasNoise);
        tabbedPane.add("Noise Analysis", this.canvasNoiseAnalysis);
        tabbedPane.add("MIP Signal", this.canvasMIPsignal);
        tabbedPane.add("MIP Analysis", this.canvasMIPAnalysis);
        tabbedPane.add("Time", this.canvasTime);
        tabbedPane.add("Time Analysis", this.canvasTimeAnalysis);
        tabbedPane.add("Table", this.canvasTable);
        
        tabbedPane.addChangeListener(this);
        tabbedPane.setSelectedIndex(this.tabSel);
        this.initCanvas();
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        JButton fitBtn = new JButton("Calibrate");
        fitBtn.addActionListener(this);
        buttonPane.add(fitBtn);

        JButton saveBtn = new JButton("Save Calibration Constants");
        saveBtn.addActionListener(this);
        buttonPane.add(saveBtn);
       
        JButton setDefaultBtn = new JButton("Set Constants to Default");
        setDefaultBtn.addActionListener(this);
        buttonPane.add(setDefaultBtn);

        JButton resetBtn = new JButton("Reset Histograms/Constants");
        resetBtn.addActionListener(this);
        buttonPane.add(resetBtn);
        
        
        toggleGainCCDBBtn = new JToggleButton("Use CCDB SiPM gain");
        buttonPane.add(toggleGainCCDBBtn);
        toggleGainCCDBBtn.addActionListener(this);
        toggleGainCCDBBtn.setSelected(useGainCCDB);
        if (useGainCCDB){
            LabelGainCCDB = new JLabel ( "ON" ) ;
        }
        else{
            LabelGainCCDB = new JLabel ( "OFF" ) ;
        }
        buttonPane.add(LabelGainCCDB);
        
        
        
   
        rBPaneGain = new JPanel();
        rBPaneGain.setLayout(new FlowLayout());
        
        String[] NoiseAnalysis_DetElec_Strings = { "Detector View", "Electronics View"};
        NoiseAnalysis_DetElec_List = new JComboBox(NoiseAnalysis_DetElec_Strings);
        rBPaneGain.add(NoiseAnalysis_DetElec_List);
        if (drawByElec)
            NoiseAnalysis_DetElec_List.setSelectedIndex(1);
        else
            NoiseAnalysis_DetElec_List.setSelectedIndex(0);
        NoiseAnalysis_DetElec_List.addActionListener(this);
  
        
        String[] NoiseAnalysis_ch_mv_Strings = { "max Voltage", "Charge"};
        NoiseAnalysis_ch_mv_List = new JComboBox(NoiseAnalysis_ch_mv_Strings);
        rBPaneGain.add(NoiseAnalysis_ch_mv_List);
        if (useGain_mV)
            NoiseAnalysis_ch_mv_List.setSelectedIndex(0);
        else
            NoiseAnalysis_ch_mv_List.setSelectedIndex(1);
        NoiseAnalysis_ch_mv_List.addActionListener(this);
        
     
        rBPaneMIP = new JPanel();
        rBPaneMIP.setLayout(new FlowLayout());

        String[] MIP_ch_mv_chmvStrings = { "max Voltage", "Charge", "Voltage & Charge"};
        MIP_ch_mv_chmvList = new JComboBox(MIP_ch_mv_chmvStrings);
        rBPaneMIP.add(MIP_ch_mv_chmvList);
        MIP_ch_mv_chmvList.setSelectedIndex(plotVoltageChargeBoth-1);
        MIP_ch_mv_chmvList.addActionListener(this);
   
        
        toggleMIPMatchingTilesBtn = new JToggleButton("Matching Tiles Analysis:");
        rBPaneMIP.add(toggleMIPMatchingTilesBtn);
        toggleMIPMatchingTilesBtn.addActionListener(this);
        toggleMIPMatchingTilesBtn.setSelected(matchingTiles);
        if (matchingTiles){
            LabelMIPMatchingTiles = new JLabel ( "ON" ) ;
        }
        else{
            LabelMIPMatchingTiles = new JLabel ( "OFF" ) ;
        }
        rBPaneMIP.add(LabelMIPMatchingTiles);

        toggleMIPLEDBtn = new JToggleButton("LED Analysis:");
        rBPaneMIP.add(toggleMIPLEDBtn);
        toggleMIPLEDBtn.addActionListener(this);
        toggleMIPLEDBtn.setSelected(histogramsFTHodo.ledAnalysis);
        if (histogramsFTHodo.ledAnalysis){
            LabelMIPLED = new JLabel ( "ON" ) ;
        }
        else{
            LabelMIPLED = new JLabel ( "OFF" ) ;
        }
        rBPaneMIP.add(LabelMIPLED);
        
 
        rBPaneMIPgain = new JPanel();
        rBPaneMIPgain.setLayout(new FlowLayout());
 
        String[] MIPAnalysis_DetElec_Strings = { "Detector View", "Electronics View"};
        MIPAnalysis_DetElec_List = new JComboBox(MIPAnalysis_DetElec_Strings);
        rBPaneMIPgain.add(MIPAnalysis_DetElec_List);
        if (drawByElec)
            MIPAnalysis_DetElec_List.setSelectedIndex(1);
        else
            MIPAnalysis_DetElec_List.setSelectedIndex(0);
        MIPAnalysis_DetElec_List.addActionListener(this);


        String[] MIPAnalysis_ch_mv_Strings = { "max Voltage", "Charge"};
        MIPAnalysis_ch_mv_List = new JComboBox(MIPAnalysis_ch_mv_Strings);
        rBPaneMIPgain.add(MIPAnalysis_ch_mv_List);
        if (useGain_mV)
            MIPAnalysis_ch_mv_List.setSelectedIndex(0);
        else
            MIPAnalysis_ch_mv_List.setSelectedIndex(1);
        MIPAnalysis_ch_mv_List.addActionListener(this);
        
        toggleNPEBtn = new JToggleButton("NPE");
        rBPaneMIPgain.add(toggleNPEBtn);
        toggleNPEBtn.addActionListener(this);
        toggleNPEBtn.setSelected(plotNPE);
        if (plotNPE){
            LabelNPE = new JLabel ( "ON" ) ;
        }
        else{
            LabelNPE = new JLabel ( "OFF" ) ;
        }
        rBPaneMIPgain.add(LabelNPE);
        
        toggleMIPGainMatchingTilesBtn = new JToggleButton("Matching Tiles Analysis:");
        rBPaneMIPgain.add(toggleMIPGainMatchingTilesBtn);
        toggleMIPGainMatchingTilesBtn.addActionListener(this);
        toggleMIPGainMatchingTilesBtn.setSelected(matchingTiles);
        if (matchingTiles){
            LabelMIPGainMatchingTiles = new JLabel ( "ON" ) ;
        }
        else{
            LabelMIPGainMatchingTiles = new JLabel ( "OFF" ) ;
        }
        rBPaneMIPgain.add(LabelMIPGainMatchingTiles);
        
        toggleMIPGainLEDBtn = new JToggleButton("LED Analysis:");
        rBPaneMIPgain.add(toggleMIPGainLEDBtn);
        toggleMIPGainLEDBtn.addActionListener(this);
        toggleMIPGainLEDBtn.setSelected(histogramsFTHodo.ledAnalysis);
        if (histogramsFTHodo.ledAnalysis){
            LabelMIPGainLED = new JLabel ( "ON" ) ;
        }
        else{
            LabelMIPGainLED = new JLabel ( "OFF" ) ;
        }
        rBPaneMIPgain.add(LabelMIPGainLED);

        //=================================
        //      PLOTTING OPTIONS
        //=================================
        this.canvasPane.add(tabbedPane, BorderLayout.CENTER);
        this.canvasPane.add(buttonPane, BorderLayout.PAGE_END);
        this.detectorPane.add(detectorView, BorderLayout.CENTER);
        this.detectorPane.add(this.detectorDecoder.getFadcPanel(), BorderLayout.PAGE_END);
        splitPane.setRightComponent(this.canvasPane);
        splitPane.setLeftComponent(this.detectorPane);
        splitPane.setDividerLocation(400);
        this.add(splitPane, BorderLayout.CENTER);
    } // end of: public void initPanel() {

    public void initCanvas() {
        GStyle.getAxisAttributesX().setTitleFontSize(16);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(16);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
 
        this.canvasEvent.divide(3, 2);
        this.canvasEvent.setGridX(false);
        this.canvasEvent.setGridY(false);
        this.canvasEvent.setAxisFontSize(10);
        this.canvasEvent.setStatBoxFontSize(8);
        this.canvasEvent.initTimer(timerUpdate);
        drawCanvasEvent(secSel, laySel, comSel);

        this.canvasPed.divide(3, 2);
        this.canvasPed.setGridX(false);
        this.canvasPed.setGridY(false);
        this.canvasPed.setAxisFontSize(10);
        this.canvasPed.setStatBoxFontSize(8);
        this.canvasPed.initTimer(timerUpdate);
        drawCanvasPed(secSel, laySel, comSel);

        this.canvasNoise.divide(3, 2);
        this.canvasNoise.setGridX(false);
        this.canvasNoise.setGridY(false);
        this.canvasNoise.setAxisFontSize(10);
        this.canvasNoise.setStatBoxFontSize(8);
        this.canvasNoise.initTimer(timerUpdate);
        drawCanvasNoise(secSel, laySel, comSel);

        this.canvasNoiseAnalysis.divide(3, 3);
        this.canvasNoiseAnalysis.setGridX(false);
        this.canvasNoiseAnalysis.setGridY(false);
        this.canvasNoiseAnalysis.setAxisFontSize(10);
        this.canvasNoiseAnalysis.setStatBoxFontSize(2);
        this.canvasNoiseAnalysis.initTimer(timerUpdate);
        if (drawByElec)
            drawCanvasNoiseAnalysisElec(secSel, laySel, comSel);
        else
            drawCanvasNoiseAnalysis();
        
        this.canvasMIPsignal.divide(2, 2);
        this.canvasMIPsignal.setGridX(false);
        this.canvasMIPsignal.setGridY(false);
        this.canvasMIPsignal.setAxisFontSize(10);
        this.canvasMIPsignal.setStatBoxFontSize(2);
        this.canvasMIPsignal.initTimer(timerUpdate);
        drawCanvasMIPsignal(secSel, laySel, comSel);
        
        this.canvasMIPAnalysis.divide(3, 3);
        this.canvasMIPAnalysis.setGridX(false);
        this.canvasMIPAnalysis.setGridY(false);
        this.canvasMIPAnalysis.setAxisFontSize(10);
        this.canvasMIPAnalysis.setStatBoxFontSize(2);
        this.canvasMIPAnalysis.initTimer(timerUpdate);
        if (drawByElec)
            drawCanvasMIPAnalysisElec(secSel, laySel, comSel);
        else
            drawCanvasMIPAnalysis();

        this.canvasTime.divide(3, 2);
        this.canvasTime.setGridX(false);
        this.canvasTime.setGridY(false);
        this.canvasTime.setAxisFontSize(10);
        this.canvasTime.setStatBoxFontSize(8);
        this.canvasTime.initTimer(timerUpdate);
        drawCanvasTime(secSel, laySel, comSel);
    }   
    
    
    public void initCCDB() {
        System.out.println("monitor.initCCDB()");
        ccdb.init(Arrays.asList(new String[]{
            "/daq/fadc/fthodo",
            "/daq/tt/fthodo",
            "/geometry/ft/fthodo",
            "/calibration/ft/fthodo/status",
            "/calibration/ft/fthodo/noise",
            "/calibration/ft/fthodo/charge_to_energy"}));
        this.detectorDecoder.getFadcPanel().init(ccdb,this.getRunNumber(),"/daq/fadc/fthodo", 72,3,1);
        this.geometryTable = ccdb.getConstants(this.getRunNumber(), "/geometry/ft/fthodo");
        this.calibrationfADC = ccdb.getConstants(this.getRunNumber(), "/daq/fadc/fthodo");
        this.calibrationStatusTable = ccdb.getConstants(this.getRunNumber(), "/calibration/ft/fthodo/status");
        this.calibrationNoiseTable = ccdb.getConstants(this.getRunNumber(), "/calibration/ft/fthodo/noise");
        this.calibrationChargeToEnergyTable=ccdb.getConstants(this.getRunNumber(), "/calibration/ft/fthodo/charge_to_energy");
        this.calibrationTranslationTable=ccdb.getConstants(this.getRunNumber(), "/daq/tt/fthodo");
    }

    private void initConstants() {
        histogramsFTHodo.initConstants();
        readCCDBconstants();
    }
    
    private void initTable() {
        ConstantsTable = new CalibrationConstants(3,
                "ped/F:" +//3
                "ped_fadcCCDB/F:"+//4
                "ped_noiseCCDB/F:"+//5
                "gain_pc/F:"+//6
                "gain_pc_NoiseCCDB/F:"+//7
                "gain_mv/F:"+//8
                "gain_mv_NoiseCCDB/F:"+//9
                "MIPS_pC_all/F:"+//10
                "MIPS_pC_MatchingTiles/F:"+//11
                "MIPS_pC_C2ECCDB/F:"+//12
                "MIPS_maxV_all/F:"+//13
                "MIPS_maxV_MatchingTiles/F:"+//14
                "NPE_pC_all/F:"+//15
                "NPE_pC_MatchingTiles/F:"+//16
                "NPE_maxV_all/F:"+//17
                "NPE_maxV_MatchingTiles/F:"+//18
                "status/I:"+//19
                "status_CCDB/I"); //20
        ConstantsTable.setPrecision(3);
        ConstantsTable.addConstraint(3, 100, 400);//ped
        ConstantsTable.addConstraint(4, 100, 400);//ped from fadc file
        ConstantsTable.addConstraint(5, 100, 400);//ped from noise file
        ConstantsTable.addConstraint(6, 15, 25);//noise gain in pC
        ConstantsTable.addConstraint(7, 15, 25);//noise gain from noise in pC
        ConstantsTable.addConstraint(8, 8, 13);//noise gain in mV
        ConstantsTable.addConstraint(9, 8, 13);//noise gain from noise in mV
        ConstantsTable.addConstraint(10, 700, 3000);//MIPS charge
        ConstantsTable.addConstraint(11, 700, 3000);//MIPS charge matching tiles
        ConstantsTable.addConstraint(12, 700, 3000);//MIPS charge from CCDB
        ConstantsTable.addConstraint(13, 350, 1500);//MIPS max volt
        ConstantsTable.addConstraint(14, 350, 1500);//MIPS max volt mathicng tiles
        ConstantsTable.addConstraint(15, 30, 150);//NPE from charge
        ConstantsTable.addConstraint(16, 30, 150);//NPE from charge mathicng tiles
        ConstantsTable.addConstraint(17, 30, 150);//NPE from maxV
        ConstantsTable.addConstraint(18, 30, 150);//NPE from maxV matching tiles
        ConstantsTable.addConstraint(19, 0, 0);//status
        ConstantsTable.addConstraint(20, 0, 0);//status from ccdb

        CCDBTableNoise= new CalibrationConstants(3,
                                                 "pedestal/F:" +//3
                                                 "pedestal_rms/F:"+//4
                                                 "gain_pc/F:"+//5
                                                 "gain_mv/F:"+//6
                                                 "npe_threshold/F");//7
        CCDBTableNoise.setPrecision(3);
        CCDBTableNoise.addConstraint(3, 100, 400);
        CCDBTableNoise.addConstraint(4, 0, 20);
        CCDBTableNoise.addConstraint(5, 10, 40);
        CCDBTableNoise.addConstraint(6, 5, 20);
        CCDBTableNoise.addConstraint(7, 1.5, 5);

        
        CCDBTableCharge2Energy= new CalibrationConstants(3,
                                                 "mips_charge/F:" +//3
                                                 "mips_energy/F");//4
        CCDBTableCharge2Energy.setPrecision(3);
        CCDBTableCharge2Energy.addConstraint(3, 500, 2000);
        CCDBTableCharge2Energy.addConstraint(4, 1, 3);

        CCDBTableCharge2EnergyMatchingTiles= new CalibrationConstants(3,
                                                         "mips_charge/F:" +//3
                                                         "mips_energy/F");//4
        CCDBTableCharge2EnergyMatchingTiles.setPrecision(3);
        CCDBTableCharge2EnergyMatchingTiles.addConstraint(3, 500, 2000);
        CCDBTableCharge2EnergyMatchingTiles.addConstraint(4, 1, 3);
        
        
        CCDBTablefACD= new CalibrationConstants(3,
                                                "pedestal/F:" +//3
                                                "nsb/I:" +//4
                                                "nsa/I:" +//5
                                                "tet/I:" +//6
                                                "window_offset/I:" +//7
                                                "window_size/I"); //8
        CCDBTablefACD.addConstraint(3, 100, 400);
        CCDBTablefACD.addConstraint(4, 1, 20);
        CCDBTablefACD.addConstraint(5, 20, 70);
        CCDBTablefACD.addConstraint(6, 2, 400);
       

        GAINandMIPSTableOverview = new CalibrationConstants(3,
                                                            "gain_pc/F:" +//3
                                                            "gain_mv/F:" +//4
                                                            "mips_charge/F:" +//5
                                                            "mips_voltage/F:" +//6
                                                            "mips_charge_MatchingTiles/F:" +//7
                                                            "mips_voltage_MatchingTiles/F:" +//8
                                                            "gainerr_pc/F:" +//9
                                                            "gainerr_mv/F:" +//10
                                                            "mipserr_charge/F:" +//11
                                                            "mipserr_voltage/F:" +//12
                                                            "mipserr_charge_MatchingTiles/F:" +//13
                                                            "mipserr_voltage_MatchingTiles/F"); //14
                                                           
                                                            
                                                            
        for (int sector = 1; sector < 9; sector++) {
            for (int layer = 1; layer < 3; layer++) {
                for (int component = 1; component < 21; component++) {
                    if (sector % 2 == 1 && component > 9) {
                        continue;
                    }
                    ConstantsTable.addEntry(sector,layer,component);
                    ConstantsTable.setDoubleValue(0.0,"ped", sector, layer, component);
                    ConstantsTable.setDoubleValue(ped_fadcCCDB[sector][layer][component],"ped_fadcCCDB", sector, layer, component);
                    ConstantsTable.setDoubleValue(ped_noiseCCDB[sector][layer][component],"ped_noiseCCDB", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"gain_pc", sector, layer, component);
                    ConstantsTable.setDoubleValue(gain_pc_NoiseCCDB[sector][layer][component],"gain_pc_NoiseCCDB", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"gain_mv", sector, layer, component);
                    ConstantsTable.setDoubleValue(gain_mV_NoiseCCDB[sector][layer][component],"gain_mv_NoiseCCDB", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"MIPS_pC_all", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"MIPS_pC_MatchingTiles", sector, layer, component);
                    ConstantsTable.setDoubleValue(MIPS_pC_C2ECCDB[sector][layer][component],"MIPS_pC_C2ECCDB", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"MIPS_maxV_all", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"MIPS_maxV_MatchingTiles", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"NPE_pC_all", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"NPE_pC_MatchingTiles", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"NPE_maxV_all", sector, layer, component);
                    ConstantsTable.setDoubleValue(0.0,"NPE_maxV_MatchingTiles", sector, layer, component);
                    ConstantsTable.setIntValue(0,"status", sector, layer, component);
                    ConstantsTable.setIntValue(status_CCDB[sector][layer][component],"status_CCDB", sector, layer, component);
                    
                    CCDBTableNoise.addEntry(sector,layer,component);
                    CCDBTableNoise.setDoubleValue(0.0,"pedestal", sector, layer, component);
                    CCDBTableNoise.setDoubleValue(0.0,"pedestal_rms", sector, layer, component);
                    CCDBTableNoise.setDoubleValue(0.0,"gain_pc", sector, layer, component);
                    CCDBTableNoise.setDoubleValue(0.0,"gain_mv", sector, layer, component);
                    CCDBTableNoise.setDoubleValue(0.0,"npe_threshold", sector, layer, component);
                
                    CCDBTableCharge2Energy.addEntry(sector,layer,component);
                    double mipscharge=0.0, mipsenergy=0.0;
//                    if (layer%2!=0){
//                         mipscharge=1000.0;
//                         mipsenergy=1.2;
//                    }
//                    else{
//                         mipscharge=2000.0;
//                         mipsenergy=2.65;
//                    }
                    CCDBTableCharge2Energy.setDoubleValue(mipscharge,"mips_charge", sector, layer, component);
                    CCDBTableCharge2Energy.setDoubleValue(mipsenergy,"mips_energy", sector, layer, component);

                    CCDBTableCharge2EnergyMatchingTiles.addEntry(sector,layer,component);
                    CCDBTableCharge2EnergyMatchingTiles.setDoubleValue(0.0,"mips_charge", sector, layer, component);
                    CCDBTableCharge2EnergyMatchingTiles.setDoubleValue(0.0,"mips_energy", sector, layer, component);
                    
                    GAINandMIPSTableOverview.addEntry(sector,layer,component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"gain_pc", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"gain_mv", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mips_charge", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mips_voltage", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mips_charge_MatchingTiles", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mips_voltage_MatchingTiles", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"gainerr_pc", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"gainerr_mv", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mipserr_charge", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mipserr_voltage", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mipserr_charge_MatchingTiles", sector, layer, component);
                    GAINandMIPSTableOverview.setDoubleValue(0.0,"mipserr_voltage_MatchingTiles", sector, layer, component);
   
                    ////HERE: Need to look at the order we write this info as GEMC is a bit weird
                    
                    int craten=InverseTranslationTable.getIntValue("crate", sector,layer,component);
                    int slotn=InverseTranslationTable.getIntValue("slot", sector,layer,component);//wireFTHodo.getSlot4SLC(sector, layer, component);
                    int channeln=InverseTranslationTable.getIntValue("chan", sector,layer,component);//wireFTHodo.getChan4SLC(sector, layer, component);
                    CCDBTablefACD.addEntry(craten,slotn,channeln);
                    CCDBTablefACD.setDoubleValue(0.0,"pedestal", craten,slotn,channeln);
                    CCDBTablefACD.setIntValue(10,"nsb", craten,slotn,channeln);
                    CCDBTablefACD.setIntValue(50,"nsa", craten,slotn,channeln);
                    CCDBTablefACD.setIntValue(10,"tet", craten,slotn,channeln);
                    CCDBTablefACD.setIntValue(940,"window_offset", craten,slotn,channeln);
                    CCDBTablefACD.setIntValue(400,"window_size", craten,slotn,channeln);
                }
            }
        }


//        ConstantsTable = new CalibrationConstants(3,
//                "status/I:" + // 3
//                "ped/F:" + // 4
//                "ped_rms/F:" + // 5
//                "gain_pc/F:" + // 6
//                "gain_mv/F:" + // 7
//                "thr_npe/F:" + // 8
//                "mips_e/F:" + // 9
//                "mips_q/F:" + // 10
//                "t_offset/F:" + // 11
//                "t_rms/F");// 12
//        ConstantsTable.setPrecision(3);
//        ConstantsTable.addConstraint(3, -0.5, 0.5);
//        ConstantsTable.addConstraint(4, 130.0, 440.0);
//        ConstantsTable.addConstraint(5, 1.0, 100.0);
//        ConstantsTable.addConstraint(6, 10.0, 30.0);
//        ConstantsTable.addConstraint(7, 6.0, 16.0);
//        ConstantsTable.addConstraint(8, 2.0, 3.0);
//        ConstantsTable.addConstraint(9, 1.0, 4.0);
//        ConstantsTable.addConstraint(10, 500, 3000);
//        ConstantsTable.addConstraint(11, -2.0, 2.0);
//        ConstantsTable.addConstraint(12, -5.0, 5.0);
//
//        for (int layer = 2; layer > 0; layer--) {
//            for (int sector = 1; sector < 9; sector++) {
//                for (int component = 1; component < 21; component++) {
//
//                    if (sector % 2 == 1 && component > 9) {
//                        continue;
//                    }
//                    ConstantsTable.addEntry(sector,layer,component);
//                    ConstantsTable.setIntValue(0,             "status", sector, layer, component);
//                    ConstantsTable.setDoubleValue(200.,          "ped", sector, layer, component);
//                    ConstantsTable.setDoubleValue(histogramsFTHodo.nThrshNPE, "ped_rms", sector, layer, component);
//                    ConstantsTable.setDoubleValue(10.,       "gain_pc", sector, layer, component);
//                    ConstantsTable.setDoubleValue(20.,       "gain_mv", sector, layer, component);
//                    ConstantsTable.setDoubleValue(10.,       "thr_npe", sector, layer, component);
//                    ConstantsTable.setDoubleValue(1.4,        "mips_e", sector, layer, component);
//                    ConstantsTable.setDoubleValue(700.,       "mips_q", sector, layer, component);
//                    ConstantsTable.setDoubleValue(99.9,     "t_offset", sector, layer, component);
//                    ConstantsTable.setDoubleValue(99.9,        "t_rms", sector, layer, component);
//                }
//            }
//        }
        ConstantsTable.fireTableDataChanged();
        canvasTable.addConstants(ConstantsTable, this);
    } // end of : private void initTable() {
    //-----------------------------------------
    private String[] readTable(int s,int l,int c,String fileName,int nColumns) {
        BufferedReader br;
        String[] values = {"0.", "0.", "0.", "0.", "0.",
            "0.", "0.", "0.", "0.", "0.",
            "0.", "0.", "0.", "0.", "0,"};
        String sString = String.valueOf(s);
        String lString = String.valueOf(l);
        String cString = String.valueOf(c);

        // System.out.println( sString + " " +
        // 			lString + " " + 
        // 			cString);
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("1")
                        || line.startsWith("2")
                        || line.startsWith("3")
                        || line.startsWith("4")
                        || line.startsWith("5")
                        || line.startsWith("6")
                        || line.startsWith("7")
                        || line.startsWith("8")) {

                    String slc_values[] = line.split("\t",
                            nColumns);

                    if (slc_values[0].compareTo(sString) == 0 && slc_values[1].compareTo(lString) == 0 && slc_values[2].compareTo(cString) == 0) {
// 			 System.out.println( slc_values[0] + " " +
// 					     slc_values[1] + " " +
// 					     slc_values[2]);
                        for (int i = 0; i < (nColumns - 3); i++) {
                            values[i] = slc_values[i + 3];
                        }
                    }
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println(" FileNotFoundException ");
        } catch (IOException ex) {
            System.out.println(" IOException ");
        }
        return values;
    }
 
    public FTHODOModule() {
        System.out.println(" -------------------");
        System.out.println(" FTHODOViewerModule ");
        System.out.println(" -------------------");
        histogramsFTHodo.threshold = (int) histogramsFTHodo.threshD;
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to: " + this.workDir);

    }
    public void initDetector() {
        detectorView.add("Detector", this.drawDetector(0., 0.));
        detectorView.add("Electronics", this.drawChannels(0., 0.));
        detectorView.addChangeListener(new changeListener());
        detectorView.setSelectedIndex(this.tabSelDetectorView);
    }
    class changeListener implements ChangeListener {
        public void stateChanged(ChangeEvent changeEvent) {
            JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            System.out.println("Detector Tab changed to: " + sourceTabbedPane.getTitleAt(index));
            if ("Detector".equals(sourceTabbedPane.getTitleAt(index))){
                drawByElec = false;
                MIPAnalysis_DetElec_List.setSelectedIndex(0);
                NoiseAnalysis_DetElec_List.setSelectedIndex(0);
            }else if ("Electronics".equals(sourceTabbedPane.getTitleAt(index))){
                drawByElec = true;
                MIPAnalysis_DetElec_List.setSelectedIndex(1);
                NoiseAnalysis_DetElec_List.setSelectedIndex(1);
            }
            
            if (tabSel == tabIndexNoiseAnalysis) {
                if (drawByElec == false) {
                    drawCanvasNoiseAnalysis();
                } else {
                    drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
                }
            }else if (tabSel == tabIndexMIPAnalysis){
                if (drawByElec == false) {
                    drawCanvasMIPAnalysis();
                } else {
                    drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
                }
            }
        }
    }
//
//    ChangeListener changeListener = new ChangeListener() {
//        public void stateChanged(ChangeEvent changeEvent) {
//            JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
//            int index = sourceTabbedPane.getSelectedIndex();
//            System.out.println("Detector Tab changed to: " + sourceTabbedPane.getTitleAt(index));
//            if ("Detector".equals(sourceTabbedPane.getTitleAt(index))){
//                drawByElec = false;
//                drawCanvasNoiseAnalysis();
//                drawCanvasMIPAnalysis();
//                MIPAnalysis_DetElec_List.setSelectedIndex(0);
//                NoiseAnalysis_DetElec_List.setSelectedIndex(0);
//            }else if ("Electronics".equals(sourceTabbedPane.getTitleAt(index))){
//                drawByElec = true;
//                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
//                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
//                MIPAnalysis_DetElec_List.setSelectedIndex(1);
//                NoiseAnalysis_DetElec_List.setSelectedIndex(1);
//            }
//        }
//    };



    public DetectorPane2D drawChannels(double x0, double y0) {
        DetectorPane2D channels = new DetectorPane2D();
        int nChannels = 16;
        int nMezz = 15;
        int sec;
        int com;
        int lay;
        int width = 10;
        for (int iMez = 0; iMez < nMezz; iMez++) {
            lay = 1;
            for (int iCh = 0; iCh < nChannels; iCh++) {
                if (iCh > 7) {
                    lay = 2;
                }
                if (iMez == 14 && wireFTHodo.isChannelEmpty(iCh)) {
                    continue;
                }
                com = wireFTHodo.getComp4ChMez(iCh, iMez);
                sec = wireFTHodo.getSect4ChMez(iCh, iMez);
                DetectorShape2D channel = new DetectorShape2D(DetectorType.FTHODO,
                        sec, lay, com);
                channel.createBarXY(width, width);
                channel.getShapePath().translateXYZ(2 * (iMez - 7) * width,
                        (width * iCh) + width * (lay - 1),
                        0.0);
                //viewChannels.setColor(0, 145, 0, 0);
                channels.getView().addShape("Crate", channel);
            }
        }
        channels.getView().addDetectorListener(this);
        for (String layer : channels.getView().getLayerNames()) {
            System.out.println(layer);
            channels.getView().setDetectorListener(layer, this);
        }
        channels.updateBox();
        return channels;
    }
    public DetectorPane2D drawDetector(double x0, double y0) {
        DetectorPane2D detector = new DetectorPane2D();
        // sectors 1-8 for each layer.
        // detector symmetry is fourfold
        // with elements 0-28 for each quarter.
        int sector;
        // tile component
        // 1-9 for odd sectors
        // 1-20 for even
        int component;
        // thick and thin
        int layer;
        // y-offset to place thin and thick layer on same pane
        double[] layerOffsetY = {-200.0, 200.0};
        // size of tiles per quadrant
        double[] tileSize = {15.0, 30.0, 15.0, 30.0, 30.0, 30.0, 30.0, 30.0, 15.0,
            30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0,
            30.0, 30.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0};
        double[] tileThickness = {7., 15.};
        //============================================================
        double[] xx = {-97.5, -75.0, -127.5, -105.0, -75.0,
            -135.0, -105.0, -75.0, -52.5,
            -45.0, -15.0, 15.0, 45.0, -45.0,
            -15.0, 15.0, 45.0, -45.0, -15.0,
            15.0, 45.0, -52.5, -37.5, -22.5,
            -7.5, 7.5, 22.5, 37.5, 52.5};
        double[] yy = {-127.5, -135.0, -97.5, -105.0, -105.0,
            -75.0, -75.0, -75.0, -52.5,
            -150.0, -150.0, -150.0, -150.0, -120.0,
            -120.0, -120.0, -120.0, -90.0, -90.0,
            -90.0, -90.0, -67.5, -67.5, -67.5,
            -67.5, -67.5, -67.5, -67.5, -67.5};
        //============================================================
        double xcenter = 0;
        double ycenter = 0;
        double zcenter;
        // two layers: I==0 for thin and I==1 for thick
        for (int layerI = 0; layerI < 2; layerI++) {
            layer = layerI + 1;
            // 4 symmetry sectors per layer (named quadrant) from 0-3
            for (int quadrant = 0; quadrant < 4; quadrant++) {
                // 29 elements per symmetry sector
                for (int element = 0; element < 29; element++) {
                    // sector is odd for first 9 elements
                    // and even for the rest
                    if (element < 9) {
                        sector = quadrant * 2 + 1;
                        // component number for odd sector is 1-9
                        component = element + 1;
                    } else {
                        sector = quadrant * 2 + 2;
                        // component number for even sector is 1-20
                        component = element + 1 - 9;
                    }

                    // calculate the x-element of the center of each tile;
                    switch (quadrant) {
                        case 0:
                            xcenter = xx[element];
                            break;
                        case 1:
                            xcenter = -yy[element];
                            break;
                        case 2:
                            xcenter = -xx[element];
                            break;
                        case 3:
                            xcenter = yy[element];
                            break;
                        default:
                            break;
                    }

                    // calculate the y-element of the center of each tile
                    switch (quadrant) {
                        case 0:
                            ycenter = yy[element] + layerOffsetY[layerI];
                            break;
                        case 1:
                            ycenter = xx[element] + layerOffsetY[layerI];
                            break;
                        case 2:
                            ycenter = -yy[element] + layerOffsetY[layerI];
                            break;
                        case 3:
                            ycenter = -xx[element] + layerOffsetY[layerI];
                            break;
                        default:
                            break;
                    }

                    if (layerI == 0) {
                        zcenter = -tileThickness[layerI] / 2.0;
                    } else {
                        zcenter = tileThickness[layerI] / 2.0;
                    }

                    // Sectors 1-8
                    // (sect=1: upper left - clockwise);
                    // layers 1-2 (thin==1, thick==2);
                    // tiles (1-9 for odd and 1-20 for even sectors)
                    DetectorShape2D shape = new DetectorShape2D(DetectorType.FTHODO,
                            sector,
                            layer,
                            component);

                    DetectorShape2D shape2 = new DetectorShape2D(DetectorType.FTHODO,
                            sector,
                            layer,
                            component);
                    // defines the 2D bars dimensions using the element size
                    shape.createBarXY(tileSize[element], tileSize[element]);
                    shape2.createBarXY(tileSize[element], tileThickness[layerI]);
                    // defines the placements of the 2D bar according to the
                    // xcenter and ycenter calculated above
                    shape.getShapePath().translateXYZ(xcenter, ycenter, zcenter);
                    shape.setColor(0, 0, 0, 0);
                    //===========================================================
                    // calculate the y-element of the center of each tile
                    switch (quadrant) {
                        case 0:
                            ycenter = yy[element];
                            break;
                        case 1:
                            ycenter = xx[element];
                            break;
                        case 2:
                            ycenter = -yy[element];
                            break;
                        case 3:
                            ycenter = -xx[element];
                            break;
                        default:
                            break;
                    }

                    shape2.setColor(0, 0, 0, 0);

                    shape2.getShapePath().translateXYZ(xcenter, zcenter, 0);

                    detector.getView().addShape("Side", shape2);
                    detector.getView().addShape("Front", shape);

                }
            }
        }
        detector.getView().addDetectorListener(this);
        for (String lay : detector.getView().getLayerNames()) {
            System.out.println(lay);
            detector.getView().setDetectorListener(lay, this);
        }
        detector.updateBox();
        return detector;
    }

    public void actionPerformed(ActionEvent e) {
        //System.out.println("ACTION = " + e.getActionCommand());
        if ("comboBoxChanged".equals(e.getActionCommand())) {
            JComboBox comboBox = (JComboBox) e.getSource();
            Object selected = comboBox.getSelectedItem();
            //System.out.println("Selected Item  = " + selected);
            if ("max Voltage".equals(selected)){
                this.plotVoltageChargeBoth=1;
                this.useGain_mV=true;
                NoiseAnalysis_ch_mv_List.setSelectedIndex(0);
                MIPAnalysis_ch_mv_List.setSelectedIndex(0);
            }else  if ("Charge".equals(selected)){
                this.plotVoltageChargeBoth=2;
                this.useGain_mV=false;
                drawCanvasMIPsignal(secSel,laySel,comSel);
                NoiseAnalysis_ch_mv_List.setSelectedIndex(1);
                MIPAnalysis_ch_mv_List.setSelectedIndex(1);
            }else  if ("Voltage & Charge".equals(selected)){
                this.plotVoltageChargeBoth=3;
                drawCanvasMIPsignal(secSel,laySel,comSel);
            }
            else if ("Detector View".equals(selected)){
                this.drawByElec=false;
                MIPAnalysis_DetElec_List.setSelectedIndex(0);
                NoiseAnalysis_DetElec_List.setSelectedIndex(0);
            }else if ("Electronics View".equals(selected)){
                this.drawByElec=true;
                MIPAnalysis_DetElec_List.setSelectedIndex(1);
                NoiseAnalysis_DetElec_List.setSelectedIndex(1);
            }

            MIP_ch_mv_chmvList.setSelectedIndex(this.plotVoltageChargeBoth-1);
            if (!drawByElec){
                drawCanvasNoiseAnalysis();
                drawCanvasMIPAnalysis();
            }
            else {
                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
            }
            drawCanvasMIPsignal(secSel,laySel,comSel);
        }
        if (e.getActionCommand().compareTo("Reset Histograms/Constants") == 0) {
            this.resetHistograms();
            this.setArraysToDefault();
            this.setGGraphGain();
            this.updateTable();
        }
        if (e.getActionCommand().compareTo("Calibrate") == 0) {
            this.fitHistograms();
            this.updateArrays();
            this.setGGraphGain();
            this.updateTable();
        }
        if (e.getActionCommand().compareTo("Set Constants to Default") == 0) {
            this.setArraysToDefault();
            this.setGGraphGain();
            this.updateTable();
        }
  
        if (e.getActionCommand().compareTo("Save Calibration Constants") == 0) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftHodo_Noise_" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.saveTable(fileName, CCDBTableNoise);
            }
            //System.out.println(fileName);
            
            fileName = "ftHodo_ChargeToEnergy_" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            fc = new JFileChooser();
            workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            file = new File(fileName);
            fc.setSelectedFile(file);
            returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.saveTable(fileName, CCDBTableCharge2Energy);
            }

            fileName = "ftHodo_ChargeToEnergyMatchingTiles_" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            fc = new JFileChooser();
            workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            file = new File(fileName);
            fc.setSelectedFile(file);
            returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.saveTable(fileName, CCDBTableCharge2EnergyMatchingTiles);
            }
            
            
            fileName = "ftHodo_fADC_" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            fc = new JFileChooser();
            workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            file = new File(fileName);
            fc.setSelectedFile(file);
            returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.saveTable(fileName, CCDBTablefACD);
            }
            
            
            fileName = "ftHodo_OverviewFile_" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            fc = new JFileChooser();
            workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            file = new File(fileName);
            fc.setSelectedFile(file);
            returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.saveTable(fileName, GAINandMIPSTableOverview);
            }
            
            
            fileName = "ftHodo_adcft3_ped" + this.getRunNumber() + "_" + df.format(new Date()) + ".txt";
            String timeSrng= " "+df.format(new Date());
            fc = new JFileChooser();
            workingDirectory = new File(this.workDir);
            System.out.println("Save: "+this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            file = new File(fileName);
            fc.setSelectedFile(file);
            returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();
                this.savefADC250ft3PedTable(fileName, timeSrng);
            }
        }
        if (e.getActionCommand().compareTo("Use CCDB SiPM gain") == 0) {
            if (this.toggleGainCCDBBtn.isSelected()) {
                System.out.println("CCDB gain button selected");
                this.useGainCCDB=true;
                LabelGainCCDB.setText("ON");
                this.updateArrays();
                this.setGGraphGain();
                this.updateTable();
            } else {
                System.out.println("CCDB gain  unselected");
                this.useGainCCDB=false;
                LabelGainCCDB.setText("OFF");
                this.updateArrays();
                this.setGGraphGain();
                this.updateTable();
            }
        }


//        if (e.getActionCommand().compareTo("max Voltage") == 0) {
//            if (this.tabSel==tabIndexNoiseAnalysis){
//                this.useGain_mV = true;
//                if (this.drawByElec){
//                    drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
//                }else{
//                    drawCanvasNoiseAnalysis();
//                }
//            }
//            if (this.tabSel==tabIndexMIPAnalysis){
//                this.useGain_mV = true;
//                if (this.drawByElec){
//                    drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
//                }else{
//                    drawCanvasMIPAnalysis();
//                }
//            }
//            if (this.tabSel==tabIndexMIPsignal){
//                this.plotVoltageChargeBoth=1;
//                drawCanvasMIPsignal(secSel,laySel,comSel);
//            }
//        }
//        if (e.getActionCommand().compareTo("Charge") == 0) {
//            if (this.tabSel==tabIndexNoiseAnalysis){
//                this.useGain_mV = false;
//                if (this.drawByElec) {
//                    drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
//                } else{
//                    drawCanvasNoiseAnalysis();
//                }
//            }
//            if (this.tabSel==tabIndexMIPAnalysis){
//                this.useGain_mV = false;
//                if (this.drawByElec) {
//                    drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
//                } else{
//                    drawCanvasMIPAnalysis();
//                }
//            }
//            if (this.tabSel==tabIndexMIPsignal){
//                this.plotVoltageChargeBoth=2;
//                drawCanvasMIPsignal(secSel,laySel,comSel);
//            }
//        }
//        if (e.getActionCommand().compareTo("Electronics View") == 0) {
//            this.drawByElec = true;
//            if (this.tabSel==tabIndexNoiseAnalysis){
//                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
//            }
//            else if (this.tabSel==tabIndexMIPAnalysis){
//                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
//            }
//        }
//        if (e.getActionCommand().compareTo("Detector View") == 0) {
//            this.drawByElec = false;
//            if (this.tabSel==tabIndexNoiseAnalysis){
//                drawCanvasNoiseAnalysis();
//            }
//            else if (this.tabSel==tabIndexMIPAnalysis){
//                drawCanvasMIPAnalysis();
//            }
//        }
//        if (e.getActionCommand().compareTo("Voltage & Charge") == 0) {
//            this.plotVoltageChargeBoth=3;
//            drawCanvasMIPsignal(secSel,laySel,comSel);
//        }
        if (e.getActionCommand().compareTo("Matching Tiles Analysis:") == 0) {
            if (this.tabSel==tabIndexMIPsignal){
                if (this.toggleMIPMatchingTilesBtn.isSelected()) {
                    System.out.println("Matching Tiles button selected");
                    this.matchingTiles=true;
                } else {
                    System.out.println("Matching Tiles button nunselected");
                    this.matchingTiles=false;
                }
                drawCanvasMIPsignal(secSel,laySel,comSel);
            }
            if (this.tabSel==tabIndexMIPAnalysis){
                if (this.toggleMIPGainMatchingTilesBtn.isSelected()) {
                    System.out.println("Matching Tiles button selected");
                    this.matchingTiles=true;

                } else {
                    System.out.println("Matching Tiles button  unselected");
                    this.matchingTiles=false;
                }
                if (!this.drawByElec)
                    drawCanvasMIPAnalysis();
                else if (this.drawByElec)
                    drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
            }
        }           
        if (matchingTiles){
            LabelMIPMatchingTiles.setText("ON");
            LabelMIPGainMatchingTiles.setText("ON");
        }
        else {
            LabelMIPMatchingTiles.setText("OFF");
            LabelMIPGainMatchingTiles.setText("OFF");
        }
        toggleMIPGainMatchingTilesBtn.setSelected(matchingTiles);
        toggleMIPMatchingTilesBtn.setSelected(matchingTiles);
        

        if (e.getActionCommand().compareTo("NPE") == 0) {
            if (this.toggleNPEBtn.isSelected()) {
                System.out.println("NPE button selected");
                this.plotNPE=true;
            } else {
                System.out.println("NPE button  unselected");
                this.plotNPE=false;
            }
            if (!this.drawByElec)
                drawCanvasMIPAnalysis();
            else if (this.drawByElec)
                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
        }
        if (plotNPE){
            LabelNPE.setText("ON");
        }
        else {
            LabelNPE.setText("OFF");
        }
        
        if (e.getActionCommand().compareTo("LED Analysis:") == 0) {
            if (this.tabSel==tabIndexMIPsignal){
                if (this.toggleMIPLEDBtn.isSelected()) {
                    System.out.println("LED Analysis selected");
                    histogramsFTHodo.ledAnalysis=true;
                } else {
                    System.out.println("LED Analysis unselected");
                    histogramsFTHodo.ledAnalysis=false;
                }
                this.setArraysToDefault();
                this.setGGraphGain();
                histogramsFTHodo.InitFunctions();
                drawCanvasMIPsignal(secSel,laySel,comSel);
            }
            if (this.tabSel==tabIndexMIPAnalysis){
                if (this.toggleMIPGainLEDBtn.isSelected()) {
                    System.out.println("LED Analysis selected");
                    histogramsFTHodo.ledAnalysis=true;
                } else {
                    System.out.println("LED Analysis selected unselected");
                    histogramsFTHodo.ledAnalysis=false;
                }
                this.setArraysToDefault();
                this.setGGraphGain();
                histogramsFTHodo.InitFunctions();
                if (!this.drawByElec)
                    drawCanvasMIPAnalysis();
                else if (this.drawByElec)
                    drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
            }
        }
        if (histogramsFTHodo.ledAnalysis){
            LabelMIPLED.setText("ON");
            LabelMIPGainLED.setText("ON");
        }
        else {
            LabelMIPLED.setText("OFF");
            LabelMIPGainLED.setText("OFF");
        }
        toggleMIPGainLEDBtn.setSelected(histogramsFTHodo.ledAnalysis);
        toggleMIPLEDBtn.setSelected(histogramsFTHodo.ledAnalysis);
    }

    private void fitHistograms() {
        histogramsFTHodo.fitHistograms();
    }

    void drawCanvasEvent(int secSel,int laySel,int comSel) {
        int layCD = laySel - 1;
        int oppCD = laySel % 2;// map [1,2] to [1,0]
        int oppSel = (laySel % 2) + 1;        // map [1,2] to [2,1]
        int layCDL = 3 * layCD;
        int oppCDL = 3 * oppCD;
        int layCDM = layCDL+1;
        int oppCDM = oppCDL+1;
        int layCDR = layCDM+1;
        int oppCDR = oppCDM+1;
        //----------------------------------------
        // Left top (bottom) for thin (thick) layer
        //  fADC pulse pedestal unsubtracted
        canvasEvent.cd(layCDL);
        if (histogramsFTHodo.H_FADC_RAW.hasEntry(secSel,laySel,comSel)){
            this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW.get(secSel,laySel,comSel)); 
                 if (histogramsFTHodo.H_FADC_RAW_PED.hasEntry(secSel,laySel,comSel))
                    this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW_PED.get(secSel,laySel,comSel),"same");
                 if (histogramsFTHodo.H_FADC_RAW_PUL.hasEntry(secSel,laySel,comSel))
                    this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW_PUL.get(secSel,laySel,comSel),"same");
                 //if (histogramsFTHodo.G_FADC_ANALYSIS.hasEntry(secSel,laySel,comSel)){
                 //   if (histogramsFTHodo.G_FADC_ANALYSIS.get(secSel,laySel,comSel).getDataSize(1)>1)   
                 //       this.canvasEvent.draw(histogramsFTHodo.G_FADC_ANALYSIS.get(secSel, laySel, comSel),"same");
                 //}
        }
        canvasEvent.cd(oppCDL);
        if (histogramsFTHodo.H_FADC_RAW.hasEntry(secSel,oppSel,comSel)){
            this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW.get(secSel,oppSel,comSel)); 
                 if (histogramsFTHodo.H_FADC_RAW_PED.hasEntry(secSel,oppSel,comSel))
                    this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW_PED.get(secSel,oppSel,comSel),"same");
                 if (histogramsFTHodo.H_FADC_RAW_PUL.hasEntry(secSel,oppSel,comSel))
                    this.canvasEvent.draw(histogramsFTHodo.H_FADC_RAW_PUL.get(secSel,oppSel,comSel),"same");
                 //if (histogramsFTHodo.G_FADC_ANALYSIS.hasEntry(secSel,oppSel,comSel)){
                 //   if (histogramsFTHodo.G_FADC_ANALYSIS.get(secSel,oppSel,comSel).getDataSize(1)>1)   
                 //       this.canvasEvent.draw(histogramsFTHodo.G_FADC_ANALYSIS.get(secSel, oppSel, comSel),"same");
                 //}
        }

        //----------------------------------------
        // Middle top (bottom) for thin (thick) layer
        //  fADC pulse pedestal subtracted
        canvasEvent.cd(layCDM);
        if (histogramsFTHodo.H_FADC.hasEntry(secSel, laySel, comSel)) {
            this.canvasEvent.draw(histogramsFTHodo.H_FADC.get(secSel,laySel,comSel));
            if (histogramsFTHodo.fThr.hasEntry(secSel,laySel,comSel)) {
                this.canvasEvent.draw(histogramsFTHodo.fThr.get(secSel,laySel,comSel), "same");
            }
        }
        //----------------------------------------
        // Middle top (bottom) for thin (thick) layer
        //  fADC pulse pedestal Subtracted
        canvasEvent.cd(oppCDM);
        if (histogramsFTHodo.H_FADC.hasEntry(secSel,oppSel,comSel)) {
            this.canvasEvent.draw(histogramsFTHodo.H_FADC.get(secSel,oppSel,comSel));
            if (histogramsFTHodo.fThr.hasEntry(secSel,oppSel,comSel)) {
                this.canvasEvent.draw(histogramsFTHodo.fThr.get(secSel,oppSel,comSel), "same");
            }
        }
        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        canvasEvent.cd(layCDR);
        if (histogramsFTHodo.H_VT.hasEntry(secSel,laySel,comSel)) {
            this.canvasEvent.draw(histogramsFTHodo.H_VT.get(secSel,laySel,comSel));
            if (histogramsFTHodo.fVThr.hasEntry(secSel,laySel,comSel)) {
                this.canvasEvent.draw(histogramsFTHodo.fVThr.get(secSel,laySel,comSel), "same");
            }
        }
        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        canvasEvent.cd(oppCDR);
        if (histogramsFTHodo.H_VT.hasEntry(secSel,oppSel,comSel)) {
            this.canvasEvent.draw(histogramsFTHodo.H_VT.get(secSel,oppSel,comSel));
            if (histogramsFTHodo.fVThr.hasEntry(secSel,oppSel,comSel)) {
                this.canvasEvent.draw(histogramsFTHodo.fVThr.get(secSel,oppSel,comSel), "same");
            }
        }

    }

    void drawCanvasPed(int secSel,int laySel,int comSel) {
        int layCD = laySel - 1;        // map [1,2] to [0,1]
        int oppCD = laySel % 2;        // map [1,2] to [1,0]
        int oppSel = (laySel % 2) + 1;        // map [1,2] to [2,1]
        int layCDL = 3 * layCD;// 1-->0; 2-->3;
        int oppCDL = 3 * oppCD;// 1-->3; 2-->0;
        int layCDM = layCDL + 1;
        int oppCDM = oppCDL + 1;
        int layCDR = layCDL + 2;
        int oppCDR = oppCDL + 2;
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        canvasPed.cd(layCDL);
        if (histogramsFTHodo.H_PED.hasEntry(secSel, laySel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED.get(secSel,laySel,comSel));
            if (histogramsFTHodo.fPed.hasEntry(secSel,laySel,comSel)) {
                this.canvasPed.draw(histogramsFTHodo.fPed.get(secSel,laySel,comSel), "same S");
            }
        }
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        canvasPed.cd(oppCDL);
        if (histogramsFTHodo.H_PED.hasEntry(secSel, oppSel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED.get(secSel,oppSel,comSel));
            if (histogramsFTHodo.fPed.hasEntry(secSel,oppSel,comSel)) {
                this.canvasPed.draw(histogramsFTHodo.fPed.get(secSel,oppSel,comSel), "same S");
            }
        }
        //----------------------------------------
        // Middle top (bottom) for thin (thick) layer
        canvasPed.cd(layCDM);
        if (histogramsFTHodo.H_PED_TEMP.hasEntry(secSel, laySel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED_TEMP.get(secSel,laySel,comSel));
        }
        //----------------------------------------
        // Middle top (bottom) for thin (thick) layer
        canvasPed.cd(oppCDM);
        if (histogramsFTHodo.H_PED_TEMP.hasEntry(secSel, oppSel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED_TEMP.get(secSel,oppSel,comSel));

        }
        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        canvasPed.cd(layCDR);
        if (histogramsFTHodo.H_PED_VS_EVENT.hasEntry(secSel, laySel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED_VS_EVENT.get(secSel,laySel,comSel));
        }
        // GraphErrors
        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        canvasPed.cd(oppCDR);
        if (histogramsFTHodo.H_PED_VS_EVENT.hasEntry(secSel, oppSel, comSel)) {
            this.canvasPed.draw(histogramsFTHodo.H_PED_VS_EVENT.get(secSel,oppSel,comSel));
        }
    }
    void drawCanvasNoise(int secSel,int laySel,int comSel) {
        int layCD = laySel - 1;        // map [1,2] to [0,1]
        int oppCD = laySel % 2;        // map [1,2] to [1,0]
        int oppSel = oppCD + 1;        // map [1,0] to [2,1]
        int layCDL = 3 * layCD;        // map [0,1] to [0,3]
        int oppCDL = 3 * oppCD;        // map [1,0] to [3,0]
        int layCDM = layCDL + 1;        // map [0,3] to [1,4]
        int oppCDM = oppCDL + 1;        // map [3,0] to [4,1]
        int layCDR = layCDL + 2;        // map [0,3] to [2,5]
        int oppCDR = oppCDL + 2;        // map [3,0] to [5,2]
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        // calibrated fADC pulse
        canvasNoise.cd(layCDL);
        if (histogramsFTHodo.H_VT.hasEntry(secSel, laySel, comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_VT.get(secSel,laySel,comSel));
        }
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        // calibrated fADC pulse
        canvasNoise.cd(oppCDL);
        if (histogramsFTHodo.H_VT.hasEntry(secSel, oppSel, comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_VT.get(secSel,oppSel,comSel));
        }
        //----------------------------------------
        // middle top (bottom) for thin (thick) layer
        // voltage maximum
        canvasNoise.cd(layCDM);
        if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel, laySel, comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_NOISE_V.get(secSel,laySel,comSel));
            if (histogramsFTHodo.fV2.hasEntry(secSel,laySel,comSel)) {
                this.canvasNoise.draw(histogramsFTHodo.fV2.get(secSel,laySel,comSel), "same S");
            }
        }

        //----------------------------------------
        // middle top (bottom) for thin (thick) layer
        // calibrated fADC pulse
        canvasNoise.cd(oppCDM);
        if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel, oppSel, comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_NOISE_V.get(secSel,oppSel,comSel));
            if (histogramsFTHodo.fV2.hasEntry(secSel,oppSel,comSel)) {
                this.canvasNoise.draw(histogramsFTHodo.fV2.get(secSel,oppSel,comSel), "same S");
            }
        }

        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        // accumulated noise charge
        canvasNoise.cd(layCDR);
        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel,laySel,comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,laySel,comSel));
        }
        if (histogramsFTHodo.fQ2.hasEntry(secSel,laySel,comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.fQ2.get(secSel,laySel,comSel), "same S");
        }
        //----------------------------------------
        // right top (bottom) for thin (thick) layer
        // calibrated fADC pulse
        canvasNoise.cd(oppCDR);
        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel,oppSel,comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,oppSel,comSel));
        }
        if (histogramsFTHodo.fQ2.hasEntry(secSel,oppSel,comSel)) {
            this.canvasNoise.draw(histogramsFTHodo.fQ2.get(secSel,oppSel,comSel), "same S");
        }
    }

    void drawCanvasTime(int secSel,int laySel, int comSel) {
        int layCD = laySel - 1;// map [1,2] to [0,1]
        int oppCD = laySel % 2; // map [1,2] to [1,0]
        int oppSel = oppCD + 1;// map [1,0] to [2,1]
        int layCDL = 3 * layCD; // map [0,1] to [0,3]
        int oppCDL = 3 * oppCD;// map [1,0] to [3,0]
        int layCDM = layCDL + 1;// map [0,3] to [1,4]
        int oppCDM = oppCDL + 1;// map [3,0] to [4,1]
        int layCDR = layCDL + 2;// map [0,3] to [2,5]
        int oppCDR = oppCDL + 2;// map [3,0] to [5,2]
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        canvasTime.cd(layCDL);
        if (histogramsFTHodo.H_MAXV_VS_T.hasEntry(secSel,laySel,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_MAXV_VS_T.get(secSel,laySel,comSel));
        }
        canvasTime.cd(oppCDL);
        if (histogramsFTHodo.H_MAXV_VS_T.hasEntry(secSel,oppSel,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_MAXV_VS_T.get(secSel,oppSel,comSel));
        }
        //----------------------------------------
        // middle top
        canvasTime.cd(layCDM);
        if (histogramsFTHodo.H_T_MODE7.hasEntry(secSel,laySel,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_T_MODE7.get(secSel,laySel,comSel));
            if (histogramsFTHodo.fT.hasEntry(secSel,laySel,comSel)) {
                this.canvasTime.draw(histogramsFTHodo.fT.get(secSel,laySel,comSel), "same S");
            }
        }
        canvasTime.cd(oppCDM);
        if (histogramsFTHodo.H_T_MODE7.hasEntry(secSel,oppSel,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_T_MODE7.get(secSel,oppSel,comSel));
            if (histogramsFTHodo.fT.hasEntry(secSel,oppSel,comSel)) {
                this.canvasTime.draw(histogramsFTHodo.fT.get(secSel,oppSel,comSel), "same S");
            }
        }
        //----------------------------------------
        // right top
        canvasTime.cd(2);
        if (histogramsFTHodo.H_DT_MODE7.hasEntry(secSel,1,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_DT_MODE7.get(secSel,1,comSel));
            //         if(fT.hasEntry(secSel,
            //                laySel,
            //                comSel))
            //                 this.canvasTime.draw(fT.get(secSel,
            //                         laySel,
            //                         comSel),"same S");
        }
        canvasTime.cd(5);
        if (histogramsFTHodo.H_T1_T2.hasEntry(secSel,1,comSel)) {
            this.canvasTime.draw(histogramsFTHodo.H_T1_T2.get(secSel,1,comSel));
        }
    }

    
    void drawCanvasMIPsignal(int secSel,int laySel,int comSel) {
        int layCD = laySel - 1;        // map [1,2] to [0,1]
        int oppCD = laySel % 2;        // map [1,2] to [1,0]
        int oppSel = (laySel % 2) + 1;// map [1,2] to [2,1]
        int layCDL = 2 * layCD;
        int oppCDL = 2 * oppCD;
        int layCDR = layCDL + 1;
        int oppCDR = oppCDL + 1;
        //----------------------------------------
        // left top (bottom) for thin (thick) layer
        if (plotVoltageChargeBoth==2){
            canvasMIPsignal.cd(layCDL);
            if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel, laySel, comSel)) {
                this.canvasMIPsignal.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,laySel,comSel));
                if (histogramsFTHodo.fQ2.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.fQ2.get(secSel,laySel,comSel), "same S");
                }
            }
            //----------------------------------------
            // left top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(oppCDL);
            if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel,oppSel,comSel)) {
                this.canvasMIPsignal.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,oppSel,comSel));
                if (histogramsFTHodo.fQ2.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.fQ2.get(secSel,oppSel,comSel), "same S");
                }
            }
            //----------------------------------------
            // right top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(layCDR);
            if (!matchingTiles){
                if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fQMIP.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,laySel,comSel), "same S");
                    }
                }
            }
            else {
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fQMIPMatching.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIPMatching.get(secSel,laySel,comSel), "same S");
                    }
                }
            }
            //----------------------------------------
            // right top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(oppCDR);
            if (!matchingTiles){
                if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fQMIP.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
            else {
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fQMIPMatching.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIPMatching.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
        }
        else if (plotVoltageChargeBoth==1){
            canvasMIPsignal.cd(layCDL);
            if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel, laySel, comSel)) {
                this.canvasMIPsignal.draw(histogramsFTHodo.H_NOISE_V.get(secSel,laySel,comSel));
                if (histogramsFTHodo.fV2.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.fV2.get(secSel,laySel,comSel), "same S");
                }
            }
            //----------------------------------------
            // left top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(oppCDL);
            if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel,oppSel,comSel)) {
                this.canvasMIPsignal.draw(histogramsFTHodo.H_NOISE_V.get(secSel,oppSel,comSel));
                if (histogramsFTHodo.fV2.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.fV2.get(secSel,oppSel,comSel), "same S");
                }
            }
            //----------------------------------------
            // right top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(layCDR);
            if (!matchingTiles){
                if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fVMIP.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,laySel,comSel), "same S");
                    }
                }
            }
            else {
                if (histogramsFTHodo.H_MIP_V_MatchingTiles.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fVMIPMatching.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIPMatching.get(secSel,laySel,comSel), "same S");
                    }
                }
            }
            //----------------------------------------
            // right top (bottom) for thin (thick) layer
            canvasMIPsignal.cd(oppCDR);
            if (!matchingTiles){
                if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fVMIP.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
            else{
                if (histogramsFTHodo.H_MIP_V_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fVMIPMatching.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIPMatching.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
        }
        else {
            if (!matchingTiles){
                canvasMIPsignal.cd(layCDL);
                if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fVMIP.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,laySel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // left top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDL);
                if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fVMIP.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,oppSel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(layCDR);
                if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fQMIP.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,laySel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDR);
                if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fQMIP.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
            else {
                canvasMIPsignal.cd(layCDL);
                if (histogramsFTHodo.H_MIP_V_MatchingTiles.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fVMIPMatching.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIPMatching.get(secSel,laySel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // left top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDL);
                if (histogramsFTHodo.H_MIP_V_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fVMIPMatching.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fVMIPMatching.get(secSel,oppSel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(layCDR);
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,laySel,comSel));
                    if (histogramsFTHodo.fQMIPMatching.hasEntry(secSel,laySel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIPMatching.get(secSel,laySel,comSel), "same S");
                    }
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDR);
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,oppSel,comSel));
                    if (histogramsFTHodo.fQMIPMatching.hasEntry(secSel,oppSel,comSel)) {
                        this.canvasMIPsignal.draw(histogramsFTHodo.fQMIPMatching.get(secSel,oppSel,comSel), "same S");
                    }
                }
            }
        }
    }
  
    void drawCanvasNoiseAnalysis() {
        this.canvasNoiseAnalysis.divide(3, 3);
        int sector2CD[] = {0,1,2,5,8,7,6,3};
        for (int isec=0; isec<8; isec++){
            canvasNoiseAnalysis.cd(sector2CD[isec]);
      
            if (useGain_mV){
               if (isec%2==0)
                    canvasNoiseAnalysis.draw( histogramsFTHodo.H_EMPTYGAIN_MV9);
               else
                    canvasNoiseAnalysis.draw(histogramsFTHodo.H_EMPTYGAIN_MV20);
                canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainDetectorV[0][isec],"same");
                canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainDetectorV[1][isec],"same");
            }
            else {
                if (isec%2==0)
                    canvasNoiseAnalysis.draw(histogramsFTHodo.H_EMPTYGAIN_PC9);
               else
                    canvasNoiseAnalysis.draw(histogramsFTHodo.H_EMPTYGAIN_PC20);
                canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainDetectorC[0][isec],"same");
                canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainDetectorC[1][isec],"same");
            }
        }
    }
    
    
    
    
    void drawCanvasMIPAnalysis() {
        //System.out.println("Mathcing:" +matchingTiles+" mv: "+useGain_mV);
        this.canvasMIPAnalysis.divide(3, 3);
        int sector2CD[] = {0,1,2,5,8,7,6,3};
        for (int isec=0; isec<8; isec++){
            canvasMIPAnalysis.cd(sector2CD[isec]);
            if (plotNPE){
                if (!matchingTiles){
                    if (useGain_mV){
                        if (isec%2==0)
                            canvasMIPAnalysis.draw( histogramsFTHodo.H_EMPTYMIPGAIN_MV9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_MV20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetectorV[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetectorV[1][isec],"same");
                    }
                    else {
                        if (isec%2==0)
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_PC9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_PC20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetectorC[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetectorC[1][isec],"same");
                    }
                }else  if (matchingTiles){
                    if (useGain_mV){
                        if (isec%2==0)
                            canvasMIPAnalysis.draw( histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_MV9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_MV20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetector_matchingTilesV[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetector_matchingTilesV[1][isec],"same");
                    }
                    else {
                        if (isec%2==0)
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_PC9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_PC20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetector_matchingTilesC[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainDetector_matchingTilesC[1][isec],"same");
                    }
                }
            }else {
                if (!matchingTiles){
                    if (useGain_mV){
                        if (isec%2==0)
                            canvasMIPAnalysis.draw( histogramsFTHodo.H_EMPTYMIPSIGN_MV9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_MV20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetectorV[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetectorV[1][isec],"same");
                    }
                    else {
                        if (isec%2==0)
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_PC9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_PC20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetectorC[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetectorC[1][isec],"same");
                    }
                }else  if (matchingTiles){
                    if (useGain_mV){
                        if (isec%2==0)
                            canvasMIPAnalysis.draw( histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_MV9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_MV20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetector_matchingTilesV[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetector_matchingTilesV[1][isec],"same");
                    }
                    else {
                        if (isec%2==0)
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_PC9);
                        else
                            canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_PC20);
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetector_matchingTilesC[0][isec],"same");
                        canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignDetector_matchingTilesC[1][isec],"same");
                    }
                }
            }
        }
    }
    
    void drawCanvasMIPAnalysisElec(int secSel,int laySel,int comSel) {
        if (secSel == 0 || laySel == 0) {
            return;
        }
        //System.out.println("Mathcing:" +matchingTiles+" mv: "+useGain_mV);
        canvasMIPAnalysis.divide(1, 1);
        int mezz = wireFTHodo.getMezz4SLC(secSel, laySel, comSel);
        canvasMIPAnalysis.cd(0);
        if (plotNPE){
            if (!matchingTiles){
                if (useGain_mV){
                    histogramsFTHodo.H_EMPTYMIPGAIN_ELE_MV.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_ELE_MV);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainElectronicsV[mezz],"same");
                }
                else{
                    histogramsFTHodo.H_EMPTYMIPGAIN_ELE_PC.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_ELE_PC);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainElectronicsC[mezz],"same");
                }
            }else if (matchingTiles){
                if (useGain_mV){
                    histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_ELE_MV.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_ELE_MV);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz],"same");
                }
                else{
                    histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_ELE_PC.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPGAIN_matchingTiles_ELE_PC);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz],"same");
                }
            }
        }else {
            if (!matchingTiles){
                if (useGain_mV){
                    histogramsFTHodo.H_EMPTYMIPSIGN_ELE_MV.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_ELE_MV);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignElectronicsV[mezz],"same");
                }
                else{
                    histogramsFTHodo.H_EMPTYMIPSIGN_ELE_PC.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_ELE_PC);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignElectronicsC[mezz],"same");
                }
            }else if (matchingTiles){
                if (useGain_mV){
                    histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_ELE_MV.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_ELE_MV);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz],"same");
                }
                else{
                    histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_ELE_PC.setTitle("Mezzanine "+mezz);
                    canvasMIPAnalysis.draw(histogramsFTHodo.H_EMPTYMIPSIGN_matchingTiles_ELE_PC);
                    canvasMIPAnalysis.draw(histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz],"same");
                }
            }
        }
    }
    

    void setGGraphGain(){
        for (int mezz = 0; mezz < 15; mezz++) {
            int sectI;
            int compI;
            int layeI;
            int ii = 0;
            double[] gainArr = new double[16];
            double[] gainErrArr = new double[16];
            double[] gainArrmV = new double[16];
            double[] gainErrArrmV = new double[16];
            
            double[] MIPgainArr = new double[16];
            double[] MIPgainErrArr = new double[16];
            double[] MIPgainArrmV = new double[16];
            double[] MIPgainErrArrmV = new double[16];
            
            double[] MIPMatchingTilesgainArr = new double[16];
            double[] MIPMatchingTilesgainErrArr = new double[16];
            double[] MIPMatchingTilesgainArrmV = new double[16];
            double[] MIPMatchingTilesgainErrArrmV = new double[16];

            double[] MIPsignArr = new double[16];
            double[] MIPsignErrArr = new double[16];
            double[] MIPsignArrmV = new double[16];
            double[] MIPsignErrArrmV = new double[16];
            
            double[] MIPMatchingTilessignArr = new double[16];
            double[] MIPMatchingTilessignErrArr = new double[16];
            double[] MIPMatchingTilessignArrmV = new double[16];
            double[] MIPMatchingTilessignErrArrmV = new double[16];
            
            double[] chanArr = new double[16];
            double[] chanErrArr = new double[16];
            
            for (int chan = 0; chan < 16; chan++) {
                sectI = wireFTHodo.getSect4ChMez(chan, mezz);
                compI = wireFTHodo.getComp4ChMez(chan, mezz);
                layeI = chan / 8 + 1;
                gainArr[ii] = gain[sectI][layeI][compI];
                gainErrArr[ii] = errGain[sectI][layeI][compI];
                gainArrmV[ii] = gain_mV[sectI][layeI][compI];
                gainErrArrmV[ii] = errGain_mV[sectI][layeI][compI];

                MIPgainArr[ii] = MIPgain[sectI][layeI][compI];
                MIPgainErrArr[ii] = MIPerrgain[sectI][layeI][compI];
                MIPgainArrmV[ii] = MIPgain_mV[sectI][layeI][compI];
                MIPgainErrArrmV[ii] = MIPerrgain_mV[sectI][layeI][compI];
                
                MIPMatchingTilesgainArr[ii] = MIPMatchingTilesgain[sectI][layeI][compI];
                MIPMatchingTilesgainErrArr[ii] = MIPMatchingTileserrgain[sectI][layeI][compI];
                MIPMatchingTilesgainArrmV[ii] = MIPMatchingTilesgain_mV[sectI][layeI][compI];
                MIPMatchingTilesgainErrArrmV[ii] = MIPMatchingTileserrgain_mV[sectI][layeI][compI];

                MIPsignArr[ii] = MIPS_pC_all[sectI][layeI][compI];
                MIPsignErrArr[ii] = MIPSerr_pC_all[sectI][layeI][compI];
                MIPsignArrmV[ii] = MIPS_maxV_all[sectI][layeI][compI];
                MIPsignErrArrmV[ii] = MIPSerr_maxV_all[sectI][layeI][compI];
                
                MIPMatchingTilessignArr[ii] = MIPS_pC_MatchingTiles[sectI][layeI][compI];
                MIPMatchingTilessignErrArr[ii] = MIPSerr_pC_MatchingTiles[sectI][layeI][compI];
                MIPMatchingTilessignArrmV[ii] = MIPS_maxV_MatchingTiles[sectI][layeI][compI];
                MIPMatchingTilessignErrArrmV[ii] = MIPSerr_maxV_MatchingTiles[sectI][layeI][compI];

                chanArr[ii] = chan;
                chanErrArr[ii] = 0;
                ii++;
            }
            String titleHY = "Gain (pC)";
            String title;
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGgainElectronicsC[mezz] = new GraphErrors("G_GainC"+mezz,chanArr,gainArr,chanErrArr,gainErrArr);
            histogramsFTHodo.GGgainElectronicsC[mezz].setTitle(title);
            histogramsFTHodo.GGgainElectronicsC[mezz].setTitleX("Channel");
            histogramsFTHodo.GGgainElectronicsC[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGgainElectronicsC[mezz].setMarkerSize(5);
            histogramsFTHodo.GGgainElectronicsC[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGgainElectronicsC[mezz].setMarkerStyle(laySel); // 1 or 2
            titleHY = "Gain (mV)";
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGgainElectronicsV[mezz] = new GraphErrors("G_GainV"+mezz,chanArr,gainArrmV,chanErrArr,gainErrArrmV);
            histogramsFTHodo.GGgainElectronicsV[mezz].setTitle(title);
            histogramsFTHodo.GGgainElectronicsV[mezz].setTitleX("Channel");
            histogramsFTHodo.GGgainElectronicsV[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGgainElectronicsV[mezz].setMarkerSize(5);
            histogramsFTHodo.GGgainElectronicsV[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGgainElectronicsV[mezz].setMarkerStyle(laySel); // 1 or 2
            
            titleHY = "NPE (from charge)";
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGMIPgainElectronicsC[mezz] = new GraphErrors("G_MIPGainC"+mezz,chanArr,MIPgainArr,chanErrArr,MIPgainErrArr);
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setTitle(title);
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPgainElectronicsC[mezz].setMarkerStyle(laySel); // 1 or 2
            
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz] = new GraphErrors("G_MIPGain_matchingTilesC"+mezz,chanArr,MIPMatchingTilesgainArr,chanErrArr,MIPMatchingTilesgainErrArr);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setTitle(title);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesC[mezz].setMarkerStyle(laySel); // 1 or 2
            
            titleHY = "NPE (from mV)";
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGMIPgainElectronicsV[mezz] = new GraphErrors("G_MIPGainC"+mezz,chanArr,MIPgainArrmV,chanErrArr,MIPgainErrArrmV);
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setTitle(title);
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPgainElectronicsV[mezz].setMarkerStyle(laySel); // 1 or 2
            
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz] = new GraphErrors("G_MIPGain_matchingTilesC"+mezz,chanArr,MIPMatchingTilesgainArrmV,chanErrArr,MIPMatchingTilesgainErrArrmV);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setTitle(title);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPgainElectronics_matchingTilesV[mezz].setMarkerStyle(laySel); // 1 or 2
            
            titleHY = "Charge (pC)";
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGMIPsignElectronicsC[mezz] = new GraphErrors("G_MIPSignC"+mezz,chanArr,MIPsignArr,chanErrArr,MIPsignErrArr);
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setTitle(title);
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPsignElectronicsC[mezz].setMarkerStyle(laySel); // 1 or 2
            
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz] = new GraphErrors("G_MIPSign_matchingTilesC"+mezz,chanArr,MIPMatchingTilessignArr,chanErrArr,MIPMatchingTilessignErrArr);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setTitle(title);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesC[mezz].setMarkerStyle(laySel); // 1 or 2
            
            titleHY = "Max V (mV)";
            title = "mezzanine" + mezz;
            histogramsFTHodo.GGMIPsignElectronicsV[mezz] = new GraphErrors("G_MIPSignC"+mezz,chanArr,MIPsignArrmV,chanErrArr,MIPsignErrArrmV);
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setTitle(title);
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPsignElectronicsV[mezz].setMarkerStyle(laySel); // 1 or 2
            
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz] = new GraphErrors("G_MIPSign_matchingTilesC"+mezz,chanArr,MIPMatchingTilessignArrmV,chanErrArr,MIPMatchingTilessignErrArrmV);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setTitle(title);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setTitleX("Channel");
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setTitleY(titleHY);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setMarkerSize(5);
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setMarkerColor(1); // 0-9 for given palette
            histogramsFTHodo.GGMIPsignElectronics_matchingTilesV[mezz].setMarkerStyle(laySel); // 1 or 2
        }

        for (int ilay = 0; ilay < 2; ilay++) {
            double[] gainArr;
            double[] gainErrArr;
            double[] gainArrmV;
            double[] gainErrArrmV;
            
            double[] MIPgainArr;
            double[] MIPgainErrArr;
            double[] MIPgainArrmV;
            double[] MIPgainErrArrmV;
            
            double[] MIPMatchingTilesgainArr;
            double[] MIPMatchingTilesgainErrArr;
            double[] MIPMatchingTilesgainArrmV;
            double[] MIPMatchingTilesgainErrArrmV;
            
            double[] MIPsignArr;
            double[] MIPsignErrArr;
            double[] MIPsignArrmV;
            double[] MIPsignErrArrmV;
            
            double[] MIPMatchingTilessignArr;
            double[] MIPMatchingTilessignErrArr;
            double[] MIPMatchingTilessignArrmV;
            double[] MIPMatchingTilessignErrArrmV;
            
            double[] chanArr;
            double[] chanErrArr;
            for (int isec = 0; isec < 8; isec++) {
                int elemntsInSec;
                if (isec%2==0)
                    elemntsInSec=9;
                else
                    elemntsInSec=20;
                gainArr = new double[elemntsInSec];
                gainErrArr = new double[elemntsInSec];
                gainArrmV = new double[elemntsInSec];
                gainErrArrmV = new double[elemntsInSec];
                
                MIPgainArr = new double[elemntsInSec];
                MIPgainErrArr = new double[elemntsInSec];
                MIPgainArrmV = new double[elemntsInSec];
                MIPgainErrArrmV = new double[elemntsInSec];
                
                MIPMatchingTilesgainArr = new double[elemntsInSec];
                MIPMatchingTilesgainErrArr = new double[elemntsInSec];
                MIPMatchingTilesgainArrmV = new double[elemntsInSec];
                MIPMatchingTilesgainErrArrmV = new double[elemntsInSec];

                MIPsignArr = new double[elemntsInSec];
                MIPsignErrArr = new double[elemntsInSec];
                MIPsignArrmV = new double[elemntsInSec];
                MIPsignErrArrmV = new double[elemntsInSec];
                
                MIPMatchingTilessignArr = new double[elemntsInSec];
                MIPMatchingTilessignErrArr = new double[elemntsInSec];
                MIPMatchingTilessignArrmV = new double[elemntsInSec];
                MIPMatchingTilessignErrArrmV = new double[elemntsInSec];
                
                
                
                chanArr = new double[elemntsInSec];
                chanErrArr = new double[elemntsInSec];
                for (int chan = 0; chan < elemntsInSec; chan++) {
                    gainArr[chan] = gain[isec+1][ilay+1][chan+1];
                    gainErrArr[chan] = errGain[isec+1][ilay+1][chan+1];
                    gainArrmV[chan] = gain_mV[isec+1][ilay+1][chan+1];
                    gainErrArrmV[chan] = errGain_mV[isec+1][ilay+1][chan+1];
                    
                    MIPgainArr[chan] = MIPgain[isec+1][ilay+1][chan+1];
                    MIPgainErrArr[chan] = MIPerrgain[isec+1][ilay+1][chan+1];
                    MIPgainArrmV[chan] = MIPgain_mV[isec+1][ilay+1][chan+1];
                    MIPgainErrArrmV[chan] = MIPerrgain_mV[isec+1][ilay+1][chan+1];
                    
                    MIPMatchingTilesgainArr[chan] = MIPMatchingTilesgain[isec+1][ilay+1][chan+1];
                    MIPMatchingTilesgainErrArr[chan] = MIPMatchingTileserrgain[isec+1][ilay+1][chan+1];
                    MIPMatchingTilesgainArrmV[chan] = MIPMatchingTilesgain_mV[isec+1][ilay+1][chan+1];
                    MIPMatchingTilesgainErrArrmV[chan] = MIPMatchingTileserrgain_mV[isec+1][ilay+1][chan+1];

                    
                    MIPsignArr[chan] = MIPS_pC_all[isec+1][ilay+1][chan+1];
                    MIPsignErrArr[chan] = MIPSerr_pC_all[isec+1][ilay+1][chan+1];
                    MIPsignArrmV[chan] = MIPS_maxV_all[isec+1][ilay+1][chan+1];
                    MIPsignErrArrmV[chan] = MIPSerr_maxV_all[isec+1][ilay+1][chan+1];
                    
                    MIPMatchingTilessignArr[chan] = MIPS_pC_MatchingTiles[isec+1][ilay+1][chan+1];
                    MIPMatchingTilessignErrArr[chan] = MIPSerr_pC_MatchingTiles[isec+1][ilay+1][chan+1];
                    MIPMatchingTilessignArrmV[chan] = MIPS_maxV_MatchingTiles[isec+1][ilay+1][chan+1];
                    MIPMatchingTilessignErrArrmV[chan] = MIPSerr_maxV_MatchingTiles[isec+1][ilay+1][chan+1];
                    
                    
                    chanArr[chan] = chan+1+ilay*0.3;
                    chanErrArr[chan] = 0;
                }
                String titleHY = "Gain (pC)";
                String title = "Sector " + (isec+1);
                histogramsFTHodo.GGgainDetectorC[ilay][isec] = new GraphErrors("G_GainC"+isec,chanArr,gainArr,chanErrArr,gainErrArr);
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setTitle(title);
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGgainDetectorC[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                titleHY = "Gain (mV)";
                title = "Sector " + (isec+1);
                histogramsFTHodo.GGgainDetectorV[ilay][isec] = new GraphErrors("G_GainV"+isec,chanArr,gainArrmV,chanErrArr,gainErrArrmV);
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setTitle(title);
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGgainDetectorV[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                titleHY = "NPE (from charge)";
                title = "Sector " + (isec+1);
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec] = new GraphErrors("G_MIPGainC"+isec,chanArr,MIPgainArr,chanErrArr,MIPgainErrArr);
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPgainDetectorC[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec] = new GraphErrors("G_MIP_matchingTilesGainC"+isec,chanArr,MIPMatchingTilesgainArr,chanErrArr,MIPMatchingTilesgainErrArr);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPgainDetector_matchingTilesC[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2

                titleHY = "NPE (from mV)";
                title = "Sector " + (isec+1);
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec] = new GraphErrors("G_MIPGainV"+isec,chanArr,MIPgainArrmV,chanErrArr,MIPgainErrArrmV);
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPgainDetectorV[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec] = new GraphErrors("G_MIP_matchingTilesGainV"+isec,chanArr,MIPMatchingTilesgainArrmV,chanErrArr,MIPMatchingTilesgainErrArrmV);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPgainDetector_matchingTilesV[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                titleHY = "Charge (pC)";
                title = "Sector " + (isec+1);
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec] = new GraphErrors("G_MIPSignC"+isec,chanArr,MIPsignArr,chanErrArr,MIPsignErrArr);
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPsignDetectorC[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec] = new GraphErrors("G_MIP_matchingTilesSignC"+isec,chanArr,MIPMatchingTilessignArr,chanErrArr,MIPMatchingTilessignErrArr);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPsignDetector_matchingTilesC[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                titleHY = "Max V (mV)";
                title = "Sector " + (isec+1);
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec] = new GraphErrors("G_MIPSignV"+isec,chanArr,MIPsignArrmV,chanErrArr,MIPsignErrArrmV);
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPsignDetectorV[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
                
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec] = new GraphErrors("G_MIP_matchingTilesSignV"+isec,chanArr,MIPMatchingTilessignArrmV,chanErrArr,MIPMatchingTilessignErrArrmV);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setTitle(title);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setTitleX("Component");
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setTitleY(titleHY);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setMarkerSize(5);
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setMarkerColor(ilay+1); // 0-9 for given palette
                histogramsFTHodo.GGMIPsignDetector_matchingTilesV[ilay][isec].setMarkerStyle(ilay+1); // 1 or 2
            }
        }
    }
    
    void drawCanvasNoiseAnalysisElec(int secSel,int laySel,int comSel) {
        if (secSel == 0 || laySel == 0) {
            return;
        }
        canvasNoiseAnalysis.divide(1, 1);
        int mezz = wireFTHodo.getMezz4SLC(secSel, laySel, comSel);
        canvasNoiseAnalysis.cd(0);
        if (useGain_mV){
            histogramsFTHodo.H_EMPTYGAIN_ELE_MV.setTitle("Mezannine "+mezz);
            canvasNoiseAnalysis.draw(histogramsFTHodo.H_EMPTYGAIN_ELE_MV);
            canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainElectronicsV[mezz],"same");
        }
        else{
            histogramsFTHodo.H_EMPTYGAIN_ELE_PC.setTitle("Mezannine "+mezz);
            canvasNoiseAnalysis.draw(histogramsFTHodo.H_EMPTYGAIN_ELE_PC);
            canvasNoiseAnalysis.draw(histogramsFTHodo.GGgainElectronicsC[mezz],"same");
        }
    }
    
    
    
//=======================================================
//    public Color getComponentStatus(int sec, int lay, int com) {
//        int index = com;
//        if (lay > 0){ // cal layer is always 0
//            index = getIndex4SLC(sec, lay, com);
//        }
//        Color col = new Color(100, 100, 100);
//        if (histogramsFTHodo.H_W_MAX.getBinContent(index) > histogramsFTHodo.cosmicsThrsh) {
//            col = palette.
//                    getColor3D(histogramsFTHodo.H_W_MAX.getBinContent(index),
//                            4000,
//                            true);
//        }
//        return col;
//    }

    public int getIndex4SLC(int sec, int lay, int com) {
        int sector_count[] = {0, 9, 29, 38, 58, 67, 87, 96};
        int index = (lay - 1) * 116 + sector_count[sec - 1] + com;
        return index;
    }
    public int[] getSLC4Index(int index) {
        int[] slc = {0, 0, 0};
        int layer = index / 116 + 1;
        int quadrant = (index - (layer - 1) * 116) / 29;
        int element = index - quadrant * 29 - (layer - 1) * 116;
        int sector;
        int component;
        if (element < 9) {
            sector = quadrant * 2 + 1;
            component = element + 1;
        } else {
            sector = quadrant * 2 + 2;
            component = element + 1 - 9;
        }
        slc[0] = sector;
        slc[1] = layer;
        slc[2] = component;
        return slc;
    }

    // for all shapes made this is executed
    // for every event and every action
    public void update(DetectorShape2D shape) {
        int sec = shape.getDescriptor().getSector();
        int lay = shape.getDescriptor().getLayer();
        int com = shape.getDescriptor().getComponent();
        int index = com;
        
        if (shape.getDescriptor().getType() == DetectorType.FTCAL) {
            sec = 0;
            lay = 0;
            index = com;
        } else {
            if (sec > 0 && sec < 9) {
                index = getIndex4SLC(sec, lay, com);
            } else {
                System.out.println(" S = " + sec + ",L = " + lay + ",C = " + com);
            }
        }
        double waveMax = histogramsFTHodo.H_W_MAX.getBinContent(index);
        double voltMax = histogramsFTHodo.H_V_MAX.getBinContent(index);
        double npeWaveMax = histogramsFTHodo.H_NPE_MAX.getBinContent(index);
        // map [0,4095] to [0,255]
        int signalAlpha = abs(min((int) (waveMax) / 16, 255));
        // map [0,20] to [0,255]
        int noiseAlpha = abs(min((int) (npeWaveMax / 20 * 255), 255));
        
        int pedDiff;
        if (histogramsFTHodo.H_PED.get(sec, lay, com).integral()>2){
            double avePedfromHist = histogramsFTHodo.H_PED.get(sec, lay, com).getMaximumBin();
            avePedfromHist = avePedfromHist * histogramsFTHodo.H_PED.get(sec, lay, com).getAxis().getBinWidth(2);
            avePedfromHist = avePedfromHist + histogramsFTHodo.H_PED.get(sec, lay, com).getAxis().min();
            pedDiff=(int) (abs(pedEvent[sec][lay][com]-avePedfromHist));
        }else{
            pedDiff=(int) (abs(pedEvent[sec][lay][com]-pedPreviousEvent[sec][lay][com]));
        }
        int pedAlpha = abs(min((int) (pedDiff), 255));
        Color noiseColor = palette.getColor3D(vMax[sec][lay][com],2 * histogramsFTHodo.nGain_mV,false);
        Color gainColor;
        if (useGain_mV) {
            gainColor = palette.getColor3D(gain_mV[sec][lay][com],1.0 * histogramsFTHodo.nGain_mV,true);
        } else {
            gainColor = palette.getColor3D(gain[sec][lay][com],1.0 * histogramsFTHodo.nGain,true);
        }
        Color voltColor = palette.getColor3D(vMax[sec][lay][com], 70 * histogramsFTHodo.nGain_mV,false);
        Color qColor = palette.getColor3D(qMax[sec][lay][com],250 * histogramsFTHodo.nGain,true);
        //System.out.println("qMax: "+qMax[sec][lay][com]);
        
        if (tabSel == tabIndexEvent) {
            if (waveMax > histogramsFTHodo.cosmicsThrsh) {
                shape.setColor(0, 255, 0, signalAlpha); //GREEN
            } else if (waveMax > histogramsFTHodo.noiseThrsh) {
                shape.setColor(255, 255, 0, noiseAlpha); //Yellow
            } else {
                shape.setColor(255, 255, 255, 0); //white
            }
        } else if (tabSel == tabIndexPed) {
            if (pedDiff > 10) {
                shape.setColor(255, 255, 0, pedAlpha); //Yellow
            }else {
                shape.setColor(0, 255, 0, pedAlpha); //Green
            }
        } else if (tabSel == tabIndexNoise) {
            shape.setColor(noiseColor.getRed(),noiseColor.getGreen(),noiseColor.getBlue());
        } else if (tabSel == tabIndexNoiseAnalysis) {
            shape.setColor(gainColor.getRed(),gainColor.getGreen(),gainColor.getBlue());
        } else if (tabSel == tabIndexMIPsignal){
            shape.setColor(voltColor.getRed(),voltColor.getGreen(),voltColor.getBlue());
        }
        
    } // end of : public void update(Detec

    //--------------------------------------------
    // Constants
    private double getStatus(int s, int l, int c) {
        return status[s][l][c];
    }

    //HERE: Check set status malakia!
    // 0  Okay, 1  Noisy, 3  Dead, 5  Other
    private void setStatus(int s, int l, int c) {

        boolean goodQ = false;
        boolean lowQ = false;
        boolean zeroQ = false;

        double meanQ = getQMean(s, l, c);

        if ((l == 1 && meanQ > 500)
                || (l == 2 && meanQ > 1000)) {
            goodQ = true;
        } else if (qMax[s][l][c] > 0.0) {
            lowQ = true;
        } else {
            zeroQ = true;
        }

        if (goodQ) {
            status[s][l][c] = 0.0;
        } else if (gain[s][l][c] == 0.0) {
            status[s][l][c] = 1.0;
        } else if (lowQ) {
            status[s][l][c] = 5.0;
        } else if (zeroQ) {
            status[s][l][c] = 3.0;
        }

    }

    private double getThrshNPE(int s, int l, int c) {
        return histogramsFTHodo.nThrshNPE;
    }

    private void setPedestal(int s, int l, int c) {
        int slot=InverseTranslationTable.getIntValue("slot", s,l,c);//wireFTHodo.getSlot4SLC(s,l,c);
        int chan=InverseTranslationTable.getIntValue("chan", s,l,c);//wireFTHodo.getChan4SLC(s,l,c);
        if (histogramsFTHodo.fPed.hasEntry(s, l, c)) {
            pedMean[s][l][c] = histogramsFTHodo.fPed.get(s, l, c).getParameter(1);
            pedRMS[s][l][c] = histogramsFTHodo.fPed.get(s, l, c).getParameter(2);
            pedValues4SlotChan[slot][chan]=pedMean[s][l][c];
        } else {
            pedMean[s][l][c] = histogramsFTHodo.nPedMean;
            pedRMS[s][l][c] = histogramsFTHodo.nPedRMS;
            pedValues4SlotChan[slot][chan]=500.0;
        }
    }
 
    private double getPedMean(int s, int l, int c) {
        return pedMean[s][l][c];
    }
    
    private double getPedRMS(int s, int l, int c) {
        return pedRMS[s][l][c];
    }
    
    private void setGainAll(int s, int l, int c) {
        double thisGain = 0.0;
        double gainError = 0.0;

        if (!this.useGainCCDB){
        if (histogramsFTHodo.fQ2.hasEntry(s, l, c)) {
            double n2 = histogramsFTHodo.fQ2.get(s, l, c).getParameter(4);
            double n1 = histogramsFTHodo.fQ2.get(s, l, c).getParameter(1);
            thisGain = n2 - n1;
        }
        else {
            thisGain = 0.0;
        }
        if (thisGain > 0.0) {
            double n2Error = histogramsFTHodo.fQ2.get(s, l, c).parameter(4).error();
            double n1Error = histogramsFTHodo.fQ2.get(s, l, c).parameter(1).error();
            gainError = n2Error * n2Error + n1Error * n1Error;
            gainError = sqrt(gainError);
        }
        else {
            gainError = 20.0;
        }
        gain[s][l][c] = thisGain;
        errGain[s][l][c] = gainError;

        if (histogramsFTHodo.fV2.hasEntry(s, l, c)) {
            double n2 = histogramsFTHodo.fV2.get(s, l, c).getParameter(4);
            double n1 = histogramsFTHodo.fV2.get(s, l, c).getParameter(1);
            thisGain = n2 - n1;
        }
        else {
            thisGain = 0.0;
        }
        if ( thisGain > 0.0) {
            double n2Error = histogramsFTHodo.fV2.get(s, l, c).parameter(4).error();
            double n1Error = histogramsFTHodo.fV2.get(s, l, c).parameter(1).error();
            gainError = n2Error * n2Error + n1Error * n1Error;
            gainError = sqrt(gainError);
        }
        else {
            gainError = 10.0;
        }
            gain_mV[s][l][c] = thisGain;
            errGain_mV[s][l][c] = gainError;
    }else {
        gain[s][l][c] = gain_pc_NoiseCCDB[s][l][c];
        errGain[s][l][c] = 0.0;
        gain_mV[s][l][c] = gain_mV_NoiseCCDB[s][l][c];
        errGain_mV[s][l][c] = 0.0;
    }
    }
    
    
    private double getGain(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return gain[s][l][c];
        else
            return gain_mV[s][l][c];
    }
   
    private double getGainError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return errGain[s][l][c];
        else
            return errGain_mV[s][l][c];
    }

    
    
    private void setMIPAll(int s, int l, int c) {
        double thisGain = 0.0;
        double gainError=0.0;
        if (histogramsFTHodo.fQMIP.hasEntry(s, l, c)){
            double n1 = histogramsFTHodo.fQMIP.get(s, l, c).getParameter(1);
            MIPS_pC_all[s][l][c]=n1;
            if (gain[s][l][c]>0)
                thisGain=n1/gain[s][l][c];
            else
                thisGain=0.0;
        }
        else {
            MIPS_pC_all[s][l][c]=0.0;
            thisGain=0.0;
        }
        MIPgain[s][l][c]=thisGain;
        if (thisGain > 0.0) {
            double n1Error = histogramsFTHodo.fQMIP.get(s, l, c).parameter(1).error()/histogramsFTHodo.fQMIP.get(s, l, c).getParameter(1);
            double n2Error =getGainError(s, l, c, "charge")/getGain(s, l, c, "charge");
            MIPSerr_pC_all[s][l][c]=histogramsFTHodo.fQMIP.get(s, l, c).getParameter(2);
            gainError=thisGain*sqrt(n1Error*n1Error+n2Error*n2Error);
        }
        else{
            MIPSerr_pC_all[s][l][c]=1000.0;
            gainError=50.0;
        }
        MIPerrgain[s][l][c]=gainError;

        if (histogramsFTHodo.fQMIPMatching.hasEntry(s, l, c)){
            double n1 = histogramsFTHodo.fQMIPMatching.get(s, l, c).getParameter(1);
            MIPS_pC_MatchingTiles[s][l][c]=n1;
            if (gain[s][l][c]>0)
                thisGain=n1/gain[s][l][c];
            else
                thisGain=0.0;
        }
        else {
            MIPS_pC_MatchingTiles[s][l][c]=0.0;
            thisGain=0.0;
        }
        MIPMatchingTilesgain[s][l][c]=thisGain;
        if (thisGain > 0.0) {
            double n1Error = histogramsFTHodo.fQMIPMatching.get(s, l, c).parameter(1).error()/histogramsFTHodo.fQMIPMatching.get(s, l, c).getParameter(1);
            double n2Error =getGainError(s, l, c, "charge")/getGain(s, l, c, "charge");
            MIPSerr_pC_MatchingTiles[s][l][c]=histogramsFTHodo.fQMIPMatching.get(s, l, c).getParameter(2);
            gainError=thisGain*sqrt(n1Error*n1Error+n2Error*n2Error);
        }else {
            MIPSerr_pC_MatchingTiles[s][l][c]=1000.0;
            gainError=50.0;
        }
        MIPMatchingTileserrgain[s][l][c]=gainError;
        
        if (histogramsFTHodo.fVMIP.hasEntry(s, l, c)){
            double n1 = histogramsFTHodo.fVMIP.get(s, l, c).getParameter(1);
            MIPS_maxV_all[s][l][c]=n1;
            if (gain_mV[s][l][c]>0)
                thisGain=n1/gain_mV[s][l][c];
            else
                thisGain=0.0;
        }
        else {
            MIPS_maxV_all[s][l][c]=0.0;
            thisGain=0.0;
        }
        MIPgain_mV[s][l][c]=thisGain;
        if (thisGain > 0.0) {
            double n1Error = histogramsFTHodo.fVMIP.get(s, l, c).parameter(1).error()/histogramsFTHodo.fVMIP.get(s, l, c).getParameter(1);
            double n2Error =getGainError(s, l, c, "peakvolt")/getGain(s, l, c, "peakvolt");
            MIPSerr_maxV_all[s][l][c]=histogramsFTHodo.fVMIP.get(s, l, c).getParameter(2);
            gainError=thisGain*sqrt(n1Error*n1Error+n2Error*n2Error);
        }else {
            MIPSerr_maxV_all[s][l][c]=1000.0;
            gainError=50.0;
        }
        MIPerrgain_mV[s][l][c]=gainError;
        if (histogramsFTHodo.fVMIPMatching.hasEntry(s, l, c)){
            double n1 = histogramsFTHodo.fVMIPMatching.get(s, l, c).getParameter(1);
            MIPS_maxV_MatchingTiles[s][l][c]=n1;
            if (gain_mV[s][l][c]>0)
                thisGain=n1/gain_mV[s][l][c];
            else
                thisGain=0.0;
        }
        else {
            MIPS_maxV_MatchingTiles[s][l][c]=0.0;
            thisGain=0.0;
        }
        MIPMatchingTilesgain_mV[s][l][c]=thisGain;
        if (thisGain > 0.0) {
            double n1Error = histogramsFTHodo.fVMIPMatching.get(s, l, c).parameter(1).error()/histogramsFTHodo.fVMIPMatching.get(s, l, c).getParameter(1);
            double n2Error =getGainError(s, l, c, "peakvolt")/getGain(s, l, c, "peakvolt");
            MIPSerr_maxV_MatchingTiles[s][l][c]= histogramsFTHodo.fVMIPMatching.get(s, l, c).getParameter(2);
            gainError=getMIPMatchingNPE(s, l, c, "peakvolt")*sqrt(n1Error*n1Error+n2Error*n2Error);
        }
        else{
            MIPSerr_maxV_MatchingTiles[s][l][c]=1000.0;
            gainError=50.0;
        }
        MIPMatchingTileserrgain_mV[s][l][c]=gainError;
    }

   
    private double getMIPsignal(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPS_pC_all[s][l][c];
        else
            return MIPS_maxV_all[s][l][c];
    }

    private double getMIPMatchingTilessignal(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPS_pC_MatchingTiles[s][l][c];
        else
            return MIPS_maxV_MatchingTiles[s][l][c];
    }
    
    private double getMIPsignalError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPSerr_pC_all[s][l][c];
        else
            return MIPSerr_maxV_all[s][l][c];
    }
    
    private double getMIPMatchingTilessignalError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPSerr_pC_MatchingTiles[s][l][c];
        else
            return MIPSerr_maxV_MatchingTiles[s][l][c];
    }
    
    private double getMIPNPE(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPgain[s][l][c];
        else
            return MIPgain_mV[s][l][c];
    }
    
    private double getMIPMatchingNPE(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPMatchingTilesgain[s][l][c];
        else
            return MIPMatchingTilesgain_mV[s][l][c];
    }
    
    private double getMIPNPEError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPerrgain[s][l][c];
        else
            return MIPerrgain_mV[s][l][c];
    }
    private double getMIPMatchingNPEError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return MIPMatchingTileserrgain[s][l][c];
        else
            return MIPMatchingTileserrgain_mV[s][l][c];
    }
    
    private double getE(int s, int l, int c) {
        return histogramsFTHodo.nMipE[l];
    }
    private double getQMean(int s, int l, int c) {

        double qMean = 0.0;

        if (histogramsFTHodo.fQMIP.hasEntry(s, l, c)) {
            qMean = histogramsFTHodo.fQMIP.get(s, l, c).getParameter(1);
        }

        return qMean;

    }

    private double getVMean(int s, int l, int c) {
        double vMean = 0.0;
        if (histogramsFTHodo.fVMIP.hasEntry(s, l, c)) {
            vMean = histogramsFTHodo.fVMIP.get(s, l, c).getParameter(1);
        }
        return vMean;
    }

    private double getQMeanError(int s, int l, int c) {
        double qMeanError = 0.0;
        if (histogramsFTHodo.fQMIP.hasEntry(s, l, c)) {
            qMeanError = histogramsFTHodo.fQMIP.get(s, l, c).parameter(1).error();
        }
        return qMeanError;
    }

    private double getTMean(int s, int l, int c) {

        double tMean = 0.0;

        if (histogramsFTHodo.fT.hasEntry(s, l, c)) {
            tMean = histogramsFTHodo.fT.get(s, l, c).getParameter(1);
        }

        return tMean;

    }

    private double getTSigma(int s, int l, int c) {
        double tSigma = 0.0;
        if (histogramsFTHodo.fT.hasEntry(s, l, c)) {
            tSigma = histogramsFTHodo.fT.get(s, l, c).getParameter(2);
        }
        return tSigma;
    }

    private double getVMeanError(int s, int l, int c) {
        double vMeanError = 0.0;
        if (histogramsFTHodo.fVMIP.hasEntry(s, l, c)) {
            vMeanError = histogramsFTHodo.fVMIP.get(s, l, c).parameter(1).error();
        }
        return vMeanError;
    }


    public void updateArrays() {
        System.out.println(" Setting arrays");
        for (int s = 1; s < 9; s++) {
            for (int l = 1; l < 3; l++) {
                for (int c = 1; c < 21; c++) {
                    if (s % 2 == 1 && c > 9) {
                        continue;
                    }
                    setPedestal(s, l, c);
                    setGainAll(s, l, c);
                    setMIPAll(s, l, c);
                    //HERE for the status
                    //setStatus(s, l, c);
                }
            }
        }
        System.out.println(" Arrays Set");
    }

    public void updateTable() {
        int s,l,c;
        for (int index = 0; index < 232; index++) {
            histogramsFTHodo.HP.setAllParameters(index, 'h');
            s=histogramsFTHodo.HP.getS();
            l=histogramsFTHodo.HP.getL();
            c=histogramsFTHodo.HP.getC();
            ConstantsTable.setDoubleValue(getPedMean(s, l, c),"ped",s, l, c);
            ConstantsTable.setDoubleValue(getGain(s, l, c,"charge"),"gain_pc",s, l, c);
            ConstantsTable.setDoubleValue(getGain(s, l, c,"peakVolt"),"gain_mv",s, l, c);
            ConstantsTable.setDoubleValue(getMIPsignal(s, l, c,"charge"),"MIPS_pC_all",s, l, c);
            ConstantsTable.setDoubleValue(getMIPsignal(s, l, c,"peakVolt"),"MIPS_maxV_all",s, l, c);
            ConstantsTable.setDoubleValue(getMIPMatchingTilessignal(s, l, c,"charge"),"MIPS_pC_MatchingTiles",s, l, c);
            ConstantsTable.setDoubleValue(getMIPMatchingTilessignal(s, l, c,"peackVolt"),"MIPS_maxV_MatchingTiles",s, l, c);
            ConstantsTable.setDoubleValue(getMIPNPE(s, l, c,"charge"),"NPE_pC_all",s, l, c);
            ConstantsTable.setDoubleValue(getMIPNPE(s, l, c,"peakvolt"),"NPE_maxV_all",s, l, c);
            ConstantsTable.setDoubleValue(getMIPMatchingNPE(s, l, c,"charge"),"NPE_pC_MatchingTiles",s, l, c);
            ConstantsTable.setDoubleValue(getMIPMatchingNPE(s, l, c,"peakvolt"),"NPE_pC_MatchingTiles",s, l, c);
            
            CCDBTableNoise.setDoubleValue(getPedMean(s, l, c),"pedestal",s, l, c);
            CCDBTableNoise.setDoubleValue(getPedRMS(s, l, c),"pedestal_rms",s, l, c);
            CCDBTableNoise.setDoubleValue(getGain(s, l, c,"charge"),"gain_pc",s, l, c);
            CCDBTableNoise.setDoubleValue(getGain(s, l, c,"peakVolt"),"gain_mv",s, l, c);
            CCDBTableNoise.setDoubleValue(2.5,"npe_threshold",s, l, c);

            CCDBTableCharge2Energy.setDoubleValue(getMIPsignal(s, l, c, "charge"),"mips_charge",s, l, c);
            CCDBTableCharge2EnergyMatchingTiles.setDoubleValue(getMIPMatchingTilessignal(s, l, c, "charge"),"mips_charge",s, l, c);

            if (l%2!=0){
                CCDBTableCharge2Energy.setDoubleValue(1.2,"mips_energy",s, l, c);
                CCDBTableCharge2EnergyMatchingTiles.setDoubleValue(1.2,"mips_energy",s, l, c);
            }
            else{
                CCDBTableCharge2Energy.setDoubleValue(2.65,"mips_energy",s, l, c);
                CCDBTableCharge2EnergyMatchingTiles.setDoubleValue(2.65,"mips_energy",s, l, c);
            }
            
            int craten=InverseTranslationTable.getIntValue("crate", s,l,c);//craten=72;
            int slotn=InverseTranslationTable.getIntValue("slot", s,l,c);//wireFTHodo.getSlot4SLC(s, l, c);
            int channeln=InverseTranslationTable.getIntValue("chan", s,l,c);//wireFTHodo.getChan4SLC(s, l, c);
            CCDBTablefACD.setDoubleValue(getPedMean(s, l, c),"pedestal",craten, slotn, channeln);
            double tetValD=getGain(s, l, c,"peakvolt")*histogramsFTHodo.nThrshNPE/histogramsFTHodo.LSB;
            int tetVal=(int) tetValD;
            CCDBTablefACD.setIntValue(tetVal,"tet",craten, slotn, channeln);

            GAINandMIPSTableOverview.setDoubleValue(getGain(s, l, c,"charge"),"gain_pc",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getGain(s, l, c,"peakvolt"),"gain_mv",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPsignal(s, l, c,"charge"),"mips_charge",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPsignal(s, l, c,"peakvolt"),"mips_voltage",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPMatchingTilessignal(s, l, c,"charge"),"mips_charge_MatchingTiles",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPMatchingTilessignal(s, l, c,"peakvolt"),"mips_voltage_MatchingTiles",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getGainError(s, l, c,"charge"),"gainerr_pc",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getGainError(s, l, c,"peakvolt"),"gainerr_mv",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPsignalError(s, l, c,"charge"),"mipserr_charge",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPsignalError(s, l, c,"peakvolt"),"mipserr_voltage",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPMatchingTilessignalError(s, l, c,"charge"),"mipserr_charge_MatchingTiles",s, l, c);
            GAINandMIPSTableOverview.setDoubleValue(getMIPMatchingTilessignalError(s, l, c,"peakvolt"),"mipserr_voltage_MatchingTiles",s, l, c);

            ConstantsTable.fireTableDataChanged();
            if (histogramsFTHodo.testMode) {
               ConstantsTable.show();
            }
            this.canvasPane.repaint();
        }
    }
    
    
    
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        tabSel = sourceTabbedPane.getSelectedIndex();
       
        if ((this.previousTabSel == this.tabIndexMIPsignal && tabSel != this.tabIndexMIPsignal)) {
            this.canvasPane.remove(rBPaneMIP);
        }
        if ((this.previousTabSel == this.tabIndexMIPAnalysis && tabSel != this.tabIndexMIPAnalysis)) {
            this.canvasPane.remove(rBPaneMIPgain);
        }
        if ((this.previousTabSel == this.tabIndexNoiseAnalysis && tabSel != this.tabIndexNoiseAnalysis)){
            this.canvasPane.remove(rBPaneGain);
        }
        if (tabSel == this.tabIndexNoiseAnalysis) {
            this.canvasPane.add(rBPaneGain, BorderLayout.NORTH);
        }
        if (tabSel ==  this.tabIndexMIPAnalysis ){
            this.canvasPane.add(rBPaneMIPgain, BorderLayout.NORTH);
        }
        if (tabSel == this.tabIndexMIPsignal  ) {
            this.canvasPane.add(rBPaneMIP, BorderLayout.NORTH);
        }

        if (tabSel == this.tabIndexEvent) {
            drawCanvasEvent(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexPed) {
            drawCanvasPed(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexNoise) {
            drawCanvasNoise(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexNoiseAnalysis) {
            if (drawByElec == false) {
                drawCanvasNoiseAnalysis();
            } else {
                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
            }
        }else if (tabSel == this.tabIndexMIPsignal){
            drawCanvasMIPsignal(secSel,laySel,comSel);
        }else if (tabSel == this.tabIndexMIPAnalysis){
            if (drawByElec == false) {
                drawCanvasMIPAnalysis();
            } else {
                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
            }
        }else if (tabSel == this.tabIndexTime){
            drawCanvasTime(secSel,laySel,comSel);
        }
        
        System.out.println("Tab changed to: "+ sourceTabbedPane.getTitleAt(tabSel)+ " with index " + tabSel);
        previousTabSel = tabSel;
        this.detectorView.repaint();
        this.canvasPane.repaint();
    }

    public void initHistograms() {
        histogramsFTHodo.InitHistograms();
    }

    public void resetHistograms() {
        histogramsFTHodo.InitHistograms();
    }

    public void initArrays() {
        status = new double[9][3][21];
        thrshNPE = new double[9][3][21];
        pedMean = new double[9][3][21];
        pedValues4SlotChan= new double[20][16];
        pedEvent = new double[9][3][21];
        pedRMS = new double[9][3][21];
        pedPreviousEvent = new double[9][3][21];
        vMax = new double[9][3][21];
        vMaxEvent = new double[9][3][21];
        qMax = new double[9][3][21];
        meanNPE = new double[9][3][21];
        errNPE = new double[9][3][21];
        sigNPE = new double[9][3][21];
        gain = new double[9][3][21];
        errGain = new double[9][3][21];
        meanNPE_mV = new double[9][3][21];
        errNPE_mV = new double[9][3][21];
        gain_mV = new double[9][3][21];
        errGain_mV = new double[9][3][21];
        
        MIPgain = new double[9][3][21];
        MIPerrgain = new double[9][3][21];
        MIPgain_mV = new double[9][3][21];
        MIPerrgain_mV = new double[9][3][21];
        
        MIPS_pC_all = new double[9][3][21];
        MIPS_pC_MatchingTiles = new double[9][3][21];
        MIPS_maxV_all = new double[9][3][21];
        MIPS_maxV_MatchingTiles = new double[9][3][21];

        MIPSerr_pC_all = new double[9][3][21];
        MIPSerr_pC_MatchingTiles = new double[9][3][21];
        MIPSerr_maxV_all = new double[9][3][21];
        MIPSerr_maxV_MatchingTiles = new double[9][3][21];
        
        MIPMatchingTilesgain = new double[9][3][21];
        MIPMatchingTileserrgain = new double[9][3][21];
        MIPMatchingTilesgain_mV = new double[9][3][21];
        MIPMatchingTileserrgain_mV = new double[9][3][21];
        
        npeEvent = new double[9][3][21];
        time_M3 = new double[9][3][21];
        time_M7 = new double[9][3][21];
        dT_M3 = new double[9][21];
        dT_M7 = new double[9][21];
        for (int s = 0; s < 9; s++) {
            for (int l = 0; l < 3; l++) {
                for (int c = 0; c < 21; c++) {
                    this.status[s][l][c] = histogramsFTHodo.nStatus;
                    this.thrshNPE[s][l][c] = histogramsFTHodo.nThrshNPE;
                    this.pedMean[s][l][c] = histogramsFTHodo.nPedMean;
                    
                    this.pedEvent[s][l][c] = 0;
                    this.pedPreviousEvent[s][l][c] = 0;
                    this.pedRMS[s][l][c] = histogramsFTHodo.nPedRMS;
                    this.gain[s][l][c] = histogramsFTHodo.nGain;
                    this.errGain[s][l][c] = histogramsFTHodo.nErrGain;
                    this.gain_mV[s][l][c] = histogramsFTHodo.nGain_mV;
                    this.errGain_mV[s][l][c] = histogramsFTHodo.nErrGain_mV;
                    
                    this.MIPgain[s][l][c] = 75.0;
                    this.MIPerrgain[s][l][c] = 25.0;
                    this.MIPgain_mV[s][l][c] = 75.0;
                    this.MIPerrgain_mV[s][l][c] = 25.0;

                    this.MIPMatchingTilesgain[s][l][c] = 75.0;
                    this.MIPMatchingTileserrgain[s][l][c] = 25.0;
                    this.MIPMatchingTilesgain_mV[s][l][c] = 75.0;
                    this.MIPMatchingTileserrgain_mV[s][l][c] = 25.0;
                    
                    this.MIPS_pC_all[s][l][c] = 0.0;
                    this.MIPS_pC_MatchingTiles[s][l][c] = 0.0;
                    this.MIPS_maxV_all[s][l][c] = 0.0;
                    this.MIPS_maxV_MatchingTiles[s][l][c] = 0.0;
                    this.MIPSerr_pC_all[s][l][c] = 0.0;
                    this.MIPSerr_pC_MatchingTiles[s][l][c] = 0.0;
                    this.MIPSerr_maxV_all[s][l][c] = 0.0;
                    this.MIPSerr_maxV_MatchingTiles[s][l][c] = 0.0;
                    
                    this.meanNPE[s][l][c] = 0.0;
                    this.errNPE[s][l][c] = 0.0;
                    this.sigNPE[s][l][c] = 100.0;
                    this.meanNPE_mV[s][l][c] = 0.0;
                    this.errNPE_mV[s][l][c] = 0.0;
                    this.vMax[s][l][c] = 0.0;
                    this.vMaxEvent[s][l][c] = 0.0;
                    this.qMax[s][l][c] = 0.0;
                    this.npeEvent[s][l][c] = 0.0;
                } // end: for ( int c = 0 ; c...
            } // end: for (int l = 0; l < 3; l...
        } // end: for (int s = 0; s...
        
        for (int s = 0; s < 20; s++) {
            for (int c = 0; c < 16; c++) {
                this.pedValues4SlotChan[s][c]=500.0;
            }
        }
    }// end: public void initArra....
    
    public void processDecodedSimEvent(DetectorCollection<Double> adc, DetectorCollection<Double> tdc) {
        boolean calChalData = true;
        double[] time_tdc = {-9.9, -99.9, -999.9};
        double[] time = {-9.9, -99.9, -999.9};
        double time2Hodo = 6.0;
        double time2Tile[] = {-99.0, 6.0, 6.0};
        double startTime = 0.0;
        double timeOrderFactor = 1.;
        if (calChalData) {
            //startTime = 124.25;
            timeOrderFactor = 100.;
        }
        boolean[] goodTime = {false, false, false};
        boolean[] veryGoodTime = {false, false, false};
        boolean goodDT;
        double tCut = 5.0;
        double tRange = 100.0;
        double dtCut = 5.0;
        boolean applyTCuts = false;
        boolean[] applyTCut = {false, applyTCuts, applyTCuts};
        boolean applyDTCut = false;
        boolean applyNoCuts = true;
        if (applyNoCuts) {
            for (int i = 0; i < 3; i++) {
                applyTCut[i] = false;
            }
            applyDTCut = false;
        }
        if ((applyTCut[1]
                || applyTCut[2]
                || applyDTCut)
                && applyNoCuts) {
            System.out.println(" conflict in cut logic ");
        }
        double[] charge = {0.0, 0.0, 0.0};
        double[] peakVolt = {0.0, 0.0, 0.0};
        double deltaT;
        Double x, y, z, w, d;
        Double u[] = {0., 0., 0.};
        double xyz[] = {0., 0., 0.};
        for (int s = 1; s < 9; s++) {
            for (int c = 1; c < 21; c++) {
                if (s % 2 == 1 && c > 9) {
                    continue;
                }
                deltaT = -999.9;
                goodDT = false;
                time[1] = -199.9;
                time[2] = -991.9;
                time_tdc[1] = -99.9;
                time_tdc[2] = -999.9;
                time2Tile[1] = -9.9;
                time2Tile[2] = -9999.9;
                for (int l = 1; l < 3; l++) {
                    //System.out.println(" xyzwd[0] = " + xyzwd[0]);
                    x = geometryTable.getDoubleValue("x", s, l, c);
                    y = geometryTable.getDoubleValue("y", s, l, c);
                    z = geometryTable.getDoubleValue("z", s, l, c);
                    w = geometryTable.getDoubleValue("width", s, l, c);
                    d = geometryTable.getDoubleValue("thickness", s, l, c);
                    xyz[0] = x;
                    xyz[1] = y;
                    xyz[2] = z;
                    u[l] = 0.;
                    u[l] = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
                    // convert to cm
                    u[l] = u[l] / 10.;
                    peakVolt[l] = 0.0;
                    charge[l] = 0.0;
                    goodTime[l] = false;
                    veryGoodTime[l] = false;
                    if (tdc.hasEntry(s, l, c)) {
                        time_tdc[l] = tdc.get(s, l, c) / timeOrderFactor;
                        // time in ns = (u in cm) / (c in cm/ns)
                        // should be around zero
                        time2Tile[l] = u[l] / 30.0 + startTime;
                        if (debugging_mode) {
                            for (int i = 0; i < 3; i++) {
                                System.out.println(" xyz[" + i + "] = "
                                        + xyz[i]);
                            }
                            System.out.println(" u[" + l + "]/30.(s,l,c) = "
                                    + u[l] / 30. + "(" + s + ","
                                    + l + "," + c + ")");
                            System.out.println(" time_tdc[" + l
                                    + "] = " + time_tdc[l]);
                            System.out.println(" time2Tile[" + l
                                    + "] = " + time2Tile[l]);
                        }
                        time[l] = time_tdc[l] - time2Tile[l];
                        if (debugging_mode) {
                            System.out.println(" -----------------------");
                            System.out.println(" time[" + l + "] = "
                                    + time[l]);
                            System.out.println(" -----------------------");
                        }
                        if (abs(time[l]) < tRange) {
                            goodTime[l] = true;
                        }
                        if (abs(time[l]) < tCut) {
                            veryGoodTime[l] = true;
                        }
                    }
                    if (adc.hasEntry(s, l, c)) {
                        charge[l] = adc.get(s, l, c);
                        peakVolt[l] = charge[l] / 2;
                        if (charge[l] > qMax[s][l][c]) {
                            qMax[s][l][c] = charge[l];
                        }
                        // cut conditions
                        if ((applyTCut[l]&& veryGoodTime[l]) || (applyDTCut&& goodDT) || (applyNoCuts)) {
                            histogramsFTHodo.H_MIP_Q.get(s, l, c).fill(charge[l]);
                            histogramsFTHodo.H_NOISE_Q.get(s, l, c).fill(charge[l]);
                            histogramsFTHodo.H_MIP_V.get(s, l, c).fill(peakVolt[l]);
                            histogramsFTHodo.H_NOISE_V.get(s, l, c).fill(peakVolt[l]);
                        } // end of cut conditions
                        histogramsFTHodo.H_MAXV_VS_T.get(s, l, c)
                                .fill(time[l], peakVolt[l]);
                    } // end of: if (adc.hasEntry(s,l,c)) {....
                    if (debugging_mode
                            && l == 2
                            && charge[2] > 0.0) {
                        System.out.println(" -----------------------");
                        System.out.println(" time diff. stuff ");
                        for (int i = 1; i < 3; i++) {
                            System.out.println(" goodTime[" + i
                                    + "] = " + goodTime[i]);
                            System.out.println(" charge[" + i
                                    + "] = " + charge[i]);
                        }
                    }
                    // timing stuff
                    if (l == 2
                            && goodTime[1]
                            && goodTime[2]
                            && (charge[1] > 500.0)
                            && (charge[2] > 1000.0)) {

                        deltaT = time[2] - time[1];
                        if (abs(deltaT) < dtCut) {
                            goodDT = true;
                        }
                        if (goodDT) {
                            histogramsFTHodo.H_T_MODE7.get(s, 1, c).fill(time[1]);
                            histogramsFTHodo.H_T_MODE7.get(s, 2, c).fill(time[2]);
                        }
                        if (debugging_mode) {
                            System.out.println(" -------------------- ");
                            for (int i = 0; i < 3; i++) {
                                System.out.println(" xyz[" + i + "] = "
                                        + xyz[i]);
                            }
                            System.out.println(" u[1]/30.(s,l,c) = "
                                    + u[1] / 30. + "(" + s + ",1,"
                                    + c + ")");
                            System.out.println(" u[2]/30.(s,l,c) = "
                                    + u[2] / 30. + "(" + s + ",2,"
                                    + c + ")");
                            System.out.println(" time_tdc[1] = " + time_tdc[1]);
                            System.out.println(" time_tdc[2] = " + time_tdc[2]);
                            System.out.println(" time2Tile[1] = " + time2Tile[1]);
                            System.out.println(" time2Tile[2] = " + time2Tile[2]);
                            System.out.println(" time[1] = " + time[1]);
                            System.out.println(" time[2] = " + time[2]);
                            System.out.println(" deltaT  = " + deltaT);
                            System.out.println(" -------------------- ");
                        }
                    }// end of time difference stuff

                    // 		    if(charge > 500)
// 			System.out.println(" charge (s,l,c) = " + 
// 					   charge               +
// 					   "(" + s + "," + l    +
// 					   "," + c + ")");
                } // end of: for (int l = 1 ; l < 3 ; l++)
            }
        }

//        if (tabSel == tabIndexCharge) {
//            drawCanvasCharge(secSel, laySel, comSel);
//        } else if (tabSel == tabIndexTime) {
//            drawCanvasTime(secSel, laySel, comSel);
//        }
    }

    public void dataEvioEventAction(DataEvent event) {
        nDecodedProcessed++;
        if (event instanceof EvioDataEvent) {
            try {
                List<DetectorDataDgtz> dataList = decoder.getDataEntries((EvioDataEvent) event);
                this.setRunNumber(this.getDecoder().getRunNumber());
                detectorDecoder.translate(dataList);
                detectorDecoder.fitPulses(dataList);
                //   System.out.println(dataList.size());
                //    System.out.println("event #: " + nProcessed);
                //     List<DetectorCounter> counters = decoder.getDetectorCounters(DetectorType.FTCAL);
                List<DetectorDataDgtz> counters = new ArrayList<DetectorDataDgtz>();
                for (DetectorDataDgtz entry : dataList) {
                    if (entry.getDescriptor().getType() == DetectorType.FTHODO) {
                        if (entry.getADCSize() > 0) {
                            counters.add(entry);
                        }
                    }
                }
                int nPosADC;
                int nNegADC;
                histogramsFTHodo.H_W_MAX.reset();
                histogramsFTHodo.H_V_MAX.reset();
                histogramsFTHodo.H_NPE_MAX.reset();
                int nTilesAboveThresholdLayer1=0;
                int nTilesAboveThresholdLayer2=0;
                double threshDV = (double) histogramsFTHodo.matchingTilesThreshold * histogramsFTHodo.LSB;

                //=-=-=-=-=-=-=-=-=-=-=-=-=-
                // Loop One
                double previousChargePed = 0.0;
                for (DetectorDataDgtz counter : counters) {
                    if (counter.getDescriptor().getType() != (DetectorType.FTHODO)) {
                        break;
                    }
                    int sec = counter.getDescriptor().getSector();
                    int lay = counter.getDescriptor().getLayer();
                    int com = counter.getDescriptor().getComponent();

                    int maxPedbin = 0;
                    double avePed = 0.0;
                    double nEventsAvePed = 0;
                    int index = getIndex4SLC(sec, lay, com);
                    
                    histogramsFTHodo.threshold=this.detectorDecoder.getFadcPanel().tet;
                    histogramsFTHodo.initThresholdParameters(sec, lay, com);
                    //System.out.println("Threshold:"+this.detectorDecoder.getFadcPanel().tet);
                    
                    short pulse[] = counter.getADCData(0).getPulseArray();
                    int ped_i1=counter.getADCData(0).getPedistalMinBin();
                    int ped_i2=counter.getADCData(0).getPedistalMaxBin();
                    int pul_i1=counter.getADCData(0).getPulseMinBin();
                    int pul_i2=counter.getADCData(0).getPulseMaxBin();
                    double npeWave;
                    double calibratedWave;
                    double baselineSubRaw;
                    int eventloop;
                    // reset non-accumulating histograms
                    histogramsFTHodo.H_FADC.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW_PED.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW_PUL.get(sec, lay, com).reset();
                    //histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).reset();
                    histogramsFTHodo.H_VT.get(sec, lay, com).reset();
                    histogramsFTHodo.H_NPE.get(sec, lay, com).reset();
                    
                    
                    //===============================
                    // npe for this event only
                    // to be used in loop two below
                    npeEvent[sec][lay][com] = 0.0;
                    if (gain[sec][lay][com] > 15.0) {
                        npeEvent[sec][lay][com] = counter.getADCData(0).getADC() * histogramsFTHodo.LSB * 4.0 / 50. / gain[sec][lay][com];
                        histogramsFTHodo.H_NPE_INT.get(sec, lay, com).fill(npeEvent[sec][lay][com]);
                    }
                    double compEvntPed = counter.getADCData(0).getPedestal();
                    pedPreviousEvent[sec][lay][com]=pedEvent[sec][lay][com];
                    pedEvent[sec][lay][com]=compEvntPed;

                    histogramsFTHodo.H_PED.get(sec, lay, com).fill(compEvntPed);
                    // Maybe this can be of number of events in histogram instead.. 
                    // however all histograms should have ped for every event
                    //                    if (abs(counter.getADCData(0).getPedestal()
                    //                            - pedMean[sec][lay][com]) > 5.
                    //                            && pedMeanGood) {
                    //                        compEvntPed = pedMean[sec][lay][com];
                    //                    }
                    if (nDecodedProcessed / 100 < histogramsFTHodo.nPointsPed) {
                        eventloop = nDecodedProcessed;
                    } else {
                        eventloop = nDecodedProcessed - nDecodedProcessed
                                / (histogramsFTHodo.nPointsPed * 100) * histogramsFTHodo.nPointsPed * 100;
                    }
                    if (eventloop % 100 != 0) {
                        // Fills a histogram for pedestal by averaging 
                        // out a number of bins at a hardcoded position
                        //H_PED_TEMP.get(sec,lay,com).fill(fadcFitter.getPedestal()); 
                        histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).fill(compEvntPed);
                    } 
                    else {
                        //System.out.println("Nick: " + eventloop/100);
                        //Finds maximum-content bin of pedestal histogram
                        maxPedbin = histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getMaximumBin();
                        avePed = 0.0;
                        nEventsAvePed = 0;
                        // Calculates most prob pedestal value by taking 
                        // +/-5 bins from maximum-content bin
                        for (int i = 0; i < 5; i++) {
                            avePed = avePed + histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getAxis().getBinWidth(2)
                                    * (maxPedbin + i)
                                    * histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getBinContent(maxPedbin + i)
                                    + histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getAxis().getBinWidth(2) * (maxPedbin - i)
                                    * histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getBinContent(maxPedbin - i);
                            nEventsAvePed = nEventsAvePed
                                    + histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getBinContent(maxPedbin + i)
                                    + histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getBinContent(maxPedbin - i);
                        }
                        //calculates average and corrects for offset of histogram
                        avePed = avePed / nEventsAvePed + histogramsFTHodo.PedQX[0];
                        histogramsFTHodo.py_H_PED_VS_EVENT[sec - 1][lay - 1][com - 1][eventloop / 100] = avePed;
                        histogramsFTHodo.H_PED_VS_EVENT.
                                get(sec, lay, com).setPoint(eventloop/100, eventloop/100, avePed);      
                        histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).reset();
                    }
                    
                    if (abs(counter.getADCData(0).getPedestal()
                            - pedMean[sec][lay][com]) > 5.
                            && pedMeanGood) {
                        compEvntPed = pedMean[sec][lay][com];
                    }
                    //                    histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(ped_i1+1.5,counter.getADCData(0).getPedestal(),0,0);
                    //                    histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(ped_i2+0.5,counter.getADCData(0).getPedestal(),0,0);
                    //                    histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(pul_i1+0.5,counter.getADCData(0).getPulseValue(pul_i1),0,0);
                    //                    histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(pul_i2+0.5,counter.getADCData(0).getPulseValue(pul_i2),0,0);
                    //                    if(counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal()>this.detectorDecoder.getFadcPanel().tet){
                    //                        histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(counter.getADCData(0).getThresholdCrossing()+0.5,counter.getADCData(0).
                    //                              getPulseValue(counter.getADCData(0).getThresholdCrossing()),0,0);
                    //                        histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(counter.getADCData(0).getTime()/histogramsFTHodo.nsPerSample +0.5,
                    //                                                                                    (counter.getADCData(0).getHeight()+ counter.getADCData(0).getPedestal())/2,0,0);
                    //                        histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).addPoint(counter.getADCData(0).getPosition()+0.5,counter.getADCData(0).getHeight(),0,0);
                    //                    }
                    
                    // Loop through fADC bins filling event-by-event histograms
                    for (int i = 0;i < min(pulse.length, histogramsFTHodo.H_FADC.get(sec, lay, com).getAxis().getNBins()); i++) {
                        if (i == 100) {
                            System.out.println(" pulse[" + i + "] = " + pulse[i]);
                        }
                        histogramsFTHodo.H_FADC_RAW.get(sec, lay, com).fill(i, pulse[i]);
                        if (i>ped_i1 &&i<=ped_i2)
                            histogramsFTHodo.H_FADC_RAW_PED.get(sec, lay, com).fill(i, pulse[i]);
                        if (i>pul_i1 &&i<=pul_i2)
                            histogramsFTHodo.H_FADC_RAW_PUL.get(sec, lay, com).fill(i, pulse[i]);
                        
                        // Baseline unsubtracted
                        baselineSubRaw = pulse[i] - compEvntPed + histogramsFTHodo.PedOffset;
                        histogramsFTHodo.H_FADC.get(sec, lay, com).fill(i, baselineSubRaw);
                        calibratedWave = (pulse[i] - compEvntPed) * histogramsFTHodo.LSB +  histogramsFTHodo.vPedOffset;
                        histogramsFTHodo.H_VT.get(sec, lay, com).fill(i * 4, calibratedWave);
                        npeWave = (pulse[i] - compEvntPed) * histogramsFTHodo.LSB / histogramsFTHodo.voltsPerSPE;
                        histogramsFTHodo.H_NPE.get(sec, lay, com).fill(i * 4, npeWave);
                    }
                    double waveMax = 0.;
                    waveMax = -compEvntPed;
                    waveMax = waveMax + counter.getADCData(0).getHeight();
                    vMaxEvent[sec][lay][com] = waveMax * histogramsFTHodo.LSB;
                    if (vMaxEvent[sec][lay][com] > threshDV){
                        if (lay==1)
                            nTilesAboveThresholdLayer1++;
                        else if (lay==2)
                            nTilesAboveThresholdLayer2++;
                    }
                }

                //=-=-=-=-=-=-=-=-=-=-=-=-=-
                // Loop Two
                int matchedTile=-1;

                for (DetectorDataDgtz counter : counters) {
                    int sec = counter.getDescriptor().getSector();
                    int lay = counter.getDescriptor().getLayer();
                    int com = counter.getDescriptor().getComponent();
                    int opp = (lay % 2) + 1;
                    int index;
                    if (counter.getDescriptor().getType() == DetectorType.FTHODO) {
                        index = getIndex4SLC(sec, lay, com);
                    } else {
                        index = com;
                        sec = 0;
                        lay = 0;
                    }
                    // Fill Charge Histograms
                    histogramsFTHodo.H_MIP_Q.get(sec, lay, com).fill(counter.getADCData(0).getADC() * histogramsFTHodo.LSB * histogramsFTHodo.nsPerSample / 50);
                    if (counter.getADCData(0).getPosition()>15 && counter.getADCData(0).getPosition()<60)
                        histogramsFTHodo.H_NOISE_Q.get(sec, lay, com).fill(counter.getADCData(0).getADC() * histogramsFTHodo.LSB * histogramsFTHodo.nsPerSample / 50);
                    
                    double waveMax = 0.;
                    double compEvntPed = counter.getADCData(0).getPedestal();
                    // first use of pedestal (in second loop)
                    if (abs(counter.getADCData(0).getPedestal()- pedMean[sec][lay][com]) > 5.&& pedMeanGood) {
                        compEvntPed = pedMean[sec][lay][com];
                    }
                    waveMax = -compEvntPed;
                    waveMax = waveMax + counter.getADCData(0).getHeight();
                    double voltMax = waveMax * histogramsFTHodo.LSB;
                    double npeMax = 0.;
                    npeMax = voltMax / gain_mV[sec][lay][com];
                    // Matching Hits in layers
                    if (gain[sec][lay][com] > 15.0
                            && gain[sec][opp][com] > 15.0) {
                        double npeOtherLayer = npeEvent[sec][opp][com];
                        double meanNPEOther = meanNPE[sec][opp][com];
                        double sigNPEOther = sigNPE[sec][opp][com];
                        double npeLowLimOther = meanNPEOther - abs(sigNPEOther);
                        //if ( npeLowLimOther < 3.0 )
                        //npeLowLimOther = 3.0;
                        //if ( npeOtherLayer > npeLowLimOther )
                        //double threshDV = (double) histogramsFTHodo.threshold * histogramsFTHodo.LSB;
                        //if (vMaxEvent[sec][lay][com] > threshDV && vMaxEvent[sec][opp][com] > threshDV && (matched_sec<0 || (matched_sec==sec && matched_comp==com))) {
                        if (vMaxEvent[sec][lay][com] > threshDV && vMaxEvent[sec][opp][com] > threshDV ) {
                            boolean toFill=false;
                            if (histogramsFTHodo.SingleMatchedTile && nTilesAboveThresholdLayer1==1 && nTilesAboveThresholdLayer2==1){
                                toFill=true;
                            }
                            else if (!histogramsFTHodo.SingleMatchedTile){
                                toFill=true;
                            }
                            if (toFill){
                                histogramsFTHodo.H_MIP_V_MatchingTiles.get(sec, lay, com).fill(vMaxEvent[sec][lay][com]);
                                double intcharge=counter.getADCData(0).getADC() * histogramsFTHodo.LSB * histogramsFTHodo.nsPerSample / 50;
                                histogramsFTHodo.H_MIP_Q_MatchingTiles.get(sec, lay, com).fill(intcharge);
                                if (histogramsFTHodo.testMode){
                                    System.out.println(" vMaxEvent["+sec+"]["+lay+"]["+com+"] =" + vMaxEvent[sec][lay][com] + " voltmax: "+ voltMax);
                                    System.out.println(" vMaxEvent["+sec+"]["+opp+"]["+com+"] =" + vMaxEvent[sec][opp][com]);
                                    System.out.println(" threshDV                             =" + threshDV);
                                }
                            }

                            if (npeEvent[sec][lay][com] > 0.0) {
                                histogramsFTHodo.H_NPE_MATCH.get(sec, lay, com).fill(npeEvent[sec][lay][com]);
                            }
                        }
                    }
                    histogramsFTHodo.H_W_MAX.fill(index, waveMax);
                    histogramsFTHodo.H_V_MAX.fill(index, voltMax);
                    histogramsFTHodo.H_NPE_MAX.fill(index, npeMax);
                    histogramsFTHodo.H_NOISE_V.get(sec, lay, com).fill(voltMax);
                    if (lay > 0 && voltMax > vMax[sec][lay][com]) {
                        vMax[sec][lay][com] = voltMax;
                    }
                    if (lay > 0&& counter.getADCData(0).getTimeCourse() > 10) {
                        time_M3[sec][lay][com] = counter.getADCData(0).getTimeCourse();
                        time_M7[sec][lay][com] = counter.getADCData(0).getTime();
                        histogramsFTHodo.H_T_MODE3.get(sec,lay,com).fill(time_M3[sec][lay][com]);
                        histogramsFTHodo.H_MAXV_VS_T.get(sec,lay,com).fill(time_M7[sec][lay][com],voltMax);
                    }
                    histogramsFTHodo.H_MIP_V.get(sec, lay, com).fill(voltMax);
                } // end of: for (DetectorCounter counter : counters) {

                for (int sec = 1; sec < 9; sec++) {
                    for (int com = 1; com < 21; com++) {
                        // odd sectors have only 9 elements
                        if ((sec) % 2 == 1 && com > 9) {
                            continue;
                        }
                        if (time_M3[sec][2][com] > 0 && time_M3[sec][1][com] > 0) {
                            dT_M3[sec][com] = -time_M3[sec][2][com];
                            dT_M3[sec][com] += time_M3[sec][1][com];
                            histogramsFTHodo.H_T_MODE3.get(sec, 1, com).fill(dT_M3[sec][com]);
                            if (time_M7[sec][2][com] > 0&& time_M7[sec][1][com] > 0) {
                                dT_M7[sec][com] = -time_M7[sec][2][com];
                                dT_M7[sec][com] += time_M7[sec][1][com];
                                if (vMaxEvent[sec][1][com] > 200.&& vMaxEvent[sec][1][com] < 1550.&& vMaxEvent[sec][2][com] > 400.&& vMaxEvent[sec][2][com] < 1550.) {
                                    histogramsFTHodo.H_T_MODE7.get(sec, 1, com).fill(time_M7[sec][1][com]);
                                    histogramsFTHodo.H_T_MODE7.get(sec, 2, com).fill(time_M7[sec][2][com]);
                                    histogramsFTHodo.H_DT_MODE7.get(sec, 1, com).fill(dT_M7[sec][com]);
                                    histogramsFTHodo.H_T1_T2.get(sec, 1, com).fill(time_M7[sec][1][com],time_M7[sec][2][com]);
                                }
                            }
                        }
                    }
                }


                if (laySel == 0) {
                    return;
                }

                //============================================================
                // Event Tab Selected
                double hwmax = histogramsFTHodo.H_W_MAX.getBinContent(indexSel);
                if (tabSel == tabIndexEvent) {
                    this.detectorView.repaint();
                    this.canvasEvent.update();
                }
                if (tabSel == tabIndexPed)
                    this.detectorView.repaint();
                if (tabSel == tabIndexMIPsignal)
                    this.detectorView.repaint();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dataHipoEventAction(HipoDataEvent event) {       
        DetectorCollection<Double> sim_adc = new DetectorCollection<Double>();
        DetectorCollection<Double> sim_tdc = new DetectorCollection<Double>();

        if (event.hasBank("FTHODO::adc")) {
            DataBank adcFTHODO = event.getBank("FTHODO::adc");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            for (int loop = 0; loop < adcFTHODO.rows(); loop++) {
                int    s    = adcFTHODO.getByte("sector", loop);
                int    l    = adcFTHODO.getByte("layer", loop);
                int    c    = adcFTHODO.getInt("component", loop);
                int    adc    = adcFTHODO.getInt("ADC", loop);
                double time   = adcFTHODO.getFloat("time", loop);                
                double ped    = adcFTHODO.getShort("pedestal", loop);                
                double charge =((double) adc)*(histogramsFTHodo.LSB*histogramsFTHodo.nsPerSample/50);
                sim_adc.add(s, l, c, charge);
                sim_tdc.add(s, l, c, time);
            }
        }
        this.processDecodedSimEvent(sim_adc, sim_tdc);
    }
    public void processShape(DetectorShape2D shape) {
        DetectorDescriptor desc = shape.getDescriptor();
        secSel = desc.getSector();
        laySel = desc.getLayer();
        comSel = desc.getComponent();
        indexSel = getIndex4SLC(secSel, laySel, comSel);
        String detector;
        boolean calSel = false;
        boolean hodSelect = false;
        if (desc.getType() == DetectorType.FTHODO) {
            detector = "FTHODO";
            hodSelect = true;
        } else {
            detector = "Unidentified";
            return;
        }
        System.out.println(" Detector Selected is "+ detector);
        System.out.println(" Sector = "+ secSel+ ", Layer = "+ laySel+ ", Component = "+ comSel);
        System.out.println(" Channel = "+ wireFTHodo.getChan4SLC(secSel, laySel, comSel));
        System.out.println(" Mezzanine = "+ wireFTHodo.getMezz4SLC(secSel, laySel, comSel));

        if (tabSel == this.tabIndexEvent) {
            drawCanvasEvent(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexPed) {
            drawCanvasPed(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexNoise) {
            drawCanvasNoise(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexNoiseAnalysis) {
            if (drawByElec == false) {
                drawCanvasNoiseAnalysis();
            } else {
                //this.canvasNoiseAnalysis.divide(1, 1);
                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel);
            }
        }else if (tabSel == this.tabIndexMIPsignal){
                drawCanvasMIPsignal(secSel,laySel,comSel);
        }else if (tabSel == this.tabIndexMIPAnalysis){
            if (drawByElec == false) {
                drawCanvasMIPAnalysis();
            } else {
                drawCanvasMIPAnalysisElec(secSel,laySel,comSel);
            }
        }else if (tabSel == this.tabIndexTime){
                drawCanvasTime(secSel,laySel,comSel);
        }
    }
    public void constantsEvent(CalibrationConstants cc, int i, int i1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void adjustFit() {
        if (this.tabSel==tabIndexPed){
            System.out.println("Adjusting Ped fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
            H1F histtofit =  histogramsFTHodo.H_PED.get(secSel, laySel, comSel);
            F1D ftofit = histogramsFTHodo.fPed.get(secSel, laySel, comSel);
            FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ");
        }
        else if (this.tabSel==tabIndexNoise){
            System.out.println("Adjusting Noise fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
            H1F histtofit1 =  histogramsFTHodo.H_NOISE_Q.get(secSel, laySel, comSel);
            F1D ftofit1 = histogramsFTHodo.fQ2.get(secSel, laySel, comSel);
            FTAdjustFit cfit1 = new FTAdjustFit(histtofit1, ftofit1, "LRQ");
            
            H1F histtofit2 =  histogramsFTHodo.H_NOISE_V.get(secSel, laySel, comSel);
            F1D ftofit2 = histogramsFTHodo.fV2.get(secSel, laySel, comSel);
            FTAdjustFit cfit2 = new FTAdjustFit(histtofit2, ftofit2, "LRQ");
        }else if (this.tabSel==tabIndexMIPsignal){
            if (!matchingTiles){
                if (plotVoltageChargeBoth==1 || plotVoltageChargeBoth==3){
                    System.out.println("Adjusting MIP mV fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
                    H1F histtofit =  histogramsFTHodo.H_MIP_V.get(secSel, laySel, comSel);
                    F1D ftofit = histogramsFTHodo.fVMIP.get(secSel, laySel, comSel);
                    FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ");
                }
                if (plotVoltageChargeBoth==2 ||plotVoltageChargeBoth==3){
                    System.out.println("Adjusting MIP pC fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
                    H1F histtofit1 =  histogramsFTHodo.H_MIP_Q.get(secSel, laySel, comSel);
                    F1D ftofit1 = histogramsFTHodo.fQMIP.get(secSel, laySel, comSel);
                    FTAdjustFit cfit1 = new FTAdjustFit(histtofit1, ftofit1, "LRQ");
                }
            }else if (matchingTiles){
                if (plotVoltageChargeBoth==1 || plotVoltageChargeBoth==3){
                    System.out.println("Adjusting MIP Matching Tiles mV fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
                    H1F histtofit =  histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel, laySel, comSel);
                    F1D ftofit = histogramsFTHodo.fVMIPMatching.get(secSel, laySel, comSel);
                    FTAdjustFit cfit = new FTAdjustFit(histtofit, ftofit, "LRQ");
                }
                if (plotVoltageChargeBoth==2 || plotVoltageChargeBoth==3){
                    System.out.println("Adjusting MIP Matching Tiles pC fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
                    H1F histtofit1 =  histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel, laySel, comSel);
                    F1D ftofit1 = histogramsFTHodo.fQMIPMatching.get(secSel, laySel, comSel);
                    FTAdjustFit cfit1 = new FTAdjustFit(histtofit1, ftofit1, "LRQ");
                }
            }
        }
    }
    
    public void adjustFitConstants() {
        if (this.tabSel==tabIndexPed){
            System.out.println("Old Mean:"+pedMean[secSel][laySel][comSel] +" "+ pedRMS[secSel][laySel][comSel]);
            this.pedMean[secSel][laySel][comSel]=histogramsFTHodo.fPed.get(secSel, laySel, comSel).getParameter(1);
            this.pedRMS[secSel][laySel][comSel]=histogramsFTHodo.fPed.get(secSel, laySel, comSel).getParameter(2);
            System.out.println("New Mean:"+pedMean[secSel][laySel][comSel] +" "+ pedRMS[secSel][laySel][comSel]);

            this.updateTable();
        }
        else if (this.tabSel==tabIndexNoise){
            System.out.println("Old Mean:"+gain_mV[secSel][laySel][comSel] +" "+ errGain_mV[secSel][laySel][comSel]);
            this.gain_mV[secSel][laySel][comSel]=histogramsFTHodo.fV2.get(secSel, laySel, comSel).getParameter(4)-histogramsFTHodo.fV2.get(secSel, laySel, comSel).getParameter(1);
            double gainError=histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(4).error()*histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(4).error()+histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(1).error()*histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(1).error();
            this.errGain_mV[secSel][laySel][comSel] = sqrt(gainError);
            System.out.println("New Mean:"+gain_mV[secSel][laySel][comSel] +" "+ errGain_mV[secSel][laySel][comSel]);
            
            System.out.println("Old Mean:"+gain[secSel][laySel][comSel] +" "+ errGain[secSel][laySel][comSel]);
            this.gain[secSel][laySel][comSel]=histogramsFTHodo.fQ2.get(secSel, laySel, comSel).getParameter(4)-histogramsFTHodo.fQ2.get(secSel, laySel, comSel).getParameter(1);
            gainError=histogramsFTHodo.fQ2.get(secSel, laySel, comSel).parameter(4).error()*histogramsFTHodo.fQ2.get(secSel, laySel, comSel).parameter(4).error()+histogramsFTHodo.fQ2.get(secSel, laySel, comSel).parameter(1).error()*histogramsFTHodo.fQ2.get(secSel, laySel, comSel).parameter(1).error();
            this.errGain[secSel][laySel][comSel] = sqrt(gainError);
            System.out.println("New Mean:"+gain[secSel][laySel][comSel] +" "+ errGain[secSel][laySel][comSel]);
            
            this.setGGraphGain();
            this.updateTable();
            if (!drawByElec)
                drawCanvasNoiseAnalysis();
            else
                drawCanvasNoiseAnalysisElec(secSel,laySel,comSel );
        }else if (this.tabSel==tabIndexMIPsignal){
            if (!matchingTiles){
                if (plotVoltageChargeBoth==1 || plotVoltageChargeBoth==3){
                    System.out.println("Old Mean:"+MIPS_maxV_all[secSel][laySel][comSel] +" "+ MIPSerr_maxV_all[secSel][laySel][comSel]);
                    this.MIPS_maxV_all[secSel][laySel][comSel]=histogramsFTHodo.fVMIP.get(secSel, laySel, comSel).getParameter(1);
                    this.MIPSerr_maxV_all[secSel][laySel][comSel]=histogramsFTHodo.fVMIP.get(secSel, laySel, comSel).getParameter(2);
                    if (gain_mV[secSel][laySel][comSel]>0)
                        this.MIPgain_mV[secSel][laySel][comSel]=MIPS_maxV_all[secSel][laySel][comSel]/gain_mV[secSel][laySel][comSel];
                    else
                        this.MIPgain_mV[secSel][laySel][comSel]=0.0;
                    if (MIPgain_mV[secSel][laySel][comSel]>0){
                        double n1Error = histogramsFTHodo.fVMIP.get(secSel, laySel, comSel).parameter(1).error()/histogramsFTHodo.fVMIP.get(secSel, laySel, comSel).getParameter(1);
                        double n2Error =getGainError(secSel, laySel, comSel, "peakvolt")/getGain(secSel, laySel, comSel, "peakvolt");
                        this.MIPerrgain_mV[secSel][laySel][comSel]=MIPgain_mV[secSel][laySel][comSel]*sqrt(n1Error*n1Error+n2Error*n2Error);
                    }else {
                        this.MIPerrgain_mV[secSel][laySel][comSel]=100.0;
                    }
                    System.out.println("New Mean:"+MIPS_maxV_all[secSel][laySel][comSel] +" "+ MIPSerr_maxV_all[secSel][laySel][comSel]);
                }
                if (plotVoltageChargeBoth==2 || plotVoltageChargeBoth==3){
                    System.out.println("Old Mean:"+MIPS_pC_all[secSel][laySel][comSel] +" "+ MIPSerr_pC_all[secSel][laySel][comSel]);
                    this.MIPS_pC_all[secSel][laySel][comSel]=histogramsFTHodo.fQMIP.get(secSel, laySel, comSel).getParameter(1);
                    this.MIPSerr_pC_all[secSel][laySel][comSel]=histogramsFTHodo.fQMIP.get(secSel, laySel, comSel).getParameter(2);
                    if (gain[secSel][laySel][comSel]>0)
                        this.MIPgain[secSel][laySel][comSel]=MIPS_pC_all[secSel][laySel][comSel]/gain[secSel][laySel][comSel];
                    else
                        this.MIPgain[secSel][laySel][comSel]=0.0;
                    if (MIPgain[secSel][laySel][comSel]>0){
                        double n1Error = histogramsFTHodo.fQMIP.get(secSel, laySel, comSel).parameter(1).error()/histogramsFTHodo.fQMIP.get(secSel, laySel, comSel).getParameter(1);
                        double n2Error =getGainError(secSel, laySel, comSel, "charge")/getGain(secSel, laySel, comSel, "charge");
                        this.MIPerrgain[secSel][laySel][comSel]=MIPgain[secSel][laySel][comSel]*sqrt(n1Error*n1Error+n2Error*n2Error);
                    }else
                        this.MIPerrgain[secSel][laySel][comSel]=100.0;
                    System.out.println("New Mean:"+MIPS_pC_all[secSel][laySel][comSel] +" "+ MIPSerr_pC_all[secSel][laySel][comSel]);
                }
            }else if (matchingTiles){
                if (plotVoltageChargeBoth==1 || plotVoltageChargeBoth==3){
                    System.out.println("Old Mean:"+MIPS_maxV_MatchingTiles[secSel][laySel][comSel] +" "+ MIPSerr_maxV_MatchingTiles[secSel][laySel][comSel]);
                    this.MIPS_maxV_MatchingTiles[secSel][laySel][comSel]=histogramsFTHodo.fVMIPMatching.get(secSel, laySel, comSel).getParameter(1);
                    this.MIPSerr_maxV_MatchingTiles[secSel][laySel][comSel]=histogramsFTHodo.fVMIPMatching.get(secSel, laySel, comSel).getParameter(2);
                    if (gain_mV[secSel][laySel][comSel]>0)
                        this.MIPMatchingTilesgain_mV[secSel][laySel][comSel]=MIPS_maxV_MatchingTiles[secSel][laySel][comSel]/gain_mV[secSel][laySel][comSel];
                    else
                        this.MIPMatchingTilesgain_mV[secSel][laySel][comSel]=0.0;
                    if (this.MIPMatchingTilesgain_mV[secSel][laySel][comSel]>0){
                        double n1Error = histogramsFTHodo.fVMIPMatching.get(secSel, laySel, comSel).parameter(1).error()/histogramsFTHodo.fVMIPMatching.get(secSel, laySel, comSel).getParameter(1);
                        double n2Error =getGainError(secSel, laySel, comSel, "peakvolt")/getGain(secSel, laySel, comSel, "peakvolt");
                        this.MIPMatchingTileserrgain_mV[secSel][laySel][comSel]=MIPMatchingTilesgain_mV[secSel][laySel][comSel]*sqrt(n1Error*n1Error+n2Error*n2Error);
                    }else {
                        this.MIPMatchingTileserrgain_mV[secSel][laySel][comSel]=100.0;
                    }
                    System.out.println("New Mean:"+MIPS_maxV_MatchingTiles[secSel][laySel][comSel] +" "+ MIPSerr_maxV_MatchingTiles[secSel][laySel][comSel]);
                }
                if (plotVoltageChargeBoth==2 || plotVoltageChargeBoth==3){
                    System.out.println("Old Mean:"+MIPS_pC_MatchingTiles[secSel][laySel][comSel] +" "+ MIPSerr_pC_MatchingTiles[secSel][laySel][comSel]);
                    this.MIPS_pC_MatchingTiles[secSel][laySel][comSel]=histogramsFTHodo.fQMIPMatching.get(secSel, laySel, comSel).getParameter(1);
                    this.MIPSerr_pC_MatchingTiles[secSel][laySel][comSel]=histogramsFTHodo.fQMIPMatching.get(secSel, laySel, comSel).getParameter(2);
                    if (gain[secSel][laySel][comSel]>0)
                        this.MIPMatchingTilesgain[secSel][laySel][comSel]=MIPS_pC_MatchingTiles[secSel][laySel][comSel]/gain[secSel][laySel][comSel];
                    else
                        this.MIPMatchingTilesgain[secSel][laySel][comSel]=0.0;
                    if (this.MIPMatchingTilesgain[secSel][laySel][comSel]>0){
                        double n1Error = histogramsFTHodo.fQMIPMatching.get(secSel, laySel, comSel).parameter(1).error()/histogramsFTHodo.fQMIPMatching.get(secSel, laySel, comSel).getParameter(1);
                        double n2Error =getGainError(secSel, laySel, comSel, "charge")/getGain(secSel, laySel, comSel, "charge");
                        this.MIPMatchingTileserrgain[secSel][laySel][comSel]=MIPMatchingTilesgain[secSel][laySel][comSel]*sqrt(n1Error*n1Error+n2Error*n2Error);
                    }else {
                        this.MIPMatchingTileserrgain[secSel][laySel][comSel]=100.0;
                    }
                    System.out.println("New Mean:"+MIPS_pC_MatchingTiles[secSel][laySel][comSel] +" "+ MIPSerr_pC_MatchingTiles[secSel][laySel][comSel]);
                }
            }
            this.setGGraphGain();
            this.updateTable();
            if (!drawByElec)
                drawCanvasMIPAnalysis();
            else
                drawCanvasMIPAnalysisElec(secSel,laySel,comSel );
        }
    }

    
    public void readCCDBconstants(){
        int s,l,c, chan, slot;
        int crate=72;
        this.ped_fadcCCDB= new double[9][3][21];
        this.ped_noiseCCDB= new double[9][3][21];
        this.pedrms_noiseCCDB= new double[9][3][21];
        this.gain_pc_NoiseCCDB= new double[9][3][21];
        this.gain_mV_NoiseCCDB= new double[9][3][21];
        this.MIPS_pC_C2ECCDB= new double[9][3][21];
        this.npeThreshold_NoiseCCDB= new double[9][3][21];
        this.status_CCDB=new int[9][3][21];
        
        InverseTranslationTable = new CalibrationConstants(3,
                                                  "crate/I:" +//3
                                                  "slot/I:"+//4
                                                  "chan/I");
        for (int slotn = 3; slotn < 20; slotn++) {
            for (int chann = 0; chann < 16; chann++) {
                if (!calibrationTranslationTable.hasEntry(crate, slotn, chann))
                    continue;
                int secn = calibrationTranslationTable.getIntValue("sector", crate, slotn, chann);
                int layn = calibrationTranslationTable.getIntValue("layer", crate, slotn, chann);
                int compn = calibrationTranslationTable.getIntValue("component", crate, slotn, chann);
                //System.out.println("about to add Entry "+secn+" "+layn+" "+compn);
                InverseTranslationTable.addEntry(secn,layn,compn);
                InverseTranslationTable.setIntValue(crate,"crate", secn,layn,compn);
                InverseTranslationTable.setIntValue(slotn,"slot", secn,layn,compn);
                InverseTranslationTable.setIntValue(chann,"chan", secn,layn,compn);
                //System.out.println("Added Entry "+secn+" "+layn+" "+compn);
            }
        }
        
        for (int index = 0; index < 232; index++) {
            histogramsFTHodo.HP.setAllParameters(index, 'h');
            s=histogramsFTHodo.HP.getS();
            l=histogramsFTHodo.HP.getL();
            c=histogramsFTHodo.HP.getC();
            chan=InverseTranslationTable.getIntValue("chan", s,l,c);//wireFTHodo.getChan4SLC(s,l,c);
            slot=InverseTranslationTable.getIntValue("slot", s,l,c);//wireFTHodo.getSlot4SLC(s,l,c);
            this.ped_fadcCCDB[s][l][c]=calibrationfADC.getDoubleValue("pedestal", crate, slot, chan);
            this.status_CCDB[s][l][c]=calibrationStatusTable.getIntValue("status", s,l,c);
            this.ped_noiseCCDB[s][l][c]=calibrationNoiseTable.getDoubleValue("pedestal", s,l,c);
            this.pedrms_noiseCCDB[s][l][c]=calibrationNoiseTable.getDoubleValue("pedestal_rms", s,l,c);
            this.gain_pc_NoiseCCDB[s][l][c]=calibrationNoiseTable.getDoubleValue("gain_pc", s,l,c);
            this.gain_mV_NoiseCCDB[s][l][c]=calibrationNoiseTable.getDoubleValue("gain_mv", s,l,c);
            this.npeThreshold_NoiseCCDB[s][l][c]=calibrationNoiseTable.getDoubleValue("npe_threshold", s,l,c);
            this.MIPS_pC_C2ECCDB[s][l][c]=calibrationChargeToEnergyTable.getDoubleValue("mips_charge", s,l,c);
            //System.out.println(InverseTranslationTable.getIntValue("crate", s,l,c)+" "+InverseTranslationTable.getIntValue("slot", s,l,c)+" "+InverseTranslationTable.getIntValue("chan", s,l,c)+" "+s+" "+l+" "+c+" ");
        }
    }
    
    public void saveTable(String name, CalibrationConstants calibConstants) {
        try {
            // Open the output file
            File outputFile = new File(name);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);
            
            for (int i = 0; i < calibConstants.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < calibConstants.getColumnCount(); j++) {
                    line = line + calibConstants.getValueAt(i, j);
                    if (j < calibConstants.getColumnCount() - 1) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            outputBw.close();
            System.out.println("Constants saved to'" + name);
        } catch (IOException ex) {
            System.out.println("Error writing file '"+ name + "'");
            // Or we could just do this:
            ex.printStackTrace();
        }
    }
    
    
    public void savefADC250ft3PedTable(String name, String time) {
        try {
            // Open the output file
            File outputFile = new File(name);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);
            String line = new String();
            line ="#";
            outputBw.write(line);
            outputBw.newLine();
            line = new String();
            line ="# File generated automatically by FTHodo Calib at "+time;
            outputBw.write(line);
            outputBw.newLine();
            line = new String();
            line ="#";
            outputBw.write(line);
            outputBw.newLine();
            line = new String();
            line ="FADC250_CRATE adcft3";
            outputBw.write(line);
            outputBw.newLine();
            for (int i = 3; i < 20; i++) {
                if (i==11||i==12)
                    continue;
                line = new String();
                line ="FADC250_SLOT " +i;
                outputBw.write(line);
                outputBw.newLine();
                line = new String();
                line ="FADC250_ALLCH_PED ";
                for (int j = 0; j < 16; j++) {
                    line = line +  String.format("%.3f", pedValues4SlotChan[i][j]);
                    if (j < 15) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            line = new String();
            line ="FADC250_CRATE end";
            outputBw.write(line);
            outputBw.newLine();
            outputBw.close();
            System.out.println("Constants saved to'" + name);
        } catch (IOException ex) {
            System.out.println("Error writing file '"+ name + "'");
            ex.printStackTrace();
        }
    }
    
    
    
    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }
    
    public int getRunNumber() {
        return runNumber;
    }
    
    public CodaEventDecoder getDecoder() {
        return decoder;
    }
    
    public void setArraysToDefault(){
        System.out.println(" Setting arrays to CCDB values");
        for (int s = 1; s < 9; s++) {
            for (int l = 1; l < 3; l++) {
                for (int c = 1; c < 21; c++) {
                    if (s % 2 == 1 && c > 9) {
                        continue;
                    }
                    setPedestalDefault(s, l, c);
                    setGainAllDefault(s, l, c);
                    setMIPAllDefault(s, l, c);
                }
            }
        }
        System.out.println(" Arrays Set");
    }

    private void setPedestalDefault(int s, int l,int c){
        pedMean[s][l][c]=ped_fadcCCDB[s][l][c];
        pedRMS[s][l][c]=pedrms_noiseCCDB[s][l][c];
        int slot=InverseTranslationTable.getIntValue("slot", s,l,c);//wireFTHodo.getSlot4SLC(s,l,c);
        int chan=InverseTranslationTable.getIntValue("chan", s,l,c);//wireFTHodo.getChan4SLC(s,l,c);
        //System.out.println(slot+" "+InverseTranslationTable.getIntValue("slot", s,l,c)+" "+chan+" "+InverseTranslationTable.getIntValue("chan", s,l,c));
        pedValues4SlotChan[slot][chan]=ped_fadcCCDB[s][l][c];
    }
    
    private void setGainAllDefault(int s, int l,int c){
        gain[s][l][c]=gain_pc_NoiseCCDB[s][l][c];
        errGain[s][l][c]=0.0;
        gain_mV[s][l][c]=gain_mV_NoiseCCDB[s][l][c];
        errGain_mV[s][l][c]=0.0;
    }
    
    private void setMIPAllDefault(int s, int l,int c){
        MIPS_pC_all[s][l][c]=MIPS_pC_C2ECCDB[s][l][c];
        MIPSerr_pC_all[s][l][c]=0.0;
        MIPgain[s][l][c]=MIPS_pC_C2ECCDB[s][l][c]/getGain(s, l, c,"charge");
        MIPerrgain[s][l][c]=0.0;
        MIPS_pC_MatchingTiles[s][l][c]=MIPS_pC_C2ECCDB[s][l][c];
        MIPSerr_pC_MatchingTiles[s][l][c]=0.0;
        MIPMatchingTilesgain[s][l][c]=MIPS_pC_C2ECCDB[s][l][c]/getGain(s, l, c,"charge");
        MIPMatchingTileserrgain[s][l][c]=0.0;
        MIPS_maxV_all[s][l][c]=0.0;
        MIPSerr_maxV_all[s][l][c]=0.0;
        MIPgain_mV[s][l][c]=0.0;
        MIPerrgain_mV[s][l][c]=0.0;
        MIPS_maxV_MatchingTiles[s][l][c]=0.0;
        MIPSerr_maxV_MatchingTiles[s][l][c]=0.0;
        MIPMatchingTilesgain_mV[s][l][c]=0.0;
        MIPMatchingTileserrgain_mV[s][l][c]=0.0;
    }
    

}
