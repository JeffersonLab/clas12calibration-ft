package org.clas.viewer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.jlab.groot.graphics.EmbeddedCanvas;


/**
 *
 * @author devita
 */
public class FTApplication implements ActionListener {

    private String             appName    = null;
    private FTDetector         detector   = null;
    private FTDataSet          dataSet    = null;
    private List<EmbeddedCanvas> canvases   = new ArrayList<>();
    private JPanel             radioPane  = new JPanel();
    private List<String>       fields     = new ArrayList<>();
    private String             buttonSelect;
    private int                buttonIndex;
    private String             canvasSelect;
    private int                canvasIndex;
    
    public FTApplication(FTDetector d) {
        this.detector = d;
        this.dataSet  = new FTDataSet(d);
    }
  
    public FTApplication(FTDetector d, String name) {
        this.appName  = name;
        this.detector = d;
        this.dataSet  = new FTDataSet(d);
        this.addCanvas(name);
    }
  
    public FTApplication(FTDetector d, String name, String... fields) {
        this.appName  = name;
        this.detector = d;
        this.dataSet  = new FTDataSet(d);
        this.addCanvas(name);
    } 
    
    public FTDetector getDetector() {
        return this.detector;
    }
    
    public String getName() {
        return this.appName;
    }
    
    public void setName(String name) {
        this.appName=name;
    }
    
    public final void addCanvas(String name) {
        EmbeddedCanvas c = new EmbeddedCanvas();
        this.canvases.add(c);
        this.canvases.get(this.canvases.size()-1).setName(name);
    }
    
    public EmbeddedCanvas getCanvas(int index) {
        return this.canvases.get(index);
    }
    
    public EmbeddedCanvas getCanvas(String name) {
        int index=0;
        for(int i=0; i<this.canvases.size(); i++) {
            if(this.canvases.get(i).getName() == name) {
                index=i;
                break;
            }
        }
        return this.canvases.get(index);
    }
    
    public void setRadioPane(JPanel radioPane) {
        this.radioPane = radioPane;
    }

    public JPanel getRadioPane() {
        return radioPane;
    }
    
    public String getButtonSelect() {
        return buttonSelect;
    }
    
    public int getButtonIndex() {
        return buttonIndex;
    }
    
    public String getCanvasSelect() {
        if(canvasSelect == null) {
            canvasIndex  = 0;
            canvasSelect = this.canvases.get(0).getName();
        }
        return canvasSelect;
    }
    
    public void setCanvasSelect(String name) {
        canvasIndex  = 0;
        canvasSelect = this.canvases.get(0).getName();
        for(int i=0; i<canvases.size(); i++) {
            if(canvases.get(i).getName() == name) {
                canvasIndex = i;
                canvasSelect = name;
                break;
            }
        }
    }
    
    public void setCanvasIndex(int index) {
        if(index>=0 && index < this.canvases.size()) {
            canvasIndex  = index;
            canvasSelect = this.canvases.get(index).getName();
        }
        else {
            canvasIndex  = 0;
            canvasSelect = this.canvases.get(0).getName();
        }
    }

    public FTDataSet getData() {
        return dataSet;
    }
        
    public void setRadioButtons() {
        this.radioPane.setLayout(new FlowLayout());
    	ButtonGroup bG = new ButtonGroup();
        for (String field : this.fields) {
//            System.out.println(field);
            String item = field;
            // add buttons named as "fields" to the button group and panel
            JRadioButton b = new JRadioButton(item);
            if(bG.getButtonCount()==0) b.setSelected(true);
            b.addActionListener(this);
            this.radioPane.add(b);
            bG.add(b);
        }
    }   
    
    public Color getColor(int key) {
        Color col = new Color(100, 100, 100);
        return col;
    }
        
    public void actionPerformed(ActionEvent e) {
//        System.out.println(this.getName() + " application radio button set to: " + e.getActionCommand());
        buttonSelect=e.getActionCommand();
        for(int i=0; i<this.fields.size(); i++) {
            if(buttonSelect == this.fields.get(i)) {
                buttonIndex=i;
                break;
            }
        }
    }
    
    public void resetCollections() {   
    }
    
    public void fitCollections() {
    }
    
    public void customizeFit(int key) {   
    }
    
    public void saveToFile(String hipoFileName) {
        
    }
        
}
