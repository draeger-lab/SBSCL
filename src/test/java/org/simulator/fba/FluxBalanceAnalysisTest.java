package org.simulator.fba;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import org.simulator.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FluxBalanceAnalysisTest {

    private static final Logger logger = LoggerFactory.getLogger(FluxBalanceAnalysisTest.class);
	  private double EPS = 1E-4;
	  private double ACTIVE_OBJECTIVE_VALUE = 0.8739215069684307;

    @Test
    //@Ignore
    public void solveEColiCore() throws ModelOverdeterminedException, XMLStreamException, IOException {
        String path = TestUtils.getPathForTestResource("/fba/e_coli_core.xml");
        SBMLDocument doc = JSBML.readSBML(path);
        assertNotNull(doc);
        logger.info(doc.toString());

        FluxBalanceAnalysis solver = new FluxBalanceAnalysis(doc);

        // Solver should return non-null object
        assertNotNull(solver.solve());

        // Objective value should math CobraPy answer with some tolerance
        assertEquals(ACTIVE_OBJECTIVE_VALUE, solver.getObjectiveValue(), EPS);
    }

    @Test
    @Ignore
    public void solveEColiCoreGZ() throws ModelOverdeterminedException, XMLStreamException, IOException {
        String path = TestUtils.getPathForTestResource("/fba/e_coli_core.xml.gz");

        // read SBML
        InputStream is = new FileInputStream(path);
        GZIPInputStream gzis = new GZIPInputStream(is);

        SBMLDocument doc = SBMLReader.read(gzis);
        assertNotNull(doc);
        logger.info(doc.toString());

        FluxBalanceAnalysis solver = new FluxBalanceAnalysis(doc);

        // Solver should return non-null object
        assertNotNull(solver.solve());

        // Objective value should math CobraPy answer with some tolerance
        assertEquals(ACTIVE_OBJECTIVE_VALUE, solver.getObjectiveValue(), EPS);
        gzis.close();
        is.close();
    }

}
