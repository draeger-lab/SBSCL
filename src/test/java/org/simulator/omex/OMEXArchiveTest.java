package org.simulator.omex;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simulator.TestUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.*;


public class OMEXArchiveTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testOMEXArchive1() throws CombineArchiveException, JDOMException, ParseException, IOException {

        String omexPath = TestUtils.getPathForTestResource("/omex/12859_2014_369_MOESM1_ESM.zip");
        OMEXArchive archive = new OMEXArchive(new File(omexPath));

        // archive exists
        assertNotNull(archive);

        // archive contains SBML model
        assertTrue(archive.containsSBMLModel());

        // archive contains SED-ML model
        assertTrue(archive.containsSEDMLDescp());

        // close the file object
        archive.close();
    }
}
