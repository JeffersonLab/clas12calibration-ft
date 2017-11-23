/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fthodo;

import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;

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

    // 2D
    DetectorCollection<H2F> H_MAXV_VS_T = new DetectorCollection<H2F>();
    DetectorCollection<H2F> H_T1_T2 = new DetectorCollection<H2F>();
    // 1D
    DetectorCollection<H1F> H_COSMIC_fADC = new DetectorCollection<H1F>();
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
    
    H1F H_EMPTYGAIN_MV9 = null;
    H1F H_EMPTYGAIN_MV20= null;
    H1F H_EMPTYGAIN_PC9= null;
    H1F H_EMPTYGAIN_PC20=null;

    H1F H_EMPTYGAIN_ELE_PC=null;
    H1F H_EMPTYGAIN_ELE_MV=null;

    H1F H_W_MAX = null;
    H1F H_V_MAX = null;
    H1F H_NPE_MAX = null;
    H1F H_CHARGE_MAX = null;
    //=================================
    //           CONSTANTS
    //=================================
    // n is for nominal
    final double nStatus = 5.0;
    final double nThrshNPE = 2.5;
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
    double triggerDelay = 190.0;
    final double thrshNoiseNPE = 0.5;
    double voltsPerSPE = 10.;
    double binsPerSPE = voltsPerSPE / LSB;
    double thrshVolts = nThrshNPE * voltsPerSPE;
    double noiseThrshV = thrshNoiseNPE * voltsPerSPE;
    double cosmicsThrsh = thrshVolts / LSB;
    double noiseThrsh = noiseThrshV / LSB;
    boolean testMode = false;
    //==================
    // greater than 2.5 p.e.
    // (pe * v/pe * bins /v )
    // = 51
    double threshD = nThrshNPE*10.0/(LSB);
    public int threshold;
    double nsPerSample = 4.0;

    final boolean fitBackground = false;
    int NBinsCosmic = 50;
    double CosmicQXMin[] = {0, 5.0 * nGain, 5.0 * nGain};
    double CosmicQXMax[] = {10000, 6000, 6000};
    final int nBinsVMIP = 50;
    final double CosmicVXMin[] = {0, 5.0 * nGain_mV, 5.0 * nGain_mV};
    final int CosmicVXMax[] = {10000, 1800, 1800};
    final int CosmicNPEXMin[] = {0, 3, 5};
    final int CosmicNPEXMax[] = {200, 93, 133};
    boolean simulatedAnalysis = true;
    boolean useDefaultGain = false;
    double NoiseQXMin[] = {0., 0.5 * nGain, 0.5 * nGain};
    double NoiseQXMax[] = {310., 3.0 * nGain, 3.0 * nGain};
    double NoiseVXMin[] = {0., 0.5 * nGain_mV, 0.5 * nGain_mV};
    double NoiseVXMax[] = {0., 3.0 * nGain_mV, 3.0 * nGain_mV};
    int[] NBinsNoiseQ = {0, 75, 75};
    int[] NBinsNoiseV = {0, 50, 50};
    final int NBinsPed = 400;
    //pedestal min and max bin values for histogram
    int[] PedQX = {100, 500};
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
        H_FADC.add(HP.getS(), HP.getL(), HP.getC(),
                   new H1F(DetectorDescriptor.getName("H_FADC",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(), 100, 0.0, 100.0));
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

        H_VT.add(HP.getS(), HP.getL(), HP.getC(), new H1F(DetectorDescriptor.getName("H_VT", HP.getS(), HP.getL(),HP.getC()),
                         HP.getTitle(), 100, 0.0, 400.0));
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Time (ns)");
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Voltage - ped sub (mV)");
        
        H_NPE.add(HP.getS(), HP.getL(), HP.getC(),
                  new H1F(DetectorDescriptor.
                          getName("H_NPE", HP.getS(),HP.getL(), HP.getC()),
                          HP.getTitle(), 100, 0.0, 400.0));
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Time (ns)");
        H_NPE.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Photoelectrons (amp mV/spe mV");
        
        //----------------------------
        // Accumulated Histograms
        // PEDESTAL CANVAS
        H_PED.add(HP.getS(), HP.getL(), HP.getC(),
                  new H1F(DetectorDescriptor.getName("H_PED", HP.getS(),HP.getL(),HP.getC()), HP.getTitle(),NBinsPed, PedQX[0], PedQX[1]));
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Pedestal");
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        String namePed = HP.getTitle() + " for 100 events";
        String namePed2 = HP.getTitle() + " every 100 events";

        H_PED_TEMP.add(HP.getS(), HP.getL(), HP.getC(),
                       new H1F(DetectorDescriptor.getName("H_PED_TEMP",HP.getS(),HP.getL(),HP.getC()), namePed, NBinsPed/2, PedQX[0], PedQX[1]));
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Pedestal");
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        for (int i = 0; i < nPointsPed; i++) {
            px_H_PED_VS_EVENT[i] = i * 1.0;
            pex_H_PED_VS_EVENT[i] = 0.0;
            //py_H_PED_VS_EVENT[i] = 0.0;
            //pey_H_PED_VS_EVENT[i] = 0.0;
        }
        
        H_PED_VS_EVENT.add(HP.getS(), HP.getL(), HP.getC(),
                           new GraphErrors(namePed2,
                                           px_H_PED_VS_EVENT,
                                           py_H_PED_VS_EVENT[HP.getS() - 1][HP.getL() - 1][HP.getC() - 1],  pex_H_PED_VS_EVENT,
                                           pey_H_PED_VS_EVENT[HP.getS() - 1][HP.getL() - 1][HP.getC() - 1]));
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitle(namePed);
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Event Index");
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Average Pedestal");
        H_PED_VS_EVENT.get(HP.getS(), HP.getL(), HP.getC()).setMarkerSize(5);
        
        H_MIP_Q.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("Cosmic Charge",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(),NBinsCosmic,CosmicQXMin[HP.getL()],CosmicQXMax[HP.getL()]));
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_MIP_Q_MatchingTiles.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP Charge Matching Tiles",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(),NBinsCosmic,CosmicQXMin[HP.getL()],CosmicQXMax[HP.getL()]));
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_MIP_Q_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        
        
        H_NOISE_Q.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("Noise Charge",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(),NBinsNoiseQ[HP.getL()],NoiseQXMin[HP.getL()],NoiseQXMax[HP.getL()]));
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
                HP.getTitle(), NBinsNoiseV[HP.getL()],NoiseVXMin[HP.getL()],NoiseVXMax[HP.getL()]));
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_MIP_V.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP WAVEMAX",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(), nBinsVMIP, CosmicVXMin[HP.getL()], CosmicVXMax[HP.getL()]));
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_MIP_V_MatchingTiles.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("MIP WAVEMAX Matching Tiles",HP.getS(),HP.getL(),HP.getC()),
                HP.getTitle(), nBinsVMIP,CosmicVXMin[HP.getL()], CosmicVXMax[HP.getL()]));
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_MIP_V_MatchingTiles.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_T_MODE3.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),
                HP.getTitle(), 100, 0.0, 400));
        H_T_MODE3.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(4);
        H_T_MODE3.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 3 Time (ns)");
        H_T_MODE3.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        // simulated data
        timeMin[1] = -15.0;
        timeMin[2] = -15.0;
        
        timeMax[1] = 15.0;
        timeMax[2] = 15.0;
        
        // paddle trigger
        timeMin[1] += triggerDelay;
        timeMin[2] += triggerDelay;
        
        timeMax[1] += triggerDelay;
        timeMax[2] += triggerDelay;
        
        H_MAXV_VS_T.add(HP.getS(),HP.getL(),HP.getC(), new H2F(DetectorDescriptor.getName("H_MAXV_VS_T",HP.getS(), HP.getL(), HP.getC()),
             HP.getTitle(),64, timeMin[HP.getL()], timeMax[HP.getL()],64, 0., 2000.));
        H_MAXV_VS_T.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_MAXV_VS_T.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Peak Voltage (mV)");
        
        H_T_MODE3.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),
                              HP.getTitle(), 32, timeMin[HP.getL()], timeMax[HP.getL()]));
        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
        H_T_MODE3.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 3 (ns)");
        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        
        H_T_MODE7.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_T_MODE7",HP.getS(), HP.getL(), HP.getC()),
                              HP.getTitle(), 64, timeMin[HP.getL()], timeMax[HP.getL()]));
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
        H_T_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        
        if (HP.getL() == 1) {
            H_T1_T2.add(HP.getS(),HP.getL(),HP.getC(),new H2F(DetectorDescriptor.getName("H_T1_T2",HP.getS(), HP.getL(), HP.getC()),
                                HP.getTitle(),64, timeMin[1], timeMax[1],64, timeMin[2], timeMax[2]));
            
            H_T1_T2.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns) [thin layer]");
            H_T1_T2.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Mode 7 Time (ns) [thick layer]");
            // DT
            H_DT_MODE7.add(HP.getS(),HP.getL(),HP.getC(),new H1F(DetectorDescriptor.getName("H_DT_MODE7",HP.getS(), HP.getL(), HP.getC()),
                                   HP.getTitle(), 64, -15., 15.));
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
            H_DT_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time difference (ns)");
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        }
        
        H_COSMIC_fADC.add(HP.getS(), HP.getL(), HP.getC(),new H1F(DetectorDescriptor.getName("Cosmic fADC",HP.getS(), HP.getL(), HP.getC()),
                                  HP.getTitle(), 100, 0.0, 100.0));
        H_COSMIC_fADC.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(3);
        H_COSMIC_fADC.get(HP.getS(), HP.getL(),HP.getC()).setTitleX("fADC Sample");
        H_COSMIC_fADC.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("fADC Counts");
    }
    
    public void InitHistograms() {
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
        H_EMPTYGAIN_ELE_PC=new H1F("H_EMPTYGAIN_ELE_PC", 500, 0.0, 17);
        H_EMPTYGAIN_ELE_PC.setTitleX("component");
        H_EMPTYGAIN_ELE_PC.setTitleY("gain (pC)");
        H_EMPTYGAIN_ELE_PC.setBinContent(1,40);
        H_EMPTYGAIN_ELE_MV=new H1F("H_EMPTYGAIN_ELE_MV", 500, 0.0, 17);
        H_EMPTYGAIN_ELE_MV.setTitleX("component");
        H_EMPTYGAIN_ELE_MV.setTitleY("gain (mV)");
        H_EMPTYGAIN_ELE_MV.setBinContent(1,20);
        
        for (int mezin = 0; mezin < 15; mezin++){
            GGgainElectronicsV[mezin]=new GraphErrors();
            GGgainElectronicsV[mezin].addPoint(0,0,0,0);
            GGgainElectronicsV[mezin].addPoint(16,0,0,0);
            GGgainElectronicsC[mezin]=new GraphErrors();
            GGgainElectronicsC[mezin].addPoint(0,0,0,0);
            GGgainElectronicsC[mezin].addPoint(16,0,0,0);
        }
        for (int lay = 0; lay < 2; lay++){
            for (int sec = 0; sec < 8; sec++){
                GGgainDetectorV[lay][sec]=new GraphErrors();
                GGgainDetectorV[lay][sec].addPoint(0,0,0,0);
                GGgainDetectorC[lay][sec]=new GraphErrors();
                GGgainDetectorC[lay][sec].addPoint(0,0,0,0);
                if (sec%2==0){
                    GGgainDetectorV[lay][sec].addPoint(10,0,0,0);
                    GGgainDetectorC[lay][sec].addPoint(10,0,0,0);
                }
                else {
                    GGgainDetectorV[lay][sec].addPoint(21,0,0,0);
                    GGgainDetectorC[lay][sec].addPoint(21,0,0,0);
                }
            }
        }
        
    }
    
    public void resetHistograms(){
        for (int index = 0; index < 232; index++) {
            resetAllHistograms(index, 'h');
        }
    }
    private void resetAllHistograms(int index, char detector) {
        
        HP.setAllParameters(index, detector);
        if (detector == 'h') {
            
            H_MIP_Q.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_MIP_Q_MatchingTiles.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_NOISE_Q.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_NPE_INT.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_NPE_MATCH.get(HP.getS(),  HP.getL(),  HP.getC()).reset();
            H_FADC.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_FADC_RAW.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_FADC_RAW_PED.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_FADC_RAW_PUL.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_NOISE_V.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_T_MODE7.get(HP.getS(),HP.getL(),HP.getC()).reset();
            //G_FADC_ANALYSIS.get(HP.getS(), HP.getL(), HP.getC()).reset();
            
            H_PED.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_COSMIC_fADC.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_MIP_V.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_MIP_V_MatchingTiles.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_MAXV_VS_T.get(HP.getS(),      HP.getL(),      HP.getC()).reset();
            if (HP.getL() == 1) {
                H_DT_MODE7.get(HP.getS(),HP.getL(),HP.getC()).reset();
                H_T1_T2.get(HP.getS(),HP.getL(),HP.getC()).reset();
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
            //fitQMIP(s,l,c,fitOption);
            //fitT(HP.getS(), HP.getL(), HP.getC(), fitOption);
        }
        System.out.println(" Fitting Histograms complete");
    }
    private void fitPedestals(int s, int l, int c, String fitOption) {
        if (testMode)  System.out.println(" Fitting Pedestal (S,L,C) = ("+ s + "," + l + "," + c + ")");
        
        if (initFitPedestalParameters(s, l, c, H_PED.get(s, l, c))) {
            DataFitter.fit(fPed.get(s, l, c), H_PED.get(s, l, c), fitOption);
            if (testMode) {
                System.out.println(" Fitted Pedestal (S,L,C) = ("+ s + "," + l + "," + c + ")");
            }
        } else if (testMode) {
            System.out.println(" No Pedestal Fit (S,L,C) = (" + s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitPedestalParameters(int s, int l, int c, H1F H1) {
        if (H1.integral() > 100) {
            double ampl = H1.getBinContent(H1.getMaximumBin());
            double mean = H1.getMaximumBin();
            mean = mean * H1.getAxis().getBinWidth(2);
            mean = mean + H1.getAxis().min();
            
            fPed.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", 130., 440.));
            fPed.get(s, l, c).setLineColor(2);
            fPed.get(s, l, c).setLineWidth(2);
            fPed.get(s, l, c).setParameter(0, ampl);
            fPed.get(s, l, c).setParameter(1, mean);
            fPed.get(s, l, c).setParameter(2, 2.0);
            fPed.get(s, l, c).setParLimits(0,0.1 * ampl,2.0 * ampl);
            fPed.get(s, l, c).setParLimits(1, 0.5*mean, 1.5*mean);
            fPed.get(s, l, c).setParLimits(2, 0, 3.0);
            return true;
        } else {
            return false;
        }
    }

    private void fitVNoise(int s, int l, int c, String fitOption) {
        if (testMode)
            System.out.println(" Fitting V Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitNoiseParameters(s, l, c, H_NOISE_V.get(s, l, c), fV2)) {
            if (testMode)
                System.out.println(" Fitting Voltage Noise (fV2) ");
            DataFitter.fit(fV2.get(s, l, c), H_NOISE_V.get(s, l, c), fitOption);
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
        if (initFitNoiseParameters(s, l, c, H_NOISE_Q.get(s, l, c), fQ2)) {
            DataFitter.fit(fQ2.get(s, l, c), H_NOISE_Q.get(s, l, c), fitOption);
            if (testMode)
                System.out.println(" Fitted Q Noise (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else if (testMode) {
        System.out.println(" ot enough entries to fit charge Noise (S,L,C) =(" + s + "," + l + "," + c + ")");
    }
}
    
    private boolean initFitNoiseParameters(int s, int l, int c, H1F H1, DetectorCollection<F1D> DCFunc) {
        if (testMode) System.out.println(" initFitNoiseParameters start ");
        double ampl = H1.getBinContent(H1.getMaximumBin());
        double mean = H1.getMaximumBin();
        mean = mean * H1.getAxis().getBinWidth(2);
        mean = mean + H1.getAxis().min();
        double std = 0.5;
        double exp0 = H1.getBinContent(1) + H1.getBinContent(2);
        if (testMode) System.out.println(" initFitNoiseParameters variables initialised ");
        if (H1.getEntries() > 500) {
            if (testMode) System.out.println(" initFitNoiseParameters setting fV2 parameters ");
            DCFunc.add(s, l, c, new F1D("gaus", "[amp1]*gaus(x,[mean1],[sigma1])+[amp2]*gaus(x,[mean2],[sigma2])+[amp3]*exp(x*[scale])",H1.getAxis().min(),H1.getAxis().max()));
            DCFunc.get(s, l, c).setLineColor(2);
            DCFunc.get(s, l, c).setLineWidth(2);
            DCFunc.get(s, l, c).setParameter(0, ampl);
            DCFunc.get(s, l, c).setParameter(1, mean);
            DCFunc.get(s, l, c).setParameter(2, std);
            DCFunc.get(s, l, c).setParameter(3, ampl / 4.0);
            DCFunc.get(s, l, c).setParameter(4, 2.0 * mean);
            DCFunc.get(s, l, c).setParameter(5, std);
            DCFunc.get(s, l, c).setParameter(6, exp0);
            DCFunc.get(s, l, c).setParameter(7, -0.2);
            if (testMode) System.out.println(" initFitNoiseParameters setting fV2 limits ");
            DCFunc.get(s, l, c).setParLimits(0, 0., ampl * 2);
            DCFunc.get(s, l, c).setParLimits(1, H1.getAxis().min(), 1.5 * mean);
            DCFunc.get(s, l, c).setParLimits(2, std/3, std * 5.0);
            DCFunc.get(s, l, c).setParLimits(3, 0.0, ampl);
            DCFunc.get(s, l, c).setParLimits(4,1.2 * mean,3.0 * mean);
            DCFunc.get(s, l, c).setParLimits(5, std/3, std * 5.0);
            DCFunc.get(s, l, c).setParLimits(6, 0.1 * exp0, 10.0 * exp0);
            DCFunc.get(s, l, c).setParLimits(7, -10.0, 0);
            return true;
        } else {
            if (testMode) System.out.println(" initFitNoiseParameters insufficient entries ");
            return false;
        }
    }
    
    private void fitVMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_V.get(s, l, c), fVMIP)) {
            DataFitter.fit(fVMIP.get(s, l, c), H_MIP_V.get(s, l, c), fitOption);
            if (testMode) System.out.println(" Fitted V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No V MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }

    private void fitQMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitMIPParameters(s, l, c, H_MIP_Q.get(s, l, c), fQMIP)) {
            DataFitter.fit(fVMIP.get(s, l, c), H_MIP_Q.get(s, l, c), fitOption);
            if (testMode) System.out.println(" Fitted Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No Q MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    
    
    private boolean initFitMIPParameters(int s, int l, int c, H1F H1,DetectorCollection<F1D> DCFunc) {
        double min, max;
        double mean= H1.getMean();
        double ampl = H1.getBinContent(H1.getXaxis().getBin(mean)); //set as starting amplitude the value of the bin at mean
        double gamma = H1.getRMS();
        double ampl1 = H1.getBinContent(1); //set as starting amplitude of exponential the value of the bin at xmin

        if (H1.integral() > 100) {
            DCFunc.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])+[amp2]*exp(x*[scale])",CosmicVXMin[l],CosmicVXMax[l]));
            DCFunc.get(s, l, c).setParameter(0, ampl);
            DCFunc.get(s, l, c).setParameter(1, mean);
            DCFunc.get(s, l, c).setParameter(2, gamma);
            DCFunc.get(s, l, c).setParameter(3, ampl1);
            DCFunc.get(s, l, c).setParameter(4, -0.2);
           
            DCFunc.get(s, l, c).setParLimits(0, ampl * 0.1, ampl * 10.0);
            DCFunc.get(s, l, c).setParLimits(1, CosmicVXMin[l], CosmicVXMax[l]);
            DCFunc.get(s, l, c).setParLimits(2, gamma/10, gamma*10);
            DCFunc.get(s, l, c).setParLimits(3, ampl1 * 0.1, ampl1 * 100.0);
            DCFunc.get(s, l, c).setParLimits(4, -0.1, -0.0001);
            return true;
        } else {
            return false;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//
//    private void fitVMIP(int s, int l, int c, String fitOption) {
//        if (testMode) System.out.println(" Fitting V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
//
//        if (H_MIP_V.hasEntry(s, l, c) && initFitVMIPParameters(s, l, c, H_MIP_V.get(s, l, c))) {
//            DataFitter.fit(fVMIP.get(s, l, c), H_MIP_V.get(s, l, c), fitOption);
//            //         H_MIP_V.get(s,l,c).
//            //         fit(fVMIP.get(s,l,c),fitOption);
//            if (testMode) System.out.println(" Fitted V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        } else {
//            if (testMode) System.out.println(" No V MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        }
//    }
//    private boolean initFitVMIPParameters(int s, int l, int c, H1F H1) {
//        double min, max, mean;
//        double ampl = H1.getBinContent(H1.getMaximumBin());
//        double std = 5.0;
//        double[] rangeLow = {0.0, 100., 250};
//        double[] rangeHigh = {0.0, 1250., 2000};
//        if (H1.integral() > 100) {
//            fVMIP.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])",CosmicVXMin[l],CosmicVXMax[l]));
//            mean = H1.getMean();
//            if (mean > 1400.) {
//                System.out.println(" check gain for: ");
//                System.out.println(" (s,l,c) = (" + s + "," + l + "c)");
//            }
//            //mean = getNominalVLandauMean();
//            fVMIP.get(s, l, c).setParameter(0, ampl);
//            fVMIP.get(s, l, c).setParameter(1, mean);
//            fVMIP.get(s, l, c).setParameter(2, 200);
//            min = CosmicVXMin[l];
//            max = CosmicVXMax[l];
//            fVMIP.get(s, l, c).setParLimits(0, ampl * 0.5, ampl * 2.5);
//            fVMIP.get(s, l, c).setParLimits(1, min, max);
//            fVMIP.get(s, l, c).setParLimits(2, 50, 200);
//            return true;
//        } else {
//            return false;
//        }
//    }
//    private void fitT(int s, int l, int c, String fitOption) {
//        if (testMode) System.out.println(" Fitting T (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        if (H_T_MODE7.hasEntry(s, l, c)  && initFitTParameters(s, l, c, H_T_MODE7.get(s, l, c))) {
//            DataFitter.fit(fT.get(s, l, c), H_T_MODE7.get(s, l, c), fitOption);
//            //         H_T_MODE7.get(s,l,c).
//            //         fit(fT.get(s,l,c),fitOption);
//            if (testMode) System.out.println(" Fitted T (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        } else {
//            if (testMode) System.out.println(" No T Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        }
//    }
//    private boolean initFitTParameters(int s, int l, int c, H1F H1) {
//        double ampl = H1.getBinContent(H1.getMaximumBin());
//        double mean = H1.getMaximumBin();
//        mean = mean * H1.getAxis().getBinWidth(2);
//        mean = mean + H1.getAxis().min();
//        double std = 1.0;
//        double rangeLow = -15.;
//        double rangeHigh = 15.0;
//        rangeLow += triggerDelay;
//        rangeHigh += triggerDelay;
//        if (H1.integral() > 100) {
//            fT.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])",rangeLow,rangeHigh));
//            fT.get(s, l, c).setParameter(0, ampl);
//            fT.get(s, l, c).setParameter(1, mean);
//            fT.get(s, l, c).setParameter(2, std);
//            fT.get(s, l, c).setParLimits(0, ampl * 0.5, ampl * 2);
//            fT.get(s, l, c).setParLimits(1, rangeLow, rangeHigh);
//            fT.get(s, l, c).setParLimits(2, 0.3, 4.0);
//            fT.get(s, l, c).setLineColor(1);
//            return true;
//        } else {
//            return false;
//        }
//    }
//    private void fitQMIP(int s, int l, int c, String fitOption) {
//        if (testMode) System.out.println(" Fitting Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        if (initFitQMIPParameters(s, l, c, H_MIP_Q.get(s, l, c))) {
//            DataFitter.fit(fQMIP.get(s, l, c), H_MIP_Q.get(s, l, c), fitOption);
//            //         H_MIP_Q.get(s,l,c).
//            //         fit(fQMIP.get(s,l,c),fitOption);
//            if (testMode) System.out.println(" Fitted Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        } else {
//            if (testMode) System.out.println(" No Q MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
//        }
//    }
//    private boolean initFitQMIPParameters(int s, int l, int c, H1F H1) {
//        double[] rangeLow = {0.0, 200., 500};
//        double[] rangeHigh = {0.0, 2500., 4000};
//        int integralLowBin = (int) (rangeLow[l] - CosmicQXMin[l]) * NBinsCosmic;
//        integralLowBin = integralLowBin / ((int) CosmicQXMax[l] - (int) CosmicQXMin[l]);
//        int integralHighBin = NBinsCosmic - 1;
//        if (H1.integral(integralLowBin,integralHighBin) > 25) {
//            double ampl = 0;
//            double mean = 0;
//            for (int i = integralLowBin; i < integralHighBin;i++) {
//                if (H1.getBinContent(i) > ampl) {
//                    ampl = H1.getBinContent(i);
//                    mean = i * (CosmicQXMax[l] - CosmicQXMin[l]);
//                    mean = mean / NBinsCosmic + CosmicQXMin[l];
//                }
//            }
//            String fitFunc = "landau";
//            String fitFunc1 = "[amp]*landau(x,[mean],[gamma])";
//            if (fitBackground) {
//                fitFunc = "landau+exp";
//                fitFunc1 = "[amp]*landau(x,[mean],[gamma])+[a]*exp([f])";
//            }
//            fQMIP.add(s, l, c, new F1D(fitFunc, fitFunc1,rangeLow[l],rangeHigh[l]));
//            fQMIP.get(s, l, c).setParameter(0, ampl);
//            fQMIP.get(s, l, c).setParameter(1, mean);
//            fQMIP.get(s, l, c).setParameter(2, 150);
//            if (fitBackground) {
//                fQMIP.get(s, l, c).setParameter(3, ampl / 5);
//                fQMIP.get(s, l, c).setParameter(4, -0.001);
//            }
//            fQMIP.get(s, l, c).setParLimits(0, 0, ampl * 2.0);
//            fQMIP.get(s, l, c).setParLimits(1, mean - 400, mean + 400);
//            fQMIP.get(s, l, c).setParLimits(2, 50, 1500);
//            if (fitBackground) {
//                fQMIP.get(s, l, c).setParLimits(3, ampl / 10, ampl * 20.0);
//                fQMIP.get(s, l, c).setParLimits(4, -5.0, 0.00);
//            }
//            fQMIP.get(s, l, c).setLineColor(1);
//            return true;
//        } else {
//            return false;
//        }
//    }
    
    private void initThresholdParameters(int s, int l, int c) {
        fThr.add(s, l, c, new F1D("p01", "[p0]", 0., 100.));
        fThr.get(s, l, c).setParameter(0, threshold);
        fThr.get(s, l, c).setLineColor(2);
        double vTh = threshold * LSB;
        fVThr.add(s, l, c, new F1D("p02", "[p0]", 0., 400.));
        fVThr.get(s, l, c).setParameter(0, vTh);
        fVThr.get(s, l, c).setLineColor(2);
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
