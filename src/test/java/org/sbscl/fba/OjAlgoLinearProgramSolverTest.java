package org.sbscl.fba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.simulator.fba.OjAlgoLinearProgramSolver;

import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.problems.LinearProgram;

/**
 * Basic tests for the pure Java {@link OjAlgoLinearProgramSolver}.
 */
public class OjAlgoLinearProgramSolverTest {

  @Test
  public void testSimpleMaximisation() {
    // Maximise 10*x0 + 6*x1 + 4*x2
    // subject to x0 + x1 + x2 <= 100, x >= 0
    LinearProgram lp = new LinearProgram(new double[] {10.0, 6.0, 4.0});

    lp.addConstraint(new LinearSmallerThanEqualsConstraint(
        new double[] {1.0, 1.0, 1.0},
        100.0,
        "c1"));

    lp.setLowerbound(new double[] {0.0, 0.0, 0.0});
    lp.setUpperbound(new double[] {
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        Double.MAX_VALUE
    });

    OjAlgoLinearProgramSolver solver = new OjAlgoLinearProgramSolver();
    double[] solution = solver.solve(lp);

    assertNotNull(solution, "Solver should return a solution array");
    assertEquals(3, solution.length, "Solution dimension must match number of variables");

    double objective = lp.evaluate(solution);

    // Best is to put all 100 units into x0:
    // 10*100 = 1000, with x1 = x2 = 0
    assertEquals(1000.0, objective, 1e-6, "Objective value should be optimal");
  }
}