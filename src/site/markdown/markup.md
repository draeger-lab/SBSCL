# Systems Biology Simulation Core Library

Welcome to the Systems Biology Simulation Core Library!

The Systems Biology Simulation Core Library provides an efficient and exhaustive Java™ implementation of methods to interpret the content of models encoded in the Systems Biology Markup Language (SBML) and its numerical solution. This library is based on the JSBML project and can be used on every operating system, for which a Java Virtual Machine is available.

Please note that this project does not contain any user interface, neither a command-line interface, nor a graphical user interface. This project has been developed as a pure programming library. To support the Minimum Information About a Simulation Experiment (MIASE) effort, it understands Simulation Experiment Description Markup Language (SED-ML) files.

Its abstract type and interface hierarchy facilitates the implementation of further community standards, such as CellML.

When using the Simulation Core Library, please cite:
Roland Keller, Alexander Dörr, Akito Tabira, Akira Funahashi, Michael J. Ziller, Richard Adams, Nicolas Rodriguez, Nicolas Le Novère, Noriko Hiroi, Hannes Planatscher, Andreas Zell, and Andreas Dräger. The systems biology simulation core algorithm. BMC Systems Biology, 7:55, July 2013. [ DOI | link | pdf ]

## Capabilities

### Numerical simulation of SBML models

* Several solvers from the Apache Commons Math Library included
* Rosenbrock solver for integration of stiff differential equation systems
* Clear separation of SBML interpretation and integration routines
* Fast SBML interpretation by using a transformed syntax graph
* Full support of SBML events, algebraic rules and fast reactions
* Support of all all models from the SBML Test Suite (v. 2.2) for all levels and versions: Simulation results


### Support for SED-ML

* Several quality functions for computation of the distance from simulated data to some given data

## Documentation of the application programing interfaces

To get startet with this library, please see the API documentation (JavaDoc) for the most recent Version 1.4. See the introduction, use-cases, and coding examples here: Description. 

In case you like to use older versions, you can find the documentation here:
* Version 1.4
* Version 1.3
* Version 1.2
* Version 1.1
* Version 1.0

Here you can find Release Notes for user-visible changes.

## Download

You can obtain all versions of this library by going to the sourceforge download area of the project.

## Acknowledgments

Many thanks to B. Kotcon, S. Mesuro, D. Rozenfeld, A. Yodpinyanee, A. Perez, E. Doi, R. Mehlinger, S. Ehrlich, M. Hunt, G. Tucker, P. Scherpelz, A. Becker, E. Harley, and C. Moore, Harvey Mudd College, USA, for providing a Java implementation of Rosenbrock's method as part of the ODEToolkit.

We like to thank Michael T. Cooling, University of Auckland, New Zealand, for fruitful discussion.

Special thanks to D. M. Wouamba, P. Stevens, M. Zwießele, M. Kronfeld, and A. Schröder for source code contribution and fruitful discussion.

This work was funded by the Federal Ministry of Education and Research (BMBF, Germany) as part of the Virtual Liver Network.

