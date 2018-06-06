#!/bin/bash
#
# This script can be used to verify that any modifications to the data generator
# will result in the same output as the original version
#
# It includes verifying all data formats (except DAML which no-one uses anyway)
# and verifies both consolidated and non-consolidated outputs
#
# It does not verify compressed outputs since that simply uses GZip and we assume
# that the JDK GZip implementation is correct
#
# Pre-requisites
#
# - Maven is on your path
# - The rdfdiff and riot tools (from Apache Jena) are available either on the PATH
#   or JENA_HOME is set such that they can be located
# - There is (or can be created) a temporary directory at /tmp/lubm/ under which
#   sub-directories can be created

function verifyTool() {
  local TOOL=$1
  local TRY_JENA_HOME=$2

  which ${TOOL} >/dev/null
  if [ $? -ne 0 ]; then
    if [ -n "${TRY_JENA_HOME}" ]; then
      if [ -n "${JENA_HOME}" ]; then
        export PATH="${PATH}:${JENA_HOME}/bin"
        which ${TOOL} >/dev/null
        if [ $? -ne 0 ]; then
          echo "Unable to locate required ${TOOL} tool either on your PATH or at JENA_HOME/bin"
          exit 1
        fi
      else
        echo "Unable to locate required ${TOOL} tool on your PATH and JENA_HOME is not set"
        exit 1
      fi
    else
      echo "Unable to locate required ${TOOL} tool on your PATH"
      exit 1
    fi
  fi
}

function createTempDir() {
  local DIR=$1

  if [ ! -d "${DIR}" ]; then
    mkdir -p "${DIR}"
    if [ $? -ne 0 ]; then
      echo "Unable to create required temporary directory ${DIR}"
      exit 1
    fi
  fi
}

verifyTool mvn
verifyTool rdfdiff "yes"
verifyTool riot "yes"

NUM="${1:-10}"
ONTO_URL="http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"



# Use the original code to generate data
git checkout master
mvn clean package

BASE="/tmp/lubm/"
createTempDir "${BASE}"
ORIG_BASE="${BASE}orig/"
createTempDir "${ORIG_BASE}"
rm -f ${ORIG_BASE}*.owl
./generate.sh -onto "${ONTO_URL}" -univ ${NUM} -out "${ORIG_BASE}"

# Switch to the improved code for the rest of the testing
git checkout improved
mvn clean package

function ext() {
  case $1 in
    OWL)
      echo ".owl"
      ;;
    NTRIPLES)
      echo ".nt"
      ;;
    TURTLE)
      echo ".ttl"
      ;;
    *)
      echo "Unknown format"
      exit 1
  esac
}

function lang() {
  case $1 in
    OWL)
      echo "RDF/XML"
      ;;
    NTRIPLES|TURTLE)
      # Have to use N3 even for N-Triples as otherwise Jena won't do any
      # Base URI resolution and we'll get false reports of non-matching data
      echo "N3"
      ;;
    *)
      echo "Unknown format"
      exit 1
  esac
}

FORMATS="OWL NTRIPLES TURTLE"
FORMATS_ARRAY=(${FORMATS})

for FORMAT in ${FORMATS_ARRAY[@]};
do
  echo "Generating data in ${FORMAT} format"

  EXT=$(ext "${FORMAT}")
  LANG=$(lang "${FORMAT}")

  

  ST_BASE="${BASE}improved/single-threaded"
  createTempDir "${ST_BASE}"
  rm -f ${ST_BASE}/${FORMAT}/*${EXT}
  ./generate.sh --univ ${NUM} --output ${ST_BASE}/${FORMAT} --onto "${ONTO_URL}" --format ${FORMAT} --timing --quiet

  MT_BASE="${BASE}improved/multi-threaded"
  createTempDir "${MT_BASE}/${FORMAT}"
  rm -f ${MT_BASE}/${FORMAT}/*${EXT}
  ./generate.sh --univ ${NUM} --output ${MT_BASE}/${FORMAT} --threads 8 --onto "${ONTO_URL}" --format ${FORMAT} --timing --quiet

  CON_SOME_BASE="${BASE}improved/consolidated/some"
  createTempDir "${CON_SOME_BASE}/${FORMAT}"
  rm -f ${CON_SOME_BASE}/${FORMAT}/*${EXT}
  rm -f ${CON_SOME_BASE}/${FORMAT}/*_orig.nt
  ./generate.sh --univ ${NUM} --output ${CON_SOME_BASE}/${FORMAT} --threads 8 --onto "${ONTO_URL}" --format ${FORMAT} --timing --quiet --consolidate Partial

  CON_FULL_BASE="${BASE}improved/consolidated/full"
  createTempDir "${CON_FULL_BASE}/${FORMAT}"
  rm -f ${CON_FULL_BASE}/${FORMAT}/*${EXT}
  rm -f ${CON_SOME_BASE}/${FORMAT}/*_orig.nt
  ./generate.sh --univ ${NUM} --output ${CON_FULL_BASE}/${FORMAT} --threads 8 --onto "${ONTO_URL}" --format ${FORMAT} --timing --quiet --consolidate Full

  

  # Check outputs
  for FILE in $(ls ${ORIG_BASE}/*.owl);
  do
    NAME=$(basename "${FILE}")
    OUT_NAME=${NAME%%.owl}
    if [ ! -e "${ST_BASE}/${FORMAT}/${OUT_NAME}${EXT}" ]; then
      echo "Missing file ${ST_BASE}/${FORMAT}/${OUT_NAME}${EXT} from improved single-threaded output"
      exit 1
    fi

    rdfdiff ${BASE}/orig/${NAME} ${ST_BASE}/${FORMAT}/${OUT_NAME}${EXT} RDF/XML ${LANG} http://example.org/ http://example.org/
    if [ $? -ne 0 ]; then
      echo "File ${ST_BASE}/${FORMAT}/${OUT_NAME}${EXT} from improved single-threaded output is different from ${FILE}"
      exit 1
    fi
  
    if [ ! -e "${MT_BASE}/${FORMAT}/${OUT_NAME}${EXT}" ]; then
      echo "Missing file ${MT_BASE}/${FORMAT}/${OUT_NAME}${EXT} from improved multi-threaded output"
      exit 1
    fi

    rdfdiff /tmp/lubm/orig/${NAME} ${MT_BASE}/${FORMAT}/${OUT_NAME}${EXT} RDF/XML ${LANG} http://example.org/ http://example.org/
    if [ $? -ne 0 ]; then
      echo "File ${OUT_NAME}${EXT} from improved multi-threaded output is different from ${FILE}"
      echo rdfdiff /tmp/lubm/orig/${NAME} ${MT_BASE}/${FORMAT}/${OUT_NAME}${EXT} RDF/XML ${LANG} http://example.org/ http://example.org/
      exit 1
    fi

    OUT_NAME=${OUT_NAME%%_*}
    if [ ! -e "${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}${EXT}" ]; then
      echo "Missing file ${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}${EXT} from improved consolidated output"
      exit 1
    fi

    # Prepare the concatenated output for testing
    
    CONSOLIDATED_FILE="${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}_orig.nt"
    if [ ! -e "${CONSOLIDATED_FILE}" ]; then
      rm errors.txt
      riot --quiet --stream=N-TRIPLE --base=http://example.org/ ${ORIG_BASE}/${OUT_NAME}_* > "${CONSOLIDATED_FILE}" 2> errors.txt
      grep "ERROR" errors.txt >/dev/null
      if [ $? -eq 0 ]; then
        echo "RIOT failed to parse ${ORIG_BASE}/${OUT_NAME}_* original data files"
        cat errors.txt
        echo "riot --quiet --stream=N-TRIPLE --base=http://example.org/ ${ORIG_BASE}/${OUT_NAME}_* > ${CONSOLIDATED_FILE}"
        exit 1
      fi

      rdfdiff "${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}_orig.nt" "${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}${EXT}" N3 ${LANG} http://example.org/ http://example.org/
      if [ $? -ne 0 ]; then
        echo "File ${OUT_NAME}${EXT} from partially consolidated output is different from non-consolidated outputs"
        echo rdfdiff "${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}_orig.nt" "${CON_SOME_BASE}/${FORMAT}/${OUT_NAME}${EXT}" N3 ${LANG} http://example.org/ http://example.org/
        exit 1
      fi
    fi
    
    CONSOLIDATED_FILE="${CON_FULL_BASE}/${FORMAT}/Universities_orig.nt"
    if [ ! -e "${CONSOLIDATED_FILE}" ]; then
      rm errors.txt
      riot --quiet --stream=N-TRIPLE --base=http://example.org/ ${ORIG_BASE}/* > "${CONSOLIDATED_FILE}" 2> errors.txt
      grep "ERROR" errors.txt >/dev/null
      if [ $? -eq 0 ]; then
        echo "RIOT failed to parse ${ORIG_BASE}/* consolidated data files"
        cat errors.txt
        echo "riot --quiet --stream=N-TRIPLE --base=http://example.org/ ${ORIG_BASE}/* > ${CONSOLIDATED_FILE}"
        exit 1
      fi
      rm errors.txt
      riot --quiet --stream=N-TRIPLE --syntax=${LANG} --base=http://example.org/ ${CON_FULL_BASE}/${FORMAT}/Universities-*${EXT} > "${CON_FULL_BASE}/${FORMAT}/Universities_full.nt" 2> errors.txt
      grep "ERROR" errors.txt >/dev/null
      if [ $? -eq 0 ]; then
        echo "RIOT failed to parse ${CON_FULL_BASE}/${FORMAT}/Universities-*${EXT} consolidated data files"
        cat errors.txt
        echo "riot --quiet --stream=N-TRIPLE --syntax=${LANG} --base=http://example.org/ ${CON_FULL_BASE}/${FORMAT}/Universities-*${EXT} > ${CON_FULL_BASE}/${FORMAT}/Universities_full.nt"
        exit 1
      fi
      rm errors.txt

      rdfdiff "${CONSOLIDATED_FILE}" "${CON_FULL_BASE}/${FORMAT}/Universities_full.nt" N3 N3 http://example.org/ http://example.org/
      if [ $? -ne 0 ]; then
        echo "Fully consolidated output is different from non-consolidated outputs"
        echo rdfdiff "${CONSOLIDATED_FILE}" "${CON_FULL_BASE}/${FORMAT}/Universities_full.nt" N3 N3 http://example.org/ http://example.org/
        exit 1
      fi
    fi
  done

  echo "Output Format ${FORMAT} is all OK"

  # Clean Up
  rm -f "${ST_BASE}/${FORMAT}/*${EXT}"
  rm -f "${MT_BASE}/${FORMAT}/*${EXT}"
  rm -f "${CON_SOME_BASE}/${FORMAT}/*${EXT}"
  rm -f "${CON_FULL_BASE}/${FORMAT}/*${EXT}"
done



echo "All output formats were OK"
rm -f /tmp/lubm/orig/*.owl
exit 0
