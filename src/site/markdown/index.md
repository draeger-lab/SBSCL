# Systems Biology Simulation Core Library

Welcome to the Systems Biology Simulation Core Library!

The Systems Biology Simulation Core Library provides an efficient and exhaustive Java™ implementation of methods to interpret the content of models encoded in the Systems Biology Markup Language ([SBML](http://sbml.org)) and its numerical solution. This library is based on the [JSBML](http://sbml.org/Software/JSBML) project and can be used on every operating system, for which a Java Virtual Machine is available.

Please note that this project does not contain any user interface, neither a command-line interface, nor a graphical user interface. This project has been developed as a pure programming library. To support the Minimum Information About a Simulation Experiment ([MIASE](http://biomodels.net/miase/)) effort, it understands Simulation Experiment Description Markup Language ([SED-ML](http://sed-ml.org)) files.

Its abstract type and interface hierarchy facilitates the implementation of further community standards, such as [CellML](http://www.cellml.org).

When using the Simulation Core Library, please cite:
Roland Keller, Alexander Dörr, Akito Tabira, Akira Funahashi, Michael J. Ziller, Richard Adams, Nicolas Rodriguez, Nicolas Le Novère, Noriko Hiroi, Hannes Planatscher, Andreas Zell, and Andreas Dräger. The systems biology simulation core algorithm. *BMC Systems Biology*, 7:55, July 2013. [ [DOI](https://doi.org/10.1186/1752-0509-7-55) | [link](https://bmcsystbiol.biomedcentral.com/articles/10.1186/1752-0509-7-55) | [pdf](https://bmcsystbiol.biomedcentral.com/track/pdf/10.1186/1752-0509-7-55) ]

## Capabilities

### Numerical simulation of SBML models

* Several solvers from the [Apache Commons Math Library](http://commons.apache.org/math/) included
* Rosenbrock solver for integration of stiff differential equation systems
* Clear separation of [SBML](http://sbml.org) interpretation and integration routines
* Fast [SBML](http://sbml.org) interpretation by using a transformed syntax graph
* Full support of [SBML](http://sbml.org) events, algebraic rules and fast reactions
* Support of all all models from the [SBML Test Suite](http://sbml.org/Software/SBML_Test_Suite) (v. 2.2) for all levels and versions: [Simulation results](http://sbml.org/Facilities/Database/Submission/Details/45)


### Support for SED-ML

* Several quality functions for computation of the distance from simulated data to some given data

## Documentation of the application programing interfaces

To get startet with this library, please see the API documentation (JavaDoc) for the most recent Version 1.5. See the introduction, use-cases, and coding examples here: ([Description](project-reports.html)). 

In case you like to use older versions, you can find the documentation here:
* ([Version 1.4](old_javadoc/version_1.4/index.html))
* ([Version 1.3](old_javadoc/version_1.3/index.html))
* ([Version 1.2](old_javadoc/version_1.2/index.html))
* ([Version 1.1](old_javadoc/version_1.1/index.html))
* ([Version 1.0](old_javadoc/version_1.0/index.html))

## Download

You can obtain all versions of this library by going to the [download area](https://github.com/draeger-lab/SBSCL/releases/) of the project.
There you can also find the release notes for user-visible changes.

## Acknowledgments

Many thanks to B. Kotcon, S. Mesuro, D. Rozenfeld, A. Yodpinyanee, A. Perez, E. Doi, R. Mehlinger, S. Ehrlich, M. Hunt, G. Tucker, P. Scherpelz, A. Becker, E. Harley, and C. Moore, Harvey Mudd College, USA, for providing a Java implementation of Rosenbrock's method as part of the [ODEToolkit](http://odetoolkit.hmc.edu).

We like to thank [Michael T. Cooling](http://www.abi.auckland.ac.nz/uoa/mike-cooling/), University of Auckland, New Zealand, for fruitful discussion.

This work was funded by the Federal Ministry of Education and Research ([BMBF](http://www.bmbf.de/en/), Germany) as part of the [Virtual Liver Network](http://www.virtual-liver.de).

