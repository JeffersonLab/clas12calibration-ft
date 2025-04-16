package org.clas.viewer;

import org.jlab.geom.prim.Point3D;

/**
 *
 * @author devita
 */
public class FTCalConstants {
    
    
    // geometry constants
    public final static double CRYSTALSIZE     = 1.53;  //cm
    public final static double CRYSTALDISTANCE = 189.8; //cm
    public final static double CRYSTALLENGTH   = 20.0;  //cm                                                                                            
    

    // analysis realted info
    public final static double NSPERSAMPLE  = 4;
    public final static double LSB          = 0.4883;
    public final static double EMIPS        = 15.3; //MeV    
    public final static double CHARGEMIPS   = 6.005; //MeV
    public final static double SHOWERDEPTH  = 6.5; //cm                                                                                                
    public final static double LIGHTSPEED   = 15.0; //cm/ns     
    public final static double DEFAULTEMIPS = 4.0; //MeV

    // selection cuts
    public final static double CHARGETHR   = 10.0;
    public final static double SIGNALTHR   = 50.0;// Single channel selection MeV
    public final static double CLUSTERTHR  = 500; // Vertical selection
    public final static int    CLUSTERSIZE = 3;   // Vertical selection
    public final static double PI0MINANGLE = 2.5; // minimum opening angle of two photons in pi0 analysis
    public final static double THETAMIN    = 2.5; // minimum angle for FT acceptance
    public final static double THETAMAX    = 4.5; // maximum angle for FT acceptance
    
    // target position
    public static double Z0 = -3.0; // cm
    public static double ZLENGTH = 28.0; // cm
    public static Point3D TARGET = new Point3D(0, 0, Z0);

    // vertex
    public static boolean VERTEXMODE = false; // use target if false or trigger particle vertex if true
    
    public FTCalConstants() {
        System.out.println("Constants loaded");
    }

    public static void setVertexMode(boolean mode) {
        VERTEXMODE = mode;
        System.out.println("Vertex mode set to = " + VERTEXMODE + ", FT particle vertex will be set based on " + (VERTEXMODE ? "trigger particle" : "target position"));
    }

    public static void setTargetZ(double z) {
        Z0 = z;
        TARGET = new Point3D(0, 0, Z0);
        System.out.println("Target position set to Z0 = " + Z0 + " (cm)");
    }
}
