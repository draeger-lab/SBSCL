/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2015 jointly by the following organizations:
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
package org.simulator.fba;

import ilog.concert.IloException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

/**
 * A simple test class to demonstrate the capabilities of the FBA implementation
 * in Simulation Core Library. This is based on an installation of
 * <a href="http://www.ibm.com/software/commerce/optimization/cplex-optimizer/">CPLEX</a>.
 * In order to run this example, it is necessary to launch the JVM with the
 * argument {@code -Djava.library.path=/path/to/the/binaries/of/CPLEX/}.
 * Note that this project does not redistribute CPLEX.
 * 
 * @author Andreas Dr&auml;ger
 * @version 1.5
 */
public class COBRAsolverExample {

  /**
   * Simple test function that reads and solves an SBML file in a flux balance
   * constraints framework.
   * 
   * @param args
   *        the path to a valid SBML file with fbc version 2.
   * @throws XMLStreamException
   *         if the file cannot be parsed into an {@link SBMLDocument}.
   * @throws IOException
   *         if the given path is invalid or cannot be read
   * @throws IloException
   *         if the construction of the linear program fails.
   * @throws ModelOverdeterminedException
   *         if the model is over determined through {@link AlgebraicRule}s.
   * @throws SBMLException
   *         if the model is invalid or inappropriate for flux balance analysis.
   */
  public static void main(String[] args) throws XMLStreamException, IOException, IloException, SBMLException, ModelOverdeterminedException {
    COBRAsolver solver = new COBRAsolver(SBMLReader.read(new File(args[0])));
    if (solver.solve()) {
      System.out.println("Objective value:\t" + solver.getObjetiveValue());
      System.out.println("Fluxes:\t" + Arrays.toString(solver.getValues()));
    }
  }

}
