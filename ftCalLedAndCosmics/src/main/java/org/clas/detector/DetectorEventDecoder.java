/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clas.detector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.clas.detector.DetectorDataDgtz.ADCData;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class DetectorEventDecoder {
    
    ConstantsManager  translationManager = new ConstantsManager();
    ConstantsManager  fitterManager      = new ConstantsManager();
    
    FadcControlPanel fadcPanel           = new FadcControlPanel();

    List<String>  tablesTrans            = null;
    List<String>  keysTrans              = null;
    
    List<String>  tablesFitter            = null;
    List<String>  keysFitter              = null;
    
    private  int  runNumber               = 10;
    
    private  BasicFADCFitter      basicFitter     = new BasicFADCFitter();
    private  ExtendedFADCFitter   extendedFitter  = new ExtendedFADCFitter();
    
    private  Boolean          useExtendedFitter   = false;

    
    public DetectorEventDecoder(boolean development){
        if(development==true){
            this.initDecoderDev();
        } else {
            this.initDecoder();
        }
    }
    
    public void setRunNumber(int run){
        this.runNumber = run;
    }
    
    public DetectorEventDecoder(){
        this.initDecoder();
        /*
        keysTrans = Arrays.asList(new String[]{
            "FTCAL","FTHODO","LTCC","ECAL","FTOF","HTCC","DC"
        });
        
        tablesTrans = Arrays.asList(new String[]{
            "/daq/tt/ftcal","/daq/tt/fthodo","/daq/tt/ltcc",
            "/daq/tt/ec","/daq/tt/ftof","/daq/tt/htcc","/daq/tt/dc"
        });
        
        translationManager.init(keysTrans,tablesTrans);
        
        keysFitter   = Arrays.asList(new String[]{"FTCAL","FTOF","LTCC","ECAL","HTCC"});
        tablesFitter = Arrays.asList(new String[]{
            "/daq/fadc/ftcal","/daq/fadc/ftof","/daq/fadc/ltcc","/daq/fadc/ec",
            "/daq/fadc/htcc"
        });
        fitterManager.init(keysFitter, tablesFitter);
        */
    }
    
    public FadcControlPanel getFadcPanel() {
        return fadcPanel;
    }

    public final void initDecoderDev(){
        keysTrans = Arrays.asList(new String[]{ "HTCC","BST"} );
        tablesTrans = Arrays.asList(new String[]{ "/daq/tt/clasdev/htcc","/daq/tt/clasdev/svt" });
        
        keysFitter   = Arrays.asList(new String[]{"HTCC"});
        tablesFitter = Arrays.asList(new String[]{"/daq/fadc/clasdev/htcc"});
        translationManager.init(keysTrans,tablesTrans);
        fitterManager.init(keysFitter, tablesFitter);
    }
    
    public final void initDecoder(){
        keysTrans = Arrays.asList(new String[]{
		"FTCAL","FTHODO","LTCC","EC","FTOF","HTCC","DC","CTOF","SVT","RF","BMT","FMT"
        });
        
        tablesTrans = Arrays.asList(new String[]{
            "/daq/tt/ftcal","/daq/tt/fthodo","/daq/tt/ltcc",
            "/daq/tt/ec","/daq/tt/ftof","/daq/tt/htcc","/daq/tt/dc","/daq/tt/ctof","/daq/tt/svt",
            "/daq/tt/rf","/daq/tt/bmt","/daq/tt/fmt"
        });
        
        translationManager.init(keysTrans,tablesTrans);
        
        keysFitter   = Arrays.asList(new String[]{"FTCAL","FTOF","LTCC","EC","HTCC","CTOF"});
        tablesFitter = Arrays.asList(new String[]{
            "/daq/fadc/ftcal","/daq/fadc/ftof","/daq/fadc/ltcc","/daq/fadc/ec",
            "/daq/fadc/htcc","/daq/fadc/ctof"
        });
        fitterManager.init(keysFitter, tablesFitter);
    }
    /**
     * Set the flag to use extended fitter instead of basic fitter
     * which simply integrates over given bins inside of the given
     * windows for the pulse. The pulse parameters are provided by 
     * fitterManager (loaded from database).
     * @param flag 
     */
    public void setUseExtendedFitter(boolean flag){
        this.useExtendedFitter = flag;
    }
   
    // set fadc integration parameters for mode 7 emulation
    public void setBasicFitterParameters(int ped1, int ped2, int pul1, int pul2, int tet) {
        this.basicFitter.setPedestal(ped1, ped2);
        this.basicFitter.setPulse(pul1, pul2);
        this.basicFitter.setThreshold(tet);
    }
    
    /**
     * applies translation table to the digitized data to translate
     * crate,slot channel to sector layer component.
     * @param detectorData 
     */
    public void translate(List<DetectorDataDgtz>  detectorData){
        
        for(DetectorDataDgtz data : detectorData){
            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();
            //if(crate==69){
	    //System.out.println(" MVT " + crate + " " + slot + 
	    //  "  " + channel);
	// }
            boolean hasBeenAssigned = false;
            
            for(String table : keysTrans){
                IndexedTable  tt = translationManager.getConstants(runNumber, table);
                DetectorType  type = DetectorType.getType(table);
	
                if(tt.hasEntry(crate,slot,channel)==true){
                    int sector    = tt.getIntValue("sector", crate,slot,channel);
                    int layer     = tt.getIntValue("layer", crate,slot,channel);
                    int component = tt.getIntValue("component", crate,slot,channel);
                    int order     = tt.getIntValue("order", crate,slot,channel);
                    
                    /*if(crate>60&&crate<64){
                        System.out.println(" SVT " + sector + " " + layer + 
                                "  " + component);
                    }*/
                    data.getDescriptor().setSectorLayerComponent(sector, layer, component);
                    data.getDescriptor().setOrder(order);
                    data.getDescriptor().setType(type);
                    for(int i = 0; i < data.getADCSize(); i++) {
                        data.getADCData(i).setOrder(order);
                    }
                    for(int i = 0; i < data.getTDCSize(); i++) {
                        data.getTDCData(i).setOrder(order);
                    }
                }
            }
        }
        //Collections.sort(detectorData);
    }
    
    public void fitPulses(List<DetectorDataDgtz>  detectorData){
        
        int nsa = this.fadcPanel.nsa;
        int nsb = this.fadcPanel.nsb;
        int tet = this.fadcPanel.tet;
        for(DetectorDataDgtz data : detectorData){            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();
            //System.out.println(" looking for " + crate + "  " 
            //       + slot + " " + channel);
            for(String table : keysFitter){
                IndexedTable  daq = fitterManager.getConstants(runNumber, table);
                DetectorType  type = DetectorType.getType(table);
                if(daq.hasEntry(crate,slot,channel)==true){                    
                    if(this.fadcPanel.useCCDB) {
                        nsa = daq.getIntValue("nsa", crate,slot,channel);
                        nsb = daq.getIntValue("nsb", crate,slot,channel);
                        tet = daq.getIntValue("tet", crate,slot,channel);
                    }
                    if(data.getADCSize()>0){
                        for(int i = 0; i < data.getADCSize(); i++){
                            ADCData adc = data.getADCData(i);
                            if(adc.getPulseSize()>0){
                                //System.out.println("-----");
                                //System.out.println(" FITTING PULSE " + 
                                //        crate + " / " + slot + " / " + channel);
                                try {
                                    if(this.fadcPanel.useMode7) { 
                                        extendedFitter.fit(nsa, nsb, tet, 0, adc.getPulseArray());
                                        int adc_corrected = extendedFitter.adc + extendedFitter.ped*(nsa+nsb);
                                        adc.setIntegral(adc_corrected);
                                        adc.setTimeWord(this.extendedFitter.t0);
                                        adc.setPedestal((short) this.extendedFitter.ped); 
                                        adc.setPedistalMaxBin(this.extendedFitter.pedistalMaxBin);
                                        adc.setPedistalMinBin(this.extendedFitter.pedistalMinBin);
                                        adc.setHeight((short) this.extendedFitter.pulsePeakValue);
                                        adc.setPosition(this.extendedFitter.pulsePeakPosition);
                                        adc.setPulseMaxBin(this.extendedFitter.pulseMaxBin);
                                        adc.setPulseMinBin(this.extendedFitter.pulseMinBin);
                                        adc.setThresholdCrossing(this.extendedFitter.thresholdCrossing);
                                        adc.setRMS(this.extendedFitter.rms);
                                        adc.setFWHM(this.extendedFitter.pulseWidth);
                                    }
                                    else {
                                        this.setBasicFitterParameters(this.fadcPanel.ped1, this.fadcPanel.ped2, this.fadcPanel.pul1, this.fadcPanel.pul2, this.fadcPanel.tet);                    
                                        basicFitter.fit(0, adc.getPulseArray());
                                        int adc_corrected = basicFitter.adc + basicFitter.ped*(basicFitter.pulseLength);
                                        adc.setIntegral(adc_corrected);
                                        adc.setTimeWord(this.basicFitter.t0);
                                        adc.setPedestal((short) this.basicFitter.ped); 
                                        adc.setPedistalMaxBin(this.basicFitter.getPedistalMaxBin());
                                        adc.setPedistalMinBin(this.basicFitter.getPedistalMinBin());
                                        adc.setHeight((short) this.basicFitter.pulsePeakValue);
                                        adc.setPosition(this.basicFitter.pulsePeakPosition);
                                        adc.setPulseMaxBin(this.basicFitter.getPulseMaxBin());
                                        adc.setPulseMinBin(this.basicFitter.getPulseMinBin());
                                        adc.setThresholdCrossing(this.basicFitter.thresholdCrossing);
                                        adc.setRMS(this.basicFitter.rms);
                                        adc.setFWHM(this.basicFitter.pulseWidth);
                                        adc.setADC(0, basicFitter.pulseLength);
                                    }
                                } catch (Exception e) {
                                    System.out.println(">>>> error : fitting pulse "
                                    +  crate + " / " + slot + " / " + channel);
                                }
                                //System.out.println(" FIT RESULT = " + extendedFitter.adc + " / "
                                //        + this.extendedFitter.t0 + " / " + this.extendedFitter.ped);
                            }
                        }
                    }
                    //System.out.println(" apply nsa nsb " + nsa + " " + nsb);
                    if(data.getADCSize()>0){
                        for(int i = 0; i < data.getADCSize(); i++){
                            if(this.fadcPanel.useMode7) data.getADCData(i).setADC(nsa, nsb);
                        }
                    }
                }
            }
        }
    }
}
