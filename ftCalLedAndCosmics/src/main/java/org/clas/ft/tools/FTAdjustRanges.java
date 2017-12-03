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


public class FTAdjustRanges {
    
    private F1D fct;
    private String opt;
    private double[]          range    = new double[2];
    private JFrame            frame    = new JFrame();
    private CustomPanel2      panel    = null;
    
    public FTAdjustRanges(F1D f,String opt){
        this.fct = f;
        this.opt = opt;
        this.openFitPanel("Adjust fit ranges...");
    }
    
    public void openFitPanel(String title){
        
        panel = new CustomPanel2();
        int result = JOptionPane.showConfirmDialog(null, panel, 
                        "Adjust Fit", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
           if(!panel.minRange.getText().isEmpty())this.range[0] = Double.parseDouble(panel.minRange.getText());
            else this.range[0] = fct.getMin();
            if(!panel.maxRange.getText().isEmpty())this.range[1] = Double.parseDouble(panel.maxRange.getText());
            else this.range[1] = fct.getMax();
            fct.setRange(this.range[0], this.range[1]);
        }       
    }


    private final class CustomPanel2 extends JPanel {
        JLabel label;
        JPanel panel;
    	JTextField minRange = new JTextField(5);
	JTextField maxRange = new JTextField(5);
        JButton   fitButton = null;
        
  
        public CustomPanel2() {        
            super(new BorderLayout());

            panel = new JPanel(new GridLayout(2, 2));            
           
           panel.add(new JLabel("Fit range minimum"));
            minRange.setText(Double.toString(fct.getRange().getMin()));
            panel.add(minRange);
            panel.add(new JLabel("Fit range maximum"));
            maxRange.setText(Double.toString(fct.getRange().getMax()));
            panel.add(maxRange);
            fitButton = new JButton("Fit");
//            fitButton.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    refit();
//                    return;
//                }
//            });
            this.add(panel, BorderLayout.CENTER);
//            this.add(fitButton, BorderLayout.PAGE_END);
            
            label = new JLabel("Click the \"Show it!\" button"
                           + " to bring up the selected dialog.",
                           JLabel.CENTER);
       }

                
        
        
        void setLabel(String newText) {
            label.setText(newText);
        }

    }
}