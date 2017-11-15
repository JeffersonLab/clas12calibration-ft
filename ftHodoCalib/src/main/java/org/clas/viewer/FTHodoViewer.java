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
    String workDir         = null;
   
    public FTHodoViewer() {
        this.initDetector();
        this.initHistograms();
        this.initArrays();
        this.initMenu();
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to:" + this.workDir);

        
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
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval...", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
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
        return "Gary Smith";
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
//                        moduleFTHODO.analyze();
                        nf++;
                    }
                }
            }
            moduleFTHODO.updateConstants();
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
	
