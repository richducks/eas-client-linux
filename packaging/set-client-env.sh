#!/bin/sh

DIR_BIN=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
EAS_HOME=$(CDPATH= cd -- "$DIR_BIN/../.." && pwd)
export EAS_HOME
JAVA_HOME="$EAS_HOME/clientjdk"
export JAVA_HOME
UPDATE_SERVER=58.57.65.34:6888
export UPDATE_SERVER
EAS_SERVER=tcp://58.57.65.34:11034
export EAS_SERVER
JVM_INITIAL_HEAPSIZE=64
export JVM_INITIAL_HEAPSIZE
JVM_MAX_HEAPSIZE=512
export JVM_MAX_HEAPSIZE
ONDEMAND_UPDATE=true
export ONDEMAND_UPDATE
ENABLE_CDN=false
export ENABLE_CDN
preheatClient=false
export preheatClient
HTTPS_UPDATE=false
export HTTPS_UPDATE
