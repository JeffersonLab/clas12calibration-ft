package org.clas.viewer;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, CalibrationConstantsListener, DetectorListener {
    
    private final int[] npaddles = new int[]{23,62,5};

//    CalibrationEngineView view = null;
    FTCalibrationModule        ce     = null;

    JPanel                   mainPanel 	   = null;
    DataSourceProcessorPane  processorPane = null;
    JSplitPane               splitPanel    = null;
    JPanel                   detectorPanel = null;
    FTCalDetector            detectorView  = null;
    JSplitPane               moduleView    = null;
    EmbeddedCanvas           canvas        = null;
    CalibrationConstantsView ccview        = null;
    
    

    public CalibrationViewer() {
       
        // create main panel
        mainPanel = new JPanel();	
	mainPanel.setLayout(new BorderLayout());
        
        // create detector panel
        detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new FTCalDetector("FTCal");
        initDetector();
        detectorPanel.add(detectorView);
        
        // create calibration module
        ce     = new FTCalibrationModule(detectorView);        
        ccview = new CalibrationConstantsView();
        ccview.addConstants(ce.getCalibrationConstants().get(0));
        
        // create module viewer
        moduleView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas(); 
        canvas.initTimer(2000);
        ccview = new CalibrationConstantsView();
        ccview.addConstants(ce.getCalibrationConstants().get(0),this);
        moduleView.setTopComponent(canvas);
        moduleView.setBottomComponent(ccview);
        moduleView.setDividerLocation(0.5);        
        moduleView.setResizeWeight(0.6);
 
        // create split panel to host detector view and canvas+constants view
        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorPanel);
        splitPanel.setRightComponent(moduleView);

        // create data processor panel
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(10000);
        processorPane.addEventListener(this);
    
        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);

    }
    
    public void initDetector() {
        detectorView.setThresholds(0);
        detectorView.getView().addDetectorListener(this);
        detectorView.updateBox();
    }
     
    @Override
    public void dataEventAction(DataEvent de) {

        if (de.getType()==DataEventType.EVENT_START) {
                this.ce.resetEventListener();
                this.ce.processEvent(de);

        }
        else if (de.getType()==DataEventType.EVENT_ACCUMULATE) {
                this.ce.processEvent(de);
        }
        else if (de.getType()==DataEventType.EVENT_STOP) {
                this.ce.analyze();
        } 

        if (de.getType()==DataEventType.EVENT_STOP) {

        }
        this.detectorView.repaint();

    }

    public void timerUpdate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void resetEventListener() {
        this.ce.resetEventListener();
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = ce.getDataGroup();
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            DataGroup dataGroup = group.getItem(sector,layer,component);
            this.canvas.clear();
            this.canvas.draw(dataGroup);
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
	// show summary
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = ce.getDataGroup();        
        
        if(group.hasItem(sector,layer,paddle)==true){
            this.canvas.clear();
            this.canvas.draw(this.ce.getDataGroup().getItem(sector,layer,paddle));
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
        
    }

    public void update(DetectorShape2D dsd) {
	// show summary
        int sector = dsd.getDescriptor().getSector();
        int layer  = dsd.getDescriptor().getLayer();
        int key    = dsd.getDescriptor().getComponent();
        ColorPalette palette = new ColorPalette();
        Color col = new Color(100, 100, 100);
//        if (this.detectorView.hasComponent(key)) {
//            int nent = this.ce.getNEvents(sector, layer, key);
//            if (nent > 0) {
//                col = palette.getColor3D(nent, this.detectorView.getView()., true);
//                }
//            } 
//            Color col = ce.getColor(sector,layer,paddle);
//            dsd.setColor(col.getRed(),col.getGreen(),col.getBlue());  
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalibrationViewer viewer = new CalibrationViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }


}
