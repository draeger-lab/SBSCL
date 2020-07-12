# Framework for Evaluation of Reaction Networks

FERN (Framework for Evaluation of Reaction Networks) is an extensible and comprehensive framework for efficient simulations and analysis of chemical reaction networks written in Java. It includes state of the art algorithms for stochastic simulation and a powerful visualization system based on gnuplot and Cytoscape.

Although FERN consists of more than 100 classes and interfaces, most classes are basically just implementations of one of three major interfaces and abstract classes.

-   The interface  **Network**  provides the network structure of the model.
-   The abstract class  **Simulator**  performs simulations on a  **Network**. It additionally calls the registered observers during the simulation run.
-   The abstract class  **Observer**  traces the simulation progress and creates the simulation output.

# Table of Contents
1. [File structure](#file-structure)
2. [Availability and Requirements](#availability-and-requirements)
3. [Using FERN in your project](#using-fern-in-your-project)
4. [SBML support](#sbml-support)
5. [Using the start scripts](#using-the-start-scripts)
6. [Discovering the examples](#discovering-the-examples)
7. [Compile Errors regarding SBML/Cytoscape](#compile-errors-regarding-sbmlcytoscape)
8. [Using the Cytoscape/CellDesigner plugin](#using-the-cytoscapecelldesigner-plugin)
9. [Gnuplot](#gnuplot)
10. [Windows users](#windows-users)

# File structure

```
doc/javadoc/			contains the complete javadoc of fern
doc/slides.pdf 			presentation about FERN's structure / features
doc/guide.pdf  			user guide
doc/cytoscape.pdf		documentation / tutorial for the Cytoscape plugin

lib                             3rd party libraries needed for compilation and execution

src/main/java/fern		source code
src/main/resources/examples     example FernML and SBML files
src/test/java                   test files for FERN 			

LICENSE                         license under which project is distributed
pom.xml                         Maven file for building the project
README.md                       this file
start* 				examples of running FERN's command line class under different environments
```

[Copyright for the Colt package.](http://dsd.lbl.gov/~hoschek/colt/)
Copyright (c) 1999 CERN - European Organization for Nuclear Research.

Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. CERN makes no representations about the suitability of this software for any purpose. It is provided "as is" without expressed or implied warranty. 

# Availability and Requirements
FERN is freely available under the GNU Lesser General Public License (LGPL) for academic users. 
For non-academic use a license is required.

FERN requires Java 5.0 or higher. If you don't have an up-to-date Java environment on your computer, we highly recommend to update to the newest [version](http://java.sun.com/javase/downloads/index.jsp). You can discover your Java version by typing

	java -version

into a console (or the MSDOS command line if you are running windows - see also [section 10](#windows-users))

It additionally requires the [Colt package](http://dsd.lbl.gov/~hoschek/colt/) and [JDOM](http://www.jdom.org/) (see [section 3](#using-fern-in-your-project)) . 

Optional are [Cytoscape 2.4.0](http://www.cytoscape.org/) or higher to use the plugin (see [section 8](#using-the-cytoscapecelldesigner-plugin)) and gnuplot (see [section 9](#gnuplot)).


# Using FERN in your project

Just include the four jar files (already included in the FERN package)

```
colt.jar		(http://dsd.lbl.gov/~hoschek/colt-download/releases/)
concurrent.jar		(included in the colt package)
jdom.jar		(http://www.jdom.org/dist/binary/)
```

into your classpath. 


# SBML support

FERN provides complete support for all the updated SBML versions from Level 1 Version 1 to Level 3 Version 2. 

# Using the start scripts

Depending on your operating environment you can start the command line class "Start" by starting one of
```
start.sh	for unix/linux systems
start.bat	for windows systems
start_cygwin.sh	for cygwin/windows systems
```

They differ only in the way the classpath is given (unix' java vm needs it ":" separated, windows ";" separated; this is a problem in cygwin because a ";" would be interpreted by the shell).

The usage is explained when you call it without parameters. If you want to use the -i (for plotting the result in a window) or -p (for creating a png figure) flags, read [section 9](#gnuplot) on using gnuplot!

The start scripts can be used both with FernML and SBML networks. Example networks for both input
formats are given in the examples directory.


# Discovering the examples
The best way of starting with FERN is to import the src directory into your Java IDE,
include the colt.jar, concurrent.jar and jdom.jar (see below) files and then run one of the examples.

The package fern.example includes several small applications which illustrate how to use FERN
functions such as e.g. importing and simulating a model.

Alternatively, you can use the command-line scripts (see [section 5](#using-the-start-scripts)) to perform simulations for the 
examples provided with FERN.

To test if your system is configured correctly just type in the fern directory (for unix/linux systems, for windows or cygwin/windows use the corresponding start.bat and start_cygwin.sh scripts):<br />

for FernML:<br />

	start.sh examples/mm.xml 10 0.5 -n 50

for SBML:<br />

	start.sh examples/mm_sbml.xml 10 0.5 -n 50


# Compile Errors regarding SBML/Cytoscape
Without the Cytoscape package within the classpath, you will get compile errors in fern.cytoscape. 
Other classes should not be affected. If you want to get rid of the errors or the framework will not compile at all, just delete these packages (you may have to delete some examples as well) or include  Cytoscape into your project (see [section 8](#using-the-cytoscapecelldesigner-plugin)).



# Using the Cytoscape/CellDesigner plugin

Cytoscape: Simply copy the fern.jar into the cytoscape/plugin folder and start Cytoscape (available at http://www.cytoscape.org/). You will find FERN in the plugins menu. The Cytoscape plugin requires Cytoscape version 2.4.0 or higher.

CellDesigner: Make sure to download the newest version of CellDesigner from http://www.systems-biology.org/cd/ (at least version 4.0beta). The current version of CellDesigner (4.0beta) is not as flexible as Cytoscape regarding plugins, so there is some more work to do. Once again you have to copy fern.jar into the CellDesigner/plugin folder and additionally colt.jar, concurrent.jar and jdom.jar into the CellDesigner/lib folder. You have to replace the original start script by the one shipped with fern, since the Classpath of the original one is hard coded and would not include the three new required jars. You can start the plugin by using the plugin menu of CellDesigner.


# Gnuplot

If you want to use the plot method of the GnuPlot class, make sure the program gnuplot is in the path environment variable (just try to invoke it from command line).


# Windows users
Since windows users are usually not familiar with a command line console, here some first steps:

To start the console use Start - Run (or just the search field under vista) to type 

	cmd

and press Enter. Now you can navigate to you FERN installation by

	cd "C:\Program Files\fern"

if you unpacked FERN into that folder. Once you are within this folder, you can start FERN by using the start.bat as described above.
