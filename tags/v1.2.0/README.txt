$Id$
$URL$
$Rev$
---------------------------------------------------------------------
 This file is part of Simulation Core Library, a Java-based library
 for efficient numerical simulation of biological models.

 Copyright (C) 2007-2012 jointly by the following organizations:
 1. University of Tuebingen, Germany
 2. Keio University, Japan
 3. Harvard University, USA
 4. The University of Edinburgh
 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK

This library is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation. A copy of the license
agreement is provided in the file named "LICENSE.txt" included with
this software distribution and also available online as
<http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
---------------------------------------------------------------------

Please cite the original work and the authors when using this program.

===================
# Getting started #
===================

The folder doc/api contains a directory called version_XX, where XX stands for
the current version of the library. For an introduction of how to use this
library, please open the index.html file from this folder in your system's
web browser. There you can find instructions and source code examples, including
some use cases. The dist folder contains three different versions of JAR files
to work with.


===================
# File structure  #
===================

Just a brief explanation of the folders and files contained in this distribution.

Most importantly, see 
 * the dist folder containing the JAR files
 * the doc folder containing an exhaustive source code and api documentation

The package structure in more detail:
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


===================
# Troubleshooting #
===================

Please e-mail any bugs, problems, suggestions, or issues regarding this library
to the mailing list:

simulation-core-development@lists.sourceforge.net

Or use the bug tracker at

http://sourceforge.net/projects/simulation-core/

