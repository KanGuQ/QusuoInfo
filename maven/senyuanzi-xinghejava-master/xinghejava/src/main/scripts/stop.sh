#!/usr/bin/env bash

echo 'stop forcpace bj...'
kill $(ps -aux | grep forcpace-bj-api-1.0-SNAPSHOT.jar | grep -v grep|awk '{print $2}')

#------ 根据端口号停止服务 ------
#kill $(netstat -nlp | grep :23001 | awk '{print $7}' | awk -F"/" '{ print $1 }')