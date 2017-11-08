/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author devita
 */
public class FTViewer implements ActionListener {
    
    
    JMenuBar menuBar = new JMenuBar();    

    public FTViewer() {
    }

    
    public void addConstantMenu() {
        JMenuItem menuItem;
        JMenu constants = new JMenu("Constants");
        menuItem = new JMenuItem("Load...", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Load constants from file");
        menuItem.addActionListener(this);
        constants.add(menuItem);        
        menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save constants to file");
        menuItem.addActionListener(this);
        constants.add(menuItem);
        this.menuBar.add(constants);                
    }
 
    public void addFileMenu() {
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        menuItem = new JMenuItem("Load files...");
        menuItem.getAccessibleContext().setAccessibleDescription("Load files");
        menuItem.addActionListener(this);
        file.add(menuItem);        
        file.addSeparator();
        menuItem = new JMenuItem("Open histograms file...");
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms...");
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        this.menuBar.add(file);  
    }
    
    public void addFitMenu() {
        JMenuItem menuItem;
        JMenu fit = new JMenu("Fit");
        menuItem = new JMenuItem("Adjust fit...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit parameters and range");
        menuItem.addActionListener(this);
        fit.add(menuItem);        
        menuItem = new JMenuItem("View all");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        fit.add(menuItem);
        this.menuBar.add(fit);                
    }
        
    public void addSettingsMenu() {
        JMenuItem menuItem;
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set analysis update interval...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        this.menuBar.add(settings);
        menuItem = new JMenuItem("Choose work directory...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set analysis work directory");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        this.menuBar.add(settings);
    }

    public void addTableMenu() {
        JMenuItem menuItem;
        JMenu table = new JMenu("Table");
        table.setMnemonic(KeyEvent.VK_A);
        table.getAccessibleContext().setAccessibleDescription("Table operations");
        menuItem = new JMenuItem("Save table...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save table content to file");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuItem = new JMenuItem("Clear table");
        menuItem.getAccessibleContext().setAccessibleDescription("Clear table content");
        menuItem.addActionListener(this);
        table.add(menuItem);
        this.menuBar.add(table);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    
    
}
