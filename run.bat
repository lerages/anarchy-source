@echo off
gradlew clean build installApp
build/install/server/bin/server.bat -Xmx2048m -Xms512m
pause