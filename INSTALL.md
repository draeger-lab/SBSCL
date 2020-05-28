# Installation
This document provides important information on how to setup and install SBSCL and its optional dependencies.


SBSCL can be build with maven. To build without running the tests use
```
mvn clean install -DskipTests
```

The tests require additional resources which have to be downloaded. These include the sbml-testsuite and the BiGG models.
```
source ./src/test/download_bigg_models.sh
source ./src/test/download_sbml-test-suite.sh
```
which then can be run as part of the build step
```
mvn clean install
```

## SBML Test Runner
The SBML Test Runner is a standalone desktop application with a graphical user interface (GUI) for controlling SBML-compatible applications and making them run test cases from the [SBML Test Suite](http://sbml.org/Software/SBML_Test_Suite). It is written in Java with the Eclipse SWT widget toolkit and can be used on macOS, Linux and Windows.

To use SBML Test Runner to simulate the SBML Test Suite with SBSCL, you need to install SBML Test Runner to your device. Follow this [link](https://github.com/sbmlteam/sbml-test-suite/tree/master/src/test-runner/testsuite-ui) for installation guidelines.

After installing the SBML Test Runner, you need to add a wrapper to simulate the SBML Test Suite files using SBSCL simulator.
- When you first start the Runner, you will not have any wrapper configurations defined except for one pseudo-wrapper definition named `-- no wrapper --`.
- To add for SBSCL, go to `File->Options/Wrappers` or just press `Ctrl + K`.
- Click Add option.
- Then just add the Wrapper path (absolute path to `sbml_test_suite_runner_wrapper.sh` present in SBSCL source code) and output directory path (wherever you want to store the results of the simulator).
- And, just click on Save and it's done!
- Now, you can simulate any of the SBML Test Suite case and see the results and the distance plot (in reference to predefined solutions from SBML Test Suite) in the test runner.

For complete guidelines about the SBML Test Runner and the wrapper, go to the [SBML Test Runner repository](https://github.com/sbmlteam/sbml-test-suite/tree/master/src/test-runner/testsuite-ui).  


## LP solver
For running flux balance analysis simulations a LP solver is required. The respective jars have to be made available.

Download the GLPK Solver Pack `GLPKSolverPack.jar` (please note the GPL licensing terms)
and LPSOLVE Solver Pack `LPSOLVESolverPack.jar` (please note the LGPL licensing terms) from
http://scpsolver.org/ and add in the library folder according to the `pom.xml`.


### GLPK

```
sudo apt-get install libglpk-dev
sudo apt-get install libglpk-java
```

File -> Project Structure -> Libraries

/usr/lib/x86_64-linux-gnu/jni/libglpk_java.a
/usr/lib/x86_64-linux-gnu/jni/libglpk_java.so
/usr/lib/x86_64-linux-gnu/jni/libglpk_java.so.36
/usr/lib/x86_64-linux-gnu/jni/libglpk_java.so.36.2.0


The artifact does not include the binary libraries, which have to be installed separately.
When testing with Maven it may be necessary to indicate the installation path of the GLPK for Java shared library (.so or .dll).
```
mvn clean install -DargLine='-Djava.library.path=/usr/local/lib/jni:/usr/lib/jni'
```


### cplex
- get academic license, install, add cplex jar to cplex folder.
