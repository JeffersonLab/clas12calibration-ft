package org.clas.viewer;

import org.clas.ftdata.FTCalEvent;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTCalibrationModule implements CalibrationConstantsListener {
    
    //private final int[] npaddles = new int[]{23,62,5};
    private String                            moduleName = null;
    private FTDetector                                ft = null;
    private ConstantsManager                        ccdb = null;
    private FTCalConstants                      costants = new FTCalConstants();
    private CalibrationConstants                   calib = null;
    private CalibrationConstants               prevCalib = null;
    private Map<String,CalibrationConstants> globalCalib = null;
    private final IndexedList<DataGroup>      dataGroups = new IndexedList<>(3);
    private JSplitPane                        moduleView = null;
    private EmbeddedCanvas                        canvas = null;
    private CalibrationConstantsView              ccview = null;
    private FTCanvasBook                      canvasBook = new FTCanvasBook();
    private int                               nProcessed = 0;
    private int                              selectedKey = 8;
    private double[]                               range = new double[2];
    private double[]                                cols = null;
    private double                             reference = 0;
    private double[]                          scaleshift = {1,0};

    // configuration
    public int              calDBSource = 0;
    public static final int CAL_DEFAULT = 0;
    public static final int    CAL_FILE = 1;
    public static final int      CAL_DB = 2;
    public String  prevCalFilename;
    public int     prevCalRunNo;
    public boolean prevCalRead = false;
    public String  ccdbTableName;
    
    public FTCalibrationModule(FTDetector d, String ModuleName, String Constants,int Precision, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
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
                
        this.ft    = d; 
        this.initModule(ModuleName,Constants,Precision, ccdb, gConstants);
        this.resetEventListener();
    }

    public void analyze() {

    }    

    public void adjustFit() {
        this.printErr("option not implemented in current module\n");
    }
    
    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
//        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
//        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = this.getDataGroup();
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            this.drawDataGroup(sector, layer, component);
        } else {
            this.printErr("ERROR: can not find the data group\n");
        }
        this.selectedKey = component;
    }
        
    public static void copyConstants(CalibrationConstants origin, CalibrationConstants destination) {
        for(int i=0; i<origin.getRowCount(); i++) {
            int sector    = Integer.parseInt((String) origin.getValueAt(i, 0));
            int layer     = Integer.parseInt((String) origin.getValueAt(i, 1));
            int component = Integer.parseInt((String) origin.getValueAt(i, 2)); 
            for (int j = 3; j < origin.getColumnCount(); j++) {
                double value = origin.getDoubleValue(origin.getColumnName(j), sector, layer, component);
                destination.setDoubleValue(value, origin.getColumnName(j), sector, layer, component);
            }
        }
        destination.fireTableDataChanged();
    }

    public void dataEventAction(FTCalEvent event) {
        nProcessed++;
        this.processEvent(event);
    }

    public void drawDataGroup(int sector, int layer, int component) {
        if(this.getDataGroup().hasItem(sector,layer,component)==true){
            DataGroup dataGroup = this.getDataGroup().getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().draw(dataGroup);
            this.getCanvas().setGridX(false);
            this.getCanvas().setGridY(false);
            this.getCanvas().update();
            this.setDrawOptions();
        }
    }
    
    public EmbeddedCanvas getCanvas() {
        return canvas;
    }

    public CalibrationConstantsView getCcview() {
        return ccview;
    }
    
    public CalibrationConstants getCalibrationTable() {
	return calib;
    }

    public FTCanvasBook getCanvasBook() {
        return canvasBook;
    }

    public FTCalConstants getConstants() {
        return costants;
    }
    
    public ConstantsManager getConstantsManager() {
        return ccdb;
    }
    
    public final Color getColor(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        Color col = new Color(100, 100, 100);
        if (this.getDetector().hasComponent(key)) {
            int nent = this.getNEvents(dsd);
            if (nent > 0) {
                col = this.getColor(this.getValue(dsd),this.getNEvents(dsd));
            }
        }
        return col;
    }

    private Color getColor(double value, int nevent) {
        ColorPalette palette = new ColorPalette();
        if(cols!=null) {
            return palette.getColor3D(value-cols[0], cols[1]-cols[0], false);
        }
        else {
            return palette.getColor3D(nevent, this.getnProcessed(),true);
        }
    }

    public double getReference() {
        return reference;
    }

    public double getConstantScale() {
        return scaleshift[0];
    }

    public double getConstantShift() {
        return scaleshift[1];
    }

    public double getValue(DetectorShape2D dsd) {
        return 0;
    }
    
    public IndexedList<DataGroup> getDataGroup() {
        return dataGroups;
    }

    public FTDetector getDetector() {
        return ft;
    }

    public Map<String, CalibrationConstants> getGlobalCalibration() {
        return globalCalib;
    }
    
    public String getName() {
        return moduleName;
    }

    public int getNEvents(DetectorShape2D dsd) {
        return 0;
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

    public double getRangeMean() {
        return (range[0]+range[1])/2;
    }

    public double getRangeWidth() {
        return range[1]-range[0];
    }

    public int getSelectedKey() {
        return selectedKey;
    }

    public JSplitPane getView() {
        return moduleView;
    }
    
    public void initializeConstants(CalibrationConstants cc) {
        for (int key : this.getDetector().getDetectorComponents()) {
            cc.addEntry(1, 1, key);
            for(int i=0; i<cc.getColumnCount(); i++) {
                cc.setDoubleValue(0.0, cc.getColumnName(i), 1, 1, key);
            }
        }
        cc.fireTableDataChanged();
    }
    
    public final void initModule(String name, String Constants, int Precision, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        this.moduleName = name;
        this.nProcessed = 0;
        // create calibration constants viewer
        ccview = new CalibrationConstantsView();
        this.calib = new CalibrationConstants(3,Constants);
        this.calib.setName(name);
        this.initializeConstants(calib);
        ccview.addConstants(this.calib,this);
        this.prevCalib = new CalibrationConstants(3,Constants);
        this.prevCalib.setName(name);
	this.setCalibrationTablePrecision(Precision);
        
        
        this.ccdb        = ccdb;
        this.globalCalib = gConstants;

        moduleView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas(); 
        canvas.initTimer(2000);
 
        moduleView.setTopComponent(canvas);
        moduleView.setBottomComponent(ccview);
        moduleView.setDividerLocation(0.75);        
        moduleView.setResizeWeight(0.75);
    }
           
    public void initRange(double r1, double r2) {
        this.setRange(r1, r2);
        this.resetEventListener();
    }
        
    public void loadConstants() {   
        if(this.calDBSource==this.CAL_DB) {
            this.loadConstants(this.prevCalRunNo);
        }
        else if(this.calDBSource==this.CAL_FILE) {
            this.loadConstants(this.prevCalFilename);
        }
        else {
            this.printOut("constants set to default\n");            
        }
        if(this.prevCalib.getRowCount()!=0)
            copyConstants(prevCalib, calib);
    }  
    
    public void loadConstants(IndexedTable ccdbTable) {

    }
    
    public void loadConstants(int run) {
        if(prevCalib.getRowCount()==0 && ccdb!=null && ccdbTableName!=null) {
            this.printOut("module constants will be read  from " + ccdbTableName + " with run number " + run + "\n");
            this.loadConstants(ccdb.getConstants(run, ccdbTableName));
            if(prevCalib.getRowCount()!=0) {
                this.getGlobalCalibration().put(moduleName, prevCalib);
                this.loadConstantsToFunctions();
            }
        }
    }
    
    public void loadConstants(String fileName) {     
	this.printOut("module constants will be read from file: " + fileName + "\n");
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
                    this.printErr("wrong constants file format\n");
                }
                else {
                    int sector = Integer.parseInt(lineValues[0]);
                    int layer  = Integer.parseInt(lineValues[1]);
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
            this.getGlobalCalibration().put(moduleName, prevCalib);
        } catch (FileNotFoundException ex) {
            this.printErr("unable to open file '" + fileName + "'\n");
            return;
        } catch (IOException ex) {
            this.printErr("error reading file '" + fileName + "'\n");
            ex.printStackTrace();
            return;
        }
        this.loadConstantsToFunctions();
    }
     
    public void loadConstantsToFunctions() {
        
    }
    
    public void maxGraph(H2F hist, GraphErrors graph) {

            ArrayList<H1F> slices = hist.getSlicesX();
            int nBins = hist.getXAxis().getNBins();
            double[] sliceMax = new double[nBins];
            double[] maxErrs = new double[nBins];
            double[] xVals = new double[nBins];
            double[] xErrs = new double[nBins];
            int ngood = 0;
            for (int i=0; i<nBins; i++) {

                    //			System.out.println("getH1FEntries "+getH1FEntries(slices.get(i)));
                    //			System.out.println("H1F getEntries "+slices.get(i).getEntries());

                    if (slices.get(i).getIntegral() > 25) {
                        ngood++; 
                        int maxBin = slices.get(i).getMaximumBin();
                        sliceMax[i] = slices.get(i).getxAxis().getBinCenter(maxBin);
                        maxErrs[i]  = slices.get(i).getxAxis().getBinWidth(maxBin);
                        //maxErrs[i] = maxGraphError;

                        xVals[i] = hist.getXAxis().getBinCenter(i);
                        xErrs[i] = hist.getXAxis().getBinWidth(i)/2.0;
                    }
                    else xErrs[i]=-1;
            }
            if(ngood>1) {
                graph.reset();
                for(int i=0; i<nBins; i++) {
                    if(xErrs[i]>0) graph.addPoint(xVals[i], sliceMax[i], xErrs[i], maxErrs[i]);
                }
            }
    }
    
    public void processEvent(FTCalEvent event) {
    
    }
    
    public void printOut(String s) {
        System.out.print("\t["+ this.getName() + "] " + s);
    }
    
    public void printErr(String s) {
        System.err.print("\t["+ this.getName() + "] " + s);
    }
    
    public void processShape(DetectorShape2D dsd) {
        // plot histos for the specific component
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
//        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = this.getDataGroup();        
        
        if(group.hasItem(sector,layer,paddle)==true){
            this.drawDataGroup(sector, layer, paddle);
        } else {
            this.printErr("ERROR: can not find the data group\n");
        }       
        this.selectedKey = paddle;
    }
  
    public void readDataGroup(TDirectory dir) {
        String folder = this.getName() + "/";
        this.printOut("reading from: " + folder + "\n");
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
                    newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                    if(ds instanceof F1D) {
                        newGroup.getF1D(ds.getName()).setLineColor(((F1D) ds).getLineColor());
                        newGroup.getF1D(ds.getName()).setLineWidth(((F1D) ds).getLineWidth());
                    }
                }
            }
            map.replace(key, newGroup);
        }
        this.printOut("histogram loading completed\n");
        this.analyze();
    }   

    public void recenterRange(double center, double resolution) {
        if(center!=this.getRangeMean()) {
            center = Math.round(center/resolution)*resolution;
            this.setRange(center-this.getRangeWidth()/2, center+this.getRangeWidth()/2);
        }
    }
    
    public void reset() {
        nProcessed=0;
        this.resetEventListener();
    }
     
    public void resetEventListener() {
        
    }
     
    public void saveConstants(String name) {
        this.saveConstants(name, calib);
    }
    
    public void savePreviousConstants(String name) {
        this.saveConstants(name, prevCalib);
    }
    
    public void saveConstants(String name, CalibrationConstants constants) {

       String filename = name + "/" + this.getName() + ".txt";
            
        try {
            // Open the output file
            File outputFile = new File(filename);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);

            for (int i = 0; i < constants.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < constants.getColumnCount(); j++) {
                    line = line + constants.getValueAt(i, j);
                    if (j < constants.getColumnCount() - 1) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            outputBw.close();
            this.printOut("constants saved to'" + filename + "'\n");
        } catch (IOException ex) {
            this.printErr("error writing file '" + filename + "'\n");
            // Or we could just do this: 
            ex.printStackTrace();
        }

    }

    public void savePicture(String path) {
        if(this.getCanvas().getSize().height==0 || this.getCanvas().getSize().width==0) {
            this.getCanvas().setSize(1600, 700);
            this.getCanvas().doLayout();
        }
        this.getCanvas().save(path + "/" + this.getName() + ".pdf");
    }
    
    public void setPreviousCalibrationTable(CalibrationConstants prevCalib) {
        this.prevCalib = prevCalib;
    }

    public void setCalibrationTablePrecision(int nDigits) {
	this.calib.setPrecision(nDigits);
	this.prevCalib.setPrecision(nDigits);        
    }
    
    public void setCanvasBookData() {
        
    }
    
    public void setCanvasUpdate(int time) {
        this.getCanvas().initTimer(time);
    }

    public void setCCDBTable(String table) {
        this.ccdbTableName = table;
        this.printOut("module calibration table set to " + this.ccdbTableName + "\n");
    }
    
    public void setDrawOptions() {

    }

    public void setGlobalCalibration(Map<String, CalibrationConstants> globalCalib) {
        this.globalCalib = globalCalib;
    }

    public final void setRange(double min, double max) {
        this.range[0]=min;
        this.range[1]=max;
        this.printOut("module histogram range set to: " + String.format("%.3f:%.3f", this.range[0], this.range[1]) + "\n");
    }
    
    public final void setCols(double min, double max) {
        this.cols = new double[2];
        this.cols[0]=min;
        this.cols[1]=max;
        this.printOut("module color range et to: " + String.format("%.3f:%.3f", this.cols[0], this.cols[1]) + "\n");
    }
    
    public final void setReference(double value) {
        this.reference=value;
        this.printOut("module reference calibration value set to: " + String.format("%.3f", this.reference) + "\n");
        this.resetEventListener();
    }
    
    public final void setScaleShift(double scale, double shift) {
        this.scaleshift[0]=scale;
        this.scaleshift[1]=shift;
        this.printOut("module constant scale/shift set to: " + String.format("%.3f/%.3f", this.scaleshift[0], this.scaleshift[1]) + "\n");
        this.resetEventListener();
    }

    public void setRange() {
        JFrame frame    = new JFrame();
        JLabel label;
        JPanel panel;
    	JTextField minRange = new JTextField(5);
	JTextField maxRange = new JTextField(5);
        	
        
        panel = new JPanel(new GridLayout(2, 2));            
        panel.add(new JLabel("Histogram range minimum"));
        minRange.setText(Double.toString(this.range[0]));
        panel.add(minRange);
        panel.add(new JLabel("Histogram range maximum"));
        maxRange.setText(Double.toString(this.range[1]));
        panel.add(maxRange);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
                        "Set range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(!minRange.getText().isEmpty() && !maxRange.getText().isEmpty()) {
                this.setRange(Double.parseDouble(minRange.getText()), Double.parseDouble(maxRange.getText()));
                this.resetEventListener();
            }
        }

    }
    
    public void setCols() {
        JFrame frame    = new JFrame();
        JLabel label;
        JPanel panel;
    	JTextField minRange = new JTextField(5);
	JTextField maxRange = new JTextField(5);
        	
        
        panel = new JPanel(new GridLayout(2, 2));            
        panel.add(new JLabel("Color map range minimum"));
        minRange.setText(Double.toString(this.cols[0]));
        panel.add(minRange);
        panel.add(new JLabel("Color map range maximum"));
        maxRange.setText(Double.toString(this.cols[1]));
        panel.add(maxRange);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
                        "Set range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(!minRange.getText().isEmpty() && !maxRange.getText().isEmpty()) 
                this.setCols(Double.parseDouble(minRange.getText()), Double.parseDouble(maxRange.getText()));
       }

    }
    
    public void setReference() {
        JFrame frame    = new JFrame();
        JLabel label;
        JPanel panel;
        JTextField refValue = new JTextField(5);
	
        
        panel = new JPanel(new GridLayout(2, 1));            
        panel.add(new JLabel("Reference calibration value"));
        refValue.setText(Double.toString(this.reference));
        panel.add(refValue);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
                        "Set range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(!refValue.getText().isEmpty())this.setReference(Double.parseDouble(refValue.getText()));
        }

    }
    
    public void setScaleShift() {
        JFrame frame    = new JFrame();
        JLabel label;
        JPanel panel;
    	JTextField scale = new JTextField(5);
	JTextField shift = new JTextField(5);
        	
        
        panel = new JPanel(new GridLayout(2, 2));            
        panel.add(new JLabel("Constant scale"));
        scale.setText(Double.toString(this.scaleshift[0]));
        panel.add(scale);
        panel.add(new JLabel("Constant shift"));
        shift.setText(Double.toString(this.scaleshift[1]));
        panel.add(shift);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
                        "Set range", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(!scale.getText().isEmpty() && !shift.getText().isEmpty()) {
                this.setScaleShift(Double.parseDouble(scale.getText()), Double.parseDouble(shift.getText()));
                this.resetEventListener();
            }
        }

    }

    public void setConstantsManager(ConstantsManager ccdb) {
        this.ccdb = ccdb;
    }

    public void showPlots() {
        this.setCanvasBookData();
        if(!this.canvasBook.getCanvasDataSets().isEmpty()) {
            JFrame frame = new JFrame(this.getName());
            frame.setSize(1000, 800);        
            frame.add(canvasBook);
            // frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        else {
        this.printErr("function not implemented in current module\n");            
        }
    }
    
    public void timerUpdate() {
        this.analyze();
        this.updateTable();
    }
    
    public void updatePreviousConstants() {
//        if(this.calDBSource == FTCalibrationModule.CAL_FILE) {
            this.printOut("updating constants for next iteration\n");
            copyConstants(calib, prevCalib);
//        }
    }
    
    public void updateTable() {

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
}
