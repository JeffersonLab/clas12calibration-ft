package org.clas.modules;

import org.clas.ftdata.FTCalCluster;
import org.clas.ftdata.FTCalEvent;
import org.clas.ftdata.FTCalHit;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalConstants;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTTimeWalkCalibration extends FTCalibrationModule {

    
    public FTTimeWalkCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "amp:amp_error:lambda:lambda_error:offset:offset_error:A:A_error:L:L_error:O:O_error",3, ccdb, gConstants);
        this.setCCDBTable("/calibration/ft/ftcal/time_walk");
    }

    @Override
    public void resetEventListener() {

        H2F htsum = new H2F("htsum", 100, 0., 400., 400, -10., 10.);
        htsum.setTitleX("Charge (pC)");
        htsum.setTitleY("Time Offset (ns)");
        htsum.setTitle("Global Time Walk");

        GraphErrors  gtsum = new GraphErrors("gtsum");
        gtsum.setTitle("Global Time Walk"); //  title
        gtsum.setTitleX("Charge (pC)"); // X axis title
        gtsum.setTitleY("Time (ns)");   // Y axis title
        gtsum.setMarkerSize(3);  // size in points on the screen
        gtsum.addPoint(0., 0., 0., 0.);
        gtsum.addPoint(1., 1., 0., 0.);
        F1D ftsum = new F1D("ftsum", "[amp]*exp(-x*[lambda])+[offset]", 4*FTCalConstants.CHARGETHR, 400.);
        ftsum.setParameter(0, 1.300);
        ftsum.setParameter(1, 0.013);
        ftsum.setParLimits(0, 0.5,  5.0);
        ftsum.setParLimits(1, 0.0,  0.6);
        ftsum.setParLimits(2,-1.0,  1.0);
        ftsum.setLineColor(24);
        ftsum.setLineWidth(2);

        H2F htsum_calib = new H2F("htsum_calib", 100, 0., 400., 400, -10., 10.);
        htsum_calib.setTitleX("Charge (pC)");
        htsum_calib.setTitleY("Time (ns)");
        htsum_calib.setTitle("Global Time Walk");
        
        H1F htwamp = new H1F("htwamp", 100, 0., 8.);
        htwamp.setTitleX("TW amplitude");
        htwamp.setTitleY("Counts");
        htwamp.setFillColor(42);
        htwamp.setLineColor(22);
        htwamp.setOptStat("1111");
        H1F htwlambda = new H1F("htwlambda", 100, 0., 0.1);
        htwlambda.setTitleX("TW lambda");
        htwlambda.setTitleY("Counts");
        htwlambda.setFillColor(43);
        htwlambda.setLineColor(23);
        htwlambda.setOptStat("1111");

        F1D ftglob = new F1D("ftglob", "[amp]*exp(-x*[lambda])+[offset]", 2*FTCalConstants.CHARGETHR, 400.);
        ftglob.setParameter(0,  1.300);
        ftglob.setParameter(1,  0.013);
        ftglob.setParameter(2, -0.5);
        ftglob.setLineColor(22);
        ftglob.setLineWidth(2);

        for (int key : this.getDetector().getDetectorComponents()) {
            // initializa calibration constant table
            this.getCalibrationTable().setDoubleValue(0.00, "A",      1,1,key);
            this.getCalibrationTable().setDoubleValue(0.55, "L",      1,1,key);
            this.getCalibrationTable().setDoubleValue(0.00, "O",      1,1,key);
            this.getCalibrationTable().setDoubleValue(0.00, "amp",    1,1,key);
            this.getCalibrationTable().setDoubleValue(0.55, "lambda", 1,1,key);
            this.getCalibrationTable().setDoubleValue(0.00, "offset", 1,1,key);

            // initialize data group
            H2F htime = new H2F("htime_" + key, 50, 0, 500, 100, -10., 10.);
            htime.setTitleX("Charge (pC)");
            htime.setTitleY("Time (ns)");
            htime.setTitle("Component " + key);
            GraphErrors  gtime = new GraphErrors("gtime_" + key);
            gtime.setTitle("Component " + key); //  title
            gtime.setTitleX("Charge (pC)"); // X axis title
            gtime.setTitleY("Time (ns)");   // Y axis title
            gtime.addPoint(0., 0., 0., 0.);
            gtime.addPoint(1., 1., 0., 0.);
            gtime.setMarkerSize(3);
            H2F htime_calib = new H2F("htime_calib_" + key, 50, 0, 500, 100, -10., 10.);
            htime_calib.setTitleX("Time (ns)");
            htime_calib.setTitleY("Counts");
            htime_calib.setTitle("Component " + key);
            F1D ftime = new F1D("ftime_" + key, "[amp]*exp(-x*[lambda])+[offset]", 2*FTCalConstants.CHARGETHR, 250.);
            ftime.setParameter(0,  1.300);
            ftime.setParameter(1,  0.013);
            ftime.setParameter(2, -0.500);
            ftime.setParLimits(0, 0.5,  5.0);
            ftime.setParLimits(1, 0.0,  0.1);
            ftime.setParLimits(2,-1.0,  1.0);
            ftime.setLineColor(24);
            ftime.setLineWidth(2);
            ftime.setOptStat("1111");
//            ftime.setLineColor(2);
//            ftime.setLineStyle(1);
            DataGroup dg = new DataGroup(4, 2);
            dg.addDataSet(htsum,       0);
            dg.addDataSet(gtsum,       1);
            dg.addDataSet(ftsum,       1);
            dg.addDataSet(ftglob,      1);
            dg.addDataSet(htsum_calib, 2);
            dg.addDataSet(htwamp,      3);
            dg.addDataSet(htime,       4);
            dg.addDataSet(gtime,       5);
            dg.addDataSet(ftime,       5);
            dg.addDataSet(ftglob,      5);
            dg.addDataSet(htime_calib, 6);
            dg.addDataSet(htwlambda,   7);
            this.getDataGroup().add(dg, 1, 1, key);

        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH2F("htime_" + key).getEntries();
    }

    @Override
    public void loadConstants(IndexedTable table) {
        for(int i=0; i<table.getRowCount(); i++) {
            int sector = Integer.valueOf((String)table.getValueAt(i, 0));
            int layer  = Integer.valueOf((String)table.getValueAt(i, 1));
            int comp   = Integer.valueOf((String)table.getValueAt(i, 2));
            double amplitude  = table.getDoubleValue("amplitude", sector, layer, comp);
            double lambda     = table.getDoubleValue("lambda",    sector, layer, comp);
            double offset     = table.getDoubleValue("offset",    sector, layer, comp);
            this.getPreviousCalibrationTable().addEntry(sector, layer, comp);
            for(int j=3; j<this.getPreviousCalibrationTable().getColumnCount(); j++) {
                this.getPreviousCalibrationTable().setDoubleValue(0.0, this.getPreviousCalibrationTable().getColumnName(j), sector, layer, comp);
            }
            this.getPreviousCalibrationTable().setDoubleValue(amplitude, "amplitude", sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(lambda,    "lambda",    sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(offset,    "offset",    sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(amplitude, "A",         sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(lambda,    "L",         sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(offset,    "O",         sector, layer, comp);                
        }
        this.getPreviousCalibrationTable().fireTableDataChanged();    
    }

    @Override
    public void processEvent(FTCalEvent event) {
            // loop over FTCAL reconstructed cluster
        if(event.getTriggerPID()==11 && event.getStartTime()>0 && !event.getClusters().isEmpty()) {
            // start from clusters
            for (FTCalCluster c : event.getClusters()) {
                
                double theta = Math.toDegrees(c.position(true).toVector3D().theta());
                
                if(c.energy(true)>0.5 && c.energyR(true)>0.3 && c.size() > 3 && c.charge()==0 && theta>2.5 && theta<4.5) {                            
                    for(FTCalHit h : c) {
                        
                        int component = h.component();
    
                        double timec = h.getZeroTwTime(true) - event.getStartTime() - h.path(event.getVertex())/PhysicsConstants.speedOfLight();
                        if(h.charge()>FTCalConstants.CHARGETHR && h.time(false)>0) {
                            this.getDataGroup().getItem(1,1,component).getH2F("htsum").fill(h.charge(), timec);
                            this.getDataGroup().getItem(1,1,component).getH2F("htime_"+component).fill(h.charge(),timec);
                            if(this.getPreviousCalibrationTable().hasEntry(1,1,component)) {
                                double amp = this.getPreviousCalibrationTable().getDoubleValue("A", 1,1,component);
                                double lam = this.getPreviousCalibrationTable().getDoubleValue("L", 1,1,component);
                                double off = this.getPreviousCalibrationTable().getDoubleValue("O", 1,1,component);
                                this.getDataGroup().getItem(1,1,component).getF1D("ftglob").setParameter(0,amp);
                                this.getDataGroup().getItem(1,1,component).getF1D("ftglob").setParameter(1,lam);
                                this.getDataGroup().getItem(1,1,component).getF1D("ftglob").setParameter(2,off);
                                double twcorr = amp*Math.exp(-h.charge()*lam);
                                this.getDataGroup().getItem(1,1,component).getH2F("htsum_calib").fill(h.charge(),timec-twcorr);
                                this.getDataGroup().getItem(1,1,component).getH2F("htime_calib_"+component).fill(h.charge(),timec-twcorr);                        
                            }                            
                        }                                                
                    }
                }              
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1, 1, key).getH1F("htwamp").reset();
            this.getDataGroup().getItem(1, 1, key).getH1F("htwlambda").reset();
        }        
        for (int key : this.getDetector().getDetectorComponents()) {
            GraphErrors gtsum = this.getDataGroup().getItem(1,1,key).getGraph("gtsum");
            H2F         htsum = this.getDataGroup().getItem(1,1,key).getH2F("htsum");
            F1D         ftsum = this.getDataGroup().getItem(1,1,key).getF1D("ftsum");
            GraphErrors gtime = this.getDataGroup().getItem(1,1,key).getGraph("gtime_" + key);
            H2F         htime = this.getDataGroup().getItem(1,1,key).getH2F("htime_" + key);
            F1D         ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_" + key);
            this.maxGraph(htsum, gtsum);
            this.maxGraph(htime, gtime);
            if(gtsum.getVectorX().size()>10) {
                ftsum.setParameter(0, 1.3);
                ftsum.setParameter(1, 0.013);
                ftsum.setParameter(2, -0.5);
                DataFitter.fit(ftsum,gtsum,"LQ");
                ftsum.setOptStat("1111");
                double Amp     = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").getParameter(0);
                double AmpE    = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").parameter(0).error();
                double Lambda  = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").getParameter(1);
                double LambdaE = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").parameter(1).error();
                double Offset  = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").getParameter(2);
                double OffsetE = this.getDataGroup().getItem(1, 1, key).getF1D("ftsum").parameter(2).error();
                getCalibrationTable().setDoubleValue(Amp,     "A",            1, 1, key);
                getCalibrationTable().setDoubleValue(AmpE,    "A_error",      1, 1, key);
                getCalibrationTable().setDoubleValue(Lambda,  "L",            1, 1, key);
                getCalibrationTable().setDoubleValue(LambdaE, "L_error",      1, 1, key);
                getCalibrationTable().setDoubleValue(Offset,  "O",            1, 1, key);
                getCalibrationTable().setDoubleValue(OffsetE, "O_error",      1, 1, key);
            }            
            if(gtime.getVectorX().size()>10) {
                DataFitter.fit(ftime,gtime,"LQ");
                ftime.setOptStat("1111");
                double amp     = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(0);
                double ampE    = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(0).error();
                double lambda  = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(1);
                double lambdaE = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(1).error();
                double offset  = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(2);
                double offsetE = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(2).error();
                getCalibrationTable().setDoubleValue(amp,     "amp",          1, 1, key);
                getCalibrationTable().setDoubleValue(ampE,    "amp_error",    1, 1, key);
                getCalibrationTable().setDoubleValue(lambda,  "lambda",       1, 1, key);
                getCalibrationTable().setDoubleValue(lambdaE, "lambda_error", 1, 1, key);
                getCalibrationTable().setDoubleValue(offset,  "offset",       1, 1, key);
                getCalibrationTable().setDoubleValue(offsetE, "offset_error", 1, 1, key);
                this.getDataGroup().getItem(1, 1, key).getH1F("htwamp").fill(amp);
                this.getDataGroup().getItem(1, 1, key).getH1F("htwlambda").fill(lambda);
            }
        }
        getCalibrationTable().fireTableDataChanged();
     }

    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 5);
    }

    private void initTimeGaussFitPar(F1D ftime, H1F htime) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 2; //ns
        double rangeMin = (hMean - (0.8*hRMS)); 
        double rangeMax = (hMean + (0.2*hRMS));  
        double pm = (hMean*3.)/100.0;
        ftime.setRange(rangeMin, rangeMax);
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParLimits(1, hMean-pm, hMean+(pm));
        ftime.setParameter(2, 0.2);
        ftime.setParLimits(2, 0.1*hRMS, 0.8*hRMS);
    }    

    @Override
    public void drawDataGroup(int sector, int layer, int component) {
        if(this.getDataGroup().hasItem(sector,layer,component)==true){
            DataGroup dataGroup = this.getDataGroup().getItem(sector,layer,component);
            this.getCanvas().clear();
            this.getCanvas().divide(4,2);
            this.getCanvas().setGridX(false);
            this.getCanvas().setGridY(false);
            this.getCanvas().cd(0);
            this.getCanvas().draw(dataGroup.getH2F("htsum"));
            this.getCanvas().cd(1);
            this.getCanvas().draw(dataGroup.getGraph("gtsum"));
            this.getCanvas().draw(dataGroup.getF1D("ftglob"),"same");
            this.getCanvas().cd(2);
            this.getCanvas().draw(dataGroup.getH2F("htsum_calib"));
            this.getCanvas().cd(3);
            this.getCanvas().draw(dataGroup.getH1F("htwamp"));
            this.getCanvas().cd(4);
            this.getCanvas().draw(dataGroup.getH2F("htime_" + component));
            this.getCanvas().cd(5);
            this.getCanvas().draw(dataGroup.getGraph("gtime_" + component));
            this.getCanvas().draw(dataGroup.getF1D("ftglob"),"same");
            this.getCanvas().cd(6);
            this.getCanvas().draw(dataGroup.getH2F("htime_calib_" + component));
            this.getCanvas().cd(7);
            this.getCanvas().draw(dataGroup.getH1F("htwlambda"));
            this.getCanvas().getPad(0).getAxisZ().setLog(true);
            this.getCanvas().getPad(2).getAxisZ().setLog(true);
            this.getCanvas().getPad(3).getAxisY().setLog(true);
            this.getCanvas().getPad(4).getAxisZ().setLog(true);
            this.getCanvas().getPad(5).getAxisX().setRange(0, 200);
            this.getCanvas().getPad(6).getAxisZ().setLog(true);
            this.getCanvas().getPad(7).getAxisY().setLog(true);
        }
    }

    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
