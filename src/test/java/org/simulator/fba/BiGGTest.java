package org.simulator.fba;

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

    /**
     * HashMap for storing the reference results with key as the model names.
     */
    private Map<String, Double> referenceResults;

	@Before
	public void setUp() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(TestUtils.getPathForTestResource("/bigg/bigg_reference_solutions.csv")));
        referenceResults = new HashMap<>();
        String line;

        while ((line = reader.readLine()) != null) {
            String[] solution = line.split(",");
            referenceResults.put(solution[0], Double.parseDouble(solution[1]));
        }
	}

    /**
     * Returns location of BiGG test model directory from environment variable.
     */
    public static String getBiGGModelPath() {
        return TestUtils.getPathForTestResource("/bigg/v1.5");
    }

	public BiGGTest(String resource) {
		this.resource = resource;
	}
	
	@Parameters(name= "{index}: {0}")
	public static Iterable<Object[]> data(){
		HashSet<String> skip = null;
		String filter = null;
		Boolean mvnResource = false;

		// find all BiGG models (compressed .xml.gz files)
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
        assertTrue(objectiveValue>=0.0);
        double[] fluxes = solver.getValues();
        assertNotNull(fluxes);

        Assert.assertEquals(objectiveValue, referenceResults.get(Paths.get(resource).getFileName().toString()), RESULT_DEVIATION);

	}

}
