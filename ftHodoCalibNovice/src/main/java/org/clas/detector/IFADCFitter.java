/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clas.detector;

import org.clas.detector.DetectorDataDgtz.ADCData;


/**
 *
 * @author gavalian
 */
public interface IFADCFitter {
    void fit(ADCData data);
}
