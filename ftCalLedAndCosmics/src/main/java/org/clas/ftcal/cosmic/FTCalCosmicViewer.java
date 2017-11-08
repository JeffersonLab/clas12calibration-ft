/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.cosmic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clas.detector.CodaEventDecoder;
import org.clas.detector.DetectorDataDgtz;
import org.clas.detector.DetectorEventDecoder;
import org.clas.ftcal.tools.FTCalDetector;
import org.clas.ftcal.tools.FTModule;
import org.clas.ftcal.tools.FTParameter;
import org.clas.ftcal.tools.FTViewer;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorShape2D;


import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;


/**
 *
 * @author gavalian
 */
public class FTCalCosmicViewer extends FTViewer implements DetectorListener, IDataEventListener, ActionListener, ChangeListener {

    // Panels
    JPanel                      mainPanel = new JPanel();
    JPanel                  detectorPanel = new JPanel();
    JPanel               colorSchemePanel = new JPanel();
    JComboBox                   colorList = new JComboBox();

    DataSourceProcessorPane        evPane = new DataSourceProcessorPane();
    
    // Detector
    FTCalDetector                detector = null;
    
    // Analysis Modules
    ArrayList<FTModule>           modules = new ArrayList();

    // Decoders
    CodaEventDecoder              decoder = new CodaEventDecoder();
    DetectorEventDecoder  detectorDecoder = new DetectorEventDecoder();
 
    // Constants
    ConstantsManager                 ccdb = new ConstantsManager();
    CalibrationConstantsView summaryTable = null; 
    CalibrationConstants            calib = null;

    // Analysis parameters
    Map<FTParameter,Integer>   parameters = new LinkedHashMap<FTParameter,Integer>();
    
    // Tabs
    Map<String,Integer>              tabs = new LinkedHashMap<String,Integer>();
    
    
    int    keySelect       = 8;
    int    moduleParSelect = 0;
    int    moduleTabSelect = 0;
    int    timerUpdate     = 3000;
    int    eventUpdate     = 500;
    String parameterSelect = null;
    
    int    threshold       = 6;
    int    nProcessed      = 0;
    int    runNumber       = 0;
    String workDir         = null;
   

    public FTCalCosmicViewer() {
        this.initDetector();
        this.initPulseFitter();
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to:" + this.workDir);

        // create analysis modules
        modules.add(new FTCalEventModule(this.detector));
        modules.add(new FTCalNoiseModule(this.detector));
        modules.add(new FTCalCosmicModule(this.detector));
        for(FTModule module : this.modules) {
            module.setKeySelect(keySelect);
            module.setCanvasUpdate(timerUpdate);
            module.resetEventListener();
        }
        // add them to tabbed canvas
        JTabbedPane tabbedPane = new JTabbedPane();
        for(int i=0; i<this.modules.size(); i++) {
            for(Map.Entry<String, EmbeddedCanvas> entry : this.modules.get(i).getCanvases().entrySet()) {
                tabbedPane.add(entry.getKey(), entry.getValue());
                tabs.put(entry.getKey(), i);
            }
            if(this.modules.get(i).getConstants()!=null) {
                if(this.modules.get(i).getConstants().getColumnCount()>3) {
                    tabbedPane.add(this.modules.get(i).getName() + "Table", this.modules.get(i).getTable());
                    tabs.put(this.modules.get(i).getName() + "Table", i);
                }
            }
        } 
        tabbedPane.addChangeListener(this);
        // collect parameters
        for(int i=0; i<this.modules.size(); i++) {
            for(FTParameter par : this.modules.get(i).getParameters()) {
                this.parameters.put(par, i);
            }
        }
        // setup color scheme chooer
        this.setColorChooser();
        // setup detector panel
        this.detectorPanel.setLayout(new BorderLayout());
        this.detectorPanel.add(this.detector, BorderLayout.CENTER);
        this.detector.add(this.colorSchemePanel, BorderLayout.PAGE_START);
        this.detectorPanel.add(this.detectorDecoder.getFadcPanel(), BorderLayout.PAGE_END);
        // define split Panel
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(this.detectorPanel);
        splitPane.setRightComponent(tabbedPane);
        splitPane.setDividerLocation(0.4);        
        splitPane.setResizeWeight(0.4);        
        // set main panel layout
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(splitPane, BorderLayout.CENTER);
        this.mainPanel.add(evPane, BorderLayout.PAGE_END);
        this.evPane.setUpdateRate(eventUpdate);
        this.evPane.addEventListener(this);
        // set menu bar
        this.addFileMenu();
//        this.addConstantMenu();
        this.addFitMenu();
        this.addSettingsMenu();
        this.addTableMenu();
    }

    private void initPulseFitter() {
            System.out.println("\nInitializing connection to CCDB"); 
            ccdb.init(Arrays.asList(new String[]{
                    "/daq/fadc/ftcal",
                    "/daq/tt/ftcal"}));
            this.detectorDecoder.getFadcPanel().init(ccdb,11,"/daq/fadc/ftcal", 70,3,1); 
            this.detectorDecoder.getFadcPanel().setMode1(1, 15, 45, 80, 7);           
     }

    private void initDetector() {
        this.detector = new FTCalDetector("FTCAL");
        this.detector.setThresholds(this.threshold);
        detector.getView().addDetectorListener(this);
        for(String layer : detector.getView().getLayerNames()){
            detector.getView().setDetectorListener(layer,this);
         }
        detector.updateBox();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand() == "comboBoxChanged") {
            this.updateScale();
        }
        if(e.getActionCommand() == "Load files...") {
            this.readFiles();
        }        
        if(e.getActionCommand()=="Open histograms file...") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(this.workDir + "/" + this.getName());
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms...") {
//            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Save histograms...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftCalCosmic_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir + "/" + this.getName());
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleTabSelect).adjustFit();
        }        
        if(e.getActionCommand() == "View all") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleTabSelect).showPlots();
        }        
        if(e.getActionCommand()=="Choose work directory...") {
            String filePath = this.workDir;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose work directory...");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int returnValue = fc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               filePath = fc.getSelectedFile().getAbsolutePath();            
            }
            this.workDir = filePath;
            System.out.println("\nCurrent work directory set to:" + this.workDir);
        }
        // Table menu bar
        if(e.getActionCommand()=="Save table...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftCalLed_" + this.runNumber + "_" + df.format(new Date()) + ".txt";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.modules.get(moduleTabSelect).saveTable(fileName);
        }
        if(e.getActionCommand()=="Clear table") {
            this.modules.get(moduleTabSelect).resetTable();
        }
        
    }
        
    @Override
    public void dataEventAction(DataEvent event) {

        if (event.getType() == DataEventType.EVENT_START) {
//            resetEventListener();
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {
            processEvent(event);
            for(FTModule module : this.modules) {
                module.plotDataGroup();
            }
            this.detector.repaint();
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            for(FTModule module : this.modules) {
                module.analyze();
            }
	}        

    }
        
    public String getAuthor() {
        return "De Vita";
    }

    public String getName() {
        return "FTCalCosmic";
    }

    public String getDescription() {
        return "FT Cosmic Display";
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }

    public DetectorType getType() {
        return DetectorType.FTCAL;
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

    public void processEvent(DataEvent event) {
        nProcessed++;
        if (event instanceof EvioDataEvent) {
            try {
                List<DetectorDataDgtz> dataList = decoder.getDataEntries((EvioDataEvent) event);
                if (this.runNumber != decoder.getRunNumber()) {
                    this.runNumber = decoder.getRunNumber();
                    System.out.println("\nRun number " + this.runNumber);
                }
                detectorDecoder.translate(dataList);
                detectorDecoder.fitPulses(dataList);
                List<DetectorDataDgtz> counters = new ArrayList<DetectorDataDgtz>();
                for (DetectorDataDgtz entry : dataList) {
                    if (entry.getDescriptor().getType() == DetectorType.FTCAL) {
                        if (entry.getADCSize() > 0) {
                            counters.add(entry);
                        }
                    }
                }
                for(FTModule module : this.modules) module.addEvent(counters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
    }
    
    @Override
    public void processShape(DetectorShape2D shape) {
        int sector    = shape.getDescriptor().getSector();
        int layer     = shape.getDescriptor().getLayer();
        keySelect = shape.getDescriptor().getComponent();
        for(FTModule module : this.modules) {
            module.setKeySelect(keySelect);
            module.plotDataGroup();
        }
    }

    private void readFiles() {
        EvioSource reader = new EvioSource();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose input files directory...");
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        File workingDirectory = new File(this.workDir);
        fc.setCurrentDirectory(workingDirectory);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            int nf = 0;
            for (File fd : fc.getSelectedFiles()) {
                if (fd.isFile()) {
                    if (fd.getName().contains(".evio") || fd.getName().contains(".hipo")) {
                        reader.open(fd);
                        Integer current = reader.getCurrentIndex();
                        Integer nevents = reader.getSize();
                        System.out.println("\nFILE: " + nf + " " + fd.getName() + " N.EVENTS: " + nevents.toString() + "  CURRENT : " + current.toString());
                        for (int k = 0; k < nevents; k++) {
                            if (reader.hasEvent()) {
                                DataEvent event = reader.getNextEvent();
                                this.dataEventAction(event);
                                if(k % 1000 == 0) System.out.println("Analyzed " + k + " events");
                            }
                        }
                        for(FTModule module : this.modules) {
                            module.analyze();
                        }
                        nf++;
                    }
                }
            }
        }
    }
    
    @Override
    public void resetEventListener() {
        for(FTModule module : this.modules) {
            module.resetEventListener();
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
   
    private void setColorChooser() {
        this.colorSchemePanel.setLayout(new FlowLayout());
        for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
            this.colorList.addItem(entry.getKey().getName());
        }
        this.colorList.setSelectedIndex(0);        
        this.colorList.addActionListener(this);
        this.colorSchemePanel.add(this.colorList);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        this.moduleTabSelect = this.tabs.get(sourceTabbedPane.getTitleAt(index));
        //System.out.println("Tab changed to " + sourceTabbedPane.getTitleAt(index) + " with module index " + this.moduleTabSelect);
    }
  
    @Override
    public void timerUpdate() {
        this.updateScale();
        this.detector.repaint();
        for(FTModule module : this.modules) {
            module.analyze();
            module.plotDataGroup();
        }
    }

    @Override
    public void update(DetectorShape2D shape) {
        int sector = shape.getDescriptor().getSector();
        int layer = shape.getDescriptor().getLayer();
        int component = shape.getDescriptor().getComponent();
        Color col = new Color(100, 100, 100);
        col = this.modules.get(this.moduleParSelect).getColor(component,this.parameterSelect);
        shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
    }

    private void updateScale() {
        String item = (String) this.colorList.getSelectedItem();  
        for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
            if(item == entry.getKey().getName()) {
                this.moduleParSelect = entry.getValue();
                this.parameterSelect = item;
                this.detector.getView().getColorAxis().setRange(0, entry.getKey().getLimit());
                this.detector.getView().repaint();
                break;
            }
        }    
    }

    public static void main(String[] args) {
        FTCalCosmicViewer module = new FTCalCosmicViewer();
        JFrame frame = new JFrame();
        frame.add(module.getPanel());
        frame.pack();
        frame.setJMenuBar(module.getMenuBar());
        frame.setSize(1400, 800);        
        frame.setVisible(true);
    }    

}
