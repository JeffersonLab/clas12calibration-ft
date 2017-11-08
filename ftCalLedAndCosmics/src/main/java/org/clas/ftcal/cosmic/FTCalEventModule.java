/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.cosmic;

import java.awt.Color;
import java.util.List;
import org.clas.detector.DetectorDataDgtz;
import org.clas.ftcal.tools.FTDetector;
import org.clas.ftcal.tools.FTModule;
import org.clas.ftcal.tools.FTModuleType;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;


/**
 *
 * @author devita
 */
public class FTCalEventModule extends FTModule {

    H1F H_WMAX   = null;
    H1F H_TCROSS = null;
    H1F H_WIDTH = null;
        
    public FTCalEventModule(FTDetector d) {
        super(d);
        this.setName("Event Viewer");
        this.addCanvases("Event");
        this.setType(FTModuleType.EVENT_SINGLE);
        this.addParameters("Event");
        this.getParameter("Event").setRanges(0.0,0.0,1.0,1.0);
    }

    @Override
    public void createDataGroup() {
        H_WMAX   = new H1F("WMAX",   this.getDetector().getComponentMaxCount(), 0, this.getDetector().getComponentMaxCount());
        H_TCROSS = new H1F("TCROSS", this.getDetector().getComponentMaxCount(), 0, this.getDetector().getComponentMaxCount());
        H_WIDTH = new H1F("WIDTH",   this.getDetector().getComponentMaxCount(), 0, this.getDetector().getComponentMaxCount());
        for(int component : this.getDetector().getDetectorComponents()) {
            int ix = this.getDetector().getIdX(component);
            int iy = this.getDetector().getIdY(component);
            String title = "Crystal " + component + " (" + ix + "," + iy + ")";
            H1F H_WAVE = new H1F("Wave_" + component, title, 100, 0.0, 100.0);
            H_WAVE.setFillColor(5);
            H_WAVE.setTitleX("fADC sample");
            H_WAVE.setTitleY("fADC counts");   
            H_WAVE.setOptStat(1111);    
            H1F H_WAVE_PED = new H1F("Wave_ped_" + component, title, 100, 0.0, 100.0);
            H_WAVE_PED.setFillColor(47);
            H_WAVE_PED.setTitleX("fADC sample");
            H_WAVE_PED.setTitleY("fADC counts");   
            H_WAVE_PED.setOptStat(1111);    
            H1F H_WAVE_PUL = new H1F("Wave_pul_" + component, title, 100, 0.0, 100.0);
            H_WAVE_PUL.setFillColor(46);
            H_WAVE_PUL.setTitleX("fADC sample");
            H_WAVE_PUL.setTitleY("fADC counts");   
            H_WAVE_PUL.setOptStat(1111); 
            GraphErrors G_PULSE_ANALYSIS = new GraphErrors("Pulse_" + component);
            G_PULSE_ANALYSIS.setTitle(title); //  title
            G_PULSE_ANALYSIS.setTitleX("Event");             // X axis title
            G_PULSE_ANALYSIS.setTitleY("LED Amplitude (mV)");   // Y axis title
            G_PULSE_ANALYSIS.setMarkerColor(1); // color from 0-9 for given palette
            G_PULSE_ANALYSIS.setMarkerSize(5); // size in points on the screen
            G_PULSE_ANALYSIS.addPoint(0, 0, 0, 0);
            G_PULSE_ANALYSIS.addPoint(100, 0, 0, 0);
            DataGroup dg = new DataGroup(1, 1);
            dg.addDataSet(H_WAVE,          0);
            dg.addDataSet(H_WAVE_PED,      0);
            dg.addDataSet(H_WAVE_PUL,      0);
            dg.addDataSet(G_PULSE_ANALYSIS,0);
            this.getDataGroup().add(dg, 1, 1, component);
        }
    }
    
    @Override
    public void plotDataGroup() {
        this.getCanvases().get("Event").setGridX(false);
        this.getCanvases().get("Event").setGridY(false);
        int key = this.getSelectedKey();
        if(this.getDetector().hasComponent(key)) {
            this.getCanvases().get("Event").draw(this.getDataGroup().getItem(1,1,key).getH1F("Wave_" + key));
            this.getCanvases().get("Event").draw(this.getDataGroup().getItem(1,1,key).getH1F("Wave_ped_" + key),"same");
            this.getCanvases().get("Event").draw(this.getDataGroup().getItem(1,1,key).getH1F("Wave_pul_" + key),"same");
//            if(this.getDataGroup().getItem(1,1,key).getGraph("Pulse_" + key).getDataSize(1)>0) {
                this.getCanvases().get("Event").draw(this.getDataGroup().getItem(1,1,key).getGraph("Pulse_" + key),"same");
//            }
        }
    }
    
    @Override
    public void processEvent(List<DetectorDataDgtz> counters) {
        H_WMAX.reset();
        H_TCROSS.reset();
        H_WIDTH.reset();
        for (DetectorDataDgtz counter : counters) {
            int key = counter.getDescriptor().getComponent();
            if(this.getDetector().hasComponent(key)) {
                short pulse[] = counter.getADCData(0).getPulseArray();
                int ped_i1 = counter.getADCData(0).getPedistalMinBin();
                int ped_i2 = counter.getADCData(0).getPedistalMaxBin();
                int pul_i1 = counter.getADCData(0).getPulseMinBin();
                int pul_i2 = counter.getADCData(0).getPulseMaxBin();
                H1F hwave     = this.getDataGroup().getItem(1,1,key).getH1F("Wave_" + key);
                H1F hwave_ped = this.getDataGroup().getItem(1,1,key).getH1F("Wave_ped_" + key);
                H1F hwave_pul = this.getDataGroup().getItem(1,1,key).getH1F("Wave_pul_" + key);
                GraphErrors gpulse = this.getDataGroup().getItem(1,1,key).getGraph("Pulse_" + key);
                hwave.reset();
                hwave_ped.reset();
                hwave_pul.reset();
                gpulse.reset();
                for (int i = 0; i < Math.min(pulse.length, hwave.getDataSize(0)); i++) {
                    hwave.fill(i, pulse[i]);
                    if(i> ped_i1 && i<=ped_i2) hwave_ped.fill(i, pulse[i]);
                    if(i>=pul_i1 && i<=pul_i2) hwave_pul.fill(i, pulse[i]);                    
                }
                gpulse.addPoint(ped_i1+1.5,counter.getADCData(0).getPedestal(),0,0);
                gpulse.addPoint(ped_i2+0.5,counter.getADCData(0).getPedestal(),0,0);            
                gpulse.addPoint(pul_i1+0.5,counter.getADCData(0).getPulseValue(pul_i1),0,0);
                gpulse.addPoint(pul_i2+0.5,counter.getADCData(0).getPulseValue(pul_i2),0,0);            
                H_WMAX.fill(key,counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal());
                H_TCROSS.fill(key,counter.getADCData(0).getTimeCourse());
                H_WIDTH.fill(key,counter.getADCData(0).getFWHM());
                if(counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal()>this.getDetector().getThresholds().get(1, 1, key)) {
                    gpulse.addPoint(counter.getADCData(0).getThresholdCrossing()+0.5,counter.getADCData(0).getPulseValue(counter.getADCData(0).getThresholdCrossing()),0,0);
                    gpulse.addPoint(counter.getADCData(0).getTime()/nsPerSample+0.5,(counter.getADCData(0).getHeight()+counter.getADCData(0).getPedestal())/2,0,0);
                    gpulse.addPoint(counter.getADCData(0).getPosition()+0.5,counter.getADCData(0).getHeight(),0,0);
                }
            }
        }
    }
    
    @Override
    public Color getColor(int key, String parameterName) {
        Color col = new Color(100, 100, 100);
        if(H_WMAX.getBinContent(key)>this.getDetector().getThresholds().get(1, 1, key)) {
            if(H_TCROSS.getBinContent(key)>0) {
                col = new Color(140, 0, 200);
            }
            else {
                col = new Color(200, 0, 200);
            }
        }
        return col;
    }    

    
}
