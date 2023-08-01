#!/bin/bash
#Shebang to represent this is bash shell based script

#debugger to understand the step by step execution of this script for learning, testing, debugging of the code
#prints the command along with the output
#set -x

#comments
######################################################
#Created by : Irfan
#Created on : 24/01/23
#modified by: Satheesh
#Modified on : 04/02/23
#usage: bash sfm_insuredata.sh https://s3.amazonaws.com/inceptez.data/insurance/insuranceinfo.csv
#description: used to automate the SFM (source (cloud s3) -> ingestion(pull)-> staging(lfs)->data validate ->push->hdfs(datalake)->availability validate (trigger file mechanism)->processing/analysing (consumers))
#Modification history: 04/02/23 - adding purging stratgy.
######################################################

#Define some variables (local or environment)
dt=`date '+%Y%m%d%H'`

#placeholders
#$# total number of arguments we are passing to a shell script
#$0 will hold the script name
#$1 will hold 1st argument

#Conditional check before executing the script
#check whether the enough arguments are passed to execute the script
if [ $# -ne 1 ]
then
echo "missing the expected arguments, usage: /home/hduser/sfm_insuredata.sh https://s3.amazonaws.com/inceptez.data/insurance/insuranceinfo.csv"
exit 10
fi

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
chmod -R 770 /tmp/clouddata
echo "Landing Pad is not present, hence created" >> /tmp/sfm_${dt}.log
fi

#Ensure the archive path in lfs is available, if not create
#mkdir -p /tmp/clouddata/archive
if [ -d /tmp/clouddata/archive ]
then
echo "archival path exists"
else
mkdir /tmp/clouddata/archive
chmod -R 770 /tmp/clouddata
echo "archive Path is not present, hence created"
fi

#Data ingestion from cloud storage external source system (AWS S3) to LFS landing pad is starting (Staging data)
echo "`date` started importing data from cloud" >> /tmp/sfm_${dt}.log
wget $1 -O /tmp/clouddata/creditcard_insurance

if [ $? -eq 0 ]
then
echo "`date` import of data from cloud is completed" #Standard Output
echo "`date` import of data from cloud is completed" >> /tmp/sfm_${dt}.log
else
echo "no data imported from cloud  or failed to import "
echo "`date` no data imported from cloud or failed to import" >> /tmp/sfm_${dt}.log
exit 1
fi

#Data validation starts at staging layer level
if [ -f /tmp/clouddata/creditcard_insurance ]
then
 echo "`date` file is present, proceeding further"
 echo "`date` file is present, proceeding further" >> /tmp/sfm_${dt}.log
 mv /tmp/clouddata/creditcard_insurance /tmp/clouddata/creditcard_insurance_$dt #renaming the file by appending date portion to it

 #Source Data validation starts here
 trlcnt=`tail -1 /tmp/clouddata/creditcard_insurance_${dt} | awk -F'|' '{ print $2 }'` #Identify the trl value provided by the source system in the last line
 filecnt=`cat /tmp/clouddata/creditcard_insurance_${dt} | wc -l` #identify the total count of the file
 echo "trailer count is $trlcnt"
 echo "file count is $filecnt"
 if [ $trlcnt -ne $filecnt ] #compare both counts are matching, then continue the script further, else fail the script and move the data to reject folder and come out with exit 2
 then
  echo "`date` moving to reject, file have invalid data" >> /tmp/sfm_${dt}.log
  mkdir -p /tmp/clouddata/reject
  mv /tmp/clouddata/creditcard_insurance_${dt} /tmp/clouddata/reject/
  exit 2
 fi

 echo "`date` Remove the trailer line in the file"
 echo "`date` Remove the trailer line in the file" >> /tmp/sfm_${dt}.log
 sed -i '$d' /tmp/clouddata/creditcard_insurance_${dt}

 #Copy the staging data to the Datalake starts here
 hadoop fs -mkdir -p /user/hduser/insurance_clouddata/

 echo "Check whether the above (hdfs datalake) dir is created in Hadoop"
 hadoop fs -test -d /user/hduser/insurance_clouddata
 if [ $? -eq 0 ]
 then
  echo "`date` hadoop directory is exists/created " >> /tmp/sfm_${dt}.log
 else
  echo "`date`  failed to create the hadoop directory /user/hduser/clouddata " >> /tmp/sfm_${dt}.log
  exit 3
 fi

 hadoop fs -mkdir -p /user/hduser/insurance_clouddata/datadt=${dt}
 hadoop fs -D dfs.block.size=67108864 -copyFromLocal -f /tmp/clouddata/creditcard_insurance_${dt} /user/hduser/insurance_clouddata/datadt=${dt}/
 if [ $? -eq 0 ]
 then
  hadoop fs -touchz /user/hduser/insurance_clouddata/_SUCCESS
  echo "Data copied to HDFS successfully"
  echo "`date` Data copied to HDFS successfully" >>  /tmp/sfm_${dt}.log
 else
  echo "Failed to copy data to HDFS `date` " >> /tmp/sfm_${dt}.log
 fi

#Post Script clean up activity for Archive

 echo "`date` moving to linux archive after compressing" >> /tmp/sfm_${dt}.log
 gzip /tmp/clouddata/creditcard_insurance_${dt}
 mv /tmp/clouddata/creditcard_insurance_${dt}.gz /tmp/clouddata/archive/
 echo "`date` Data ingestion completed " 
 echo "`date` Data ingestion completed " >> /tmp/sfm_${dt}.log
else
 echo "No data to process"
 echo "`date` No data to process" >>  /tmp/sfm_${dt}.log
 exit 4
fi

#Consider as a seperate shell script - Below stuffs can be learned later after hive learning..

echo "After the data imported, the consumers may use the below code to check whether the data imported for the given hour and then take it forward for their need of analysis/reporting"

lastsuccessfile=`hadoop fs -ls /user/hduser/insurance_clouddata/_SUCCESS | awk ' { print $7 } ' | awk -F':' ' {print $1 } '`
echo "the hour when the last success file created is $lastsuccessfile"

currenthr=`date '+%H'`

if [ $lastsuccessfile -eq $currenthr ]
then
echo "Procceding further to consume the data by the Downstream systems"
#hive -e "alter table custpart add partition(datadt='$dt',hr='$dt')"
else
echo "Better luck in the  next iteration of the program"
fi
exit 0

#if [ $lastsuccessfile -eq 08 ]; then echo "The 8 AM file got imported from cloud"; else echo "waiting for 8 AM file"; fi


if [ $dt -eq 23 ]
then
hive -e "msck repair table custpart"
fi








