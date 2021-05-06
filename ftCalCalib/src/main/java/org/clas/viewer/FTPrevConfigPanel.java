package org.clas.viewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.jlab.groot.base.GStyle;


public class FTPrevConfigPanel extends JPanel 
implements ActionListener, FocusListener {

	FTCalibrationModule module;
	JTextField fileDisp = new JTextField(20); 
	JTextField runText = new JTextField(5);
	JFileChooser fc = new JFileChooser();
	
	JRadioButton defaultRad = new JRadioButton("DEFAULT");
	JRadioButton fileRad = new JRadioButton("FILE");
	JRadioButton dbRad = new JRadioButton("DB");
	
	public FTPrevConfigPanel(FTCalibrationModule engineIn) {

		module = engineIn;
		File workDir = new File(GStyle.getWorkingDirectory());
 		fc.setCurrentDirectory(workDir);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.anchor = c.NORTHWEST;
		c.insets = new Insets(2,2,2,2);

		defaultRad.setSelected(true);
		ButtonGroup radGroup = new ButtonGroup();
		radGroup.add(defaultRad);
		radGroup.add(fileRad);
		radGroup.add(dbRad);
		defaultRad.addActionListener(this);
		fileRad.addActionListener(this);
		dbRad.addActionListener(this);

		c.gridx = 0;
		c.gridy = 0;
		add(defaultRad,c);
		c.gridx = 0;
		c.gridy = 1;
		add(fileRad,c);
		c.gridx = 1;
		c.gridy = 1;
		fileDisp.setEditable(false);
		fileDisp.setText("None selected");
		add(new JLabel("Selected file: "),c);
		c.gridx = 2;
		c.gridy = 1;
		add(fileDisp,c);
		c.gridx = 3;
		c.gridy = 1;
		JButton fileButton = new JButton("Select File");
		fileButton.addActionListener(this);
		add(fileButton,c);

		c.gridx = 0;
		c.gridy = 2;
		add(dbRad,c);
		JLabel runLabel = new JLabel("Run number:");
		c.gridx = 1;
		c.gridy = 2;
		add(runLabel,c);
		c.gridx = 2;
		c.gridy = 2;
		add(runText,c);
		runText.addFocusListener(this);

		this.setBorder(BorderFactory.createTitledBorder(module.getName()));

	}

	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand() == "Select File") {
                            File workDir = new File(GStyle.getWorkingDirectory());
                            fc.setCurrentDirectory(workDir);
			int returnValue = fc.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				module.calDBSource = module.CAL_FILE;
				module.prevCalFilename = fc.getSelectedFile().getAbsolutePath();
				fileDisp.setText("Selected file: "+ fc.getSelectedFile().getAbsolutePath());
				fileRad.setSelected(true);
                                     GStyle.setWorkingDirectory(fc.getSelectedFile().getParent());
			}
		}

		if (e.getActionCommand() == "DB") {
			module.calDBSource = module.CAL_DB;
		}
		else if (e.getActionCommand() == "FILE") {
			module.calDBSource = module.CAL_FILE;
		}
		else if (e.getActionCommand() == "DEFAULT") {
			module.calDBSource = module.CAL_DEFAULT;
		}
	}

	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void focusLost(FocusEvent e) {
		module.prevCalRunNo = Integer.parseInt(runText.getText());
	}

}
