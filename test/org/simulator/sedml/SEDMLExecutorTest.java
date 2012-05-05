/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simulator.math.odes.MultiTable;

/**
 * This test class shows how a SED-ML file can be interpreted and executed using 
 *  SBML Simulator Core solvers. <br/>
 *  It makes extensive use of jlibsedml's Execution framework which performs boiler-plate
 *   code for operations such as post-processing of results, etc., 
 * @author Richard Adams
 * @version $Rev$
 * @since 1.1
 */
public class SEDMLExecutorTest {
	/*
	 * This file describes a straightforward simulation of a basic model a->b->c
	 */
	File abc1test = new File("files/sedmlTest/sed1.xml");
	File miriamtest = new File("files/sedmlTest/ClockSedML.xml");
	SedML sedml=null;
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testBasicSEDMLExecutorForLocalFile() throws XMLException {
		// get the SED-ML object model from file. The model referred to in this 
		//SEDML file is defined by a relative path and is in the top-level folder.
		SEDMLDocument doc = Libsedml.readDocument(abc1test);
		// check no errors in SEDML file, else simulation will not work so well.
		assertFalse(doc.hasErrors());
		sedml=doc.getSedMLModel();
		// in this sedml file there's just one output. If there were several,
		// we could either iterate or get user to  decide which output to generate.
		Output wanted = sedml.getOutputs().get(0);
		SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml,wanted);
		
		// Here we run all the simulations needed to create an output, and get the 
		// raw results.
		Map<Task, IRawSedmlSimulationResults>res = exe.runSimulations();
		if(res==null ||res.isEmpty() || !exe.isExecuted()){
			 fail ("Simulatation failed: " + exe.getFailureMessages().get(0).getMessage());
		}
		for(IRawSedmlSimulationResults re: res.values()){
			assertTrue(re instanceof MultTableSEDMLWrapper);
		}
			
			 MultiTable mt = exe.processSimulationResults(wanted, res);
			assertTrue( 5== mt.getColumnCount());
			assertTrue ("Num rows was: "  + mt.getRowCount(), 101 == mt.getRowCount());
			assertEquals("Time", mt.getTimeName());
			assertEquals(1,mt.getBlock(0).getColumn(0).getValue(0),0.001);
			assertEquals("A_dg",mt.getBlock(0).getColumn(0).getColumnName());
		
	}
	// retrieves model from Miriam DB - needs internet connection
	@Test
	public final void testBasicSEDMLExecutorForMiriamURNDefinedModel() throws XMLException {
		sedml=Libsedml.readDocument(miriamtest).getSedMLModel();
		// in this SED-ML file there's just one output. If there were several,
		// we could either iterate or get user to  decide what they want to run.
		Output wanted = sedml.getOutputs().get(0);
		SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml,wanted);
		// This gets the raw simulation results - one for each Task that was run.
		Map<Task, IRawSedmlSimulationResults>res = exe.runSimulations();
		if(res==null ||res.isEmpty() || !exe.isExecuted()){
			 fail ("Simulatation failed: " + exe.getFailureMessages().get(0));
		}
		      // now process.In this case, there's no processing performed - we're displaying the
		     // raw results.
			 MultiTable mt = exe.processSimulationResults(wanted, res);
			assertTrue( 3== mt.getColumnCount());
			assertTrue ("Num Rows was: "  + mt.getRowCount(), 1001 == mt.getRowCount());
			assertEquals("Time", mt.getTimeName());
	}

	

}
