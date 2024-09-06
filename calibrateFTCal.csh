#!/bin/csh -f

if ($#argv < 4) then
 
  echo "Usage: calibrateFTCal.csh <executable> <inputfile> <number-of-iterations> <open-gui> [<target-position>]"
  exit 0
 
endif

set executable = $1
set inputfile  = $2
set niteration = "-n "$3
set window     = "-w "$4
set target     = ""
if ($#argv == 5) then
   set target  = "-t "$5
endif  

$executable -l EnergyCalibration -s EnergyCalibration $inputfile -d ./ftCalCalib $niteration $target $window -q 1
$executable -l EnergyCalibration -s EnergyCalibration:TimeCalibration $inputfile -d ./ftCalCalib -n 1 $target $window -q 1
$executable -l EnergyCalibration:TimeCalibration -s EnergyCalibration:TimeWalk $inputfile -d ./ftCalCalib -n 1 $target $window -q 1
$executable -l EnergyCalibration:TimeCalibration:TimeWalk -s EnergyCalibration:TimeCalibration $inputfile -d ./ftCalCalib -n 1 $target $window -q 1
$executable -l EnergyCalibration:TimeCalibration:TimeWalk $inputfile -d ./ftCalCalib -n 1 $target $window 



