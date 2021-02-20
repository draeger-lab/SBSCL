package org.simulator.fba;

import org.junit.After;
import org.junit.Assert;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.simulator.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the BIGG models.
 */
@RunWith(value = Parameterized.class)
public class BiGGTest {

  private String resource;
  private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
  private static final double RESULT_DEVIATION = 1E-6d;
  private static final String BIGG_MODELS_RESOURCE_PATH = "/bigg/v1.5";
  private BufferedReader reader = new BufferedReader(
      new FileReader(TestUtils.getPathForTestResource("/bigg/bigg_reference_solutions.csv")));
  private Map<String, Double> referenceResults;
  static String s = "";

  @Before
  public void setUp() throws IOException {
    referenceResults = new HashMap<>();
    String line;

    while ((line = reader.readLine()) != null) {
      String[] solution = line.split(",");
      referenceResults.put(solution[0], Double.parseDouble(solution[1]));
    }
  }

  @After
  public void done() throws IOException {
    File file = new File(TestUtils.getPathForTestResource("/bigg/sbscl_bigg_model_solutions.csv"));
    FileWriter fr = new FileWriter(file);
    fr.write(s);
    fr.close();
  }

  /**
   * Returns location of BiGG test model directory from environment variable.
   */
  public static String getBiGGModelPath() {
    return TestUtils.getPathForTestResource(BIGG_MODELS_RESOURCE_PATH);
  }

  public BiGGTest(String resource) throws FileNotFoundException {
    this.resource = resource;
  }

  @Parameters(name = "{index}: {0}")
  public static Iterable<Object[]> data() {
    HashSet<String> skip = null;
    String filter = null;
    Boolean mvnResource = false;

    String biggPath = getBiGGModelPath();
    logger.info("BiGG models path: " + biggPath);
    return TestUtils.findResources(biggPath, ".xml", filter, skip, mvnResource);
  }

  @Test
  public void testFBA() throws Exception {
    logger.info("--------------------------------------------------------");
    logger.info(String.format("%s", resource));

    SBMLDocument doc = SBMLReader.read(new File(resource));
    assertNotNull(doc);

    FluxBalanceAnalysis solver = new FluxBalanceAnalysis(doc);
    boolean success = solver.solve();
    assertNotNull(success);

    double objectiveValue = solver.getObjectiveValue();
    assertTrue(objectiveValue >= 0.0);
    double[] fluxes = solver.getValues();
    assertNotNull(fluxes);

    Assert.assertEquals(objectiveValue,
        referenceResults.get(Paths.get(resource).getFileName().toString()), RESULT_DEVIATION);

    s += (Paths.get(resource).getFileName().toString() + "," + objectiveValue + "\n");
  }

}
