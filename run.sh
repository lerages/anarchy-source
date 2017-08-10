#!/bin/bash
./gradlew clean build installApp
./build/install/server/bin/server -Xmx2048m -Xms512m
# java -Xmx1024m -cp build/classes:build/install/server/lib/*:extradeps/*: org.rs2server.Server
