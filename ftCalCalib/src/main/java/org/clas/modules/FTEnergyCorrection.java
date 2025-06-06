package org.clas.modules;

import org.clas.ftdata.FTCalCluster;
import org.clas.ftdata.FTCalEvent;
import java.util.ArrayList;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTEnergyCorrection extends FTCalibrationModule {

    private final int minNumberOfEvents =100;
    
    public FTEnergyCorrection(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "c0:c1:c2:c3:c4",7, ccdb, gConstants);
        this.setCCDBTable("/calibration/ft/ftcal/energycorr");
        this.setCols(0, 0.5);
    }

    @Override
    public void resetEventListener() {
        
        // general histos
        H1F hi_de_sum = new H1F("hi_de_sum", 100, -1., 1.);
        hi_de_sum.setTitleX("#DeltaE (GeV)");
        hi_de_sum.setTitleY("counts");
        hi_de_sum.setTitle("Resolution");
        H1F hi_de_calib_sum = new H1F("hi_de_calib_sum", 100, -1., 1.);
        hi_de_calib_sum.setTitleX("#DeltaE (GeV)");
        hi_de_calib_sum.setTitleY("counts");
        hi_de_calib_sum.setTitle("Resolution");
        hi_de_calib_sum.setFillColor(44);
        H2F hi_de_theta_calib = new H2F("hi_de_theta_calib", 100, 2.0, 5.5, 100, -0.5, 0.5);
        hi_de_theta_calib.setTitleX("#theta (deg)");
        hi_de_theta_calib.setTitleY("#DeltaEcal (GeV)");
        hi_de_theta_calib.setTitle("Component ");
        H2F hi_de_phi_calib = new H2F("hi_de_phi_calib", 100, -180, 180, 100, -0.5, 0.5);
        hi_de_phi_calib.setTitleX("#phi (deg)");
        hi_de_phi_calib.setTitleY("#DeltaEcal (GeV)");
        hi_de_phi_calib.setTitle("Component ");
        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa data group
            H1F hi_de = new H1F("hi_de_" + key, 100, -1., 1.);
            hi_de.setTitleX("#DeltaE (GeV)");
            hi_de.setTitleY("counts");
            hi_de.setTitle("Component " + key);
            H1F hi_de_calib = new H1F("hi_de_calib_" + key, 100, -1., 1.);
            hi_de_calib.setTitleX("#DeltaEcal (GeV)");
            hi_de_calib.setTitleY("counts");
            hi_de_calib.setTitle("Component " + key);
            hi_de_calib.setFillColor(44);
            H1F hi_dtheta = new H1F("hi_dtheta_" + key, 100, -1., 1.);
            hi_dtheta.setTitleX("#Delta#theta (deg)");
            hi_dtheta.setTitleY("counts");
            hi_dtheta.setTitle("Component " + key);
            H1F hi_dphi = new H1F("hi_dphi_" + key, 100, -10., 10.);
            hi_dphi.setTitleX("#Delta#phi (deg)");
            hi_dphi.setTitleY("counts");
            hi_dphi.setTitle("Component " + key);
            H2F hi_de_e = new H2F("hi_de_e_" + key, 100, 0., 11., 125, 0.0, 2.5);//x,y
            hi_de_e.setTitleX("Erec(GeV)");
            hi_de_e.setTitleY("#DeltaErec (GeV)");
            hi_de_e.setTitle("Component " + key);
            GraphErrors g_de_e = new GraphErrors("g_de_e_" + key);
            g_de_e.setTitle("Component " + key);
            g_de_e.setTitleX("Erec (GeV)");
            g_de_e.setTitleY("#DeltaErec (GeV)");
            g_de_e.setMarkerSize(2);
            g_de_e.addPoint(0, 0, 0, 0);
            g_de_e.addPoint(1, 1, 0, 0);
//            double q = 0.;
//            double m = 0.;
//            double c1 = 0.;
//            double c2 = 0.;
//            double c3 = 0.;
            F1D f2 = new F1D("f2_" + key, "([c0]+x*[c1]+x*x*[c2]+x*x*x*[c3]+x*x*x*x*[c4])/1000.", 0, 10.0);
            f2.setLineColor(2);
            f2.setLineStyle(1);
            f2.setLineWidth(2);
            F1D f2calib = new F1D("f2calib_" + key, "([c0]+x*[c1]+x*x*[c2]+x*x*x*[c3]+x*x*x*x*[c4])/1000.", 0, 10.0);
            f2calib.setLineColor(4);
            f2calib.setLineStyle(1);
            H2F hi_de_e_calib = new H2F("hi_de_e_calib_" + key, 100, 0., 11., 50, -0.5, 0.5);
            hi_de_e_calib.setTitleX("Egen (GeV)");
            hi_de_e_calib.setTitleY("#DeltaEcal (GeV)");
            hi_de_e_calib.setTitle("Component " + key);            
        
            DataGroup dg = new DataGroup(3, 3);
            
            dg.addDataSet(hi_de_sum,         0);
            dg.addDataSet(hi_de_calib_sum,   0);
            dg.addDataSet(hi_de_theta_calib, 1);
            dg.addDataSet(hi_de_phi_calib,   2);
            dg.addDataSet(hi_de,             3);
            dg.addDataSet(hi_de_calib,       3);
            dg.addDataSet(hi_dtheta,         4);
            dg.addDataSet(hi_dphi,           5);
            dg.addDataSet(hi_de_e,           6);
            dg.addDataSet(f2calib,           6);
            dg.addDataSet(g_de_e,            7);
            dg.addDataSet(f2,                7);
            dg.addDataSet(f2calib,           7);
            dg.addDataSet(hi_de_e_calib,     8);
            
            this.getDataGroup().add(dg, 1, 1, key);
           

        }
    }

    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("hi_de_" + key).getIntegral();
    }

    @Override
    public void processEvent(FTCalEvent event) {
        
        // loop over FTCAL reconstructed cluster
        if (event.getGenerated() != null && !event.getClusters().isEmpty()) {

            for (FTCalCluster c : event.getClusters()) {
                
                int key = c.seed();
                double energy  = c.energy(true);
                double energyR = c.energyR(true);
                double energyR_corr=0;
                Vector3D cluster = c.position(true).toVector3D();
                Particle partGen = event.getGenerated();
                
                this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_sum").fill(energy - partGen.p());
                this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_" + key).fill(energy - partGen.p());
                this.getDataGroup().getItem(1, 1, key).getH1F("hi_dtheta_" + key).fill(Math.toDegrees(cluster.theta() - partGen.theta()));
                this.getDataGroup().getItem(1, 1, key).getH1F("hi_dphi_" + key).fill(Math.toDegrees(cluster.phi() - partGen.phi()));
                this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_e_" + key).fill(energyR, partGen.p() - energyR);
                
                if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                    F1D f2calib = this.getDataGroup().getItem(1, 1, key).getF1D("f2calib_" + key);//Is filled only if costants have been loaded
                    
                    energyR_corr = energyR + (f2calib.getParameter(0)+energyR*f2calib.getParameter(1)+energyR*energyR*f2calib.getParameter(2)
                            +energyR*energyR*energyR*f2calib.getParameter(3)+energyR*energyR*energyR*energyR*f2calib.getParameter(4))/1000.;
                    this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_calib_sum").fill(partGen.p() -energyR_corr);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_calib_" + key).fill(partGen.p() -energyR_corr);
                    this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_e_calib_" + key).fill(partGen.p(), partGen.p() -energyR_corr);
                    this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_theta_calib").fill(Math.toDegrees(partGen.theta()), partGen.p() -energyR_corr);
                    this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_phi_calib").fill(Math.toDegrees(partGen.phi()), partGen.p() -energyR_corr);                
                }

            }
        }
    }

    @Override
    public void analyze() {
        
        this.loadConstantsToFunctions();
        this.printOut("analysis of energy correction data...");
        for (int key : this.getDetector().getDetectorComponents()) {
            
            if(this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_" + key).getIntegral()>minNumberOfEvents) {
                this.getDataGroup().getItem(1, 1, key).getGraph("g_de_e_" + key).reset();
                ArrayList<H1F> hslice_1 = this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_e_" + key).getSlicesX();
                for (int i = 0; i < hslice_1.size(); i++) {
                    // System.out.println(" Slice "+i + " Key "+key);
                    double x = this.getDataGroup().getItem(1, 1, key).getH2F("hi_de_e_" + key).getXAxis().getBinCenter(i);
                    double ex = 0;
                    double y = hslice_1.get(i).getRMS();
                    double ey = 0;
                    double mean  = hslice_1.get(i).getDataX(hslice_1.get(i).getMaximumBin());
                    double amp   = hslice_1.get(i).getBinContent(hslice_1.get(i).getMaximumBin());
                    double sigma = hslice_1.get(i).getRMS();
                    if (amp > 10) {
                        F1D f1 = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", mean-0.4,mean+0.15);
                        f1.setParameter(0, amp);
                        f1.setParameter(1, mean);
                        f1.setParameter(2, 0.01);
                        DataFitter.fit(f1, hslice_1.get(i), "Q"); //No options uses error for sigma 
                        f1.setRange(f1.getParameter(1)-4*f1.getParameter(2), f1.getParameter(1)+2*f1.getParameter(2));
                        DataFitter.fit(f1, hslice_1.get(i), "Q"); //No options uses error for sigma
                        if (f1.parameter(1).error() < 0.01 && f1.getParameter(1) > -1.0 && x<=8 && f1.getParameter(1)<(0.5+0.4*x)) {
                            this.getDataGroup().getItem(1, 1, key).getGraph("g_de_e_" + key).addPoint(x, f1.getParameter(1), ex, f1.parameter(1).error());
                        }
                    }
                }
                this.getDataGroup().getItem(1, 1, key).getGraph("g_de_e_" + key).addPoint(0,0,0,0.001);
                DataFitter.fit(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key), this.getDataGroup().getItem(1, 1, key).getGraph("g_de_e_" + key), "Q");


                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(0), "c0", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(1), "c1", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(2), "c2", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(3), "c3", 1, 1, key);
                getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("f2_" + key).getParameter(4), "c4", 1, 1, key);
                System.out.print(".");
            }
        }
        getCalibrationTable().fireTableDataChanged();
        System.out.println(" done");
    }

    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            return this.getDataGroup().getItem(1, 1, key).getH1F("hi_de_calib_" + key).getRMS();
       }
        return 0;
    }

    @Override
    public void loadConstants(IndexedTable table) {
        for(int i=0; i<table.getRowCount(); i++) {
            int sector = Integer.valueOf((String)table.getValueAt(i, 0));
            int layer  = Integer.valueOf((String)table.getValueAt(i, 1));
            int comp   = Integer.valueOf((String)table.getValueAt(i, 2));
            this.getPreviousCalibrationTable().addEntry(sector, layer, comp);
            for(int j=3; j<this.getPreviousCalibrationTable().getColumnCount(); j++) {
                String colname = (String)this.getPreviousCalibrationTable().getColumnName(j);
                this.getPreviousCalibrationTable().setDoubleValue(table.getDoubleValue(colname, sector, layer, comp), 
                                                                  colname, 
                                                                  sector, layer, comp);
            }
        }
        this.getPreviousCalibrationTable().fireTableDataChanged();    
    }

    @Override
    public void loadConstantsToFunctions() {
        if(this.getPreviousCalibrationTable().hasEntry(1,1,8)) {
            for (int key : this.getDetector().getDetectorComponents()) {

                F1D f2calib = this.getDataGroup().getItem(1, 1, key).getF1D("f2calib_" + key);//Is filled only if costants have been loaded
                f2calib.setParameter(0, this.getPreviousCalibrationTable().getDoubleValue("c0", 1, 1, key));
                f2calib.setParameter(1, this.getPreviousCalibrationTable().getDoubleValue("c1", 1, 1, key));
                f2calib.setParameter(2, this.getPreviousCalibrationTable().getDoubleValue("c2", 1, 1, key));
                f2calib.setParameter(3, this.getPreviousCalibrationTable().getDoubleValue("c3", 1, 1, key));
                f2calib.setParameter(4, this.getPreviousCalibrationTable().getDoubleValue("c4", 1, 1, key));   
            }
        }
    }
    
    @Override
    public void setCanvasBookData() {
        int[] pads = {6,7,8};
        this.getCanvasBook().setData(this.getDataGroup(), pads);
    }

    @Override
    public void setDrawOptions() {
        this.getCanvas().getPad(6).getAxisY().setRange(0,2.5);
//        F1D f2limit = new F1D("f2limit", "[p0]+x*[p1]", 0, 10.0);
//        f2limit.setLineColor(3);
//        f2limit.setLineStyle(1);
//        f2limit.setParameter(0, 0.5);
//        f2limit.setParameter(1, 0.4); 
//        this.getCanvas().cd(7);
//        this.getCanvas().draw(f2limit,"same");
    }

    @Override
    public void timerUpdate() {
    }
}
