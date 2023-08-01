#!/bin/bash
#set -x

if [ $# -ge 3 ]
then
echo "proceeding further for execution"
else
echo "script needs 3 arguments, usage: scriptname.sh hello teamname filename , hence not proceeding further for execution, exiting with status code of 100"
exit 100
fi

echo "the script $0 is running"
echo "total number of parameters passed to this script $#"
echo "first parameter passed to the script $1"
echo $1 $2

ls $3
status=$?

if [ $status -eq 0 ]
then
echo "file is present "
else
echo "file is not present"
fi

#placeholders
#for getting the number of parameters we are passing to a shell script $#
#for getting the script name $0
#for getting the first parameter passed to the script $1
#for getting the second parameter passed to the script $2
#to identify the status of the command executed priorly $?
