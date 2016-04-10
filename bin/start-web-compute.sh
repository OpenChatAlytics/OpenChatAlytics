#!/bin/sh

set -e

echo Starting web server...

nohup java -cp chatalytics-web-0.3-with-dependencies.jar:config -Dlogback.configurationFile=config/web/logback.xml com.chatalytics.web.ServerMain > /dev/null 2>&1 &

echo Starting compute server...

java -cp chatalytics-compute-0.3-with-dependencies.jar:config -Dlogback.configurationFile=config/compute/logback.xml com.chatalytics.compute.ChatAlyticsEngineMain
