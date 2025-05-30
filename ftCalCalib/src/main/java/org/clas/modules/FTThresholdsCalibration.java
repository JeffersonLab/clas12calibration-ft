package org.clas.modules;

import org.clas.ftdata.FTCalADC;
import org.clas.ftdata.FTCalEvent;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalConstants;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTThresholdsCalibration extends FTCalibrationModule {

    IndexedTable charge2energy = null;

    public FTThresholdsCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "threshold:threshold_error",3, ccdb, gConstants);
        this.getCalibrationTable().addConstraint(3, 5, 40);     
        this.setCols(0,80);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 200, 0.0, 100.0);
        htsum.setTitleX("Threshold (MeV)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Thresholds Distribution");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 200, 0.0, 100.0);
        htsum_calib.setTitleX("Threshold (MeV)");
        htsum_calib.setTitleY("counts");
        htsum_calib.setTitle("Thresholds Distribution");
        htsum_calib.setFillColor(44);
        GraphErrors  gthresholds = new GraphErrors("gthresholds");
        gthresholds.setTitle("Thresholds"); //  title
        gthresholds.setTitleX("Crystal ID"); // X axis title
        gthresholds.setTitleY("Thresholds (MeV)");   // Y axis title
        gthresholds.setMarkerColor(5); // color from 0-9 for given palette
        gthresholds.setMarkerSize(5);  // size in points on the screen
//        gpedestals.setMarkerStyle(1); // Style can be 1 or 2
        gthresholds.addPoint(0., 0., 0., 0.);
        gthresholds.addPoint(1., 1., 0., 0.);
        H1F hthresholds = new H1F("hthresholds", 100, 0., 60.);
        hthresholds.setTitleX("Thresholds (MeV)");
        hthresholds.setTitleY("Crystals");
        hthresholds.setTitle("Thresholds");
        hthresholds.setFillColor(44);
        hthresholds.setLineColor(24);
        for (int key : this.getDetector().getDetectorComponents()) {
            // initialize data group
            H1F hthrs = new H1F("hthrs_" + key, 100, 0.0, 100.0);
            hthrs.setTitleX("Threshold (MeV)");
            hthrs.setTitleY("Counts");
            hthrs.setTitle("Component " + key);
            F1D fthrs = new F1D("fthrs_" + key, "[amp]*gaus(x,[mean],[sigma])", 0, 30);
            fthrs.setParameter(0, 0.0);
            fthrs.setParameter(1, 0.0);
            fthrs.setParameter(2, 2.0);
            fthrs.setLineColor(24);
            fthrs.setLineWidth(2);
//            fped.setLineColor(2);
//            fped.setLineStyle(1);
            DataGroup dg = new DataGroup(2, 2);
            dg.addDataSet(htsum      , 0);
            dg.addDataSet(gthresholds, 1);
            dg.addDataSet(hthrs,       2);
            dg.addDataSet(fthrs,       2);
            dg.addDataSet(hthresholds, 3);
            this.getDataGroup().add(dg, 1, 1, key);

        }
    }


    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("hthrs_" + key).getIntegral();
    }

    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            return this.getCalibrationTable().getDoubleValue("threshold", sector, layer, key);
       }
        return 0;
    }

    @Override
    public void processEvent(FTCalEvent event) {
        // loop over FTCAL reconstructed cluster
        int run = event.getRun();
        if(run<=0) {
            return;
        }
        if(this.getConstantsManager()!=null) charge2energy = this.getConstantsManager().getConstants(run, "/calibration/ft/ftcal/charge_to_energy");
        if (!event.getADCs().isEmpty()) {
            for (FTCalADC hit : event.getADCs()) {
                int    component = hit.component();
                int    adc       = hit.adc();
                double ped       = hit.pedestal();                
                if(adc>0) {
                    double c2e        = (FTCalConstants.LSB*FTCalConstants.NSPERSAMPLE/50)*FTCalConstants.EMIPS/FTCalConstants.CHARGEMIPS;
                    if(this.getConstantsManager()!=null) c2e = 1*charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
						                *charge2energy.getDoubleValue("mips_energy", 1,1,component)
						                /charge2energy.getDoubleValue("mips_charge", 1,1,component);
                    if(this.getGlobalCalibration().containsKey("EvergyCalibration") &&
                       this.getGlobalCalibration().get("EvergyCalibration").hasEntry(1,1,component)) {
                        c2e = charge2energy.getDoubleValue("fadc_to_charge", 1,1,component)
			     *charge2energy.getDoubleValue("mips_energy", 1,1,component)
			     /this.getGlobalCalibration().get("EvergyCalibration").getDoubleValue("charge2energy", 1, 1, component);
                    }
                    this.getDataGroup().getItem(1,1,component).getH1F("htsum").fill(adc*c2e);
                    this.getDataGroup().getItem(1,1,component).getH1F("hthrs_"+component).fill(adc*c2e);
                } 
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
//        this.getCanvas().getPad(0).getAxisY().setLog(true);
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getGraph("gthresholds").reset();
            this.getDataGroup().getItem(1,1,key).getH1F("hthresholds").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            H1F hthrs = this.getDataGroup().getItem(1,1,key).getH1F("hthrs_" + key);
            F1D fthrs = this.getDataGroup().getItem(1,1,key).getF1D("fthrs_" + key);
            this.initTimeGaussFitPar(fthrs,hthrs);
            DataFitter.fit(fthrs,hthrs,"LQ");
            
        
            double mean   = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).getParameter(1);
            double sigma  = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).getParameter(2);
            double emean  = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).parameter(1).error();
            double esigma = this.getDataGroup().getItem(1, 1, key).getF1D("fthrs_" + key).parameter(2).error();
            double thrs   = mean-1.177*sigma;
            double ethrs  = Math.sqrt(emean*emean+1.177*1.177*esigma*esigma);
            this.getDataGroup().getItem(1,1,key).getGraph("gthresholds").addPoint(key, mean, 0, emean);
            this.getDataGroup().getItem(1,1,key).getH1F("hthresholds").fill(mean);
            getCalibrationTable().setDoubleValue(mean,  "threshold",       1, 1, key);
            getCalibrationTable().setDoubleValue(emean, "threshold_error", 1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    private void initTimeGaussFitPar(F1D fped, H1F hped) {
        double hAmp  = hped.getBinContent(hped.getMaximumBin());
        double hMean = hped.getAxis().getBinCenter(hped.getMaximumBin());
        double hRMS  = hped.getRMS(); //ns
        double rangeMin = (hMean - (3.0*hRMS)); 
        double rangeMax = (hMean + (0.1*hRMS));  
        fped.setRange(rangeMin, rangeMax);
        fped.setParameter(0, hAmp);
        fped.setParLimits(0, hAmp*0.8, hAmp*1.2);
        fped.setParameter(1, hMean);
        fped.setParLimits(1, hMean-hRMS, hMean+hRMS);
        fped.setParameter(2, 0.2);
        fped.setParLimits(2, 0.1*hRMS, 2*hRMS);
    }    

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 3);
    }   
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
