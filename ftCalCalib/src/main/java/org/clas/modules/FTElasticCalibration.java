package org.clas.modules;

import org.clas.ftdata.FTCalCluster;
import org.clas.ftdata.FTCalEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTAdjustFit;
import org.clas.viewer.FTCalConstants;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author devita
 */
public class FTElasticCalibration extends FTCalibrationModule {

    // analysis realted info
    double ebeam=2221.93;//2217;//10604;//6424;//5302;//
//    double seed = 1370*ebeam/2217;//1370;//6980;//1370*ebeam/; 
    double seedTotRatio = 0.58;
    double minEvents = 50;
 

    public FTElasticCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "seed:seed_error:factor:factor_err:mips_charge:",3, ccdb, gConstants);
        this.setCCDBTable("/calibration/ft/ftcal/charge_to_energy");
        this.initRange(ebeam*0.2, ebeam*1.5);
        this.setCols(ebeam*0.9, ebeam*1.1);
        this.setReference(ebeam);

    }

    @Override
    public void resetEventListener() {
        
        H1F hClusterSum = new H1F("hClusterSum", 200, this.getRange()[0], this.getRange()[1]);
        hClusterSum.setTitleX("E (MeV)");
        hClusterSum.setTitleY("Counts");
        hClusterSum.setTitle("Cluster energy");
        hClusterSum.setFillColor(3);
        H1F hClusterSum_calib = new H1F("hClusterSum_calib", 200, this.getRange()[0], this.getRange()[1]);
        hClusterSum_calib.setTitleX("E (MeV)");
        hClusterSum_calib.setTitleY("counts");
        hClusterSum_calib.setTitle("Cluster energy");
        hClusterSum_calib.setFillColor(44);
        F1D fsum = new F1D("fsum", "[amp]*gaus(x,[mean],[sigma])", this.getRange()[1]*0.3, this.getRange()[1]*0.6);
        fsum.setParameter(0, 0.0);
        fsum.setParameter(1, 0.0);
        fsum.setParameter(2, 2.0);
        fsum.setLineColor(24);
        fsum.setLineWidth(2);
        fsum.setOptStat(1111);
        H1F hClusterSum_theta = new H1F("hClusterSum_theta", 200, this.getRange()[0], this.getRange()[1]);
        hClusterSum_theta.setTitleX("E (MeV)");
        hClusterSum_theta.setTitleY("counts");
        hClusterSum_theta.setTitle("Cluster energy");
        hClusterSum_theta.setFillColor(55);
        GraphErrors  gefactors = new GraphErrors("gefactors");
        gefactors.setTitle("Correction Factor"); //  title
        gefactors.setTitleX("Crystal ID"); // X axis title
        gefactors.setTitleY("Correction Factor");   // Y axis title
        gefactors.setMarkerColor(3); // color from 0-9 for given palette
        gefactors.setMarkerSize(3);  // size in points on the screen
//        gtoffsets.setMarkerStyle(1); // Style can be 1 or 2
        gefactors.addPoint(0., 0., 0., 0.);
        gefactors.addPoint(1., 1., 0., 0.);
        H1F hefactors = new H1F("hefactors", 100, 0.8, 1.2);
        hefactors.setTitleX("Correction Factor");
        hefactors.setTitleY("Counts");
        hefactors.setFillColor(42);
        hefactors.setLineColor(22);
        hefactors.setOptStat("1111");
        H1F heconstants = new H1F("heconstants", 100, 0., 8.0);
        heconstants.setTitleX("Charge2Energy");
        heconstants.setTitleY("Counts");
        heconstants.setFillColor(43);
        heconstants.setLineColor(23);
        heconstants.setOptStat("1111");
        for (int key : this.getDetector().getDetectorComponents()) {
            // initialize data group
            H1F hCluster = new H1F("hCluster_" + key, 200, this.getRange()[0], this.getRange()[1]);
            hCluster.setTitleX("E (MeV)");
            hCluster.setTitleY("Counts");
            hCluster.setTitle("Component " + key);
            H1F hCluster_calib = new H1F("hCluster_calib_" + key, 200, this.getRange()[0], this.getRange()[1]);
            hCluster_calib.setTitleX("E (MeV)");
            hCluster_calib.setTitleY("Counts");
            hCluster_calib.setTitle("Component " + key);
            hCluster_calib.setFillColor(44);
            F1D fcluster = new F1D("fcluster_" + key, "[amp]*gaus(x,[mean],[sigma])", this.getRange()[1]*0.3, this.getRange()[1]*0.6);
            fcluster.setParameter(0, 0.0);
            fcluster.setParameter(1, 0.0);
            fcluster.setParameter(2, 2.0);
            fcluster.setLineColor(24);
            fcluster.setLineWidth(2);
            fcluster.setOptStat(1111);
            H1F hCluster_theta = new H1F("hCluster_theta_" + key, 200, this.getRange()[0], this.getRange()[1]);
            hCluster_theta.setTitleX("E (MeV)");
            hCluster_theta.setTitleY("Counts");
            hCluster_theta.setTitle("Component " + key); 
            hCluster_theta.setFillColor(55);
            H1F hSeed = new H1F("hSeed_" + key, 100, this.getRange()[0], this.getRange()[1]*0.6);
            hSeed.setTitleX("E (MeV)");
            hSeed.setTitleY("Counts");
            hSeed.setTitle("Component " + key);
            H1F hSeed_calib = new H1F("hSeed_calib_" + key, 100, this.getRange()[0], this.getRange()[1]*0.6);
            hSeed_calib.setTitleX("E (MeV)");
            hSeed_calib.setTitleY("Counts");
            hSeed_calib.setTitle("Component " + key);
            F1D fseed = new F1D("fseed_" + key, "[amp]*gaus(x,[mean],[sigma])", this.getRange()[1]*0.3, this.getRange()[1]*0.6);
            fseed.setParameter(0, 0.0);
            fseed.setParameter(1, 0.0);
            fseed.setParameter(2, 2.0);
            fseed.setLineColor(24);
            fseed.setLineWidth(2);
            fseed.setOptStat(1111);

            DataGroup dg = new DataGroup(3, 2);
            dg.addDataSet(hClusterSum      , 0);
            dg.addDataSet(hClusterSum_calib, 0);
            dg.addDataSet(fsum,              0);
            dg.addDataSet(hClusterSum_theta, 0);
            dg.addDataSet(hCluster,          1);
            dg.addDataSet(hCluster_calib,    1);
            dg.addDataSet(hCluster_theta,    1);
            dg.addDataSet(fcluster,          1);
            dg.addDataSet(hSeed,             2);
            dg.addDataSet(hSeed_calib,       2);
            dg.addDataSet(fseed,             2);
            dg.addDataSet(gefactors,         3);
            dg.addDataSet(hefactors,         4);
            dg.addDataSet(heconstants,       5);
            this.getDataGroup().add(dg, 1, 1, key);

        }
    }
    
    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("hCluster_" + key).getIntegral();
    }

    @Override
    public void loadConstants(IndexedTable table) {
        for(int i=0; i<table.getRowCount(); i++) {
            int sector = Integer.valueOf((String)table.getValueAt(i, 0));
            int layer  = Integer.valueOf((String)table.getValueAt(i, 1));
            int comp   = Integer.valueOf((String)table.getValueAt(i, 2));
            double cmips = table.getDoubleValue("mips_charge", sector, layer, comp);
            this.getPreviousCalibrationTable().addEntry(sector, layer, comp);
            for(int j=3; j<this.getPreviousCalibrationTable().getColumnCount(); j++) {
                this.getPreviousCalibrationTable().setDoubleValue(0.0, this.getPreviousCalibrationTable().getColumnName(j), sector, layer, comp);
            }                
            this.getPreviousCalibrationTable().setDoubleValue(cmips, "mips_charge", sector, layer, comp);
        }
        this.getPreviousCalibrationTable().fireTableDataChanged();    
    }


    @Override
    public void processEvent(FTCalEvent event) {
        // loop over FTCAL reconstructed cluster
        int run = event.getRun();
        if(run<=0) {
            return;
        }
        double ebeamRCDB = ebeam;
        if(run>3000) {
            ebeamRCDB = (double) this.getConstantsManager().getRcdbConstant(run, "beam_energy").getValue();
        }
        if(ebeamRCDB!=ebeam && ebeamRCDB!=0) {
            ebeam=ebeamRCDB;
            System.out.println("Setting beam energy to: " + ebeam + " MeV");
            this.initRange(ebeam*0.2, ebeam*1.5);
            this.setCols(ebeam*0.9, ebeam*1.1);
            this.setReference(ebeam);
        }

        // loop over FTCAL reconstructed cluster
        if (!event.getClusters().isEmpty()) {
            
            List<Particle> electrons0 = new ArrayList<>();
            List<Particle> electrons  = new ArrayList<>();

            // start from clusters
            for (FTCalCluster c : event.getClusters()) {
                
                if(c.charge()==-1 && c.size()>FTCalConstants.CLUSTERSIZE) {
                        
                    Particle electron0 = c.toParticle(false, event.getVertex());
                    electron0.setProperty("energySeed", c.get(0).energy(false));
                    
                    Particle electron  = c.toParticle(true, event.getVertex());
                    electron.setProperty("energySeed", c.get(0).energy(true));
                
                    double ratio = this.seedTotRatio;
                    if(this.isThisCrystalOnTheEdge(c.seed())) ratio=0.65;

         
                    if(c.energyR(true)>FTCalConstants.CLUSTERTHR/1000 && 
                       c.get(0).energy(true)>ratio*c.energy(true) &&     
                       c.energy(true)>1) {
                        electrons.add(electron);
                        electrons0.add(electron0);
                    }
                }
            }
            if(!electrons.isEmpty()) {
                for (int i = 0; i < electrons.size(); i++) {
                    int key = (int) electrons.get(i).getProperty("seed");
                    Particle electron0 = electrons0.get(i);
                    Particle electron  = electrons.get(i);
                    double pela  = PhysicsConstants.massProton()*1000*ebeam/(2*ebeam*Math.pow(Math.sin(electron.theta()/2), 2)+PhysicsConstants.massProton()*1000);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum").fill(electron0.p()*1E3);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_" + key).fill(electron0.p()*1E3);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum_calib").fill(electron.p()*1E3);                        
                    this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_calib_" + key).fill(electron.p()*1E3);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hSeed_" + key).fill(electron.getProperty("energySeed")*1E3);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hClusterSum_theta").fill(pela, 0.1);
                    this.getDataGroup().getItem(1, 1, key).getH1F("hCluster_theta_" + key).fill(pela, 0.1);
                }
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
        H1F hsum = this.getDataGroup().getItem(1, 1, 8).getH1F("hClusterSum_calib");
        F1D fsum = this.getDataGroup().getItem(1, 1, 8).getF1D("fsum");
//        this.initClusterGaussFitPar(fsum,hsum);
//        hsum.setFunction(null);
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hseed = this.getDataGroup().getItem(1,1,key).getH1F("hSeed_" + key);
            F1D fseed = this.getDataGroup().getItem(1,1,key).getF1D("fseed_" + key);
            this.seedGaussFitPar(fseed,hseed);
//            if(fseed.getParameter(0)>10) DataFitter.fit(fseed,hseed,"LQ");
//            hseed.setFunction(null);
            H1F hcluster = this.getDataGroup().getItem(1,1,key).getH1F("hCluster_calib_" + key);
            F1D fcluster = this.getDataGroup().getItem(1,1,key).getF1D("fcluster_" + key);
            this.clusterGaussFitPar(fcluster,hcluster);
//            if(fcluster.getParameter(0)>10) DataFitter.fit(fcluster,hcluster,"LQ");
//            hcluster.setFunction(null);
        }
    }


    @Override
    public void adjustFit() {
        System.out.println("Adjusting fit for component " + this.getSelectedKey());
        H1F hseed = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getH1F("hCluster_calib_" + this.getSelectedKey());
        F1D fseed = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getF1D("fcluster_" + this.getSelectedKey());
        FTAdjustFit cfit = new FTAdjustFit(hseed, fseed, "LRQ");
        this.getCanvas().update();
    }

    private void seedGaussFitPar(F1D fseed, H1F hseed) {
        double hAmp  = hseed.getBinContent(hseed.getMaximumBin());
        double hMean = hseed.getAxis().getBinCenter(hseed.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.02*hMean)); 
        double rangeMax = (hMean + (0.80*hMean));  
        double pm = hMean*0.2;
        fseed.setRange(rangeMin, rangeMax);
        fseed.setParameter(0, hAmp);
        fseed.setParLimits(0, hAmp*0.8, hAmp*1.2);
        fseed.setParameter(1, hMean);
        fseed.setParLimits(1, hMean-pm, hMean+pm);
        fseed.setParameter(2, 80);
        fseed.setParLimits(2, 10., 500.);
//        System.out.println(fseed.getRange().getMin() + " " + fseed.getRange().getMin() + " " + rangeMin + " " + rangeMax);
        if(hAmp>this.minEvents) {
            DataFitter.fit(fseed,hseed,"LQ");
            hseed.setFunction(null);
        }
    }    

    private void clusterGaussFitPar(F1D fseed, H1F hseed) {
        double hAmp  = hseed.getBinContent(hseed.getMaximumBin());
        double hMean = hseed.getAxis().getBinCenter(hseed.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.045*hMean)); 
        double rangeMax = (hMean + (0.070*hMean));  
        double pm = hMean*0.1;
        fseed.setRange(rangeMin, rangeMax);
        fseed.setParameter(0, hAmp);
//        fseed.setParLimits(0, hAmp*0.8, hAmp*1.2);
        fseed.setParameter(1, hMean);
//        fseed.setParLimits(1, hMean-pm, hMean+pm);
        fseed.setParameter(2, hMean*0.03);
//        fseed.setParLimits(2, 10., 500.);
//        System.out.println(fseed.getRange().getMin() + " " + fseed.getRange().getMin() + " " + rangeMin + " " + rangeMax);
        if(hAmp>this.minEvents) {
            DataFitter.fit(fseed,hseed,"LQ");
            hseed.setFunction(null);
        }

    }    
    
    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            F1D fcluster = this.getDataGroup().getItem(1,1,key).getF1D("fcluster_" + key);
            return fcluster.getParameter(1);
       }
        return 0;
    }

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 1);
    }
    
    @Override
    public void updateTable() {
        
        double c2eDefault = 4;
        if(this.getDataGroup().getItem(1,1,8).getH1F("heconstants").getMean()>0) 
            c2eDefault = this.getDataGroup().getItem(1,1,8).getH1F("heconstants").getMean();
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gefactors").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("hefactors").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("heconstants").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hcluster = this.getDataGroup().getItem(1,1,key).getH1F("hCluster_calib_" + key);
            F1D fcluster = this.getDataGroup().getItem(1,1,key).getF1D("fcluster_" + key);

            double clusterE     = 0;
            double clusterE_err = 0;
            double factorE      = 0;
            double factorE_err  = 0;
            double cmips        = FTCalConstants.CHARGEMIPS;
            if(this.getPreviousCalibrationTable().hasEntry(1,1,key)) {
                cmips = this.getPreviousCalibrationTable().getDoubleValue("mips_charge", 1, 1, key);
            }                        
            if(hcluster.getBinContent(hcluster.getMaximumBin())>10 && fcluster.getParameter(1)>500) {
                clusterE       = fcluster.getParameter(1);
                clusterE_err   = fcluster.parameter(1).error();
                factorE        = this.ebeam/fcluster.getParameter(1);
                factorE_err    = factorE*clusterE_err/clusterE;
                cmips          = cmips/factorE;
            }
            else {
                cmips          = c2eDefault;
            }
            this.getDataGroup().getItem(1,1,key).getGraph("gefactors").addPoint(key, factorE, 0, factorE_err);
            this.getDataGroup().getItem(1,1,key).getH1F("hefactors").fill(factorE);
            this.getDataGroup().getItem(1,1,key).getH1F("heconstants").fill(cmips);
            getCalibrationTable().setDoubleValue(clusterE,     "seed",            1, 1, key);
            getCalibrationTable().setDoubleValue(clusterE_err, "seed_error",      1, 1, key);
            getCalibrationTable().setDoubleValue(factorE,      "factor",          1, 1, key);
            getCalibrationTable().setDoubleValue(factorE_err,  "factor_err" ,     1, 1, key);
            getCalibrationTable().setDoubleValue(cmips,        "mips_charge" ,    1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }
    
        public boolean isThisCrystalOnTheEdge(int id) {

        boolean crystalEdge=false;
        int iy = id / 22;
        int ix = id - iy * 22;

        double xcrystal = 1.0 * (22 - ix - 0.5);
        double ycrystal = 1.0 * (22 - iy - 0.5);
        double rcrystal = Math.sqrt(Math.pow(xcrystal - 1.0 * 11, 2.0) + Math.pow(ycrystal - 1.0 * 11, 2.0));
        if (rcrystal < 1.0 * 4.8 || rcrystal >1.0 * 10.11) {
            crystalEdge=true;
        }
        return crystalEdge;
    }
    
}
