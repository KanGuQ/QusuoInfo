#!/usr/bin/env bash

echo 'starting forcpace-bj restapi...'

PORT=23008
DIRECTORY=/home/www/forcpace_bj_file_upload

java -jar forcpace-bj-api-1.0-SNAPSHOT.jar $PORT $DIRECTORY &