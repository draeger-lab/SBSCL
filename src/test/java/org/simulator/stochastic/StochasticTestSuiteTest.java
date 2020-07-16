package org.simulator.stochastic;

import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.simulator.TestUtils;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.MultiTable.Block.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

  public StochasticTestSuiteTest(String path) {
    this.path = path;
  }

  @Parameters(name = "{index}: {0}")
  public static Iterable<Object[]> data() {

    // environment variable for semantic test case folder
    String testsuite_path = TestUtils.getPathForTestResource(File.separator + "sbml-test-suite" + File.separator + "cases" + File.separator + "stochastic" + File.separator);
    System.out.println(STOCHASTIC_TEST_SUITE_PATH + ": " + testsuite_path);

    if (testsuite_path.length() == 0) {
      Object[][] resources = new String[0][1];
      logger.warn(String.format("%s environment variable not set.", STOCHASTIC_TEST_SUITE_PATH));
      return Arrays.asList(resources);
    }

    int N = 39;
    Object[][] resources = new String[N][1];
    for (int model_number = 1; model_number <= N; model_number++){

      // System.out.println("model " + model_number);

      StringBuilder modelFile = new StringBuilder();
      modelFile.append(model_number);
      while (modelFile.length() < 5) {
        modelFile.insert(0, '0');
      }
      String path = modelFile.toString();
      modelFile.append(File.separator);
      modelFile.append(path);
      modelFile.insert(0, testsuite_path);
      path = modelFile.toString();

      resources[(model_number - 1)][0] = path;

    }
    return Arrays.asList(resources);

  }

  @Test
  public void testModel() throws IOException {
    String sbmlfile, csvfile, configfile;
    csvfile = path + "-results.csv";
    configfile = path + "-settings.txt";

    Properties props = new Properties();
    props.load(new BufferedReader(new FileReader(configfile)));

    double duration = (!props.getProperty(DURATION).isEmpty()) ? Double.parseDouble(props.getProperty(DURATION)) : 0d;
    double steps = (!props.getProperty(STEPS).isEmpty()) ? Double.parseDouble(props.getProperty(STEPS)) : 0d;
    String outputColNames = props.getProperty("output");
    String[] list = outputColNames.split("\\s*, \\s*");

    Map<String, Object> orderedArgs = new HashMap<>();

    sbmlfile = path + "-sbml-l3v1.xml";

    orderedArgs.put("file", sbmlfile);
    orderedArgs.put("time", duration);
    orderedArgs.put("interval", (duration * 1d / steps));
    orderedArgs.put("n", 1);
    orderedArgs.put("s", new String[0]);
    orderedArgs.put("method", 0.0);
    orderedArgs.put("i", false);
    orderedArgs.put("p", "");

    // Creates a network from the SBML model
    Network net = null;
    boolean errorInNet = false;
    try {
      net = createNetwork(orderedArgs);
    } catch (Exception e){
      errorInNet = true;
    }
    Assert.assertNotNull(net);
    Assert.assertFalse(errorInNet);

    // Initializes the simulator for performing the stochastic simulation
    Simulator sim = null;
    boolean errorInSimulator = false;
    try {
      sim = createSimulator(net, orderedArgs);
    } catch (Exception e){
      errorInSimulator = true;
    }
    Assert.assertNotNull(sim);
    Assert.assertFalse(errorInSimulator);

    // Initializes the observer for the amounts of molecule species
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
    boolean errorInSimulation = false;
    try {
      runSimulation(sim, obs, orderedArgs);
    } catch (Exception e) {
      errorInSimulation = true;
    }
    Assert.assertFalse(errorInSimulation);

    // Gets the result from the observer
    double[][] output = obs.getAvgLog();

    // Gets the time points of the simulation
    double[] timepoints = output.clone()[0];

    // Gets the identifiers of the molecule species
    String[] identifiers = getIdentifiers(sim, orderedArgs);

    // 2D result array storing a simulation solution of particular simulation
    double[][] result = new double[output[0].length][output.length-1];
    for (int i = 0; i != result.length; i++) {
      Arrays.fill(result[i], Double.NaN);
    }

    for (int i = 0; i < result.length; i++){
      for (int j = 0; j < result[0].length; j++){
        result[i][j] = output[j+1][i];
      }
    }

    /*
     * Array of MultiTable storing the results of each stochastic simulation
     */
    MultiTable[] solution = new MultiTable[TOTAL_SIMULATION_COUNT];
    solution[0] = new MultiTable(timepoints, result, identifiers, null);

    // 2D array storing the square of the results required for the standard
    // deviation.
    double[][] square = new double[result.length][result[0].length];
    for (int i = 0; i < square.length; i++){
      for (int j = 0; j < square[0].length; j++){
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
    for (int i=1;i<meanSD.getColumnCount();i++){
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
        runSimulation(sim, obs, orderedArgs);
      } catch (Exception e) {
        errorInSimulation = true;
      }
      Assert.assertFalse(errorInSimulation);

      // Gets updated output from observer
      output = obs.getAvgLog();

      // Updates the current simulation result
      result = new double[output[0].length][output.length-1];

      for (int i = 0; i < result.length; i++){
        for (int j = 0; j < result[0].length; j++){
          result[i][j] = output[j+1][i];
        }
      }

      // Stores the pth simulation solution
      solution[p] = new MultiTable(timepoints, result, identifiers, null);

      // Updates the square sum using the solution from pth simulation
      for (int i = 1; i < square_sum.getColumnCount(); i++) {
        Column column = square_sum.getColumn(i);
        for (int j = 0; j < column.getRowCount(); j++) {
          double currValue = column.getValue(j);
          currValue += Math.pow(result[j][i-1], 2);
          square_sum.setValueAt(currValue, j, i);
        }
      }

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
            double sdValue = Math.sqrt((squareSumColumn.getValue(j) / (p + 1)) - Math.pow(meanValue, 2));
            meanSD.setValueAt(sdValue, j, i);
          }
        }
      }
    }

  }

  private static GnuPlot runSimulation(Simulator sim, AmountIntervalObserver obs,
      Map<String, Object> orderedArgs) throws IOException {

    GnuPlot gp = new GnuPlot();
    gp.setDefaultStyle("with linespoints");
    if ((Boolean)orderedArgs.get("i")) {
      gp.setVisible(true);
    }

    for (int i=0; i<(Integer)orderedArgs.get("n"); i++) {
      sim.start((Double)orderedArgs.get("time"));

      if ((Boolean)orderedArgs.get("i")) {
        obs.toGnuplot(gp);
        gp.plot();
        gp.clearData();
      }
    }

    return gp;
  }


  private static AmountIntervalObserver createObserver(Simulator sim,
      Map<String, Object> orderedArgs) {
    String[] species = getIdentifiers(sim, orderedArgs);
    return (AmountIntervalObserver) sim.addObserver(new AmountIntervalObserver(sim,(Double)orderedArgs.get("interval"),((Double)orderedArgs.get("time")).intValue(),species));
  }

  private static String[] getIdentifiers(Simulator sim, Map<String, Object> orderedArgs) {
    String[] species = (String[]) orderedArgs.get("s");
    if (species.length==0)
      species = NetworkTools.getSpeciesNames(sim.getNet(), NumberTools.getNumbersTo(sim.getNet().getNumSpecies()-1));
    return species;
  }

  private static Network createNetwork(Map<String, Object> orderedArgs) throws IOException,
      JDOMException, FeatureNotSupportedException, ClassNotFoundException {
    return NetworkTools.loadNetwork(new File((String) orderedArgs.get("file")));
  }

  private static Simulator createSimulator(Network net,
      Map<String, Object> orderedArgs) {
    double eps = (Double) orderedArgs.get("method");
    if (eps==0)
      return new GillespieEnhanced(net);
    else if (eps==-1)
      return new HybridMaximalTimeStep(net);
    else {
      AbstractBaseTauLeaping re = new TauLeapingSpeciesPopulationBoundSimulator(net);
      re.setEpsilon(eps);
      return re;
    }
  }



}
