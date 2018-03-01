
package org.clas.fthodo;

import java.lang.Math;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import java.util.ArrayList;

public class FTHodoHistograms {
    FTHodoHistParams HP=new FTHodoHistParams();
    //=================================
    //     HISTOGRAMS, GRAPHS
    //=================================
    //---------------
    // Event-by-Event
    // raw pulse
    DetectorCollection<H1F> H_FADC_RAW = new DetectorCollection<H1F>(); //  pulse
    DetectorCollection<H1F> H_FADC_RAW_PED = new DetectorCollection<H1F>(); //
    DetectorCollection<H1F> H_FADC_RAW_PUL = new DetectorCollection<H1F>(); //
//    DetectorCollection<GraphErrors> G_FADC_ANALYSIS = new DetectorCollection<GraphErrors>();
    DetectorCollection<H1F> H_FADC = new DetectorCollection<H1F>(); // baseline subtracted pulse calibrated to voltage and time
    DetectorCollection<H1F> H_VT = new DetectorCollection<H1F>();   // '' calibrated to no. photoelectrons and time
    DetectorCollection<H1F> H_NPE = new DetectorCollection<H1F>();    // Semi Accumulated
    DetectorCollection<H1F> H_PED_TEMP = new DetectorCollection<H1F>();     // Accumulated
    DetectorCollection<H1F> H_PED = new DetectorCollection<H1F>();
    DetectorCollection<GraphErrors> H_PED_VS_EVENT = new DetectorCollection<GraphErrors>();
    DetectorCollection<H1F> H_NOISE_V = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_MIP_V = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_NOISE_Q = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_MIP_Q = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_NPE_INT = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_NPE_NOISE = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_NPE_MATCH = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_T_MODE3 = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_T_MODE7 = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_DT_MODE7 = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_MIP_Q_MatchingTiles = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H_MIP_V_MatchingTiles = new DetectorCollection<H1F>();
    DetectorCollection<H2F> H_MAXV_VS_T = new DetectorCollection<H2F>();
    DetectorCollection<H2F> H_T1_T2 = new DetectorCollection<H2F>();
    // Fit Functions
    DetectorCollection<F1D> fPed = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQ2 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQMIP = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQMIPMatching = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fV2 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fVMIP = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fVMIPMatching = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fT = new DetectorCollection<F1D>();
    // Functions (that are not used to fit)
    DetectorCollection<F1D> fThr = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fVThr = new DetectorCollection<F1D>();
    DetectorCollection<Integer> dcHits = new DetectorCollection<Integer>();
    GraphErrors[] GGgainElectronicsV=new GraphErrors[15];
    GraphErrors[] GGgainElectronicsC=new GraphErrors[15];
    GraphErrors[][] GGgainDetectorV=new GraphErrors[2][8];
    GraphErrors[][] GGgainDetectorC=new GraphErrors[2][8];
    
    GraphErrors[] GGMIPgainElectronicsV=new GraphErrors[15];
    GraphErrors[] GGMIPgainElectronicsC=new GraphErrors[15];
    GraphErrors[][] GGMIPgainDetectorV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPgainDetectorC=new GraphErrors[2][8];
    GraphErrors[] GGMIPgainElectronics_matchingTilesV=new GraphErrors[15];
    GraphErrors[] GGMIPgainElectronics_matchingTilesC=new GraphErrors[15];
    GraphErrors[][] GGMIPgainDetector_matchingTilesV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPgainDetector_matchingTilesC=new GraphErrors[2][8];
    
    GraphErrors[] GGMIPsignElectronicsV=new GraphErrors[15];
    GraphErrors[] GGMIPsignElectronicsC=new GraphErrors[15];
    GraphErrors[][] GGMIPsignDetectorV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPsignDetectorC=new GraphErrors[2][8];
    GraphErrors[] GGMIPsignElectronics_matchingTilesV=new GraphErrors[15];
    GraphErrors[] GGMIPsignElectronics_matchingTilesC=new GraphErrors[15];
    GraphErrors[][] GGMIPsignDetector_matchingTilesV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPsignDetector_matchingTilesC=new GraphErrors[2][8];

    GraphErrors[] GGMIPDeltaEoverEElectronicsV=new GraphErrors[15];
    GraphErrors[] GGMIPDeltaEoverEElectronicsC=new GraphErrors[15];
    GraphErrors[][] GGMIPDeltaEoverEDetectorV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPDeltaEoverEDetectorC=new GraphErrors[2][8];
    GraphErrors[] GGMIPDeltaEoverEElectronics_matchingTilesV=new GraphErrors[15];
    GraphErrors[] GGMIPDeltaEoverEElectronics_matchingTilesC=new GraphErrors[15];
    GraphErrors[][] GGMIPDeltaEoverEDetector_matchingTilesV=new GraphErrors[2][8];
    GraphErrors[][] GGMIPDeltaEoverEDetector_matchingTilesC=new GraphErrors[2][8];
    
    H1F H_EMPTYGAIN_MV9 = null;
    H1F H_EMPTYGAIN_MV20= null;
    H1F H_EMPTYGAIN_PC9= null;
    H1F H_EMPTYGAIN_PC20=null;
    H1F H_EMPTYGAIN_ELE_PC=null;
    H1F H_EMPTYGAIN_ELE_MV=null;
    H1F H_EMPTYMIPGAIN_MV9 = null;
    H1F H_EMPTYMIPGAIN_MV20= null;
    H1F H_EMPTYMIPGAIN_PC9= null;
    H1F H_EMPTYMIPGAIN_PC20=null;
    H1F H_EMPTYMIPGAIN_ELE_PC=null;
    H1F H_EMPTYMIPGAIN_ELE_MV=null;
    H1F H_EMPTYMIPGAIN_matchingTiles_MV9 = null;
    H1F H_EMPTYMIPGAIN_matchingTiles_MV20= null;
    H1F H_EMPTYMIPGAIN_matchingTiles_PC9= null;
    H1F H_EMPTYMIPGAIN_matchingTiles_PC20=null;
    H1F H_EMPTYMIPGAIN_matchingTiles_ELE_PC=null;
    H1F H_EMPTYMIPGAIN_matchingTiles_ELE_MV=null;
   
    H1F H_EMPTYMIPSIGN_MV9 = null;
    H1F H_EMPTYMIPSIGN_MV20= null;
    H1F H_EMPTYMIPSIGN_PC9= null;
    H1F H_EMPTYMIPSIGN_PC20=null;
    H1F H_EMPTYMIPSIGN_ELE_PC=null;
    H1F H_EMPTYMIPSIGN_ELE_MV=null;
    H1F H_EMPTYMIPSIGN_matchingTiles_MV9 = null;
    H1F H_EMPTYMIPSIGN_matchingTiles_MV20= null;
    H1F H_EMPTYMIPSIGN_matchingTiles_PC9= null;
    H1F H_EMPTYMIPSIGN_matchingTiles_PC20=null;
    H1F H_EMPTYMIPSIGN_matchingTiles_ELE_PC=null;
    H1F H_EMPTYMIPSIGN_matchingTiles_ELE_MV=null;

    H1F H_EMPTYMIPDeltaEoverE_MV9 = null;
    H1F H_EMPTYMIPDeltaEoverE_MV20= null;
    H1F H_EMPTYMIPDeltaEoverE_PC9= null;
    H1F H_EMPTYMIPDeltaEoverE_PC20=null;
    H1F H_EMPTYMIPDeltaEoverE_ELE_PC=null;
    H1F H_EMPTYMIPDeltaEoverE_ELE_MV=null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_MV9 = null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_MV20= null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_PC9= null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_PC20=null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC=null;
    H1F H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV=null;

    H1F H_W_MAX = null;
    H1F H_V_MAX = null;
    H1F H_NPE_MAX = null;
    H1F H_CHARGE_MAX = null;
    //=================================
    //           CONSTANTS
    //=================================
    // n is for nominal
    final double nStatus = 5.0;
    final double nThrshNPE = 2.5; //put back at 2.5
    final double nPedMean = 200;
    final double nPedRMS = 10.0;
    final double nGain = 20.0;
    final double nErrGain = 0.0;
    final double nGain_mV = 10;
    final double nErrGain_mV = 0.0;
    final double nMipCThin = 700.;
    final double nMipCThick = 1500.;
    final double nTOff = 0.0;
    final double nTRes = 1.0;
    final double[] nMipE = {0.0, 1.2, 2.65};
    final double[] nMipQ = {0.0, 700., 1500.};
    int fADCBins = 4096;
    double voltageMax = 2000.;
    //double LSB = voltageMax / (1.0*fADCBins);
    double LSB = 0.4884;
    double vPedOffset = 5.0;
    double PedOffset = 10.0;
    //double triggerDelay = 125.0;
    double triggerDelay = 190.0;
    double MIPFitXminOffset=20.0; //x-axis offset for fitting MIP
    
    ////MALAKIES for coloring the detector should be replaced with TET
    final double thrshNoiseNPE = 0.5;
    double voltsPerSPE = 10.;
    double binsPerSPE = voltsPerSPE / LSB;
    double thrshVolts = nThrshNPE * voltsPerSPE;
    double noiseThrshV = thrshNoiseNPE * voltsPerSPE;
    double cosmicsThrsh = thrshVolts / LSB;
    double noiseThrsh = noiseThrshV / LSB;
    /////////////////////////////////
    public boolean toUseNewParamsPed=false;
    public boolean toUseNewParamsNoiseV=false;
    public boolean toUseNewParamsNoiseC=false;
    public boolean toUseNewParamsMIPV=false;
    public boolean toUseNewParamsMIPC=false;
    public boolean toUseNewParamsMIPVMT=false;
    public boolean toUseNewParamsMIPCMT=false;
    public ArrayList<Double> parsPed = new ArrayList<Double>();
    public double[] rangePed={0,0};
    public ArrayList<Double> parsNoiseV = new ArrayList<Double>();
    public double[] rangeNoiseV={0,0};
    public ArrayList<Double> parsNoiseC = new ArrayList<Double>();
    public double[] rangeNoiseC={0,0};
    public ArrayList<Double> parsMIPV = new ArrayList<Double>();
    public double[] rangeMIPV={0,0};
    public ArrayList<Double> parsMIPC = new ArrayList<Double>();
    public double[] rangeMIPC={0,0};
    public ArrayList<Double> parsMIPVMT = new ArrayList<Double>();
    public double[] rangeMIPVMT={0,0};
    public ArrayList<Double> parsMIPCMT = new ArrayList<Double>();
    public double[] rangeMIPCMT={0,0};
    
    boolean testMode = false;
    //==================
    // greater than 2.5 p.e.
    // (pe * v/pe * bins /v )
    // = 51
    double threshD = nThrshNPE*10.0/(LSB);
    public int threshold;
    //public int matchingTilesThreshold=600; // Threshold to look for match tiles above this in fADC channels.
    public int matchingTilesThreshold=50; // Threshold to look for match tiles above this in fADC channels.
    //public boolean SingleMatchedTile=true;
    public boolean SingleMatchedTile=false;
    public boolean ledAnalysis=false;
    double nsPerSample = 4.0;

    final boolean fitBackground = false;
    int NBinsCosmic = 300;
    //double CosmicQXMin[] = {0, 20.0 * nGain, 20.0 * nGain};
    double CosmicQXMin[] = {0, 0.0 * nGain, 0.0 * nGain};
    double CosmicQXMax[] = {10000, 2000, 2000};
    //double CosmicQXMax[] = {10000, 2000, 2000};
    final int nBinsVMIP = 200;
    //final double CosmicVXMin[] = {0, 20.0 * nGain_mV, 20.0 * nGain_mV};
    final double CosmicVXMin[] = {0, 0.0 * nGain_mV, 0.0 * nGain_mV};
    final int CosmicVXMax[] = {10000, 1000, 1000};
    //final int CosmicVXMax[] = {10000, 800, 800};
    final int CosmicNPEXMin[] = {0, 3, 5};
    final int CosmicNPEXMax[] = {200, 93, 133};
    boolean simulatedAnalysis = true;
    boolean useDefaultGain = false;
    //double NoiseQXMin[] = {0., 0.5 * nGain, 0.5 * nGain};
    double NoiseQXMin[] = {0., 0.0 * nGain, 0.0 * nGain};
    double NoiseQXMax[] = {310., 5.0 * nGain, 5.0 * nGain};
    //double NoiseVXMin[] = {0., 0.5 * nGain_mV, 0.5 * nGain_mV};
    double NoiseVXMin[] = {0., 0.0 * nGain_mV, 0.0 * nGain_mV};
    double NoiseVXMax[] = {0., 5.0 * nGain_mV, 5.0 * nGain_mV};
    int[] NBinsNoiseQ = {0, 110, 110};
    int[] NBinsNoiseV = {0, 90, 90};
    final int NBinsPed = 200;
    //pedestal min and max bin values for histogram
    int[] PedQX = {50, 500};
    double PedBinWidth = (PedQX[1] - PedQX[0]) / (1.0 * NBinsPed);
    final int nPointsPed = 200; //number of entries in the PedGraph
    double[] px_H_PED_VS_EVENT = new double[nPointsPed];
    double[] pex_H_PED_VS_EVENT = new double[nPointsPed];
    double[][][][] py_H_PED_VS_EVENT = new double[8][2][20][nPointsPed];
    double[][][][] pey_H_PED_VS_EVENT = new double[8][2][20][nPointsPed];
    double[] timeMin = {0., 100., 100.};
    double[] timeMax = {500., 150., 150.};
    
    private void setHistogramsHodo(int index) {
        char detector = 'h';
        HP.setAllParameters(index, detector);
        //----------------------------
        // Event-by-Event Histograms
        H_FADC.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_FADC",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 100.0));
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(4);
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC Amplitude (ped sub)");
        H_FADC_RAW.add(HP.getS(), HP.getL(), HP.getC(), new H1F(DetectorDescriptor.getName("H_FADC_RAW",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 100.0));
        H_FADC_RAW.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(4);
        H_FADC_RAW.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
        H_FADC_RAW.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC raw Amplitude");
        H_FADC_RAW_PED.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_FADC_RAW_PED",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 100.0));
        H_FADC_RAW_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
        H_FADC_RAW_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC raw Amplitude");
        H_FADC_RAW_PED.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(47);
        H_FADC_RAW_PUL.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_FADC_RAW_PUL",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 100.0));
        H_FADC_RAW_PUL.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
        H_FADC_RAW_PUL.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC raw Amplitude");
        H_FADC_RAW_PUL.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(46);
//        G_FADC_ANALYSIS.add(HP.getS(), HP.getL(), HP.getC(), new GraphErrors());
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).setTitle(DetectorDescriptor.getName("G_FADC_ANALYSIS",HP.getS(),HP.getL(),HP.getC())); //  title
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC raw Amplitude");
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).setMarkerColor(1); // color from 0-9 for given palette
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).setMarkerSize(2); // size in points on the screen
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).addPoint(0, 200, 0, 0);
//        G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).addPoint(100, 200, 0, 0);
        H_VT.add(HP.getS(), HP.getL(), HP.getC(), new H1F(DetectorDescriptor.getName("H_VT", HP.getS(), HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 400.0));
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Time (ns)");
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Voltage - ped sub (mV)");
        H_NPE.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_NPE", HP.getS(),HP.getL(), HP.getC()),HP.getTitle(), 100, 0.0, 400.0));
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Time (ns)");
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Photoelectrons (amp mV/spe mV");
        //----------------------------
        // Accumulated Histograms
        // PEDESTAL CANVAS
        H_PED.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_PED", HP.getS(),HP.getL(),HP.getC()), HP.getTitle(),NBinsPed*2, PedQX[0], PedQX[1]));
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Pedestal");
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        String namePed = HP.getTitle() + " for 100 events";
        String namePed2 = HP.getTitle() + " every 100 events";
        H_PED_TEMP.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_PED_TEMP",HP.getS(),HP.getL(),HP.getC()), namePed, NBinsPed, PedQX[0], PedQX[1]));
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Pedestal");
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        for (int i = 0; i < nPointsPed; i++) {
            px_H_PED_VS_EVENT[i] = i * 1.0;
            pex_H_PED_VS_EVENT[i] = 0.0;
            //py_H_PED_VS_EVENT[i] = 0.0;
            //pey_H_PED_VS_EVENT[i] = 0.0;
        }
        H_PED_VS_EVENT.add(HP.getS(), HP.getL(), HP.getC(),new GraphErrors(namePed2,px_H_PED_VS_EVENT, py_H_PED_VS_EVENT[HP.getS() - 1][HP.getL() - 1][HP.getC() - 1],  pex_H_PED_VS_EVENT, pey_H_PED_VS_EVENT[HP.getS() - 1][HP.getL() - 1][HP.getC() - 1]));
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitle(namePed);
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Event Index");
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Average Pedestal");
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setMarkerSize(5);
        
        H_MIP_Q.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("Cosmic Charge",HP.getS(),HP.getL(),HP.getC()),"Charge "+HP.getTitle(),NBinsCosmic,CosmicQXMin[HP.getL()],CosmicQXMax[HP.getL()]));
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_MIP_Q_MatchingTiles.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP Charge Matching Tiles",HP.getS(),HP.getL(),HP.getC()),"Charge "+HP.getTitle(),NBinsCosmic,CosmicQXMin[HP.getL()],CosmicQXMax[HP.getL()]));
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_NOISE_Q.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("Noise Charge",HP.getS(),HP.getL(),HP.getC()),"Charge "+HP.getTitle(),NBinsNoiseQ[HP.getL()],NoiseQXMin[HP.getL()],NoiseQXMax[HP.getL()]));
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_NPE_INT.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("NPE integrated",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(),100, 0, 100));
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(6);
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("npe (peak/gain)");
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_NPE_MATCH.add(HP.getS(), HP.getL(), HP.getC(), new H1F(DetectorDescriptor.getName("NPE int, matched layers",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(), 100,0,CosmicNPEXMax[HP.getL()]));
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(7);
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("npe (peak/gain)");
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_NOISE_V.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("WAVEMAX",HP.getS(), HP.getL(), HP.getC()),
                "maxV "+ HP.getTitle(), NBinsNoiseV[HP.getL()],NoiseVXMin[HP.getL()],NoiseVXMax[HP.getL()]));
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_MIP_V.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP WAVEMAX",HP.getS(),HP.getL(),HP.getC()),
                "maxV "+ HP.getTitle(), nBinsVMIP, CosmicVXMin[HP.getL()], CosmicVXMax[HP.getL()]));
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_MIP_V_MatchingTiles.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP WAVEMAX Matching Tiles",HP.getS(),HP.getL(),HP.getC()),
                "maxV "+HP.getTitle(), nBinsVMIP,CosmicVXMin[HP.getL()], CosmicVXMax[HP.getL()]));
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        H_T_MODE3.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(), 100, 0.0, 400));
        H_T_MODE3.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(4);
        H_T_MODE3.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 3 Time (ns)");
        H_T_MODE3.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
       
        // simulated data
        timeMin[1] = -45.0;
        timeMin[2] = -45.0;
        timeMax[1] = 45.0;
        timeMax[2] = 45.0;
        // paddle trigger
        timeMin[1] += triggerDelay;
        timeMin[2] += triggerDelay;
        timeMax[1] += triggerDelay;
        timeMax[2] += triggerDelay;
        H_MAXV_VS_T.add(HP.getS(),HP.getL(),HP.getC(), new H2F(DetectorDescriptor.getName("H_MAXV_VS_T",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(),64, timeMin[HP.getL()], timeMax[HP.getL()],64, 0., 2000.));
        H_MAXV_VS_T.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_MAXV_VS_T.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Peak Voltage (mV)");
//        H_T_MODE3.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(), 32, timeMin[HP.getL()], timeMax[HP.getL()]));
//        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
//        H_T_MODE3.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 3 (ns)");
//        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        H_T_MODE7.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE7",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(), 64, timeMin[HP.getL()], timeMax[HP.getL()]));
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
        H_T_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        if (HP.getL() == 1) {
            H_T1_T2.add(HP.getS(),HP.getL(),HP.getC(),new H2F(DetectorDescriptor.getName("H_T1_T2",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(),64, timeMin[1], timeMax[1],64, timeMin[2], timeMax[2]));
            H_T1_T2.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns) [thin layer]");
            H_T1_T2.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Mode 7 Time (ns) [thick layer]");
            
            H_DT_MODE7.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_DT_MODE7",HP.getS(), HP.getL(), HP.getC()),HP.getTitle(), 64, -15., 15.));
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
            H_DT_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time difference (ns)");
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        }
        
    }
    
    public void InitFunctions() {
        fPed = new DetectorCollection<F1D>();
        fQ2 = new DetectorCollection<F1D>();
        fQMIP = new DetectorCollection<F1D>();
        fQMIPMatching = new DetectorCollection<F1D>();
        fV2 = new DetectorCollection<F1D>();
        fVMIP = new DetectorCollection<F1D>();
        fVMIPMatching = new DetectorCollection<F1D>();
        fT = new DetectorCollection<F1D>();
        fThr = new DetectorCollection<F1D>();
        fVThr = new DetectorCollection<F1D>();
    }
    
    public void InitHistograms() {
        this.InitFunctions();
        for (int index = 0; index < 232; index++) {
            setHistogramsHodo(index);
        }
        
        H_W_MAX = new H1F("H_W_MAX", 504, 0, 504);
        H_V_MAX = new H1F("H_V_MAX", 504, 0, 2000);
        H_NPE_MAX = new H1F("H_NPE_MAX", 500, 0, 50);
        
        H_EMPTYGAIN_MV9=new H1F("H_EMPTYGAIN_MV9", 500, 0.0, 10);
        H_EMPTYGAIN_MV9.setTitleX("component");
        H_EMPTYGAIN_MV9.setTitleY("gain (mV)");
        H_EMPTYGAIN_MV9.setBinContent(1,20);
        H_EMPTYGAIN_MV20=new H1F("H_EMPTYGAIN_MV20", 500, 0.0, 21);
        H_EMPTYGAIN_MV20.setTitleX("component");
        H_EMPTYGAIN_MV20.setTitleY("gain (mV)");
        H_EMPTYGAIN_MV20.setBinContent(1,20);
        H_EMPTYGAIN_PC9=new H1F("H_EMPTYGAIN_PC9", 500, 0.0, 10);
        H_EMPTYGAIN_PC9.setTitleX("component");
        H_EMPTYGAIN_PC9.setTitleY("gain (pC)");
        H_EMPTYGAIN_PC9.setBinContent(1,40);
        H_EMPTYGAIN_PC20=new H1F("H_EMPTYGAIN_PC20", 500, 0.0, 21);
        H_EMPTYGAIN_PC20.setTitleX("component");
        H_EMPTYGAIN_PC20.setTitleY("gain (pC)");
        H_EMPTYGAIN_PC20.setBinContent(1,40);
        H_EMPTYGAIN_ELE_PC=new H1F("H_EMPTYGAIN_ELE_PC", 500, -1.0, 16);
        H_EMPTYGAIN_ELE_PC.setTitleX("component");
        H_EMPTYGAIN_ELE_PC.setTitleY("gain (pC)");
        H_EMPTYGAIN_ELE_PC.setBinContent(1,40);
        H_EMPTYGAIN_ELE_MV=new H1F("H_EMPTYGAIN_ELE_MV", 500, -1.0, 16);
        H_EMPTYGAIN_ELE_MV.setTitleX("component");
        H_EMPTYGAIN_ELE_MV.setTitleY("gain (mV)");
        H_EMPTYGAIN_ELE_MV.setBinContent(1,20);
        
        H_EMPTYMIPGAIN_MV9=new H1F("H_EMPTYGAIN_MV9", 500, 0.0, 10);
        H_EMPTYMIPGAIN_MV9.setTitleX("component");
        H_EMPTYMIPGAIN_MV9.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_MV9.setBinContent(1,150);
        H_EMPTYMIPGAIN_MV20=new H1F("H_EMPTYGAIN_MV20", 500, 0.0, 21);
        H_EMPTYMIPGAIN_MV20.setTitleX("component");
        H_EMPTYMIPGAIN_MV20.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_MV20.setBinContent(1,150);
        H_EMPTYMIPGAIN_PC9=new H1F("H_EMPTYMIPGAIN_PC9", 500, 0.0, 10);
        H_EMPTYMIPGAIN_PC9.setTitleX("component");
        H_EMPTYMIPGAIN_PC9.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_PC9.setBinContent(1,150);
        H_EMPTYMIPGAIN_PC20=new H1F("H_EMPTYMIPGAIN_PC20", 500, 0.0, 21);
        H_EMPTYMIPGAIN_PC20.setTitleX("component");
        H_EMPTYMIPGAIN_PC20.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_PC20.setBinContent(1,150);
        H_EMPTYMIPGAIN_ELE_PC=new H1F("H_EMPTYMIPGAIN_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPGAIN_ELE_PC.setTitleX("component");
        H_EMPTYMIPGAIN_ELE_PC.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_ELE_PC.setBinContent(1,150);
        H_EMPTYMIPGAIN_ELE_MV=new H1F("H_EMPTYMIPGAIN_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPGAIN_ELE_MV.setTitleX("component");
        H_EMPTYMIPGAIN_ELE_MV.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_ELE_MV.setBinContent(1,150);

        H_EMPTYMIPGAIN_matchingTiles_MV9=new H1F("H_EMPTYGAIN_matchingTiles_MV9", 500, 0.0, 10);
        H_EMPTYMIPGAIN_matchingTiles_MV9.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_MV9.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_matchingTiles_MV9.setBinContent(1,150);
        H_EMPTYMIPGAIN_matchingTiles_MV20=new H1F("H_EMPTYGAIN_matchingTiles_MV20", 500, 0.0, 21);
        H_EMPTYMIPGAIN_matchingTiles_MV20.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_MV20.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_matchingTiles_MV20.setBinContent(1,150);
        H_EMPTYMIPGAIN_matchingTiles_PC9=new H1F("H_EMPTYMIPGAIN_matchingTiles_PC9", 500, 0.0, 10);
        H_EMPTYMIPGAIN_matchingTiles_PC9.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_PC9.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_matchingTiles_PC9.setBinContent(1,150);
        H_EMPTYMIPGAIN_matchingTiles_PC20=new H1F("H_EMPTYMIPGAIN_matchingTiles_PC20", 500, 0.0, 21);
        H_EMPTYMIPGAIN_matchingTiles_PC20.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_PC20.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_matchingTiles_PC20.setBinContent(1,150);
        H_EMPTYMIPGAIN_matchingTiles_ELE_PC=new H1F("H_EMPTYMIPGAIN_matchingTiles_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPGAIN_matchingTiles_ELE_PC.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_ELE_PC.setTitleY("NPE (from charge)");
        H_EMPTYMIPGAIN_matchingTiles_ELE_PC.setBinContent(1,150);
        H_EMPTYMIPGAIN_matchingTiles_ELE_MV=new H1F("H_EMPTYMIPGAIN_matchingTiles_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPGAIN_matchingTiles_ELE_MV.setTitleX("component");
        H_EMPTYMIPGAIN_matchingTiles_ELE_MV.setTitleY("NPE (from mV)");
        H_EMPTYMIPGAIN_matchingTiles_ELE_MV.setBinContent(1,150);
        
        H_EMPTYMIPSIGN_MV9=new H1F("H_EMPTYSIGN_MV9", 500, 0.0, 10);
        H_EMPTYMIPSIGN_MV9.setTitleX("component");
        H_EMPTYMIPSIGN_MV9.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_MV9.setBinContent(1,1000);
        H_EMPTYMIPSIGN_MV20=new H1F("H_EMPTYSIGN_MV20", 500, 0.0, 21);
        H_EMPTYMIPSIGN_MV20.setTitleX("component");
        H_EMPTYMIPSIGN_MV20.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_MV20.setBinContent(1,1000);
        H_EMPTYMIPSIGN_PC9=new H1F("H_EMPTYMIPSIGN_PC9", 500, 0.0, 10);
        H_EMPTYMIPSIGN_PC9.setTitleX("component");
        H_EMPTYMIPSIGN_PC9.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_PC9.setBinContent(1,1000);
        H_EMPTYMIPSIGN_PC20=new H1F("H_EMPTYMIPSIGN_PC20", 500, 0.0, 21);
        H_EMPTYMIPSIGN_PC20.setTitleX("component");
        H_EMPTYMIPSIGN_PC20.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_PC20.setBinContent(1,1000);
        H_EMPTYMIPSIGN_ELE_PC=new H1F("H_EMPTYMIPSIGN_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPSIGN_ELE_PC.setTitleX("component");
        H_EMPTYMIPSIGN_ELE_PC.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_ELE_PC.setBinContent(1,1000);
        H_EMPTYMIPSIGN_ELE_MV=new H1F("H_EMPTYMIPSIGN_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPSIGN_ELE_MV.setTitleX("component");
        H_EMPTYMIPSIGN_ELE_MV.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_ELE_MV.setBinContent(1,1000);
        
        H_EMPTYMIPSIGN_matchingTiles_MV9=new H1F("H_EMPTYSIGN_matchingTiles_MV9", 500, 0.0, 10);
        H_EMPTYMIPSIGN_matchingTiles_MV9.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_MV9.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_matchingTiles_MV9.setBinContent(1,1000);
        H_EMPTYMIPSIGN_matchingTiles_MV20=new H1F("H_EMPTYSIGN_matchingTiles_MV20", 500, 0.0, 21);
        H_EMPTYMIPSIGN_matchingTiles_MV20.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_MV20.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_matchingTiles_MV20.setBinContent(1,1000);
        H_EMPTYMIPSIGN_matchingTiles_PC9=new H1F("H_EMPTYMIPSIGN_matchingTiles_PC9", 500, 0.0, 10);
        H_EMPTYMIPSIGN_matchingTiles_PC9.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_PC9.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_matchingTiles_PC9.setBinContent(1,1000);
        H_EMPTYMIPSIGN_matchingTiles_PC20=new H1F("H_EMPTYMIPSIGN_matchingTiles_PC20", 500, 0.0, 21);
        H_EMPTYMIPSIGN_matchingTiles_PC20.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_PC20.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_matchingTiles_PC20.setBinContent(1,1000);
        H_EMPTYMIPSIGN_matchingTiles_ELE_PC=new H1F("H_EMPTYMIPSIGN_matchingTiles_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPSIGN_matchingTiles_ELE_PC.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_ELE_PC.setTitleY("Charge (pC)");
        H_EMPTYMIPSIGN_matchingTiles_ELE_PC.setBinContent(1,1000);
        H_EMPTYMIPSIGN_matchingTiles_ELE_MV=new H1F("H_EMPTYMIPSIGN_matchingTiles_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPSIGN_matchingTiles_ELE_MV.setTitleX("component");
        H_EMPTYMIPSIGN_matchingTiles_ELE_MV.setTitleY("max V (mv)");
        H_EMPTYMIPSIGN_matchingTiles_ELE_MV.setBinContent(1,1000);
        
        H_EMPTYMIPDeltaEoverE_MV9=new H1F("H_EMPTYDeltaEoverE_MV9", 500, 0.0, 10);
        H_EMPTYMIPDeltaEoverE_MV9.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_MV9.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_MV9.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_MV20=new H1F("H_EMPTYDeltaEoverE_MV20", 500, 0.0, 21);
        H_EMPTYMIPDeltaEoverE_MV20.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_MV20.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_MV20.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_PC9=new H1F("H_EMPTYMIPDeltaEoverE_PC9", 500, 0.0, 10);
        H_EMPTYMIPDeltaEoverE_PC9.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_PC9.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_PC9.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_PC20=new H1F("H_EMPTYMIPDeltaEoverE_PC20", 500, 0.0, 21);
        H_EMPTYMIPDeltaEoverE_PC20.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_PC20.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_PC20.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_ELE_PC=new H1F("H_EMPTYMIPDeltaEoverE_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPDeltaEoverE_ELE_PC.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_ELE_PC.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_ELE_PC.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_ELE_MV=new H1F("H_EMPTYMIPDeltaEoverE_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPDeltaEoverE_ELE_MV.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_ELE_MV.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_ELE_MV.setBinContent(1,1.0);
        
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV9=new H1F("H_EMPTYDeltaEoverE_matchingTiles_MV9", 500, 0.0, 10);
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV9.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV9.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV9.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV20=new H1F("H_EMPTYDeltaEoverE_matchingTiles_MV20", 500, 0.0, 21);
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV20.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV20.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_matchingTiles_MV20.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC9=new H1F("H_EMPTYMIPDeltaEoverE_matchingTiles_PC9", 500, 0.0, 10);
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC9.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC9.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC9.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC20=new H1F("H_EMPTYMIPDeltaEoverE_matchingTiles_PC20", 500, 0.0, 21);
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC20.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC20.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_matchingTiles_PC20.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC=new H1F("H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC", 500, -1.0, 16);
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC.setTitleY("#Delta Charge/Charge");
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_PC.setBinContent(1,1.0);
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV=new H1F("H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV", 500, -1.0, 16);
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV.setTitleX("component");
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV.setTitleY("#Delta V/V");
        H_EMPTYMIPDeltaEoverE_matchingTiles_ELE_MV.setBinContent(1,1.0);
        
        
        for (int mezin = 0; mezin < 15; mezin++){
            GGgainElectronicsV[mezin]=new GraphErrors();
            GGgainElectronicsV[mezin].addPoint(0,0,0,0);
            GGgainElectronicsV[mezin].addPoint(16,0,0,0);
            GGgainElectronicsC[mezin]=new GraphErrors();
            GGgainElectronicsC[mezin].addPoint(0,0,0,0);
            GGgainElectronicsC[mezin].addPoint(16,0,0,0);
            GGMIPgainElectronicsV[mezin]=new GraphErrors();
            GGMIPgainElectronicsV[mezin].addPoint(0,0,0,0);
            GGMIPgainElectronicsV[mezin].addPoint(16,0,0,0);
            GGMIPgainElectronicsC[mezin]=new GraphErrors();
            GGMIPgainElectronicsC[mezin].addPoint(0,0,0,0);
            GGMIPgainElectronicsC[mezin].addPoint(16,0,0,0);
            GGMIPgainElectronics_matchingTilesV[mezin]=new GraphErrors();
            GGMIPgainElectronics_matchingTilesV[mezin].addPoint(0,0,0,0);
            GGMIPgainElectronics_matchingTilesV[mezin].addPoint(16,0,0,0);   
            GGMIPgainElectronics_matchingTilesC[mezin]=new GraphErrors();
            GGMIPgainElectronics_matchingTilesC[mezin].addPoint(0,0,0,0);
            GGMIPgainElectronics_matchingTilesC[mezin].addPoint(16,0,0,0);
            GGMIPsignElectronicsV[mezin]=new GraphErrors();
            GGMIPsignElectronicsV[mezin].addPoint(0,0,0,0);
            GGMIPsignElectronicsV[mezin].addPoint(16,0,0,0);
            GGMIPsignElectronicsC[mezin]=new GraphErrors();
            GGMIPsignElectronicsC[mezin].addPoint(0,0,0,0);
            GGMIPsignElectronicsC[mezin].addPoint(16,0,0,0);
            GGMIPsignElectronics_matchingTilesV[mezin]=new GraphErrors();
            GGMIPsignElectronics_matchingTilesV[mezin].addPoint(0,0,0,0);
            GGMIPsignElectronics_matchingTilesV[mezin].addPoint(16,0,0,0);
            GGMIPsignElectronics_matchingTilesC[mezin]=new GraphErrors();
            GGMIPsignElectronics_matchingTilesC[mezin].addPoint(0,0,0,0);
            GGMIPsignElectronics_matchingTilesC[mezin].addPoint(16,0,0,0);
            
            GGMIPDeltaEoverEElectronicsV[mezin]=new GraphErrors();
            GGMIPDeltaEoverEElectronicsV[mezin].addPoint(0,0,0,0);
            GGMIPDeltaEoverEElectronicsV[mezin].addPoint(16,0,0,0);
            GGMIPDeltaEoverEElectronicsC[mezin]=new GraphErrors();
            GGMIPDeltaEoverEElectronicsC[mezin].addPoint(0,0,0,0);
            GGMIPDeltaEoverEElectronicsC[mezin].addPoint(16,0,0,0);
            GGMIPDeltaEoverEElectronics_matchingTilesV[mezin]=new GraphErrors();
            GGMIPDeltaEoverEElectronics_matchingTilesV[mezin].addPoint(0,0,0,0);
            GGMIPDeltaEoverEElectronics_matchingTilesV[mezin].addPoint(16,0,0,0);
            GGMIPDeltaEoverEElectronics_matchingTilesC[mezin]=new GraphErrors();
            GGMIPDeltaEoverEElectronics_matchingTilesC[mezin].addPoint(0,0,0,0);
            GGMIPDeltaEoverEElectronics_matchingTilesC[mezin].addPoint(16,0,0,0);
            
            
        }
        for (int lay = 0; lay < 2; lay++){
            for (int sec = 0; sec < 8; sec++){
                GGgainDetectorV[lay][sec]=new GraphErrors();
                GGgainDetectorV[lay][sec].addPoint(0,0,0,0);
                GGgainDetectorC[lay][sec]=new GraphErrors();
                GGgainDetectorC[lay][sec].addPoint(0,0,0,0);
                GGMIPgainDetectorV[lay][sec]=new GraphErrors();
                GGMIPgainDetectorV[lay][sec].addPoint(0,0,0,0);
                GGMIPgainDetectorC[lay][sec]=new GraphErrors();
                GGMIPgainDetectorC[lay][sec].addPoint(0,0,0,0);
                GGMIPgainDetector_matchingTilesV[lay][sec]=new GraphErrors();
                GGMIPgainDetector_matchingTilesV[lay][sec].addPoint(0,0,0,0);
                GGMIPgainDetector_matchingTilesC[lay][sec]=new GraphErrors();
                GGMIPgainDetector_matchingTilesC[lay][sec].addPoint(0,0,0,0);
                GGMIPsignDetectorV[lay][sec]=new GraphErrors();
                GGMIPsignDetectorV[lay][sec].addPoint(0,0,0,0);
                GGMIPsignDetectorC[lay][sec]=new GraphErrors();
                GGMIPsignDetectorC[lay][sec].addPoint(0,0,0,0);
                GGMIPsignDetector_matchingTilesV[lay][sec]=new GraphErrors();
                GGMIPsignDetector_matchingTilesV[lay][sec].addPoint(0,0,0,0);
                GGMIPsignDetector_matchingTilesC[lay][sec]=new GraphErrors();
                GGMIPsignDetector_matchingTilesC[lay][sec].addPoint(0,0,0,0);
                
                GGMIPDeltaEoverEDetectorV[lay][sec]=new GraphErrors();
                GGMIPDeltaEoverEDetectorV[lay][sec].addPoint(0,0,0,0);
                GGMIPDeltaEoverEDetectorC[lay][sec]=new GraphErrors();
                GGMIPDeltaEoverEDetectorC[lay][sec].addPoint(0,0,0,0);
                GGMIPDeltaEoverEDetector_matchingTilesV[lay][sec]=new GraphErrors();
                GGMIPDeltaEoverEDetector_matchingTilesV[lay][sec].addPoint(0,0,0,0);
                GGMIPDeltaEoverEDetector_matchingTilesC[lay][sec]=new GraphErrors();
                GGMIPDeltaEoverEDetector_matchingTilesC[lay][sec].addPoint(0,0,0,0);
                
                
                if (sec%2==0){
                    GGgainDetectorV[lay][sec].addPoint(10,0,0,0);
                    GGgainDetectorC[lay][sec].addPoint(10,0,0,0);
                    GGMIPgainDetectorV[lay][sec].addPoint(10,0,0,0);
                    GGMIPgainDetectorC[lay][sec].addPoint(10,0,0,0);
                    GGMIPgainDetector_matchingTilesV[lay][sec].addPoint(10,0,0,0);
                    GGMIPgainDetector_matchingTilesC[lay][sec].addPoint(10,0,0,0);
                    GGMIPsignDetectorV[lay][sec].addPoint(10,0,0,0);
                    GGMIPsignDetectorC[lay][sec].addPoint(10,0,0,0);
                    GGMIPsignDetector_matchingTilesV[lay][sec].addPoint(10,0,0,0);
                    GGMIPsignDetector_matchingTilesC[lay][sec].addPoint(10,0,0,0);
                    
                    GGMIPDeltaEoverEDetectorV[lay][sec].addPoint(10,0,0,0);
                    GGMIPDeltaEoverEDetectorC[lay][sec].addPoint(10,0,0,0);
                    GGMIPDeltaEoverEDetector_matchingTilesV[lay][sec].addPoint(10,0,0,0);
                    GGMIPDeltaEoverEDetector_matchingTilesC[lay][sec].addPoint(10,0,0,0);
                }
                else {
                    GGgainDetectorV[lay][sec].addPoint(21,0,0,0);
                    GGgainDetectorC[lay][sec].addPoint(21,0,0,0);
                    GGMIPgainDetectorV[lay][sec].addPoint(21,0,0,0);
                    GGMIPgainDetectorC[lay][sec].addPoint(21,0,0,0);
                    GGMIPgainDetector_matchingTilesV[lay][sec].addPoint(21,0,0,0);
                    GGMIPgainDetector_matchingTilesC[lay][sec].addPoint(21,0,0,0);
                    GGMIPsignDetectorV[lay][sec].addPoint(21,0,0,0);
                    GGMIPsignDetectorC[lay][sec].addPoint(21,0,0,0);
                    GGMIPsignDetector_matchingTilesV[lay][sec].addPoint(21,0,0,0);
                    GGMIPsignDetector_matchingTilesC[lay][sec].addPoint(21,0,0,0);
                    
                    GGMIPDeltaEoverEDetectorV[lay][sec].addPoint(21,0,0,0);
                    GGMIPDeltaEoverEDetectorC[lay][sec].addPoint(21,0,0,0);
                    GGMIPDeltaEoverEDetector_matchingTilesV[lay][sec].addPoint(21,0,0,0);
                    GGMIPDeltaEoverEDetector_matchingTilesC[lay][sec].addPoint(21,0,0,0);
                }
            }
        }
    }

    
    public void fitHistograms() {
        System.out.println(" Fitting Histograms");
        String fitOption = "NRQ";
        // Do the fitting for all components
        for (int index = 0; index < 232; index++) {
            HP.setAllParameters(index, 'h');
            fitPedestals(HP.getS(), HP.getL(), HP.getC(), fitOption);
            fitVNoise(HP.getS(), HP.getL(), HP.getC(), fitOption);
            fitQNoise(HP.getS(), HP.getL(), HP.getC(), fitOption);
            fitVMIP(HP.getS(), HP.getL(),HP.getC(), fitOption);
            fitQMIP(HP.getS(), HP.getL(),HP.getC(), fitOption);
            fitVMIPMatching(HP.getS(), HP.getL(),HP.getC(), fitOption);
            fitQMIPMatching(HP.getS(), HP.getL(),HP.getC(), fitOption);
            //fitT(HP.getS(), HP.getL(), HP.getC(), fitOption);
        }
        System.out.println(" Fitting Histograms complete");
    }
    
    private void fitPedestals(int s, int l, int c, String fitOption) {
        if (testMode)  System.out.println(" Fitting Pedestal (S,L,C) = ("+ s + "," + l + "," + c + ")");
        
        if (initFitPedestalParameters(s, l, c, H_PED.get(s, l, c), toUseNewParamsPed)) {
            DataFitter.fit(fPed.get(s, l, c), H_PED.get(s, l, c), fitOption);
            H_PED.get(s, l, c).setFunction(null);
            if (testMode) {
                System.out.println(" Fitted Pedestal (S,L,C) = ("+ s + "," + l + "," + c + ")");
            }
        } else if (testMode) {
            System.out.println(" No Pedestal Fit (S,L,C) = (" + s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitPedestalParameters(int s, int l, int c, H1F H1, boolean newvars) {
        //double ampl=0.0, mean=0.0,sigma=0.0,min=0.0, max=0.0;
        if (H1.integral() > 100) {
            double ampl = H1.getBinContent(H1.getMaximumBin());
            double mean = H1.getMaximumBin();
            mean = mean * H1.getAxis().getBinWidth(2);
            mean = mean + H1.getAxis().min();
            double sigma=1.0;
            double min=130.;
            double max=440.;
            if (newvars){
                ampl = parsPed.get(0);
                mean = parsPed.get(1);
                sigma = parsPed.get(2);
                min=rangePed[0];
                max=rangePed[1];
            }
            fPed.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", min, max));
            fPed.get(s, l, c).setLineColor(2);
            fPed.get(s, l, c).setLineWidth(2);
            fPed.get(s, l, c).setParameter(0, ampl);
            fPed.get(s, l, c).setParameter(1, mean);
            fPed.get(s, l, c).setParameter(2, sigma);
            fPed.get(s, l, c).setParLimits(0,0.1 * ampl,2.0 * ampl);
            fPed.get(s, l, c).setParLimits(1, 0.5*mean, 1.5*mean);
            fPed.get(s, l, c).setParLimits(2, 0, 3*sigma);
            return true;
        } else {
            return false;
        }
    }

    private void fitVNoise(int s, int l, int c, String fitOption) {
        if (testMode)
            System.out.println(" Fitting V Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitNoiseParameters(s, l, c, H_NOISE_V.get(s, l, c), fV2,toUseNewParamsNoiseV, "mV" )) {
            if (testMode)
                System.out.println(" Fitting Voltage Noise (fV2) ");
            DataFitter.fit(fV2.get(s, l, c), H_NOISE_V.get(s, l, c), fitOption);
            H_NOISE_V.get(s, l, c).setFunction(null);
            if (testMode){
                System.out.println(" Fitted V Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
            }
        } else if (testMode) {
            System.out.println(" Not enough entries to fit Voltage Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    private void fitQNoise(int s, int l, int c, String fitOption) {
        if (testMode)
            System.out.println(" Fitting Q Noise(S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitNoiseParameters(s, l, c, H_NOISE_Q.get(s, l, c), fQ2, toUseNewParamsNoiseC, "pC" )) {
            DataFitter.fit(fQ2.get(s, l, c), H_NOISE_Q.get(s, l, c), fitOption);
            H_NOISE_Q.get(s, l, c).setFunction(null);
            if (testMode)
                System.out.println(" Fitted Q Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else if (testMode) {
        System.out.println(" ot enough entries to fit charge Noise (S,L,C) =(" + s + "," + l + "," + c + ")");
    }
}
    
    private boolean initFitNoiseParameters(int s, int l, int c, H1F H1, DetectorCollection<F1D> DCFunc, boolean newvars, String VoltChar) {
        if (testMode) System.out.println(" initFitNoiseParameters start ");
        //double ampl1=0.0,mean1=0.0,std1=0.0, ampl2=0.0,mean2=0.0,std2=0.0,exp0=0.0, exp1=0.0, min=0.0, max=0.0;
        String volt="mV";
        String charge="pC";
        double ampl1 = H1.getBinContent(H1.getMaximumBin());
        double mean1 = H1.getMaximumBin();
        mean1 = mean1 * H1.getAxis().getBinWidth(2);
        mean1 = mean1 + H1.getAxis().min();
        double std1 = 0.5;
        double ampl2=ampl1/6.0;
        double mean2=2.0*mean1;
        double std2=std1;
        double exp0 = H1.getBinContent(1) + H1.getBinContent(2);
        double exp1=-0.2;
        double min=H1.getAxis().min();
        double max=H1.getAxis().max();
        if (newvars && VoltChar==volt){
            ampl1 =parsNoiseV.get(0);
            mean1 =parsNoiseV.get(1);
            std1 =parsNoiseV.get(2);
            ampl2=parsNoiseV.get(3);
            mean2=parsNoiseV.get(4);
            std2=parsNoiseV.get(5);
            exp0=parsNoiseV.get(6);
            exp1=parsNoiseV.get(7);
            min=rangeNoiseV[0];
            max=rangeNoiseV[1];
        }
        else if (newvars && VoltChar==charge){
            ampl1 =parsNoiseC.get(0);
            mean1 =parsNoiseC.get(1);
            std1 =parsNoiseC.get(2);
            ampl2=parsNoiseC.get(3);
            mean2=parsNoiseC.get(4);
            std2=parsNoiseC.get(5);
            exp0=parsNoiseC.get(6);
            exp1=parsNoiseC.get(7);
            min=rangeNoiseC[0];
            max=rangeNoiseC[1];
        }
        if (testMode) System.out.println(" initFitNoiseParameters variables initialised ");
        if (H1.getEntries() > 250) {
            if (testMode) System.out.println(" initFitNoiseParameters setting fV2 parameters ");
            DCFunc.add(s, l, c, new F1D("gaus", "[amp1]*gaus(x,[mean1],[sigma1])+[amp2]*gaus(x,[mean2],[sigma2])+[amp3]*exp(x*[scale])",min, max));
            DCFunc.get(s, l, c).setLineColor(2);
            DCFunc.get(s, l, c).setLineWidth(2);
            DCFunc.get(s, l, c).setParameter(0, ampl1);
            DCFunc.get(s, l, c).setParameter(1, mean1);
            DCFunc.get(s, l, c).setParameter(2, std1);
            DCFunc.get(s, l, c).setParameter(3, ampl2);
            DCFunc.get(s, l, c).setParameter(4, mean2);
            DCFunc.get(s, l, c).setParameter(5, std2);
            DCFunc.get(s, l, c).setParameter(6, exp0);
            DCFunc.get(s, l, c).setParameter(7, exp1);
            if (testMode) System.out.println(" initFitNoiseParameters setting fV2 limits ");
            DCFunc.get(s, l, c).setParLimits(0, 0., ampl1 * 2);
            DCFunc.get(s, l, c).setParLimits(1, H1.getAxis().min(), 1.5 * mean1);
            DCFunc.get(s, l, c).setParLimits(2, std1/6, std1 * 5.0);
            DCFunc.get(s, l, c).setParLimits(3, 0.0, ampl1);
            DCFunc.get(s, l, c).setParLimits(4,1.2 * mean1,3.0 * mean1);
            DCFunc.get(s, l, c).setParLimits(5, std1/6, std1 * 5.0);
            DCFunc.get(s, l, c).setParLimits(6, 0.1 * exp0, 10.0 * exp0+50);
            DCFunc.get(s, l, c).setParLimits(7, -10.0, 0);
            return true;
        } else {
            if (testMode) System.out.println(" initFitNoiseParameters insufficient entries ");
            return false;
        }
    }
    
    private void fitVMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_V.get(s, l, c), fVMIP, toUseNewParamsMIPV, "mV")) {
            DataFitter.fit(fVMIP.get(s, l, c), H_MIP_V.get(s, l, c), fitOption);
            H_MIP_V.get(s, l, c).setFunction(null);
            if (testMode) System.out.println(" Fitted V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No V MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }

    private void fitQMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_Q.get(s, l, c), fQMIP,toUseNewParamsMIPC, "pC")) {
            DataFitter.fit(fQMIP.get(s, l, c), H_MIP_Q.get(s, l, c), fitOption);
            H_MIP_Q.get(s, l, c).setFunction(null);

            if (testMode) System.out.println(" Fitted Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No Q MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }

    
    private void fitVMIPMatching(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting V MIP Matching (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_V_MatchingTiles.get(s, l, c), fVMIPMatching, toUseNewParamsMIPVMT, "mVMT")) {
            DataFitter.fit(fVMIPMatching.get(s, l, c), H_MIP_V_MatchingTiles.get(s, l, c), fitOption);
            H_MIP_V_MatchingTiles.get(s, l, c).setFunction(null);
            if (testMode) System.out.println(" Fitted V MIP Matching (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No V MIP Fit Matching (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    
    private void fitQMIPMatching(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting Q MIP Matching (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_Q_MatchingTiles.get(s, l, c), fQMIPMatching, toUseNewParamsMIPCMT, "pCMT")) {
            DataFitter.fit(fQMIPMatching.get(s, l, c), H_MIP_Q_MatchingTiles.get(s, l, c), fitOption);
            H_MIP_Q_MatchingTiles.get(s, l, c).setFunction(null);
            if (testMode) System.out.println(" Fitted Q MIP Matching (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No Q MIP MatchingFit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    
    private boolean initFitMIPParameters(int s, int l, int c, H1F H1,DetectorCollection<F1D> DCFunc, boolean newvars, String VoltCharMT) {
        //double min=0.0, max=0.0, ampl=0.0, mean=0.0, gamma=0.0, exp0=0.0, exp1=0.0;
        String volt="mV";
        String charge="pC";
        String voltMT="mVMT";
        String chargeMT="pCMT";
        double mean= H1.getMean();
        double ampl = H1.getBinContent(H1.getXaxis().getBin(mean)); //set as starting amplitude the value of the bin at mean
        double gamma = H1.getRMS();
        double exp0 = H1.getBinContent(H1.getMaximumBin()); //set as starting amplitude of exponential the value of the bin at xmin
        double exp1=-0.001;
        double min=H1.getAxis().min();
        double max=H1.getAxis().max();
        if (newvars && VoltCharMT ==volt){
            ampl = parsMIPV.get(0);
            mean = parsMIPV.get(1);
                gamma = parsMIPV.get(2);
            if (!(ledAnalysis)){
                exp0 = parsMIPV.get(3);
                exp1= parsMIPV.get(4);
            }
                min= rangeMIPV[0];
                max=rangeMIPV[1];
        }
        else if (newvars && VoltCharMT ==charge){
            ampl = parsMIPC.get(0);
            mean = parsMIPC.get(1);
            gamma = parsMIPC.get(2);
            if (!(ledAnalysis)){
                exp0 = parsMIPC.get(3);
                exp1= parsMIPC.get(4);
            }
            min= rangeMIPC[0];
            max=rangeMIPC[1];
        }
        else if (newvars && VoltCharMT ==voltMT){
            ampl = parsMIPVMT.get(0);
            mean = parsMIPVMT.get(1);
            gamma = parsMIPVMT.get(2);
            if (!(ledAnalysis)){
                exp0 = parsMIPVMT.get(3);
                exp1= parsMIPVMT.get(4);
            }
            min= rangeMIPVMT[0];
            max=rangeMIPVMT[1];
        }
        else if (newvars && VoltCharMT ==chargeMT){
            ampl = parsMIPCMT.get(0);
            mean = parsMIPCMT.get(1);
            gamma = parsMIPCMT.get(2);
            if (!(ledAnalysis)){
                exp0 = parsMIPCMT.get(3);
                exp1= parsMIPCMT.get(4);
            }
            min= rangeMIPCMT[0];
            max=rangeMIPCMT[1];
        }
        
        
        if (H1.integral() > 100) {
            if (!(ledAnalysis)){
                DCFunc.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])+[amp2]*exp(x*[scale])",min, max));
                //DCFunc.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])+[amp2]*exp(x*[scale])",H1.getAxis().min()+MIPFitXminOffset,H1.getAxis().max()));
                //DCFunc.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])",H1.getAxis().min(),H1.getAxis().max()));
                DCFunc.get(s, l, c).setLineColor(2);
                DCFunc.get(s, l, c).setLineWidth(2);
                DCFunc.get(s, l, c).setParameter(0, ampl);
                DCFunc.get(s, l, c).setParameter(1, mean);
                DCFunc.get(s, l, c).setParameter(2, gamma);
                DCFunc.get(s, l, c).setParameter(3, exp0);
                DCFunc.get(s, l, c).setParameter(4, exp1);
                
                DCFunc.get(s, l, c).setParLimits(0, 0, ampl * 100.0);
                DCFunc.get(s, l, c).setParLimits(1, H1.getAxis().min(),H1.getAxis().max());
                DCFunc.get(s, l, c).setParLimits(2, gamma/10, gamma*10);
                DCFunc.get(s, l, c).setParLimits(3, exp0 * 0.01, exp0 * 100.0);
                DCFunc.get(s, l, c).setParLimits(4, -1.0, 0);
            } else if ((ledAnalysis)){
                DCFunc.add(s, l, c, new F1D("gaus", "[amp1]*gaus(x,[mean1],[sigma1])",H1.getAxis().min(),H1.getAxis().max()));
                DCFunc.get(s, l, c).setLineColor(2);
                DCFunc.get(s, l, c).setLineWidth(2);
                DCFunc.get(s, l, c).setParameter(0, ampl);
                DCFunc.get(s, l, c).setParameter(1, mean);
                DCFunc.get(s, l, c).setParameter(2, gamma);
                DCFunc.get(s, l, c).setParLimits(0, ampl * 0.1, ampl * 10.0);
                DCFunc.get(s, l, c).setParLimits(1, H1.getAxis().min(),H1.getAxis().max());
                DCFunc.get(s, l, c).setParLimits(2, gamma/10, gamma*10);
            }
            return true;
        } else {
            return false;
        }
    }

    
    public void initThresholdParameters(int s, int l, int c) {
        if (fThr.hasEntry(s, l, c)){
            fThr.get(s, l, c).setParameter(0, threshold+this.PedOffset);
            fThr.get(s, l, c).setLineColor(2);
        }else {
            fThr.add(s, l, c, new F1D("p01", "[p0]", 0., 100.));
            fThr.get(s, l, c).setParameter(0, threshold+this.PedOffset);
            fThr.get(s, l, c).setLineColor(2);
        }
        double vTh = threshold * LSB+ this.vPedOffset;
        if (fVThr.hasEntry(s, l, c)){
            fVThr.get(s, l, c).setParameter(0, vTh);
            fVThr.get(s, l, c).setLineColor(2);
        }else{
            fVThr.add(s, l, c, new F1D("p02", "[p0]", 0., 400.));
            fVThr.get(s, l, c).setParameter(0, vTh);
            fVThr.get(s, l, c).setLineColor(2);
        }
    }
    public void initConstants() {
        double mipE = 0.0;
        double mipC = 0.0;
        for (int index = 0; index < 232; index++) {
            HP.setAllParameters(index, 'h');
            mipE = 2.0;
            mipC = 1000.0;
            if (HP.getL() == 1) {
                mipE = mipE * 0.7;
                mipC = mipC * 0.7;
            } else if (HP.getL() == 2) {
                mipE = mipE * 1.5;
                mipC = mipC * 1.5;
            }
            initThresholdParameters(HP.getS(), HP.getL(), HP.getC());
        }
    }

    
    
    
    
}
