package org.testsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.log4j.Logger;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.comp.CompSimulator;
import org.simulator.examples.CompExample;
import org.simulator.fba.FluxBalanceAnalysis;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.LSODAIntegrator_F;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Wrapper for the SBML Test Suite
 * <p>
 * The <a href="https://github.com/sbmlteam/sbml-test-suite" target="_blank">SBML Test Suite</a> is
 * a conformance testing system. It allows developers and users to test the degree and correctness
 * of the SBML support provided in a software package. The SBML Test Suite consists of (1) a
 * collection of SBML models, each with associated simulated results where appropriate; (2) a
 * testing framework for running software tools through the suite; and (3) basic documentation on
 * the test cases and the use of the suite. This class provides the required wrapper class used to
 * verify tests with the SBML Test Runner.
 * </p>
 * <p>
 * The simulation results of SBSCL by the wrapper are stored as a CSV file in the specified output
 * directory in SBML Test Runner. The SBML Test Runner then performs the comparison of the
 * simulation results against the reference output and creates the distance plots comparing this
 * results with the pre-defined results from SBML Test Suite. The comparison is performed using the
 * criteria specified in <a href="http://sbml.org/Software/SBML_Test_Suite/Case_Descriptions#The_.22settings.22_file"
 * target="_blank">link</a>
 * </p>
 */
public class SBMLTestSuiteRunnerWrapper {

  public static final String STEPS = "steps";
  public static final String AMOUNT = "amount";
  public static final String CONCENTRATION = "concentration";
  public static final String ABSOLUTE = "absolute";
  public static final String RELATIVE = "relative";
  public static final String NAN = "NaN";
  private static final double TOLERANCE_FACTOR = 1E-5;
  private static final Logger LOGGER = Logger.getLogger(CompExample.class.getName());

  /**
   * The wrapper executes the simulation of a given SBML file and writes result to a specified CSV
   * file. The information for the wrapper is parsed via command line arguments.
   *
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   * @throws ModelOverdeterminedException
   * @throws DerivativeException
   */
  public static void main(String[] args)
      throws IOException, XMLStreamException, ModelOverdeterminedException, DerivativeException {

    // Configuration
    String dirPath = args[0];  // path of sbml-test-suite
    String currentCase = args[1];  // current test case
    String outputDirPath = args[2];  // output directory for results
    String level = args[3];  // SBML level
    String version = args[4]; // SBML version

    String filePath =
        dirPath + File.separator + currentCase + File.separator + currentCase + "-sbml-l" + level
        + 'v' + version + ".xml";
    String settingsPath =
        dirPath + File.separator + currentCase + File.separator + currentCase + "-settings.txt";
    String outputFilePath = outputDirPath + File.separator + currentCase + ".csv";
    String resultsPath =
        dirPath + File.separator + currentCase + File.separator + currentCase + "-results.csv";

    Properties properties = new Properties();
    properties.load(new BufferedReader(new FileReader(settingsPath)));
    double duration;
    double steps = (!properties.getProperty(STEPS).isEmpty()) ? Double
      .parseDouble(properties.getProperty(STEPS)) : 0d;
      String[] amounts = String.valueOf(properties.getProperty(AMOUNT)).split(",");
      String[] concentrations = String.valueOf(
        properties.getProperty(CONCENTRATION)).split(",");

      Map<String, Boolean> amountHash = createAmountHash(amounts, concentrations);

      // Read the model and initialize solver
      File sbmlfile = new File(filePath);
      SBMLDocument document = null;
      try {
        document = (new SBMLReader()).readSBML(sbmlfile);
      } catch (Exception e) {
        e.printStackTrace();
      }
      Model model = document.getModel();

      // get pre-defined test suite results
      MultiTable inputData = getPredefinedTestSuiteResults(model, resultsPath);

      // get timepoints
      double[] timePoints = inputData.getTimePoints();
      duration = timePoints[timePoints.length - 1] - timePoints[0];

      MultiTable solution;

      // writes results to the output file in CSV format
      File outputFile = new File(outputFilePath);
      try {
        outputFile.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
        LOGGER.error("Error in creating the output file");
      }

      LOGGER.info(Paths.get(outputFilePath));
      FileWriter csvWriter = new FileWriter(outputFilePath);

      StringBuilder output;

      if (model.getExtension(FBCConstants.shortLabel) != null) {

        FluxBalanceAnalysis solver = new FluxBalanceAnalysis(document);

        boolean isSolved = false;
        try {
          isSolved = solver.solve();
        } catch (Exception e) {
          e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new FileReader(resultsPath));
        String[] keys = reader.readLine().trim().split(",");
        output = getFBCResultAsCSV(solver, keys, isSolved);
        reader.close();

      } else {
        if (model.getExtension(CompConstants.shortLabel) == null) {

          if (useLSODA(properties)) {
            solution = runLSODASimulation(model, duration, steps, properties, timePoints, amountHash);
          } else {
            solution = runSBMLSimulation(model, duration, steps, properties, timePoints, amountHash);
          }
        } else {
          solution = runCompSimulation(sbmlfile, duration, steps);
        }

        MultiTable left = solution;
        if (solution.isSetTimePoints() && inputData.isSetTimePoints()) {
          left = solution.filter(inputData.getTimePoints());
        }

        // Map of variables present in the test suite results file
        Map<String, Integer> resultColumns = new HashMap<>();
        for (int i = 0; i < inputData.getColumnCount(); i++) {
          resultColumns.put(inputData.getColumnName(i), 1);
        }

        // Boolean array to check which variables are present in the test suite results file
        boolean[] variablesToAdd = new boolean[solution.getColumnCount()];
        if (resultColumns.containsKey(left.getColumnName(0))) {
          variablesToAdd[0] = true;
        }
        for (int i = 1; i < left.getColumnCount(); i++) {
          if (resultColumns.containsKey(left.getColumnName(i))) {
            variablesToAdd[i] = true;
          }
        }

        output = getSBMLOrCompResultAsCSV(left, variablesToAdd);
      }

      csvWriter.append(output);
      csvWriter.flush();
      csvWriter.close();

  }

  private static boolean useLSODA(Properties properties) {
    // this right now checks if "solver" property´s value is LSODA
    // TODO: implement correct checking
    return "LSODA".equalsIgnoreCase(properties.getProperty("solver"));
  }

  /**
   * Performs the simulation of the SBML models with comp extension using {@link CompSimulator}.
   *
   * @param sbmlFile the file with the SBML model
   * @param duration the duration of the simulation
   * @param steps    total steps in the simulation
   * @return the results of the simulation of comp model (null can be returned on exception)
   * @throws IOException
   * @throws XMLStreamException
   */
  private static MultiTable runCompSimulation(File sbmlFile, double duration, double steps) {

    CompSimulator compSimulator = null;
    try {
      compSimulator = new CompSimulator(sbmlFile);
    } catch (XMLStreamException e) {
      e.printStackTrace();
      LOGGER.error("XMLStreamException while reading the SBML model");
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.error("IOException occurred!");
    }

    double stepSize = (duration / steps);

    MultiTable solution = null;
    try {
      solution = compSimulator.solve(duration, stepSize);
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Error in solving the comp model!");
    }

    return solution;
  }

  /**
   * Performs the simulation of the SBML models using {@link RosenbrockSolver}.
   *
   * @param model      the SBML {@link Model}
   * @param duration   the duration of the simulation
   * @param steps      total steps in the simulation
   * @param properties different fields provided in the settings file of test case from SBML Test
   *                   Suite
   * @param timePoints array with time points of the simulation
   * @param amountHash Stores whether species has amount or concentration units
   * @return the results of the simulation
   * @throws DerivativeException
   * @throws ModelOverdeterminedException
   */
  private static MultiTable runSBMLSimulation(Model model, double duration, double steps,
    Properties properties, double[] timePoints,
    Map<String, Boolean> amountHash) throws DerivativeException {

    double absolute = (!properties.getProperty(ABSOLUTE).isEmpty()) ? Double
      .parseDouble(properties.getProperty(ABSOLUTE)) : 0d;
      double relative = (!properties.getProperty(RELATIVE).isEmpty()) ? Double
        .parseDouble(properties.getProperty(RELATIVE)) : 0d;

        DESSolver solver = new RosenbrockSolver();
        solver.setStepSize(duration / steps);

        if (solver instanceof AbstractDESSolver) {
          solver.setIncludeIntermediates(false);
        }

        if (solver instanceof AdaptiveStepsizeIntegrator) {
          ((AdaptiveStepsizeIntegrator) solver).setAbsTol(TOLERANCE_FACTOR * absolute);
          ((AdaptiveStepsizeIntegrator) solver).setRelTol(TOLERANCE_FACTOR * relative);
        }

        /**
         * Initialize the SBMLinterpreter
         *
         * Parameters passed:
         * SBML model, defaultSpeciesValue, defaultParameterValue,
         * defaultCompartmentValue, amountHash
         */
        SBMLinterpreter interpreter = null;
        try {
          interpreter = new SBMLinterpreter(model, 0, 0, 1, amountHash);
        } catch (ModelOverdeterminedException e) {
          e.printStackTrace();
          LOGGER.error(
              "Model Overdetermined while creating a mapping for converting Algebraic rule to Assignment rule");
        }

        // Compute the numerical solution of the problem
        return solver.solve(interpreter, interpreter.getInitialValues(), timePoints);
  }

  public static MultiTable runLSODASimulation(Model model, double duration, double steps,
  Properties properties, double[] timePoints,
  Map<String, Boolean> amountHash) throws DerivativeException {
    double absolute = (!properties.getProperty(ABSOLUTE).isEmpty()) ? Double
      .parseDouble(properties.getProperty(ABSOLUTE)) : 0d;
      double relative = (!properties.getProperty(RELATIVE).isEmpty()) ? Double
        .parseDouble(properties.getProperty(RELATIVE)) : 0d;

        DESSolver solver = new LSODAIntegrator();
        solver.setStepSize(duration / steps);

        if (solver instanceof AbstractDESSolver) {
          solver.setIncludeIntermediates(false);
        }

        if (solver instanceof AdaptiveStepsizeIntegrator) {
          ((AdaptiveStepsizeIntegrator) solver).setAbsTol(TOLERANCE_FACTOR * absolute);
          ((AdaptiveStepsizeIntegrator) solver).setRelTol(TOLERANCE_FACTOR * relative);
        }

        SBMLinterpreter interpreter = null;
        try {
          interpreter = new SBMLinterpreter(model, 0, 0, 1, amountHash);
        } catch (ModelOverdeterminedException e) {
          e.printStackTrace();
          LOGGER.error(
              "Model Overdetermined while creating a mapping for converting Algebraic rule to Assignment rule");
        }

          return solver.solve(interpreter, interpreter.getInitialValues(), timePoints);
  }

  /**
   * Creates an amount hash which keeps track whether the variable has amount units or concentration
   * units.
   *
   * @param amounts        the ids of the variables which are in amount units
   * @param concentrations the ids of the variables which are in concentration units
   * @return the amount hash
   */
  private static Map<String, Boolean> createAmountHash(String[] amounts, String[] concentrations) {

    Map<String, Boolean> amountHash = new HashMap<>();
    for (String s : amounts) {
      s = s.trim();
      if (!s.isEmpty()) {
        amountHash.put(s, true);
      }
    }

    for (String s : concentrations) {
      s = s.trim();
      if (!s.isEmpty()) {
        amountHash.put(s, false);
      }
    }

    return amountHash;
  }

  /**
   * Converts the simulated results of the SBML models with fbc extension in CSV format.
   *
   * @param fbcSolver Instance of FluxBalanceAnalysis for solving FBC model
   * @param keys      the ids of the variables present in the pre-defined result file
   * @param isSolved  boolean that shows whether model is solved or not
   * @return the StringBuilder in the CSV format
   */
  private static StringBuilder getFBCResultAsCSV(FluxBalanceAnalysis fbcSolver, String[] keys,
    boolean isSolved) {
    StringBuilder output = new StringBuilder("");
    if (isSolved) {
      Map<String, Double> fbcSolution = fbcSolver.getSolution();
      for (int i = 0; i < (keys.length - 1); i++) {
        output.append(keys[i]).append(",");
      }
      output.append(keys[keys.length - 1]).append("\n");
      for (String key : keys) {
        output.append(fbcSolution.get(key)).append(",");
      }
      if (output.length() > 0) {
        output.deleteCharAt(output.length() - 1);
        output.append("\n");
      }
    } else {
      output.append(keys[0]).append("\n").append(NAN).append("\n");
    }
    return output;
  }

  /**
   * Converts the simulated results of the SBML models as well as the SBML models with comp
   * extension in CSV format.
   *
   * @param result
   * @param variablesToAdd
   * @return the StringBuilder in the CSV format
   */
  private static StringBuilder getSBMLOrCompResultAsCSV(MultiTable result,
    boolean[] variablesToAdd) {
    StringBuilder output = new StringBuilder("");
    if (variablesToAdd[0]) {
      output.append(result.getColumnName(0)).append(",");
    }
    for (int i = 1; i < (result.getColumnCount() - 1); i++) {
      if (variablesToAdd[i]) {
        output.append(result.getColumnName(i)).append(",");
      }
    }
    if (variablesToAdd[result.getColumnCount() - 1]) {
      output.append(result.getColumnName(result.getColumnCount() - 1)).append("\n");
    } else {
      if (output.length() > 0) {
        output.deleteCharAt(output.length() - 1);
        output.append("\n");
      }
    }

    for (int i = 0; i < result.getRowCount(); i++) {
      for (int j = 0; j < (result.getColumnCount() - 1); j++) {
        if (variablesToAdd[j]) {
          output.append(result.getValueAt(i, j)).append(",");
        }
      }
      if (variablesToAdd[result.getColumnCount() - 1]) {
        output.append(result.getValueAt(i, result.getColumnCount() - 1)).append("\n");
      } else {
        if (output.length() > 0) {
          output.deleteCharAt(output.length() - 1);
          output.append("\n");
        }
      }
    }
    return output;
  }

  private static MultiTable getPredefinedTestSuiteResults(Model model, String resultFilePath) {
    CSVImporter csvimporter = new CSVImporter();
    MultiTable result = null;
    try {
      result = csvimporter.readMultiTableFromCSV(model, resultFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.error("IOException in reading the CSV file");
    }
    return result;
  }

}
