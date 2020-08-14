package org.simulator.sbml;

import org.apache.commons.math.ode.DerivativeException;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbml.jsbml.*;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.TestUtils;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class SimulatorTest {

  /**
   * Testing time course simulation with RosenbrockSolver.
   *
   * @throws XMLStreamException
   * @throws IOException
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   * @throws DerivativeException
   */
  @Test
  public void testTimeCourseSimulation() throws XMLStreamException,
      IOException, ModelOverdeterminedException, SBMLException,
      DerivativeException {

    // read the model
    String sbmlPath = TestUtils.getPathForTestResource("/sbml/BIOMD0000000012.xml");
    SBMLDocument doc = JSBML.readSBML(sbmlPath);
    assertNotNull(doc);
    Model model = doc.getModel();
    assertNotNull(model);

    // Configuration
    double stepSize = 1.0;
    double timeEnd = 100.0;

    // initialize solver
    AbstractDESSolver solver = new RosenbrockSolver();
    solver.setStepSize(stepSize);
    SBMLinterpreter interpreter = new SBMLinterpreter(model);
    if (solver instanceof AbstractDESSolver) {
      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
    }

    // Compute the numerical solution of the initial value problem
    // TODO: Rel-Tolerance, Abs-Tolerance.
    MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), 0d, timeEnd);

    // Uncomment this for steadyState simulation
    // If SteadyState simulation is desired, simple call solver without end time.
    // Pass number of steps. The output will contains that many steps, in this case 10 steps.

    // MultiTable solution = solver.steadystate(interpreter, interpreter.getInitialValues(), 10d);

    assertNotNull(solution);
    assertEquals(101, solution.getRowCount());
  }

  /**
   * Testing steady state simulation.
   *
   * @throws XMLStreamException
   * @throws IOException
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   * @throws DerivativeException
   */
  @Test
  public void testSteadyStateSimulation() throws XMLStreamException,
      IOException, ModelOverdeterminedException, SBMLException,
      DerivativeException {

        /* Model is simple reaction of:
            J0: S1 -> S2; k1*S1;
            S1 = 10.0; S2 = 0.0; k1 = 1.0;
           which will reach steady state: S1=0.0; S2=10.0.
         */

    // read the model
    String sbmlPath = TestUtils.getPathForTestResource("/sbml/steadystate/steadystate.xml");
    SBMLDocument doc = JSBML.readSBML(sbmlPath);
    assertNotNull(doc);
    Model model = doc.getModel();
    assertNotNull(model);

    // initialize solver
    double stepSize = 1.0;
    AbstractDESSolver solver = new RosenbrockSolver();
    solver.setStepSize(stepSize);
    SBMLinterpreter interpreter = new SBMLinterpreter(model);
    if (solver instanceof AbstractDESSolver) {
      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
    }

    // Compute the steady state
    MultiTable solution = solver.steadystate(interpreter, interpreter.getInitialValues(), 100);

    assertNotNull(solution);
    assertEquals(1, solution.getRowCount());

    MultiTable.Block.Column S1 = solution.getColumn("S1");
    assertEquals(0.0, S1.getValue(0), 1E-6);

    MultiTable.Block.Column S2 = solution.getColumn("S2");
    assertEquals(10.0, S2.getValue(0), 1E-6);
  }

}
