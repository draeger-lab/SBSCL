DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $DIR/../../../src/lib/sbml_test_runner_wrapper/GLPKSolverPack-4.35v2.jar:$DIR/../../../target/classes/:$DIR/../../../src/lib/sbml_test_runner_wrapper/* org.testsuite.SBMLTestSuiteRunnerWrapper_LSODA $1 $2 $3 $4 $5
