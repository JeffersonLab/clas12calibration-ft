#!/bin/csh -f


set indir   = $1
set rmin    = $2
set rmax    = $3
set comment = "$4"


./upload_ftCal_charge.sh $indir/EnergyCalibration.txt $rmin $rmax "$comment"

./upload_ftCal_time.sh   $indir/TimeCalibration.txt $rmin $rmax "$comment" 

./upload_ftCal_tw.sh     $indir/TimeWalk.txt $rmin $rmax "$comment"

