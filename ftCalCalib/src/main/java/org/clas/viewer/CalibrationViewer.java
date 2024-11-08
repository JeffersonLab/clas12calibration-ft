package org.clas.viewer;


import org.clas.ftdata.FTCalDataProvider;
import org.clas.ftdata.FTCalEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.FileSystems;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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
import org.clas.modules.FTElasticCalibration;
import org.clas.modules.FTEnergyCalibration;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.clas.modules.FTEnergyCorrection;
import org.clas.modules.FTPedestalCalibration;
import org.clas.modules.FTThresholdsCalibration;
import org.clas.modules.FTTimeCalibration;
import org.clas.modules.FTTimeWalkCalibration;
import org.clas.view.DetectorListener;
import org.clas.view.DetectorShape2D;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, DetectorListener, ChangeListener {
    
    public int i = 0;

    JPanel                   mainPanel     = null;
    JMenuBar                 menuBar       = null;
    FTProcessorPane          processorPane = null;
    JSplitPane               splitPanel    = null;
    JPanel                   detectorPanel = null;
    FTCalDetector            detectorView  = null;
    JTabbedPane               modulePanel  = null;
    String                    moduleSelect = null;
    JDialog                    configFrame = null;
    JTabbedPane                 configPane = new JTabbedPane();
    
    ConstantsManager                        ccdb = new ConstantsManager();
    Map<String,CalibrationConstants> globalCalib = new HashMap<>();
    
    FTCalDataProvider dataProvider = null;
    
    private int      canvasUpdateTime   = 2000;
    private int      analysisUpdateTime = 2000000;
    private int      runNumber          = 0;
    private List<List<String>> loadConstants = new ArrayList<>();
    private List<List<String>> saveConstants = new ArrayList<>();
    private String   constantsDir       = null;
    private boolean  quitWhenDone       = false;
    private int      currentIteration   = 0;
    private String   workDir            = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
   
    
    Map<String, FTCalibrationModule> modules = new LinkedHashMap();

    public static Logger LOGGER = Logger.getLogger(CalibrationViewer.class.getName());
    
    public CalibrationViewer(boolean quitWhenDone, double target, boolean vertex, boolean mctrue) {
       
        LOGGER.setLevel(Level.INFO);
        
        GStyle.setWorkingDirectory(workDir);

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
        menuItem = new JMenuItem("View all");
        menuItem.getAccessibleContext().setAccessibleDescription("View all histograms");
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
        menuItem = new JMenuItem("Set color map range...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set color map range");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set range...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set histogram range");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set reference calibration value...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set reference calibration value");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set calibration scale factor...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set calibration scale factor");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);

        // create detector panel
        detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new FTCalDetector("FTCal");
        initDetector();
        detectorPanel.add(detectorView);
        
        // init constants manager
        ccdb.init(Arrays.asList(new String[]{
                    "/calibration/ft/ftcal/charge_to_energy",
                    "/calibration/ft/ftcal/time_offsets",
                    "/calibration/ft/ftcal/time_walk",
                    "/calibration/ft/ftcal/energycorr",
                    "/daq/tt/ftcal"}));
        ccdb.setVariation("default");
        
        // create module viewer
        modules.put("EnergyCalibration",    new FTEnergyCalibration(detectorView,"EnergyCalibration",ccdb,globalCalib));
        modules.put("ElasticCalibration",   new FTElasticCalibration(detectorView,"ElasticCalibration",ccdb,globalCalib));
        modules.put("TimeCalibration",      new FTTimeCalibration(detectorView,"TimeCalibration",ccdb,globalCalib));
        modules.put("TimeWalk",             new FTTimeWalkCalibration(detectorView,"TimeWalk",ccdb,globalCalib));
        modules.put("PedestalCalibration",  new FTPedestalCalibration(detectorView,"PedestalCalibration",ccdb,globalCalib));
        modules.put("ThresholdCalibration", new FTThresholdsCalibration(detectorView,"ThresholdCalibration",ccdb,globalCalib));
        modules.put("EnergyCorrection",     new FTEnergyCorrection(detectorView,"EnergyCorrection",ccdb,globalCalib));
        modulePanel = new JTabbedPane();
        for(String name : this.modules.keySet()) {
            modulePanel.add(name,modules.get(name).getView());
        }
        modulePanel.addChangeListener(this);
        if(moduleSelect == null) moduleSelect = "EnergyCalibration";
        this.modules.get(moduleSelect).processShape(detectorView.getDefaultShape());
        this.detectorView.repaint();
        
        dataProvider = new FTCalDataProvider(detectorView, mctrue);
        
        // create split panel to host detector view and canvas+constants view
        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorPanel);
        splitPanel.setRightComponent(modulePanel);
        splitPanel.setDividerLocation(0.2);        
        splitPanel.setResizeWeight(0.2);


        // create data processor panel
        processorPane = new FTProcessorPane();
        processorPane.addEventListener(this);
        processorPane.setUpdateRate(analysisUpdateTime);
    
        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
        this.setCanvasUpdate(canvasUpdateTime);
        
        this.quitWhenDone  = quitWhenDone;
        FTCalConstants.setVertex(target);
     
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if("Set analysis update interval...".equals(e.getActionCommand())) {
            this.chooseUpdateInterval();
        }
        if("Adjust fit...".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleSelect).adjustFit();
        }        
        if("Set range...".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleSelect).setRange();
        }        
        if("Set color map range...".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleSelect).setCols();
            this.detectorView.repaint();
        }        
        if("Set reference calibration value...".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleSelect).setReference();
            this.detectorView.repaint();
        }        
        if("Set calibration scale factor...".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.modules.get(moduleSelect).setScaleShift();
            this.detectorView.repaint();
        }        
        if("Open histograms file...".equals(e.getActionCommand())) {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(this.workDir + "/FTCalCalib-histos");
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if("Print histograms to file...".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH.mm.ss");
            String dirName = "ftCalCalib_" + this.runNumber + "_" + df.format(new Date());
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(dirName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               dirName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.savePictures(dirName);
        }
        if("Save histograms...".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH.mm.ss");
            String fileName = "ftCalCalib_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir + "/FTCalCalib-histos");
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        if("View all".equals(e.getActionCommand())) {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(String name : this.modules.keySet()) {
                if(this.modules.get(name).getName().equals(moduleSelect)) {
                    this.modules.get(name).showPlots();
                }
            } 
        }
        if("Load...".equals(e.getActionCommand())) {
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
            this.loadConstants(filePath);
        }
        if("Save...".equals(e.getActionCommand())) {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH.mm.ss");
            String dirName = "ftCalCalib_" + this.runNumber + "_" + df.format(new Date());
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(dirName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               dirName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveConstants(dirName);
        }
        if("Update table".equals(e.getActionCommand())) {
            for(String name : this.modules.keySet()) {
                this.modules.get(name).updateTable();
            }
        }
        if (e.getActionCommand().compareTo("Next")==0) {
            int currentTab = configPane.getSelectedIndex();
            for (int i=currentTab+1; i<configPane.getTabCount(); i++) {
                if (configPane.isEnabledAt(i)) {
                    configPane.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (e.getActionCommand().compareTo("Back")==0) {
            int currentTab = configPane.getSelectedIndex();
            for (int i=currentTab-1; i>=0; i--) {
                if (configPane.isEnabledAt(i)) {
                    configPane.setSelectedIndex(i);
                    break;
                }
            }        
        }
        if (e.getActionCommand().compareTo("Cancel")==0) {
                System.exit(0);
        }
        if (e.getActionCommand().compareTo("Finish")==0) {
            configFrame.setVisible(false);

            System.out.println("");
            System.out.println("Configuration settings - Previous calibration values");
            System.out.println("----------------------------------------------------");
            // get the previous iteration calibration values
            for(String name : this.modules.keySet()) {
                this.modules.get(name).loadConstants();
            }
            dataProvider.loadConstants(globalCalib);
        }
    }

    public void addIteration(String toLoad, String toSave) {
        this.loadConstants.add(this.arrayToList(toLoad));
        this.saveConstants.add(this.arrayToList(toSave));    
    }
    
    private List<String> arrayToList(String s) {
        String[] ms = s.split(":");
        List<String> ls = new ArrayList<>();
        if(ms.length>0) {
            for(String name : ms) {
                if(this.modules.containsKey(name))
                    ls.add(name);
            }
        }
        return ls;
    }
    
    public void configureFrame() {

        configFrame = new JDialog(new JFrame("Select FTCal calibration settings"), "Select FTCal calibration settings");
        configFrame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        configFrame.setSize(900, 1000);
        //configFrame.setSize(1000, 600); // vnc size
        configFrame.setLocationRelativeTo(mainPanel);
        configFrame.setDefaultCloseOperation(configFrame.DO_NOTHING_ON_CLOSE);

        // Which steps    
        JPanel stepOuterPanel = new JPanel(new BorderLayout());
        JPanel stepPanel      = new JPanel(new GridBagLayout());
        stepOuterPanel.add(stepPanel, BorderLayout.NORTH);
        GridBagConstraints c  = new GridBagConstraints();

        int k = 0;
        for(String name : this.modules.keySet()) {
            c.gridx = 0; c.gridy = k;
            c.anchor = c.WEST;
            JCheckBox stepCheck = new JCheckBox();
            stepCheck.setName(name);
            stepCheck.setText(name);
            stepCheck.setSelected(true);
            stepCheck.addActionListener(this);
            stepPanel.add(stepCheck,c);
            k++;
        }
		
        JPanel butPage1 = new configButtonPanel(this, false, "Next");
        stepOuterPanel.add(butPage1, BorderLayout.SOUTH);

        //configPane.add("Select steps", stepOuterPanel);    

        // Previous calibration values
        JPanel confOuterPanel = new JPanel(new BorderLayout());
        Box confPanel = new Box(BoxLayout.Y_AXIS);
        
        for(String name : this.modules.keySet()) {
            FTPrevConfigPanel configPanel = new FTPrevConfigPanel(this.modules.get(name));
            confPanel.add(configPanel);
        }
		
        JPanel butPage = new configButtonPanel(this, true, "Finish");
        confOuterPanel.add(confPanel, BorderLayout.NORTH);
        confOuterPanel.add(butPage, BorderLayout.SOUTH);

        configPane.add("Previous calibration values", confOuterPanel);

        configFrame.add(configPane);
        configFrame.setVisible(true);

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
        
        if(de!=null && this.getRunNumber(de)>0) {
            int run = this.getRunNumber(de);
            if(runNumber!=run) {
                runNumber = run;
                for(String name : this.modules.keySet()) {
                    this.modules.get(name).loadConstants(runNumber);
                }
                dataProvider.loadConstants(globalCalib);
            }
        }
        
        if (de.getType()==DataEventType.EVENT_START)
            System.out.println("\nStarting iteration " + currentIteration);
        
        if (de.getType()==DataEventType.EVENT_START ||
            de.getType()==DataEventType.EVENT_ACCUMULATE ||
            de.getType()==DataEventType.EVENT_SINGLE) {
            FTCalEvent ftEvent = dataProvider.getEvent(de);
            for(String name : this.modules.keySet()) {
                this.modules.get(name).dataEventAction(ftEvent);
            }
        }
        else if (de.getType()==DataEventType.EVENT_STOP) {
            
            System.out.println("EVENT_STOP");           

            for(String name : this.modules.keySet()) {
                this.modules.get(name).analyze();
                this.modules.get(name).updateTable();
                this.modules.get(name).processShape(detectorView.getDefaultShape());
            }
            this.detectorView.repaint(); 

            for(String name : saveConstants.get(currentIteration)) {
                this.modules.get(name).updatePreviousConstants();
            }
            this.saveAll();

            currentIteration++;
            if(currentIteration<loadConstants.size()) {
                System.out.println("\nResetting for iteration " + currentIteration);
                this.dataProvider.loadConstants(globalCalib);
                wait(5000);
                this.processorPane.setHipo4File(this.processorPane.getDataFile());
            }
            else if(this.quitWhenDone)  {
                System.exit(0);
            }
        }
    }

    public void loadConstants(String path) {
        if(!path.isEmpty())
            this.constantsDir = path;
        if(loadConstants==null || loadConstants.size()==0) {
            loadConstants.add(new ArrayList<>(this.modules.keySet()));
            saveConstants.add(new ArrayList<>(this.modules.keySet()));
        }
        for(String name : loadConstants.get(currentIteration)) {
            String fileName = path + "/" + name + ".txt";
            this.modules.get(name).calDBSource = FTCalibrationModule.CAL_FILE;
            this.modules.get(name).prevCalFilename = fileName;
            this.modules.get(name).loadConstants();
        }
        dataProvider.loadConstants(globalCalib);        
    }
    
    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(String name : this.modules.keySet()) {
            this.modules.get(name).readDataGroup(dir);
        }
        this.detectorView.repaint();
    }
    
    public void saveAll() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH.mm.ss");
        String dirName = String.format("%s/ftCalCalib_%06d", this.workDir, this.runNumber);
        String saveDir =  dirName + "_" + df.format(new Date());
        this.saveConstants(saveDir);
        this.savePictures(saveDir);
        if(this.constantsDir==null)
            this.saveConstants(dirName);
        else 
            this.saveConstants(this.constantsDir);
    }
    
    public void saveConstants(String dirName) {
        System.out.println();
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
        for(String name : loadConstants.get(currentIteration)) {
            this.modules.get(name).savePreviousConstants(dirName);
        }
    }
    
    public void savePictures(String dirName) {
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
        for(String name : this.modules.keySet()) {
            this.modules.get(name).savePicture(dirName);
        }
    }
    
    @Override
    public void timerUpdate() {
        if(this.processorPane.getProgress()>0) {
            for(String name : this.modules.keySet()) {
                this.modules.get(name).timerUpdate();
            }
            this.detectorView.repaint(); 
        }
        System.out.println(this.processorPane.getStatus().getText());
        wait(5000);
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting modules");
	for(String name : this.modules.keySet()) {
            this.modules.get(name).resetEventListener();
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
	for(String name : this.modules.keySet()) {
            this.modules.get(name).processShape(dsd);
        }
    }

    @Override
    public void update(DetectorShape2D dsd) {
//        System.out.println("Changing color");
	for(String name : this.modules.keySet()) {
            if(name.equals(moduleSelect)) {
                Color col = this.modules.get(name).getColor(dsd);
                dsd.setColor(col.getRed(), col.getGreen(), col.getBlue());
                dsd.setCounter(this.modules.get(name).getNEvents(dsd));
            }
        }
    }
    
   public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(String name : this.modules.keySet()) {
            this.modules.get(name).writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
            
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for(String name : this.modules.keySet()) {
            this.modules.get(name).setCanvasUpdate(time);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        moduleSelect = sourceTabbedPane.getTitleAt(index);
        this.detectorView.repaint();
    }

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args){
        
        OptionParser parser = new OptionParser("ftCalCalib");
        
        parser.addOption("-c", "0",  "calibrate (0/1)");
        parser.addOption("-d", "",   "path to previous calibration constants folder");
        parser.addOption("-l", "",   "colon-separated list of modules that should load constants from text files");
        parser.addOption("-m", "0",  "use MC true information");
        parser.addOption("-n", "1",  "number of iterations");
        parser.addOption("-q", "0",  "quit when completed (0=false; 1=true)");
        parser.addOption("-s", "",   "colon-separated list of modules that should save constants to text files");
        parser.addOption("-t", "-3", "target position in cm");
        parser.addOption("-v", "0",  "use target center (0) or trigger particle vertex to set the FT particle vertex");
        parser.addOption("-w", "1",  "open GUI window (0=false; 1=true)");
        
        parser.parse(args);
        
        if(parser.getInputList().isEmpty()) {
            System.out.println("[ERROR] No input file was specified, exiting");
            System.exit(1);
        }
        
        boolean calibrate      = parser.getOption("-c").intValue()!=0;
        String  constantsDir   = parser.getOption("-d").stringValue();
        String  loadConstants  = parser.getOption("-l").stringValue();
        boolean useMCTrueInfo  = parser.getOption("-m").intValue()!=0;
        int     nIterations    = parser.getOption("-n").intValue();
        boolean quitWhenDone   = parser.getOption("-q").intValue()!=0;
        String  saveConstants  = parser.getOption("-s").stringValue();
        double  targetPosition = parser.getOption("-t").doubleValue();
        boolean vertexMode     = parser.getOption("-v").intValue()==1;
        boolean openWindow     = parser.getOption("-w").intValue()==1;
        
        DefaultLogger.initialize();
        if(!openWindow) System.setProperty("java.awt.headless", "true");
        
        CalibrationViewer viewer = new CalibrationViewer(quitWhenDone, targetPosition, vertexMode, useMCTrueInfo);
        if(calibrate) {
            for(int i=0; i<nIterations; i++) {
                viewer.addIteration("EnergyCalibration", "EnergyCalibration");
            }
            viewer.addIteration("EnergyCalibration:TimeCalibration", "TimeCalibration");
            viewer.addIteration("EnergyCalibration:TimeCalibration:TimeWalk", "TimeWalk");
            viewer.addIteration("EnergyCalibration:TimeCalibration:TimeWalk", "TimeCalibration");
            viewer.addIteration("EnergyCalibration:TimeCalibration:TimeWalk", "");
        }
        else if(!loadConstants.isBlank() || !saveConstants.isBlank()) {
            for(int i=0; i<nIterations; i++) {
                viewer.addIteration(loadConstants, saveConstants);
            }
        }
        
        if(openWindow) {
            JFrame frame = new JFrame(parser.getInputList().get(0));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(viewer.mainPanel);
            frame.setJMenuBar(viewer.menuBar);
            frame.setSize(1600, 900);
            frame.setVisible(true);
        }
        
        if(constantsDir.isBlank())
            viewer.configureFrame();
        else 
            viewer.loadConstants(constantsDir);

        viewer.processorPane.setHipo4File(parser.getInputList().get(0));
    }


}
