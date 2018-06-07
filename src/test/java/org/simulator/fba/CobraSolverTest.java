package org.simulator.fba;

import static org.junit.Assert.*;


import java.io.IOException;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.simulator.TestUtils;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CobraSolverTest {
    private static final Logger logger = LoggerFactory.getLogger(CobraSolverTest.class);


    @Test
    public void test1() {
        assertTrue(true);
    }

    @Test
    public void solveEColiCore() throws ModelOverdeterminedException, IOException, XMLStreamException {

        String path = TestUtils.FBA_RESOURCE_PATH + "/e_coli_core.xml";
        SBMLDocument doc = JSBML.readSBML(path);
        COBRAsolver solver = new COBRAsolver(doc);
        if (solver.solve()) {
            System.out.println(path);
            System.out.println("Objective value:\t" + solver.getObjetiveValue());
            System.out.println("Fluxes:\t" + Arrays.toString(solver.getValues()));
        } else {
            logger.error("\nSolver returned null for " + path);
        }
    }
}
