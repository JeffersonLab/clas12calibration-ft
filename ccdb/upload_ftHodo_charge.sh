#!/bin/csh -f


set infile  = $1
set rmin    = $2
set rmax    = $3
set comment = "$4"


echo loading charge_to_energy constants from file $infile to run range $rmin-$rmax with comment $comment
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add calibration/ft/fthodo/charge_to_energy $infile -r $rmin-$rmax \#\"$comment\"
echo
