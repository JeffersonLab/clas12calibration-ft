package org.clas.viewer;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

import org.clas.modules.FTEnergyCalibration;
import org.clas.modules.FTTimeCalibration;
import org.clas.modules.FTPedestalCalibration;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, DetectorListener, ChangeListener {
    
    public int i = 0;

    JPanel                   mainPanel 	   = null;
    JMenuBar                 menuBar       = null;
    DataSourceProcessorPane  processorPane = null;
    JSplitPane               splitPanel    = null;
    JPanel                   detectorPanel = null;
    FTHodoDetector            detectorView  = null;
    JTabbedPane               modulePanel  = null;
    String                    moduleSelect = null;
    
    ConstantsManager         ccdb = new ConstantsManager();
    
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 100000;
    private int runNumber  = 0;
    private String workDir = "/Users/nicholaszachariou/JLabPhysics/Hodoscope/";

    ArrayList<FTCalibrationModule> modules = new ArrayList();

    public CalibrationViewer() {
       
        // create main panel
        mainPanel = new JPanel();	
	mainPanel.setLayout(new BorderLayout());
        
	// create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu constants = new JMenu("Constants");
        menuItem = new JMenuItem("Load...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Load constants from file");
        menuItem.addActionListener(this);
        constants.add(menuItem);              
        menuItem = new JMenuItem("CCDB...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Load constants from ccdb");
        menuItem.addActionListener(this);
        constants.add(menuItem);        
        menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save constants to file");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        menuItem = new JMenuItem("Update table");
        menuItem.getAccessibleContext().setAccessibleDescription("Update table content");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        menuBar.add(constants);         
        JMenu file = new JMenu("Histograms");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Adjust fit...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit parameters and range");
        menuItem.addActionListener(this);
        file.add(menuItem);        
        menuItem = new JMenuItem("Open histograms file...");
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms to file...");
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Set range...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set histogram range");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("View all fits");
        menuItem.getAccessibleContext().setAccessibleDescription("View all histograms");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("View all constants");
        menuItem.getAccessibleContext().setAccessibleDescription("View all constants");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set analysis update interval...", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);

        // create detector panel
        detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new FTHodoDetector("FTHodo");
        initDetector();
        detectorPanel.add(detectorView);
        
        // create module viewer
//        modules.add(new FTElasticCalibration(detectorView,"ElasticCalibration"));
        modules.add(new FTEnergyCalibration(detectorView,"EnergyCalibration"));
        modules.add(new FTTimeCalibration(detectorView,"TimeCalibration"));
        modules.add(new FTPedestalCalibration(detectorView,"PedestalCalibration"));
//        modules.add(new FTTimeCalibration(detectorView,"TimeCalibration"));
//        modules.add(new FTPedestalCalibration(detectorView,"PedestalCalibration"));
//        modules.add(new FTEnergyCorrection(detectorView,"EnergyCorrection"));
        modulePanel = new JTabbedPane();
        for(int k=0; k<modules.size(); k++) {
            modulePanel.add(modules.get(k).getName(),modules.get(k).getView());
            if(moduleSelect == null) moduleSelect = modules.get(k).getName();
        }
        modulePanel.addChangeListener(this);
        
        // create split panel to host detector view and canvas+constants view
        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorPanel);
        splitPanel.setRightComponent(modulePanel);
        splitPanel.setDividerLocation(0.3);        
        splitPanel.setResizeWeight(0.3);


        // create data processor panel
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);
        processorPane.addEventListener(this);
    
        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
        this.setCanvasUpdate(canvasUpdateTime);
        
        // init constants manager
        ccdb.init(Arrays.asList(new String[]{
                    "/daq/tt/fthodo",
                    "/daq/fadc/fthodo",
                    "/calibration/ft/fthodo/charge_to_energy",
                    "/calibration/ft/fthodo/time_offsets"}));
        
    }
    
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand()=="Set analysis update interval...") {
            this.chooseUpdateInterval();
        }
        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).adjustFit();
                }
            } 
        }        
        if(e.getActionCommand()=="Open histograms file...") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(this.workDir + "/FTHodoCalib-histos");
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms to file...") {
            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Save histograms...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftHodoCalib_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir + "/FTHodoCalib-histos");
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        if(e.getActionCommand() == "Set range...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).setRange();
                }
            } 
        }        
        if(e.getActionCommand() == "View all fits") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).showPlots();
                }
            } 
        }
        if(e.getActionCommand() == "View all constants") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).showConstants();
                }
            } 
        }
        if(e.getActionCommand()=="Load...") {
            String filePath = null;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose Constants Folder...");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int returnValue = fc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               filePath = fc.getSelectedFile().getAbsolutePath();            
            }
            for(int k=0; k<this.modules.size(); k++) {
                this.modules.get(k).loadConstants(filePath);
            }
        }
        if(e.getActionCommand()=="CCDB...") {            
            for(int k=0; k<this.modules.size(); k++) {
                this.modules.get(k).loadConstants(ccdb);
            }
        }
        if(e.getActionCommand()=="Save...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String dirName = "ftHodoCalib_" + this.runNumber + "_" + df.format(new Date());
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(dirName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               dirName = fc.getSelectedFile().getAbsolutePath();            
            }
            File theDir = new File(dirName);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try{
                    theDir.mkdir();
                    result = true;
                } 
                catch(SecurityException se){
                    //handle it
                }        
                if(result) {    
                System.out.println("Created directory: " + dirName);
                }
            }
            for(int k=0; k<this.modules.size(); k++) {
                this.modules.get(k).saveConstants(dirName);
            }
        }
        if(e.getActionCommand()=="Update table") {
            for(int k=0; k<this.modules.size(); k++) {
                this.modules.get(k).updateTable();
            }
        }
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }

        
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum = bank.getInt("run", 0);
        }
        return rNum;
    }

    public void initDetector() {
        detectorView.setThresholds(0);
        detectorView.getView().addDetectorListener(this);
        for(String layer : detectorView.getView().getLayerNames()){
            detectorView.getView().setDetectorListener(layer,this);
         }
        detectorView.updateBox();
    }
     
    @Override
    public void dataEventAction(DataEvent de) {
        
        if(de!=null) this.runNumber = this.getRunNumber(de);
        if (de.getType()==DataEventType.EVENT_START) {
                //System.out.println(" EVENT_START");
        }
        else if (de.getType()==DataEventType.EVENT_ACCUMULATE) {
               // System.out.println(" EVENT_ACCUMULATE" + i);
        }
        else if (de.getType()==DataEventType.EVENT_SINGLE) {
             //   System.out.println("EVENT_SINGLE from CalibrationViewer");
        }
        else if (de.getType()==DataEventType.EVENT_STOP) {
               // System.out.println(" EVENT_STOP else");
               // System.out.println(" Analyzed");
        } 
	for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).dataEventAction(de);
        }
        this.detectorView.repaint();

    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).readDataGroup(dir);
        }
    }
    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String data = this.workDir + "/Plots/clas12rec_run_" + this.runNumber + "_" + df.format(new Date());        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("Directory does not exist: " + data);
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        String fileName = data + "/clas12_canvas.png";
        System.out.println(fileName);
    }
    
    public void timerUpdate() {
        this.detectorView.repaint();
	for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).timerUpdate();
        }
        
    }

    public void resetEventListener() {
	for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).resetEventListener();
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
	for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).processShape(dsd);
        }
    }

    @Override
    public void update(DetectorShape2D dsd) {
//        System.out.println("Changing color");
	for(int k=0; k<this.modules.size(); k++) {
            if(this.modules.get(k).getName()==moduleSelect) {
                Color col = this.modules.get(k).getColor(dsd);
                dsd.setColor(col.getRed(), col.getGreen(), col.getBlue());
            }
        }
    }
    
   public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
            
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).setCanvasUpdate(time);
        }
    }

   public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        moduleSelect = sourceTabbedPane.getTitleAt(index);
        this.detectorView.repaint();
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalibrationViewer viewer = new CalibrationViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }


}
