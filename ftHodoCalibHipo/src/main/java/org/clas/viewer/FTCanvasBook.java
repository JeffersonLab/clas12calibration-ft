/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasGroup;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public class FTCanvasBook extends JPanel implements ActionListener {

    private EmbeddedCanvas canvas = new EmbeddedCanvas();
    private int     padsPerPage = 9;
    private int     currentPage = 0;
    private int        maxPages = 1;
    private List<DataGroup> canvasDataSets = new ArrayList<DataGroup>();
    JLabel  progressLabel = null;
    
    public FTCanvasBook(){
        super();
        setLayout(new BorderLayout());
        JPanel buttonsPanel = new JPanel();
        JButton buttonPrev = new JButton("<");
        this.progressLabel = new JLabel("0/0");
        JButton buttonNext = new JButton(">");
        buttonPrev.addActionListener(this);
        buttonNext.addActionListener(this);
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(buttonPrev);
        buttonsPanel.add(this.progressLabel);
        buttonsPanel.add(buttonNext);
        add(this.canvas,BorderLayout.CENTER);
        add(buttonsPanel,BorderLayout.PAGE_END);
    }
    
    
    public void setData(IndexedList<DataGroup> dataGroups, int iPad){
        this.canvasDataSets.clear();
        Map<Long, DataGroup> map = dataGroups.getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            List<IDataSet> dsList = group.getData(iPad);
            DataGroup newGroup = new DataGroup(1,1);
            for(IDataSet ds : dsList) newGroup.addDataSet(ds, 0);
            this.canvasDataSets.add(newGroup);
        }
        this.currentPage = 0;
        this.maxPages = this.canvasDataSets.size()/this.padsPerPage;
        if(maxPages*this.padsPerPage<this.canvasDataSets.size()){
            this.maxPages++;
        }
        this.updateCanvas();
    }

        public void setDataConstants(IndexedList<DataGroup> dataGroups, int iPad){
        this.canvasDataSets.clear();
        Map<Long, DataGroup> map = dataGroups.getMap();
       
        String Sec= "-1";
        String Lay="-1";
        String tempSec= "-1";
        String tempLay="-1";
        int numbGraphs=0;
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            List<IDataSet> dsList = group.getData(iPad);
            DataGroup newGroup = new DataGroup(1,1);
            int dataentries=0;
            for(IDataSet ds : dsList){
                //System.out.println("Name: "+ds.getName());
                String TitleSecLay[]= ds.getName().split("_");
                tempSec=TitleSecLay[1];
                tempLay=TitleSecLay[2];
                if (Objects.equals(tempSec, Sec) && Objects.equals(tempLay, Lay) && numbGraphs>2){
                    continue;
                }
                else if (!Objects.equals(tempSec, Sec) || !Objects.equals(tempLay, Lay)&& numbGraphs!=0)
                    numbGraphs=0;
                numbGraphs++;
                Sec=tempSec;
                Lay=tempLay;
                newGroup.addDataSet(ds, 0);
                dataentries++;
            }
            if (dataentries!=0)
                this.canvasDataSets.add(newGroup);
        }
        this.currentPage = 0;
        this.maxPages = this.canvasDataSets.size()/this.padsPerPage;
        if(maxPages*this.padsPerPage<this.canvasDataSets.size()){
            this.maxPages++;
        }
        this.updateCanvas();
    }
    
    
    
    
    
    
    
    
    public List<DataGroup> getCanvasDataSets() {
        return canvasDataSets;
    }
        
    public void updateCanvas(){
        this.canvas.clear();
        this.canvas.divide(3, 3);
        this.canvas.setGridX(false);
        this.canvas.setGridY(false);
        for(int i = 0; i < this.padsPerPage; i++){
            int index = currentPage*this.padsPerPage + i;
            this.canvas.cd(i);
            if(index<this.canvasDataSets.size()){
                DataGroup group = this.canvasDataSets.get(index);
                int nrows = group.getRows();
                int ncols = group.getColumns();
                int nds   = nrows*ncols;
                for(int j = 0; j < nds;j++){
                    List<IDataSet> dsList = group.getData(j);
                    for(IDataSet ds : dsList){
                        if(ds!=null) this.canvas.draw(ds,"same");
                    }
                }
            }
        }
        this.progressLabel.setText(String.format("%d/%d", this.currentPage+1,this.maxPages));
    }
    
    public void nextPage(){
        if((currentPage+1)<maxPages){
            currentPage++;
            this.updateCanvas();
        }
    }
    
    public void previousPage(){
        if(currentPage>0){
            currentPage--;
            this.canvas.clear();
            this.updateCanvas();
        }
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("<")==0){
            this.previousPage();
        }
        if(e.getActionCommand().compareTo(">")==0){
            this.nextPage();
        }
    }
     public static void main(String[] args){
        JFrame frame = new JFrame();
        EmbeddedCanvasGroup canvasTab = new EmbeddedCanvasGroup();
        //EmbeddedCanvasTabbed canvasTab = new EmbeddedCanvasTabbed();
        frame.add(canvasTab);
        frame.pack();
        frame.setMinimumSize(new Dimension(300,300));
        frame.setVisible(true);
    }
}

