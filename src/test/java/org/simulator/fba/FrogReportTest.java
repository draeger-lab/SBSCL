package org.simulator.fba;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.TestUtils;

/**
 * Basic smoke test for FROG report generation.
 *
 * This test runs FBA on the e_coli_core model and creates a FROG JSON file.
 * It only checks that the file is created and contains the main FROG sections.
 */
public class FrogReportTest {

  @Test
  public void createFrogReportForEColiCore()
      throws SBMLException, ModelOverdeterminedException, XMLStreamException, Exception {

    // SBML FBC model used in other FBA tests
    String modelPath = TestUtils.getPathForTestResource("/fba/e_coli_core.xml");
    File modelFile = new File(modelPath);
    assertTrue("Model file must exist for test", modelFile.isFile());

    // Output location under target so it is cleaned with the build
    File outDir = new File("target/test-output/frog");
    if (!outDir.exists()) {
      assertTrue("Could not create output directory", outDir.mkdirs());
    }
    File frogFile = new File(outDir, "e_coli_core_frog.json");

    // Generate FROG report
    FrogReport.writeFrogReport(modelFile, frogFile);

    // Basic checks on the created file
    assertTrue("FROG report file must exist", frogFile.isFile());
    assertTrue("FROG report file must not be empty", frogFile.length() > 0L);

    // Read content and check for main sections of the FROG schema
    String content = new String(Files.readAllBytes(frogFile.toPath()), StandardCharsets.UTF_8);
    assertNotNull(content);
    assertTrue("FROG report must contain metadata section", content.contains("\"metadata\""));
    assertTrue("FROG report must contain objectives section", content.contains("\"objectives\""));
    assertTrue("FROG report must contain fva section", content.contains("\"fva\""));
    assertTrue("FROG report must contain reaction_deletions section",
        content.contains("\"reaction_deletions\""));
    assertTrue("FROG report must contain gene_deletions section",
        content.contains("\"gene_deletions\""));
  }
}