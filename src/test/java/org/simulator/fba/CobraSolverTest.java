package org.simulator.fba;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CobraSolverTest {
    private static final Logger logger = LoggerFactory.getLogger(CobraSolverTest.class);
	private double eps = 1E-4;
	private double COBRA_OBJ_VAL = 0.8739215069684307;

    @Test
    @Ignore
    public void solveEColiCore() throws ModelOverdeterminedException, XMLStreamException {

        String resourceName = "fba/e_coli_core.xml";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(resourceName);
        SBMLDocument doc = SBMLReader.read(is);
        assertNotNull(doc);
        logger.info(doc.toString());

        COBRAsolver solver = new COBRAsolver(doc);
        
        // Solver should return non-null object
        assertNotNull(solver.solve());
        
        // Objective value should math CobraPy answer with some tolerance
        assertEquals(COBRA_OBJ_VAL, solver.getObjetiveValue(), eps);
    }
}
