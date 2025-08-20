package org.simulator.optsolvx;

import org.junit.Test;
import static org.junit.Assert.*;
import org.optsolvx.model.*;
import org.optsolvx.solver.*;

import java.util.HashMap;
import java.util.Map;

public class OptSolvXSolverAdapterTest {

    @Test(expected = IllegalArgumentException.class)
    public void solve_requires_non_null_model() {
        LPSolverAdapter s = new OptSolvXSolverAdapter(new CommonsMathSolver(), false);
        s.solve(null); // must throw
    }

    @Test(expected = IllegalStateException.class)
    public void solve_requires_build() {
        AbstractLPModel m = new AbstractLPModel();
        m.addVariable("x", 0.0d, 10.0d);
        LPSolverAdapter s = new OptSolvXSolverAdapter(new CommonsMathSolver(), false);
        s.solve(m); // not built -> must throw
    }

    @Test
    public void smoke_minimize_with_eq_and_bounds() {
        // Minimize 2x + y, s.t. x + y = 5, 0 <= x,y <= 5
        AbstractLPModel m = new AbstractLPModel();
        m.addVariable("x", 0.0d, 5.0d);
        m.addVariable("y", 0.0d, 5.0d);

        Map<String, Double> obj = new HashMap<>();
        obj.put("x", 2.0d);
        obj.put("y", 1.0d);
        m.setObjective(obj, OptimizationDirection.MINIMIZE);

        Map<String, Double> eq = new HashMap<>();
        eq.put("x", 1.0d);
        eq.put("y", 1.0d);
        m.addConstraint("sum", eq, Constraint.Relation.EQ, 5.0d);

        m.build();

        LPSolverAdapter s = new OptSolvXSolverAdapter(new CommonsMathSolver(), false);
        LPSolution sol = s.solve(m);

        assertTrue(sol.isFeasible());
        // Optimum at x=0, y=5 -> objective = 5
        assertEquals(5.0d, sol.getObjectiveValue(), 1e-6d);
        assertEquals(0.0d, sol.getVariableValues().get("x"), 1e-6d);
        assertEquals(5.0d, sol.getVariableValues().get("y"), 1e-6d);
    }
}
