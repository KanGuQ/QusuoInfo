@echo off
title 小斐配单正式
set PORT=23008
set DIRECTORY=C:\Project\forcpace-bj-file-upload
java -jar forcpace-bj-api-1.0-SNAPSHOT.jar %PORT% %DIRECTORY%
@pause