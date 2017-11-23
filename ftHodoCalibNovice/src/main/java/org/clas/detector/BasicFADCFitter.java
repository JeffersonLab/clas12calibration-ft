/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clas.detector;

/**
 *
 * @author gavalian
 */
public class BasicFADCFitter  implements IFADCFitter {

    private int  pedistalMinBin = 0;
    private int  pedistalMaxBin = 0;
    private int  pulseMinBin    = 0;
    private int  pulseMaxBin    = 0;
    private int  threshold      = 0;
    
    public int     t0,adc,ped,pedsum;
    public int     thresholdCrossing,pulsePeakValue, pulsePeakPosition, pulseWidth, pulseLength;
    public double  baseline, rms;
    private int    tcourse, tfine;
    
    public BasicFADCFitter(){
        
    }
     
    public BasicFADCFitter(int pmin, int pmax, int amin, int amax){
        
    }

    public int getPedistalMinBin() {
        return pedistalMinBin;
    }

    public int getPedistalMaxBin() {
        return pedistalMaxBin;
    }

    public int getPulseMinBin() {
        return pulseMinBin;
    }

    public int getPulseMaxBin() {
        return pulseMaxBin;
    }
    
    public final BasicFADCFitter setPedestal(int pmin, int pmax){
        pedistalMinBin = pmin;
        pedistalMaxBin = pmax;
        return this;
    }
    
    public final BasicFADCFitter setPulse(int pmin, int pmax){
        pulseMinBin = pmin;
        pulseMaxBin = pmax;
        pulseLength = pmax - pmin +1;
        return this;
    }
    
    public final BasicFADCFitter setThreshold(int tet){
        threshold = tet;
        return this;
    }
    
    public void fit(int pedr, short[] pulse) {
            t0=0; adc=0; ped=0; pedsum=0; baseline=0; rms=0; 
            thresholdCrossing=0; pulsePeakValue=0; pulsePeakPosition=0; pulseWidth=0;
            tcourse=0; tfine=0;
            double noise  = 0;
            int    tstart = pedistalMaxBin+1;
            int    tcross = 0; 
            int    pmax   = 0;
            int    ppos   = 0;
            // calculate pedestal means and noise
            if (pedr!=0) ped=pedr;        // use default mode 7 pedestal range (1-4)
            if (pedr==0) {
                tstart = pulseMinBin;
                for (int bin = pedistalMinBin+1; bin < pedistalMaxBin+1; bin++) {
                    pedsum += pulse[bin];
                    noise  += pulse[bin] * pulse[bin];
                }
                baseline = ((double) pedsum)/ (pedistalMaxBin - pedistalMinBin);
                ped = pedsum/(pedistalMaxBin - pedistalMinBin);	//(int) baseline;
                rms = Math.sqrt(noise / (pedistalMaxBin - pedistalMinBin) - baseline * baseline);
            }
            // find threshold crossing
            for (int bin=Math.max(0,pulseMinBin); bin<pulse.length; bin++) {
                if(pulse[bin]>ped+threshold) {
                    tcross=bin;
                    thresholdCrossing=tcross;
                    break;
                }
            }
            // calculate integral and find maximum
            for (int bin=Math.max(0,pulseMinBin); bin<Math.min(pulse.length,pulseMaxBin+1); bin++) { // sum should be up to tcross+nsa (without +1), this was added to match the old fit method
                adc+=pulse[bin]-ped;
                if(pulse[bin]>pmax) {
                    pmax=pulse[bin];
                    ppos=bin;
                }
            }
            pulsePeakPosition=ppos;
            pulsePeakValue=pmax;
            // calculating mode 7 pulse time    
            double halfMax = (pmax+baseline)/2;
            int s0 = -1;
            int s1 = -1;
            for (int bin=Math.max(0,pulseMinBin); bin<Math.min(pulse.length-1,ppos+1); bin++) {
                if (pulse[bin]<=halfMax && pulse[bin+1]>halfMax) {
                    s0 = bin;
                    break;
                }
            }
            for (int bin=ppos; bin<Math.min(pulse.length,pulseMaxBin+1); bin++) {
                if (pulse[bin]>halfMax && pulse[bin+1]<=halfMax) {
                    s1 = bin;
                    break;
                }
            }
            if(s0>-1) { 
                int a0 = pulse[s0];
                int a1 = pulse[s0+1];
                // set course time to be the sample before the 50% crossing
                tcourse = s0;
                // set the fine time from interpolation between the two samples before and after the 50% crossing (6 bits resolution)
                tfine   = ((int) ((halfMax - a0)/(a1-a0) * 64));
                t0      = (tcourse << 6) + tfine;
            }
            if(s1>-1 && s0>-1) {
                int a0 = pulse[s1];
                int a1 = pulse[s1+1];
                pulseWidth  = s1 - s0;
            }
// System.out.println(ped + " " + pmax + " " + adc + " " + tcross + " " + ((float) tcourse+tfine/64.) + " " + ppos);
    }
    
    private double findPedestal(DetectorDataDgtz.ADCData data){
        int ped = 0;
        for(int i = this.pedistalMinBin; i < this.pedistalMaxBin; i++){
            ped += data.getPulseValue(i);
        }
        double ped_norm =   ((double) ped) / (Math.abs(this.pedistalMaxBin-this.pedistalMinBin)); 
        return ped_norm;
    }

    public void fit(DetectorDataDgtz.ADCData data) {
        if(data.getPulseSize()==0) {
            System.out.println("[SimpleFADCFitter] ---> there is no pulse in ADCData..");
            return;
        }
        
    }
    
}
