#!/bin/bash -ex

bash $WORKSPACE/gradlew build
cf login -a http://api.192.168.4.14.xip.io -u admin -p password -o newwave -s dev
cf push --no-start
APP_NAME=`cat manifest.yml | grep -i name | awk '{ print $3 }'`
cf restage $APP_NAME
cf start $APP_NAME