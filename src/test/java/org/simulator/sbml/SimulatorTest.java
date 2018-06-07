package org.simulator.sbml;

import org.apache.commons.math.ode.DerivativeException;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

public class SimulatorTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimulation() throws XMLStreamException,
            IOException, ModelOverdeterminedException, SBMLException,
            DerivativeException {

        // Configuration
        double stepSize = 1.0;
        double timeEnd = 100.0;

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("sbml/BIOMD0000000012.xml");

        // Read the model and initialize solver

        SBMLDocument doc = SBMLReader.read(is);
        assertNotNull(doc);

        Model model = doc.getModel();
        assertNotNull(model);

        DESSolver solver = new RosenbrockSolver();
        solver.setStepSize(stepSize);
        SBMLinterpreter interpreter = new SBMLinterpreter(model);
        if (solver instanceof AbstractDESSolver) {
            ((AbstractDESSolver) solver).setIncludeIntermediates(false);
        }

        // Compute the numerical solution of the initial value problem
        // TODO: Rel-Tolerance, Abs-Tolerance.
        MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), 0d, timeEnd);

        assertNotNull(solution);
        assertEquals(101, solution.getRowCount());
    }
}
