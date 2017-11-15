/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.clas.detector.DetectorDataDgtz;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;


/**
 *
 * @author devita
 */
public class FTModule {

    private String                     name           = null;
    private FTModuleType               type           = null;
    private FTDetector                 detector       = null;
    private IndexedList<DataGroup>     moduleData     = new IndexedList<DataGroup>(3);
    private IndexedList<DataGroup>     compareData    = new IndexedList<DataGroup>(3);
    private Map<String,EmbeddedCanvas> canvases       = new LinkedHashMap<String,EmbeddedCanvas>();
    private FTCanvasBook               canvasBook    = new FTCanvasBook();
    private JPanel                     radioPane      = new JPanel();
    private List<FTParameter>          parameters     = new ArrayList<FTParameter>();
    private int                        numberOfEvents = 0;
    private int                        keySelect;
    private int                        runNumber      = 0;
    
    public double nsPerSample = 4;
    public double LSB         = 0.4884;
    
    public FTModule(FTDetector d) {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        this.detector = d;
        this.type     = FTModuleType.EVENT_ACCUMULATE;
        this.numberOfEvents = 0;
    }
  
    public FTModule(FTDetector d, String name, FTModuleType type) {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        this.name  = name;
        this.type  = type;
        this.detector = d;
        this.numberOfEvents = 0;
    }
  
    public FTModule(FTDetector d, String name, String... fields) {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        this.name  = name;
        this.detector = d;
        this.addParameters(fields);
    } 
    
    public final void addCanvases(String... names) {
        for(String item : names){
            EmbeddedCanvas can = new EmbeddedCanvas();
            this.canvases.put(item, can);
        }
    }

    public final void addComparisonCanvas() {
        EmbeddedCanvas can = new EmbeddedCanvas();  
        this.canvases.put("Comparison", can);
    }

     
    public void addEvent(List<DetectorDataDgtz> counters) {
        this.numberOfEvents++;
        this.processEvent(counters);
    }
    
    public final void addParameters(String... fields){
        for(String item : fields){
            this.parameters.add(new FTParameter(item));
        }
    }
    
    public void adjustFit() {
        System.out.println("\nAdjust-fit function not implemented for current module");
    }
    
    public void analyze() {
        // analyze detector data at the end of data processing
    }
    
    public IndexedList<DataGroup> createDataGroup() {
        IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>();
        return dataGroups;
    }

    private void createDataGroups() {
        this.moduleData  = this.createDataGroup();
        this.compareData = this.createDataGroup();
    }

    public Map<String,EmbeddedCanvas> getCanvases() {
        return canvases;
    }
    
    public EmbeddedCanvas getCanvas(String name) {
        return canvases.get(name);
    }

    public FTCanvasBook getCanvasBook() {
        return canvasBook;
    }
    
    public Color getColor(int key, String name) {
        Color col = this.getParameter(name).getColor(this.getParameterValue(name, key));
        return col;
    }

    public IndexedList<DataGroup> getDataGroup(){
        return moduleData;
    }

    public IndexedList<DataGroup> getComparisonDataGroup() {
        return compareData;
    }

    public FTDetector getDetector() {
        return this.detector;
    }
    
    public String getName() {
        return name;
    }
        
    public int getNumberOfEvents() {
        return numberOfEvents;
    }
    
    public List<FTParameter> getParameters() {
        return this.parameters;
    }  

    public FTParameter getParameter(int index) {
        return this.parameters.get(index);
    }  

    public FTParameter getParameter(String name) {
        FTParameter par = null;
        for(int i=0; i<this.parameters.size(); i++) {
            if(name == this.parameters.get(i).getName()) {
                par = this.parameters.get(i);
                break;
            }
        }
        return par;
    }  

    public double getParameterValue(int index, int key) {
        return this.parameters.get(index).getValue();
    }

    public double getParameterValue(String name, int key) {
        int index=-1;
        for (int i=0; i<this.parameters.size(); i++) {
            if (name == this.parameters.get(i).getName()) {
                index=i;
                break;
            }
        }
        if(index==-1) return index;
        else          return this.getParameterValue(index,key);
    }
        
    public int getSelectedKey() {
        return keySelect;
    }

    public FTModuleType getType() {
        return type;
    }
        
    public void processEvent(List<DetectorDataDgtz> counters) {
        // process event
    }
    
    public void plotDataGroup() {

    }
    
    public void plotDataGroups() {
        this.plotDataGroup();
        if(this.canvases.containsKey("Comparison")) this.plotComparison();
    }
    
    public void plotComparison() {

    }
    
    public void printCanvas(String dir) {
        // print canvas to files
        for(Map.Entry<String, EmbeddedCanvas> entry : this.canvases.entrySet()) {
            String fileName = dir + "/" + this.name + "_" + entry.getKey() + ".png";
            System.out.println(fileName);
            entry.getValue().save(fileName);
        }
    }
    
    public void readDataGroup(TDirectory dir, boolean ref) {
        String folder = this.name + "/";
        System.out.println("Reading from: " + folder);        
        Map<Long, DataGroup> map = null;
        if(ref) map = this.getComparisonDataGroup().getMap();
        else    map = this.getDataGroup().getMap();
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
        this.plotDataGroups();
        this.setFunctionStyle();
        this.detector.repaint();
    }
    
    public void resetEventListener() {
        System.out.println("Resetting " + this.name + " module");
        this.createDataGroups();
        this.plotDataGroups();
        this.detector.repaint();
    }
    
    public void setAnalysisParameters() {
               
    }
    
    public void setCanvasBookData() {
        
    }
    
    public void setCanvasUpdate(int time) {
        if(this.type == FTModuleType.EVENT_ACCUMULATE) {
            for(Map.Entry<String, EmbeddedCanvas> entry : this.canvases.entrySet()) entry.getValue().initTimer(time);
        }
        else {
            for(Map.Entry<String, EmbeddedCanvas> entry : this.canvases.entrySet()) entry.getValue().initTimer(1000);
        }
    }
    
    public void setKeySelect(int keySelect) {
        this.keySelect = keySelect;
    }
    
    public void setFunctionStyle() {
        
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public void showPlots() {
        this.setCanvasBookData();
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
        String folder = "/" + this.name;
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

    public void setType(FTModuleType type) {
        this.type = type;
    }
}
