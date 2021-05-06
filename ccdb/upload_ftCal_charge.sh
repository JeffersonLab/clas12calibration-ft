#!/bin/csh -f


set infile  = $1
set rmin    = $2
set rmax    = $3
set comment = "$4"

if(`filetest -e temp.txt` == "1") then
   rm temp.txt
endif
foreach line ("`cat $infile`")
   set argv = ( $line )
   if(`filetest -e temp.txt` == "0") then
       echo $1 $2 $3 $8 15.3 0.039064 700 150 >  temp.txt
   else 
       echo $1 $2 $3 $8 15.3 0.039064 700 150 >> temp.txt
   endif
end
echo loading charge_to_energy constants from file $infile to run range $rmin-$rmax with comment $comment
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add calibration/ft/ftcal/charge_to_energy temp.txt -r $rmin-$rmax \#\"$comment\"
echo
