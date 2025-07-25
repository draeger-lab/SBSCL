#!/bin/bash
#################################################
# Run a single test of SBML Test Suite and pass results to CSV file
#
# usage:
#  source ./sbml_test_suite_runner_wrapper.sh
#################################################

# lib directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $DIR/../../../src/lib/sbml_test_runner_wrapper/GLPKSolverPack-4.35v2.jar:$DIR/../../../target/classes/:$DIR/../../../src/lib/sbml_test_runner_wrapper/* org.testsuite.SBMLTestSuiteRunnerWrapper_LSODA $1 $2 $3 $4 $5
