#!/bin/bash
#################################################
# Run a single test of SBML Test Suite and pass results to CSV file
#
# usage:
#  source ./sbml_test_suite_runner_wrapper.sh
#################################################

# lib directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp "$DIR/../../../target/classes:$DIR/../../../src/lib/sbml_test_runner_wrapper/*" org.testsuite.SBMLTestSuiteRunnerWrapper "$@"
