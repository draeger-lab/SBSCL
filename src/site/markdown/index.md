# Systems Biology Simulation Core Library

Welcome to the Systems Biology Simulation Core Library (SBSCL)!

SBSCL provides an efficient and exhaustive Java™ implementation of methods to interpret the content of models encoded in the Systems Biology Markup Language ([SBML](http://sbml.org)) and to compute their numerical solution. The library is based on [JSBML](http://sbml.org/Software/JSBML) and can be used on every operating system for which a Java Virtual Machine is available.

Please note that this project does not contain any user interface, neither a command-line interface nor a graphical user interface. It is intended as a pure programming library that can be embedded into other tools and workflows.

To support the Minimum Information About a Simulation Experiment ([MIASE](http://biomodels.net/miase/)) effort, SBSCL understands Simulation Experiment Description Markup Language ([SED-ML](http://sed-ml.org)) files and can execute simulation experiments stored in COMBINE archive ([OMEX](https://combinearchive.org/)) files.

Its abstract type and interface hierarchy is designed to facilitate the future implementation of further community standards (for example, [CellML](http://www.cellml.org)). At the moment, however, CellML models are **not yet supported**.

When using the Simulation Core Library, please cite:

1. Hemil Panchiwala, Shalin Shah, Hannes Planatscher, Mykola Zakharchuk, Matthias König, Andreas Dräger. The Systems Biology Simulation Core Library. _Bioinformatics_, btab669, 2021.  
   [[DOI](https://doi.org/10.1093/bioinformatics/btab669)]
2. Roland Keller, Alexander Dörr, Akito Tabira, Akira Funahashi, Michael J. Ziller, Richard Adams, Nicolas Rodriguez, Nicolas Le Novère, Noriko Hiroi, Hannes Planatscher, Andreas Zell, and Andreas Dräger. The systems biology simulation core algorithm. _BMC Systems Biology_, 7:55, July 2013.  
   [[DOI](https://doi.org/10.1186/1752-0509-7-55)] [[link](https://bmcsystbiol.biomedcentral.com/articles/10.1186/1752-0509-7-55)] [[pdf](https://bmcsystbiol.biomedcentral.com/track/pdf/10.1186/1752-0509-7-55)]

## Capabilities

### Deterministic simulation of SBML models

* Several solvers from the [Apache Commons Math Library](http://commons.apache.org/math/) included
* Rosenbrock solver for integration of stiff differential equation systems
* Clear separation of [SBML](http://sbml.org) interpretation and integration routines
* Fast [SBML](http://sbml.org) interpretation by using a transformed syntax graph
* Full support of [SBML](http://sbml.org) events, algebraic rules and fast reactions
* Support of all models from the [SBML Test Suite](http://sbml.org/Software/SBML_Test_Suite) (v. 3.3.0) for all levels and versions: [Simulation results](http://sbml.org/Facilities/Database/Submission/Details/257)

### Stochastic simulation of SBML models

* Supports different solvers from the FERN library
* Gillespie algorithm used currently to solve the SBML models
* Support of models from the [Stochastic Test Suite](https://github.com/sbmlteam/sbml-test-suite/tree/master/cases/stochastic) for all levels and versions

### Constraint-based modeling (flux balance analysis)

* Support for SBML models that use the flux-balance-constraints ([FBC](http://sbml.org/Documents/Specifications/SBML_Level_3/Packages/fbc)) extension (versions 1 and 2)
* Suitable for constraint-based modeling and flux balance analysis (FBA) of genome-scale metabolic models
* Tested with models from the [BiGG models FBA benchmark set](https://github.com/matthiaskoenig/bigg-models-fba)
* Example code is available in the [FBA example](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/FBAExample.java)

### Support for SED-ML and COMBINE archives (OMEX)

* Reads and executes simulation experiments described in [SED-ML](http://sed-ml.org) Level 1 Version 3
* Can work directly with COMBINE archive ([OMEX](https://combinearchive.org/)) files containing SBML, SED-ML and associated resources
* Several quality functions for computation of the distance from simulated data to given reference data
* Example code is available in the [OMEX example](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/OMEXExample.java) and the [SED-ML example](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/SEDMLExample.java)

## Examples and demo repository

A dedicated [Demo Repository](https://github.com/draeger-lab/SBSCL-demo) provides a few use-case examples and an exemplary [POM](https://github.com/draeger-lab/SBSCL-demo/blob/main/pom.xml) file that helps setting up a project using SBSCL.

Further examples can be found directly within this repository in the [examples package](https://github.com/draeger-lab/SBSCL/tree/master/src/main/java/org/simulator/examples), including:

* How to [run models directly from BioModels](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/BiomodelsExample.java)
* How to [run hierarchically structured models](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/CompExample.java) that use the SBML extension package "comp"
* How to [listen to constraint violations](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/ConstraintExample.java)
* How to run a [flux balance analysis](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/FBAExample.java)
* How to work with an [OMEX archive](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/OMEXExample.java) file
* How to execute a simulation as instructed in a [SED-ML file](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/SEDMLExample.java)
* How to run a [dynamic simulation](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/org/simulator/examples/SimulatorExample.java)
* How to run a [stochastic simulation](https://github.com/draeger-lab/SBSCL/blob/master/src/main/java/fern/Start.java)

## Documentation of the application programming interfaces

To get started with this library, please see the API documentation (JavaDoc) on the project website:  
<https://draeger-lab.github.io/SBSCL/apidocs/overview-summary.html>

See also the introduction, use-cases and coding examples summarized on the [project reports page](project-reports.html).

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

We thank [Deepak Yadav](https://www.linkedin.com/in/deepak-yadav-97aa22297/) for contributing updates to the SBSCL project website and documentation.

This work was funded by the Federal Ministry of Education and Research ([BMBF](http://www.bmbf.de/en/), Germany) as part of the [Virtual Liver Network](http://www.virtual-liver.de).