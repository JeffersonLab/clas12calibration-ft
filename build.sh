#!/bin/csh -f
#=================================================================
# BUILDING SCRIPT for COATJAVA PROJECT (first maven build)
# then the documentation is build from the sources and committed
# to the documents page
#=================================================================
# Maven Build

set suites = (ftCalCalib ftCalLedAndCosmics ftHodoCalibNovice ftHodoCalibHipo)

foreach suite ($suites)
    echo Building $suite ...
    cd $suite
    mvn clean
    mvn install
    mvn package
    cd ..
end

# Finishing touches
echo ""
echo "--> Done building....."
echo ""
echo "    Usage : build.sh"
echo ""
