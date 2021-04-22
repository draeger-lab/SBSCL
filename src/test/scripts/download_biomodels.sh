#!/bin/bash
#################################################
# Download BioModels from Biomodels REST API
#
# usage:
#  source ./download_biomodels.sh
#################################################

set -eo pipefail

BIOMODELS_BASE_URL=https://www.ebi.ac.uk/biomodels/search/download
FILE_NAMES=BIOMD0000000001
for i in {2..9}
do
  FILE_NAMES=$FILE_NAMES%2CBIOMD000000000$i
done
for i in {10..50}
do
  FILE_NAMES=$FILE_NAMES%2CBIOMD00000000$i
done
ZIP_NAME=models.zip

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the biomodels
rm -rf $TEST_DIR/../resources/biomodels
mkdir -p $TEST_DIR/../resources/biomodels
cd $TEST_DIR/../resources/biomodels
curl -X GET "$BIOMODELS_BASE_URL?models=$FILE_NAMES" -H "accept: application/zip" -o $ZIP_NAME
unzip $ZIP_NAME
rm $ZIP_NAME

# set environment variable
export BIOMODELS_PATH=${TEST_DIR}/../resources/biomodels/

echo $BIOMODELS_PATH

# move back
cd $_CWD
