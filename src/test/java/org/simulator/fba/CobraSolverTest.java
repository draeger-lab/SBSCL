package org.simulator.fba;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CobraSolverTest {
    private static final Logger logger = LoggerFactory.getLogger(CobraSolverTest.class);

    @Test
    @Ignore  // breaks on Ubuntu: https://github.com/shalinshah1993/SBSCL/issues/24
    public void solveEColiCore() throws ModelOverdeterminedException, XMLStreamException {

        String resourceName = "fba/e_coli_core.xml";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(resourceName);
        SBMLDocument doc = SBMLReader.read(is);
        logger.info(doc.toString());

        COBRAsolver solver = new COBRAsolver(doc);
        if (solver.solve()) {
            System.out.println(resourceName);
            System.out.println("Objective value:\t" + solver.getObjetiveValue());
            System.out.println("Fluxes:\t" + Arrays.toString(solver.getValues()));
        } else {
            logger.error("\nSolver returned null for " + resourceName);
        }
    }
}
