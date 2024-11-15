#!/bin/bash

JAVA_FILE="src/test/java/org/simulator/sbml/SBMLTestSuiteWrapper.java"
OUTPUT_DIR="target/generated-test-sources" #does this work as the output directory?
START_MODEL=1
END_MODEL=1 # Im not sure what the Start/End model should be
SBML_LEVEL=2
SBML_VERSION=3 # Im not sure what the SBML version and level should be


cd "/Users/arthur/Documents/Uni/HiWi-SBSCL/Code/SBSCL"
#mvn clean verify source:jar source:jar-no-fork javadoc:jar install:install gpg:sign deploy:deploy release:clean release:prepare release:perform deploy -Dmaven.test.skip=true
mvn clean compile -X

#compile the java file
echo "Attempting to compile java file"
if [ $? -ne 0 ]; then
    echo "Compiling failed"
    exit 1
fi
echo "Succesfully compiled!"


#run the java class file
echo "Attempting to run java class"

#CLASS_FILE="target/classes/org/testsuite/SBMLTestSuiteRunnerWrapper.class"
#XML_FILE="/Users/arthur/Documents/Uni/HiWi-SBSCL/Code/SBSCL/src/main/resources/examples/mapk.xml"
# variablees made redundant by maven implementation

mvn clean compile package assembly:single -X #is this the correct maven implementation? --> finish implementing LSODA Integrator to see if the test cases run!
#java -cp "$CLASS_FILE" "$XML_FILE" "$START_MODEL" "$OUTPUT_DIR" "$SBML_LEVEL" "$SBML_VERSION" "$END_MODEL" #not sure about the arguments here
if [ $? -ne 0 ]; then
    echo "executing java class failed"
    exit 1
fi


#echo "Test completed"

wirte coe which build mpak.xml file