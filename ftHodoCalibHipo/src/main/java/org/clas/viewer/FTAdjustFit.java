/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.GraphErrors;


/**
 *
 * @author fanchini
 * Edited by Nick Zachariou
 * */
public class FTAdjustFit {
    
    public  F1D newfct;
    private F1D fct;
    private H1F hist;
    private String opt;
    private ArrayList<Double> pars     = new ArrayList<Double>();
    private ArrayList<Double> err_pars = new ArrayList<Double>();
    private double[]          range    = new double[2];
    private JFrame            frame    = new JFrame();
    private CustomPanel2      panel    = null;
    private CalibrationConstants calibcons= null;
    
    private int sector = 0;
    private int layer = 0;
    private int component = 0;
    private String moduleName;
    private GraphErrors gGraphToUpdate  = null;
    
    public FTAdjustFit(H1F h, F1D f, String opt, String moduleName){
        this.fct     = f;
        this.newfct  = f;
        this.hist    = h;
        this.opt     = opt;
        this.moduleName  = moduleName;
        this.openFitPanel(this.hist.getTitle());
    }
    
    public void openFitPanel(String title){
        panel = new CustomPanel2();
        frame.setSize(250, 300);
        frame.setTitle(title);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
    
    public void refit(boolean toFit){
        this.pars.clear();
        this.err_pars.clear();
        int npar = fct.getNPars();
        this.newfct.setName(fct.getName());
        for(int i=0; i<npar; i++){
            if(panel.params[i].getText().isEmpty()){
                this.pars.add(fct.getParameter(i));
                this.err_pars.add(fct.parameter(i).error());
            }
            else {
                this.pars.add(Double.parseDouble(panel.params[i].getText()));
            }
        }
        if(!panel.minRange.getText().isEmpty())this.range[0] = Double.parseDouble(panel.minRange.getText());
        else this.range[0] = fct.getMin();
        if(!panel.maxRange.getText().isEmpty())this.range[1] = Double.parseDouble(panel.maxRange.getText());
        else this.range[1] = fct.getMax();
        for(int i=0; i<this.pars.size(); i++){
            this.newfct.setParameter(i, this.pars.get(i));
        }
        this.newfct.setRange(range[0], range[1]);
        if (toFit)
            DataFitter.fit(newfct,hist,opt);
        hist.setFunction(null);
        for(int i=0; i<this.pars.size(); i++){
            this.err_pars.add(this.newfct.parameter(i).error());
        }
        if (toFit)
            this.newfct.setLineColor(4);
        else
            this.newfct.setLineColor(1);
    }
    
    public void setCalibTable(CalibrationConstants calibcons){
        this.calibcons = calibcons;
    }

    
    
    public void setSecLayComp(int sect, int lay, int comp){
        this.sector = sect;
        this.layer = lay;
        this.component = comp;
    }
    public void setGraphToUpdate(GraphErrors gGraphToUpdate){
        this.gGraphToUpdate = gGraphToUpdate;
    }
    
    
    
    public void SetToDefault(){
        
        if (Objects.equals(moduleName,"energy")){
            this.calibcons.setDoubleValue(400.0, "mips_charge", this.sector, this.layer, this.component);
            this.calibcons.setDoubleValue(100.0, "mips_charge_error", this.sector, this.layer, this.component);
                        
            if (gGraphToUpdate != null){
                this.gGraphToUpdate.setPoint(this.component-1, this.component,400.0);
                this.gGraphToUpdate.setError(this.component-1, 0, 100.0);
            }
            this.calibcons.fireTableDataChanged();
        }else if (Objects.equals(moduleName,"time")){
            this.calibcons.setDoubleValue(40.0, "time_offset", this.sector, this.layer, this.component);
            this.calibcons.setDoubleValue(10.0, "time_rms", this.sector, this.layer, this.component);
                        
            if (gGraphToUpdate != null){
                this.gGraphToUpdate.setPoint(this.component-1, this.component,40.0);
                this.gGraphToUpdate.setError(this.component-1, 0, 10.0);
            }
            this.calibcons.fireTableDataChanged();
        }
        else {
            System.out.println("Function not implemented in current module");
        }
    }
    
    
    
    private void SaveConstants(){
        if (this.calibcons==null || this.sector == 0 || this.layer == 0 || this.component == 0){
            System.out.println("Calibration table or detector component not set");
            return;
        }
        if (Objects.equals(moduleName,"energy")){
            this.calibcons.setDoubleValue(fct.getParameter(1), "mips_charge", this.sector, this.layer, this.component);
            this.calibcons.setDoubleValue(fct.getParameter(2), "mips_charge_error", this.sector, this.layer, this.component);
            this.calibcons.fireTableDataChanged();
            if (gGraphToUpdate != null){
                this.gGraphToUpdate.setPoint(this.component-1, this.component,fct.getParameter(1));
                this.gGraphToUpdate.setError(this.component-1, 0, fct.getParameter(2));
            }
        }else if (Objects.equals(moduleName,"time")){
            this.calibcons.setDoubleValue(fct.getParameter(1), "time_offset", this.sector, this.layer, this.component);
            this.calibcons.setDoubleValue(fct.getParameter(2), "time_rms", this.sector, this.layer, this.component);
            this.calibcons.fireTableDataChanged();
            if (gGraphToUpdate != null){
                this.gGraphToUpdate.setPoint(this.component-1, this.component,fct.getParameter(1));
                this.gGraphToUpdate.setError(this.component-1, 0, fct.getParameter(2));
            }
        }
        else {
            System.out.println("Function not implemented in current module");
        }
    }
    
    
    
    private final class CustomPanel2 extends JPanel {
        JLabel label;
        JPanel panel;
        JPanel subPanel;
        
        JTextField minRange = new JTextField(5);
        JTextField maxRange = new JTextField(5);
        JTextField[] params = new JTextField[10];
        JButton   fitButton = null;
        JButton   setButton = null;
        JButton   setDefaultButton = null;
        JButton   saveButton = null;

        public CustomPanel2() {
            super(new BorderLayout());
            
            int npar = newfct.getNPars();
            panel = new JPanel(new GridLayout(npar+2, 2));
            subPanel= new JPanel();
            for (int i = 0; i < npar; i++) {
                JLabel l = new JLabel(newfct.parameter(i).name(), JLabel.TRAILING);
                panel.add(l);
                params[i] = new JTextField(5);
                params[i].setText(String.format("%.3f", fct.getParameter(i)));
                panel.add(params[i]);
            }
            panel.add(new JLabel("  Fit range minimum"));
            minRange.setText(Double.toString(fct.getRange().getMin()));
            panel.add(minRange);
            panel.add(new JLabel("  Fit range maximum"));
            maxRange.setText(Double.toString(fct.getRange().getMax()));
            panel.add(maxRange);
            fitButton = new JButton("Fit");
            fitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refit(true);
                    return;
                }
            });
            setButton = new JButton("Set");
            setButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refit(false);
                    return;
                }
            });
            setDefaultButton = new JButton("Save to Default");
            setDefaultButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SetToDefault();
                    return;
                }
            });
            saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SaveConstants();
                    return;
                }
            });
            
            
            
            subPanel.add(fitButton);
            subPanel.add(setButton);
            subPanel.add(saveButton);
            subPanel.add(setDefaultButton);
            this.add(panel, BorderLayout.CENTER);
            //this.add(fitButton, BorderLayout.PAGE_END);
            this.add(subPanel, BorderLayout.PAGE_END);
            label = new JLabel("Click the \"Show it!\" button"
                               + " to bring up the selected dialog.",
                               JLabel.CENTER);
        }
        
        void setLabel(String newText) {
            label.setText(newText);
        }
        
    }
}
