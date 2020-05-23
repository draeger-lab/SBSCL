#!/bin/bash
#################################################
# Download bigg models for testing
#
# usage:
#  source ./download_bigg_models.sh
#################################################

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
cd TEST_DIR
mkdir -p $TEST_DIR/resources/bigg
cd $TEST_DIR/resources/bigg
wget https://github.com/matthiaskoenig/bigg-models-fba/raw/master/models/bigg_models_v1.5.tar.gz
tar xzf bigg_models_v1.5.tar.gz
rm bigg_models_v1.5.tar.gz

# set environment variable
export BIGG_MODELS_PATH=${TEST_DIR}/resources/bigg/v1.5

echo $BIGG_MODELS_PATH

# move back
cd $_CWD
