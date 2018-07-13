/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jlab.groot.math.F1D;

/**
 *
 * @author nicholas
 */


public class FTAdjustFitParams {
  
    public  F1D fct;
    private ArrayList<Double> pars     = new ArrayList<Double>();
    private double[]          range    = new double[2];
    private JFrame            frame    = new JFrame();
    private CustomPanelFitParams      panel    = null;
    
    public FTAdjustFitParams(F1D f, String title){
        this.fct     = f;
        this.openFitPanel(title);
    }
    
    public void openFitPanel(String title){
        panel = new CustomPanelFitParams();
        frame.setSize(250, 300);
        frame.setTitle(title);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        //        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
    }
    public void changeFitParams(){
        this.pars.clear();
        int npar = fct.getNPars();
        for(int i=0; i<npar; i++){
            if(panel.params[i].getText().isEmpty()){
                this.pars.add(fct.getParameter(i));
            }
            else {
                this.pars.add(Double.parseDouble(panel.params[i].getText()));
            }
            System.out.println("Param["+i+"] = " + pars.get(i));
        }
        if(!panel.minRange.getText().isEmpty()){
            this.range[0] = Double.parseDouble(panel.minRange.getText());
        }
        else{
            this.range[0] = fct.getMin();
        }
        if(!panel.maxRange.getText().isEmpty()){
            this.range[1] = Double.parseDouble(panel.maxRange.getText());
        }
        else{
            this.range[1] = fct.getMax();
        }
        System.out.println("Range: " + this.range[0] +" -- "+ this.range[1]);
    }
    public double[] getRangeFit(){
        return range;
    }
    public ArrayList<Double> getParamsFit(){
        return pars;
    }
    
    
//    public void refit(){
//
//        this.err_pars.clear();
//
//        this.newfct.setName(fct.getName());
//
    
    
    
//        if(!panel.minRange.getText().isEmpty())this.range[0] = Double.parseDouble(panel.minRange.getText());
//        else this.range[0] = fct.getMin();
//        if(!panel.maxRange.getText().isEmpty())this.range[1] = Double.parseDouble(panel.maxRange.getText());
//        else this.range[1] = fct.getMax();
//        for(int i=0; i<this.pars.size(); i++){
//            this.newfct.setParameter(i, this.pars.get(i));
//        }
//        this.newfct.setRange(range[0], range[1]);
//        DataFitter.fit(newfct,hist,opt);
//        hist.setFunction(null);
//        for(int i=0; i<this.pars.size(); i++){
//            this.err_pars.add(this.newfct.parameter(i).error());
//        }
//        this.newfct.setLineColor(3);
//    }
    
    private final class CustomPanelFitParams extends JPanel {
        JLabel label;
        JPanel panel;
        JTextField minRange = new JTextField(5);
        JTextField maxRange = new JTextField(5);
        JTextField[] params = new JTextField[10];
        JButton   SetButton = null;
        
        public CustomPanelFitParams() {
            super(new BorderLayout());
            int npar = fct.getNPars();
            panel = new JPanel(new GridLayout(npar+2, 2));
            for (int i = 0; i < npar; i++) {
                JLabel l = new JLabel(fct.parameter(i).name(), JLabel.TRAILING);
                panel.add(l);
                params[i] = new JTextField(5);
                params[i].setText(String.format("%.3f", fct.getParameter(i)));
                panel.add(params[i]);
            }
            panel.add(new JLabel("Fit range minimum "));
            minRange.setText(Double.toString(fct.getRange().getMin()));
            panel.add(minRange);
            panel.add(new JLabel("Fit range maximum "));
            maxRange.setText(Double.toString(fct.getRange().getMax()));
            panel.add(maxRange);
            SetButton = new JButton("Set new fit parameters");
            SetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changeFitParams();
                    frame.dispose();
                    return;
                }
            });
            this.add(panel, BorderLayout.CENTER);
            this.add(SetButton, BorderLayout.PAGE_END);
            
            label = new JLabel("Click the \"Show it!\" button"
                               + " to bring up the selected dialog.",
                               JLabel.CENTER);
        }
        void setLabel(String newText) {
            label.setText(newText);
        }
        
    }
}
