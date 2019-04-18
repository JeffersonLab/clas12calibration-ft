/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

/**
 *
 * @author devita
 */
public class FTCalConstants {
    
    
    // geometry constants
    public final double crystal_size     = 1.53;  //cm
    public final double crystal_distance = 189.8; //cm
    public final double crystal_length   = 20.0;  //cm                                                                                            
    

    // analysis realted info
    public final double nsPerSample  = 4;
    public final double LSB          = 0.4884;
    public final double eMips        = 15.3; //MeV    
    public final double chargeMips   = 6.005; //MeV
    public final double shower_depth = 6.5; //cm                                                                                                
    public final double light_speed  = 15.0; //cm/ns     

    // selection cuts
    public final double clusterTh   = 50.0;// Vertical selection
    public final double chargeThr   = 10.0;// Single channel selection MeV
    public final double signalThr   = 50.0;
    public final double clusterThr  = 500;// Vertical selection
    public final int    clusterSize = 3;// Vertical selection


    public FTCalConstants() {
        System.out.println("Constants loaded");
    }

}
