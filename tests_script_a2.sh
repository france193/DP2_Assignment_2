#!/bin/bash

COUNTER=200

while [  $COUNTER -lt 250 ]; do
  echo " > Doing the $COUNTER execution..."

  time="$(date +%H%M%S)"

  ant runFuncTest -Dtestcase=0 -Dseed="$time" > ./tests/"$COUNTER"_test0_"$time"_dp2_a2.txt
  echo " > 1/3..."
  ant runFuncTest -Dtestcase=1 -Dseed="$time" > ./tests/"$COUNTER"_test1_"$time"_dp2_a2.txt
  echo " > 2/3..."
  ant runFuncTest -Dtestcase=2 -Dseed="$time" > ./tests/"$COUNTER"_test2_"$time"_dp2_a2.txt
  echo " > 3/3..."

  let COUNTER=COUNTER+1

  echo " > Done!"

done
