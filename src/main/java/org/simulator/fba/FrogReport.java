/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, Durham, NC, US
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.fba;
import javax.xml.stream.XMLStreamException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

/**
 * Utility for creating FROG reference files (JSON) for FBA models.
 * <p>
 * The generated JSON follows the FROG schema version 1 as implemented in
 * https://github.com/matthiaskoenig/fbc_curation.
 * <p>
 * This initial implementation focuses on:
 * <ul>
 *   <li>metadata (model, software, solver, environment)</li>
 *   <li>the primary FBA objective</li>
 *   <li>empty sections for FVA and deletion analyses (placeholders for future work)</li>
 * </ul>
 */
public final class FrogReport {

  private static final Logger logger = Logger.getLogger(FrogReport.class.getName());

  private FrogReport() {
    // utility class
  }

  /**
   * Create a FROG JSON report for the given SBML FBC model.
   *
   * @param modelFile  SBML file with FBC information
   * @param outputFile JSON file to write the FROG report to
   */
  public static void writeFrogReport(File modelFile, File outputFile)
      throws SBMLException, ModelOverdeterminedException, IOException, XMLStreamException {

    if (modelFile == null || !modelFile.isFile()) {
      throw new IllegalArgumentException("Model file does not exist: " + modelFile);
    }

    SBMLDocument doc = SBMLReader.read(modelFile);
    Model model = doc.getModel();
    String modelId = model.isSetId() ? model.getId()
        : (model.isSetName() ? model.getName() : modelFile.getName());

    // Run FBA
    FluxBalanceAnalysis solver = new FluxBalanceAnalysis(doc);
    boolean solved = false;
    try {
      solved = solver.solve();
    } catch (RuntimeException exc) {
      logger.severe("Error while solving FBA model for FROG report: " + exc.getMessage());
    }

    String status = solved ? "optimal" : "infeasible";
    double objectiveValue = solved ? solver.getObjectiveValue() : 0.0;

    Map<String, Double> fluxes = solved ? solver.getSolution() : null;

    // metadata
    String modelLocation = modelFile.getName(); // location within archive; here just filename
    String modelMd5 = computeMD5(modelFile);

    String frogId = "sbscl-" + UUID.randomUUID();
    String sbsclVersion = FrogReport.class.getPackage() != null
        ? FrogReport.class.getPackage().getImplementationVersion()
        : null;
    if (sbsclVersion == null) {
      sbsclVersion = "unknown";
    }

    String os = System.getProperty("os.name", "unknown") + " "
        + System.getProperty("os.arch", "");

    // Build JSON
    StringBuilder json = new StringBuilder();
    json.append("{\n");

    // metadata
    json.append("  \"metadata\": {\n");
    json.append("    \"model.location\": ").append(jsonString(modelLocation)).append(",\n");
    json.append("    \"model.md5\": ")
        .append(modelMd5 != null ? jsonString(modelMd5) : "null").append(",\n");
    json.append("    \"frog_id\": ").append(jsonString(frogId)).append(",\n");

    // frog.software
    json.append("    \"frog.software\": {\n");
    json.append("      \"name\": ").append(jsonString("SBSCL FROG")).append(",\n");
    json.append("      \"version\": ").append(jsonString(sbsclVersion)).append(",\n");
    json.append("      \"url\": ").append(jsonString("https://github.com/draeger-lab/SBSCL"))
        .append("\n");
    json.append("    },\n");

    // frog.curators – currently a technical curator entry
    json.append("    \"frog.curators\": [\n");
    json.append("      {\n");
    json.append("        \"familyName\": ").append(jsonString("SBSCL")).append(",\n");
    json.append("        \"givenName\": ").append(jsonString("Team")).append(",\n");
    json.append("        \"email\": null,\n");
    json.append("        \"organization\": ").append(jsonString("SBSCL")).append(",\n");
    json.append("        \"site\": null,\n");
    json.append("        \"orcid\": null\n");
    json.append("      }\n");
    json.append("    ],\n");

    // software (FBA implementation)
    json.append("    \"software\": {\n");
    json.append("      \"name\": ").append(jsonString("SBSCL FluxBalanceAnalysis")).append(",\n");
    json.append("      \"version\": ").append(jsonString(sbsclVersion)).append(",\n");
    json.append("      \"url\": ").append(jsonString("https://github.com/draeger-lab/SBSCL"))
        .append("\n");
    json.append("    },\n");

    // solver (LP solver)
    json.append("    \"solver\": {\n");
    json.append("      \"name\": ").append(jsonString("SCPSolver/GLPK")).append(",\n");
    json.append("      \"version\": ").append(jsonString("unknown")).append(",\n");
    json.append("      \"url\": ").append(jsonString("https://github.com/optimatika/scpsolver"))
        .append("\n");
    json.append("    },\n");

    json.append("    \"environment\": ").append(jsonString(os.trim())).append("\n");
    json.append("  },\n");

    // objectives
    json.append("  \"objectives\": {\n");
    json.append("    \"objectives\": [\n");
    json.append("      {\n");
    json.append("        \"model\": ").append(jsonString(modelId)).append(",\n");
    json.append("        \"objective\": ")
        .append(jsonString(solver.getActiveObjective())).append(",\n");
    json.append("        \"status\": ").append(jsonString(status)).append(",\n");
    json.append("        \"value\": ").append(objectiveValue).append("\n");
    json.append("      }\n");
    json.append("    ]\n");
    json.append("  },\n");

    // fva – currently no dedicated FVA; keep empty list as placeholder
    json.append("  \"fva\": {\n");
    json.append("    \"fva\": []\n");
    json.append("  },\n");

    // reaction deletions – placeholder, empty list
    json.append("  \"reaction_deletions\": {\n");
    json.append("    \"deletions\": []\n");
    json.append("  },\n");

    // gene deletions – placeholder, empty list
    json.append("  \"gene_deletions\": {\n");
    json.append("    \"deletions\": []\n");
    json.append("  }\n");

    json.append("}\n");

    // Write file
    if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
      if (!outputFile.getParentFile().mkdirs()) {
        logger.warning("Could not create directories for output file: " + outputFile);
      }
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      writer.write(json.toString());
    }
  }

  /**
   * Compute MD5 checksum of a file; returns null if MD5 is not available.
   */
  private static String computeMD5(File file) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
          md.update(buffer, 0, read);
        }
      }
      byte[] digest = md.digest();
      return bytesToHex(digest);
    } catch (NoSuchAlgorithmException | IOException exc) {
      Logger.getLogger(FrogReport.class.getName())
          .warning("Could not compute MD5 for file " + file + ": " + exc.getMessage());
      return null;
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format(Locale.ROOT, "%02x", b));
    }
    return sb.toString();
  }

  /**
   * Quote and escape a Java string as JSON string literal.
   */
  private static String jsonString(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) {
            sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }
}   