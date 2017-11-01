/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

//import org.clas.detector.DetectorPane2D;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

public class FadcControlPanel extends JPanel implements ActionListener {

//   DetectorPane2D detectorView = null;
    IndexedTable fadc = null;

    JPanel mode = new JPanel();
    JPanel mode1 = new JPanel();
    JPanel mode7 = new JPanel();

    ButtonGroup bG = new ButtonGroup();
    JRadioButton bGa = new JRadioButton("Mode 1");
    JRadioButton bGb = new JRadioButton("Mode 7");

    ButtonGroup bG7 = new ButtonGroup();
    JRadioButton bG7a = new JRadioButton("CCDB");
    JRadioButton bG7b = new JRadioButton("User");

    public JTextField tnsa = new JTextField(3);
    public JTextField tnsb = new JTextField(3);
    public JTextField ttet = new JTextField(3);

    public JTextField tped1 = new JTextField(3);
    public JTextField tped2 = new JTextField(3);
    public JTextField tpul1 = new JTextField(3);
    public JTextField tpul2 = new JTextField(3);
    public JTextField tthrs = new JTextField(3);

    public int nsa;
    public int nsb;
    public int tet = 20;
    public int ped1 = 1;
    public int ped2 = 15;
    public int pul1 = 21;
    public int pul2 = 60;
    public int CCDB_tet = 0;
    public int CCDB_nsa = 0;
    public int CCDB_nsb = 0;
    public boolean useMode7 = true;
    public boolean useCCDB = true;

    public FadcControlPanel() {

        this.setLayout(new BorderLayout());
        this.add(mode, BorderLayout.PAGE_START);
        this.add(mode1, BorderLayout.CENTER);
        this.add(mode7, BorderLayout.PAGE_END);
        mode1.setVisible(false);

        // mode 1/7 choice
        this.mode.add(bGa, BorderLayout.NORTH);
        bGa.setActionCommand("Mode 1");
        bGa.setToolTipText("Use fixed-window integration");
        bGa.addActionListener(this);
        bGa.setSelected(false);
        this.mode.add(bGb, BorderLayout.NORTH);
        bGb.setActionCommand("Mode 7");
        bGb.setToolTipText("Use mode-7 emulation");
        bGb.addActionListener(this);
        bGb.setSelected(true);
        bGa.setBackground(Color.LIGHT_GRAY);
        bGb.setBackground(Color.LIGHT_GRAY);
        bG.add(bGa);
        bG.add(bGb);

        // mode 7 buttons and fields
        this.mode7.add(bG7a);
        bG7a.setActionCommand("CCDB");
        bG7a.setToolTipText("Use CCDB fADC integration parameters");
        bG7a.addActionListener(this);
        bG7a.setSelected(true);
        this.mode7.add(bG7b);
        bG7b.setActionCommand("User");
        bG7b.setToolTipText("Use user-selected fADC integration parameters");
        bG7b.addActionListener(this);
        this.mode7.add(new JLabel("TET"));
        this.mode7.add(ttet);
        ttet.setEnabled(false);
        ttet.setActionCommand("TET");
        ttet.setToolTipText("Threshold for pulse detection");
        ttet.addActionListener(this);
        this.mode7.add(new JLabel("NSB"));
        this.mode7.add(tnsb);
        tnsb.setEnabled(false);
        tnsb.setActionCommand("NSB");
        tnsb.setToolTipText("Number of samples before the trigger");
        tnsb.addActionListener(this);
        this.mode7.add(new JLabel("NSA"));
        this.mode7.add(tnsa);
        tnsa.setEnabled(false);
        tnsa.setActionCommand("NSA");
        tnsa.setToolTipText("Number of sample after he trigger");
        tnsa.addActionListener(this);
        bG7a.setBackground(Color.LIGHT_GRAY);
        bG7b.setBackground(Color.LIGHT_GRAY);
        bG7.add(bG7a);
        bG7.add(bG7b);

        // mode 1 buttons and fields
        this.mode1.add(new JLabel("Pedestal"));
        this.mode1.add(tped1);
        tped1.setActionCommand("PED1");
        tped1.setToolTipText("First bin in pedestal range");
        tped1.addActionListener(this);
        this.mode1.add(new JLabel(":"));
        this.mode1.add(tped2);
        tped2.setActionCommand("PED2");
        tped2.setToolTipText("Last bin in pedestal range");
        tped2.addActionListener(this);
        this.mode1.add(new JLabel("Pulse"));
        this.mode1.add(tpul1);
        tpul1.setActionCommand("PUL1");
        tpul1.setToolTipText("First bin in pulse range");
        tpul1.addActionListener(this);
        this.mode1.add(new JLabel(":"));
        this.mode1.add(tpul2);
        tpul2.setActionCommand("PUL2");
        tpul2.setToolTipText("Last bin in pulse range");
        tpul2.addActionListener(this);
        this.mode1.add(new JLabel("TET"));
        this.mode1.add(tthrs);
        tthrs.setActionCommand("TET");
        tthrs.setToolTipText("Threshold for pulse detection");
        tthrs.addActionListener(this);
    }

    public void configMode7(int cr, int sl, int ch) {
        this.nsa = (int) fadc.getIntValue("nsa", cr, sl, ch);
        this.nsb = (int) fadc.getIntValue("nsb", cr, sl, ch);
        this.tet = (int) fadc.getIntValue("tet", cr, sl, ch);
        CCDB_tet = this.tet;
        CCDB_nsa = this.nsa;
        CCDB_nsb = this.nsb;
    }

    public void init(ConstantsManager ccdb, int run, String table, int cr, int sl, int ch) {
        this.fadc = ccdb.getConstants(run, table);
        configMode7(cr, sl, ch);
        updateGUI();
    }

    public void setTET(int tet) {
        this.bG7b.setSelected(true);
        this.ttet.setEnabled(true);
        this.tnsa.setEnabled(true);
        this.tnsb.setEnabled(true);
        this.tet = tet;
        updateGUI();
    }

    public void updateGUI() {
        ttet.setText(Integer.toString(this.tet));
        tnsa.setText(Integer.toString(this.nsa));
        tnsb.setText(Integer.toString(this.nsb));
        tped1.setText(Integer.toString(this.ped1));
        tped2.setText(Integer.toString(this.ped2));
        tpul1.setText(Integer.toString(this.pul1));
        tpul2.setText(Integer.toString(this.pul2));
        tthrs.setText(Integer.toString(this.tet));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().compareTo("Mode 1") == 0) {
            this.mode1.setVisible(true);
            this.mode7.setVisible(false);
            this.useMode7 = false;
            this.ped1 = Integer.parseInt(tped1.getText());
            this.ped2 = Integer.parseInt(tped2.getText());
            this.pul1 = Integer.parseInt(tpul1.getText());
            this.pul2 = Integer.parseInt(tpul2.getText());
            this.tet = Integer.parseInt(tthrs.getText());
            System.out.println("\nfADC integration in fixed windows:");
            System.out.println("\t pedestal:  " + ped1 + ":" + ped2);
            System.out.println("\t pulse:     " + pul1 + ":" + pul2);
            System.out.println("\t threshold: " + tet);
        }
        if (e.getActionCommand().compareTo("Mode 7") == 0) {
            this.mode1.setVisible(false);
            this.mode7.setVisible(true);
            this.useMode7 = true;
            this.tet = Integer.parseInt(ttet.getText());
            System.out.println("\nfADC integration with mode-7 emulation:");
            System.out.println("\t nsa: " + nsa);
            System.out.println("\t nsb: " + nsb);
            System.out.println("\t tet: " + tet);
        }
        if (e.getActionCommand().compareTo("PED1") == 0) {
            this.ped1 = Integer.parseInt(tped1.getText());
            System.out.println("\nPedestal first bin set to: " + ped1);
        }
        if (e.getActionCommand().compareTo("PED2") == 0) {
            this.ped2 = Integer.parseInt(tped2.getText());
            System.out.println("\nPedestal last bin set to: " + ped2);
        }
        if (e.getActionCommand().compareTo("PUL1") == 0) {
            this.pul1 = Integer.parseInt(tpul1.getText());
            System.out.println("\nPulse first bin set to: " + pul1);
        }
        if (e.getActionCommand().compareTo("PUL2") == 0) {
            this.pul2 = Integer.parseInt(tpul2.getText());
            System.out.println("\nPulse last bin set to: " + pul2);
        }
        if (e.getActionCommand().compareTo("User") == 0) {
            this.useCCDB = false;
            this.ttet.setEnabled(true);
            this.tnsa.setEnabled(true);
            this.tnsb.setEnabled(true);
            System.out.println("\nUsing user-defined parameters for mode-7 emulation");
        }
        if (e.getActionCommand().compareTo("TET") == 0) {
            bG7b.setSelected(true);
            if (this.useMode7) {
                this.tet = Integer.parseInt(ttet.getText());
            } else {
                this.tet = Integer.parseInt(tthrs.getText());
            }
            System.out.println("\nPulse threshold set to: " + tet);
        }
        if (e.getActionCommand().compareTo("NSA") == 0) {
            bG7b.setSelected(true);
            this.nsa = Integer.parseInt(tnsa.getText());
            System.out.println("\nSetting nsa to: " + nsa);
        }
        if (e.getActionCommand().compareTo("NSB") == 0) {
            bG7b.setSelected(true);
            this.nsb = Integer.parseInt(tnsb.getText());
            System.out.println("\nSetting nsa to: " + nsb);
        }
        if (e.getActionCommand().compareTo("CCDB") == 0) {
            this.useCCDB = true;
            this.ttet.setEnabled(false);
            this.tnsa.setEnabled(false);
            this.tnsb.setEnabled(false);
            ttet.setText(Integer.toString(CCDB_tet));
            tnsa.setText(Integer.toString(CCDB_nsa));
            tnsb.setText(Integer.toString(CCDB_nsb));
            this.tet = CCDB_tet;
            this.nsa = CCDB_nsa;
            this.nsb = CCDB_nsb;
            System.out.println("\nUsing CCDB parameters for mode-7 emulation");
        }
    }

}
