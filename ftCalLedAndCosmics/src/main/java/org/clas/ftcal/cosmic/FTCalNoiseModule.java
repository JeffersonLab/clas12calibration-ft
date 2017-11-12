/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.cosmic;

import java.awt.Color;
import java.util.List;
import org.clas.detector.DetectorDataDgtz;
import org.clas.ft.tools.FTDetector;
import org.clas.ft.tools.FTModule;
import org.clas.ft.tools.FTModuleType;
import org.clas.ft.tools.FTParameter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;


/**
 *
 * @author devita
 */
public class FTCalNoiseModule extends FTModule {
    
    // Data Collection
    double[] detectorIDs;
    double[] pedestalMEAN;
    double[] pedestalRMS;
    double[] noiseRMS;
    

    public FTCalNoiseModule(FTDetector d) {
        super(d);
        this.setName("Noise");
        this.addCanvases("Noise");
        this.setType(FTModuleType.EVENT_ACCUMULATE);
        this.addParameters("Status", "Pedestal (Channels)", "Pedestal RMS (Channels)", "Noise (mV)");
        this.getParameter("Status").setRanges(0.75,1.05,1.0,2.0, true);
        this.getParameter("Pedestal (Channels)").setRanges(130.0,250.0,1.0,500.0, false);
        this.getParameter("Pedestal RMS (Channels)").setRanges(0.0,2.0,1.0,2.0,false);
        this.getParameter("Noise (mV)").setRanges(0.75,1.05,1.0,2.0,false);
    }

    @Override
    public IndexedList<DataGroup> createDataGroup() {
        IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>();
        for(int component : this.getDetector().getDetectorComponents()) {
            int ix = this.getDetector().getIdX(component);
            int iy = this.getDetector().getIdY(component);
            String title = "Crystal " + component + " (" + ix + "," + iy + ")";
            H1F H_PED = new H1F("Pedestal_" + component, title, 120, 130., 250.);
            H_PED.setFillColor(2);
            H_PED.setTitleX("fADC channels");
            H_PED.setTitleY("Counts");   
            H_PED.setOptStat(1111);    
            H1F H_NOISE = new H1F("Noise_" + component, title, 200, 0.0, 10.0);
            H_NOISE.setFillColor(4);
            H_NOISE.setTitleX("RMS (mV)");
            H_NOISE.setTitleY("Counts");   
            H_NOISE.setOptStat(1111);    
            DataGroup dg = new DataGroup(2, 1);
            dg.addDataSet(H_PED,        0);
            dg.addDataSet(H_NOISE,      1);
            dataGroups.add(dg, 1, 1, component);
        }
        pedestalMEAN    = new double[this.getDetector().getNComponents()];
        pedestalRMS     = new double[this.getDetector().getNComponents()];
        noiseRMS        = new double[this.getDetector().getNComponents()];
        detectorIDs     = new double[this.getDetector().getNComponents()];        
        for(int i=0; i< this.getDetector().getNComponents(); i++) {
            detectorIDs[i]=this.getDetector().getIDArray()[i]; 
        }
        return dataGroups;
    }
    
    @Override
    public void plotDataGroup() {
        EmbeddedCanvas canvas = this.getCanvas("Noise");
        canvas.divide(2, 2);
        canvas.setGridX(false);
        canvas.setGridY(false);
        int ipointer=0;
        for(int key : this.getDetector().getDetectorComponents()) {
            pedestalMEAN[ipointer] = this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key).getMean();
            pedestalRMS[ipointer]  = this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key).getRMS();
            noiseRMS[ipointer]     = this.getDataGroup().getItem(1,1,key).getH1F("Noise_" + key).getMean();
            ipointer++;
        }
        GraphErrors  G_PED = new GraphErrors("Pedestals",detectorIDs,pedestalRMS);
        G_PED.setTitle(" "); //  title
        G_PED.setTitleX("Crystal ID"); // X axis title
        G_PED.setTitleY("Pedestal RMS (Channels)");   // Y axis title
        G_PED.setMarkerColor(2); // color from 0-9 for given palette
        G_PED.setMarkerSize(4); // size in points on the screen
        G_PED.setMarkerStyle(0); // Style can be 1 or 2
        GraphErrors  G_NOISE = new GraphErrors("Noise",detectorIDs,noiseRMS);
        G_NOISE.setTitle(" "); //  title
        G_NOISE.setTitleX("Crystal ID"); // X axis title
        G_NOISE.setTitleY("Noise (mV)");   // Y axis title
        G_NOISE.setMarkerColor(4); // color from 0-9 for given palette
        G_NOISE.setMarkerSize(4); // size in points on the screen
        G_NOISE.setMarkerStyle(0); // Style can be 1 or 2
        canvas.cd(0);
        canvas.draw(G_PED);
        canvas.cd(1);
        canvas.draw(G_NOISE);        
        int key = this.getSelectedKey();
        if(this.getDetector().hasComponent(key)) {
            canvas.cd(2);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key));
            canvas.cd(3);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Noise_" + key));
        }
    }
    
    @Override
    public void processEvent(List<DetectorDataDgtz> counters) {
        for (DetectorDataDgtz counter : counters) {
            int key = counter.getDescriptor().getComponent();
            if(this.getDetector().hasComponent(key)) {
                this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key).fill(counter.getADCData(0).getPedestal());
                this.getDataGroup().getItem(1,1,key).getH1F("Noise_" + key).fill(counter.getADCData(0).getRMS()*LSB);
            }
        }
    }
    
    @Override
    public double getParameterValue(String parameterName, int key) {
        double value = -1;
        FTParameter par = this.getParameter(parameterName);
        if(par != null) {
            switch (parameterName) {
                case "Status":
                {
                    this.getParameter(parameterName).setValue(this.getDataGroup().getItem(1,1,key).getH1F("Noise_" + key).getMean());
                    value = this.getParameter(parameterName).getValue();
                    break;
                }
                case "Pedestal (Channels)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key).getMean();
                    break;
                }
                case "Pedestal RMS (Channels)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getH1F("Pedestal_" + key).getRMS();
                    break;
                }
                case "Noise (mV)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getH1F("Noise_" + key).getMean();
                    break;
                }
                default:
                {
                    value = -1;
                    break;
                }
            }
        }
        return value;
    }   
}
    
    
    
    
