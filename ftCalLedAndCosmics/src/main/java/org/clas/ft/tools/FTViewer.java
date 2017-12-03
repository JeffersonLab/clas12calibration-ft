/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clas.detector.CodaEventDecoder;
import org.clas.detector.DetectorEventDecoder;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author devita
 */
public class FTViewer implements ActionListener, DetectorListener, CalibrationConstantsListener, ChangeListener, IDataEventListener {
    
    // Panels
    JPanel                      mainPanel = new JPanel();
    JPanel                  detectorPanel = new JPanel();
    JPanel               colorSchemePanel = new JPanel();
    JComboBox                   colorList = new JComboBox();
    JTabbedPane                tabbedPane = new JTabbedPane(); 
    DataSourceProcessorPane        evPane = null;

    // Detector
    FTDetector                   detector = null;
    
    // Constants
    ConstantsManager                 ccdb = new ConstantsManager();
    CalibrationConstantsView   calibTable = new CalibrationConstantsView();
    CalibrationConstants   calibConstants = null;

    // Decoders
    CodaEventDecoder              decoder = new CodaEventDecoder();
    DetectorEventDecoder  detectorDecoder = new DetectorEventDecoder();
    
    // Analysis Modules
    ArrayList<FTModule>           modules = new ArrayList();

    // Analysis parameters
    Map<FTParameter,Integer>   parameters = new LinkedHashMap<FTParameter,Integer>();
    
    // Tabs
    Map<String,Integer>              tabs = new LinkedHashMap<String,Integer>();

    JMenuBar menuBar = new JMenuBar();    

    int    keySelect       = 8;
    int    moduleParSelect = 0;
    int    moduleTabSelect = 0;
    int    moduleSelect    = 0;
    int    timerUpdate     = 3000;
    int    eventUpdate     = 500;
    String parameterSelect = null;    
    String workDir         = null;

    int    runNumber       = 0;
    
    public FTViewer() {
        this.initDetector();
        this.initPulseFitter();
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to:" + this.workDir);

    }

    public String getAuthor() {
        return "De Vita";
    }

    public String getName() {
        return "FT";
    }

    public String getDescription() {
        return "FT Display";
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
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName, false);
        }        
        if(e.getActionCommand()=="Print histograms...") {
//            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Save histograms...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftCalCosmic_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        if(e.getActionCommand()=="Load reference...") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName, true);
        }        
        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.getModules().get(moduleTabSelect).adjustFit();
        }        
        if(e.getActionCommand() == "Adjust all fit ranges...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.getModules().get(moduleTabSelect).adjustAllFitRanges();
        }        
        if(e.getActionCommand() == "View all") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.getModules().get(moduleTabSelect).showPlots();
        }        
        if(e.getActionCommand()=="Set analysis parameters...") {
            this.getModules().get(moduleSelect).setAnalysisParameters();
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
            this.saveTable(fileName);
        }
        if(e.getActionCommand()=="Clear table") {
            this.resetTable();
        }
        if(e.getActionCommand()=="Update table") {
            this.updateTable();
        }
        
    }
   
    public void addConstants() {
        String columnNames = "";
        for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
            if(columnNames.equals("")) columnNames = entry.getKey().getName();
            else {
                columnNames = columnNames + ":";
                columnNames = columnNames + entry.getKey().getName() + "/F";
            }
        }
        this.calibConstants = new CalibrationConstants(3, columnNames); 
        int icol=0;
        for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
            FTParameter par = entry.getKey();
            if(par.getType()) this.calibConstants.addConstraint(3+icol, 0, 0);
            else              this.calibConstants.addConstraint(3+icol, par.getMin(), par.getMax());
            icol++;
        }
        this.calibConstants.setPrecision(3);
        for (int component : this.detector.getDetectorComponents()) this.calibConstants.addEntry(1, 1, component);
        this.calibConstants.fireTableDataChanged();
        this.resetTable();
        this.calibTable.addConstants(this.calibConstants,this);
    }

    public void addConstantMenu() {
        JMenuItem menuItem;
        JMenu constants = new JMenu("Constants");
        menuItem = new JMenuItem("Load...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Load constants from file");
        menuItem.addActionListener(this);
        constants.add(menuItem);        
        menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save constants to file");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        this.menuBar.add(constants);                
    }
 
    public void addFileMenu() {
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        menuItem = new JMenuItem("Load files...");
        menuItem.getAccessibleContext().setAccessibleDescription("Load files");
        menuItem.addActionListener(this);
        file.add(menuItem);        
        file.addSeparator();
        menuItem = new JMenuItem("Open histograms file...");
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms...");
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        file.addSeparator();
        menuItem = new JMenuItem("Load reference...");
        menuItem.getAccessibleContext().setAccessibleDescription("Load reference histograms");
        menuItem.addActionListener(this);
        file.add(menuItem);
        this.menuBar.add(file);  
    }
    
    public void addFitMenu() {
        JMenuItem menuItem;
        JMenu fit = new JMenu("Fit");
        menuItem = new JMenuItem("Adjust fit...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit parameters and range");
        menuItem.addActionListener(this);
        fit.add(menuItem);        
        menuItem = new JMenuItem("Adjust all fit ranges...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit range for all histograms");
        menuItem.addActionListener(this);
        fit.add(menuItem);        
        menuItem = new JMenuItem("View all");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        this.menuBar.add(fit);                
    }
        
    public void addSettingsMenu() {
        JMenuItem menuItem;
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set analysis parameters...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        this.menuBar.add(settings);
        menuItem = new JMenuItem("Choose work directory...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis work directory");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        this.menuBar.add(settings);
    }

    public void addTableMenu() {
        JMenuItem menuItem;
        JMenu table = new JMenu("Table");
        table.setMnemonic(KeyEvent.VK_A);
        table.getAccessibleContext().setAccessibleDescription("Table operations");
        menuItem = new JMenuItem("Save table...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save table content to file");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuItem = new JMenuItem("Clear table");
        menuItem.getAccessibleContext().setAccessibleDescription("Clear table content");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuItem = new JMenuItem("Update table");
        menuItem.getAccessibleContext().setAccessibleDescription("Update table content");
        menuItem.addActionListener(this);
        table.add(menuItem);
        this.menuBar.add(table);
    }

    public void addSummaryTable() {
        tabbedPane.add("Summary", this.calibTable);
        tabs.put("Summary", this.modules.size());
        tabbedPane.addChangeListener(this);
        // create summary table
        this.addConstants();
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
        for(FTModule module : this.modules) {
            module.setKeySelect(keySelect);
            module.plotDataGroups();
        }
    }
    
    @Override
    public void dataEventAction(DataEvent event) {

        if (event.getType() == DataEventType.EVENT_START) {
//            resetEventListener();
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {
            processEvent(event);
            for(FTModule module : this.getModules()) {
                module.plotDataGroups();
            }
            this.getDetector().repaint();
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            processEvent(event);
            if(this.evPane.getDataFile()!=null) {
                for(FTModule module : this.getModules()) {
                    module.analyze();
                }
                this.getDetector().repaint();
            }
	} 
        if(this.getModules().get(moduleSelect).getType())         this.getModules().get(moduleSelect).plotDataGroup();
        if(this.getModules().get(this.moduleParSelect).getType()) this.getDetector().repaint();

    }

    public ConstantsManager getConstantManager() {
        return ccdb;
    }

    public CodaEventDecoder getDecoder() {
        return decoder;
    }

    public DetectorEventDecoder getDetectorDecoder() {
        return detectorDecoder;
    }

    public DataSourceProcessorPane getEvPane() {
        return evPane;
    }

    public int getKeySelect() {
        return keySelect;
    }

    public FTDetector getDetector() {
        return detector;
    }

    public int getModuleParSelect() {
        return moduleParSelect;
    }

    public int getModuleSelect() {
        return moduleSelect;
    }

    public ArrayList<FTModule> getModules() {
        return modules;
    }

    public int getModuleTabSelect() {
        return moduleTabSelect;
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }
    
    public Map<FTParameter, Integer> getParameters() {
        return parameters;
    }

    public String getParameterSelect() {
        return parameterSelect;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public Map<String, Integer> getTabs() {
        return tabs;
    }

    public void initDetector() {

    }
    
    public void initModules() {
        for(FTModule module : this.getModules()) {
            module.setKeySelect(this.getKeySelect());
            module.setCanvasUpdate(timerUpdate);
            module.resetEventListener();
        }
        // add them to tabbed canvas
        for(int i=0; i<this.getModules().size(); i++) {
            for(Map.Entry<String, EmbeddedCanvas> entry : this.getModules().get(i).getCanvases().entrySet()) {
                tabbedPane.add(entry.getKey(), entry.getValue());
                tabs.put(entry.getKey(), i);
            }
        }
        // collect parameters
        for(int i=0; i<this.modules.size(); i++) {
            for(FTParameter par : this.modules.get(i).getParameters()) {
                this.parameters.put(par, i);
                if(this.parameterSelect == null) this.parameterSelect = par.getName();
            }
        }
    }

    public void initPanel() {
                 // setup color scheme chooer
        this.setColorChooser();
        // setup detector panel
        this.detectorPanel.setLayout(new BorderLayout());
        this.detectorPanel.add(this.getDetector(), BorderLayout.CENTER);
        this.getDetector().add(this.colorSchemePanel, BorderLayout.PAGE_START);
        this.detectorPanel.add(this.detectorDecoder.getFadcPanel(), BorderLayout.PAGE_END);
        // define split Panel
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(this.detectorPanel);
        splitPane.setRightComponent(tabbedPane);
        splitPane.setDividerLocation(0.3);        
        splitPane.setResizeWeight(0.3);        
        // set main panel layout
        this.evPane = new DataSourceProcessorPane();
        this.evPane.addEventListener(this);
        this.getEvPane().setUpdateRate(this.eventUpdate);
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(splitPane, BorderLayout.CENTER);
        this.mainPanel.add(this.evPane, BorderLayout.PAGE_END);
        // set menu bar
        this.addFileMenu();
//        this.addConstantMenu();
        this.addFitMenu();
        this.addSettingsMenu();
        this.addTableMenu();
    }
    
    public void initPulseFitter() {

    }
    
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    
    public void loadHistosFromFile(String fileName, boolean ref) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).readDataGroup(dir,ref);
        }
        this.updateTable();
    }
    
    public void processEvent(DataEvent event) {

    }

    @Override
    public void processShape(DetectorShape2D shape) {
        int sector    = shape.getDescriptor().getSector();
        int layer     = shape.getDescriptor().getLayer();
        keySelect = shape.getDescriptor().getComponent();
        for(FTModule module : this.getModules()) {
            module.setKeySelect(keySelect);
            module.plotDataGroups();
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
                                if(k % 1000 == 0) System.out.println("Read " + k + " events");
                            }
                        }
                        for(FTModule module : this.getModules()) {
                            module.analyze();
                        }
                        nf++;
                    }
                }
            }
            this.updateTable();
            System.out.println("Task completed");
        }
    }
    
    @Override
    public void resetEventListener() {
        for(FTModule module : this.modules) {
            module.resetEventListener();
            module.setNumberOfEvents(0);
        }
        this.resetTable();
    }
        
    public void resetTable() {
        if(this.calibConstants!=null) {
            for (int j=3; j<this.calibConstants.getColumnCount(); j++) {
                for (int component : this.detector.getDetectorComponents()) { 
                    this.calibConstants.setDoubleValue(-1.,this.calibConstants.getColumnName(j),1, 1, component);
                }
            }
            this.calibConstants.fireTableDataChanged();
        }
    }

    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.getModules().size(); k++) {
            this.getModules().get(k).writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
   
    public void saveTable(String name) {
       try {
            // Open the output file
            File outputFile = new File(name);
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);

            for (int i = 0; i < this.calibConstants.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < this.calibConstants.getColumnCount(); j++) {
                    line = line + this.calibConstants.getValueAt(i, j);
                    if (j < this.calibConstants.getColumnCount() - 1) {
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

    private void setColorChooser() {
        this.colorSchemePanel.setLayout(new FlowLayout());
        for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
            this.colorList.addItem(entry.getKey().getName());
        }
        this.colorList.setSelectedIndex(0);        
        this.colorList.addActionListener(this);
        this.colorSchemePanel.add(this.colorList);
    }
    
    public void setDetector(FTDetector detector) {
        this.detector = detector;
    }

    public void setKeySelect(int keySelect) {
        this.keySelect = keySelect;
    }
 
    public void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public void setModuleSelect(int moduleSelect) {
        this.moduleSelect = moduleSelect;
    }

    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }

    public void setTimerUpdate(int timerUpdate) {
        this.timerUpdate = timerUpdate;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        if(this.tabs.get(sourceTabbedPane.getTitleAt(index))<this.modules.size()) {
            this.moduleTabSelect = this.tabs.get(sourceTabbedPane.getTitleAt(index));
        }
        //System.out.println("Tab changed to " + sourceTabbedPane.getTitleAt(index) + " with module index " + this.moduleTabSelect);
    }
    
    @Override
    public void timerUpdate() {
        this.updateScale();
        this.getDetector().repaint();
        for(FTModule module : this.modules) {
            module.analyze();
            module.plotDataGroups();
        }
        this.updateTable();
    }

    @Override
    public void update(DetectorShape2D shape) {
        int sector = shape.getDescriptor().getSector();
        int layer = shape.getDescriptor().getLayer();
        int component = shape.getDescriptor().getComponent();
        Color col = new Color(100, 100, 100);
        col = this.getModules().get(this.moduleParSelect).getColor(component,this.parameterSelect);
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

    public void updateTable() {
        if(this.calibConstants!=null) {
            for(int key : this.detector.getDetectorComponents()) {
                for(Map.Entry<FTParameter, Integer> entry : this.parameters.entrySet()) {
                    FTParameter par = entry.getKey();
                    int imod = entry.getValue();
                    if(par.getType()) {
                        this.calibConstants.setDoubleValue(par.getStatus(this.modules.get(imod).getParameterValue(par.getName(), key)), par.getName(), 1, 1, key);
                    }
                    else {
                        this.calibConstants.setDoubleValue(this.modules.get(imod).getParameterValue(par.getName(), key), par.getName(), 1, 1, key);
                    }
                }
            }
            this.calibConstants.fireTableDataChanged();
        }
    }


}
