/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.utils.groups.IndexedList;
import org.jlab.groot.graphics.EmbeddedCanvas;

/**
 *
 * @author devita
 */
public class FTCalibrationModule extends CalibrationEngine implements CalibrationConstantsListener {
    
    private String                       moduleName = null;
    private FTDetector                           ft = null;
    ConstantsManager                           ccdb = null;
    private CalibrationConstants              calib = null;
    private CalibrationConstants          CCDBcalib = null;
    private CalibrationConstants          prevCalib = null;
    private final IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);
    private JSplitPane                   moduleView = null;
    private EmbeddedCanvas                   canvas = null;
    private CalibrationConstantsView         ccview = null;
    private FTCanvasBook                 canvasBook = new FTCanvasBook();
    private int                          nProcessed = 0;
    private int                         selectedKey = 8;
    private double[]                          range = new double[2];
    private String                    CCDBConstants = null;
    public FTCalibrationModule(FTDetector d, String ModuleName, String Constants, String CCDBConstants, int Precision) {
        GStyle.getAxisAttributesX().setTitleFontSize(24);
        GStyle.getAxisAttributesX().setLabelFontSize(18);
        GStyle.getAxisAttributesY().setTitleFontSize(24);
        GStyle.getAxisAttributesY().setLabelFontSize(18);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.setPalette("kDefault");
        GStyle.getAxisAttributesX().setLabelFontName("Avenir");
        GStyle.getAxisAttributesY().setLabelFontName("Avenir");
        GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
        GStyle.getAxisAttributesX().setTitleFontName("Avenir");
        GStyle.getAxisAttributesY().setTitleFontName("Avenir");
        GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(1);
        this.CCDBConstants=CCDBConstants;
        this.ft    = d;
        this.initModule(ModuleName,Constants, CCDBConstants, Precision);
        this.resetEventListener();
    }
    
    public void putConstantsInCCDBFormat(String CCDBConstants) { 
        for (int ssec=1;ssec<9;ssec++){
            for (int llay=1;llay<3;llay++){
                for (int key : this.getDetector().getDetectorComponents(ssec,llay)) {
                    String ccdbArray[]= CCDBConstants.split(":");
                    this.CCDBcalib.addEntry(ssec, llay, key);
                     for (String temp: ccdbArray){
                         this.CCDBcalib.setDoubleValue(getCalibrationTable().getDoubleValue(temp, ssec,llay,key), temp, ssec, llay, key);
                         System.out.println(temp+" "+this.CCDBcalib.getDoubleValue(temp, ssec,llay,key));
                     }
                }
            }
        }
    }

    public void analyze() {
    }
    
    public void adjustFit() {
        System.out.println("Option not implemented in current module");
    }
    
    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = this.getDataGroup();
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            DataGroup dataGroup = group.getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().draw(dataGroup);
            this.getCanvas().update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
        this.selectedKey = component;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        nProcessed++;
        if (event.getType() == DataEventType.EVENT_START) {
            System.out.println("EVENT_START");
            resetEventListener();
            processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
        }
        else if (event.getType()==DataEventType.EVENT_SINGLE) {
            processEvent(event);
            System.out.println("EVENT_SINGLE from FTCalibrationModule");
        }
        else if (event.getType() == DataEventType.EVENT_STOP) {
            System.out.println("EVENT_STOP");
            analyze();
            updateTable();
        }
    }
    
    public EmbeddedCanvas getCanvas() {
        return canvas;
    }
    
    public CalibrationConstantsView getCcview() {
        return ccview;
    }
    
    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(calib);
    }
    
    public CalibrationConstants getCalibrationTable() {
        return calib;
    }
    
    public FTCanvasBook getCanvasBook() {
        return canvasBook;
    }
    
    public ConstantsManager getConstantsManager() {
        return ccdb;
    }
    
    public Color getColor(DetectorShape2D dsd) {
        Color col = new Color(100, 100, 100);
        return col;
    }
    
    @Override
    public IndexedList<DataGroup> getDataGroup() {
        return dataGroups;
    }
    
    public FTDetector getDetector() {
        return ft;
    }
    
    public String getName() {
        return moduleName;
    }
    
    public CalibrationConstants getPreviousCalibrationTable() {
        return prevCalib;
    }
    
    public int getnProcessed() {
        return nProcessed;
    }
    
    public double[] getRange() {
        return range;
    }
    
    public int getSelectedKey() {
        return selectedKey;
    }
    
    public JSplitPane getView() {
        return moduleView;
    }
    
    public void initModule(String name, String Constants, String CCDBConstants, int Precision) {
        this.moduleName = name;
        this.nProcessed = 0;
        // create calibration constants viewer
        ccview = new CalibrationConstantsView();
        this.calib = new CalibrationConstants(3,Constants);
        this.calib.setName(name);
        this.CCDBcalib = new CalibrationConstants(3,CCDBConstants);
        this.CCDBcalib.setName("CCDB"+name);
        this.prevCalib = new CalibrationConstants(3,Constants);
        this.prevCalib.setName(name);
        this.setCalibrationTablePrecision(Precision);
        ccview.addConstants(this.getCalibrationConstants().get(0),this);
        
        moduleView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas();
        canvas.initTimer(2000);
        
        moduleView.setTopComponent(canvas);
        moduleView.setBottomComponent(ccview);
        moduleView.setDividerLocation(0.75);
        moduleView.setResizeWeight(0.75);
    }
    
    public void initRange(double r1, double r2) {
        this.range[0]=r1;
        this.range[1]=r2;
        //this.resetEventListener();
    }
    
    public void loadConstants(ConstantsManager ccdb) {
        this.ccdb = ccdb;
    }
    
    public void loadConstants(String path) {
        System.out.println("Loading calibration values for module " + this.getName());
        String fileName = path + "/" + this.getName() + ".txt";
        System.out.println("File: " + fileName);
        // read in the left right values from the text file
        String line = null;
        try {
            // Open the file
            FileReader fileReader = new FileReader(fileName);
            
            // Always wrap FileReader in BufferedReader
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            line = bufferedReader.readLine();
            
            int irow =0;
            while (line != null) {
                String[] lineValues;
                lineValues = line.split(" ");
                
                if(lineValues.length!=prevCalib.getColumnCount()) {
                    System.out.println("Wrong constants file format");
                }
                else {
                    int sector = Integer.parseInt(lineValues[0]);
                    int layer = Integer.parseInt(lineValues[1]);
                    int paddle = Integer.parseInt(lineValues[2]);
                    prevCalib.addEntry(sector, layer, paddle);
                    //                    System.out.println(lineValues.length + " " + prevCalib.getColumnCount() + " " + prevCalib.getRowCount()+ " " + line);
                    for(int icol=3; icol<prevCalib.getColumnCount(); icol++) {
                        //                        System.out.println(lineValues[icol] + " " + irow + " " + icol);
                        prevCalib.setDoubleValue(Double.parseDouble(lineValues[icol]),prevCalib.getColumnName(icol), sector, layer, paddle);
                    }
                }
                
                line = bufferedReader.readLine();
                irow++;
            }
            
            bufferedReader.close();
            prevCalib.show();
        } catch (FileNotFoundException ex) {
            System.out.println(
                               "Unable to open file '"
                               + fileName + "'");
            return;
        } catch (IOException ex) {
            System.out.println(
                               "Error reading file '"
                               + fileName + "'");
            ex.printStackTrace();
            return;
        }
    }
    
    public void processEvent(DataEvent event) {
    }
    
    public void processShape(DetectorShape2D dsd) {
        // plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        
        IndexedList<DataGroup> group = this.getDataGroup();
        
        if(group.hasItem(sector,layer,paddle)==true){
            this.getCanvas().clear();
            this.getCanvas().draw(this.getDataGroup().getItem(sector,layer,paddle));
            this.getCanvas().update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
        this.selectedKey = paddle;
    }
    
    public void readDataGroup(TDirectory dir) {
        String folder = this.getName() + "/";
        System.out.println("Reading from: " + folder);
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            Long key = entry.getKey();
            DataGroup group = entry.getValue();
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds   = nrows*ncols;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                }
            }
            map.replace(key, newGroup);
        }
        this.analyze();
    }
    
    public void saveConstants(String name) {
        String filename = name + "/" + this.getName() + ".txt";
        try {
            // Open the output file
            File outputFile = new File(filename);
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
            System.out.println(this.getName() + "constants save to'" + filename);
        } catch (IOException ex) {
            System.out.println("Error writing file '"+ filename + "'");
            // Or we could just do this:
            ex.printStackTrace();
        }
        this.putConstantsInCCDBFormat(this.CCDBConstants);
        filename = name + "/CCDB" + this.getName() + ".txt";
        try {
            // Open the output file
            File outputFile = new File(filename);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);
            
            for (int i = 0; i < CCDBcalib.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < CCDBcalib.getColumnCount(); j++) {
                    line = line + CCDBcalib.getValueAt(i, j);
                    if (j < CCDBcalib.getColumnCount() - 1) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            outputBw.close();
            System.out.println(this.getName() + "constants save to'" + filename );
        } catch (IOException ex) {
            System.out.println("Error writing file '"+ filename + "'");
            // Or we could just do this:
            ex.printStackTrace();
        }
    }
    
    public void setCalibrationTablePrecision(int nDigits) {
        this.calib.setPrecision(nDigits);
        this.CCDBcalib.setPrecision(nDigits);
        this.prevCalib.setPrecision(nDigits);
    }
    
    public void setCanvasBookData(String PlotsToShow) {
        if (Objects.equals(PlotsToShow, "fits"))
            canvasBook.setData(this.getDataGroup(), 0);
        else if (Objects.equals(PlotsToShow, "constants"))
            canvasBook.setDataConstants(this.getDataGroup(), 2);
    }
    
    public void setCanvasUpdate(int time) {
        this.getCanvas().initTimer(time);
    }
    
    public void setRange() {
        JFrame frame    = new JFrame();
        JLabel label;
        JPanel panel;
        JTextField minRange = new JTextField(5);
        JTextField maxRange = new JTextField(5);
        
        
        panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Histogram range minimum"));
        minRange.setText(Double.toString(0.0));
        panel.add(minRange);
        panel.add(new JLabel("Histogram range maximum"));
        maxRange.setText(Double.toString(100));
        panel.add(maxRange);
        
        int result = JOptionPane.showConfirmDialog(null, panel,
                                                   "Set range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(!minRange.getText().isEmpty())this.range[0] = Double.parseDouble(minRange.getText());
            else this.range[0] = 0;
            if(!maxRange.getText().isEmpty())this.range[1] = Double.parseDouble(maxRange.getText());
            else this.range[1] = 100;
            System.out.println("Histogram range set to: " + this.range[0] + ":" + - this.range[1]);
            this.resetEventListener();
        }
        
    }
    
    public void  showPlots() {
        this.setCanvasBookData("fits");
        if(this.canvasBook.getCanvasDataSets().size()!=0) {
            JFrame frame = new JFrame(this.getName());
            frame.setSize(1000, 800);
            frame.add(canvasBook);
            // frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        else {
            System.out.println("Function not implemented in current module");
        }
    }
    
        public void  showConstants() {
        this.setCanvasBookData("constants");
        if(this.canvasBook.getCanvasDataSets().size()!=0) {
            JFrame frame = new JFrame(this.getName());
            frame.setSize(1000, 800);
            frame.add(canvasBook);
            // frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        else {
            System.out.println("Function not implemented in current module");
        }
    }
    
    
    
    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        dir.cd(folder);
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
        }
    }
    
    public void setConstantsManager(ConstantsManager ccdb) {
        this.ccdb = ccdb;
    }
    
    public void timeUpdate() {
        
    }
    
    public void updateTable() {
        getCalibrationTable().fireTableDataChanged();
    }
}
