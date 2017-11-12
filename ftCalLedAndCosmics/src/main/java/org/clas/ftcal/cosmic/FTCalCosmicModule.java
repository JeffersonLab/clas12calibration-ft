/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.cosmic;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.clas.detector.DetectorDataDgtz;
import org.clas.ft.tools.FTAdjustFit;
import org.clas.ft.tools.FTCanvasBook;
import org.clas.ft.tools.FTDetector;
import org.clas.ft.tools.FTModule;
import org.clas.ft.tools.FTModuleType;
import org.clas.ft.tools.FTParameter;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public class FTCalCosmicModule extends FTModule {

    // Data Collections
    H1F hfADC          = null;
    H1F H_fADC_N       = null;
    H1F H_COSMIC_N     = null;
    H1F H_WMAX         = null;
    
    // analysis realted info
    int    cosmicMult  = 4;  // Horizonthal selection // number of crystals above threshold in a column for cosmics selection
    int    cosmicRange = 5;
    double singleChThr = 7;// Single channel selection
    
    public FTCalCosmicModule(FTDetector d)  {
        super(d);
        this.setName("Cosmics");
        this.setType(FTModuleType.EVENT_ACCUMULATE);
        this.addCanvases("Energy");
        this.addComparisonCanvas();
        this.addParameters("Occupancy", "<Q> (pC)", "\u03C3(Q) (pC)", "\u03C7\u00B2(Q)"/*, "<T>", "\u03C3(T)"*/);
        this.getParameter(0).setRanges(0.0,1000000.0,1.0,1000000.0, false);
        this.getParameter(1).setRanges(4.0,45.0,10.0,10.0, false);///Range Charge
        this.getParameter(2).setRanges(0.0,10.0,10.0,1.0, false);
        this.getParameter(3).setRanges(0.0,2.0,10.0,2.0, false);
//        this.getParameter(4).setRanges(0.0,50.0,10.0,50.0, false);
//        this.getParameter(5).setRanges(0.0,5.0,10.0,5.0, false);
        this.getDetector().setThresholds(this.singleChThr);
    }

    @Override
    public void adjustFit() {
        System.out.println("Adjusting fit for component " + this.getSelectedKey());
        H1F hcharge = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getH1F("Charge_" + this.getSelectedKey());
        F1D fcharge = this.getDataGroup().getItem(1,1,this.getSelectedKey()).getF1D("Landau_" + this.getSelectedKey());
        FTAdjustFit cfit = new FTAdjustFit(hcharge, fcharge, "LRQ");
        this.getCanvas("Energy").update();
    }
    
    @Override
    public void analyze() {
        for(int key : this.getDetector().getDetectorComponents()) {
            H1F hfadcn  = this.getDataGroup().getItem(1,1,key).getH1F("fADC");
            H1F hfadcr  = this.getDataGroup().getItem(1,1,key).getH1F("fADC_RAW_" + key);
            H1F hfadc   = this.getDataGroup().getItem(1,1,key).getH1F("fADC_" + key);
            H1F hcosmn  = this.getDataGroup().getItem(1,1,key).getH1F("EVENT");
            H1F hcosmr  = this.getDataGroup().getItem(1,1,key).getH1F("Cosmic_fADC_RAW_" + key);
            H1F hcosm   = this.getDataGroup().getItem(1,1,key).getH1F("Cosmic_fADC_" + key);
            for(int i=0; i<hfadc.getDataSize(0); i++) {
                if(hfadcn.getBinContent(key)>0) hfadc.setBinContent(i, hfadcr.getBinContent(i)/hfadcn.getBinContent(key));
                if(hcosmn.getBinContent(key)>0) hcosm.setBinContent(i, hcosmr.getBinContent(i)/hcosmn.getBinContent(key));
            }
            H1F hcharge = this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key);
            F1D fcharge = this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key);
            if(hcharge.getEntries()>100) {
                this.initLandauFitPar(hcharge, fcharge);
                DataFitter.fit(fcharge,hcharge,"LRQ");
            }
        }
    }
    
    @Override
    public IndexedList<DataGroup> createDataGroup() {
        H_fADC_N       = new H1F("fADC"      , this.getDetector().getComponentMaxCount()+1, 0, this.getDetector().getComponentMaxCount()+1);
        H_COSMIC_N     = new H1F("EVENT"     , this.getDetector().getComponentMaxCount()+1, 0, this.getDetector().getComponentMaxCount()+1);
        H_WMAX         = new H1F("WMAX"      , this.getDetector().getComponentMaxCount()+1, 0, this.getDetector().getComponentMaxCount()+1);
        IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>();
        for(int component : this.getDetector().getDetectorComponents()) {
            int ix = this.getDetector().getIdX(component);
            int iy = this.getDetector().getIdY(component);
            String title = "Crystal " + component + " (" + ix + "," + iy + ")";
            H1F H_fADC = new H1F("fADC_RAW_" + component, title, 100, 0.0, 100.0);
            H_fADC.setFillColor(5);
            H_fADC.setTitleX("fADC sample");
            H_fADC.setTitleY("fADC channels");   
            H_fADC.setOptStat(1);    
            H1F H_fADC_NORM = new H1F("fADC_" + component, title, 100, 0.0, 100.0);
            H_fADC_NORM.setFillColor(5);
            H_fADC_NORM.setTitleX("fADC sample");
            H_fADC_NORM.setTitleY("fADC channels");   
            H_fADC_NORM.setOptStat(1);    
             H1F H_COSMIC_fADC = new H1F("Cosmic_fADC_RAW_" + component, title, 100, 0.0, 100.0);
            H_COSMIC_fADC.setFillColor(3);
            H_COSMIC_fADC.setTitleX("fADC sample");
            H_COSMIC_fADC.setTitleY("fADC channels");   
            H_COSMIC_fADC.setOptStat(1); 
             H1F H_COSMIC_fADC_NORM = new H1F("Cosmic_fADC_" + component, title, 100, 0.0, 100.0);
            H_COSMIC_fADC_NORM.setFillColor(3);
            H_COSMIC_fADC_NORM.setTitleX("fADC sample");
            H_COSMIC_fADC_NORM.setTitleY("fADC channels");   
            H_COSMIC_fADC_NORM.setOptStat(1); 
            H1F H_COSMIC_CHARGE = new H1F("Charge_" + component, title, 96, -2.0, 30.0);
            H_COSMIC_CHARGE.setFillColor(2);
            H_COSMIC_CHARGE.setTitleX("Charge (pC)");
            H_COSMIC_CHARGE.setTitleY("Counts");   
            H_COSMIC_CHARGE.setOptStat(1111); 
            H1F H_COSMIC_VMAX = new H1F("Amplitude_" + component, title, 64, -2.0, 30.0);
            H_COSMIC_VMAX.setFillColor(2);
            H_COSMIC_VMAX.setTitleX("Amplitude (mV)");
            H_COSMIC_VMAX.setTitleY("Counts");   
            H_COSMIC_VMAX.setOptStat(1111); 
            F1D F_ChargeLandau = new F1D("Landau_" + component,"[amp]*landau(x,[mean],[sigma])+[e0]*exp(-x*[e1])", 0.0, 40.0);
            F_ChargeLandau.setParameter(0,0.0);
            F_ChargeLandau.setParameter(1,0.0);
            F_ChargeLandau.setParameter(2,0.0);
            F_ChargeLandau.setParameter(3,0.0);
            F_ChargeLandau.setParameter(4,0.0);
            F_ChargeLandau.setLineColor(4);  
            F_ChargeLandau.setOptStat(1111111); 
            F_ChargeLandau.setLineWidth(2);  
            DataGroup dg = new DataGroup(2, 4);
            dg.addDataSet(H_fADC_N,           0);
            dg.addDataSet(H_fADC,             0);
            dg.addDataSet(H_fADC_NORM,        0);
            dg.addDataSet(H_COSMIC_N,         1);
            dg.addDataSet(H_COSMIC_fADC,      1);
            dg.addDataSet(H_COSMIC_fADC_NORM, 1);
            dg.addDataSet(H_COSMIC_VMAX,      2);
            dg.addDataSet(H_COSMIC_CHARGE,    3);
            dg.addDataSet(F_ChargeLandau,     4);
            dataGroups.add(dg, 1, 1, component);
        }
        return dataGroups;
    }
    
    private void initLandauFitPar(H1F hcharge, F1D fcharge) {
        double mlMin=hcharge.getAxis().min();
        double mlMax=hcharge.getAxis().max();
        mlMin=2.0;
        F1D ff;
        if(hcharge.getBinContent(0)<20) {//Changed from 10
            fcharge.setParameter(0, hcharge.getBinContent(hcharge.getMaximumBin()));
        }
        else {
            fcharge.setParameter(0, 20);//Changed from 10
        }
        fcharge.setParLimits(0, 0.0, 10000000.0); 
        fcharge.setParameter(1,hcharge.getMean());
        fcharge.setParLimits(1, 4.0, 30.0);//Changed from 5-30        
        fcharge.setParameter(2,1.6);//Changed from 2
        fcharge.setParLimits(2, 0.3, 5);//Changed from 0.5-10
//        if(hcharge.getBinContent(1)==0){
//            fcharge.setParLimits(3, 0, 0);
//            fcharge.setParLimits(4, 0, 0);
//        }
//        else {
            fcharge.setParameter(3,hcharge.getBinContent(5));
            fcharge.setParLimits(3,  0.0, 10000000.); 
            fcharge.setParameter(4, -0.3);//Changed from -0.2
            fcharge.setParLimits(4,  0.0, 3.0); //Changed from -10-0
//        }
        
    }
    
    public Boolean isLargeEvent() {
        Boolean flag = false;
        int nCrystalInEvent = 0;
        for (int component : this.getDetector().getDetectorComponents()) {
            if (H_WMAX.getBinContent(component) > 1000.0) {
                nCrystalInEvent++;
            }
        }
        if (nCrystalInEvent > 200) {
            flag = true;
        }
        return flag;
    }

    @Override
    public void plotDataGroup() {
        EmbeddedCanvas canvas = this.getCanvases().get("Energy");
        canvas.divide(2, 2);
        canvas.setGridX(false);
        canvas.setGridY(false);
        int key = this.getSelectedKey();
        if(this.getDetector().hasComponent(key)) {
            canvas.cd(0);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("fADC_" + key));
            canvas.cd(1);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Cosmic_fADC_" + key));
            canvas.cd(2);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Amplitude_" + key));
            canvas.cd(3);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key));
//            canvas.draw(this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key),"same");
        }
    }
    
    @Override
    public void plotComparison() {
        EmbeddedCanvas canvas = this.getCanvases().get("Comparison");
        canvas.divide(2, 2);
        canvas.setGridX(false);
        canvas.setGridY(false);
        int key = this.getSelectedKey();
        if(this.getDetector().hasComponent(key)) {
            canvas.cd(0);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Amplitude_" + key));
            canvas.cd(1);
            canvas.draw(this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key));
            canvas.cd(2);
            canvas.draw(this.getComparisonDataGroup().getItem(1,1,key).getH1F("Amplitude_" + key));
            canvas.cd(3);
            canvas.draw(this.getComparisonDataGroup().getItem(1,1,key).getH1F("Charge_" + key));
        }
    }

    @Override
    public void processEvent(List<DetectorDataDgtz> counters) {
        
        H_WMAX.reset();
        double tPMTCross=0;
        double tPMTHalf=0;
        for(DetectorDataDgtz counter : counters) {
            int key = counter.getDescriptor().getComponent();
            if(this.getDetector().hasComponent(key)) {
                H1F hfadc  = this.getDataGroup().getItem(1,1,key).getH1F("fADC_RAW_" + key);
                this.getDataGroup().getItem(1,1,key).getH1F("fADC").fill(key);
                short pulse[] = counter.getADCData(0).getPulseArray();
                for (int i = 0; i < Math.min(pulse.length, hfadc.getAxis().getNBins()); i++) {
                    hfadc.fill(i, pulse[i] - counter.getADCData(0).getPedestal() + 10.0);
                }
                H_WMAX.fill(key,counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal());

            }
        }
//        System.out.println("new event");
        for(DetectorDataDgtz counter : counters) {
            int key = counter.getDescriptor().getComponent();
            if(this.getDetector().hasComponent(key)) {
                if(this.selectCosmics(key) && !this.isLargeEvent()){
                    H1F hcfadc  = this.getDataGroup().getItem(1,1,key).getH1F("Cosmic_fADC_RAW_" + key);
                    H1F hcharge = this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key);
                    H1F hampli  = this.getDataGroup().getItem(1,1,key).getH1F("Amplitude_" + key);
                    this.getDataGroup().getItem(1,1,key).getH1F("EVENT").fill(key);
                    short pulse[] = counter.getADCData(0).getPulseArray();
                    for (int i = 0; i < Math.min(pulse.length, hcfadc.getAxis().getNBins()); i++) {
                         hcfadc.fill(i, pulse[i] - counter.getADCData(0).getPedestal() + 10.0);
                    }
                    double charge = counter.getADCData(0).getADC()*LSB*nsPerSample/50;
                    hcharge.fill(charge);
                    hampli.fill((counter.getADCData(0).getHeight()-counter.getADCData(0).getPedestal())*LSB);     
                }  
            }
         }
    }

    @Override
    public double getParameterValue(String parameterName, int key) {
        double value = -1;
        FTParameter par = this.getParameter(parameterName);
        if(par != null) {
            switch (parameterName) {
                case "Occupancy":
                {
                    if(this.getNumberOfEvents()>0) {
                        this.getParameter("Occupancy").setLimit(this.getNumberOfEvents()/15);
                    }
                    else {
                        this.getParameter("Occupancy").setLimit(20000);
                    }
                    value = this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key).getIntegral();
                    break;
                }
                case "<Q> (pC)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key).getParameter(1);
                    break;
                }
                case "\u03C3(Q) (pC)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key).parameter(1).error();
                    break;
                }
                case "\u03C7\u00B2(Q)":
                {
                    value = this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key).getChiSquare()/
                            this.getDataGroup().getItem(1,1,key).getF1D("Landau_" + key).getNDF();
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
    
    @Override
    public void setAnalysisParameters() {
	JTextField multiplicity = new JTextField(5);
	JTextField range        = new JTextField(5);
	JTextField threshold    = new JTextField(5);
	
        
	JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,2));            
           
        panel.add(new JLabel("Cosmic multiplicity"));
        multiplicity.setText(Integer.toString(this.cosmicMult));
        panel.add(multiplicity);
        panel.add(new JLabel("Column range"));
        range.setText(Integer.toString(this.cosmicRange));
        panel.add(range);
        panel.add(new JLabel("Threshold"));
        threshold.setText(Double.toString(this.singleChThr));
        panel.add(threshold);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Analysis parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {        
            if (!multiplicity.getText().isEmpty()) {
                this.cosmicMult = Integer.parseInt(multiplicity.getText());
            } 
            if (!range.getText().isEmpty()) {
                this.cosmicRange = Integer.parseInt(range.getText());
            } 
            if (!threshold.getText().isEmpty()) {
                this.singleChThr = Double.parseDouble(threshold.getText());
            } 
            System.out.println("Cosmic analysis paramters");
            System.out.println("\n\tCrystal multiplicity for cosmic ray selection: " + this.cosmicMult);
            System.out.println("\n\tRange of crystals in a column: +/-" + this.cosmicRange);
            System.out.println("\n\tSingle channel threshold: " + this.singleChThr);
        }
    }

    public Boolean selectCosmics(int key) {
        //System.out.println("HorizonthalSel "+singleChThr);
        Boolean flag = false;
        int nCrystalInColumn = 0;
        int ix = this.getDetector().getIX(key);
        int iy = this.getDetector().getIY(key);
//        int i1 = Math.max(0, iy - this.cosmicRange);    // allowing for +/- to cope with dead channels
//        int i2 = iy + cosmicRange;
//        for (int i = i1; i<= i2; i++) {
//            int component = this.getDetector().getComponent(ix, i);
//            if (i != iy && this.getDetector().hasComponent(component)) {
//                if (H_WMAX.getBinContent(component) > this.singleChThr) {
//                    nCrystalInColumn++;
//                }
//            }
//        }
        int ntest = 0;
        for (int i = 1; i<=this.cosmicRange; i++) {
            if(ntest<=this.cosmicRange) {
                int component = this.getDetector().getComponent(ix, iy + i);
                if (this.getDetector().hasComponent(component) ) {
                    ntest++;
                    if (H_WMAX.getBinContent(component) > this.singleChThr) nCrystalInColumn++;               
                }
                component = this.getDetector().getComponent(ix, iy - i);
                if (this.getDetector().hasComponent(component) && (iy-1)>=0) {
                    ntest++;
                    if (H_WMAX.getBinContent(component) > this.singleChThr) nCrystalInColumn++;               
                }
            }
        }
        if (nCrystalInColumn >= cosmicMult) {
            flag = true;
        }
        return flag;
    }
    
    @Override
    public void setFunctionStyle() {
        for(int key : this.getDetector().getDetectorComponents()) {
            H1F hcharge = null;
            hcharge = this.getDataGroup().getItem(1,1,key).getH1F("Charge_" + key);
            if(hcharge.getFunction()!= null) {
                hcharge.getFunction().setLineColor(4);
                hcharge.getFunction().setLineWidth(2);
                hcharge.getFunction().setOptStat(1111111);
            }
            hcharge = this.getComparisonDataGroup().getItem(1,1,key).getH1F("Charge_" + key);
            if(hcharge.getFunction()!= null) {
                hcharge.getFunction().setLineColor(4);
                hcharge.getFunction().setLineWidth(2);
                hcharge.getFunction().setOptStat(1111111);
            }
        }
    }
    
    @Override
    public void showPlots() {
        FTCanvasBook canvasGroup = new FTCanvasBook();
        canvasGroup.setData(this.getDataGroup(), 3);
        JFrame frame = new JFrame(this.getName());
        frame.setSize(1000, 800);        
        frame.add(canvasGroup);
        // frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    

}
