/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.clas.fthodo.FTHODOModule;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;



/**
 *
 * @author gavalian
 */
public class FTHodoViewer implements IDataEventListener, ActionListener {

    JPanel              panel = null;
    FTHODOModule moduleFTHODO = new FTHODOModule();
    JMenuBar          menuBar = null;
    DataSourceProcessorPane evPane = new DataSourceProcessorPane();
    int          nProcessed = 0;
    //String workDir         = null;
   
    public FTHodoViewer() {
        this.initDetector();
        this.initHistograms();
        this.initArrays();
        this.initMenu();
        moduleFTHODO.workDir = System.getProperty("user.dir");
        //System.out.println("\nCurrent work directory set to:" + this.workDir);

        
        this.evPane.addEventListener(this);
       // this.evPane.setUpdateRate(500);
        this.evPane.setUpdateRate(10);

        /*Graphics starts here*/
        // whole panel
        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());

	// create tabbed objects for CAL and HODO modules
    // this.moduleFTHODO = new JPanel(new BorderLayout());
	
    // filling main panel with tabs for different FT subdetectors
	// and event handling panel
        this.panel.add(this.moduleFTHODO, BorderLayout.CENTER);
        this.panel.add(this.evPane,        BorderLayout.PAGE_END);
        moduleFTHODO.setLayout(new BorderLayout());
        moduleFTHODO.initPanel();
    }

    private void initDetector() {
        moduleFTHODO.initDetector();
    }

    private void initHistograms() {
        moduleFTHODO.initHistograms();
    }

    private void initArrays() {
	// if(!onlyHodo)
	//   moduleFTCAL.initArrays();
        moduleFTHODO.initArrays();
    }

    private void initMenu() {
        	// create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File");
        menuItem = new JMenuItem("Open evio files...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Load histograms...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms...", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuBar.add(file);
        
        JMenu fit = new JMenu("Fit");
        menuItem = new JMenuItem("Adjust fit...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit parameters for distribution in Tab");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        menuItem = new JMenuItem("Update constants...");
        menuItem.getAccessibleContext().setAccessibleDescription("Update constants from adjusted fit");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        //HERE-NEW
        menuItem = new JMenuItem("Set new global fit parameters...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set new fit parameters for all functions");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        menuItem = new JMenuItem("Use new global fit parameters...");
        menuItem.getAccessibleContext().setAccessibleDescription("Use the newly chosen fit parameters for all functions");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        menuItem = new JMenuItem("Use default global fit parameters...");
        menuItem.getAccessibleContext().setAccessibleDescription("Use the new fit parameters for all functions");
        menuItem.addActionListener(this);
        fit.add(menuItem);

        this.menuBar.add(fit);
        
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval...", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
        
        menuItem = new JMenuItem("Choose work directory...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis work directory");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        this.menuBar.add(settings);
    }
    private void resetHistograms() {
        moduleFTHODO.resetHistograms();
    }    

    public void dataEventAction(DataEvent de) {
	// Simulated Data
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

        if(de instanceof EvioDataEvent)
            moduleFTHODO.dataEvioEventAction(de);
        else if(de instanceof HipoDataEvent) {
            moduleFTHODO.dataHipoEventAction((HipoDataEvent) de);
        }
    }
    
    public String getName() {
        return "HODOViewerModule";
    }
    
    public String getAuthor() {
        return "Nicholas Zachariou";
    }
    
    public DetectorType getType() {
        return DetectorType.FTHODO;
    }
    
    public String getDescription() {
        return "FT HODO Display";
    }
    
    public JPanel getDetectorPanel() {
        return this.panel;
    }
    
    public void actionPerformed(ActionEvent e) {
        System.out.println("FTViewer ACTION = " + e.getActionCommand());
        if(e.getActionCommand().compareTo("Open evio files...")==0) this.readFiles();

        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            moduleFTHODO.adjustFit();
        }
        if(e.getActionCommand() == "Update constants...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            moduleFTHODO.adjustFitConstants();
        }
        if(e.getActionCommand() == "Set new global fit parameters...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            moduleFTHODO.adjustFitParameters();
        }
        //HERE--
        if(e.getActionCommand() == "Use new global fit parameters...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            moduleFTHODO.SetFitParameters(true);
        }
        if(e.getActionCommand() == "Use default global fit parameters...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            moduleFTHODO.SetFitParameters(false);
        }

        
//        if(e.getActionCommand() == "Adjust Noise mV fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustNoisemvFit();
//        }
//        if(e.getActionCommand() == "Update Noise mV constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustNoisemVFitConstants();
//        }
//        if(e.getActionCommand() == "Adjust Noise charge fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustNoisechargeFit();
//        }
//        if(e.getActionCommand() == "Update Noise charge constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustNoisechargeFitConstants();
//        }
//
//        if(e.getActionCommand() == "Adjust MIP mV fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPmvFit();
//        }
//        if(e.getActionCommand() == "Update MIP mV constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPmVFitConstants();
//        }
//        if(e.getActionCommand() == "Adjust MIP charge fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPchargeFit();
//        }
//        if(e.getActionCommand() == "Update MIP charge constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPchargeFitConstants();
//        }
//        if(e.getActionCommand() == "Adjust MIP mV Matching Tiles fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPmvMatchingTilesFit();
//        }
//        if(e.getActionCommand() == "Update MIP mV Matching Tiles constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPmVMatchingTilesFitConstants();
//        }
//        if(e.getActionCommand() == "Adjust MIP charge Matching Tiles fit...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPchargeMatchingTilesFit();
//        }
//        if(e.getActionCommand() == "Update MIP charge Matching Tiles constants...") {
//            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
//            moduleFTHODO.adjustMIPchargeMatchingTilesFitConstants();
//        }
        
        
        if(e.getActionCommand().compareTo("Choose work directory...")==0) {
            //System.out.println("\nYOooooooo");
            String filePath = moduleFTHODO.workDir;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose work directory...");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            File workingDirectory = new File(moduleFTHODO.workDir);
            fc.setCurrentDirectory(workingDirectory);
            int returnValue = fc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                filePath = fc.getSelectedFile().getAbsolutePath();
            }
            moduleFTHODO.workDir = filePath;
            System.out.println("\nCurrent work directory set to:" + moduleFTHODO.workDir);
        }
    }
    
    private void readFiles() {
        EvioSource reader = new EvioSource();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose input files directory...");
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        File workingDirectory = new File(moduleFTHODO.workDir);
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
//                        moduleFTHODO.analyze();
                        nf++;
                    }
                }
            }
            //moduleFTHODO.updateConstants();
            System.out.println("Task completed");
        }
    }
    
    public static void main(String[] args) {
        FTHodoViewer module = new FTHodoViewer();
        JFrame frame = new JFrame();
        frame.add(module.getDetectorPanel());
        frame.setJMenuBar(module.menuBar);
        frame.pack();
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }
    public void timerUpdate() {
    }
    
    public void resetEventListener() {
        this.moduleFTHODO.resetHistograms();
    }


}
	
