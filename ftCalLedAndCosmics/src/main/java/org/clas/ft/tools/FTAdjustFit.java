/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ft.tools;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;



/**
 *
 * @author fanchini
 */


public class FTAdjustFit {
    
    public F1D newfct; 
    private F1D fct;
    private H1F hist;
    private String opt;
    private ArrayList<Double> pars     = new ArrayList<Double>();
    private ArrayList<Double> err_pars = new ArrayList<Double>();
    private double[]          range    = new double[2];
    
    public FTAdjustFit(H1F h, F1D f, String opt){
        this.fct     = f;
        this.newfct  = f;
        this.hist    = h;
        this.opt     = opt;
        this.openFitPanel();
    }
    
    public void openFitPanel(){
        this.pars.clear();
        this.err_pars.clear();
        int npar = fct.getNPars();
        this.newfct.setName(fct.getName());
        
        CustomPanel panel = new CustomPanel();

        int result = JOptionPane.showConfirmDialog(null, panel,"Adjust Fit", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
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
            
            refit(); 
        }       
    }

    public void refit(){
        for(int i=0; i<this.pars.size(); i++){
            this.newfct.setParameter(i, this.pars.get(i));
        }
        this.newfct.setRange(range[0], range[1]);
        DataFitter.fit(newfct,hist,opt);
        for(int i=0; i<this.pars.size(); i++){
            this.err_pars.add(this.newfct.parameter(i).error());
        }
        this.newfct.setLineColor(3);
        
    }

	
    private class CustomPanel extends JPanel {
        
	JTextField minRange = new JTextField(5);
	JTextField maxRange = new JTextField(5);
	JTextField[] params = new JTextField[10];
        
	private CustomPanel(){
            int npar = fct.getNPars();
            this.setLayout(new GridLayout(npar+2, 2));            
           
            for (int i = 0; i < npar; i++) {  
                JLabel l = new JLabel("Par"+i, JLabel.TRAILING);
                this.add(l);
                params[i] = new JTextField(5);
                params[i].setText(Double.toString(fct.getParameter(i)));
                this.add(params[i]);
            }
            this.add(new JLabel("Fit range minimum"));
            minRange.setText(Double.toString(fct.getRange().getMin()));
            this.add(minRange);
            this.add(new JLabel("Fit range maximum"));
            maxRange.setText(Double.toString(fct.getRange().getMax()));
            this.add(maxRange);
  	}
    }    
   

    public void clear(){
        this.pars.clear();
        this.range[0]=0;
        this.range[1]=0;
        this.hist.reset();  
    }


    
    
}