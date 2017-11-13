/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.physics.Particle;
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
import org.jlab.groot.data.GraphErrors;

/**
 *
 * @author devita
 */
public class FTEnergyCorrection extends FTCalibrationModule {

    private int minNumberOfEvents =100;
      public double theta[] = new double[500];
      public double phi[] = new double[500];
    
    public FTEnergyCorrection(FTDetector d, String name) {
        super(d, name, "c0:c1:c2:c3:c4",7);
    }

    @Override
    public void resetEventListener() {
        
        GraphErrors tgeE_theta = new GraphErrors("tgeE_theta");
        tgeE_theta.setTitle("E_g - E_r_c_n vs. #theta");
        tgeE_theta.setTitleX("#theta (deg)");
        tgeE_theta.setTitleY("E_g - E_r_c_n (GeV)");
        tgeE_theta.setMarkerSize(1);
        tgeE_theta.addPoint(0, 0, 0, 0);
        GraphErrors tgeE_phi = new GraphErrors("tgeE_phi");
        tgeE_phi.setTitle("E_g - E_r_c_n vs. #phi");
        tgeE_phi.setTitleX("#phi (deg)");
        tgeE_phi.setTitleY("E_g - E_r_c_n (GeV)");
        tgeE_phi.setMarkerSize(1);
        tgeE_phi.addPoint(0, 0, 0, 0);
        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().addEntry(1, 1, key);
            getCalibrationTable().setDoubleValue(0., "c0", 1, 1, key);
            getCalibrationTable().setDoubleValue(0., "c1", 1, 1, key);
            getCalibrationTable().setDoubleValue(0., "c2", 1, 1, key);
            getCalibrationTable().setDoubleValue(0., "c3", 1, 1, key);
            getCalibrationTable().setDoubleValue(0., "c4", 1, 1, key);
            // initializa data group
            H1F h1 = new H1F("h1_" + key, 100, 0., 11);
            h1.setTitleX("E( GeV)");
            h1.setTitleY("counts");
            h1.setTitle("Component " + key);
            H1F h2 = new H1F("h2_" + key, 100, -1., 1.);
            h2.setTitleX("#Delta E (GeV)");
            h2.setTitleY("counts");
            h2.setTitle("Component " + key);
            H1F h3 = new H1F("h3_" + key, 100, -2., 2.);
            h3.setTitleX("#Delta#theta (deg)");
            h3.setTitleY("counts");
            h3.setTitle("Component " + key);
            H1F h4 = new H1F("h4_" + key, 100, -10., 10.);
            h4.setTitleX("#Delta#phi (deg)");
            h4.setTitleY("counts");
            h4.setTitle("Component " + key);
            H2F h5 = new H2F("h5_" + key, 100, 0., 11., 50, 0.0, 1.2);//x,y
            h5.setTitleX("E_{r}( GeV)");
            h5.setTitleY("E_{g}-E_{r} ( GeV)");
            h5.setTitle("Component " + key);
            GraphErrors h6 = new GraphErrors("h6_" + key);
            h6.setTitle("Component " + key);
            h6.setTitleX("E_{r}( GeV)");
            h6.setTitleY("E_{g}-E_{r} ( GeV)");
            h6.setMarkerSize(1);
            h6.addPoint(0, 0, 0, 0);
            // h6=h5.getProfileX();
            double q = 0.;
            double m = 0.;
            double c1 = 0.;
            double c2 = 0.;
            double c3 = 0.;
            F1D f2 = new F1D("f2_" + key, "([q]+x*[m]+x*x*[c1]+x*x*x*[c2]+x*x*x*x*[c3])/1000.", 0, 10.0);
            f2.setLineColor(2);
            f2.setLineStyle(1);
            f2.setLineWidth(2);
            F1D f2calib = new F1D("f2calib_" + key, "([q]+x*[m]+x*x*[c1]+x*x*x*[c2]+x*x*x*x*[c3])/1000.", 0, 10.0);
            f2calib.setLineColor(4);
            f2calib.setLineStyle(1);
            H2F h7 = new H2F("h7_" + key, 100, 0., 11., 50, -5.0, 5.0);//x,y
            h7.setTitleX("E_{g}( GeV)");
            h7.setTitleY("E_{g}-E_{r_c_p} ( GeV)");
            h7.setTitle("Component " + key);
            GraphErrors h8 = new GraphErrors("h8_" + key);
            h8.setTitle("Component " + key);
            h8.setTitleX("E_{g}( GeV)");
            h8.setTitleY("E_{g}-E_{r_c_p} ( GeV)");
            h8.setMarkerSize(1);
            h8.addPoint(0, 0, 0, 0);
            GraphErrors h9 = new GraphErrors("h9_" + key);
            h9.setTitle("Component " + key);
            h9.setTitleX("E_{g}( GeV)");
            h9.setTitleY("E_{g}-E_{r_c_n} ( GeV)");
            h9.setMarkerSize(1);
            h9.addPoint(0, 0, 0, 0);
            
            
        
            DataGroup dg = new DataGroup(3, 4);//Ho corretto qui
            dg.addDataSet(h1, 0);
            dg.addDataSet(h2, 1);
            dg.addDataSet(h3, 2);
            dg.addDataSet(h4, 3);
            dg.addDataSet(h5, 4);
            dg.addDataSet(h6, 5);
            dg.addDataSet(f2, 5);
            dg.addDataSet(f2calib, 5);
            //dg.addDataSet(h6_bis, 6); 
            dg.addDataSet(h9, 6); //E_{g}-E_{r_c_n} vs E_{g}
            dg.addDataSet(tgeE_theta, 7);
            dg.addDataSet(tgeE_phi, 8);
            dg.addDataSet(h7, 9);
            dg.addDataSet(h8, 10);//E_{g}-E_{r_c_p} vs E_{g}
            
            this.getDataGroup().add(dg, 1, 1, key);
           

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        return Arrays.asList(getCalibrationTable());
    }

    public int getNEvents(int isec, int ilay, int icomp) {
        return this.getDataGroup().getItem(1, 1, icomp).getH1F("h1_" + icomp).getEntries();
    }

    public void processEvent(DataEvent event) {
        Particle partGen = null;
        // get generated particle information
        if (event.hasBank("MC::Particle")) {
            DataBank genBank = event.getBank("MC::Particle");
            for (int loop = 0; loop < genBank.rows(); loop++) {
                Particle genPart = new Particle(
                        genBank.getInt("pid", loop),
                        genBank.getFloat("px", loop),
                        genBank.getFloat("py", loop),
                        genBank.getFloat("pz", loop),
                        genBank.getFloat("vx", loop),
                        genBank.getFloat("vy", loop),
                        genBank.getFloat("vz", loop));
                if (genPart.pid() == 22 || genPart.pid() == 11) {//Sia per i fotoni che per gli elettroni
                    if (partGen == null) {
                        partGen = genPart;
                    }
                }
            }
        }
        // loop over FTCAL reconstructed cluster
        if (event.hasBank("FTCAL::clusters") && partGen != null) {
            DataBank recFTCAL = event.getBank("FTCAL::clusters");//if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.rows());
            for (int loop = 0; loop < recFTCAL.rows(); loop++) {
                int key = getDetector().getComponent(recFTCAL.getFloat("x", loop), recFTCAL.getFloat("y", loop));
                //if(recFTCAL.rows()>1)System.out.println(" recFTCAL.rows() "+recFTCAL.getFloat("x",loop)+" "+recFTCAL.getFloat("y",loop));//Only seed is included
                double energy = recFTCAL.getFloat("energy", loop);
                double energyR = recFTCAL.getFloat("recEnergy", loop);
                double energyR_corr=0;
                Vector3D cluster = new Vector3D(recFTCAL.getFloat("x", loop), recFTCAL.getFloat("y", loop), recFTCAL.getFloat("z", loop));
                theta[key]=Math.toDegrees(cluster.theta());//if(theta[key]<1)System.out.println(key + " theta= "+theta[key]);
                phi[key]=Math.toDegrees(cluster.phi());
                this.getDataGroup().getItem(1, 1, key).getH1F("h1_" + key).fill(energy);
                this.getDataGroup().getItem(1, 1, key).getH1F("h2_" + key).fill(energy - partGen.p());
                this.getDataGroup().getItem(1, 1, key).getH1F("h3_" + key).fill(Math.toDegrees(cluster.theta() - partGen.theta()));
                this.getDataGroup().getItem(1, 1, key).getH1F("h4_" + key).fill(Math.toDegrees(cluster.phi() - partGen.phi()));
                //Random Ran= new Random();
                //for(int j=0;j<1000;j++){
                // double x = Ran.nextDouble()*6;
                // this.dataGroups.getItem(1,1,key).getH2F("h5_"+key).fill(x,0.02*x-0.08+Ran.nextDouble()*0.04);//System.out.println("x " + x);
                // } 
                this.getDataGroup().getItem(1, 1, key).getH2F("h5_" + key).fill(energyR, partGen.p() - energyR);//System.out.println(" Analyzed 1");   
                if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                    F1D f2calib = this.getDataGroup().getItem(1, 1, key).getF1D("f2calib_" + key);//Is filled only if costants have been loaded
                    f2calib.setParameter(0, this.getPreviousCalibrationTable().getDoubleValue("c0", 1, 1, key));
                    f2calib.setParameter(1, this.getPreviousCalibrationTable().getDoubleValue("c1", 1, 1, key));
                    f2calib.setParameter(2, this.getPreviousCalibrationTable().getDoubleValue("c2", 1, 1, key));
                    f2calib.setParameter(3, this.getPreviousCalibrationTable().getDoubleValue("c3", 1, 1, key));
                    f2calib.setParameter(4, this.getPreviousCalibrationTable().getDoubleValue("c4", 1, 1, key));    
                    
                  energyR_corr = energyR + (f2calib.getParameter(0)+energyR*f2calib.getParameter(1)+energyR*energyR*f2calib.getParameter(2)+energyR*energyR*energyR*f2calib.getParameter(3)+energyR*energyR*energyR*energyR*f2calib.getParameter(4))/1000.;
                this.getDataGroup().getItem(1, 1, key).getH2F("h7_" + key).fill(partGen.p(), partGen.p() -energyR_corr);//System.out.println(" Analyzed 1 = "+ (partGen.p() -energyR_corr));
                         
                
                }

            }
        }
    }

    public void analyze() {

        for (int key : this.getDetector().getDetectorComponents()) {

            ArrayList<H1F> hslice_0 = this.getDataGroup().getItem(1, 1, key).getH2F("h7_" + key).getSlicesX();

            for (int i = 0; i < hslice_0.size(); i++) {
                // System.out.println(" Slice "+i + " Key "+key);
                double x = this.getDataGroup().getItem(1, 1, key).getH2F("h7_" + key).getXAxis().getBinCenter(i);
                double ex = 0;
                double y = hslice_0.get(i).getRMS();
                double ey = 0;
                double mean = hslice_0.get(i).getDataX(hslice_0.get(i).getMaximumBin());
                double amp = hslice_0.get(i).getBinContent(hslice_0.get(i).getMaximumBin());
                double sigma = hslice_0.get(i).getRMS();
                F1D f1 = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", -1.0, 1.0);
                f1.setParameter(0, amp);
                f1.setParameter(1, mean);
                f1.setParameter(2, 0.01);
                DataFitter.fit(f1, hslice_0.get(i), "Q"); //No options uses error for sigma 
                if (amp > 1 && f1.parameter(1).error() < 1.0 && f1.getParameter(1) > -1.0) {
                    this.getDataGroup().getItem(1, 1, key).getGraph("h8_" + key).addPoint(x, f1.getParameter(1), ex, f1.parameter(1).error());
                }
            //System.out.println(" Added point "+x+" "+f1.getParameter(1)+" "+ex+" "+f1.parameter(1).error());

            }
            
            ArrayList<H1F> hslice_1 = this.getDataGroup().getItem(1, 1, key).getH2F("h5_" + key).getSlicesX();

            for (int i = 0; i < hslice_1.size(); i++) {
                // System.out.println(" Slice "+i + " Key "+key);
                double x = this.getDataGroup().getItem(1, 1, key).getH2F("h5_" + key).getXAxis().getBinCenter(i);
                double ex = 0;
                double y = hslice_1.get(i).getRMS();
                double ey = 0;
                double mean = hslice_1.get(i).getDataX(hslice_1.get(i).getMaximumBin());
                double amp = hslice_1.get(i).getBinContent(hslice_1.get(i).getMaximumBin());
                double sigma = hslice_1.get(i).getRMS();
                F1D f1 = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", -1.0, 1.0);
                f1.setParameter(0, amp);
                f1.setParameter(1, mean);
                f1.setParameter(2, 0.01);
                DataFitter.fit(f1, hslice_1.get(i), "Q"); //No options uses error for sigma 
                if (amp > 1 && f1.parameter(1).error() < 1.0 && f1.getParameter(1) > -1.0) {
                    this.getDataGroup().getItem(1, 1, key).getGraph("h6_" + key).addPoint(x, f1.getParameter(1), ex, f1.parameter(1).error());
                }
            //System.out.println(" Added point "+x+" "+f1.getParameter(1)+" "+ex+" "+f1.parameter(1).error());

            }
            DataFitter.fit(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key), this.getDataGroup().getItem(1, 1, key).getGraph("h6_" + key), "Q");
             
          
            double Egen_Erec=0;
            for (int i = 0; i < hslice_1.size(); i++) {
            
                double x = this.getDataGroup().getItem(1, 1, key).getH2F("h5_" + key).getXAxis().getBinCenter(i);
                double ex = 0;
                double y = hslice_1.get(i).getRMS();
                double ey = 0;
                double mean = hslice_1.get(i).getDataX(hslice_1.get(i).getMaximumBin());
                double amp = hslice_1.get(i).getBinContent(hslice_1.get(i).getMaximumBin());
                double sigma = hslice_1.get(i).getRMS();
                F1D f4 = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", -1.0, 1.0);
                f4.setParameter(0, amp);
                f4.setParameter(1, mean);
                f4.setParameter(2, 0.01);
                DataFitter.fit(f4, hslice_1.get(i), "Q"); 
                double Egen = f4.getParameter(1);
                Egen=Egen+x;
                double c_0=this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(0);
                double c_1=this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(1);
                double c_2=this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(2);
                double c_3=this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(3);
                double c_4=this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(4);
                Egen_Erec=Egen-(x+0.001*(c_0+x*c_1+x*x*c_2+x*x*x*c_3+x*x*x*x*c_4));
        // System.out.println(" Egen-Erec"+Egen_Erec+" , "+x);
                if (amp > 1 && f4.parameter(1).error() < 1.0 && f4.getParameter(1) > -1.0) {
            this.getDataGroup().getItem(1, 1, key).getGraph("h9_" + key).addPoint(Egen, Egen_Erec, ex, ex);
            this.getDataGroup().getItem(1, 1, key).getGraph("tgeE_theta").addPoint(theta[key], Egen_Erec, ex, ex);
            this.getDataGroup().getItem(1, 1, key).getGraph("tgeE_phi").addPoint(phi[key], Egen_Erec, ex, ex);
                }
            //if(Egen_Erec>5.)System.out.println("Egen_Erec= "+Egen_Erec+" , "+key);
            //if(theta[key]<1)System.out.println("theta= "+theta[key]+", Egen_Erec= "+Egen_Erec+" , "+key);
            
            }
            
      // consts=(" a "+1 + " b "+2);

      // calib.setStringValue(consts,"a",1, 1, key); 
            if(this.getDataGroup().getItem(1, 1, key).getH1F("h1_" + key).getEntries()>minNumberOfEvents) {
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(0), "c0", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(1), "c1", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(2), "c2", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(3), "c3", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(4), "c4", 1, 1, key);
            }
        }
        getCalibrationTable().show();
    }

    @Override
    public Color getColor(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        if (this.getDetector().hasComponent(key)) {
            int nent = this.getNEvents(sector, layer, key);
            if (nent > 0) {
                col = palette.getColor3D(nent, this.getnProcessed(), true);
            }
        }
//        col = new Color(100, 0, 0);
        return col;
    }
}
