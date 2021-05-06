package org.clas.viewer;


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
import java.util.Map;
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
import org.jlab.io.task.DataSourceProcessorPane;
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

/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, DetectorListener, ChangeListener {
    
    public int i = 0;

    JPanel                   mainPanel     = null;
    JMenuBar                 menuBar       = null;
    DataSourceProcessorPane  processorPane = null;
    JSplitPane               splitPanel    = null;
    JPanel                   detectorPanel = null;
    FTCalDetector            detectorView  = null;
    JTabbedPane               modulePanel  = null;
    String                    moduleSelect = null;
    JFrame                innerConfigFrame = new JFrame("Select FTCal calibration settings");
    JDialog                    configFrame = new JDialog(innerConfigFrame, "Select FTCal calibration settings");
    JTabbedPane                 configPane = new JTabbedPane();
    
    ConstantsManager                        ccdb = new ConstantsManager();
    Map<String,CalibrationConstants> globalCalib = new HashMap<>();
    
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 200000;
    private int runNumber  = 0;
    private String workDir = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
    ArrayList<FTCalibrationModule> modules = new ArrayList();

    public CalibrationViewer() {
       
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
        menuItem = new JMenuItem("Set range...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set histogram range");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Set color map range...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set color map range");
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
                    "/calibration/ft/ftcal/energycorr",
                    "/daq/tt/ftcal"}));
        ccdb.setVariation("default");
        
        // create module viewer
        modules.add(new FTEnergyCalibration(detectorView,"EnergyCalibration",ccdb,globalCalib));
        modules.add(new FTElasticCalibration(detectorView,"ElasticCalibration",ccdb,globalCalib));
        modules.add(new FTTimeCalibration(detectorView,"TimeCalibration",ccdb,globalCalib));
        modules.add(new FTTimeWalkCalibration(detectorView,"TimeWalk",ccdb,globalCalib));
        modules.add(new FTPedestalCalibration(detectorView,"PedestalCalibration",ccdb,globalCalib));
        modules.add(new FTThresholdsCalibration(detectorView,"ThresholdCalibration",ccdb,globalCalib));
        modules.add(new FTEnergyCorrection(detectorView,"EnergyCorrection",ccdb,globalCalib));
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
        splitPanel.setDividerLocation(0.2);        
        splitPanel.setResizeWeight(0.2);


        // create data processor panel
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);
        processorPane.addEventListener(this);
    
        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
        this.setCanvasUpdate(canvasUpdateTime);
        
        configFrame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
     
    }
    
    @Override
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
            File workingDirectory = new File(this.workDir + "/FTCalCalib-histos");
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
        if(e.getActionCommand() == "Set range...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).setRange();
                }
            } 
        }        
        if(e.getActionCommand() == "Set color map range...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).setCols();
                }
            } 
            this.detectorView.repaint();
        }        
        if(e.getActionCommand() == "View all") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            for(int k=0; k<this.modules.size(); k++) {
                if(this.modules.get(k).getName()==moduleSelect) {
                    this.modules.get(k).showPlots();
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
                String fileName = filePath + "/" + this.modules.get(k).getName() + ".txt";
                this.modules.get(k).loadConstants(filePath);
            }
       }
        if(e.getActionCommand()=="Save...") {
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
            for(int k=0; k<this.modules.size(); k++) {
                this.modules.get(k).loadConstants();
            }
        }
    }

    public void configureFrame() {

        configFrame.setSize(900, 1000);
        //configFrame.setSize(1000, 600); // vnc size
        configFrame.setLocationRelativeTo(mainPanel);
        configFrame.setDefaultCloseOperation(configFrame.DO_NOTHING_ON_CLOSE);

        // Which steps    
        JPanel stepOuterPanel = new JPanel(new BorderLayout());
        JPanel stepPanel      = new JPanel(new GridBagLayout());
        stepOuterPanel.add(stepPanel, BorderLayout.NORTH);
        GridBagConstraints c  = new GridBagConstraints();

        
        for(int k=0; k<this.modules.size(); k++) {
            c.gridx = 0; c.gridy = k;
            c.anchor = c.WEST;
            JCheckBox stepCheck = new JCheckBox();
            stepCheck.setName(this.modules.get(k).getName());
            stepCheck.setText(this.modules.get(k).getName());
            stepCheck.setSelected(true);
            stepCheck.addActionListener(this);
            stepPanel.add(stepCheck,c);
        }
		
        JPanel butPage1 = new configButtonPanel(this, false, "Next");
        stepOuterPanel.add(butPage1, BorderLayout.SOUTH);

        //configPane.add("Select steps", stepOuterPanel);    

        // Previous calibration values
        JPanel confOuterPanel = new JPanel(new BorderLayout());
        Box confPanel = new Box(BoxLayout.Y_AXIS);
        
        for(int k=0; k<this.modules.size(); k++) {        
            FTPrevConfigPanel configPanel = new FTPrevConfigPanel(this.modules.get(k));
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
        this.detectorView.repaint();
    }
    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH.mm.ss");
        String data = this.workDir + "/kpp-pictures/clas12rec_run_" + this.runNumber + "_" + df.format(new Date());        
        File theDir = new File(data);
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
            System.out.println("Created directory: " + data);
            }
        }
        String fileName = data + "/clas12_canvas.png";
        System.out.println(fileName);
    }
    
    @Override
    public void timerUpdate() {
        this.detectorView.repaint();
	for(int k=0; k<this.modules.size(); k++) {
            this.modules.get(k).timerUpdate();
        }
        
    }

    @Override
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
            if(this.modules.get(k).getName().equals(moduleSelect)) {
                Color col = this.modules.get(k).getColor(dsd);
                dsd.setColor(col.getRed(), col.getGreen(), col.getBlue());
                dsd.setCounter(this.modules.get(k).getNEvents(dsd));
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

    @Override
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
        frame.setSize(1600, 900);
        frame.setVisible(true);
        viewer.configureFrame();
    }


}
