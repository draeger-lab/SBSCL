/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
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
package org.simulator.examples;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.SBMLReader;
import org.simulator.math.odes.*;
import org.simulator.plot.PlotMultiTable;
import org.simulator.sbml.EquationSystem;
import org.simulator.sbml.SBMLinterpreter;

/**
 * A simple program that performs a simulation of a model.
 *
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class SimulatorExample implements PropertyChangeListener {

  private static Logger logger = Logger.getLogger(SimulatorExample.class.getName());
  private static final double TOLERANCE_FACTOR = 1E-3;
  private static final int WIDTH = 400;
  private static final int HEIGHT = 400;
  private static final String RESULT = "result";
  private static SimulatorExample simulatorExample;

  /**
   * Starts a simulation at the command line.
   *
   * @param args file name, step size, and end time.
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   * @throws DerivativeException
   */
  public static void main(String[] args) throws XMLStreamException,
      IOException, ModelOverdeterminedException, SBMLException,
      DerivativeException {

    String fileName = null;
    double stepSize = 0d;
    double timeEnd = 0d;
    double absTol = 0d;
    double relTol = 0d;

    // Configuration
    try {
      fileName = args[0];
      stepSize = Double.parseDouble(args[1]);
      timeEnd = Double.parseDouble(args[2]);
      absTol = TOLERANCE_FACTOR * Double.parseDouble(args[3]);
      relTol = TOLERANCE_FACTOR * Double.parseDouble(args[4]);
    } catch (NumberFormatException e) {
      logger.warn("Please enter numerical values wherever needed");
    } catch (IllegalArgumentException e) {
      logger.error("Provide proper arguments");
    }

    // Read the model and initialize solver
    SBMLDocument document = (new SBMLReader()).readSBML(fileName);
    Model model = document.getModel();

    DESSolver solver = new RosenbrockSolver();
    solver.setStepSize(stepSize);
    EquationSystem interpreter = new SBMLinterpreter(model);
    if (solver instanceof AbstractDESSolver) {
      solver.setIncludeIntermediates(false);
    }

    // Compute the numerical solution of the initial value problem
    if (solver instanceof AdaptiveStepsizeIntegrator) {
      ((AdaptiveStepsizeIntegrator) solver).setAbsTol(absTol);
      ((AdaptiveStepsizeIntegrator) solver).setRelTol(relTol);
    }

    simulatorExample = new SimulatorExample();
    MultiTable solution = solver.solve(interpreter, interpreter
        .getInitialValues(), 0d, timeEnd, simulatorExample);

    // Display simulation result to the user
    JScrollPane resultDisplay = new JScrollPane(new JTable(solution));
    resultDisplay.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    JOptionPane.showMessageDialog(null, resultDisplay, "The solution of model "
        + model.getId(), JOptionPane.INFORMATION_MESSAGE);

    // plot all the reactions species
    PlotMultiTable p = new PlotMultiTable(solution, "Output plot");
    p.pack();
    RefineryUtilities.centerFrameOnScreen(p);
    p.setVisible(true);
  }

  @Override
  public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    if (propertyChangeEvent.getPropertyName().equals(RESULT)) {
      logger.info(Arrays.toString((double[]) propertyChangeEvent.getNewValue()));
    } else {
      logger.info(propertyChangeEvent.getNewValue());
    }
  }

}
