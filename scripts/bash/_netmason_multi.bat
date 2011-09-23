#!/bin/bash


for node in `seq 20 50`
do           
	for process in 1 2 3
	do
		prefix="node${node}_$process"
		command="sh ./_netmason_node.bat ${prefix} $1"
		echo $command
		ssh -n node$node $command &
	done
done