#!/bin/bash
#################################################
# Download sbml-test-suite for testing
#
# usage:
#  source ./download_sbml-test-suite.sh
#################################################

TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
mkdir -p resources/sbml-test-suite
cd resources/sbml-test-suite
wget https://github.com/sbmlteam/sbml-test-suite/releases/download/3.3.0/sbml-semantic-test-cases-2017-12-12.zip
unzip sbml-semantic-test-cases-2017-12-12.zip
rm sbml-semantic-test-cases-2017-12-12.zip

# set environment variable
export SBML_TEST_SUITE_PATH=${TEST_DIR}/resources/sbml-test-suite/cases/semantic/

echo $SBML_TEST_SUITE_PATH

# move back
cd $TEST_DIR






