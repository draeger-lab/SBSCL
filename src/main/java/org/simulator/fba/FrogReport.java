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

import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import scpsolver.lpsolver.LinearProgramSolver;

/**
 * Utility for creating FROG reference files (JSON) for FBA models.
 * <p>
 * The generated JSON follows the FROG schema version 1 as implemented in
 * https://github.com/matthiaskoenig/fbc_curation.
 */
public final class FrogReport {

  private static final Logger logger = Logger.getLogger(FrogReport.class.getName());

  private FrogReport() {
    // utility class
  }

  /**
   * Convenience method: read the model from a file and write a FROG report.
   *
   * @param modelFile  SBML file with FBC information
   * @param outputFile JSON file to write the FROG report to
   */
  public static void writeFrogReport(File modelFile, File outputFile)
      throws SBMLException, ModelOverdeterminedException, IOException, XMLStreamException {

    if (modelFile == null || !modelFile.isFile()) {
      throw new IllegalArgumentException("Model file does not exist: " + modelFile);
    }

    SBMLDocument document = SBMLReader.read(modelFile);
    String modelLocation = modelFile.getName();
    String modelMd5 = computeMD5(modelFile);

    writeFrogReportInternal(document, modelLocation, modelMd5, outputFile);
  }

  /**
   * Create a FROG JSON report for the given SBML FBC model.
   *
   * @param document   SBMLDocument with FBC information
   * @param outputFile JSON file to write the FROG report to
   */
  public static void writeFrogReport(SBMLDocument document, File outputFile)
      throws SBMLException, ModelOverdeterminedException, IOException {

    if (document == null || !document.isSetModel()) {
      throw new IllegalArgumentException("SBMLDocument does not contain a model.");
    }

    Model model = document.getModel();
    String modelId = model.isSetId() ? model.getId()
        : (model.isSetName() ? model.getName() : "model");

    // when called with SBMLDocument directly, we don't know the file path/MD5
    writeFrogReportInternal(document, modelId, null, outputFile);
  }

  /**
   * Internal helper that does the actual work once we have an SBMLDocument and
   * optional location/MD5 information.
   */
  private static void writeFrogReportInternal(SBMLDocument document,
                                              String modelLocation,
                                              String modelMd5,
                                              File outputFile)
      throws SBMLException, ModelOverdeterminedException, IOException {

    Model model = document.getModel();
    String modelId = model.isSetId() ? model.getId()
        : (model.isSetName() ? model.getName() : modelLocation);

    // Run FBA
    FluxBalanceAnalysis solver = new FluxBalanceAnalysis(document);
    boolean solved = false;
    try {
      solved = solver.solve();
    } catch (RuntimeException exc) {
      logger.severe("Error while solving FBA model for FROG report: " + exc.getMessage());
    }

    String status = solved ? "optimal" : "infeasible";
    double objectiveValue = solved ? solver.getObjectiveValue() : 0.0;
    Map<String, Double> fluxes = solved ? solver.getSolution() : null;

    // metadata fields
    String frogId = "sbscl-" + UUID.randomUUID();

    String sbsclVersion = FrogReport.class.getPackage() != null
        ? FrogReport.class.getPackage().getImplementationVersion()
        : null;
    if (sbsclVersion == null) {
      sbsclVersion = "unknown";
    }

    String os = System.getProperty("os.name", "unknown") + " "
        + System.getProperty("os.arch", "");

    // detect LP solver name
    LinearProgramSolver lpSolver = solver.getLinearProgramSolver();
    String solverName = (lpSolver != null) ? lpSolver.getClass().getSimpleName() : "unknown";

    // Build JSON using org.json
    JSONObject frog = new JSONObject();

    // metadata
    JSONObject metadata = new JSONObject();
    metadata.put("model.location", modelLocation != null ? modelLocation : modelId);
    metadata.put("model.md5", modelMd5 != null ? modelMd5 : JSONObject.NULL);
    metadata.put("frog_id", frogId);

    JSONObject frogSoftware = new JSONObject()
        .put("name", "SBSCL FROG")
        .put("version", sbsclVersion)
        .put("url", "https://github.com/draeger-lab/SBSCL");
    metadata.put("frog.software", frogSoftware);

    JSONArray curators = new JSONArray();
    curators.put(new JSONObject()
        .put("familyName", "SBSCL")
        .put("givenName", "Team")
        .put("email", JSONObject.NULL)
        .put("organization", "SBSCL")
        .put("site", JSONObject.NULL)
        .put("orcid", JSONObject.NULL));
    metadata.put("frog.curators", curators);

    JSONObject software = new JSONObject()
        .put("name", "SBSCL FluxBalanceAnalysis")
        .put("version", sbsclVersion)
        .put("url", "https://github.com/draeger-lab/SBSCL");
    metadata.put("software", software);

    JSONObject solverJson = new JSONObject()
        .put("name", solverName)
        .put("version", "unknown")
        .put("url", "https://github.com/optimatika/scpsolver");
    metadata.put("solver", solverJson);

    metadata.put("environment", os.trim());
    frog.put("metadata", metadata);

    // objectives
    JSONArray objectivesArray = new JSONArray();
    JSONObject objective = new JSONObject()
        .put("model", modelId)
        .put("objective", solver.getActiveObjective())
        .put("status", status)
        .put("value", objectiveValue);
    objectivesArray.put(objective);
    frog.put("objectives", new JSONObject().put("objectives", objectivesArray));

    // fva – currently empty placeholder
    frog.put("fva", new JSONObject().put("fva", new JSONArray()));

    // reaction deletions – placeholder
    frog.put("reaction_deletions", new JSONObject().put("deletions", new JSONArray()));

    // gene deletions – placeholder
    frog.put("gene_deletions", new JSONObject().put("deletions", new JSONArray()));

    // If needed later, fluxes could be used to populate FVA-like entries

    // Write JSON file
    if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
      if (!outputFile.getParentFile().mkdirs()) {
        logger.warning("Could not create directories for output file: " + outputFile);
      }
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      writer.write(frog.toString(2)); // pretty-printed with indentation
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
}