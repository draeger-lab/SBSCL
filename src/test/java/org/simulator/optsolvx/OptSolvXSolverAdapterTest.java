package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;
import org.optsolvx.model.OptimizationDirection;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;
import org.optsolvx.solver.OptSolvXConfig;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OptSolvXSolverAdapterTest {

    private static final double TOLERANCE = 1e-6d;

    private static final LPSolverAdapter NOOP_BACKEND =
            new LPSolverAdapter() {
                @Override
                public LPSolution solve(AbstractLPModel model) {
                    throw new AssertionError(
                            "Backend must not be called for these tests.");
                }
            };

    @Test
    public void solve_requires_non_null_model() {
        LPSolverAdapter solver =
                new OptSolvXSolverAdapter(NOOP_BACKEND, false);

        assertThrows(IllegalArgumentException.class,
                () -> solver.solve(null));
    }

    @Test
    public void solve_requires_build() {
        AbstractLPModel model = new AbstractLPModel();
        model.addVariable("x", 0.0d, 10.0d);

        LPSolverAdapter solver =
                new OptSolvXSolverAdapter(NOOP_BACKEND, false);

        assertThrows(IllegalStateException.class,
                () -> solver.solve(model));
    }

    @ParameterizedTest
    @CsvSource({
            "commons-math, CommonsMathSolver",
            "ojalgo, OjAlgoSolver"
    })
    public void smoke_minimize_with_eq_and_bounds(
            String solver, String expectedBackend) {

        // Minimize 2x + y subject to x + y = 5.
        AbstractLPModel model = new AbstractLPModel();
        model.addVariable("x", 0.0d, 5.0d);
        model.addVariable("y", 0.0d, 5.0d);

        Map<String, Double> objective = new HashMap<>();
        objective.put("x", 2.0d);
        objective.put("y", 1.0d);

        model.setObjective(objective, OptimizationDirection.MINIMIZE);

        Map<String, Double> coefficients = new HashMap<>();
        coefficients.put("x", 1.0d);
        coefficients.put("y", 1.0d);

        model.addConstraint(
                "sum", coefficients, Constraint.Relation.EQ, 5.0d);

        model.build();

        LPSolverAdapter backend =
                OptSolvXConfig.resolve(model, solver);

        assertNotNull(backend);
        assertEquals(expectedBackend, backend.getClass().getSimpleName());

        LPSolverAdapter adapter =
                new OptSolvXSolverAdapter(backend, false);

        LPSolution solution = adapter.solve(model);

        assertTrue(solution.isFeasible());

        // Expected optimum: x = 0, y = 5, objective = 5.
        assertEquals(5.0d, solution.getObjectiveValue(), TOLERANCE);
        assertEquals(0.0d, solution.getVariableValues().get("x"), TOLERANCE);
        assertEquals(5.0d, solution.getVariableValues().get("y"), TOLERANCE);
    }
}