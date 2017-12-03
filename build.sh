#!/bin/csh -f
#=================================================================
# BUILDING SCRIPT for COATJAVA PROJECT (first maven build)
# then the documentatoin is build from the sources and commited
# to the documents page
#=================================================================
# Maven Build

if(`filetest -e lib` == '0') then
    mkdir lib
endif

# ftCalCalib
echo "Building ftCalCalib..."
    cd ftCalCalib
    mvn install
    mvn package
    cp target/FTCalCalib-1.0.jar ../lib/
    cd ..

# ftCalLedAndCosmics
echo "Building ftCalLedAndCosmics..."
    cd ftCalLedAndCosmics
    mvn install
    mvn package
    cp target/FTCalLedAndCosmics-1.0.jar ../lib/
    cd ..

# ftCalCalib
echo "Building ftHodoCalib..."
    cd ftHodoCalibNovice
    mvn install
    mvn package
    cp target/ftHodoCalibNovice-4.0-SNAPSHOT.jar ../lib/
    cd ..


# Finishing touches
echo ""
echo "--> Done building....."
echo ""
echo "    Usage : build.sh"
echo ""
