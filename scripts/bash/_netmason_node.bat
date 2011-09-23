#!/bin/bash

prefix=$1
cd $2
nice -n 10 /apps/java/default-i586/bin/java -Xmx1024M -jar netmason.jar $prefix 

