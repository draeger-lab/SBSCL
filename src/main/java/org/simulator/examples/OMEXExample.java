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
package org.simulator.examples;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.simulator.omex.OMEXArchive;
import org.simulator.sedml.MultTableSEDMLWrapper;
import org.simulator.sedml.SedMLSBMLSimulatorExecutor;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.CombineArchiveException;

/**
 * @author Shalin
 * @since 1.5
 */
public class OMEXExample {

  public static void main(String[] args)
      throws IOException, ParseException, CombineArchiveException,
      JDOMException, XMLException, OWLOntologyCreationException {
    String file = args[0];
    if (file.isEmpty()) {
      LOGGER.warn("Please enter a valid omex file as argument.");
      return;
    }

    OMEXArchive archive = new OMEXArchive(new File(file));

    if (archive.containsSBMLModel() && archive.containsSEDMLDescp()) {
      // Execute SED-ML file and run simulations
      SEDMLDocument doc = Libsedml.readDocument(archive.getSEDMLDescription());
      SedML sedml = doc.getSedMLModel();

      Output wanted = sedml.getOutputs().get(0);
      SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted,
          archive.getSEDMLDescription().getParentFile().getAbsolutePath());

      Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
      if ((res == null) || res.isEmpty() || !exe.isExecuted()) {
        fail("Simulation failed: " + exe.getFailureMessages().get(0).getMessage());
      }

      for (List<IRawSedmlSimulationResults> re_list : res.values()) {
        for (IRawSedmlSimulationResults re : re_list) {
          assertTrue(re instanceof MultTableSEDMLWrapper);
        }
      }
    }

  }
}
