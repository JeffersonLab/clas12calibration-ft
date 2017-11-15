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
    DetectorCollection<H1F> H_FADC = new DetectorCollection<H1F>(); // baseline subtracted pulse calibrated to voltage and time
    DetectorCollection<H1F> H_VT = new DetectorCollection<H1F>();   // '' calibrated to no. photoelectrons and time
    DetectorCollection<H1F> H_NPE = new DetectorCollection<H1F>();    // Semi Accumulated
    DetectorCollection<H1F> H_PED_TEMP = new DetectorCollection<H1F>();     // Accumulated
    DetectorCollection<H1F> H_PED = new DetectorCollection<H1F>();
    DetectorCollection<GraphErrors> H_PED_VS_EVENT = new DetectorCollection<GraphErrors>();
    DetectorCollection<GraphErrors> H_PED_INDEX;
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
    // 2D
    DetectorCollection<H2F> H_MAXV_VS_T = new DetectorCollection<H2F>();
    DetectorCollection<H2F> H_T1_T2 = new DetectorCollection<H2F>();
    // 1D
    DetectorCollection<H1F> H_COSMIC_fADC = new DetectorCollection<H1F>();
    // Fit Functions
    DetectorCollection<F1D> fPed = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQ1 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQ2 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fQMIP = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fV1 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fV2 = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fVMIP = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fT = new DetectorCollection<F1D>();
    // Functions (that are not used to fit)
    DetectorCollection<F1D> fThr = new DetectorCollection<F1D>();
    DetectorCollection<F1D> fVThr = new DetectorCollection<F1D>();
    DetectorCollection<Integer> dcHits = new DetectorCollection<Integer>();
    H1F H_W_MAX = null;
    H1F H_V_MAX = null;
    H1F H_NPE_MAX = null;
    H1F H_CHARGE_MAX = null;
    
    //=================================
    //           CONSTANTS
    //=================================
    boolean fitTwoPeaksV = false;
    boolean fitTwoPeaksQ = false;
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
    double LSB = voltageMax / (1.0*fADCBins);
    double triggerDelay = 190.0;
    final double thrshNoiseNPE = 0.5;
    double voltsPerSPE = 10.;
    double binsPerSPE = voltsPerSPE / LSB;
    double thrshVolts = nThrshNPE * voltsPerSPE;
    double noiseThrshV = thrshNoiseNPE * voltsPerSPE;
    double cosmicsThrsh = thrshVolts / LSB;
    double noiseThrsh = noiseThrshV / LSB;
    boolean testMode = true;
    //==================
    // greater than 2.5 p.e.
    // (pe * v/pe * bins /v )
    // = 51
    double threshD = nThrshNPE * 10.0 / LSB;
    public int threshold;
    double nsPerSample = 4.0;
    //=================
    //     int ped_i1 = 4;
    //     int ped_i2 = 24;
    //     int pul_i1 = 30;
    //     int pul_i2 = 70;
    // cosmics
    int ped_i1 = 4;
    int ped_i2 = 44;
    int pul_i1 = 45;
    int pul_i2 = 99;
    final boolean fitBackground = false;
    int NBinsCosmic = 50;
    double CosmicQXMin[] = {0, 3.0 * nGain, 3.0 * nGain};
    double CosmicQXMax[] = {10000, 6000, 6000};
    final int nBinsVMIP = 50;
    final double CosmicVXMin[] = {0, 3.0 * nGain_mV, 3.0 * nGain_mV};
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
    final int NBinsPed = 200;
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
                   new H1F(DetectorDescriptor.getName("H_FADC",HP.getS(),HP.getL(),HP.getC()),
                           HP.getTitle(), 100, 0.0, 100.0));
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(4);
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("fADC Time");
        H_FADC.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("fADC Amplitude");
        
        H_VT.add(HP.getS(), HP.getL(), HP.getC(),
                 new H1F(DetectorDescriptor.getName("H_VT", HP.getS(), HP.getL(),HP.getC()),
                         HP.getTitle(), 100, 0.0, 400.0));
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Time (ns)");
        H_VT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Voltage (mV)");
        
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
                  new H1F(DetectorDescriptor.getName("H_PED", HP.getS(),HP.getL(),HP.getC()),
                          HP.getTitle(),NBinsPed, PedQX[0], PedQX[1]));
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(2);
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Pedestal");
        H_PED.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        String namePed = HP.getTitle() + " for 100 events";
        String namePed2 = HP.getTitle() + " every 100 events";

        H_PED_TEMP.add(HP.getS(), HP.getL(), HP.getC(),
                       new H1F(DetectorDescriptor.getName("H_PED_TEMP",HP.getS(),HP.getL(),HP.getC()),
                               namePed, NBinsPed, PedQX[0], PedQX[1]));
        H_PED_TEMP.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(2);
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
        
        H_MIP_Q.add(HP.getS(), HP.getL(), HP.getC(),
                    new H1F(DetectorDescriptor.getName("Cosmic Charge",HP.getS(),HP.getL(),HP.getC()),HP.getTitle(),
                            NBinsCosmic,CosmicQXMin[HP.getL()],CosmicQXMax[HP.getL()]));
        
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_MIP_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_NOISE_Q.add(HP.getS(), HP.getL(), HP.getC(),
                      new H1F(DetectorDescriptor.getName("Noise Charge",HP.getS(),HP.getL(),HP.getC()),
                              HP.getTitle(),NBinsNoiseQ[HP.getL()],NoiseQXMin[HP.getL()],NoiseQXMax[HP.getL()]));
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(5);
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Charge (pC)");
        H_NOISE_Q.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_NPE_INT.add(HP.getS(), HP.getL(), HP.getC(),
                      new H1F(DetectorDescriptor.getName("NPE integrated",HP.getS(),HP.getL(),HP.getC()),
                              HP.getTitle(),100, 0, 100));
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(6);
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("npe (peak/gain)");
        H_NPE_INT.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_NPE_MATCH.add(HP.getS(), HP.getL(), HP.getC(),
                        new H1F(DetectorDescriptor.getName("NPE int, matched layers",HP.getS(),HP.getL(),HP.getC()),
                                HP.getTitle(), 100,0,CosmicNPEXMax[HP.getL()]));
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(7);
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("npe (peak/gain)");
        H_NPE_MATCH.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_NOISE_V.add(HP.getS(), HP.getL(), HP.getC(),
                      new H1F(DetectorDescriptor.getName("WAVEMAX",HP.getS(), HP.getL(), HP.getC()),
                              HP.getTitle(), NBinsNoiseV[HP.getL()],NoiseVXMin[HP.getL()],NoiseVXMax[HP.getL()]));
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(2);
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        
        H_NOISE_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_MIP_V.add(HP.getS(), HP.getL(), HP.getC(),
                    new H1F(DetectorDescriptor.getName("MIP WAVEMAX",HP.getS(),HP.getL(),HP.getC()),
                            HP.getTitle(), nBinsVMIP,CosmicVXMin[HP.getL()], CosmicVXMax[HP.getL()]));
        
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setFillColor(3);
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleX("Waveform Max (mV)");
        H_MIP_V.get(HP.getS(), HP.getL(), HP.getC()).setTitleY("Counts");
        
        H_T_MODE3.add(HP.getS(), HP.getL(), HP.getC(),
                      new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),
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
        
        H_MAXV_VS_T.add(HP.getS(),HP.getL(),HP.getC(),
                        new H2F(DetectorDescriptor.getName("H_MAXV_VS_T",HP.getS(), HP.getL(), HP.getC()),
                                HP.getTitle(),64, timeMin[HP.getL()], timeMax[HP.getL()],64, 0., 2000.));
        H_MAXV_VS_T.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_MAXV_VS_T.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Peak Voltage (mV)");
        
        H_T_MODE3.add(HP.getS(),HP.getL(),HP.getC(),
                      new H1F(DetectorDescriptor.getName("H_T_MODE3",HP.getS(), HP.getL(), HP.getC()),
                              HP.getTitle(), 32, timeMin[HP.getL()], timeMax[HP.getL()]));
        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
        H_T_MODE3.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 3 (ns)");
        H_T_MODE3.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        
        H_T_MODE7.add(HP.getS(),HP.getL(),HP.getC(),
                      new H1F(DetectorDescriptor.getName("H_T_MODE7",HP.getS(), HP.getL(), HP.getC()),
                              HP.getTitle(), 64, timeMin[HP.getL()], timeMax[HP.getL()]));
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
        H_T_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns)");
        H_T_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        
        if (HP.getL() == 1) {
            H_T1_T2.add(HP.getS(),HP.getL(),HP.getC(),
                        new H2F(DetectorDescriptor.getName("H_T1_T2",HP.getS(), HP.getL(), HP.getC()),
                                HP.getTitle(),64, timeMin[1], timeMax[1],64, timeMin[2], timeMax[2]));
            
            H_T1_T2.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time (ns) [thin layer]");
            H_T1_T2.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Mode 7 Time (ns) [thick layer]");
            // DT
            H_DT_MODE7.add(HP.getS(),HP.getL(),HP.getC(),
                           new H1F(DetectorDescriptor.getName("H_DT_MODE7",HP.getS(), HP.getL(), HP.getC()),
                                   HP.getTitle(), 64, -15., 15.));
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setFillColor(4);
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setLineColor(1);
            H_DT_MODE7.get(HP.getS(),HP.getL(),HP.getC()).setTitleX("Mode 7 Time difference (ns)");
            H_DT_MODE7.get(HP.getS(), HP.getL(),HP.getC()).setTitleY("Counts");
        }
        
        H_COSMIC_fADC.add(HP.getS(), HP.getL(), HP.getC(),
                          new H1F(DetectorDescriptor.getName("Cosmic fADC",HP.getS(), HP.getL(), HP.getC()),
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
    }
    
    public void resetHistograms(){
        for (int index = 0; index < 232; index++) {
            resetAllHistograms(index, 'h');
        }
    }
    private void resetAllHistograms(int index, char detector) {
        
        HP.setAllParameters(index, detector);
        
        H_MIP_Q.get(HP.getS(),HP.getL(),HP.getC()).reset();
        H_NOISE_Q.get(HP.getS(),HP.getL(),HP.getC()).reset();
        H_NPE_INT.get(HP.getS(),HP.getL(),HP.getC()).reset();
        H_NPE_MATCH.get(HP.getS(),  HP.getL(),  HP.getC()).reset();
        H_FADC.get(HP.getS(),HP.getL(),HP.getC()).reset();
        H_NOISE_V.get(HP.getS(),HP.getL(),HP.getC()).reset();
        H_T_MODE7.get(HP.getS(),HP.getL(),HP.getC()).reset();
        
        if (detector == 'h') {
            H_PED.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_COSMIC_fADC.get(HP.getS(),HP.getL(),HP.getC()).reset();
            H_MIP_V.get(HP.getS(),HP.getL(),HP.getC()).reset();
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
        int s, l, c;
        
        // Do the fitting for all components
        for (int index = 0; index < 232; index++) {
            HP.setAllParameters(index, 'h');
            fitPedestals(HP.getS(), HP.getL(), HP.getC(), fitOption);
            fitVNoise(HP.getS(), HP.getL(), HP.getC(), fitOption);
            //fitQNoise(s,l,c,fitOption);
            fitVMIP(HP.getS(), HP.getL(),HP.getC(), fitOption);
            //fitQMIP(s,l,c,fitOption);
            fitT(HP.getS(), HP.getL(), HP.getC(), fitOption);
        }
        System.out.println(" Fitting Histograms complete");
    }
    private void fitPedestals(int s, int l, int c, String fitOption) {
        if (testMode)  System.out.println(" Fitting Pedestal (S,L,C) = ("
                               + s + "," + l + "," + c + ")");
        
        if (initFitPedestalParameters(s, l, c, H_PED.get(s, l, c))) {
            DataFitter.fit(fPed.get(s, l, c), H_PED.get(s, l, c), fitOption);
            if (testMode) {
                System.out.println(" Fitted Pedestal (S,L,C) = ("
                                   + s + "," + l + "," + c + ")");
            }
        } else if (testMode) {
            System.out.println(" No Pedestal Fit (S,L,C) = ("
                               + s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitPedestalParameters(int s, int l, int c, H1F H1) {
        double max = H1.getBinContent(H1.getMaximumBin());
        if (testMode)
            System.out.println(" Nick1");
        if (H1.integral() > 100) {
            if (testMode)  System.out.println(" Nick2");
            fPed.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", 130., 440.));
            fPed.get(s, l, c).setParameter(0, 200);
            fPed.get(s, l, c).setParameter(1, H1.getMean());
            fPed.get(s, l, c).setParameter(2, 1.0);
            fPed.get(s, l, c).setParLimits(0,0.1 * max,10.0 * max);
            fPed.get(s, l, c).setParLimits(1, 130, 440);
            fPed.get(s, l, c).setParLimits(2, 0, 2.0);
            return true;
        } else {
            return false;
        }
    }

    private void fitVNoise(int s, int l, int c, String fitOption) {
        if (testMode) {
            System.out.println(" Fitting V Noise (S,L,C) = ("
                               + s + "," + l + "," + c + ")");
        }
        if (initFitVNoiseParameters(s, l, c, H_NOISE_V.get(s, l, c))) {
            if (testMode) System.out.println(" Fitting fV1 ");
            DataFitter.fit(fV1.get(s, l, c), H_NOISE_V.get(s, l, c), fitOption);
            //         H_NOISE_V.get(s,l,c).
            //         fit(fV1.get(s,l,c),fitOption);
            if (this.fitTwoPeaksV) {
                if (testMode) System.out.println(" Fitting fV2 ");
                DataFitter.fit(fV2.get(s, l, c), H_NOISE_V.get(s, l, c), fitOption);
                //         H_NOISE_V.get(s,l,c).
                //             fit(fV2.get(s,l,c),fitOption);
            } else {
                if (testMode) System.out.println(" Skipping fV2 Fit ");
            }
            
            if (testMode) {
                System.out.println(" Fitted V Noise 1 & 2 (S,L,C) = ("
                                   + s + "," + l + "," + c + ")");
            }
        } else if (testMode) {
            System.out.println(" No V Noise 1 & 2 Fits (S,L,C) = ("
                               + s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitVNoiseParameters(int s, int l, int c, H1F H1) {
        if (testMode) System.out.println(" initFitVNoiseParameters start ");
        double ampl = H1.getBinContent(H1.getMaximumBin());
        double mean = H1.getMaximumBin();
        mean = mean * H1.getAxis().getBinWidth(2);
        mean = mean + H1.getAxis().min();
        double std = 0.5;
        double exp0 = H1.getBinContent(1) + H1.getBinContent(2);
        if (testMode) System.out.println(" initFitVNoiseParameters variables" + " initialised ");
        if (H1.getEntries() > 500) {
            if (testMode)  System.out.println(" initFitVNoiseParameters "+ " setting fV1 parameters ");
            fV1.add(s, l, c, new F1D("gaus+exp", "[amp]*gaus(x,[mean],[sigma])+[h]*exp([f])",
                                     H1.getAxis().min(),
                                     2.0 * nGain_mV));
            // gaus
            fV1.get(s, l, c).setParameter(0, ampl);
            //fV1.get(s,l,c).setParameter(1, nGain_mV);
            fV1.get(s, l, c).setParameter(1, mean);
            fV1.get(s, l, c).setParameter(2, std);
            // expo
            fV1.get(s, l, c).setParameter(3, exp0);
            fV1.get(s, l, c).setParameter(4, -0.2);
            if (testMode) System.out.println(" initFitVNoiseParameters setting "+ " fV1 limits ");
            // gaus
            fV1.get(s, l, c).setParLimits(0, 0., ampl * 10);
            fV1.get(s, l, c).setParLimits(1, H1.getAxis().min(), 2 * nGain_mV);
            fV1.get(s, l, c).setParLimits(2, std / 2, std * 2.0);
            // expo
            fV1.get(s, l, c).setParLimits(3, 0.1 * exp0, 10.0 * exp0);
            fV1.get(s, l, c).setParLimits(4, -0.1, -1.0);
            if (testMode) System.out.println(" initFitVNoiseParameters setting " + " fV2 parameters ");
            fV2.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])",1.5 * nGain_mV,3.0 * nGain_mV));
            fV2.get(s, l, c).
            setParameter(0, ampl / 3.0);
            fV2.get(s, l, c).
            setParameter(1, 2.0 * nGain_mV);
            fV2.get(s, l, c).
            setParameter(2, std);
            if (testMode) System.out.println(" initFitVNoiseParameters setting"+ " fV2 limits ");
            fV2.get(s, l, c).
            setParLimits(0, 0, ampl);
            fV2.get(s, l, c).
            setParLimits(1,1.5 * nGain_mV,2.5 * nGain_mV);
            fV2.get(s, l, c).setParLimits(2, 0., std * 4.0);
            return true;
        } else {
            if (testMode) System.out.println(" initFitVNoiseParameters insufficient"+ " entries ");
            return false;
        }
    }
    private void fitVMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        
        if (H_MIP_V.hasEntry(s, l, c) && initFitVMIPParameters(s, l, c, H_MIP_V.get(s, l, c))) {
            DataFitter.fit(fVMIP.get(s, l, c), H_MIP_V.get(s, l, c), fitOption);
            //         H_MIP_V.get(s,l,c).
            //         fit(fVMIP.get(s,l,c),fitOption);
            if (testMode) System.out.println(" Fitted V MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");      
        } else {
            if (testMode) System.out.println(" No V MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitVMIPParameters(int s, int l, int c, H1F H1) {
        double min, max, mean;
        double ampl = H1.getBinContent(H1.getMaximumBin());
        double std = 5.0;
        double[] rangeLow = {0.0, 100., 250};
        double[] rangeHigh = {0.0, 1250., 2000};
        if (H1.integral() > 100) {
            fVMIP.add(s, l, c, new F1D("landau", "[amp]*landau(x,[mean],[gamma])",CosmicVXMin[l],CosmicVXMax[l]));
            mean = H1.getMean();
            if (mean > 1400.) {
                System.out.println(" check gain for: ");
                System.out.println(" (s,l,c) = (" + s + "," + l + "c)");
            }
            //mean = getNominalVLandauMean();
            fVMIP.get(s, l, c).setParameter(0, ampl);
            fVMIP.get(s, l, c).setParameter(1, mean);
            fVMIP.get(s, l, c).setParameter(2, 200);
            min = CosmicVXMin[l];
            max = CosmicVXMax[l];
            fVMIP.get(s, l, c).setParLimits(0, ampl * 0.5, ampl * 2.5);
            fVMIP.get(s, l, c).setParLimits(1, min, max);
            fVMIP.get(s, l, c).setParLimits(2, 50, 200);
            return true;
        } else {
            return false;
        }
    }
    private void fitT(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting T (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (H_T_MODE7.hasEntry(s, l, c)  && initFitTParameters(s, l, c, H_T_MODE7.get(s, l, c))) {
            DataFitter.fit(fT.get(s, l, c), H_T_MODE7.get(s, l, c), fitOption);
            //         H_T_MODE7.get(s,l,c).
            //         fit(fT.get(s,l,c),fitOption);
            if (testMode) System.out.println(" Fitted T (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No T Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitTParameters(int s, int l, int c, H1F H1) {
        double ampl = H1.getBinContent(H1.getMaximumBin());
        double mean = H1.getMaximumBin();
        mean = mean * H1.getAxis().getBinWidth(2);
        mean = mean + H1.getAxis().min();
        double std = 1.0;
        double rangeLow = -15.;
        double rangeHigh = 15.0;
        rangeLow += triggerDelay;
        rangeHigh += triggerDelay;
        if (H1.integral() > 100) {
            fT.add(s, l, c, new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])",rangeLow,rangeHigh));
            fT.get(s, l, c).setParameter(0, ampl);
            fT.get(s, l, c).setParameter(1, mean);
            fT.get(s, l, c).setParameter(2, std);
            fT.get(s, l, c).setParLimits(0, ampl * 0.5, ampl * 2);
            fT.get(s, l, c).setParLimits(1, rangeLow, rangeHigh);
            fT.get(s, l, c).setParLimits(2, 0.3, 4.0);
            fT.get(s, l, c).setLineColor(1);
            return true;
        } else {
            return false;
        }
    }
    private void fitQMIP(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitQMIPParameters(s, l, c, H_MIP_Q.get(s, l, c))) {
            DataFitter.fit(fQMIP.get(s, l, c), H_MIP_Q.get(s, l, c), fitOption);
            //         H_MIP_Q.get(s,l,c).
            //         fit(fQMIP.get(s,l,c),fitOption);
            if (testMode) System.out.println(" Fitted Q MIP (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else {
            if (testMode) System.out.println(" No Q MIP Fit (S,L,C) = ("+ s + "," + l + "," + c + ")");
        }
    }
    private boolean initFitQMIPParameters(int s, int l, int c, H1F H1) {
        double[] rangeLow = {0.0, 200., 500};
        double[] rangeHigh = {0.0, 2500., 4000};
        int integralLowBin = (int) (rangeLow[l] - CosmicQXMin[l]) * NBinsCosmic;
        integralLowBin = integralLowBin / ((int) CosmicQXMax[l] - (int) CosmicQXMin[l]);
        int integralHighBin = NBinsCosmic - 1;
        if (H1.integral(integralLowBin,integralHighBin) > 25) {
            double ampl = 0;
            double mean = 0;
            for (int i = integralLowBin; i < integralHighBin;i++) {
                if (H1.getBinContent(i) > ampl) {
                    ampl = H1.getBinContent(i);
                    mean = i * (CosmicQXMax[l] - CosmicQXMin[l]);
                    mean = mean / NBinsCosmic + CosmicQXMin[l];
                }
            }
            String fitFunc = "landau";
            String fitFunc1 = "[amp]*landau(x,[mean],[gamma])";
            if (fitBackground) {
                fitFunc = "landau+exp";
                fitFunc1 = "[amp]*landau(x,[mean],[gamma])+[a]*exp([f])";
            }
            fQMIP.add(s, l, c, new F1D(fitFunc, fitFunc1,rangeLow[l],rangeHigh[l]));
            fQMIP.get(s, l, c).setParameter(0, ampl);
            fQMIP.get(s, l, c).setParameter(1, mean);
            fQMIP.get(s, l, c).setParameter(2, 150);
            if (fitBackground) {
                fQMIP.get(s, l, c).setParameter(3, ampl / 5);
                fQMIP.get(s, l, c).setParameter(4, -0.001);
            }
            fQMIP.get(s, l, c).setParLimits(0, 0, ampl * 2.0);
            fQMIP.get(s, l, c).setParLimits(1, mean - 400, mean + 400);
            fQMIP.get(s, l, c).setParLimits(2, 50, 1500);
            if (fitBackground) {
                fQMIP.get(s, l, c).setParLimits(3, ampl / 10, ampl * 20.0);
                fQMIP.get(s, l, c).setParLimits(4, -5.0, 0.00);
            }
            fQMIP.get(s, l, c).setLineColor(1);
            return true;
        } else {
            return false;
        }
    }
    
    private boolean initFitQNoiseParameters(int s, int l, int c, H1F H1) {
        if (testMode) System.out.println(" initFitQNoiseParameters start ");
        double ampl = H1.getBinContent(H1.getMaximumBin());
        double mean = H1.getMaximumBin();
        mean = mean * H1.getAxis().getBinWidth(2);
        mean = mean + H1.getAxis().min();
        double std = 5.0;
        if (testMode) System.out.println(" initFitQNoiseParameters variables initialised ");
        if (H1.getEntries() > 500) {
            if (testMode) System.out.println(" initFitQNoiseParameters setting fQ1 parameters ");
            fQ1.add(s, l, c, new F1D("exp+gaus", "exp+gaus",H1.getAxis().min(),nGain * 1.5));
            // exponential
            fQ1.get(s, l, c).setParameter(0, ampl / 5.);
            fQ1.get(s, l, c).setParameter(1, -0.001);
            // gaus 1
            fQ1.get(s, l, c).setParameter(2, ampl);
            fQ1.get(s, l, c).setParameter(3, nGain);
            fQ1.get(s, l, c).setParameter(4, std);
            if (testMode) System.out.println(" initFitQNoiseParameters setting fQ1 limits ");
            // exponential
            fQ1.get(s, l, c).setParLimits(0, ampl / 10.0, 5.0 * ampl);
            fQ1.get(s, l, c).setParLimits(1, -5.0, -0.0001);
            // gaus 1
            fQ1.get(s, l, c).setParLimits(2, ampl / 2., ampl * 2);
            fQ1.get(s, l, c).setParLimits(3, 0.5 * nGain, 1.5 * nGain);
            fQ1.get(s, l, c).setParLimits(4, 1, std / 2.);
            fQ1.get(s, l, c).setLineColor(1);
            if (testMode) System.out.println(" initFitQNoiseParameters setting fQ2 parameters ");
            fQ2.add(s, l, c,new F1D("gaus", "gaus", 1.5 * nGain, 2.5 * nGain));
            fQ2.get(s, l, c).setParameter(0, ampl / 5.0);
            fQ2.get(s, l, c).setParameter(1, 2.0 * nGain);
            fQ2.get(s, l, c).setParameter(2, std);
            if (testMode) System.out.println(" initFitQNoiseParameters setting fQ2 limits ");
            fQ2.get(s, l, c).setParLimits(0, 0, ampl / 1.5);
            fQ2.get(s, l, c).setParLimits(1, mean + 20, mean + 100);
            fQ2.get(s, l, c).setParLimits(2, 1, std * 3.0);
            fQ2.get(s, l, c).setLineColor(1);
            return true;
        } else {
            if (testMode) System.out.println(" initFitQNoiseParameters insufficient entries ");
            return false;
        }
    }
    private void fitQNoise(int s, int l, int c, String fitOption) {
        if (testMode) System.out.println(" Fitting Q Noise(S,L,C) = ("+ s + "," + l + "," + c + ")");
        if (initFitQNoiseParameters(s, l, c, H_NOISE_Q.get(s, l, c))) {
            DataFitter.fit(fQ1.get(s, l, c), H_NOISE_Q.get(s, l, c), fitOption);
            //         H_NOISE_Q.get(s,l,c).
            //         fit(fQ1.get(s,l,c),fitOption);
            if (this.fitTwoPeaksQ) {
                DataFitter.fit(fQ2.get(s, l, c), H_NOISE_Q.get(s, l, c), fitOption);
                //         H_NOISE_Q.get(s,l,c).
                //             fit(fQ2.get(s,l,c),fitOption);
            }
            if (testMode) 
                System.out.println(" Fitted Q Noise 1 & 2 (S,L,C) = ("+ s + "," + l + "," + c + ")");
        } else if (testMode) {
            System.out.println(" No Q Noise 1 & 2 Fits (S,L,C) = (" + s + "," + l + "," + c + ")");
        }
    }
    
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
