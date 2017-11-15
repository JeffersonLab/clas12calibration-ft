/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fthodo;

/**
 *
 * @author pikkaros
 */
public class FTHodoHistParams extends Object {
        public int layer;
        public int quadrant;
        public int element;
        public int sector;
        public int component;
        public String title;
        public String layerStr;
        public void setAllParameters(int index, char detector) {
            if (detector == 'h') {
                layer = index / 116 + 1;
                if (layer == 1) {
                    layerStr = "Thin";
                } else {
                    layerStr = "Thick";
                }
                // (map indices in both layers to [0,115])
                // /map indices to quadrants [0,3]
                quadrant = (index - (layer - 1) * 116) / 29;

                // map indices to [0,28]
                element = index - quadrant * 29 - (layer - 1) * 116;

                // map quadrant to sectors [1,8]
                // map element to tiles [1,9] or
                // map element to tiles [1,20]
                if (element < 9) {
                    sector = quadrant * 2 + 1;
                    component = element + 1;
                } 
                else {
                    sector = quadrant * 2 + 2;
                    component = element + 1 - 9;
                }
                title = " " + layerStr + " S" + sector + " C" + component;

            } else {
                layer = 0;
                quadrant = 0;
                element = index;
                component = index;
                sector = 0;

                if (component == 501) {
                    layerStr = "Top Long Paddle";
                } 
                else if (component == 502) {
                    layerStr = "Bottom Long Paddle";
                } 
                else if (component == 503) {
                    layerStr = "Top Short Paddle";
                }
                else if (component == 504) {
                    layerStr = "Bottom Short Paddle";
                }
                title = " " + layerStr;
            }
        }
        public int getL() {
            return layer;
        }
        public int getQuad() {
            return quadrant;
        }
        public int getElem() {
            return element;
        }
        public int getS() {
            return sector;
        }
        public int getC() {
            return component;
        }
        public String getTitle() {
            return title;
        }
        public String getLayerStr() {
            return layerStr;
        }
    }

