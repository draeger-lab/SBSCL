#!/bin/bash
#################################################
# Run a single test of SBML Test Suite and pass results to CSV file
#
# usage:
#  source ./sbml_test_suite_runner_wrapper.sh
#################################################

# lib directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $DIR/../../../target/classes/:$DIR/../../../src/lib/sbml_test_runner_wrapper/*:$DIR/../../../src/lib/maven/kisao/libkisao/1.0.3.1-rc/libkisao-1.0.3.1-rc.jar:$DIR/../../../src/lib/maven/scpsolver/GLPKSolverPack/4.35v2/GLPKSolverPack-4.35v2.jar:$DIR/../../../src/lib/maven/scpsolver/LPSOLVESolverPack/5.5.2.5/LPSOLVESolverPack-5.5.2.5.jar:$DIR/../../../src/lib/maven/scpsolver/SCPSolver/1.0v2/SCPSolver-1.0v2.jar org.simulator.examples.SBMLTestSuiteRunnerWrapper $1 $2 $3 $4 $5