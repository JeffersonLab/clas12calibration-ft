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
import java.util.Random;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.utils.groups.IndexedList;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.data.GraphErrors;

/**
 *
 * @author devita
 */
public class FTCalibrationModule extends CalibrationEngine {
    
    //private final int[] npaddles = new int[]{23,62,5};
    private FTDetector                           ft = null;
    private CalibrationConstants              calib = null;
    private final IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);
    
    public FTCalibrationModule(FTDetector d) {
        this.ft    = d; 
        this.calib = new CalibrationConstants(3,"c0:c1:c2:c3:c4");
        this.calib.setName("myConstants");
	this.calib.setPrecision(3);
        this.resetEventListener();
    }

    @Override
    public void resetEventListener() {
        for(int key : this.ft.getDetectorComponents()) {
            // initializa calibration constant table
            calib.addEntry(1, 1, key);
            //  calib.setDoubleValue(0.,"a",1, 1, key);
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
            H2F h5 = new H2F("h5_"+key, 100, 0., 11., 50, 0.0, 1.2);//x,y
            h5.setTitleX("E_{rec}( GeV)");
            h5.setTitleY("E_{gen}-E_{rec} ( GeV)");
            h5.setTitle("Component " + key);
            GraphErrors h6 = new GraphErrors("h6_"+key);
            h6.setTitle("Component " + key);
            h6.setTitleX("E_{rec}( GeV)");
            h6.setTitleY("E_{gen}-E_{rec} ( GeV)");
            h6.setMarkerSize(1);
           // h6=h5.getProfileX();
            double q = 0.;
            double m = 0.;
            double c1 = 0.;
            double c2 = 0.;
            double c3 = 0.;
            F1D f2 = new F1D("f2_"+key,"[q]+x*[m]+x*x*[c1]+x*x*x*[c2]+x*x*x*x*[c3]", 0, 10.0);
            f2.setLineColor(2);
            f2.setLineStyle(1);
            DataGroup dg = new DataGroup(2,3);
            dg.addDataSet(h1,0);
            dg.addDataSet(h2,1);
            dg.addDataSet(h3,2);
            dg.addDataSet(h4,3);
            dg.addDataSet(h5,4);
            dg.addDataSet(h6,5);
            dg.addDataSet(f2,5);
            this.dataGroups.add(dg, 1,1,key);
            
        }
        calib.fireTableDataChanged();
    }

    @Override
    public void dataEventAction(DataEvent event) {
        if (event.getType() == DataEventType.EVENT_START) {
                System.out.println("EVENT_START");
                resetEventListener();
                processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
                processEvent(event);
        }
        else if (event.getType()==DataEventType.EVENT_SINGLE) {
                processEvent(event);
                System.out.println("EVENT_SINGLE from FTCalibrationModule");
        }
        else if (event.getType() == DataEventType.EVENT_STOP) {
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
//        System.out.println(" Event Processed ");
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
                if(genPart.pid()==22) {
                    if(partGen == null) {
                        partGen = genPart;
                    }
                }
            }
        }
        // loop over FTCAL reconstructed cluster
        if(event.hasBank("FTCAL::clusters") && partGen!=null) {
            DataBank recFTCAL = event.getBank("FTCAL::clusters");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            for(int loop=0; loop<recFTCAL.rows(); loop++) {
                int      key      = ft.getComponent(recFTCAL.getFloat("x",loop), recFTCAL.getFloat("y",loop));
                //if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.getFloat("x",loop)+" "+recFTCAL.getFloat("y",loop));//Only seed is included
                double   energy   = recFTCAL.getFloat("energy",loop);
                double   energyR  = recFTCAL.getFloat("recEnergy",loop);
                Vector3D cluster = new Vector3D(recFTCAL.getFloat("x",loop),recFTCAL.getFloat("y",loop),recFTCAL.getFloat("z",loop));  
                this.dataGroups.getItem(1,1,key).getH1F("h1_"+key).fill(energy);
                this.dataGroups.getItem(1,1,key).getH1F("h2_"+key).fill(energy-partGen.p());
                this.dataGroups.getItem(1,1,key).getH1F("h3_"+key).fill(Math.toDegrees(cluster.theta()-partGen.theta()));
                this.dataGroups.getItem(1,1,key).getH1F("h4_"+key).fill(Math.toDegrees(cluster.phi()-partGen.phi()));
                //Random Ran= new Random();
              //for(int j=0;j<1000;j++){
               // double x = Ran.nextDouble()*6;
               // this.dataGroups.getItem(1,1,key).getH2F("h5_"+key).fill(x,0.02*x-0.08+Ran.nextDouble()*0.04);//System.out.println("x " + x);
             // }         
              this.dataGroups.getItem(1,1,key).getH2F("h5_"+key).fill(energyR,partGen.p()-energyR);//System.out.println(" Analyzed 1");
              
      }
   }

        
    }
    
    public void analyze() {
       
        for(int key : this.ft.getDetectorComponents()) {
      
        ArrayList<H1F> hslice_1 = this.dataGroups.getItem(1,1,key).getH2F("h5_"+key).getSlicesX();
        
         for(int i=0; i<hslice_1.size(); i++) {
            // System.out.println(" Slice "+i + " Key "+key);
            double  x = this.dataGroups.getItem(1,1,key).getH2F("h5_"+key).getXAxis().getBinCenter(i);
            double ex = 0;
            double  y = hslice_1.get(i).getRMS();
            double ey = 0;
            double mean  = hslice_1.get(i).getDataX(hslice_1.get(i).getMaximumBin());
            double amp   = hslice_1.get(i).getBinContent(hslice_1.get(i).getMaximumBin());
            double sigma = hslice_1.get(i).getRMS();
            F1D f1 = new F1D("gaus","[amp]*gaus(x,[mean],[sigma])", -1.0, 1.0);
            f1.setParameter(0, amp);
            f1.setParameter(1, mean);
            f1.setParameter(2, 0.01);
            DataFitter.fit(f1, hslice_1.get(i), "Q"); //No options uses error for sigma 
            if(amp>1 && f1.parameter(1).error()<1.0 && f1.getParameter(1)>-1.0) this.dataGroups.getItem(1,1,key).getGraph("h6_"+key).addPoint(x, f1.getParameter(1), ex, f1.parameter(1).error());
            //System.out.println(" Added point "+x+" "+f1.getParameter(1)+" "+ex+" "+f1.parameter(1).error());
             
        }
       DataFitter.fit(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key), this.dataGroups.getItem(1,1,key).getGraph("h6_"+key), "Q");
      // consts=(" a "+1 + " b "+2);
       
      // calib.setStringValue(consts,"a",1, 1, key); 
     
           calib.setDoubleValue(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key).getParameter(0),"c0",1, 1, key); 
           calib.setDoubleValue(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key).getParameter(1),"c1",1, 1, key); 
           calib.setDoubleValue(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key).getParameter(2),"c2",1, 1, key); 
           calib.setDoubleValue(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key).getParameter(3),"c3",1, 1, key); 
           calib.setDoubleValue(this.dataGroups.getItem(1,1,key).getF1D("f2_"+key).getParameter(4),"c4",1, 1, key); 
        
        
        }

    }    
}
