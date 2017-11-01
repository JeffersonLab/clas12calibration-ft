/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.led;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;


import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;


/**
 *
 * @author gavalian
 */
public class FTCalLedViewer implements IDataEventListener, ActionListener  {

    
    JPanel ledPanel = null;
     
    FTCalLedModule     moduleFTCAL = new FTCalLedModule();
    
    JMenuBar               menuBar = new JMenuBar();
    
    DataSourceProcessorPane evPane = new DataSourceProcessorPane();
       
    int nProcessed = 0;
    int runNumber  = 0;
    String workDir = null;
      

    public FTCalLedViewer() {
        
        this.evPane.addEventListener(this);
        this.evPane.setUpdateRate(500);
        
        this.addSettingsMenu();
        this.addTableMenu();
        
        /*Graphics starts here*/
        this.ledPanel = new JPanel();
        this.ledPanel.setLayout(new BorderLayout());
               
        // filling main panel with tabs for different FT subdetectors and event handling panel
        this.ledPanel.add(this.moduleFTCAL, BorderLayout.CENTER);
        this.ledPanel.add(this.evPane, BorderLayout.PAGE_END);
        
        this.workDir = System.getProperty("user.dir");
        System.out.println("Current work directory set to:" + this.workDir);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        //        System.out.println("FTViewer ACTION = " + e.getActionCommand());
        // Settings menu
        if(e.getActionCommand()=="Set analysis update interval...") {
            this.chooseUpdateInterval();
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
            System.out.println("Current work directory set to:" + this.workDir);
        }

        // Table menu bar
        if(e.getActionCommand()=="Save table...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "ftCalLed_" + df.format(new Date()) + ".txt";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.moduleFTCAL.saveTable(fileName);
        }
        if(e.getActionCommand()=="Clear table") {
            this.moduleFTCAL.resetTable();
        }
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
 
    public void addHistogramMenu() {
        JMenuItem menuItem;
        JMenu file = new JMenu("Histograms");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms to file...", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...", KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        this.menuBar.add(file);
    }
    
    public void addSettingsMenu() {
        JMenuItem menuItem;
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set analysis update interval...");
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
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
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save table content to file");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuItem = new JMenuItem("Clear table");
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Clear table content");
        menuItem.addActionListener(this);
        table.add(menuItem);
        this.menuBar.add(table);
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
                this.moduleFTCAL.setTimerUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }
    @Override
    public void dataEventAction(DataEvent de) {

        nProcessed++;
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
                
        moduleFTCAL.dataEventAction(de);  
        if(this.runNumber != moduleFTCAL.runNumber) {
            this.runNumber = moduleFTCAL.runNumber;
            System.out.println("\nRun number " + this.runNumber);
        }
    }

        
    public String getName() {
        return "FTLEDViewerModule";
    }

    public String getAuthor() {
        return "De Vita";
    }

    public DetectorType getType() {
        return DetectorType.FTCAL;
    }

    public String getDescription() {
        return "FT LED Display";
    }

    public JPanel getDetectorPanel() {
        return this.ledPanel;
    }

    @Override
    public void resetEventListener() {
        this.moduleFTCAL.resetModule();
        this.moduleFTCAL.resetWave(nProcessed);
    }

    @Override
    public void timerUpdate() {
        this.moduleFTCAL.plotHistograms();
        this.moduleFTCAL.updateTable();
    }

    public static void main(String[] args) {
        FTCalLedViewer module = new FTCalLedViewer();
        JFrame frame = new JFrame();
        frame.add(module.getDetectorPanel());
        frame.pack();
        frame.setJMenuBar(module.menuBar);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }


  
    
}
