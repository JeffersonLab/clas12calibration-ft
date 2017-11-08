/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.util.ArrayList;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.math.F1D;


/**
 *
 * @author devita
 */
public class FTDataSet {
    FTDetector ft;
    ArrayList<DetectorCollection> dc      = new ArrayList<DetectorCollection>();
    ArrayList<String>             dclabel = new ArrayList<String>();

    public FTDataSet(FTDetector d) {
        this.ft = d;
    }
    
    public DetectorCollection addCollection(H1F histo, String listlabel) {
        DetectorCollection h = new DetectorCollection();
        for(int key : this.ft.getDetectorComponents()) {
            H1F histoComponent = histo.histClone(histo.getName() + "_" + key);
            histoComponent.setTitle("Component " + key + this.ft.getComponentName(key));
            h.add(0,0,key,histoComponent);
        }
        this.dc.add(h);
        this.dclabel.add(listlabel);
        return h;
    }

    public DetectorCollection addCollection(H1F histo, String XTitle, String YTitle, int Col,String listlabel) {
        DetectorCollection h = new DetectorCollection();
        for(int key : this.ft.getDetectorComponents()) {
            H1F histoComponent = histo.histClone(histo.getName() + "_" + key);
            histoComponent.setTitle("Component " + key + " " + this.ft.getComponentName(key));
            histoComponent.setTitleX(XTitle);
            histoComponent.setTitleY(YTitle);
            histoComponent.setFillColor(Col);
            histoComponent.setName(histo.getName() + "_" + key);
            h.add(0,0,key,histoComponent);
            }
        this.dc.add(h);
        this.dclabel.add(listlabel);
        return h;
    }

    public DetectorCollection addCollection(F1D funct, String listlabel) {
        DetectorCollection f = new DetectorCollection();
        for(int key : this.ft.getDetectorComponents()) {
            f.add(0,0,key,funct);
        }
        this.dc.add(f);
        this.dclabel.add(listlabel);
        return f;
    }
    
    public DetectorCollection addCollection(F1D funct, String name, String listlabel) {
        DetectorCollection f = new DetectorCollection();
        for(int key : this.ft.getDetectorComponents()) {
            f.add(0,0,key,funct);
        }
        this.dc.add(f);   
        this.dclabel.add(listlabel);
        return f;
    }

    public DetectorCollection addCollection(GraphErrors graf,String listlabel) {
        DetectorCollection g = new DetectorCollection();
        for(int key : this.ft.getDetectorComponents()) {
            GraphErrors graphComponent = new GraphErrors();
            graphComponent.setName(graf.getName());
            g.add(0,0,key,graphComponent);
        }
        this.dc.add(g);
        this.dclabel.add(listlabel);
        return g;
    }
    
    public DetectorCollection getDataSet(int j){
        return this.dc.get(j);
    }
    
    public ArrayList getData(){
        return this.dc;
    }
    
    public ArrayList getLabels(){
        return this.dclabel;
    }

    
}
