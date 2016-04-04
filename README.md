# The Systems Biology Simulation Core Library

#### Description
The Systems Biology Simulation Core Library (SBSCL) provides an efficient and exhaustive Java implementation of methods to interpret the content of models encoded in the Systems Biology Markup Language (SBML) and its numerical solution. This library is based on the JSBML project and can be used on every operating system for which a Java Virtual Machine is available. Please note that this project does not contain any user interface, neither a command-line interface, nor a graphical user interface. This project has been developed as a pure programming library. To support the MIASE effort, it understands SED-ML files. Its abstract type and interface hierarchy facilitates the implementation of further community standards, such as CellML.

When using this library, please cite: http://www.biomedcentral.com/1752-0509/7/55.

#### Categories
Bio-Informatics, Libraries, Simulations

#### Features
* Numerical simulation
* Ordinary differential equation solver
* Time-course analysis
* Systems Biology Markup Language
* Application programming interface


## Getting started

The folder doc/api contains a directory called version_XX, where XX stands for the current version of the library. For an introduction of how to use this library, please open the index.html file from this folder in your system's web browser. There you can find instructions and source code examples, including some use cases. The dist folder contains three different versions of JAR files to work with.

## File structure

Just a brief explanation of the folders and files contained in this distribution.

Most importantly, see 
 * the dist folder containing the JAR files
 * the doc folder containing an exhaustive source code and api documentation

The package structure in more detail:
```
 /
 |- dist        -> Contains three different JAR file versions of the library
 |- doc
    |- api      -> JavaDoc including examples for usage of the library
 |- lib         -> 3rd party libraries needed for compilation and execution
 |- licenses    -> License agreements of all 3rd party libs and a list of 
 |                 authors of this library
 |- resources   -> A source folder containing required resource files.
 |- src         -> The main source folder containing all Java files and the 
 |                 overview.html providing a brief overview of the project.
 |- test        -> Source code for testing, including BioModels and SBML Test
 |                 Suite
 |- build.xml   -> an Apache ANT script which compiles the source code and
 |                 provides several options to create distribution files.
 |- LICENSE.txt -> the license, under which this project is distributed
 |- pom.xml     -> Maven support for the project
 |- README.txt  -> this file
```

## Troubleshooting

Please e-mail any bugs, problems, suggestions, or issues regarding this library to the mailing list: simulation-core-development@lists.sourceforge.net

Or use the bug tracker at https://github.com/cogsys-tuebingen/SBSCL/issues
