/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.ftcal.tools;

/**
 *
 * @author devita
 */
public enum FTModuleType {
    UNDEFINED          ( 0, "UNDEFINED"),
    EVENT_SINGLE       ( 1, "EVENT_SINGLE"),
    EVENT_ACCUMULATE   ( 2, "EVENT_ACCUMULATE");    
    
    private final int typeId;
    private final String typeName;
    
    FTModuleType(){
        typeId = 0;
        typeName = "UNDEFINED";
    }
    
    FTModuleType(int id, String name){
        typeId = id;
        typeName = name;
    }
    
    public FTModuleType getType(int typeid){
        for(FTModuleType id: FTModuleType.values())
            if (id.typeId == typeid) 
                return id;
        return UNDEFINED;
    }
}