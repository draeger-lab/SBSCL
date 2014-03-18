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
package org.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;

/**
 * Automatically evaluates models from the SBML Test Suite within a given range
 * of model numbers, thereby using the {@link RosenbrockSolver} as integration
 * method.
 * 
 * @author Roland Keller
 * @version $Rev$
 */
public class SBMLTestSuiteWrapper {

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(SBMLTestSuiteWrapper.class.getName());

  /**
   * Computes a statistic for the SBML test suite using the {@link RosenbrockSolver} as integrator
   * @param path
   * @param modelnr
   * @param outputPath
   * @param level
   * @param version
   * @throws FileNotFoundException
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void testRosenbrockSolver(String path, int modelnr, String outputPath, int level, int version)
      throws FileNotFoundException, IOException, URISyntaxException {
    String sbmlfile, csvfile, configfile;
    if ((modelnr >= 1124) && (modelnr <= 1183)) {
      return;
    }

    RosenbrockSolver solver = new RosenbrockSolver();

    List<Integer> modelsWithStrongerTolerance = new LinkedList<Integer>();
    modelsWithStrongerTolerance.add(863);
    modelsWithStrongerTolerance.add(882);
    modelsWithStrongerTolerance.add(893);
    modelsWithStrongerTolerance.add(994);
    modelsWithStrongerTolerance.add(1109);
    modelsWithStrongerTolerance.add(1121);

    List<Integer> modelsWithStrongestTolerance = new LinkedList<Integer>();
    modelsWithStrongestTolerance.add(872);
    modelsWithStrongestTolerance.add(987);
    modelsWithStrongestTolerance.add(1052);

    if (modelsWithStrongestTolerance.contains(modelnr)) {
      solver.setAbsTol(1E-14);
      solver.setRelTol(1E-12);
    }
    else if (modelsWithStrongerTolerance.contains(modelnr)) {
      solver.setAbsTol(1E-12);
      solver.setRelTol(1E-8);
    }
    else {
      solver.setAbsTol(1E-12);
      solver.setRelTol(1E-6);
    }

    StringBuilder fileBuilder = new StringBuilder();
    fileBuilder.append(modelnr);
    while (fileBuilder.length() < 5) {
      fileBuilder.insert(0, '0');
    }
    String folder = fileBuilder.toString();
    fileBuilder.append('/');
    fileBuilder.append(folder);
    fileBuilder.insert(0, path + "/");
    String modelFile = fileBuilder.toString();
    csvfile = modelFile + "-results.csv";
    configfile = modelFile + "-settings.txt";

    Properties props = new Properties();
    props.load(new BufferedReader(new FileReader(configfile)));
    double duration = Double.valueOf(props.getProperty("duration"));

    int steps = Integer.valueOf(props.getProperty("steps"));
    Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
    String[] amounts = String.valueOf(props.getProperty("amount"))
        .trim().split(",");
    String[] concentrations = String.valueOf(
      props.getProperty("concentration")).trim().split(",");
    String[] variables = String.valueOf(props.getProperty("variables"))
        .trim().split(",");


    for (String s : amounts) {
      s = s.trim();
      if (!s.equals("")) {
        amountHash.put(s, true);
      }
    }

    for (String s : concentrations) {
      s = s.trim();
      if (!s.equals("")) {
        amountHash.put(s, false);
      }
    }

    for (int i = 0; i!=variables.length; i++) {
      variables[i] = variables[i].trim();
    }

    sbmlfile = modelFile + "-sbml-l" + level + "v" + version + ".xml";
    Model model = null;
    try {
      model = (new SBMLReader()).readSBML(sbmlfile).getModel();
    } catch (Exception e) {

    }
    if (model != null) {
      CSVImporter csvimporter = new CSVImporter();
      MultiTable inputData = csvimporter.convert(model, csvfile);

      double[] timepoints = inputData.getTimePoints();

      MultiTable solution = null;
      solver.reset();
      try {
        solution = SBMLTestSuiteRunner.testModel(solver, model,
          timepoints, duration/ steps, amountHash);
      } catch (DerivativeException e) {
        logger.warning("Exception in model " + modelnr);
        solution = null;
      } catch (ModelOverdeterminedException e) {
        logger.warning("OverdeterminationException in model "
            + modelnr);
        solution = null;
      }
      if (solution != null) {
        writeMultiTableToFile(outputPath+"/" + folder + ".csv", variables, solution);
      }
    }
    else {
      logger.warning("The model "+ modelnr + " does not exist");
    }
  }

  /**
   * 
   * @param outputFile
   * @param variables
   * @param solution
   * @throws IOException
   */
  private static void writeMultiTableToFile(String outputFile, String[] variables, MultiTable solution) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    writer.append("time");
    writer.append(",");
    for (int i = 0; i!=variables.length; i++) {
      writer.append(variables[i]);
      if (i < variables.length - 1) {
        writer.append(",");
      }
    }

    for (int row = 0; row != solution.getTimePoints().length; row++) {
      writer.newLine();
      writer.append(String.valueOf(solution.getTimePoint(row)));
      writer.append(',');
      for (int i = 0; i!=variables.length; i++) {
        writer.append(String.valueOf(solution.getColumn(variables[i]).getValue(row)));
        if (i < variables.length - 1) {
          writer.append(',');
        }
      }
    }
    writer.newLine();
    writer.close();
  }

  /**
   * 
   * @param args
   *            Expected arguments are (in this order): the path to the SBML
   *            Test Suite, number of the model where to start the test, out
   *            path (i.e., the path where to store results), SBML Level, SBML
   *            version (for the given level), number of the last test case to
   *            be evaluated
   * @throws NumberFormatException
   * @throws FileNotFoundException
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void main(String[] args) throws NumberFormatException, FileNotFoundException, IOException, URISyntaxException {
    int begin = Integer.parseInt(args[1]);
    int end = begin;
    if (args.length > 5) {
      end = Integer.parseInt(args[5]);
    }
    int level = Integer.parseInt(args[3]);
    int version = Integer.parseInt(args[4]);
    for (int modelnr = begin; modelnr <= end; modelnr++) {
      testRosenbrockSolver(args[0], modelnr, args[2], level, version);
    }
  }

}
