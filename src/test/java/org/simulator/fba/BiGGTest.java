package org.simulator.fba;

import org.junit.Ignore;
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
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the BIGG models.
 */
@RunWith(value = Parameterized.class)
public class BiGGTest {
	private String resource;
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);


	@Before
	public void setUp(){ }

    /**
     * Returns location of BiGG test model directory from environment variable.
     */
    public static String getBiGGModelPath() {
        Map<String, String> env = System.getenv();
        String key = "BIGG_MODELS_PATH";
        String value = null;
        if (env.containsKey(key)) {
            value = env.get(key);
            logger.info(String.format("BiGG models folder found: %s", value));
        }
        else {
            logger.info(String.format("%s environment variable not set.", key));
        }
        return value;
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
        System.out.println("BiGG models path: " + biggPath);
		return TestUtils.findResources(biggPath, ".xml.gz", filter, skip, mvnResource);
	}

	@Test
    @Ignore
	public void testFBA() throws Exception {
        logger.info("--------------------------------------------------------");
        logger.info(String.format("%s", resource));
        System.out.println("BiGG Resource:" + resource);

        if ((resource.endsWith("iAF987.xml.gz") ||
                (resource.endsWith("iAF692.xml.gz")))) {
            /*
            BiGG Resource://home/mkoenig/git/sbscl-shalin/src/test/resources/bigg/v1.5/iAF987.xml.gz
            glp_free: memory allocation error
            Error detected in file env/alloc.c at line 72

            Process finished with exit code 134 (interrupted by signal 6: SIGABRT)
            */
            return;
        }


        // read SBML
        InputStream is = new FileInputStream(resource);
        GZIPInputStream gzis = new GZIPInputStream(is);

        SBMLDocument doc = SBMLReader.read(gzis);
        logger.info(doc.toString());
        assertNotNull(doc);

        FluxBalanceAnalysis solver = new FluxBalanceAnalysis(doc);
        boolean success = solver.solve();
        assertNotNull(success);

        double objectiveValue = solver.getObjectiveValue();
        assertTrue(objectiveValue>=0.0);
        double[] fluxes = solver.getValues();
        assertNotNull(fluxes);

        //TODO: check against reference solution
        is.close();
	}

}