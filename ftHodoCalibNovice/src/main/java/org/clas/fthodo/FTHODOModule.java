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
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.clas.detector.CodaEventDecoder;
import org.clas.detector.DetectorDataDgtz;
import org.clas.detector.DetectorEventDecoder;

import org.clas.view.DetectorListener;
import org.clas.view.DetectorPane2D;
import org.clas.view.DetectorShape2D;

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
    CalibrationConstants       ccdbTable = null;

    EmbeddedCanvas canvasEvent = new EmbeddedCanvas();
    EmbeddedCanvas canvasPed = new EmbeddedCanvas();
    EmbeddedCanvas canvasNoise = new EmbeddedCanvas();
    EmbeddedCanvas canvasGain = new EmbeddedCanvas();
    EmbeddedCanvas canvasMIPsignal = new EmbeddedCanvas();

    //    EmbeddedCanvas canvasCharge = new EmbeddedCanvas();
//    EmbeddedCanvas canvasVoltage = new EmbeddedCanvas();
//    EmbeddedCanvas canvasMIP = new EmbeddedCanvas();
//    EmbeddedCanvas canvasMatch = new EmbeddedCanvas();
//    EmbeddedCanvas canvasTime = new EmbeddedCanvas();
    // Gagik to implement
    // view.addChangeListener(this);
    ColorPalette palette = new ColorPalette();
    FTHodoHistograms histogramsFTHodo = new FTHodoHistograms();
    //=================================
    //           ARRAYS
    //=================================
    private double[][][] status;
    private double[][][] thrshNPE;
    private double[][][] pedMean;
    private double[][][] pedRMS;
    // save the pedestal of the previous event
    private double[][][] pedPrevious;
    private double[][][] gain;
    private double[][][] errGain;
    private double[][][] gain_mV;
    private double[][][] errGain_mV;
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
    IndexedTable geometryTable = null;
//    String geomFileName = "./Tables/fthodo_geometry.txt";
//    String noisFileName = "./Tables/fthodo_noise.txt";
    String ccdbFileName = "./Tables/fthodo_ccdb.txt";
    boolean debugging_mode = false;
    boolean setConstantsToCCDB = false;
    boolean pedMeanGood = false;


    //=================================
    //           VARIABLES
    //=================================
    double tile_size = 15;
    int nDecodedProcessed = 0;
    private int secSel = 3;
    private int laySel = 1;
    private int comSel = 1;
    private int indexSel = 1;
    private boolean drawByElec = true;
    private boolean useGain_mV = true;
    private boolean matchingTiles = false;

    private int plotVoltageChargeBoth=1; //1==voltage 2 is Charge, 3 is both
    JPanel rBPaneGain;
    JPanel rBPaneMIP;

    int previousTabSel = 0;
    private int tabSel = 0;
    private int timerUpdate = 1000;

    // the following indices must correspond
    // to the order the canvased are added
    // to 'tabbedPane'
    final private int tabIndexEvent = 0;
    final private int tabIndexPed = 1;
    final private int tabIndexNoise = 2;
    final private int tabIndexGain = 3;
    final private int tabIndexMIPsignal = 4;

    //    final private int tabIndexCharge = 4;
//    final private int tabIndexVoltage = 5;
//    final private int tabIndexMIP = 6;
//    final private int tabIndexMatch = 7;
//    final private int tabIndexTime = 8;
//    final private int tabIndexTable = 9;

    public void initPanel() {
//        canvasTable.addListener(this);
        this.initTable();
        this.initConstants();
        this.initCCDB();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Event", this.canvasEvent);
        tabbedPane.add("Pedestal", this.canvasPed);
        tabbedPane.add("Noise", this.canvasNoise);
        tabbedPane.add("Gain", this.canvasGain);
        tabbedPane.add("MIP signal", this.canvasMIPsignal);

//        tabbedPane.add("Charge", this.canvasCharge);
//        tabbedPane.add("Voltage", this.canvasVoltage);
//        tabbedPane.add("MIP", this.canvasMIP);
//        tabbedPane.add("Match", this.canvasMatch);
//        tabbedPane.add("Time", this.canvasTime);
//        tabbedPane.add("Table", canvasTable);
        tabbedPane.addChangeListener(this);
        tabbedPane.setSelectedIndex(this.tabSel);
        this.initCanvas();
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        JButton fitBtn = new JButton("Calibrate");
        fitBtn.addActionListener(this);
        buttonPane.add(fitBtn);
        JButton constantsBtn = new JButton("Check Constants");
        constantsBtn.addActionListener(this);
        buttonPane.add(constantsBtn);

        JButton printBtn = new JButton("Save Calibration Constants");
        printBtn.addActionListener(this);
        buttonPane.add(printBtn);
        
//        JButton fitBtn = new JButton("Fit");
//        fitBtn.addActionListener(this);
//        buttonPane.add(fitBtn);
//
//        JButton constantsBtn = new JButton("Constants");
//        constantsBtn.addActionListener(this);
//        buttonPane.add(constantsBtn);
//
//        JButton printBtn = new JButton("Print");
//        printBtn.addActionListener(this);
//        buttonPane.add(printBtn);
//
//        JButton readBtn = new JButton("Read");
//        readBtn.addActionListener(this);
//        buttonPane.add(readBtn);
//
//        JButton pedBtn = new JButton("Ped");
//        pedBtn.addActionListener(this);
//        buttonPane.add(pedBtn);

        //====        
        ButtonGroup group1 = new ButtonGroup();
        ButtonGroup group2 = new ButtonGroup();
        rBPaneGain = new JPanel();
        rBPaneGain.setLayout(new FlowLayout());
        
        JRadioButton rBDetect = new JRadioButton("Detector View");
        group1.add(rBDetect);
        rBPaneGain.add(rBDetect);
        rBDetect.addActionListener(this);
        JRadioButton rBElec = new JRadioButton("Electronics View");
        group1.add(rBElec);
        rBPaneGain.add(rBElec);
        rBElec.addActionListener(this);
        if (this.drawByElec)
            rBElec.setSelected(true);
        else
            rBDetect.setSelected(true);
            
        JRadioButton rBGainPeak = new JRadioButton("max Voltage");
        group2.add(rBGainPeak);
        rBPaneGain.add(rBGainPeak);
        rBGainPeak.addActionListener(this);
        JRadioButton rBGainChrg = new JRadioButton("Charge");
        group2.add(rBGainChrg);
        rBPaneGain.add(rBGainChrg);
        rBGainChrg.addActionListener(this);
        if (useGain_mV)
            rBGainPeak.setSelected(true);
        else
            rBGainChrg.setSelected(true);
        
        
        ButtonGroup groupMIPSignal = new ButtonGroup();
        ButtonGroup groupMIPSignalMatch = new ButtonGroup();
        rBPaneMIP = new JPanel();
        rBPaneMIP.setLayout(new FlowLayout());
        
        JRadioButton rB_MIPmV = new JRadioButton("max Voltage");
        groupMIPSignal.add(rB_MIPmV);
        rBPaneMIP.add(rB_MIPmV);
        rB_MIPmV.addActionListener(this);
        JRadioButton rB_MIPChrg = new JRadioButton("Charge");
        groupMIPSignal.add(rB_MIPChrg);
        rBPaneMIP.add(rB_MIPChrg);
        rB_MIPChrg.addActionListener(this);
        JRadioButton rB_MIPmVChrg = new JRadioButton("Voltage & Charge");
        groupMIPSignal.add(rB_MIPmVChrg);
        rBPaneMIP.add(rB_MIPmVChrg);
        rB_MIPmVChrg.addActionListener(this);
        if (plotVoltageChargeBoth==1)
            rB_MIPmV.setSelected(true);
        else if (plotVoltageChargeBoth==2)
            rB_MIPChrg.setSelected(true);
        else if (plotVoltageChargeBoth==3)
            rB_MIPmVChrg.setSelected(true);
        
        JRadioButton rB_MIPMatchingTiles = new JRadioButton("Matching Tiles");
        groupMIPSignalMatch.add(rB_MIPMatchingTiles);
        rBPaneMIP.add(rB_MIPMatchingTiles);
        rB_MIPMatchingTiles.addActionListener(this);
        JRadioButton rB_MIPAllSign = new JRadioButton("All Signals");
        groupMIPSignalMatch.add(rB_MIPAllSign);
        rBPaneMIP.add(rB_MIPAllSign);
        rB_MIPAllSign.addActionListener(this);
        if (matchingTiles)
            rB_MIPMatchingTiles.setSelected(true);
        else
            rB_MIPAllSign.setSelected(true);
        
        //rBGainPeak.isSelected());
        //rBGainChrg.setSelected(true);
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

        this.canvasGain.divide(3, 3);
        this.canvasGain.setGridX(false);
        this.canvasGain.setGridY(false);
        this.canvasGain.setAxisFontSize(10);
        this.canvasGain.setStatBoxFontSize(2);
        this.canvasGain.initTimer(timerUpdate);
        //drawCanvasGainElec(secSel, laySel, comSel);

        this.canvasMIPsignal.divide(2, 2);
        this.canvasMIPsignal.setGridX(false);
        this.canvasMIPsignal.setGridY(false);
        this.canvasMIPsignal.setAxisFontSize(10);
        this.canvasMIPsignal.setStatBoxFontSize(2);
        this.canvasMIPsignal.initTimer(timerUpdate);
        drawCanvasMIPsignal(secSel, laySel, comSel);
        
        
        //
//        this.canvasCharge.divide(2, 2);
//        this.canvasCharge.setGridX(false);
//        this.canvasCharge.setGridY(false);
//        this.canvasCharge.setAxisFontSize(10);
//        this.canvasCharge.setStatBoxFontSize(2);
//        this.canvasCharge.initTimer(timerUpdate);
//        drawCanvasCharge(secSel, laySel, comSel);
//
//        this.canvasVoltage.divide(2, 2);
//        this.canvasVoltage.setGridX(false);
//        this.canvasVoltage.setGridY(false);
//        this.canvasVoltage.setAxisFontSize(10);
//        this.canvasVoltage.setStatBoxFontSize(2);
//        this.canvasVoltage.initTimer(timerUpdate);
//        drawCanvasVoltage(secSel, laySel, comSel);
//
//        this.canvasMIP.divide(3, 3);
//        this.canvasMIP.setGridX(false);
//        this.canvasMIP.setGridY(false);
//        this.canvasMIP.setAxisFontSize(10);
//        this.canvasMIP.setStatBoxFontSize(2);
//        this.canvasMIP.initTimer(timerUpdate);
//        drawCanvasMIPElec(secSel, laySel, comSel);
//
//        this.canvasMatch.divide(2, 2);
//        this.canvasMatch.setGridX(true);
//        this.canvasMatch.setGridY(true);
//        this.canvasMatch.setAxisFontSize(10);
//        this.canvasMatch.setStatBoxFontSize(8);
//        this.canvasMatch.initTimer(timerUpdate);
//        drawCanvasMatch(secSel, laySel, comSel);
//
//        this.canvasTime.divide(3, 2);
//        this.canvasTime.setGridX(false);
//        this.canvasTime.setGridY(false);
//        this.canvasTime.setAxisFontSize(10);
//        this.canvasTime.setStatBoxFontSize(8);
//        this.canvasTime.initTimer(timerUpdate);
//        drawCanvasTime(secSel, laySel, comSel);
    }   
    
    
    public void initCCDB() {
            System.out.println("monitor.initCCDB()"); 
            ccdb.init(Arrays.asList(new String[]{
                    "/daq/fadc/fthodo",
                    "/daq/tt/fthodo",
                    "/geometry/ft/fthodo"}));
//            this.getReverseTT(ccdb,"/daq/tt/ftof"); 
            this.detectorDecoder.getFadcPanel().init(ccdb,11,"/daq/fadc/fthodo", 72,3,1); 
            this.geometryTable = ccdb.getConstants(11, "/geometry/ft/fthodo");
    } 

    private void initConstants() {
        histogramsFTHodo.initConstants();
    }

    private void initTable() {
        ccdbTable = new CalibrationConstants(3,
                "status/I:" + // 3
                "ped/F:" + // 4
                "ped_rms/F:" + // 5
                "gain_pc/F:" + // 6
                "gain_mv/F:" + // 7
                "thr_npe/F:" + // 8
                "mips_e/F:" + // 9
                "mips_q/F:" + // 10
                "t_offset/F:" + // 11
                "t_rms/F");// 12
        ccdbTable.setPrecision(3);
        ccdbTable.addConstraint(3, -0.5, 0.5);
        ccdbTable.addConstraint(4, 130.0, 440.0);
        ccdbTable.addConstraint(5, 1.0, 100.0);
        ccdbTable.addConstraint(6, 10.0, 30.0);
        ccdbTable.addConstraint(7, 6.0, 16.0);
        ccdbTable.addConstraint(8, 2.0, 3.0);
        ccdbTable.addConstraint(9, 1.0, 4.0);
        ccdbTable.addConstraint(10, 500, 3000);
        ccdbTable.addConstraint(11, -2.0, 2.0);
        ccdbTable.addConstraint(12, -5.0, 5.0);

        for (int layer = 2; layer > 0; layer--) {
            for (int sector = 1; sector < 9; sector++) {
                for (int component = 1; component < 21; component++) {

                    if (sector % 2 == 1 && component > 9) {
                        continue;
                    }
                    ccdbTable.addEntry(sector,layer,component);
                    ccdbTable.setIntValue(0,             "status", sector, layer, component);
                    ccdbTable.setDoubleValue(200.,          "ped", sector, layer, component);
                    ccdbTable.setDoubleValue(histogramsFTHodo.nThrshNPE, "ped_rms", sector, layer, component);
                    ccdbTable.setDoubleValue(10.,       "gain_pc", sector, layer, component);
                    ccdbTable.setDoubleValue(20.,       "gain_mv", sector, layer, component);
                    ccdbTable.setDoubleValue(10.,       "thr_npe", sector, layer, component);
                    ccdbTable.setDoubleValue(1.4,        "mips_e", sector, layer, component);
                    ccdbTable.setDoubleValue(700.,       "mips_q", sector, layer, component);
                    ccdbTable.setDoubleValue(99.9,     "t_offset", sector, layer, component);
                    ccdbTable.setDoubleValue(99.9,        "t_rms", sector, layer, component);
                }
            }
        }
        ccdbTable.fireTableDataChanged();
        canvasTable.addConstants(ccdbTable, this);
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
    //-----------------------------------------
    private void printCCDBTables() {
        try {
            PrintWriter fout_full;
            fout_full = new PrintWriter("./Tables/fthodo_ccdb.txt");
            PrintWriter fout_status;
            fout_status = new PrintWriter("./Tables/fthodo_status.txt");
            PrintWriter fout_noise;
            fout_noise = new PrintWriter("./Tables/fthodo_noise.txt");
            PrintWriter fout_energy;
            fout_energy = new PrintWriter("./Tables/fthodo_energy.txt");
            PrintWriter fout_time;
            fout_time = new PrintWriter("./Tables/fthodo_time.txt");
            String col = "";
            for (int c = 0; c < ccdbTable.getColumnCount(); c++) {
                col = ccdbTable.getColumnName(c);
                // create column titles
                switch (c) {
                    case 0:
                        fout_full.printf("s \t");
                        fout_status.printf("s \t");
                        fout_noise.printf("s \t");
                        fout_energy.printf("s \t");
                        fout_time.printf("s \t");
                        break;
                    case 1:
                        fout_full.printf("l \t");
                        fout_status.printf("l \t");
                        fout_noise.printf("l \t");
                        fout_time.printf("l \t");
                        fout_energy.printf("l \t");
                        break;
                    case 2:
                        fout_full.printf("c \t");
                        fout_status.printf("c \t");
                        fout_noise.printf("c \t");
                        fout_time.printf("c \t");
                        fout_energy.printf("c \t");
                        break;
                    default:
                        fout_full.printf(col + "\t");
                        break;
                }
                if (col.equals("status")) {
                    fout_status.printf(col + "\t");
                } else if (col.equals("ped")
                        || col.equals("ped_rms")
                        || col.equals("gain_pc")
                        || col.equals("gain_mv")
                        || col.equals("thr_npe")) {
                    fout_noise.printf(col + "\t");
                } else if (col.equals("mips_e")
                        || col.equals("mips_q")) {
                    fout_energy.printf(col + "\t");
                } else if (col.equals("t_offset")
                        || col.equals("t_rms")) {
                    fout_time.printf(col + "\t");
                }
            }
            fout_full.printf("\n");
            fout_status.printf("\n");
            fout_noise.printf("\n");
            fout_energy.printf("\n");
            fout_time.printf("\n");
            
            for (int r = 0; r < ccdbTable.getRowCount(); r++) {
                for (int c = 0; c < ccdbTable.getColumnCount(); c++) {
                    col = ccdbTable.getColumnName(c);
                    if (c < 3) {
                        fout_full.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_status.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_noise.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_energy.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_time.printf(ccdbTable.getValueAt(r, c) + "\t");
                    } else if (col.equals("status")) {
                        Double pp = Double.parseDouble(ccdbTable.getValueAt(r, c).toString());
                        fout_full.printf(pp.intValue() + "\t");
                        fout_status.printf(pp.intValue() + "\t");
                    } else if (col.equals("ped")
                            || col.equals("ped_rms")
                            || col.equals("gain_pc")
                            || col.equals("gain_mv")
                            || col.equals("thr_npe")) {
                        fout_full.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_noise.printf(ccdbTable.getValueAt(r, c) + "\t");
                    } else if (col.equals("mips_e")
                            || col.equals("mips_q")) {
                        fout_full.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_energy.printf(ccdbTable.getValueAt(r, c) + "\t");
                    } else if (col.equals("t_offset")
                            || col.equals("t_rms")) {
                        fout_full.printf(ccdbTable.getValueAt(r, c) + "\t");
                        fout_time.printf(ccdbTable.getValueAt(r, c) + "\t");
                    }
                }
                fout_full.printf("\n");
                fout_status.printf("\n");
                fout_noise.printf("\n");
                fout_energy.printf("\n");
                fout_time.printf("\n");
            }
            fout_full.close();
            fout_status.close();
            fout_noise.close();
            fout_energy.close();
            fout_time.close();
            System.out.println("CCDB files written");
        } catch (FileNotFoundException ex) {
            System.out.println(" Keith Cuthbertson ");
        }
    }

    public FTHODOModule() {
        System.out.println(" -------------------");
        System.out.println(" FTHODOViewerModule ");
        System.out.println(" -------------------");
        histogramsFTHodo.threshold = (int) histogramsFTHodo.threshD;
    }
    public void initDetector() {
        detectorView.add("Detector", this.drawDetector(0., 0.));
        detectorView.add("Electronics", this.drawChannels(0., 0.));
    }
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
        System.out.println("ACTION = " + e.getActionCommand());
        if (e.getActionCommand().compareTo("Reset") == 0) {
            this.resetHistograms();
        }
        if (e.getActionCommand().compareTo("Calibrate") == 0) {
            this.fitHistograms();
            this.updateArrays();
            this.setGGraphGain();
        }
//        if (e.getActionCommand().compareTo("Constants") == 0) {
//            updateConstants();
//        }
//        if (e.getActionCommand().compareTo("Print") == 0) {
//            printCCDBTables();
//        }
//        if (e.getActionCommand().compareTo("Read") == 0) {
//            setConstantsToCCDB = true;
//            System.out.println(" setting constants to CCDB values ");
//            System.out.println(" and updating the table ");
//            updateConstants();
//            setConstantsToCCDB = false;
//        }
//        if (e.getActionCommand().compareTo("Fit") == 0) {
//            this.fitHistograms();
//        }
//        if (e.getActionCommand().compareTo("Ped") == 0) {
//
//            if (pedMeanGood) {
//                pedMeanGood = false;
//                System.out.println("using event by event pedestals only");
//            } else {
//                pedMeanGood = true;
//                System.out.println("using pedestal mean for event "
//                        + " outliers (not inc. ADC values) ");
//            }
//            System.out.println("pedMeanGood = " + pedMeanGood);
//        }
        if (e.getActionCommand().compareTo("max Voltage") == 0) {
            if (this.tabSel==tabIndexGain){
                this.useGain_mV = true;
                if (this.drawByElec){
                    drawCanvasGainElec(secSel,laySel,comSel);
                }else{
                    drawCanvasGain();
                }
            }
            if (this.tabSel==tabIndexMIPsignal){
                this.plotVoltageChargeBoth=1;
                drawCanvasMIPsignal(secSel,laySel,comSel);
            }
        }
        if (e.getActionCommand().compareTo("Charge") == 0) {
            if (this.tabSel==tabIndexGain){
                this.useGain_mV = false;
                if (this.drawByElec) {
                    drawCanvasGainElec(secSel,laySel,comSel);
                } else{
                    drawCanvasGain();
                }
            }
            if (this.tabSel==tabIndexMIPsignal){
                this.plotVoltageChargeBoth=2;
                drawCanvasMIPsignal(secSel,laySel,comSel);
            }
        }
        if (e.getActionCommand().compareTo("Electronics View") == 0) {
            this.drawByElec = true;
            drawCanvasGainElec(secSel,laySel,comSel);
        }
        if (e.getActionCommand().compareTo("Detector View") == 0) {
            this.drawByElec = false;
            drawCanvasGain();
        }
        if (e.getActionCommand().compareTo("Voltage & Charge") == 0) {
            this.plotVoltageChargeBoth=3;
            drawCanvasMIPsignal(secSel,laySel,comSel);
        }
        if (e.getActionCommand().compareTo("Matching Tiles") == 0) {
            this.matchingTiles=true;
            drawCanvasMIPsignal(secSel,laySel,comSel);
        }
        if (e.getActionCommand().compareTo("All Signals") == 0) {
            this.matchingTiles=false;
            drawCanvasMIPsignal(secSel,laySel,comSel);
        }
        
    }

    private void fitHistograms() {
        histogramsFTHodo.fitHistograms();
    }

    void drawCanvasEvent(int secSel,int laySel,int comSel) {
        int layCD = laySel - 1;
        // map [1,2] to [1,0]
        int oppCD = laySel % 2;
        // map [1,2] to [2,1]
        int oppSel = (laySel % 2) + 1;
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
        // map [1,2] to [0,1]
        int layCD = laySel - 1;
        // map [1,2] to [1,0]
        int oppCD = laySel % 2;
        // map [1,2] to [2,1]
        int oppSel = (laySel % 2) + 1;
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
        // map [1,2] to [0,1]
        int layCD = laySel - 1;
        // map [1,2] to [1,0]
        int oppCD = laySel % 2;
        // map [1,0] to [2,1]
        int oppSel = oppCD + 1;
        // map [0,1] to [0,3]
        int layCDL = 3 * layCD;
        // map [1,0] to [3,0]
        int oppCDL = 3 * oppCD;
        // map [0,3] to [1,4]
        int layCDM = layCDL + 1;
        // map [3,0] to [4,1]
        int oppCDM = oppCDL + 1;
        // map [0,3] to [2,5]
        int layCDR = layCDL + 2;
        // map [3,0] to [5,2]
        int oppCDR = oppCDL + 2;
        String style = "S";
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

//    void drawCanvasTime(int secSel,
//            int laySel,
//            int comSel) {
//
//        // map [1,2] to [0,1]
//        int layCD = laySel - 1;
//        // map [1,2] to [1,0]
//        int oppCD = laySel % 2;
//        // map [1,0] to [2,1]
//        int oppSel = oppCD + 1;
//
//        // map [0,1] to [0,3]
//        int layCDL = 3 * layCD;
//        // map [1,0] to [3,0]
//        int oppCDL = 3 * oppCD;
//
//        // map [0,3] to [1,4]
//        int layCDM = layCDL + 1;
//        // map [3,0] to [4,1]
//        int oppCDM = oppCDL + 1;
//
//        // map [0,3] to [2,5]
//        int layCDR = layCDL + 2;
//        // map [3,0] to [5,2]
//        int oppCDR = oppCDL + 2;
//
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasTime.cd(layCDL);
//
//        if (histogramsFTHodo.H_MAXV_VS_T.hasEntry(secSel,laySel,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_MAXV_VS_T.get(secSel,laySel,comSel));
//        }
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasTime.cd(oppCDL);
//        if (histogramsFTHodo.H_MAXV_VS_T.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_MAXV_VS_T.get(secSel,oppSel,comSel));
//        }
//        //----------------------------------------
//        // middle top
//        canvasTime.cd(layCDM);
//        if (histogramsFTHodo.H_T_MODE7.hasEntry(secSel,laySel,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_T_MODE7.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fT.hasEntry(secSel,laySel,comSel)) {
//                this.canvasTime.draw(histogramsFTHodo.fT.get(secSel,laySel,comSel), "same S");
//            }
//        }
//
//        //----------------------------------------
//        // middle bottom
//        canvasTime.cd(oppCDM);
//        if (histogramsFTHodo.H_T_MODE7.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_T_MODE7.get(secSel,oppSel,comSel));
//            if (histogramsFTHodo.fT.hasEntry(secSel,oppSel,comSel)) {
//                this.canvasTime.draw(histogramsFTHodo.fT.get(secSel,oppSel,comSel), "same S");
//            }
//
//        }
//        //----------------------------------------
//        // right top
//        canvasTime.cd(2);
//        if (histogramsFTHodo.H_DT_MODE7.hasEntry(secSel,1,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_DT_MODE7.get(secSel,1,comSel));
//            //         if(fT.hasEntry(secSel,
//            //                laySel,
//            //                comSel))
//            //                 this.canvasTime.draw(fT.get(secSel,
//            //                         laySel,
//            //                         comSel),"same S");
//        }
//        //----------------------------------------
//        // right bottom
//        canvasTime.cd(5);
//        if (histogramsFTHodo.H_T1_T2.hasEntry(secSel,1,comSel)) {
//            this.canvasTime.draw(histogramsFTHodo.H_T1_T2.get(secSel,1,comSel));
//        }
//    }

    
    void drawCanvasMIPsignal(int secSel,int laySel,int comSel) {
        // map [1,2] to [0,1]
        int layCD = laySel - 1;
        // map [1,2] to [1,0]
        int oppCD = laySel % 2;
        // map [1,2] to [2,1]
        int oppSel = (laySel % 2) + 1;
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
                    //if (histogramsFTHodo.fQMIP.hasEntry(secSel,laySel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,laySel,comSel), "same S");
                    //}
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
                    //if (histogramsFTHodo.fQMIP.hasEntry(secSel,oppSel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,oppSel,comSel), "same S");
                    //}
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
                    //if (histogramsFTHodo.fVMIP.hasEntry(secSel,laySel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,laySel,comSel), "same S");
                    //}
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
                    //if (histogramsFTHodo.fVMIP.hasEntry(secSel,oppSel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,oppSel,comSel), "same S");
                    //}
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
                    //if (histogramsFTHodo.fVMIP.hasEntry(secSel,laySel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,laySel,comSel), "same S");
                    //}
                }
                //----------------------------------------
                // left top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDL);
                if (histogramsFTHodo.H_MIP_V_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_V_MatchingTiles.get(secSel,oppSel,comSel));
                    //if (histogramsFTHodo.fVMIP.hasEntry(secSel,oppSel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fVMIP.get(secSel,oppSel,comSel), "same S");
                    //}
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(layCDR);
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,laySel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,laySel,comSel));
                    //if (histogramsFTHodo.fQMIP.hasEntry(secSel,laySel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,laySel,comSel), "same S");
                    //}
                }
                //----------------------------------------
                // right top (bottom) for thin (thick) layer
                canvasMIPsignal.cd(oppCDR);
                if (histogramsFTHodo.H_MIP_Q_MatchingTiles.hasEntry(secSel,oppSel,comSel)) {
                    this.canvasMIPsignal.draw(histogramsFTHodo.H_MIP_Q_MatchingTiles.get(secSel,oppSel,comSel));
                    //if (histogramsFTHodo.fQMIP.hasEntry(secSel,oppSel,comSel)) {
                    //    this.canvasMIPsignal.draw(histogramsFTHodo.fQMIP.get(secSel,oppSel,comSel), "same S");
                    //}
                }
            }
            
            
        }
    }
    
//    void drawCanvasCharge(int secSel,
//            int laySel,
//            int comSel) {
//        // map [1,2] to [0,1]
//        int layCD = laySel - 1;
//        // map [1,2] to [1,0]
//        int oppCD = laySel % 2;
//        // map [1,2] to [2,1]
//        int oppSel = (laySel % 2) + 1;
//        int layCDL = 2 * layCD;
//        int oppCDL = 2 * oppCD;
//        int layCDR = layCDL + 1;
//        int oppCDR = oppCDL + 1;
//
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasCharge.cd(layCDL);
//        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel, laySel, comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fQ1.hasEntry(secSel,laySel,comSel)) {
//                this.canvasCharge.draw(histogramsFTHodo.fQ1.get(secSel,laySel,comSel), "same S");
//            }
//            if (histogramsFTHodo.fitTwoPeaksQ
//                    && histogramsFTHodo.fQ2.hasEntry(secSel,laySel,comSel)) {
//                this.canvasCharge.draw(histogramsFTHodo.fQ2.get(secSel,laySel,comSel), "same S");
//            }
//
//        }
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasCharge.cd(oppCDL);
//        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,oppSel,comSel));
//        }
//        if (histogramsFTHodo.fQ1.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.fQ1.get(secSel,oppSel,comSel), "same S");
//        }
//        if (histogramsFTHodo.fitTwoPeaksQ
//                && histogramsFTHodo.fQ2.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.fQ2.get(secSel,oppSel,comSel), "same S");
//        }
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasCharge.cd(layCDR);
//        if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,laySel,comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.H_MIP_Q.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fQMIP.hasEntry(secSel,laySel,comSel)) {
//                this.canvasCharge.draw(histogramsFTHodo.fQMIP.get(secSel,laySel,comSel), "same S");
//            }
//        }
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasCharge.cd(oppCDR);
//        if (histogramsFTHodo.H_MIP_Q.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasCharge.draw(histogramsFTHodo.H_MIP_Q.get(secSel,oppSel,comSel));
//            if (histogramsFTHodo.fQMIP.hasEntry(secSel,oppSel,comSel)) {
//                this.canvasCharge.draw(histogramsFTHodo.fQMIP.get(secSel,oppSel,comSel), "same S");
//            }
//        }
//    }

//    void drawCanvasVoltage(int secSel,
//        int laySel,
//        int comSel) {
//        // map [1,2] to [0,1]
//        int layCD = laySel - 1;
//        // map [1,2] to [1,0]
//        int oppCD = laySel % 2;
//        // map [1,2] to [2,1]
//        int oppSel = (laySel % 2) + 1;
//        int layCDL = 2 * layCD;
//        int oppCDL = 2 * oppCD;
//        int layCDR = layCDL + 1;
//        int oppCDR = oppCDL + 1;
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasVoltage.cd(layCDL);
//        if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel, laySel, comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.H_NOISE_V.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fV1.hasEntry(secSel,laySel,comSel)) {
//                this.canvasVoltage.draw(histogramsFTHodo.fV1.get(secSel,laySel,comSel), "same S");
//            }
//            if (histogramsFTHodo.fitTwoPeaksV
//                    && histogramsFTHodo.fV2.hasEntry(secSel,laySel,comSel)) {
//                this.canvasVoltage.draw(histogramsFTHodo.fV2.get(secSel,laySel,comSel), "same S");
//            }
//        }
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasVoltage.cd(oppCDL);
//        if (histogramsFTHodo.H_NOISE_V.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.H_NOISE_V.get(secSel,oppSel,comSel));
//        }
//        if (histogramsFTHodo.fV1.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.fV1.get(secSel,oppSel,comSel), "same S");
//        }
//        if (histogramsFTHodo.fitTwoPeaksV
//                && histogramsFTHodo.fV2.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.fV2.get(secSel,oppSel,comSel), "same S");
//        }
//
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasVoltage.cd(layCDR);
//        if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,laySel,comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.H_MIP_V.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fVMIP.hasEntry(secSel,laySel,comSel)) {
//                this.canvasVoltage.draw(histogramsFTHodo.fVMIP.get(secSel,laySel,comSel), "same S");
//            }
//        }
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasVoltage.cd(oppCDR);
//        if (histogramsFTHodo.H_MIP_V.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasVoltage.draw(histogramsFTHodo.H_MIP_V.get(secSel,oppSel,comSel));
//            if (histogramsFTHodo.fVMIP.hasEntry(secSel,oppSel,comSel)) {
//                this.canvasVoltage.draw(histogramsFTHodo.fVMIP.get(secSel,oppSel,comSel), "same S");
//            }
//        }
//    }

//    void drawCanvasMatch(int secSel,int laySel,int comSel) {
//        // map [1,2] to [0,1]
//        int layCD = laySel - 1;
//        // map [1,2] to [1,0]
//        int oppCD = laySel % 2;
//        // map [1,2] to [2,1]
//        int oppSel = (laySel % 2) + 1;
//        int layCDL = 2 * layCD;
//        int oppCDL = 2 * oppCD;
//        int layCDR = layCDL + 1;
//        int oppCDR = oppCDL + 1;
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasMatch.cd(layCDL);
//        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel, laySel, comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,laySel,comSel));
//            if (histogramsFTHodo.fQ1.hasEntry(secSel,laySel,comSel)) {
//                this.canvasMatch.draw(histogramsFTHodo.fQ1.get(secSel,laySel,comSel), "same S");
//            }
//            if (histogramsFTHodo.fitTwoPeaksQ
//                    && histogramsFTHodo.fQ2.hasEntry(secSel,laySel,comSel)) {
//                this.canvasMatch.draw(histogramsFTHodo.fQ2.get(secSel,laySel,comSel), "same S");
//            }
//
//        }
//        //----------------------------------------
//        // left top (bottom) for thin (thick) layer
//        canvasMatch.cd(oppCDL);
//        if (histogramsFTHodo.H_NOISE_Q.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.H_NOISE_Q.get(secSel,oppSel,comSel));
//        }
//        if (histogramsFTHodo.fQ1.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.fQ1.get(secSel,oppSel,comSel), "same S");
//        }
//        if (histogramsFTHodo.fitTwoPeaksQ
//                && histogramsFTHodo.fQ2.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.fQ2.get(secSel,oppSel,comSel), "same S");
//        }
//
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasMatch.cd(layCDR);
//        if (histogramsFTHodo.H_NPE_MATCH.hasEntry(secSel,laySel,comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.H_NPE_MATCH.get(secSel,laySel,comSel));
//        }
//        //----------------------------------------
//        // right top (bottom) for thin (thick) layer
//        canvasMatch.cd(oppCDR);
//        if (histogramsFTHodo.H_NPE_MATCH.hasEntry(secSel,oppSel,comSel)) {
//            this.canvasMatch.draw(histogramsFTHodo.H_NPE_MATCH.get(secSel,oppSel,comSel));
//        }
//    }


//    void drawCanvasMIP(int secSel,int laySel,int comSel) {
//        if (secSel == 0) {
//            return;
//        }
//        int sector2CD[] = {4, 0, 1, 2, 5, 8, 7, 6, 3};
//        canvasMIP.cd(sector2CD[secSel]);
//        GraphErrors[] G_NPE;
//        int p30EvenI[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
//        int p15EvenI[] = {13, 14, 15, 16, 17, 18, 19, 20};
//        int p30OddI[] = {2, 4, 5, 6, 7, 8};
//        int p15OddI[] = {1, 3, 9};
//        double p30EvenD[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
//        double p15EvenD[] = {13, 14, 15, 16, 17, 18, 19, 20};
//        double p30OddD[] = {2, 4, 5, 6, 7, 8};
//        double p15OddD[] = {1, 3, 9};
//        // Was a P30 or a P15 tile selected?
//        boolean plotP30 = true;
//        boolean evenSecSelect = true;
//        if (secSel % 2 == 1) {
//            evenSecSelect = false;
//        }
//
//        if (evenSecSelect) {
//            for (int i = 0; i < p15EvenI.length; i++) {
//                if (comSel == p15EvenI[i]) {
//                    plotP30 = false;
//                    break;
//                }
//            }
//        } else {
//            for (int i = 0; i < p15OddI.length; i++) {
//                if (comSel == p15OddI[i]) {
//                    plotP30 = false;
//                    break;
//                }
//            }
//        }
//
//        double p30EvenE[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        double p15EvenE[] = {0, 0, 0, 0, 0, 0, 0, 0};
//        double p30OddE[] = {0, 0, 0, 0, 0, 0};
//        double p15OddE[] = {0, 0, 0};
//
//        double p30EvenNPE[][] = new double[2][12];
//        double p30EvenERR[][] = new double[2][12];
//        double p15EvenNPE[][] = new double[2][8];
//        double p15EvenERR[][] = new double[2][8];
//        double p30OddNPE[][] = new double[2][6];
//        double p30OddERR[][] = new double[2][6];
//        double p15OddNPE[][] = new double[2][3];
//        double p15OddERR[][] = new double[2][3];
//
//        if (useGain_mV) {
//            for (int lM = 0; lM < 2; lM++) {
//                for (int c = 0; c < p30EvenI.length; c++) {
//                    p30EvenNPE[lM][c] = meanNPE_mV[secSel][lM + 1][p30EvenI[c]];
//                    p30EvenERR[lM][c] = errNPE_mV[secSel][lM + 1][p30EvenI[c]];
//                }
//                for (int c = 0; c < p15EvenI.length; c++) {
//                    p15EvenNPE[lM][c] = meanNPE_mV[secSel][lM + 1][p15EvenI[c]];
//                    p15EvenERR[lM][c] = errNPE_mV[secSel][lM + 1][p15EvenI[c]];
//                }
//
//                for (int c = 0; c < p30OddI.length; c++) {
//                    p30OddNPE[lM][c] = meanNPE_mV[secSel][lM + 1][p30OddI[c]];
//                    p30OddERR[lM][c] = errNPE_mV[secSel][lM + 1][p30OddI[c]];
//                }
//                for (int c = 0; c < p15OddI.length; c++) {
//                    p15OddNPE[lM][c] = meanNPE_mV[secSel][lM + 1][p15OddI[c]];
//                    p15OddERR[lM][c] = errNPE_mV[secSel][lM + 1][p15OddI[c]];
//                }
//            }
//        } else {
//            for (int lM = 0; lM < 2; lM++) {
//                for (int c = 0; c < p30EvenI.length; c++) {
//                    p30EvenNPE[lM][c] = meanNPE[secSel][lM + 1][p30EvenI[c]];
//                    p30EvenERR[lM][c] = errNPE[secSel][lM + 1][p30EvenI[c]];
//                }
//                for (int c = 0; c < p15EvenI.length; c++) {
//                    p15EvenNPE[lM][c] = meanNPE[secSel][lM + 1][p15EvenI[c]];
//                    p15EvenERR[lM][c] = errNPE[secSel][lM + 1][p15EvenI[c]];
//                }
//
//                for (int c = 0; c < p30OddI.length; c++) {
//                    p30OddNPE[lM][c] = meanNPE[secSel][lM + 1][p30OddI[c]];
//                    p30OddERR[lM][c] = errNPE[secSel][lM + 1][p30OddI[c]];
//                }
//                for (int c = 0; c < p15OddI.length; c++) {
//                    p15OddNPE[lM][c] = meanNPE[secSel][lM + 1][p15OddI[c]];
//                    p15OddERR[lM][c] = errNPE[secSel][lM + 1][p15OddI[c]];
//                }
//            }
//        }
//
//        G_NPE = new GraphErrors[2];
//        for (int layerM = 0; layerM < 2; layerM++) {
//            if (plotP30) {
//                if (evenSecSelect) {
//                    G_NPE[layerM] = new GraphErrors("p30Even",
//                            p30EvenD,
//                            p30EvenNPE[layerM],
//                            p30EvenE,
//                            p30EvenERR[layerM]);
//                } else {
//                    G_NPE[layerM] = new GraphErrors("p30Odd",
//                            p30OddD,
//                            p30OddNPE[layerM],
//                            p30OddE,
//                            p30OddERR[layerM]);
//                }
//            } else {
//                if (evenSecSelect) {
//                    G_NPE[layerM] = new GraphErrors("p15Even",
//                            p15EvenD,
//                            p15EvenNPE[layerM],
//                            p15EvenE,
//                            p15EvenERR[layerM]);
//                } else {
//                    G_NPE[layerM] = new GraphErrors("p15Odd",
//                            p15OddD,
//                            p15OddNPE[layerM],
//                            p15OddE,
//                            p15OddERR[layerM]);
//                }
//            }
//
//            String title;
//            title = "sector " + secSel;
//            G_NPE[layerM].setTitle(title);
//            G_NPE[layerM].setTitleX("component");
//            G_NPE[layerM].setTitleY("NPE mean ");
//            G_NPE[layerM].setMarkerSize(5);
//            G_NPE[layerM].setMarkerColor(layerM + 1); // 0-9 for given palette
//            G_NPE[layerM].setMarkerStyle(layerM + 1); // 1 or 2
//            G_NPE[layerM].setMarkerSize(10); // 1 or 2
//
//        }
//        canvasMIP.draw(G_NPE[0]);
//        canvasMIP.draw(G_NPE[1], "same");
//        System.out.println(" Marker Size = " + G_NPE[0].getMarkerSize());
//    }

//    void drawCanvasMIPElec(int s, int l, int c) {
//        if (s == 0 || l == 0) {
//            return;
//        }
//        canvasMIP.divide(1, 1);
//        GraphErrors G_NPE;
//
//        int mezSel = wireFTHodo.getMezz4SLC(s, l, c);
//
//        double[] chanArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
//        double[] chanErr ={0, 0, 0, 0,0, 0, 0, 0,0, 0, 0, 0,0, 0, 0, 0};
//        double[] npeArr = new double[16];
//        double[] npeErr = new double[16];
//
//        int sect;
//        int comp;
//        int laye;
//
//        if (useGain_mV) {
//            for (int chan = 0; chan < 16; chan++) {
//                sect = wireFTHodo.getSect4ChMez(chan, mezSel);
//                comp = wireFTHodo.getComp4ChMez(chan, mezSel);
//                laye = chan / 8 + 1;
//
//                npeArr[chan] = meanNPE_mV[sect][laye][comp];
//                npeErr[chan] = errNPE_mV[sect][laye][comp];
//
//            }
//        } else {
//            for (int chan = 0; chan < 16; chan++) {
//                sect = wireFTHodo.getSect4ChMez(chan, mezSel);
//                comp = wireFTHodo.getComp4ChMez(chan, mezSel);
//                laye = chan / 8 + 1;
//
//                npeArr[chan] = getNPEMean(sect, laye, comp);
//                npeErr[chan] = getNPEError(sect, laye, comp);
//            }
//        }
//
//        for (int chan = 0; chan < 16; chan++) {
//            if (npeErr[chan] > 20.0
//                    || npeArr[chan] < 20.0) {
//
//                npeErr[chan] = 20.0;
//
//                if (chan < 8) {
//                    npeArr[chan] = 40.0;
//                } else {
//                    npeArr[chan] = 60.0;
//                }
//            }
//        }
//
//        G_NPE = new GraphErrors("G_NPE", chanArr, npeArr,
//                chanErr, npeErr);
//
//        String title;
//        title = "mezzanine " + mezSel;
//        G_NPE.setTitle(title);
//        G_NPE.setTitleX("channel");
//        G_NPE.setTitleY("NPE mean");
//        G_NPE.setMarkerSize(5);
//        G_NPE.setMarkerColor(1);
//        G_NPE.setMarkerStyle(1);
//        G_NPE.setMarkerSize(10);
//        canvasMIP.draw(G_NPE);
//
//    }

    void drawCanvasGain() {
        this.canvasGain.divide(3, 3);
        int sector2CD[] = {0,1,2,5,8,7,6,3};
        for (int isec=0; isec<8; isec++){
            canvasGain.cd(sector2CD[isec]);
      
            if (useGain_mV){
               if (isec%2==0)
                    canvasGain.draw( histogramsFTHodo.H_EMPTYGAIN_MV9);
               else
                    canvasGain.draw(histogramsFTHodo.H_EMPTYGAIN_MV20);
                canvasGain.draw(histogramsFTHodo.GGgainDetectorV[0][isec],"same");
                canvasGain.draw(histogramsFTHodo.GGgainDetectorV[1][isec],"same");
            }
            else {
                if (isec%2==0)
                    canvasGain.draw(histogramsFTHodo.H_EMPTYGAIN_PC9);
               else
                    canvasGain.draw(histogramsFTHodo.H_EMPTYGAIN_PC20);
                canvasGain.draw(histogramsFTHodo.GGgainDetectorC[0][isec],"same");
                canvasGain.draw(histogramsFTHodo.GGgainDetectorC[1][isec],"same");
            }
        }
//        if (s == 0){
//            return;}
//        boolean evenSecSelect = true;
//        String sectors[] = new String[8];
//        if (s % 2 == 1){
//            evenSecSelect = false;}
//
//        int sector2CD[] = {4, 0, 1, 2, 5, 8, 7, 6, 3};
//        canvasGain.cd(sector2CD[s]);
//        int evenI[] = {1, 2, 3, 4, 5,6, 7, 8, 9, 10,11, 12, 13, 14, 15,16, 17, 18, 19, 20};
//        double evenD[] = {1, 2, 3, 4, 5,6, 7, 8, 9, 10,11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
//        int oddI[] = {1, 2, 3, 4, 5,6, 7, 8, 9};
//        double oddD[] = {1, 2, 3, 4, 5,6, 7, 8, 9};
//        double evenE[] = {0, 0, 0, 0, 0,0, 0, 0, 0, 0,0, 0, 0, 0, 0,0, 0, 0, 0, 0};
//        double oddE[] = {0, 0, 0, 0, 0,0, 0, 0, 0};
//        double evenGain[][] = new double[2][20];
//        double evenGainErr[][] = new double[2][20];
//        double oddGain[][] = new double[2][9];
//        double oddGainErr[][] = new double[2][9];
//        GraphErrors[] G_Gain;
//        G_Gain = new GraphErrors[2];
//        String yTitle = "gain (pC)";
//        double maxErr = 10.;
//        if (useGain_mV) {
//            yTitle = "gain (mV)";
//            maxErr = 5.0;
//        }
//        for (int lM = 0; lM < 2; lM++) {
//            // loop over even indices
//            for (int c = 0; c < evenI.length; c++) {
//                if (!useGain_mV) {
//                    evenGain[lM][c] = gain[s][lM + 1][evenI[c]];
//                    evenGainErr[lM][c] = errGain[s][lM + 1][evenI[c]];
//                } else {
//                    evenGain[lM][c] = gain_mV[s][lM + 1][evenI[c]];
//                    evenGainErr[lM][c] = errGain_mV[s][lM + 1][evenI[c]];
//                }
//                if (evenGainErr[lM][c] > maxErr) {
//                    evenGain[lM][c] = 0.0;
//                    evenGainErr[lM][c] = 0.0;
//                }
//            }
//            // loop over odd indices
//            for (int c = 0; c < oddI.length; c++) {
//                if (!useGain_mV) {
//                    oddGain[lM][c] = gain[s][lM + 1][oddI[c]];
//                    oddGainErr[lM][c] = errGain[s][lM + 1][oddI[c]];
//                } else {
//                    oddGain[lM][c] = gain_mV[s][lM + 1][oddI[c]];
//                    oddGainErr[lM][c] = errGain_mV[s][lM + 1][oddI[c]];
//                }
//                if (oddGainErr[lM][c] > maxErr) {
//                    oddGain[lM][c] = 0.0;
//                    oddGainErr[lM][c] = 0.0;
//                }
//            }
//            if (evenSecSelect) {
//                G_Gain[lM] = new GraphErrors("G_Gain",
//                        evenD,
//                        evenGain[lM],
//                        evenE,
//                        evenGainErr[lM]);
//            } else {
//                G_Gain[lM] = new GraphErrors("G_Gain",
//                        oddD,
//                        oddGain[lM],
//                        oddE,
//                        oddGainErr[lM]);
//            }
//            String title;
//            title = "sector " + s;
//            G_Gain[lM].setTitle(title);
//            G_Gain[lM].setTitleX("component");
//            G_Gain[lM].setTitleY(yTitle);
//            G_Gain[lM].setMarkerSize(5);
//            G_Gain[lM].setMarkerColor(lM + 1); // 0-9 for given palette
//            G_Gain[lM].setMarkerStyle(lM + 1); // 1 or 2
//        }
//
//        int nXBins[] = {20, 9};
//        int nYBins = 100;
//        double[] xLimits = {0.5, (double) nXBins[s % 2] + 0.5};
//        double[] yLimits = {5.0, 30.};
//        H1F H1 = new H1F("H1", "component", "gain (pC)",
//                nXBins[s % 2], xLimits[0], xLimits[1]);
//        H1.setTitleY(yTitle);
//        canvasGain.draw(H1);
//        canvasGain.draw(G_Gain[0], "same");
//        canvasGain.draw(G_Gain[1], "same");
//        H1.getYaxis().set(nYBins, yLimits[0], yLimits[1]);
//        canvasGain.draw(H1, "same");
    } // end: drawCanvasGain.....
    
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
        }

        for (int ilay = 0; ilay < 2; ilay++) {
            double[] gainArr;
            double[] gainErrArr;
            double[] gainArrmV;
            double[] gainErrArrmV;
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
                chanArr = new double[elemntsInSec];
                chanErrArr = new double[elemntsInSec];
                for (int chan = 0; chan < elemntsInSec; chan++) {
                    gainArr[chan] = gain[isec+1][ilay+1][chan+1];
                    gainErrArr[chan] = errGain[isec+1][ilay+1][chan+1];
                    gainArrmV[chan] = gain_mV[isec+1][ilay+1][chan+1];
                    gainErrArrmV[chan] = errGain_mV[isec+1][ilay+1][chan+1];
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
            }
        }
    }
    
    void drawCanvasGainElec(int secSel,int laySel,int comSel) {
        if (secSel == 0 || laySel == 0) {
            return;
        }
        canvasGain.divide(1, 1);
        int mezz = wireFTHodo.getMezz4SLC(secSel, laySel, comSel);
        canvasGain.cd(0);
        if (useGain_mV){
            canvasGain.draw(histogramsFTHodo.H_EMPTYGAIN_ELE_MV);
            canvasGain.draw(histogramsFTHodo.GGgainElectronicsV[mezz],"same");
        }
        else{
            canvasGain.draw(histogramsFTHodo.H_EMPTYGAIN_ELE_PC);
            canvasGain.draw(histogramsFTHodo.GGgainElectronicsC[mezz],"same");

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
        Color pedColor = palette.getColor3D(pedMean[sec][lay][com],400,true);
        Color noiseColor = palette.getColor3D(vMax[sec][lay][com],2 * histogramsFTHodo.nGain_mV,false);
        Color gainColor;
        if (useGain_mV) {
            gainColor = palette.getColor3D(gain_mV[sec][lay][com],1.0 * histogramsFTHodo.nGain_mV,true);
        } else {
            gainColor = palette.getColor3D(gain[sec][lay][com],1.0 * histogramsFTHodo.nGain,true);
        }
        Color voltColor = palette.getColor3D(vMax[sec][lay][com],12 * histogramsFTHodo.nGain_mV,true);
        Color qColor = palette.getColor3D(qMax[sec][lay][com],250 * histogramsFTHodo.nGain,true);

        if (tabSel == tabIndexEvent) {
            if (waveMax > histogramsFTHodo.cosmicsThrsh) {
                shape.setColor(0, 255, 0, signalAlpha);
            } else if (waveMax > histogramsFTHodo.noiseThrsh) {
                shape.setColor(255, 255, 0, noiseAlpha);
            } else {
                shape.setColor(255, 255, 255, 0);
            }
        } else if (tabSel == tabIndexPed) {
            shape.setColor(pedColor.getRed(),pedColor.getGreen(),pedColor.getBlue());
        } else if (tabSel == tabIndexNoise) {
            shape.setColor(noiseColor.getRed(),noiseColor.getGreen(),noiseColor.getBlue());
        } else if (tabSel == tabIndexGain) {
            shape.setColor(gainColor.getRed(),gainColor.getGreen(),gainColor.getBlue());
        } 
//        else if (tabSel == tabIndexVoltage) {
//            shape.setColor(voltColor.getRed(),
//                    voltColor.getGreen(),
//                    voltColor.getBlue());
//        }
//        else if (tabSel == tabIndexCharge) {
//            shape.setColor(qColor.getRed(),
//                    qColor.getGreen(),
//                    qColor.getBlue());
//        }  else if (tabSel == tabIndexMIP
//                || tabSel == tabIndexCharge) {
//            if (waveMax > histogramsFTHodo.cosmicsThrsh) {
//                shape.setColor(0, 255, 0, (256 / 4) - 1);
//            } else if (waveMax > histogramsFTHodo.cosmicsThrsh * 1.5) {
//                shape.setColor(0, 255, 0, (256 / 2) - 1);
//            } else if (waveMax > histogramsFTHodo.cosmicsThrsh * 2.0) {
//                shape.setColor(0, 255, 0, 255);
//            }
//        }
    } // end of : public void update(Detec

    //--------------------------------------------
    // Constants
    private double getStatus(int s, int l, int c) {
        return status[s][l][c];
    }

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

    private double getPedMean(int s, int l, int c) {
        return pedMean[s][l][c];
    }

    private void setPedMean(int s, int l, int c) {
        if (setConstantsToCCDB) {
            String line[] = readTable(s, l, c,ccdbFileName,13);
            pedMean[s][l][c] = Double.parseDouble(line[1]);
        } else if (histogramsFTHodo.fPed.hasEntry(s, l, c)) {
            pedMean[s][l][c] = histogramsFTHodo.fPed.get(s, l, c).getParameter(1);
        } else {
            pedMean[s][l][c] = histogramsFTHodo.nPedMean;
        }
    }
    private double getPedRMS(int s, int l, int c) {
        return pedRMS[s][l][c];
    }
    private void setPedRMS(int s, int l, int c) {
        pedRMS[s][l][c] = histogramsFTHodo.nPedRMS;
    }

    private void setGain(int s, int l, int c, String gainstring) {
        double thisGain = 0.0;
        if (histogramsFTHodo.useDefaultGain) {
            if (gainstring=="charge")
                thisGain = histogramsFTHodo.nGain;
            else if (gainstring=="peakvolt")
                thisGain = histogramsFTHodo.nGain_mV;
        }
        else {
            if (gainstring=="charge"){
                if (histogramsFTHodo.fQ2.hasEntry(s, l, c)) {
                    double n2 = histogramsFTHodo.fQ2.get(s, l, c).getParameter(4);
                    double n1 = histogramsFTHodo.fQ2.get(s, l, c).getParameter(1);
                    thisGain = n2 - n1;
                }
                else {
                    thisGain = 0.0;
                }
            }
            else if (gainstring=="peakvolt"){
                if (histogramsFTHodo.fV2.hasEntry(s, l, c)) {
                    double n2 = histogramsFTHodo.fV2.get(s, l, c).getParameter(4);
                    double n1 = histogramsFTHodo.fV2.get(s, l, c).getParameter(1);
                    thisGain = n2 - n1;
                }
                else {
                    thisGain = 0.0;
                }
            }
        }
        if (gainstring=="charge"){
            gain[s][l][c] = thisGain;
            if (thisGain < 15.0|| thisGain > 30.0) {
                //setStatus(s, l, c);
            }
        }
        else if (gainstring=="peakvolt"){
            gain_mV[s][l][c] = thisGain;
            if (thisGain < 7.0|| thisGain > 15.0) {
                setStatus(s, l, c);
            }
        }
    }

    private double getGain(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return gain[s][l][c];
        else
            return gain_mV[s][l][c];
    }
    
    
    private void setGainError(int s, int l, int c, String gainstring) {
        double gainError = 0.0;
        if (histogramsFTHodo.useDefaultGain) {
            gainError = 0.0;
        } else {
            if (gainstring=="charge"){
                if (histogramsFTHodo.fQ2.hasEntry(s, l, c)
                    && getGain(s, l, c, "charge") > 0.0) {
                    double n2Error = histogramsFTHodo.fQ2.get(s, l, c).parameter(4).error();
                    double n1Error = histogramsFTHodo.fQ2.get(s, l, c).parameter(1).error();
                    gainError = n2Error * n2Error + n1Error * n1Error;
                    gainError = sqrt(gainError);
                }
                else {
                    gainError = 0.0;
                }
            }
            else if (gainstring=="peakvolt"){
                if (histogramsFTHodo.fV2.hasEntry(s, l, c)
                    && getGain(s, l, c, "peakvolt") > 0.0) {
                    double n2Error = histogramsFTHodo.fV2.get(s, l, c).parameter(4).error();
                    double n1Error = histogramsFTHodo.fV2.get(s, l, c).parameter(1).error();
                    gainError = n2Error * n2Error + n1Error * n1Error;
                    gainError = sqrt(gainError);
                }
                else {
                    gainError = 0.0;
                }
            }
        }
        if (gainstring=="charge")
            errGain[s][l][c] = gainError;
        else if (gainstring=="peakvolt")
            errGain_mV[s][l][c] = gainError;
    }
    
    private double getGainError(int s, int l, int c, String gainstring) {
        if (gainstring=="charge")
            return errGain[s][l][c];
        else
            return errGain_mV[s][l][c];
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

//    private void setNPEMean(int s, int l, int c) {
//        if (getGain(s, l, c) > 0.0) {
//            meanNPE[s][l][c] = getQMean(s, l, c) / getGain(s, l, c);
//        } else {
//            meanNPE[s][l][c] = 0.0;
//        }
//    }
//
//    private double getNPEMean(int s, int l, int c) {
//        return meanNPE[s][l][c];
//    }
//
//    private void setSigNPE(int s, int l, int c) {
//        sigNPE[s][l][c] = 10.0;
//    }
//
//    private double getSigNPE(int s, int l, int c) {
//        return sigNPE[s][l][c];
//    }
//
//    private void setNPEError(int s, int l, int c) {
//
//        double npeError = 0.0;
//
//        if (getQMean(s, l, c) > 0.0
//                && getGain(s, l, c) > 0.0) {
//
//            npeError = getQMeanError(s, l, c) * getQMeanError(s, l, c);
//
//            npeError = npeError / (getQMean(s, l, c) * getQMean(s, l, c));
//            npeError = npeError
//                    + (getGainError(s, l, c) * getGainError(s, l, c)
//                    / (getGain(s, l, c) * getGain(s, l, c)));
//
//            npeError = sqrt(npeError);
//            npeError = getNPEMean(s, l, c) * npeError;
//        }
//        errNPE[s][l][c] = npeError;
//    }
//
//    private double getNPEError(int s, int l, int c) {
//        return errNPE[s][l][c];
//    }
//
//    private void setNPEMean_mV(int s, int l, int c) {
//        if (getGain_mV(s, l, c) > 0.0) {
//            meanNPE_mV[s][l][c] = getVMean(s, l, c) / getGain_mV(s, l, c);
//        } else {
//            meanNPE_mV[s][l][c] = 0.0;
//        }
//    }
//
//    private double getNPEMean_mV(int s, int l, int c) {
//        return meanNPE_mV[s][l][c];
//    }
//
//    private double getNPEErr_mV(int s, int l, int c) {
//        return errNPE_mV[s][l][c];
//    }
//    private void setNPEErr_mV(int s, int l, int c) {
//
//        double npeErr_mV = 0.0;
//
//        if (getVMean(s, l, c) > 0.0
//                && getGain_mV(s, l, c) > 0.0) {
//
//            npeErr_mV = getVMeanError(s, l, c) * getVMeanError(s, l, c);
//
//            npeErr_mV = npeErr_mV / (getVMean(s, l, c) * getVMean(s, l, c));
//            npeErr_mV = npeErr_mV
//                    + (getGainErr_mV(s, l, c) * getGainErr_mV(s, l, c)
//                    / (getGain_mV(s, l, c) * getGain_mV(s, l, c)));
//
//            npeErr_mV = sqrt(npeErr_mV);
//            npeErr_mV = getNPEMean_mV(s, l, c) * npeErr_mV;
//        }
//
//        errNPE_mV[s][l][c] = npeErr_mV;
//
//    }

    public void updateArrays() {
        System.out.println(" Setting arrays");
        for (int s = 1; s < 9; s++) {
            for (int l = 1; l < 3; l++) {
                for (int c = 1; c < 21; c++) {
                    if (s % 2 == 1 && c > 9) {
                        continue;
                    }
                    setPedMean(s, l, c);
                    setPedRMS(s, l, c);
                    setGain(s, l, c, "charge");
                    setGain(s, l, c, "peakvolt");
                    setGainError(s, l, c,"charge");
                    setGainError(s, l, c,"peakvolt");
                    //setStatus(s, l, c);
                }
            }
        }
        System.out.println(" Arrays Set");
    } // end of: private void updateTable() {

    
    
    
    
    
    
//    public void updateConstants() {
//
//        int index;
//        double mipE = 0.0;
//        double mipC = 0.0;
//        String values[];
//        for (int s = 1; s < 9; s++) {
//            for (int l = 1; l < 3; l++) {
//                for (int c = 1; c < 21; c++) {
//                    if (s % 2 == 1 && c > 9) {
//                        continue;
//                    }
//                    index = getIndex4SLC(s, l, c);
//                    //------------------------------
//                    // set constants
//                    //------------------------------
//                    setPedMean(s, l, c);
//                    setPedRMS(s, l, c);
//                    setNPEMean(s, l, c);
//                    setNPEError(s, l, c);
//                    setSigNPE(s, l, c);
//                    setNPEMean_mV(s, l, c);
//                    setNPEErr_mV(s, l, c);
//                    //setGain(s, l, c);
//                    //setGainError(s, l, c);
//                    //setGainError(s, l, c);
//                    //setGain_mV(s, l, c);
//                    //setGainErr_mV(s, l, c);
//                    //setGain(s, l, c);
//                    //
//                    setStatus(s, l, c);
//                    //---------------------------------
//                    // Update the table
//                    ccdbTable.setDoubleValue(
//                            getStatus(s, l, c),
//                            "status",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getPedMean(s, l, c),
//                            "ped",
//                            s, l, c);
//
//                    ccdbTable.setDoubleValue(
//                            getPedRMS(s, l, c),
//                            "ped_rms",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getGain(s, l, c),
//                            "gain_pc",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getGain_mV(s, l, c),
//                            "gain_mv",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getThrshNPE(s, l, c),
//                            "thr_npe",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getE(s, l, c),
//                            "mips_e",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getQMean(s, l, c),
//                            "mips_q",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getTMean(s, l, c),
//                            "t_offset",
//                            s, l, c);
//                    ccdbTable.setDoubleValue(
//                            getTSigma(s, l, c),
//                            "t_rms",
//                            s, l, c);
//                }
//            }
//        }
//        ccdbTable.fireTableDataChanged();
//        if (histogramsFTHodo.testMode) {
//            ccdbTable.show();
//        }
//        this.detectorView.repaint();
//    } // end of: private void updateTable() {

    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        tabSel = sourceTabbedPane.getSelectedIndex();
       
        if ((this.previousTabSel == this.tabIndexMIPsignal && tabSel != this.tabIndexMIPsignal)) {
            this.canvasPane.remove(rBPaneMIP);
        }
        if ((this.previousTabSel == this.tabIndexGain && tabSel != this.tabIndexGain)) {
            this.canvasPane.remove(rBPaneGain);
        }
        if (tabSel == this.tabIndexGain ) {
            this.canvasPane.add(rBPaneGain, BorderLayout.NORTH);
        }
        if (tabSel == this.tabIndexMIPsignal  ) {
            this.canvasPane.add(rBPaneMIP, BorderLayout.NORTH);
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
        histogramsFTHodo.resetHistograms();
    }

    public void initArrays() {
        status = new double[9][3][21];
        thrshNPE = new double[9][3][21];
        pedMean = new double[9][3][21];
        pedRMS = new double[9][3][21];
        pedPrevious = new double[9][3][21];
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
                    this.pedRMS[s][l][c] = histogramsFTHodo.nPedRMS;
                    this.pedPrevious[s][l][c] = histogramsFTHodo.nPedMean;
                    this.gain[s][l][c] = histogramsFTHodo.nGain;
                    this.errGain[s][l][c] = histogramsFTHodo.nErrGain;
                    this.gain_mV[s][l][c] = histogramsFTHodo.nGain_mV;
                    this.errGain_mV[s][l][c] = histogramsFTHodo.nErrGain_mV;
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
// 	    String xyzwd[] = readTable(sec,lay,com,
// 				       "./Tables/fthodo_geometry.txt",
// 				       8);
                    int maxPedbin = 0;
                    double avePed = 0.0;
                    double nEventsAvePed = 0;
                    int index = getIndex4SLC(sec, lay, com);

                    short pulse[] = counter.getADCData(0).getPulseArray();
                    int ped_i1=counter.getADCData(0).getPedistalMinBin();
                    int ped_i2=counter.getADCData(0).getPedistalMaxBin();
                    int pul_i1=counter.getADCData(0).getPulseMinBin();
                    int pul_i2=counter.getADCData(0).getPulseMaxBin();
                    double npeWave;
                    double calibratedWave;
                    double baselineSubRaw;
                    double vOffset = 5.0;
                    int eventloop;
                    // reset non-accumulating histograms
                    histogramsFTHodo.H_FADC.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW_PED.get(sec, lay, com).reset();
                    histogramsFTHodo.H_FADC_RAW_PUL.get(sec, lay, com).reset();
//                    histogramsFTHodo.G_FADC_ANALYSIS.get(sec, lay, com).reset();
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
                            avePed = avePed + histogramsFTHodo.PedBinWidth
                                    * (maxPedbin + i)
                                    * histogramsFTHodo.H_PED_TEMP.get(sec, lay, com).getBinContent(maxPedbin + i)
                                    + histogramsFTHodo.PedBinWidth * (maxPedbin - i)
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
                        baselineSubRaw = pulse[i] - compEvntPed + 10.0;
                        histogramsFTHodo.H_FADC.get(sec, lay, com).fill(i, baselineSubRaw);
                        calibratedWave = (pulse[i] - compEvntPed) * histogramsFTHodo.LSB + vOffset;
                        histogramsFTHodo.H_VT.get(sec, lay, com).fill(i * 4, calibratedWave);
                        npeWave = (pulse[i] - compEvntPed) * histogramsFTHodo.LSB / histogramsFTHodo.voltsPerSPE;
                        histogramsFTHodo.H_NPE.get(sec, lay, com).fill(i * 4, npeWave);
                    }
                    double waveMax = 0.;
                    waveMax = -compEvntPed;
                    waveMax = waveMax + counter.getADCData(0).getHeight();
                    vMaxEvent[sec][lay][com] = waveMax * histogramsFTHodo.LSB;

                }

                //=-=-=-=-=-=-=-=-=-=-=-=-=-
                // Loop Two
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
                    histogramsFTHodo.H_NOISE_Q.get(sec, lay, com).fill(counter.getADCData(0).getADC() * histogramsFTHodo.LSB * histogramsFTHodo.nsPerSample / 50);
                    double waveMax = 0.;
                    double compEvntPed = counter.getADCData(0).getPedestal();
                    // first use of pedestal (in second loop)
                    if (abs(counter.getADCData(0).getPedestal()
                            - pedMean[sec][lay][com]) > 5.
                            && pedMeanGood) {

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
                        double threshDV = (double) histogramsFTHodo.threshD * histogramsFTHodo.LSB;
                        
                        if (vMaxEvent[sec][lay][com] > threshDV && vMaxEvent[sec][opp][com] > threshDV) {
                            histogramsFTHodo.H_MIP_V_MatchingTiles.get(sec, lay, com).fill(vMaxEvent[sec][lay][com]);
                            double intcharge=counter.getADCData(0).getADC() * histogramsFTHodo.LSB * histogramsFTHodo.nsPerSample / 50;
                            histogramsFTHodo.H_MIP_Q_MatchingTiles.get(sec, lay, com).fill(intcharge);
                            if (histogramsFTHodo.testMode){
                                System.out.println(" vMaxEvent["+sec+"]["+lay+"]["+com+"] =" + vMaxEvent[sec][lay][com] + " voltmax: "+ voltMax);
                                System.out.println(" vMaxEvent["+sec+"]["+opp+"]["+com+"] =" + vMaxEvent[sec][opp][com]);
                                System.out.println(" threshDV                             =" + threshDV);
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
                        if (time_M3[sec][2][com] > 0
                                && time_M3[sec][1][com] > 0) {
                            dT_M3[sec][com] = -time_M3[sec][2][com];
                            dT_M3[sec][com] += time_M3[sec][1][com];

                            histogramsFTHodo.H_T_MODE3.get(sec, 1, com).fill(dT_M3[sec][com]);

                            if (time_M7[sec][2][com] > 0&& time_M7[sec][1][com] > 0) {

                                dT_M7[sec][com] = -time_M7[sec][2][com];
                                dT_M7[sec][com] += time_M7[sec][1][com];

                                if (vMaxEvent[sec][1][com] > 200.
                                        && vMaxEvent[sec][1][com] < 1550.
                                        && vMaxEvent[sec][2][com] > 400.
                                        && vMaxEvent[sec][2][com] < 1550.) {
                                    histogramsFTHodo.H_T_MODE7.get(sec, 1, com).fill(time_M7[sec][1][com]);
                                    histogramsFTHodo.H_T_MODE7.get(sec, 2, com).fill(time_M7[sec][2][com]);
                                    histogramsFTHodo.H_DT_MODE7.get(sec, 1, com).fill(dT_M7[sec][com]);
                                    histogramsFTHodo.H_T1_T2.get(sec, 1, com).fill(time_M7[sec][1][com],time_M7[sec][2][com]);
                                }
                            }
                        }
                    }
                }
                //=======================================================
                //             DRAW HISTOGRAMS PER EVENT
                //=======================================================
//                int layCD = laySel - 1;
//                // map [1,2] to [1,0]
//                int oppCD = laySel % 2;
//                // map [1,2] to [2,1]
//                int oppSel = (laySel % 2) + 1;
//                int layCDL = 3 * layCD;
//                int oppCDL = 3 * oppCD;
//                int layCDM = layCDL+1;
//                int oppCDM = oppCDL+1;
//                int layCDR = layCDM+1;
//                int oppCDR = oppCDM+1;
                //   User chooses which histogram/s to display
                // map [1,2] to [0,1]
                //int layCD = laySel - 1;
                // map [1,2] to [1,0]
                //int oppCD = laySel % 2;
                // map [1,2] to [2,1]
                //int oppSel = (laySel % 2) + 1;
                
                //int layCDL = 2 * layCD;
                //int oppCDL = 2 * oppCD;

                //int layCDR = layCDL + 1;
                //int oppCDR = oppCDL + 1;

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
//        // map [1,2] to [0,1]
//        int layCD = laySel - 1;
//        // map [1,2] to [1,0]
//        int oppCD = laySel % 2;
//        // map [1,2] to [2,1]
//        int oppSel = (laySel % 2) + 1;
//        // indices for paddles
//        if (calSel) {
//            layCD = 0;
//            oppCD = 1;
//            oppSel = 0;
//        }
//        int layCDL = 2 * layCD;
//        int oppCDL = 2 * oppCD;
//        int layCDR = layCDL + 1;
//        int oppCDR = oppCDL + 1;
        // end of FTViewer (Combined view tab)
        //============================================================
        //============================================================
        // FTHODOViewer
        if (tabSel == this.tabIndexEvent) {
            drawCanvasEvent(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexPed) {
            drawCanvasPed(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexNoise) {
            drawCanvasNoise(secSel,laySel,comSel);
        } else if (tabSel == this.tabIndexGain) {
            if (drawByElec == false) {
                drawCanvasGain();
            } else {
                //this.canvasGain.divide(1, 1);
                drawCanvasGainElec(secSel,laySel,comSel);
            }
        }else if (tabSel == this.tabIndexMIPsignal){
                drawCanvasMIPsignal(secSel,laySel,comSel);
        }
//        else if (tabSel == this.tabIndexCharge) {
//            drawCanvasCharge(secSel,
//                    laySel,
//                    comSel);
//
//        } else if (tabSel == this.tabIndexVoltage) {
//            drawCanvasVoltage(secSel,
//                    laySel,
//                    comSel);
//        } else if (tabSel == this.tabIndexMIP) {
//            if (drawByElec == false) {
//                drawCanvasMIP(secSel,
//                        laySel,
//                        comSel);
//            } else {
//                drawCanvasMIPElec(secSel,
//                        laySel,
//                        comSel);
//            }
//        } else if (tabSel == this.tabIndexMatch) {
//            drawCanvasMatch(secSel,
//                    laySel,
//                    comSel);
//        } else if (tabSel == this.tabIndexTime) {
//            drawCanvasTime(secSel,
//                    laySel,
//                    comSel);
//        }
    }
    public void constantsEvent(CalibrationConstants cc, int i, int i1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void adjustFit() {
        System.out.println("Adjusting fit for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
        H1F hvolpeak =  histogramsFTHodo.H_NOISE_V.get(secSel, laySel, comSel);
        F1D fvolpeak = histogramsFTHodo.fV2.get(secSel, laySel, comSel);
        FTAdjustFit cfit = new FTAdjustFit(hvolpeak, fvolpeak, "LRQ");
        
        if (histogramsFTHodo.testMode)
            System.out.println("Old Mean:"+gain_mV[secSel][laySel][comSel] +" "+ errGain_mV[secSel][laySel][comSel]);
        this.canvasNoise.update();
    }
    public void adjustFitConstants() {
        System.out.println("Adjusting Constants for Sector " + secSel +" Layer " + laySel +" Component " + comSel);
        //if (histogramsFTHodo.testMode)
            System.out.println("Old Mean:"+gain_mV[secSel][laySel][comSel] +" "+ errGain_mV[secSel][laySel][comSel]);
        this.gain_mV[secSel][laySel][comSel]=histogramsFTHodo.fV2.get(secSel, laySel, comSel).getParameter(4)-histogramsFTHodo.fV2.get(secSel, laySel, comSel).getParameter(1);
        double gainError=histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(4).error()*histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(4).error()+histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(1).error()+histogramsFTHodo.fV2.get(secSel, laySel, comSel).parameter(1).error();
        this.errGain_mV[secSel][laySel][comSel] = sqrt(gainError);
        //if (histogramsFTHodo.testMode)
            System.out.println("New Mean:"+gain_mV[secSel][laySel][comSel] +" "+ errGain_mV[secSel][laySel][comSel]);
        this.setGGraphGain();
        drawCanvasGain();
    }
    
    
}
