/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public class FTCalibrationModule extends CalibrationEngine {
    
    //private final int[] npaddles = new int[]{23,62,5};
    private FTDetector                           ft = null;
    private CalibrationConstants              calib = null;
    private final IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);;

    public FTCalibrationModule(FTDetector d) {
        this.ft    = d;
        this.calib = new CalibrationConstants(3,"a");
        this.calib.setName("myConstants");
	this.calib.setPrecision(3);
        this.resetEventListener();
    }

    @Override
    public void resetEventListener() {
        for(int key : this.ft.getDetectorComponents()) {
            // initializa calibration constant table
            calib.addEntry(1, 1, key);
            calib.setDoubleValue(0.,"a",1, 1, key);
            // initializa data group
            H1F h1 = new H1F("h1_"+key, 100, 0., 11);
            h1.setTitleX("E( GeV)");
            h1.setTitleY("counts");
            h1.setTitle("Component " + key);
            H1F h2 = new H1F("h2_"+key, 100, -1., 1.);
            h2.setTitleX("#Delta E (GeV)");
            h2.setTitleY("counts");
            h2.setTitle("Component " + key);
            H1F h3 = new H1F("h3_"+key, 100, -2., 2.);
            h3.setTitleX("#Delta#theta (deg)");
            h3.setTitleY("counts");
            h3.setTitle("Component " + key);
            H1F h4 = new H1F("h4_"+key, 100, -10., 10.);
            h4.setTitleX("#Delta#phi (deg)");
            h4.setTitleY("counts");
            h4.setTitle("Component " + key);
            DataGroup dg = new DataGroup(2,2);
            dg.addDataSet(h1,0);
            dg.addDataSet(h2,1);
            dg.addDataSet(h3,2);
            dg.addDataSet(h4,3);
            this.dataGroups.add(dg, 1,1,key);
        }
        calib.fireTableDataChanged();
    }

    @Override
    public void dataEventAction(DataEvent event) {
        if (event.getType() == DataEventType.EVENT_START) {
                resetEventListener();
                processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
                processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_STOP) {
                System.out.println("EVENT_STOP");
                analyze();
        }
    }


    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
	return Arrays.asList(calib);
    }

    public int getNEvents(int isec ,int ilay ,int icomp) {
        return this.dataGroups.getItem(1,1,icomp).getH1F("h1_"+icomp).getEntries();
    }
    
    
    
    @Override
    public IndexedList<DataGroup> getDataGroup() {
        return dataGroups;
    }
    
    public void processEvent(DataEvent event) {
        Particle partGen = null;
        // get generated particle information
        if(event.hasBank("MC::Particle")) {
            DataBank genBank = event.getBank("MC::Particle");
            for(int loop = 0; loop < genBank.rows(); loop++) {   
                Particle genPart = new Particle(
                                        genBank.getInt("pid", loop),
                                        genBank.getFloat("px", loop),
                                        genBank.getFloat("py", loop),
                                        genBank.getFloat("pz", loop),
                                        genBank.getFloat("vx", loop),
                                        genBank.getFloat("vy", loop),
                                        genBank.getFloat("vz", loop));
                if(genPart.pid()==11) {
                    if(partGen == null) {
                        partGen = genPart;
                    }
                }
            }
        }
        // loop over FTCAL reconstructed cluster
        if(event.hasBank("FTCAL::clusters") && partGen!=null) {
            DataBank recFTCAL = event.getBank("FTCAL::clusters");
            for(int loop=0; loop<recFTCAL.rows(); loop++) {
                int      key     = ft.getComponent(recFTCAL.getFloat("x",loop), recFTCAL.getFloat("y",loop));
                double   energy  = recFTCAL.getFloat("energy",loop);
                Vector3D cluster = new Vector3D(recFTCAL.getFloat("x",loop),recFTCAL.getFloat("y",loop),recFTCAL.getFloat("z",loop));  
                this.dataGroups.getItem(1,1,key).getH1F("h1_"+key).fill(energy);
                this.dataGroups.getItem(1,1,key).getH1F("h2_"+key).fill(energy-partGen.p());
                this.dataGroups.getItem(1,1,key).getH1F("h3_"+key).fill(Math.toDegrees(cluster.theta()-partGen.theta()));
                this.dataGroups.getItem(1,1,key).getH1F("h4_"+key).fill(Math.toDegrees(cluster.phi()-partGen.phi()));
      }
   }

        
    }
    
    public void analyze() {

    }    
}
