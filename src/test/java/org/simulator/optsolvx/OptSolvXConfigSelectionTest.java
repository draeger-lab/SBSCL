package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolverAdapter;
import org.optsolvx.solver.OptSolvXConfig;

import static org.junit.jupiter.api.Assertions.*;

public class OptSolvXConfigSelectionTest {

    @Test
    public void unknown_solver_name_throws_exception() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        assertThrows(IllegalArgumentException.class,
                () -> OptSolvXConfig.resolve(lp, "does-not-exist"));
    }

    @Test
    public void resolves_oj_alias_to_ojalgo() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        LPSolverAdapter backend = OptSolvXConfig.resolve(lp, "oj");

        assertNotNull(backend);
        assertEquals("OjAlgoSolver", backend.getClass().getSimpleName());
    }

    @Test
    public void resolves_explicit_commons_math_backend() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        LPSolverAdapter backend =
                OptSolvXConfig.resolve(lp, "commons-math");

        assertNotNull(backend);
        assertEquals("CommonsMathSolver",
                backend.getClass().getSimpleName());
    }
}