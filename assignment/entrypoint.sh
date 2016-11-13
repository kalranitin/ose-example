#!/bin/bash
# /usr/bin/filebeat -c /etc/filebeat/filebeat.yml &

function runAppCommand(){
	java -jar app.jar
}

if [ -n "$LOG_FORWARD_HOST" ] && [ -n"$ES_INDEX" ] && [ -n "$LOG_SHIPPER_TAG" ] && [ $LOG_FORWARD_HOST != "localhost" ]; then
	sed -i -e 's/LOG_FORWARD_HOST/'$LOG_FORWARD_HOST'/g' /etc/filebeat/filebeat.yml
	sed -i -e 's/ES_INDEX/'$ES_INDEX'/g' /etc/filebeat/filebeat.yml
	sed -i -e 's/LOG_SHIPPER_TAG/'$LOG_SHIPPER_TAG'/g' /etc/filebeat/filebeat.yml
	
	runAppCommand | /usr/bin/filebeat -e -v -c /etc/filebeat/filebeat.yml
	
else
#	echo "plain"
	runAppCommand	
fi



#java -jar app.jar | /usr/bin/filebeat -e -v -c /etc/filebeat/filebeat.yml