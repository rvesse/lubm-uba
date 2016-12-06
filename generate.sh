#!/bin/bash

# Locate our directory
case "${OSTYPE}" in
  bsd*|darwin*)
    # BSD/OS X doesn't support readlink -f
    SCRIPT_DIR=$(dirname $0)
    while [ -L "${SCRIPT}" ];
    do
      SCRIPT=$(readlink "${SCRIPT}")
    done
    ;;
  *)
    # Can use readlink -f on standard Linux
    SCRIPT_DIR=$(readlink -f $(dirname $0))
    ;;
esac

# Check for the script
if [ ! -e "${SCRIPT_DIR}/target/lubm-uba.jar" ]; then
  echo "Failed to find required JAR lubm-uba.jar, pleae ensure you have done a mvn package in ${SCRIPT_DIR} first"
  exit 1
fi

# Exec the Java class
exec java ${JAVA_OPTS} -jar "${SCRIPT_DIR}/target/lubm-uba.jar" "$@"
