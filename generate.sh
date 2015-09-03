#!/bin/bash

# Locate ourselves, will fail on BSD/OS X because they don't have readlink -f
SCRIPT=$(readlink -f $0)
SCRIPT_DIR=$(dirname "${SCRIPT}")

# Check for the script
if [ ! -e "${SCRIPT_DIR}/target/lubm-uba.jar" ]; then
  echo "Failed to find required JAR lubm-uba.jar, pleae ensure you have done a mvn package in ${SCRIPT_DIR} first"
  exit 1
fi

# Exec the Java class
exec java ${JAVA_OPTS} -cp target/lubm-uba.jar edu.lehigh.swat.bench.uba.Generator $*
