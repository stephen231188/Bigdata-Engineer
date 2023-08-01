#!/bin/bash
set -x

#placeholders
#$# total number of arguments we are passing to a shell script
#$0 will hold the script name
#$1 will hold 1st argument


echo "Total number of arguments passed to this script is $#"
echo "The name of the script is $0"

if [ $# -ge 2 ]
then
echo "hello world"
echo "$1 $2 Batch "
else
echo "Please pass the enough arguments to run this script"
echo "usage: testscript.sh greeting_message batchnumber"
exit 100
fi

echo "script proceed further"


#Actual business logic starts here

#mkdir -p /tmp/clouddata
echo "`date` Script started running " > /tmp/sfm_${dt}.log

#Ensure the staging path in lfs is available, if not create
#mkdir -p /tmp/clouddata
if [ -d /tmp/clouddata ]
then
echo "Landing Pad is present" >> /tmp/sfm_${dt}.log
else
mkdir /tmp/clouddata
#chmod -R 700 /tmp/clouddata
echo "Landing Pad is not present, hence created" >> /tmp/sfm_${dt}.log
fi

#Ensure the archive path in lfs is available, if not create
#mkdir -p /tmp/clouddata/archive
if [ -d /tmp/clouddata/archive ]
then
echo "archival path exists"
else
mkdir /tmp/clouddata/archive
echo "archive Path is not present, hence created"
fi
