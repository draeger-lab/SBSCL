#!/bin/bash
cd /Users/araki/dev/SBMLTestSuiteWrapper
CD4_COMPILE="ant build_cd4"
${CD4_COMPILE}
cd /Users/araki/dev/sbml-test-suite/cases/semantic/

TESTCASES="**"
TESTCASE_DIRS=()

for testcase in ${TESTCASES[@]}; do
	TESTCASE_DIRS+="${testcase} "
done

cd /Users/araki/dev/SBMLTestSuiteWrapper/bin/

BASE_EXEC_CMD_CD4="java -cp .:../lib/SimulationCoreLibrary_v1.4_incl-libs.jar:../lib/commons-math-2.2.jar:../lib/jsbml-1.6.1-with-dependencies.jar:../lib/jsbml-1.6.1.jar:../lib/junit-4.12.jar main.CD4.Main ../../sbml-test-suite/cases/semantic/"
BASE_OUTPUT_DIR="/Users/araki/dev/SBMLTestSuiteWrapper/results/CD4/"

DEFAULT_SBML_LEVEL="2"
DEFAULT_SBML_VERSION="4"

for DIR in ${TESTCASE_DIRS[@]}; do
	${BASE_EXEC_CMD_CD4} ${DIR} ${BASE_OUTPUT_DIR} ${DEFAULT_SBML_LEVEL} ${DEFAULT_SBML_VERSION}
done
