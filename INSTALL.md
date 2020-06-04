# Installation
This document provides important information on how to setup and install SBSCL and its optional dependencies.


SBSCL can be build with maven. To build without running the tests use
```
mvn clean install -DskipTests
```

The tests require additional resources which have to be downloaded. These include the [sbml-testsuite](https://github.com/sbmlteam/sbml-test-suite) and the [BiGG models](https://github.com/matthiaskoenig/bigg-models-fba).
```
source ./src/test/scripts/download_bigg_models.sh
source ./src/test/scripts/download_sbml-test-suite.sh
```
which then can be run as part of the build step
```
mvn clean install
```

## SBML Test Runner
The SBML Test Runner is a standalone desktop application with a graphical user interface (GUI) for controlling SBML-compatible applications and making them run test cases from the [SBML Test Suite](http://sbml.org/Software/SBML_Test_Suite).

To use SBML Test Runner to simulate the SBML Test Suite with SBSCL, you need to install SBML Test Runner to your device. Follow this [link](https://github.com/sbmlteam/sbml-test-suite/tree/master/src/test-runner/testsuite-ui) for installation guidelines.

After installing the SBML Test Runner, you need to add a wrapper to simulate the SBML Test Suite files using SBSCL simulator.
- When you first start the Runner, you will not have any wrapper configurations defined except for one pseudo-wrapper definition named `-- no wrapper --`.
- To add for SBSCL, go to `File->Options/Wrappers` or just press `Ctrl + K`.
- Click Add option.
- Then just add the Wrapper path (absolute path to `sbml_test_suite_runner_wrapper.sh` present in SBSCL source code) and output directory path (wherever you want to store the results of the simulator).
- And, just click on Save and it's done!
- Now, you can simulate any of the SBML Test Suite case and see the results and the distance plot (in reference to predefined solutions from SBML Test Suite) in the test runner.
- The distance plots in the test runner is calculated under the criteria given at this [link](http://sbml.org/Software/SBML_Test_Suite/Case_Descriptions#The_.22settings.22_file).

For complete guidelines about the SBML Test Runner and the wrapper, go to the [SBML Test Runner repository](https://github.com/sbmlteam/sbml-test-suite/tree/master/src/test-runner/testsuite-ui).  


## LP solver
For running flux balance analysis simulations a LP solver is required. The respective jars have to be made available.

Download the [SCPSolver.jar](https://bitbucket.org/hplanatscher/scpsolver/downloads/SCPSolver.jar) and a solver pack:
- GLPK Solver Pack: [GLPKSolverPack.jar](https://bitbucket.org/hplanatscher/scpsolver/downloads/GLPKSolverPack.jar) (please note the GPL licensing terms)
- LPSOLVE Solver Pack: [LPSOLVESolverPack.jar](https://bitbucket.org/hplanatscher/scpsolver/downloads/LPSOLVESolverPack.jar) (please note the LGPL licensing terms)

After downloading the jars, add them in the library folder according to the `pom.xml`.

Complete guide about SCPSolver can be found at http://scpsolver.org/.


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
