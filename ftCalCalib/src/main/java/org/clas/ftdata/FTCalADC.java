package org.clas.ftdata;

import org.clas.viewer.FTCalConstants;

/**
 *
 * @author devita
 */
public class FTCalADC {

    private int    component;
    private int    ADC;
    private double charge;
    private double time;
    private int    pedestal;
    private FTCalTrue trueInfo;
    
    public FTCalADC(int component, int ADC, double time, int pedestal) {
        this.component = component;
        this.ADC = ADC;
        this.time = time;
        this.pedestal = pedestal;
        this.charge = this.ADC*(FTCalConstants.LSB*FTCalConstants.NSPERSAMPLE/50);
    }

    public int component() {
        return component;
    }

    public int adc() {
        return ADC;
    }

    public double charge() {
        return charge;
    }

    public double time() {
        return time;
    }

    public double pedestal() {
        return pedestal;
    }
    
    public FTCalTrue trueInfo() {
        return trueInfo;
    }
    
    public void addTrue(FTCalTrue t) {
        this.trueInfo = t;
    }
}
