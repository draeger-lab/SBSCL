package org.simulator.optsolvx;

import org.junit.Test;
import static org.junit.Assert.*;
import org.optsolvx.model.*;
import org.optsolvx.solver.*;

import java.util.HashMap;
import java.util.Map;

public class OptSolvXSolverAdapterTest {

    @Test(expected = IllegalStateException.class)
    public void solve_requires_build() {
        AbstractLPModel m = new AbstractLPModel();
        m.addVariable("x", 0.0d, 10.0d);
        LPSolverAdapter s = new OptSolvXSolverAdapter(new CommonsMathSolver(), false);
        s.solve(m); // not built -> must throw
    }

    @Test
    public void solves_simple_lp() {
        AbstractLPModel m = new AbstractLPModel();
        m.addVariable("x", 0.0d, Double.POSITIVE_INFINITY);
        m.addVariable("y", 0.0d, Double.POSITIVE_INFINITY);

        Map<String, Double> obj = new HashMap<>();
        obj.put("x", 1.0d); obj.put("y", 1.0d);
        m.setObjective(obj, OptimizationDirection.MAXIMIZE);

        Map<String, Double> c1 = new HashMap<>();
        c1.put("x", 1.0d); c1.put("y", 2.0d);
        m.addConstraint("c1", c1, Constraint.Relation.LEQ, 4.0d);

        Map<String, Double> c2 = new HashMap<>();
        c2.put("x", 1.0d);
        m.addConstraint("c2", c2, Constraint.Relation.LEQ, 3.0d);

        m.build();

        LPSolverAdapter s = new OptSolvXSolverAdapter(new CommonsMathSolver(), false);
        LPSolution sol = s.solve(m);

        assertTrue(sol.isFeasible());
        assertEquals(3.5d, sol.getObjectiveValue(), 1e-6d);
        assertEquals(3.0d, sol.getVariableValues().get("x"), 1e-6d);
        assertEquals(0.5d, sol.getVariableValues().get("y"), 1e-6d);
    }
}
