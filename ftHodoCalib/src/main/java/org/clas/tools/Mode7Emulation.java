/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

//import org.clas.detector.DetectorPane2D;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

public class Mode7Emulation extends JPanel implements ActionListener,ItemListener {
	
//   DetectorPane2D detectorView = null;
   IndexedTable      fadc = null;
   
   ButtonGroup       bG3  = new ButtonGroup();
   JRadioButton      bG3a = new JRadioButton("CCDB"); 
   JRadioButton      bG3b = new JRadioButton("User");
   JCheckBox           cb = new JCheckBox("RefPeds");
   public JTextField tnsa = new JTextField(3); 
   public JTextField tnsb = new JTextField(3);
   public JTextField ttet = new JTextField(3);
    
   public int tet,nsa,nsb,pedref;
   public int User_pedref=0;
   public int User_tet=0;
   public int User_nsa=0;
   public int User_nsb=0;
   public int CCDB_tet=0;
   public int CCDB_nsa=0;
   public int CCDB_nsb=0;	    
   public int useCCDB = 1;
	
//   public void setPluginClass(DetectorPane2D detectorView) {    		 
//      this.detectorView = detectorView;
//   }
	
   public Mode7Emulation() {
		
//      this.setBackground(Color.LIGHT_GRAY);
		
      this.add(bG3a); bG3a.setActionCommand("CCDB"); bG3a.addActionListener(this); bG3a.setSelected(true); 
      this.add(bG3b); bG3b.setActionCommand("User"); bG3b.addActionListener(this);
      this.add(new JLabel("TET")); this.add(ttet); ttet.setActionCommand("TET"); ttet.addActionListener(this);
      this.add(new JLabel("NSB")); this.add(tnsb); tnsb.setActionCommand("NSB"); tnsb.addActionListener(this);
      this.add(new JLabel("NSA")); this.add(tnsa); tnsa.setActionCommand("NSA"); tnsa.addActionListener(this);
      this.add(cb); cb.addItemListener(this); cb.setSelected(false);
		
      bG3a.setBackground(Color.LIGHT_GRAY);
      bG3b.setBackground(Color.LIGHT_GRAY);
        cb.setBackground(Color.LIGHT_GRAY);
      bG3.add(bG3a); bG3.add(bG3b);
   }
	
   public void configMode7(int cr, int sl, int ch) {    
      this.nsa    = (int) fadc.getIntValue("nsa",     cr,sl,ch);
      this.nsb    = (int) fadc.getIntValue("nsb",     cr,sl,ch);
      this.tet    = (int) fadc.getIntValue("tet",     cr,sl,ch);
      this.pedref = (int) fadc.getIntValue("pedestal",cr,sl,ch);
      CCDB_tet=this.tet;
      CCDB_nsa=this.nsa;
      CCDB_nsb=this.nsb;
      if (User_tet>0) this.tet=User_tet;
      if (User_nsa>0) this.nsa=User_nsa;
      if (User_nsb>0) this.nsb=User_nsb;
   }
	   
   public void init(ConstantsManager ccdb, int run, String table, int cr, int sl, int ch) {   
      this.fadc = ccdb.getConstants(run, table);
      configMode7(cr,sl,ch);
      updateGUI();
   }
   
   public void updateGUI() {
      ttet.setText(Integer.toString(this.tet)); 
      tnsa.setText(Integer.toString(this.nsa));
      tnsb.setText(Integer.toString(this.nsb));
//      detectorView.repaint();
   }

   @Override
   public void actionPerformed(ActionEvent e) {			
      if(e.getActionCommand().compareTo("TET")==0) {
         this.useCCDB=0; bG3b.setSelected(true);
         User_tet = Integer.parseInt(ttet.getText());
//         detectorView.repaint();
      }		 
      if(e.getActionCommand().compareTo("NSA")==0) {
         this.useCCDB=0; bG3b.setSelected(true);
         User_nsa = Integer.parseInt(tnsa.getText());
      }		 
      if(e.getActionCommand().compareTo("NSB")==0) {
         this.useCCDB=0; bG3b.setSelected(true);
         User_nsb = Integer.parseInt(tnsb.getText());
      }		 
      if(e.getActionCommand().compareTo("CCDB")==0) {
         this.useCCDB=1;	bG3a.setSelected(true);
         ttet.setText(Integer.toString(CCDB_tet));
         tnsa.setText(Integer.toString(CCDB_nsa));
         tnsb.setText(Integer.toString(CCDB_nsb));
         User_tet=0;User_nsa=0;User_nsb=0;
//         detectorView.repaint();
      }
   }
 	
   @Override
   public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange()==ItemEvent.DESELECTED) User_pedref=0;
      if (e.getStateChange()==ItemEvent.SELECTED)   User_pedref=1;			
   }
}
