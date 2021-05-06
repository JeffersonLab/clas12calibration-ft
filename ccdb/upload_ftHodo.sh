#!/bin/csh -f


set indir   = $1
set rmin    = $2
set rmax    = $3
set comment = "$4"


./upload_ftHodo_charge.sh $indir/Charge.txt $rmin $rmax "$comment"

./upload_ftHodo_time.sh   $indir/Time.txt $rmin $rmax "$comment" 

./upload_ftHodo_status.sh $indir/Status.txt $rmin $rmax "$comment"

