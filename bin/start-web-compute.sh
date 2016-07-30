#!/bin/sh
set -e

if [ "$#" -ne 1 ]
then
    echo "You haven't specified the configuration file"
    exit 1
fi

config_file=$1

echo "Starting compute server with configuration: $config_file..."

nohup java -cp\
    chatalytics-compute-0.3-with-dependencies.jar:config\
    -Dlogback.configurationFile=config/compute/logback.xml com.chatalytics.compute.ChatAlyticsEngineMain\
    -c $config_file 2>&1 > /dev/null &

sleep_time_secs=15

echo "Sleeping for $sleep_time_secs waiting for compute to start up"

sleep $sleep_time_secs

echo "Starting web server with configuration: $config_file..."

java -cp\
    chatalytics-web-0.3-with-dependencies.jar:config\
    -Dlogback.configurationFile=config/web/logback.xml com.chatalytics.web.ServerMain\
    -c $config_file
