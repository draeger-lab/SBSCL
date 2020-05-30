#!/bin/bash
#################################################
# Download bigg models for testing
#
# usage:
#  source ./download_bigg_models.sh
#################################################

# This URL gives the updated BiGG Models from the repository
BIGG_MODELS_BASE_URL="https://github.com/matthiaskoenig/bigg-models-fba/raw/master/models"
FILE_NAME="bigg_models_v1.5.tar.gz"

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
cd $TEST_DIR/../resources/bigg
mkdir -p $TEST_DIR/../resources/bigg
cd $TEST_DIR/../resources/bigg
wget $BIGG_MODELS_BASE_URL/$FILE_NAME
tar xzf bigg_models_v1.5.tar.gz
rm bigg_models_v1.5.tar.gz

# set environment variable
export BIGG_MODELS_PATH=${TEST_DIR}/../resources/bigg/v1.5

echo $BIGG_MODELS_PATH

# move back
cd $_CWD
