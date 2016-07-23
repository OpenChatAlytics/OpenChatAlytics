#!/bin/sh
set -e

# Kill all child processes if the main process exits
trap 'jobs -p | xargs kill' EXIT

if [ "$#" -ne 1 ]
then
    echo "You haven't specified the configuration file"
    exit 1
fi

config_file=$1

echo "Starting web server with configuration: $config_file..."

nohup java -cp\
    chatalytics-web-0.3-with-dependencies.jar:config\
    -Dlogback.configurationFile=config/web/logback.xml com.chatalytics.web.ServerMain\
    -c $config_file > /dev/null 2>&1 &

echo "Starting compute server with configuration: $config_file..."

java -cp\
    chatalytics-compute-0.3-with-dependencies.jar:config\
    -Dlogback.configurationFile=config/compute/logback.xml com.chatalytics.compute.ChatAlyticsEngineMain\
    -c $config_file 2>&1 > /dev/null
