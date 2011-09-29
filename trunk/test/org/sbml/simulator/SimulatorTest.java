/*
 * $Id:  SimulatorTest.java 15:40:12 draeger$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.sbml.simulator;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.simulator.math.SBMLinterpreter;
import org.sbml.simulator.math.odes.AbstractDESSolver;
import org.sbml.simulator.math.odes.EulerMethod;
import org.sbml.simulator.math.odes.IntegrationException;
import org.sbml.simulator.math.odes.MultiBlockTable;

/**
 * A simple program that performs a simulation of a model.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class SimulatorTest {
  
  /**
   * Starts a simulation at the command line.
   * 
   * @param args
   *        file name, step size, and end time.
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   * @throws IntegrationException
   */
  public static void main(String[] args) throws XMLStreamException,
    IOException, ModelOverdeterminedException, SBMLException,
    IntegrationException {

    // Configuration
    String fileName = args[0];
    double stepSize = Double.parseDouble(args[1]);
    double timeEnd = Double.parseDouble(args[2]);
    
    // Read the model and initialize solver
    Model model = (new SBMLReader()).readSBML(fileName).getModel();
    AbstractDESSolver solver = new EulerMethod();
    solver.setStepSize(stepSize);
    SBMLinterpreter interpreter = new SBMLinterpreter(model);
    solver.setStepSize(stepSize);
    if (solver instanceof AbstractDESSolver) {
      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
    }
    
    // Compute the numerical solution of the initial value problem
    MultiBlockTable solution = solver.solve(interpreter, interpreter
        .getInitialValues(), 0d, timeEnd);
    
    // Display simulation result to the user
    JScrollPane resultDisplay = new JScrollPane(new JTable(solution));
    resultDisplay.setPreferredSize(new Dimension(400, 400));
    JOptionPane.showMessageDialog(null, resultDisplay, "The solution of model "
        + model.getId(), JOptionPane.INFORMATION_MESSAGE);
  }
  
}
