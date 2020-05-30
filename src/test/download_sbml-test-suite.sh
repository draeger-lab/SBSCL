#!/bin/bash
#################################################
# Download sbml-test-suite for testing
#
# usage:
#  source ./download_sbml-test-suite.sh
#################################################

SBML_TEST_SUITE_LINK="https://github.com/sbmlteam/sbml-test-suite/branches/develop/cases"

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
cd $TEST_DIR
rm -rf $TEST_DIR/resources/sbml-test-suite
mkdir -p $TEST_DIR/resources/sbml-test-suite
cd $TEST_DIR/resources/sbml-test-suite
svn checkout $SBML_TEST_SUITE_LINK

# set environment variable
export SBML_TEST_SUITE_PATH=${TEST_DIR}/resources/sbml-test-suite/cases/semantic/

echo $SBML_TEST_SUITE_PATH

# move back
cd $_CWD

exit 0
