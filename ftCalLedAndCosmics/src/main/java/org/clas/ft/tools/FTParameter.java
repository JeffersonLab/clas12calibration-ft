/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.awt.Color;
import org.jlab.groot.base.ColorPalette;


/**
 *
 * @author devita
 */
public class FTParameter {
    
    private String  name;
    private double  parValue;
    private double  parMin;
    private double  parMax;
    private double  parScale;
    private double  parLimit;
    private Color   parColor;
    private boolean parType;
    
    public FTParameter(String n) {
        this.name = n;
    }

    public FTParameter(String n, double min, double max) {
        this.name = n;
        this.parMin = min;
        this.parMax = max;
    }

    public String getName() {
        return this.name;
    }

    public double getLimit() {
        return this.parLimit;
    }
        
    public double getMax() {
        return this.parMax;
    }

    public double getMin() {
        return this.parMin;
    }

    public double getScale() {
        return this.parScale;
    }

    public boolean getType() {
        return parType;
    }
    
    public double getValue() {
        return this.parValue;
    }
        
    public void setLimit(double parLimit) {
        this.parLimit = parLimit;
    }
    
    public void setMax(double parMax) {
        this.parMax = parMax;
    }
    
    public void setMin(double parMin) {
        this.parMin = parMin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRange(double parMin, double parMax) {
        this.parMin = parMin;
        this.parMax = parMax;
    }
    
    public void setRanges(double parMin, double parMax, double parScale, double parLimit) {
        this.parMin = parMin;
        this.parMax = parMax;
        this.parScale = parScale;
        this.parLimit = parLimit;
    }
    
    public void setRanges(double parMin, double parMax, double parScale, double parLimit, boolean type) {
        this.parMin = parMin;
        this.parMax = parMax;
        this.parScale = parScale;
        this.parLimit = parLimit;
        this.parType  = type;
    }
    
    public void setScale(double parScale) {
        this.parScale = parScale;
    }
    
    public void setValue(double value) {
        this.parValue = value;
    }
            
    public boolean isValid(double value) {
        boolean stat = false;
        this.setValue(value);
        if(value>=this.parMin && value<=this.parMax) stat=true;
        return stat;
    }
    
    public boolean isLow(double value) {
        boolean stat = false;
        this.setValue(value);
        if(value<this.parMin) stat=true;
        return stat;
    }
    
    public boolean isHigh(double value) {
        boolean stat = false;
        this.setValue(value);
        if(value>this.parMax) stat=true;
        return stat;
    }
    
    private Color getColor() {
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
        if(this.parType) {
            if(this.isValid(this.parValue))    col = new Color(0, 145, 0);
            else if(this.isLow(this.parValue)) col = new Color(0, 0, 100);
            else                               col = new Color(255, 100, 0);             
        }
        else {
            col = palette.getColor3D(this.parValue*this.parScale, this.parLimit*this.parScale, false);
         
        }
        return col;
    }

    public Color getColor(double value) {
        this.setValue(value);       
        return this.getColor();
    }
    
    public double getStatus(double value) {
        if(this.isValid(value))    return 0;
        else if(this.isLow(value)) return -1;
        else                       return 1;
    }
}
