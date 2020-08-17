package org.simulator.distance;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.simulator.TestUtils;
import org.simulator.io.CSVImporter;
import org.simulator.math.MaxDivergenceTolerance;
import org.simulator.math.QualityMeasure;
import org.simulator.math.odes.MultiTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@RunWith(value = Parameterized.class)
public class MaxDivergenceToleranceTest {

  private static final Logger logger = LoggerFactory.getLogger(MaxDivergenceTolerance.class);
  private String resource;
  private static final String MAX_DIVERGENCE_TOLERANCE_PATH = "MAX_DIVERGENCE_TOLERANCE_PATH";
  private static final Double absTol = 1E-6;
  private static final Double relTol = 1E-3;

  public MaxDivergenceToleranceTest(String resource) {
    this.resource = resource;
  }

  /**
   * Max absolute distance test cases
   *
   * @return
   */
  @Parameterized.Parameters(name = "{index}: {0}")
  public static Iterable<Object[]> data() {

    String distance_path = TestUtils
        .getPathForTestResource(File.separator + "distance" + File.separator + "test");
    System.out.println(MAX_DIVERGENCE_TOLERANCE_PATH + ": " + distance_path);

    if (distance_path.length() == 0) {
      Object[][] resources = new String[0][1];
      logger.warn(String.format("%s environment variable not set.", MAX_DIVERGENCE_TOLERANCE_PATH));
      return Arrays.asList(resources);
    }

    int N = 2;
    Object[][] resources = new String[N][1];
    for (int test_number = 1; test_number <= N; test_number++) {

      StringBuilder modelFile = new StringBuilder();
      modelFile.append(test_number);
      modelFile.append('/');
      modelFile.insert(0, distance_path);
      String path = modelFile.toString();

      resources[(test_number - 1)][0] = path;

    }
    return Arrays.asList(resources);

  }

  @Test
  public void testMaxDivergenceTolerance() throws IOException {

    // configuration
    String filePath = resource;

    String first = filePath + "a.csv";
    String second = filePath + "b.csv";
    String result = filePath + "max_div_tol_result.csv";

    // convert the test files to MultiTable
    CSVImporter csvImporter = new CSVImporter();
    MultiTable table1 = csvImporter.readMultiTableFromCSV(null, first);
    MultiTable table2 = csvImporter.readMultiTableFromCSV(null, second);

    // calculates max absolute distance
    QualityMeasure maxAbsDistance = new MaxDivergenceTolerance(absTol, relTol);
    List<Double> distances = maxAbsDistance.getColumnDistances(table1, table2);

    // get pre-defined results from the test case
    List<Double> inputData = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(result))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        inputData.add(Double.parseDouble(values[0]));
      }
    }

    System.out.println("Results: " + distances);
    System.out.println("Pre-defined Results: " + inputData);

    for (int i = 0; i < distances.size(); i++) {
      Assert.assertEquals(distances.get(i), inputData.get(i), 0.02);
    }

  }

}
