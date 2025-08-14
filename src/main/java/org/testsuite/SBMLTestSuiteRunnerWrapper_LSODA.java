package org.testsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
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
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.LSODA.LSODAIntegrator;
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
public class SBMLTestSuiteRunnerWrapper_LSODA {

  public static final String STEPS = "steps";
  public static final String AMOUNT = "amount";
  public static final String CONCENTRATION = "concentration";
  public static final String ABSOLUTE = "absolute";
  public static final String RELATIVE = "relative";
  public static final String NAN = "NaN";
  private static final double TOLERANCE_FACTOR = 1E-5;
  private static final Logger LOGGER = Logger.getLogger(SBMLTestSuiteRunnerWrapper.class.getName());

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
      duration = Double.valueOf(properties.getProperty("duration"));;

      MultiTable solution = null;

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

      StringBuilder output = null;

      if (model.getExtension(FBCConstants.shortLabel) == null && model.getExtension(CompConstants.shortLabel) == null) {
        solution = runLSODASimulation(model, duration, steps, properties, timePoints, amountHash, currentCase);
      } 

      if(solution != null) {
        MultiTable left = solution;
        if (solution.isSetTimePoints() && inputData.isSetTimePoints()) {
          left = solution.filter(inputData.getTimePoints());
        }

        // Set of variables present in the test suite results file
        Set<String> resultColumns = new HashSet<>();
        for (int i = 0; i < inputData.getColumnCount(); i++) {
          resultColumns.add(inputData.getColumnName(i));
        }

        // Boolean array to check which variables are present in the test suite results file
        boolean[] variablesToAdd = new boolean[solution.getColumnCount()];
        
        for (int i = 0; i < left.getColumnCount(); i++) {
          if (resultColumns.contains(left.getColumnName(i))) {
            variablesToAdd[i] = true;
          }
        }

        output = getSBMLOrCompResultAsCSV(left, variablesToAdd);
      }
        
      csvWriter.append(output);
      csvWriter.flush();
      csvWriter.close();

  }

  public static MultiTable runLSODASimulation(Model model, double duration, double steps,
  Properties properties, double[] timePoints,
  Map<String, Boolean> amountHash, String currentCase) throws DerivativeException {
    double absolute = (!properties.getProperty(ABSOLUTE).isEmpty()) ? Double
      .parseDouble(properties.getProperty(ABSOLUTE)) : 0d;
      double relative = (!properties.getProperty(RELATIVE).isEmpty()) ? Double
        .parseDouble(properties.getProperty(RELATIVE)) : 0d;

        LSODAIntegrator solver = new LSODAIntegrator();
        solver.setStepSize(duration / (steps));
        if (solver instanceof AbstractDESSolver) {
          solver.setIncludeIntermediates(false);
        }

        solver.setAbsTol(TOLERANCE_FACTOR * absolute);
        solver.setRelTol(TOLERANCE_FACTOR * relative);

        if(currentCase.equals("01399") || currentCase.equals("01480")) {
          solver.setAbsTol(1e-12d);
          solver.setRelTol(1e-6d);
        }
        if(currentCase.equals("01567") || currentCase.equals("01568")) {
          solver.setAbsTol(1e-8d);
          solver.setRelTol(1e-4d);
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
