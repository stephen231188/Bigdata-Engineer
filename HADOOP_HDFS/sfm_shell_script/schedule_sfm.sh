#scheduler & orchestration script
#nohup schedule_sfm.sh & #schedule the script in a background mode using & in the last and produce the stdoutput into the nohup.out file since we are using nohup.
while true
do
 echo "`date` Invoking the sfm_insuredata.sh"
 /home/hduser/sfm_insuredata.sh https://s3.amazonaws.com/inceptez.data/insurance/insuranceinfo.csv
 status=$?
 if [ $status -eq 0 ]
 then
  echo "`date` script is continuing"
 elif [ $status -eq 1 ]
 then
  echo "`date` Some issue occured in the data ingestion, hence terminating with the status of $status"
  echo "sending mail to the source system about the issue in ingestion"
  exit 1
 elif [ $status -eq 2 ]
 then
  echo "`date` Some issue in the data itself, with the status of $status"
  echo "sending mail to the source system about the issue in data"
 else 
  echo "`date` some other status occured $status"
 fi
 echo "`date` sfm_insuredata.sh iteration is completed"
 sleep 3600
done
