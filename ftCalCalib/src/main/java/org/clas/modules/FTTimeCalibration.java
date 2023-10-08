package org.clas.modules;

import org.clas.ftdata.FTCalCluster;
import org.clas.ftdata.FTCalEvent;
import org.clas.ftdata.FTCalHit;
import java.util.Map;
import org.clas.view.DetectorShape2D;
import org.clas.viewer.FTCalibrationModule;
import org.clas.viewer.FTDetector;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.groot.data.H2F;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class FTTimeCalibration extends FTCalibrationModule {   
    

    
    public FTTimeCalibration(FTDetector d, String name, ConstantsManager ccdb, Map<String,CalibrationConstants> gConstants) {
        super(d, name, "offset:offset_error:delta:resolution",3, ccdb, gConstants);
        this.setCCDBTable("/calibration/ft/ftcal/time_offsets");
        this.getCalibrationTable().addConstraint(5, -0.1, 0.1);
        this.setRange(30.,40.);
//        this.setRange(-10.,0.);
        this.setCols(-2, 2);
    }

    @Override
    public void resetEventListener() {

        H1F htsum = new H1F("htsum", 275, -450.0, 100.0);
        htsum.setTitleX("Time Offset (ns)");
        htsum.setTitleY("Counts");
        htsum.setTitle("Global Time Offset");
        htsum.setFillColor(3);
        H1F htsum_calib = new H1F("htsum_calib", 300, -6.0, 6.0);
        htsum_calib.setTitleX("Time Offset (ns)");
        htsum_calib.setTitleY("Counts");
        htsum_calib.setTitle("Global Time Offset");
        htsum_calib.setFillColor(44);
        H1F htsum_cluster0 = new H1F("htsum_cluster0" , 400, -2., 2.);
        htsum_cluster0.setTitleX("Time (ns)");
        htsum_cluster0.setTitleY("Counts");
        htsum_cluster0.setTitle("Cluster Time");
        htsum_cluster0.setFillColor(44);
        htsum_cluster0.setLineColor(1);
        H1F htsum_cluster = new H1F("htsum_cluster" , 400, -2., 2.);
        htsum_cluster.setTitleX("Time (ns)");
        htsum_cluster.setTitleY("Counts");
        htsum_cluster.setTitle("Cluster Time");
        htsum_cluster.setFillColor(3);
        htsum_cluster.setLineColor(1);
        F1D fsum_cluster = new F1D("fsum_cluster", "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
        fsum_cluster.setParameter(0, 0.0);
        fsum_cluster.setParameter(1, 0.0);
        fsum_cluster.setParameter(2, 2.0);
        fsum_cluster.setLineWidth(2);
        fsum_cluster.setOptStat("1111");
        H2F h2d_cluster = new H2F("h2d_cluster" , 100, 0., 12., 100, -2., 2.);
        h2d_cluster.setTitleX("Energy (GeV)");
        h2d_cluster.setTitleY("Time (ns)");
        h2d_cluster.setTitle("Cluster Time");
        GraphErrors  gtoffsets = new GraphErrors("gtoffsets");
        gtoffsets.setTitle("Timing Offsets"); //  title
        gtoffsets.setTitleX("Crystal ID"); // X axis title
        gtoffsets.setTitleY("Offset (ns)");   // Y axis title
        gtoffsets.setMarkerColor(5); // color from 0-9 for given palette
        gtoffsets.setMarkerSize(2);  // size in points on the screen
        gtoffsets.addPoint(0., 0., 0., 0.);
        gtoffsets.addPoint(1., 1., 0., 0.);
        GraphErrors  gtdeltas = new GraphErrors("gtdeltas");
        gtdeltas.setTitle("#Delta Offset (ns)"); //  title
        gtdeltas.setTitleX("Crystal ID"); // X axis title
        gtdeltas.setTitleY("#Delta Offset (ns)");   // Y axis title
        gtdeltas.setMarkerColor(3); // color from 0-9 for given palette
        gtdeltas.setMarkerSize(2);  // size in points on the screen
        gtdeltas.addPoint(0., 0., 0., 0.);
        gtdeltas.addPoint(1., 1., 0., 0.);
        H1F htoffsets = new H1F("htoffsets", 100, -2, 2);
        htoffsets.setTitle("Hit Time");
        htoffsets.setTitleX("#DeltaT (ns)");
        htoffsets.setTitleY("Counts");
        htoffsets.setFillColor(23);
        htoffsets.setLineColor(3);
        htoffsets.setOptStat("1111");

        for (int key : this.getDetector().getDetectorComponents()) {
            // initialize data group
            H1F htime_wide = new H1F("htime_wide_" + key, 275, -400.0, 150.0);
            htime_wide.setTitleX("Time (ns)");
            htime_wide.setTitleY("Counts");
            htime_wide.setTitle("Component " + key);
            H1F htime = new H1F("htime_" + key, 250, this.getRange()[0], this.getRange()[1]);
            htime.setTitleX("Time (ns)");
            htime.setTitleY("Counts");
            htime.setTitle("Component " + key);
            H1F htime_calib = new H1F("htime_calib_" + key, 100, -4., 4.);
            htime_calib.setTitleX("Time (ns)");
            htime_calib.setTitleY("Counts");
            htime_calib.setTitle("Component " + key);
            htime_calib.setFillColor(44);
            htime_calib.setLineColor(24);
            F1D ftime = new F1D("ftime_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            ftime.setParameter(0, 0.0);
            ftime.setParameter(1, 0.0);
            ftime.setParameter(2, 2.0);
            ftime.setLineColor(24);
            ftime.setLineWidth(2);
            ftime.setOptStat("1111");
            F1D ftime_calib = new F1D("ftime_calib_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            ftime_calib.setParameter(0, 0.0);
            ftime_calib.setParameter(1, 0.0);
            ftime_calib.setParameter(2, 2.0);
            ftime_calib.setLineColor(24);
            ftime_calib.setLineWidth(2);
            ftime_calib.setOptStat("1111");
//            ftime.setLineColor(2);
//            ftime.setLineStyle(1);
            H1F htcluster = new H1F("htcluster_" + key, 100, -2., 2.);
            htcluster.setTitleX("Time (ns)");
            htcluster.setTitleY("Counts");
            htcluster.setTitle("Cluster Time");
            htcluster.setFillColor(3);
            htcluster.setLineColor(1);
            F1D fcluster = new F1D("fcluster_" + key, "[amp]*gaus(x,[mean],[sigma])", -1., 1.);
            fcluster.setParameter(0, 0.0);
            fcluster.setParameter(1, 0.0);
            fcluster.setParameter(2, 2.0);
            fcluster.setLineWidth(2);
            fcluster.setOptStat("1111");
            DataGroup dg = new DataGroup(4, 2);
            dg.addDataSet(htsum,         0);
            dg.addDataSet(htsum_calib,   1);
            dg.addDataSet(htoffsets,     2);
            dg.addDataSet(htsum_cluster0,3);
            dg.addDataSet(htsum_cluster, 3);
            dg.addDataSet(fsum_cluster,  3);
//            dg.addDataSet(htime_wide,    4);
            dg.addDataSet(htime,         4);
            dg.addDataSet(ftime,         4);
            dg.addDataSet(htime_calib,   5);
            dg.addDataSet(ftime_calib,   5);
//            dg.addDataSet(gtoffsets,     6);
            dg.addDataSet(htcluster,     6);
            dg.addDataSet(fcluster,      6);
            dg.addDataSet(h2d_cluster,   7);
            this.getDataGroup().add(dg, 1, 1, key);

        }
    }


    @Override
    public int getNEvents(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        return (int) this.getDataGroup().getItem(sector,layer,key).getH1F("htime_" + key).getIntegral();
    }

    @Override
    public void loadConstants(IndexedTable table) {
        for(int i=0; i<table.getRowCount(); i++) {
            int sector = Integer.valueOf((String)table.getValueAt(i, 0));
            int layer  = Integer.valueOf((String)table.getValueAt(i, 1));
            int comp   = Integer.valueOf((String)table.getValueAt(i, 2));
            double offset     = table.getDoubleValue("time_offset", sector, layer, comp);
            double resolution = table.getDoubleValue("time_rms",    sector, layer, comp);
            this.getPreviousCalibrationTable().addEntry(sector, layer, comp);
            for(int j=3; j<this.getPreviousCalibrationTable().getColumnCount(); j++) {
                this.getPreviousCalibrationTable().setDoubleValue(0.0, this.getPreviousCalibrationTable().getColumnName(j), sector, layer, comp);
            }                
            this.getPreviousCalibrationTable().setDoubleValue(offset,     "offset",     sector, layer, comp);
            this.getPreviousCalibrationTable().setDoubleValue(resolution, "resolution", sector, layer, comp);
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
                
                if(c.energy(true)>0.5 && c.energyR(true)>0.3 && c.size() > 3 && c.charge()==0) {                            

                    for(FTCalHit h : c) {
                        
                        int component = h.component();

                        double offset = 0;
                        if(this.getPreviousCalibrationTable().hasEntry(1,1,component)) {
                            offset = this.getPreviousCalibrationTable().getDoubleValue("offset", 1, 1, component);
                        }

                        double time0 = h.getZeroOffsetTime(false) - event.getStartTime() - h.path()/PhysicsConstants.speedOfLight();
                        double timec = h.getZeroOffsetTime(true)  - event.getStartTime() - h.path()/PhysicsConstants.speedOfLight();
                        this.getDataGroup().getItem(1,1,component).getH1F("htsum").fill(time0);
                        this.getDataGroup().getItem(1,1,component).getH1F("htime_"+component).fill(time0);
                        this.getDataGroup().getItem(1,1,component).getH1F("htsum_calib").fill(timec-offset);
                        this.getDataGroup().getItem(1,1,component).getH1F("htime_calib_"+component).fill(timec-offset); 
                    }
                    if(theta>2.5 && theta<4.5) {
                        this.getDataGroup().getItem(1,1,c.seed()).getH1F("htsum_cluster0").fill(c.vertexTime(false)-event.getStartTime());
                        this.getDataGroup().getItem(1,1,c.seed()).getH1F("htsum_cluster").fill(c.vertexTime(true)-event.getStartTime());
                        this.getDataGroup().getItem(1,1,c.seed()).getH2F("h2d_cluster").fill(c.energy(true),c.vertexTime(true)-event.getStartTime());
                        this.getDataGroup().getItem(1,1,c.seed()).getH1F("htcluster_"+c.seed()).fill(c.vertexTime(true)-event.getStartTime());                                
                  //                System.out.println(time + " " + clusterTime);
                    }
                }
            }
        }
    }

    @Override
    public void analyze() {
//        System.out.println("Analyzing");
        H1F htime = this.getDataGroup().getItem(1,1,8).getH1F("htsum_cluster");
        F1D ftime = this.getDataGroup().getItem(1,1,8).getF1D("fsum_cluster");
        this.gaussFit(ftime,htime, 2.0, false);

        for (int key : this.getDetector().getDetectorComponents()) {
            this.getDataGroup().getItem(1,1,key).getH1F("htoffsets").reset();
        }
        for (int key : this.getDetector().getDetectorComponents()) {
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_" + key);
            this.gaussFit(ftime,htime, 1.5, true);
            
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htime_calib_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("ftime_calib_" + key);
            this.gaussFit(ftime,htime, 1.5, true);
            
            double hoffset = ftime.getParameter(1);
            if(Math.abs(hoffset)<2) this.getDataGroup().getItem(1,1,key).getH1F("htoffsets").fill(hoffset);
            
            htime = this.getDataGroup().getItem(1,1,key).getH1F("htcluster_" + key);
            ftime = this.getDataGroup().getItem(1,1,key).getF1D("fcluster_" + key);
            this.gaussFit(ftime,htime, 2.0, false);
            
            double finalOffset = this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).getParameter(1);
            if(this.getDataGroup().getItem(1, 1, key).getF1D("fcluster_" + key).getParameter(0)>8) {
                finalOffset = finalOffset
                            + this.getDataGroup().getItem(1, 1, key).getF1D("fcluster_" + key).getParameter(1)
                            - this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1);
            }    
            getCalibrationTable().setDoubleValue(finalOffset, "offset", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_" + key).parameter(1).error(), "offset_error", 1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1), "delta" ,  1, 1, key);
            getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(2), "resolution" ,  1, 1, key);
        }
        getCalibrationTable().fireTableDataChanged();
    }

    @Override
    public void setCanvasBookData() {
        int[] pads = {4,5,6};
        this.getCanvasBook().setData(this.getDataGroup(), pads);   
    }

    private void gaussFit(F1D ftime, H1F htime, double range, boolean limits) {
        double hAmp  = htime.getBinContent(htime.getMaximumBin());
        double hMean = htime.getAxis().getBinCenter(htime.getMaximumBin());
        double hRMS  = 0.4; //ns
        ftime.setParameter(0, hAmp);
        ftime.setParLimits(0, hAmp*0.8, hAmp*1.2);
        ftime.setParameter(1, hMean);
        ftime.setParameter(2, hRMS/2);
        if(limits) {
            ftime.setParLimits(1, hMean-hRMS/2, hMean+hRMS/2);
        }
        for(int i=0; i<5; i++) {
            double rangeMin = (ftime.getParameter(1) - range*ftime.getParameter(2)); 
            double rangeMax = (ftime.getParameter(1) + range*ftime.getParameter(2));  
            ftime.setRange(rangeMin, rangeMax);
            DataFitter.fit(ftime,htime,"LQ");
        }
    }    

    @Override
    public double getValue(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        int key = dsd.getDescriptor().getComponent();
        if (this.getDetector().hasComponent(key)) {
            return this.getDataGroup().getItem(1, 1, key).getF1D("ftime_calib_" + key).getParameter(1);
       }
        return 0;
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
            this.getCanvas().draw(dataGroup.getH1F("htsum"));
            this.getCanvas().cd(1);
            this.getCanvas().draw(dataGroup.getH1F("htsum_calib"));
            this.getCanvas().cd(2);
//            this.getCanvas().draw(dataGroup.getGraph("gtoffsets"));
            this.getCanvas().draw(dataGroup.getH1F("htoffsets"));
            this.getCanvas().getPad(2).getAxisY().setLog(true);
            this.getCanvas().cd(3);
            this.getCanvas().draw(dataGroup.getH1F("htsum_cluster0"));
            this.getCanvas().draw(dataGroup.getH1F("htsum_cluster"), "same");
//            this.getCanvas().cd(4);
//            this.getCanvas().draw(dataGroup.getH1F("htime_wide_" + component));
            this.getCanvas().cd(4);
            this.getCanvas().draw(dataGroup.getH1F("htime_" + component));
            this.getCanvas().cd(5);
            this.getCanvas().draw(dataGroup.getH1F("htime_calib_" + component));
            this.getCanvas().cd(6);
//            this.getCanvas().draw(dataGroup.getGraph("gtoffsets"));
            this.getCanvas().draw(dataGroup.getH1F("htcluster_" + component));
            this.getCanvas().cd(7);
            this.getCanvas().getPad(7).getAxisZ().setLog(true);
            this.getCanvas().draw(dataGroup.getH2F("h2d_cluster"));
        }
    }

        
    @Override
    public void timerUpdate() {
        this.analyze();
    }
}
