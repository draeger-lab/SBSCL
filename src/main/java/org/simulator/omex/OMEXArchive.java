/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
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
package org.simulator.omex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.JDOMException;
import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;

/**
 * @author Shalin
 * @since 1.5
 */
public class OMEXArchive {

  private CombineArchive archive;
  private Map<String, ArchiveEntry> entryMap;
  private boolean has_models;
  private boolean has_sim_descp;
  private File sed_ml, sb_ml;

  public OMEXArchive(File zipFile)
      throws IOException, ParseException, CombineArchiveException,
      JDOMException {
    entryMap = new HashMap<String, ArchiveEntry>();
    has_models = false;
    has_sim_descp = false;
    archive = new CombineArchive(zipFile);
    File parent = new File(System.getProperty("java.io.tmpdir"));
    String entryFormat;

    // iterate over all entries in the archive and create a Map
    for (ArchiveEntry entry : archive.getEntries()) {
      entryMap.put(entry.getFilePath(), entry);
      entryFormat = entry.getFormat().toString();
      if (entryFormat.contains("SBML") || entryFormat.contains("sbml")) {
        has_models = true;
        File sb_ml_file = new File(parent, entry.getFileName());
        sb_ml = entry.extractFile(sb_ml_file);
      }
      if (entryFormat.contains("SED-ML") || entryFormat.contains("sed-ml")) {
        has_sim_descp = true;
        File sed_ml_file = new File(parent, entry.getFileName());
        sed_ml = entry.extractFile(sed_ml_file);
      }
    }

    // read description of the archive itself
    LOGGER.debug("found " + archive.getDescriptions().size() + " meta data entries describing the archive.");
  }

  public Map<String, ArchiveEntry> getFileEntries() {
    return entryMap;
  }

  public boolean containsSBMLModel() {
    return has_models;
  }

  public boolean containsSEDMLDescp() {
    return has_sim_descp;
  }

  public File getSEDMLDescription() {
    return sed_ml;
  }

  /**
   * A simple method to uncompress combine archives at desired location
   *
   * @param destination
   * @return whether combine archive is uncompressed or not
   */
  public boolean extractArchive(File destination) throws IOException {
    try {
      archive.extractTo(destination);
      return true;
    } catch (IOException ex) {
      LOGGER.error(ex, "Error in extracting archives!");
      return false;
    } finally {
      archive.close();
    }
  }

}
