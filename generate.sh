#!/bin/bash

# Locate ourselves, will fail on BSD/OS X because they don't have readlink -f
case "${OSTYPE}" in
  bsd*|darwin*)
    # BSD/OS X doesn't support readlink -f
    SCRIPT=$0
    while [ -L "${SCRIPT}" ];
    do
      SCRIPT=$(readlink "${SCRIPT}")
    done
    ;;
  *)
    # Can use readlink -f on standard Linux
    SCRIPT=$(readlink -f $0)
    ;;
esac
SCRIPT_DIR=$(dirname "${SCRIPT}")

# Check for the script
if [ ! -e "${SCRIPT_DIR}/target/lubm-uba.jar" ]; then
  echo "Failed to find required JAR lubm-uba.jar, pleae ensure you have done a mvn package in ${SCRIPT_DIR} first"
  exit 1
fi

# Exec the Java class
exec java ${JAVA_OPTS} -jar target/lubm-uba.jar $*
