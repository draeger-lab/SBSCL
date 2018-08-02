# The Systems Biology Simulation Core Library

[![License (LGPL version 3)](https://img.shields.io/badge/license-LGPLv3.0-blue.svg?style=plastic)](http://opensource.org/licenses/LGPL-3.0)
[![Latest version](https://img.shields.io/badge/Latest_version-1.4.0-brightgreen.svg?style=plastic)](https://github.com/draeger-lab/SBSCL/releases/)
[![DOI](https://img.shields.io/badge/DOI-10.1186%2F1752--0509--7--55-blue.svg?style=plastic)](https://doi.org/10.1186/1752-0509-7-55)
[![Build Status](https://travis-ci.org/shalinshah1993/SBSCL.svg?branch=master&style=plastic)](https://travis-ci.org/shalinshah1993/SBSCL)

#### Description
The Systems Biology Simulation Core Library (SBSCL) provides an efficient and exhaustive Java™ implementation of methods to interpret the content of models encoded in the Systems Biology Markup Language ([SBML](http://sbml.org)) and its numerical solution. This library is based on the [JSBML](http://sbml.org/Software/JSBML) project and can be used on every operating system for which a Java Virtual Machine is available. Please note that this project does not contain any user interface, neither a command-line interface, nor a graphical user interface. This project has been developed as a pure programming library. To support the [MIASE](http://co.mbine.org/standards/miase) effort, it understands [SED-ML](http://sed-ml.org) files. Its abstract type and interface hierarchy facilitates the implementation of further community standards, such as [CellML](https://www.cellml.org).

When using this library, please cite: http://www.biomedcentral.com/1752-0509/7/55.

#### Categories
Bio-Informatics, Libraries, Simulations

#### Features
* Numerical simulation
* Ordinary differential equation solver
* Time-course analysis
* Systems Biology Markup Language
* Application programming interface

#### Licensing terms

This file is part of Simulation Core Library, a Java-based library for efficient numerical simulation of biological models.

Copyright (C) 2007 jointly held by the individual authors.

This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation. A copy of the license agreement is provided in the file named "LICENSE.txt" included with this software distribution and also available online as http://www.gnu.org/licenses/lgpl-3.0-standalone.html.

Please cite the original work and the authors when using this program. See the [project homepage](https://draeger-lab.github.io/SBSCL/) for details.

## Getting started

The folder doc/api contains a directory called version_XX, where XX stands for the current version of the library. For an introduction of how to use this library, please open the index.html file from this folder in your system's web browser. There you can find instructions and source code examples, including some use cases. The dist folder contains a JAR files of the simulation core library to work with.

## File structure

Just a brief explanation of the folders and files contained in this distribution.

Most importantly, see 
 * the dist folder containing the JAR files
 * the doc folder containing an exhaustive source code and api documentation

The package structure in more detail:
```
 /
 |- doc
    |- api         -> JavaDoc including examples for usage of the library
 |- src            -> The main source folder containing all the code and test files
    |- assembly    -> assembly files for maven plugins
    |- lib         -> 3rd party libraries needed for compilation and execution
    |- main        -> Core java files of simulation library
    |- test        -> JUnit test files along with resources required
    |- site		   -> Contains markup files, old javadoc, site.xml and other website
    				  resources 
 |- LICENSE.txt    -> the license, under which this project is distributed
 |- pom.xml        -> Maven file for building the project
 |- README.md      -> this file
```

## Troubleshooting

Please e-mail any bugs, problems, suggestions, or issues regarding this library to the mailing list: simulation-core-development@lists.sourceforge.net

Or use the bug tracker at https://github.com/draeger-lab/SBSCL/issues
