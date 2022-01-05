/*
 * $Id$
 * $URL$
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
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sedml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.jlibsedml.Libsedml;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.XMLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Tests the SEDMLWriter
 *
 * @author Richard Adams, Matthias König
 * @version $Rev$
 * @since 1.1
 */
public class SEDMLWriterTest {

  private static final String COMMENT = "Standard time course of ABC1 model";
  private File SBMLFile = new File(
      Thread.currentThread().getContextClassLoader().getResource("sedml/abc_1.xml").getFile());


  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * @throws IOException
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   * @throws XMLException
   */
  @Test
  public final void testSaveExperimentToSEDML()
      throws IOException, XMLStreamException, SBMLException, ModelOverdeterminedException, XMLException {

    double start = 0d;
    double end = 10d;
    double stepsize = 0.1d;
    Model model = (new SBMLReader()).readSBML(SBMLFile).getModel();
    AbstractDESSolver solver = new DormandPrince54Solver();
    solver.setStepSize(stepsize);
    SBMLinterpreter interpreter = new SBMLinterpreter(model);

    if (solver instanceof AbstractDESSolver) {
      solver.setIncludeIntermediates(false);
    }

    SEDMLWriter writer = new SEDMLWriter();
    writer.setComment(COMMENT);

    File tmp = File.createTempFile("sedmlOut", "xml");
    FileOutputStream fos = new FileOutputStream(tmp);
    writer.saveExperimentToSEDML(start, end, stepsize, solver, model, SBMLFile.toURI(), fos);

    // now test reading in SEDML file
    SEDMLDocument doc = Libsedml.readDocument(tmp);
    //assertFalse(doc.hasErrors());
    // time, A, B,C
    assertEquals(4, doc.getSedMLModel().getDataGenerators().size());

    // check comment added
    //assertEquals(COMMENT, doc.getSedMLModel().getNotes().get(0).getNotesElement().getText());

    // save will fail if OutputStream is unavailable
    fos.close();
    boolean IoExthrown = false;
    try {
      writer.saveExperimentToSEDML(start, end, stepsize, solver, model, SBMLFile.toURI(), fos);
    } catch (IOException e) {
      IoExthrown = true;
    }
    if (!IoExthrown) {
      fail("Should throw IOEXception since OS was closed");
    }

  }

}
