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
package org.simulator.sbml;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.RosenbrockSolver;

/**
 * This class tests the {@link ConstraintListener} interface implementation
 * {@link SimpleConstraintListener} by evaluating a simple test model that
 * contains a {@link Constraint}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.3
 */
public class ConstraintTest {

  /**
   * @param args not used.
   * 
   * @throws XMLStreamException
   * @throws IOException
   * @throws ParseException
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   * @throws DerivativeException
   */
  public static void main(String args[]) throws XMLStreamException, IOException, ParseException, SBMLException, ModelOverdeterminedException, DerivativeException {
    SBMLDocument doc = SBMLReader.read(ConstraintTest.class.getResourceAsStream("data/Constraint-00001-L3V1.xml"));

    double stepSize = .1d;
    double timeEnd = 5d;

    DESSolver solver = new RosenbrockSolver();
    solver.setStepSize(stepSize);
    SBMLinterpreter interpreter = new SBMLinterpreter(doc.getModel());
    if (solver instanceof AbstractDESSolver) {
      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
    }

    // Compute the numerical solution of the initial value problem
    //MultiTable solution =
    solver.solve(interpreter, interpreter.getInitialValues(), 0d, timeEnd);
  }

}
