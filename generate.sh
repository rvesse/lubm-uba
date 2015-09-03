#!/bin/bash

java ${JAVA_OPTS} -cp target/uba-1.7.0.jar edu.lehigh.swat.bench.uba.Generator $*
