package org.simulator.fba;

import org.sbml.jsbml.JSBML;
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

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Test cases for the BIGG models.
 * bigg_models v1.5 (https://github.com/SBRG/bigg_models/releases)
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
        String version = "v1.5";
        String value = null;
        if (env.containsKey(key)) {
            value = env.get(key);
            value = value + "/" + version;
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

		System.out.println("Searching resources");


		// find all BiGG models (compressed files)
        String biggPath = getBiGGModelPath();
        System.out.println("BiGG models path: " + biggPath);
		return TestUtils.findResources(biggPath, ".xml.gz", filter, skip, mvnResource);
	}

	@Test
	public void testFBA() throws Exception {
        logger.info("--------------------------------------------------------");
        logger.info(String.format("%s", resource));
        System.out.println("BiGG Resource:" + resource);

        // read SBML
        InputStream is = new FileInputStream(resource);
        GZIPInputStream gzis = new GZIPInputStream(is);

        SBMLDocument doc = SBMLReader.read(gzis);
        logger.info(doc.toString());
        assertNotNull(doc);

        // TODO: solve FBA (
        /*
        COBRAsolver solver = new COBRAsolver(doc);
        if (solver.solve()) {
            System.out.println(resourceName);
            System.out.println("Objective value:\t" + solver.getObjetiveValue());
            System.out.println("Fluxes:\t" + Arrays.toString(solver.getValues()));
        } else {
            logger.error("\nSolver returned null for " + resourceName);
        }
        */

        //TODO: check against reference solution
        is.close();
	}

}