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
 * 8. Duke University, USA
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.plot;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jfree.ui.RefineryUtilities;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Curve;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jlibsedml.execution.IRawSedmlSimulationResults;

import org.simulator.TestUtils;
import org.simulator.sedml.SedMLSBMLSimulatorExecutor;

/**
 * This test class shows how a SED-ML file can be interpreted and executed using
 * SBML Simulator Core solvers. <br/>
 * It makes extensive use of jlibsedml's Execution framework which performs boiler-plate
 * code for operations such as post-processing of results, etc., Finally plots 
 * are generated and saved in the folder
 *
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.5
 */
public class SedmlOutputPlotTest {

    private String abc1test = "/sedml/sed1.xml";
    private String miriamtest = "/sedml/ClockSedML.xml";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testBasicSEDMLExecutorForLocalFile() throws XMLException, IOException, OWLOntologyCreationException {
        // get the SED-ML object model from file. The model referred to in this
        //SEDML file is defined by a relative path and is in the top-level folder.

        String sedmlPath = TestUtils.getPathForTestResource(abc1test);
        File file = new File(sedmlPath);
        String sedmlDir = file.getAbsoluteFile().getParentFile().getAbsolutePath();

        SEDMLDocument doc = Libsedml.readDocument(new File(sedmlPath));
        assertNotNull(doc);
        assertFalse(doc.hasErrors());

        SedML sedml = doc.getSedMLModel();
        // in this sedml file there's just one output. If there were several,
        // we could either iterate or get user to  decide which output to generate.
        Output wanted = sedml.getOutputs().get(0);
        SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, sedmlDir);

        // Here we run all the simulations needed to create an output, and get the
        // raw results.
        Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
        if ((res == null) || res.isEmpty() || !exe.isExecuted()) {
            fail("Simulation failed: " + exe.getFailureMessages().get(0).getMessage());
        }

        IProcessedSedMLSimulationResults mt = exe.processSimulationResults(wanted, res);
        assertNotNull(mt);
        assertTrue(5 == mt.getNumColumns());
    }

    /**
     * Retrieves model from Miriam DB - needs internet connection
     *
     * @throws XMLException
     * @throws OWLOntologyCreationException 
     */
    @Test
    public final void testBasicSEDMLExecutorForMiriamURNDefinedModel() throws XMLException, IOException, OWLOntologyCreationException {

        String miriamPath = TestUtils.getPathForTestResource(miriamtest);
        SEDMLDocument doc = Libsedml.readDocument(new File(miriamPath));
        assertNotNull(doc);

        SedML sedml = doc.getSedMLModel();
        assertNotNull(sedml);

        // in this SED-ML file there's just one output. If there were several,
        // we could either iterate or get user to  decide what they want to run.
        Output wanted = sedml.getOutputs().get(0);
        SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, null);

        // This gets the raw simulation results - one for each Task that was run.
        Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
        if (res == null || res.isEmpty() || !exe.isExecuted()) {
            fail("Simulation failed: " + exe.getFailureMessages().get(0));
        }
        // now process.In this case, there's no processing performed - we're displaying the
        // raw results.
        IProcessedSedMLSimulationResults mt = exe.processSimulationResults(wanted, res);
        assertNotNull(mt);

        assertTrue(3 == mt.getNumColumns());
    }


    @Test
    public final void testIkappab() throws XMLException, OWLOntologyCreationException, IOException{
        String resource = "/sedml/L1V2/ikappab/ikappab.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testLeloupSBML() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/leloup-sbml/leloup-sbml.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testLorenzSBML() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/lorenz-sbml/lorenz.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testOscliNestedPulse() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/oscli-nested-pulse/oscli-nested-pulse.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testParameterScan2D() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/parameter-scan-2d/parameter-scan-2d.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testRepeatedScanOscli() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/repeated-scan-oscli/repeated-scan-oscli.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testRepeatedSteadyScanOscli() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/repeated-steady-scan-oscli/repeated-steady-scan-oscli.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testRepeatedStochasticRuns() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/repeated-stochastic-runs/repeated-stochastic-runs.xml";
        testSpecificationExample(resource);
    }

    @Test
    public final void testRepressilator() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/repressilator/repressilator.xml";
        testSpecificationExample(resource);
    }


    public void testSpecificationExample(String resource) throws XMLException, OWLOntologyCreationException, IOException {
        String sedmlPath = TestUtils.getPathForTestResource(resource);

        File file = new File(sedmlPath);
        String sedmlDir = file.getAbsoluteFile().getParentFile().getAbsolutePath();
        SEDMLDocument doc = Libsedml.readDocument(file);
        assertNotNull(doc);

        SedML sedml = doc.getSedMLModel();
        assertNotNull(sedml);

        // iterate over outputs
        List<Output> outputs = sedml.getOutputs();
        for (int k=0; k<outputs.size(); k++){

            Output wanted = outputs.get(k);
            SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, sedmlDir);

            Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
            if (res == null || res.isEmpty() || !exe.isExecuted()) {
                fail("Simulation failed: " + exe.getFailureMessages().get(0));
            }
            
            // postprocess
            IProcessedSedMLSimulationResults pr = exe.processSimulationResults(wanted, res);
            assertNotNull(pr);
            
            // save the output plot
            if(wanted.isPlot2d()) {
	    			Plot2D plots = (Plot2D) wanted;
	    			List<Curve> curves = plots.getListOfCurves();
	    			
	    			// plot all processed results as per curve descriptions
	    			String title = wanted.getId() + "(" + wanted.getName() + ")";
	    			PlotProcessedSedmlResults p = new PlotProcessedSedmlResults(pr, curves, title);
	    			p.savePlot(resource, wanted.getId());
    			}
        }
    }

    @Test
    public final void testRepressilator1() throws XMLException, OWLOntologyCreationException, IOException {
        String resource = "/sedml/L1V2/repressilator/repressilator.xml";
        testSpecificationExample(resource);
        String sedmlPath = TestUtils.getPathForTestResource(resource);
        SEDMLDocument doc = Libsedml.readDocument(new File(sedmlPath));
        assertNotNull(doc);
        SedML sedml = doc.getSedMLModel();
        assertNotNull(sedml);

        // iterate over outputs
        HashSet<String> outputIds = new HashSet<String>();
        outputIds.add("timecourse");
        outputIds.add("preprocessing");
        outputIds.add("postprocessing");

        List<Output> outputs = sedml.getOutputs();
        for (int k=0; k<outputs.size(); k++){

            Output wanted = outputs.get(k);
            SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, null);

            Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
            if (res == null || res.isEmpty() || !exe.isExecuted()) {
                fail("Simulation failed: " + exe.getFailureMessages().get(0));
            }

            // postprocess
            IProcessedSedMLSimulationResults pr = exe.processSimulationResults(wanted, res);
            assertNotNull(pr);

            String outputId = wanted.getId();
            assertTrue(outputIds.contains(outputId));

            String[] headers = pr.getColumnHeaders();
            int ncol = pr.getNumColumns();
            int nrow = pr.getNumDataRows();

            if (outputId == "timecourse"){
                assertEquals(3, ncol);
                assertEquals("plot_0__plot_0_0_0__plot_0_0_1", headers[0]);
                assertEquals("plot_0__plot_0_0_0__plot_0_1_1", headers[1]);
                assertEquals("plot_0__plot_0_0_0__plot_0_2_1", headers[2]);
            } else if (outputId == "preprocessing") {
                assertEquals(3, ncol);
                assertEquals("plot_1__plot_1_0_0__plot_1_0_1", headers[0]);
                assertEquals("plot_1__plot_1_0_0__plot_1_1_1", headers[1]);
                assertEquals("plot_1__plot_1_0_0__plot_1_2_1", headers[2]);
            } else if (outputId == "postprocessing") {
                assertEquals(3, ncol);
                assertEquals("plot_2__plot_2_0_0__plot_2_0_1", headers[0]);
                assertEquals("plot_2__plot_2_1_0__plot_2_0_0", headers[1]);
                assertEquals("plot_2__plot_2_0_1__plot_2_1_0", headers[2]);
            }
            
            // save the output plot
            if(wanted.isPlot2d()) {
	    			Plot2D plots = (Plot2D) wanted;
	    			List<Curve> curves = plots.getListOfCurves();
	    			
	    			// plot all processed results as per curve descriptions
	    			String title = wanted.getId() + "(" + wanted.getName() + ")";
	    			PlotProcessedSedmlResults p = new PlotProcessedSedmlResults(pr, curves,  title);
	    			p.savePlot(resource, wanted.getId());
    			}
        }
    }

}
