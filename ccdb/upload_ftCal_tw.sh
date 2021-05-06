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
       echo $1 $2 $3 $10 $12 $14  >  temp.txt
   else 
       echo $1 $2 $3 $10 $12 $14  >> temp.txt
   endif
end
echo loading time_walk constants from file $infile to run range $rmin-$rmax with comment $comment
ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add calibration/ft/ftcal/time_walk temp.txt -r $rmin-$rmax \#\"$comment\"
echo
