package org.simulator.omex;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.jdom2.JDOMException;
import org.junit.Test;
import org.simulator.TestUtils;

import de.unirostock.sems.cbarchive.CombineArchiveException;


public class OMEXArchiveTest {

  @Test
  public void testOMEXArchive1()
      throws CombineArchiveException, JDOMException, ParseException, IOException {

    String omexPath = TestUtils.getPathForTestResource("/omex/12859_2014_369_MOESM1_ESM.omex");
    OMEXArchive archive = new OMEXArchive(new File(omexPath));

    // archive exists
    assertNotNull(archive);

    // archive contains SBML model
    assertTrue(archive.containsSBMLModel());

    // archive contains SED-ML model
    assertTrue(archive.containsSEDMLDescp());
  }
}
