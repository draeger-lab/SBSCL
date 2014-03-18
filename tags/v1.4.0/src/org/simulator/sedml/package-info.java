/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2014 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

/**
 * <p>Classes for reading and executing
 * <a href="http://sed-ml.org/" target="_blank">SED-ML</a> files.
 * The <a href="http://www.jlibsedml.org">jlibsedml</a> library
 * is used to perform these operations.
 * <p>
 * For more information about SED-ML please visit
 * <a href="http://sed-ml.org" target="_blank">http://sed-ml.org</a>.
 * <p>
 * Classes for storing and interpreting an <a href="http://sbml.org">SBML</a>
 * model. The most important class is {@link org.simulator.sbml.SBMLinterpreter}
 * that can return the current vector of derivatives to the solver.
 * 
 * @author Richard Adams
 * @version $Rev$
 */
package org.simulator.sedml;
