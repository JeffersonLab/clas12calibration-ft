/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.Color;
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
import javax.swing.JSplitPane;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.utils.groups.IndexedList;import org.jlab.groot.graphics.EmbeddedCanvas;

/**
 *
 * @author devita
 */
public class FTCalibrationModule extends CalibrationEngine implements CalibrationConstantsListener {
    
    //private final int[] npaddles = new int[]{23,62,5};
    private String                       moduleName = null;
    private FTDetector                           ft = null;
    private CalibrationConstants              calib = null;
    private CalibrationConstants          prevCalib = null;
    private final IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);
    private JSplitPane                   moduleView = null;
    private EmbeddedCanvas                   canvas = null;
    private CalibrationConstantsView         ccview = null;
    private int                          nProcessed = 0;

    public FTCalibrationModule(FTDetector d, String ModuleName, String Constants,int Precision) {
        this.ft    = d; 
        this.initModule(ModuleName,Constants,Precision);
        this.resetEventListener();
    }

    public void analyze() {

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

    public JSplitPane getView() {
        return moduleView;
    }

    public void initModule(String name, String Constants, int Precision) {
        this.moduleName = name;
        this.nProcessed = 0;
        // create calibration constants viewer
        ccview = new CalibrationConstantsView();
        this.calib = new CalibrationConstants(3,Constants);
        this.calib.setName(name);
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
        }
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
            System.out.println(this.getName() + "constants save to'" + filename + ".txt");
        } catch (IOException ex) {
            System.out.println(
                    "Error writing file '"
                    + filename + "'");
            // Or we could just do this: 
            ex.printStackTrace();
        }

    }
    
    public void setCanvasUpdate(int time) {
        this.getCanvas().initTimer(time);
    }
    
    public void setCalibrationTablePrecision(int nDigits) {
	this.calib.setPrecision(3);
	this.prevCalib.setPrecision(3);        
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

    public void timeUpdate() {

    }

}
