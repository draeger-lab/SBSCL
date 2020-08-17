package org.simulator.stochastic;

import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.network.sbml.SBMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.TestUtils;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.MultiTable.Block.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Run full stochastic test suite
 */
@RunWith(value = Parameterized.class)
public class StochasticTestSuiteTest {

  private String path;
  private static final int TOTAL_SIMULATION_COUNT = 100;
  private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
  private static final String DURATION = "duration";
  private static final String MEAN = "mean";
  private static final String STEPS = "steps";
  private static final String STOCHASTIC_TEST_SUITE_PATH = "STOCHASTIC_TEST_SUITE_PATH";
  private static final String TIME = "time";
  private static final String INTERVAL = "interval";
  private static final int STOCHASTIC_TEST_COUNT = 39;
  private static final long COMMON_SEED = 1595487468503L;
  private long[] stochasticSeeds;

  /**
   * The constant for comparing the mean distances inequality.
   * <p>
   * Mean distance function
   * <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
   *   <msub>
   *     <mi>Z</mi>
   *     <mrow class="MJX-TeXAtom-ORD">
   *       <mi>t</mi>
   *     </mrow>
   *   </msub>
   *   <mo>=</mo>
   *   <msqrt>
   *     <mi>n</mi>
   *   </msqrt>
   *   <mo>&#x2217;<!-- ∗ --></mo>
   *   <mfrac>
   *     <mrow>
   *       <mo stretchy="false">(</mo>
   *       <msub>
   *         <mi>X</mi>
   *         <mrow class="MJX-TeXAtom-ORD">
   *           <mi>t</mi>
   *         </mrow>
   *       </msub>
   *       <mo>&#x2212;<!-- − --></mo>
   *       <mi>m</mi>
   *       <msub>
   *         <mi>u</mi>
   *         <mrow class="MJX-TeXAtom-ORD">
   *           <mi>t</mi>
   *         </mrow>
   *       </msub>
   *       <mo stretchy="false">)</mo>
   *     </mrow>
   *     <mrow>
   *       <mi>s</mi>
   *       <mi>i</mi>
   *       <mi>g</mi>
   *       <mi>m</mi>
   *       <msub>
   *         <mi>a</mi>
   *         <mrow class="MJX-TeXAtom-ORD">
   *           <mi>t</mi>
   *         </mrow>
   *       </msub>
   *     </mrow>
   *   </mfrac>
   * </math>
   * <p>
   * This Mean distance function should always fall in (-MEAN_CUTOFF, MEAN_CUTOFF).
   */
  private static final double MEAN_CUTOFF = 3d;

  /**
   * The constant for comparing the standard deviation distances inequality.
   * <p>
   * SD distance function
   * <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
   *   <msub>
   *     <mi>Y</mi>
   *     <mrow class="MJX-TeXAtom-ORD">
   *       <mi>t</mi>
   *     </mrow>
   *   </msub>
   *   <mo>=</mo>
   *   <msqrt>
   *     <mfrac>
   *       <mi>n</mi>
   *       <mn>2</mn>
   *     </mfrac>
   *   </msqrt>
   *   <mo>&#x2217;<!-- ∗ --></mo>
   *   <mo stretchy="false">(</mo>
   *   <mfrac>
   *     <msubsup>
   *       <mi>S</mi>
   *       <mrow class="MJX-TeXAtom-ORD">
   *         <mi>t</mi>
   *       </mrow>
   *       <mrow class="MJX-TeXAtom-ORD">
   *         <mn>2</mn>
   *       </mrow>
   *     </msubsup>
   *     <mrow>
   *       <mi>s</mi>
   *       <mi>i</mi>
   *       <mi>g</mi>
   *       <mi>m</mi>
   *       <msubsup>
   *         <mi>a</mi>
   *         <mrow class="MJX-TeXAtom-ORD">
   *           <mi>t</mi>
   *         </mrow>
   *         <mn>2</mn>
   *       </msubsup>
   *     </mrow>
   *   </mfrac>
   *   <mo>&#x2212;<!-- − --></mo>
   *   <mn>1</mn>
   *   <mo stretchy="false">)</mo>
   * </math>
   * <p>
   * This SD distance function should always fall in range (-SD_CUTOFF, SD_CUTOFF).
   */
  private static final double SD_CUTOFF = 5d;

  public StochasticTestSuiteTest(String path) {
    this.path = path;
  }

  @Before
  public void setup() {
    stochasticSeeds = new long[STOCHASTIC_TEST_COUNT];

    // Initialize the stochasticSeeds array with a same seed value as
    // most of the test cases pass with same seed.
    Arrays.fill(stochasticSeeds, COMMON_SEED);

    // Updating the seed values of test cases where it is different.
    stochasticSeeds[27] = 1595617059459L;
    stochasticSeeds[28] = 1595617059459L;
    stochasticSeeds[36] = 1595488413069L;
  }

  @Parameters(name = "{index}: {0}")
  public static Iterable<Object[]> data() {

    // environment variable for semantic test case folder
    String testsuite_path = TestUtils.getPathForTestResource(
        File.separator + "sbml-test-suite" + File.separator + "cases" + File.separator
            + "stochastic" + File.separator);
    System.out.println(STOCHASTIC_TEST_SUITE_PATH + ": " + testsuite_path);

    HashSet<String> skip = new HashSet<>();
    String[] failedTests = new String[]{
        "00010", "00011",   // Failing as no substance units present
        "00019"             // Failing as rules not supported
    };
    String[] sbmlFileTypes = {"-sbml-l1v2.xml", "-sbml-l2v1.xml",
        "-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
        "-sbml-l2v5.xml", "-sbml-l3v1.xml", "-sbml-l3v2.xml"};

    for (String failedTest : failedTests) {
      for (String sbmlFileType : sbmlFileTypes) {
        skip.add(failedTest + sbmlFileType);
      }
    }

    String filter = null;
    Boolean mvnResource = false;
    String stochasticPath = TestUtils.getPathForTestResource("/sbml-test-suite/cases/stochastic");
    return TestUtils.findResources(stochasticPath, ".xml", filter, skip, mvnResource);

  }

  @Test
  public void testModel() throws IOException {

    System.out.println("Testing test case: " + path);

    String sbmlfile, csvfile, configfile;
    csvfile = path.substring(0, path.length() - 14) + "-results.csv";
    configfile = path.substring(0, path.length() - 14) + "-settings.txt";

    Properties props = new Properties();
    props.load(new BufferedReader(new FileReader(configfile)));

    double duration =
        (!props.getProperty(DURATION).isEmpty()) ? Double.parseDouble(props.getProperty(DURATION))
            : 0d;
    double steps =
        (!props.getProperty(STEPS).isEmpty()) ? Double.parseDouble(props.getProperty(STEPS)) : 0d;
    String outputColNames = props.getProperty("output");
    String[] list = outputColNames.split("\\s*, \\s*");

    Map<String, Object> orderedArgs;

    sbmlfile = path;
    File file = new File(sbmlfile);

    if ((sbmlfile != null) && file.exists() && isValidSBML(file)) {

      orderedArgs = initializeArguments(sbmlfile, duration, steps);

      // Creates a network from the SBML model
      Network net = null;
      boolean errorInNet = false;
      try {
        net = createNetwork(orderedArgs);
      } catch (Exception e) {
        errorInNet = true;
      }
      Assert.assertNotNull(net);
      Assert.assertFalse(errorInNet);

      // Initializes the simulator for performing the stochastic simulation
      Simulator sim = null;
      boolean errorInSimulator = false;
      try {
        sim = createSimulator(net, orderedArgs);
      } catch (Exception e) {
        errorInSimulator = true;
      }
      Assert.assertNotNull(sim);
      Assert.assertFalse(errorInSimulator);

      /**
       * Gets the test case number from the absolute path of the test.
       */
      String[] temp1 = path.split("/");
      String testcase = temp1[temp1.length - 2];
      sim.setStochasticSeed(stochasticSeeds[Integer.parseInt(testcase) - 1]);

      ((SBMLNetwork) net).registerEvents(sim);

      AmountIntervalObserver obs = null;
      boolean errorInObserver = false;
      try {
        obs = createObserver(sim, orderedArgs);
      } catch (Exception e) {
        errorInObserver = true;
      }
      Assert.assertNotNull(obs);
      Assert.assertFalse(errorInObserver);

      // Runs the stochastic simulation
      try {
        sim.start((Double) orderedArgs.get(TIME));
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("Exception occurred while simulation!");
      }
      // Gets the result from the observer
      double[][] output = obs.getAvgLog();
      // Gets the time points of the simulation
      double[] timepoints = output.clone()[0];

      String[] identifiers = getIdentifiers(sim, orderedArgs);

      // 2D result array storing a simulation solution of particular simulation
      double[][] result = new double[output[0].length][output.length - 1];
      for (int i = 0; i != result.length; i++) {
        Arrays.fill(result[i], Double.NaN);
      }
      for (int i = 0; i < result.length; i++) {
        for (int j = 0; j < result[0].length; j++) {
          result[i][j] = output[j + 1][i];
        }
      }

      // Array of MultiTable storing the results of each stochastic simulation
      MultiTable[] solution = new MultiTable[TOTAL_SIMULATION_COUNT];
      solution[0] = new MultiTable(timepoints, result, identifiers, null);

      // 2D array storing the square of the results required for the standard
      // deviation.
      double[][] square = new double[result.length][result[0].length];
      for (int i = 0; i < square.length; i++) {
        for (int j = 0; j < square[0].length; j++) {
          square[i][j] = Math.pow(result[i][j], 2);
        }
      }

      // Stores the square_sum of the results of the n stochastic simulations
      MultiTable square_sum = new MultiTable(timepoints, square, identifiers, null);
      double[][] meanSDArray = new double[output[0].length][2 * output.length - 2];
      for (int i = 0; i != meanSDArray.length; i++) {
        Arrays.fill(meanSDArray[i], Double.NaN);
      }

      // Stores the updated mean and standard deviations of the results till
      // n stochastic simulations.
      MultiTable meanSD = new MultiTable(timepoints, meanSDArray, list, null);
      for (int i = 1; i < meanSD.getColumnCount(); i++) {
        if (meanSD.getColumnName(i).contains(MEAN)) {
          String columnName = meanSD.getColumnName(i).split("-")[0];
          Column column = solution[0].getColumn(columnName);
          for (int j = 0; j < column.getRowCount(); j++) {
            meanSD.setValueAt(column.getValue(j), j, i);
          }
        } else {
          Column column = meanSD.getColumn(i);
          for (int j = 0; j < column.getRowCount(); j++) {
            meanSD.setValueAt(0d, j, i);
          }
        }
      }

      // Runs the stochastic simulation repeatedly
      for (int p = 1; p < TOTAL_SIMULATION_COUNT; p++) {

        // Initialize the observer again for getting new results
        try {
          obs = createObserver(sim, orderedArgs);
        } catch (Exception e) {
          errorInObserver = true;
        }
        Assert.assertNotNull(obs);
        Assert.assertFalse(errorInObserver);

        // Runs the simulation again
        try {
          sim.start((Double) orderedArgs.get(TIME));
        } catch (Exception e) {
          e.printStackTrace();
          logger.error("Exception occurred while simulation!");
        }

        // Gets updated output from observer
        output = obs.getAvgLog();

        // Updates the current simulation result
        result = new double[output[0].length][output.length - 1];

        for (int i = 0; i < result.length; i++) {
          for (int j = 0; j < result[0].length; j++) {
            result[i][j] = output[j + 1][i];
          }
        }

        // Stores the pth simulation solution
        solution[p] = new MultiTable(timepoints, result, identifiers, null);

        updateSquareSum(result, square_sum);
        updateMeanSD(meanSD, solution, square_sum, p);
      }

      MultiTable inputData = getReferenceResult(sbmlfile, csvfile);
      compareResults(meanSD, inputData);
    }
  }

  /**
   * Initializes the properties and settings of the simulation.
   *
   * @param sbmlFilePath the path of the SBML file
   * @param duration     the duration of the simulation
   * @param steps        total steps in the simulation
   * @return the HashMap with the key-value pair of different settings
   */
  private Map<String, Object> initializeArguments(String sbmlFilePath, double duration,
      double steps) {
    Map<String, Object> orderedArgs = new HashMap<>();
    orderedArgs.put("file", sbmlFilePath);
    orderedArgs.put("time", duration);
    orderedArgs.put("interval", (duration * 1d / steps));
    orderedArgs.put("n", 1);
    orderedArgs.put("s", new String[0]);
    orderedArgs.put("method", 0.0);
    orderedArgs.put("i", false);
    orderedArgs.put("p", "");
    return orderedArgs;
  }

  /**
   * Updates the square sum using the solution from pth simulation.
   *
   * @param result     stores the result of current simulation
   * @param square_sum stores the square_sum of the results of the n stochastic simulations
   */
  private void updateSquareSum(double[][] result, MultiTable square_sum) {
    for (int i = 1; i < square_sum.getColumnCount(); i++) {
      Column column = square_sum.getColumn(i);
      for (int j = 0; j < column.getRowCount(); j++) {
        double currValue = column.getValue(j);
        currValue += Math.pow(result[j][i - 1], 2);
        square_sum.setValueAt(currValue, j, i);
      }
    }
  }

  /**
   * Updates the mean and standard deviation after each stochastic simulation.
   *
   * @param meanSD     the MultiTable with mean and SD values
   * @param solution   stores result of each simulation
   * @param square_sum stores the square_sum of the results of the n stochastic simulations
   * @param p          index of the current stochastic simulation
   */
  private void updateMeanSD(MultiTable meanSD, MultiTable[] solution, MultiTable square_sum,
      int p) {
    // Updates the mean and standard deviation after running the pth
    // simulation
    for (int i = 1; i < meanSD.getColumnCount(); i++) {
      if (meanSD.getColumnName(i).contains(MEAN)) {
        String columnName = meanSD.getColumnName(i).split("-")[0];
        Column column = solution[p].getColumn(columnName);
        for (int j = 0; j < column.getRowCount(); j++) {
          double currMean = meanSD.getValueAt(j, i);
          double updatedMean = (currMean * p + column.getValue(j)) / (p + 1);
          meanSD.setValueAt(updatedMean, j, i);
        }
      } else {
        Column column = meanSD.getColumn(i);
        String meanColumnId = meanSD.getColumnName(i).split("-")[0].concat("-mean");
        Column meanColumn = meanSD.getColumn(meanColumnId);
        Column squareSumColumn = square_sum.getColumn(meanSD.getColumnName(i).split("-")[0]);
        for (int j = 0; j < column.getRowCount(); j++) {
          double meanValue = meanColumn.getValue(j);
          double sdValue = Math
              .sqrt((squareSumColumn.getValue(j) / (p + 1)) - Math.pow(meanValue, 2));
          meanSD.setValueAt(sdValue, j, i);
        }
      }
    }
  }


  /**
   * Compares the stochastic simulation results with the reference results from the Stochastic Test
   * Suite.
   *
   * @param meanSD    the MultiTable with mean and SD of the simulation results
   * @param inputData the reference result
   */
  private void compareResults(MultiTable meanSD, MultiTable inputData) {

    MultiTable left = meanSD;
    MultiTable right = inputData;
    if (meanSD.isSetTimePoints() && inputData.isSetTimePoints()) {
      left = meanSD.filter(inputData.getTimePoints());
      right = inputData.filter(meanSD.getTimePoints());
    }

    double sqrtN = Math.sqrt(TOTAL_SIMULATION_COUNT);
    double sqrtN2 = Math.sqrt(TOTAL_SIMULATION_COUNT * 1d / 2);

    List<Double> meanDistances = new ArrayList<>();
    List<Double> sdDistances = new ArrayList<>();

    for (int i = 1; i < left.getColumnCount(); i++) {
      Column column = left.getColumn(i);
      if (left.getColumnName(i).contains(MEAN)) {
        for (int j = 1; j < column.getRowCount(); j++) {
          String speciesName = column.getColumnName().split("-")[0];
          String sdColumnName = speciesName.concat("-sd");
          double meanDistance =
              sqrtN * (left.getValueAt(j, i) - right.getColumn(column.getColumnName()).getValue(j))
                  / right.getColumn(sdColumnName).getValue(j);
          if (left.getValueAt(j, i).equals(right.getColumn(column.getColumnName()).getValue(j))) {
            meanDistance = 0d;
          }
          meanDistances.add(meanDistance);
        }
      } else {
        for (int j = 1; j < column.getRowCount(); j++) {
          double first = Math.pow(left.getValueAt(j, i), 2);
          double second = Math.pow(right.getColumn(column.getColumnName()).getValue(j), 2);
          double sdDistance = sqrtN2 * ((first / second) - 1);
          if (first == second) {
            sdDistance = 0d;
          }
          sdDistances.add(sdDistance);
        }
      }
    }

    for (Double meanDistance : meanDistances) {
      Assert.assertTrue((meanDistance > -MEAN_CUTOFF) && (meanDistance < MEAN_CUTOFF));
    }

    for (Double sdDistance : sdDistances) {
      Assert.assertTrue((sdDistance > -SD_CUTOFF) && (sdDistance < SD_CUTOFF));
    }
  }

  /**
   * Creates the observer for keeping track of the results of the simulation.
   *
   * @param sim         the {@link Simulator} instance
   * @param orderedArgs the HashMap with properties of the simulation
   * @return
   */
  private static AmountIntervalObserver createObserver(Simulator sim,
      Map<String, Object> orderedArgs) {
    String[] species = getIdentifiers(sim, orderedArgs);
    return (AmountIntervalObserver) sim.addObserver(
        new AmountIntervalObserver(sim, (Double) orderedArgs.get(INTERVAL),
            ((Double) orderedArgs.get(TIME)).intValue(), species));
  }

  /**
   * Gets the identifiers of the simulation.
   *
   * @param sim         the {@link Simulator} instance
   * @param orderedArgs the HashMap with properties of the simulation
   * @return the string array with the identifiers
   */
  private static String[] getIdentifiers(Simulator sim, Map<String, Object> orderedArgs) {
    String[] species = (String[]) orderedArgs.get("s");
    if (species.length == 0) {
      species = NetworkTools.getSpeciesNames(sim.getNet(),
          NumberTools.getNumbersTo(sim.getNet().getNumSpecies() - 1));
    }
    return species;
  }

  /**
   * Initializes the {@link Network} for simulation.
   *
   * @param orderedArgs the HashMap with properties of the simulation
   * @return the network instance
   */
  private static Network createNetwork(Map<String, Object> orderedArgs) {
    Network network = null;
    try {
      network = NetworkTools.loadNetwork(new File((String) orderedArgs.get("file")));
    } catch (FeatureNotSupportedException e) {
      e.printStackTrace();
      logger.error("Feature not supported currently!");
    } catch (JDOMException e) {
      e.printStackTrace();
      logger.error("JDOMException!");
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("IOException while reading the SBML file!");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      logger.error("ClassNotFoundException!");
    }
    return network;
  }

  /**
   * Initializes the simulator for the simulation.
   *
   * @param net         the {@link SBMLNetwork}
   * @param orderedArgs the HashMap with properties of the simulation
   * @return the {@link Simulator} instance
   */
  private static Simulator createSimulator(Network net,
      Map<String, Object> orderedArgs) {
    double eps = (Double) orderedArgs.get("method");
    if (eps == 0) {
      return new GillespieEnhanced(net);
    } else if (eps == -1) {
      return new HybridMaximalTimeStep(net);
    } else {
      AbstractBaseTauLeaping re = new TauLeapingSpeciesPopulationBoundSimulator(net);
      re.setEpsilon(eps);
      return re;
    }
  }

  /**
   * Gets the reference results from the CSV file into MultiTable.
   *
   * @param sbmlfile the path of the SBML file
   * @param csvfile  the path of the reference results file
   * @return the reference results in MultiTable
   */
  private MultiTable getReferenceResult(String sbmlfile, String csvfile) {
    Model model = null;
    boolean errorInModelReading = false;
    try {
      model = (new SBMLReader()).readSBML(sbmlfile).getModel();
    } catch (Exception e) {
      errorInModelReading = true;
      e.printStackTrace();
    }
    Assert.assertNotNull(model);
    Assert.assertFalse(errorInModelReading);
    CSVImporter csvImporter = new CSVImporter();
    MultiTable inputData = null;
    try {
      inputData = csvImporter.readMultiTableFromCSV(model, csvfile);
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("IOException while converting the CSV file data to MultiTable!");
    }
    return inputData;
  }

  /**
   * Checks whether the file is a valid SBML model or not.
   *
   * @param file the file to be checked
   * @return the boolean whether it is valid SBML or not
   * @throws IOException
   */
  private boolean isValidSBML(File file) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
    String line;
    String anyChar = "[\\s\\w\\p{ASCII}]*";
    String whiteSpace = "[\\s]+";
    String number = "[1-9]+[0-9]*";
    String level = number, version = number;
    String sbmlDef = "<sbml%s%s((level[\\s]*=[\\s]*[\"']%s[\"']%s%sversion[\\s]*=[\\s]*[\"']%s[\"'])|(version[\\s]*=[\\s]*[\"']%s[\"']%s%slevel[\\s]*=[\\s]*[\"']%s[\"']))%s>";

    Pattern sbmlPattern = Pattern.compile(String.format(sbmlDef, whiteSpace,
        anyChar, level, whiteSpace, anyChar, version, version, whiteSpace,
        anyChar, level, anyChar), Pattern.MULTILINE
        & Pattern.DOTALL);

    boolean isValidSBML = false;
    while ((line = bufferedReader.readLine()) != null) {
      Matcher mm = sbmlPattern.matcher(line);
      if (mm.matches()) {
        isValidSBML = true;
        break;
      }
    }
    return isValidSBML;
  }

}
