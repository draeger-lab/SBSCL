/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;

/**
 * This class can test the simulation of all models from
 * <a href="http://www.ebi.ac.uk/biomodels-main/" target="_blank">BioModels database</a>.
 *
 * @author Roland Keller
 * @version $Rev$
 */
public class BiomodelsExample {

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(BiomodelsExample.class.getName());


  /**
   * Tests the models of biomodels.org using the {@link RosenbrockSolver} as integrator
   *
   * @param file
   * @param from
   * @param to
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void testBiomodels(String file, int from, int to)
      throws FileNotFoundException, IOException {
    int errors = 0;
    int nModels = 0;
    AdaptiveStepsizeIntegrator solver = new RosenbrockSolver();
    solver.setAbsTol(1E-12);
    solver.setRelTol(1E-6);
    List<Integer> slowModels = new LinkedList<Integer>();
    for (int modelnr = from; modelnr <= to; modelnr++) {
      System.out.println("Biomodel " + modelnr);
      Model model = null;
      try {
        String modelFile = "";
        if (modelnr < 10) {
          modelFile = file + "BIOMD000000000" + modelnr + ".xml";
        } else if (modelnr < 100) {
          modelFile = file + "BIOMD00000000" + modelnr + ".xml";
        } else {
          modelFile = file + "BIOMD0000000" + modelnr + ".xml";
        }
        model = (new SBMLReader()).readSBML(modelFile).getModel();
      } catch (Exception e) {
        model = null;
        logger.warning("Exception while reading Biomodel " + modelnr);
        errors++;
      }
      if (model != null) {
        solver.reset();
        try {
          double time1 = System.nanoTime();
          SBMLinterpreter interpreter = new SBMLinterpreter(model);
          if ((solver != null) && (interpreter != null)) {
            solver.setStepSize(0.01);
            solver.solve(interpreter, interpreter.getInitialValues(), 0, 10, null);
            if (solver.isUnstable()) {
              logger.warning("unstable!");
              errors++;
            }
          }
          double time2 = System.nanoTime();
          double runningTime = (time2 - time1) / 1E9;
          if (runningTime >= 300) {
            slowModels.add(modelnr);
          }
        } catch (Exception e) {
          logger.warning("Exception in Biomodel " + modelnr);
          errors++;
        }
      }
      nModels++;
    }
    System.out.println("Models: " + nModels);
    System.out.println("Models with errors in simulation: " + errors);
    System.out.println("Models with correct simulation: " + (nModels - errors));
    for (int slowModel : slowModels) {
      System.out.println("Slow: #" + slowModel);
    }
  }


  /**
   * * Input:
   * <ol>
   * <li>directory with models (containing the biomodels),
   * <li>first model to be simulated,
   * <li>last model to be simulated,
   * </ol>
   *
   * @param args
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void main(String[] args)
      throws IOException, URISyntaxException {
    testBiomodels(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
  }
}
