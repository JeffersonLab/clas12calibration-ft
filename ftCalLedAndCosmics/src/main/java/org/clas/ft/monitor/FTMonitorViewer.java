/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.clas.detector.DetectorDataDgtz;
import org.clas.ftcal.tools.FTCalDetector;
import org.clas.ft.tools.FTModule;
import org.clas.ft.tools.FTViewer;
import org.clas.ftcal.cosmic.FTCalEventModule;


import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;


/**
 *
 * @author gavalian
 */
public class FTMonitorViewer extends FTViewer  {

    
    int    threshold       = 6;
    int    nProcessed      = 0;


    public FTMonitorViewer() {
        super();
        // create analysis modules
        this.getModules().add(new FTCalEventModule(this.getDetector()));
        this.initModules();
        this.addSummaryTable();
        this.initPanel();
    }

    @Override
    public void initPulseFitter() {
            System.out.println("\nInitializing connection to CCDB"); 
            this.getConstantManager().init(Arrays.asList(new String[]{
                    "/daq/fadc/ftcal",
                    "/daq/tt/ftcal"}));
            this.getDetectorDecoder().getFadcPanel().init(this.getConstantManager(),11,"/daq/fadc/ftcal", 70,3,1); 
            this.getDetectorDecoder().getFadcPanel().setMode1(1, 15, 45, 80, 7);           
     }

    @Override
    public void initDetector() {
        this.setDetector(new FTCalDetector("FTCAL"));
        this.getDetector().setThresholds(this.threshold);
        getDetector().getView().addDetectorListener(this);
        for(String layer : getDetector().getView().getLayerNames()){
            getDetector().getView().setDetectorListener(layer,this);
         }
        getDetector().updateBox();
    }                
    @Override
    public String getAuthor() {
        return "De Vita";
    }

    @Override
    public String getName() {
        return "FTCalCosmic";
    }

    @Override
    public String getDescription() {
        return "FT Cosmic Display";
    }

    @Override
    public void processEvent(DataEvent event) {
        nProcessed++;
        if (event instanceof EvioDataEvent) {
            try {
                List<DetectorDataDgtz> dataList = this.getDecoder().getDataEntries((EvioDataEvent) event);
                if (this.getRunNumber() != this.getDecoder().getRunNumber()) {
                    this.setRunNumber(this.getDecoder().getRunNumber());
                    System.out.println("\nRun number " + this.getRunNumber());
                }
                this.getDetectorDecoder().translate(dataList);
                this.getDetectorDecoder().fitPulses(dataList);
                List<DetectorDataDgtz> counters = new ArrayList<DetectorDataDgtz>();
                for (DetectorDataDgtz entry : dataList) {
                    if (entry.getDescriptor().getType() == DetectorType.FTCAL) {
                        if (entry.getADCSize() > 0) {
                            counters.add(entry);
                        }
                    }
                }
                for(FTModule module : this.getModules()) module.addEvent(counters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
    }
  
    public static void main(String[] args) {
        FTMonitorViewer module = new FTMonitorViewer();
        JFrame frame = new JFrame();
        frame.add(module.getPanel());
        frame.pack();
        frame.setJMenuBar(module.getMenuBar());
        frame.setSize(1400, 800);        
        frame.setVisible(true);
    }    

}
