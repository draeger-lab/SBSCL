#!/bin/bash
#################################################
# Run a single test of SBML Test Suite and pass results to CSV file
#
# usage:
#  source ./sbml_test_suite_runner_wrapper.sh
#################################################

java -cp target/classes/:src/lib/sbml_test_runner_wrapper/* org.simulator.examples.SBMLTestSuiteRunnerWrapper $1 $2 $3 $4 $5