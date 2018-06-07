package org.simulator.fba;

import org.simulator.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


import java.util.HashSet;

/**
 * Test cases for the BIGG models.
 * bigg_models v1.4 (https://github.com/SBRG/bigg_models/releases)
 * 
 * Models were retrieved on 2017-10-10 from the available database dumps on
 * dropbox.
 */
@RunWith(value = Parameterized.class)
public class BiGGTest {
	private String resource;


	@Before
	public void setUp(){ }
	
	public BiGGTest(String resource) {
		this.resource = resource;
	}
	
	@Parameters(name= "{index}: {0}")
	public static Iterable<Object[]> data(){
		HashSet<String> skip = null;
		String filter = null;

		// find all BiGG models (FIXME: work on compressed models)

        String bigg_path = TestUtils.getBiGGModelPath();
		return TestUtils.findResources(bigg_path, ".xml", filter, skip);
	}

	// TODO: read cobrapy reference values for comparison
	/**
	@Test
	public void testSingle() throws Exception {
		TestUtils.testNetwork(taskMonitor, getClass().getName(), resource);
	}

	@Test
    public void testSerialization() throws Exception {
        TestUtils.testNetworkSerialization(getClass().getName(), resource);
    }
	*/

}