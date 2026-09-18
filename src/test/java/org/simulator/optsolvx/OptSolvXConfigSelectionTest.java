package org.simulator.optsolvx;

import org.junit.Test;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolverAdapter;
import org.optsolvx.solver.OptSolvXConfig;

import static org.junit.Assert.*;

public class OptSolvXConfigSelectionTest {

    @Test(expected = IllegalArgumentException.class)
    public void unknown_solver_name_throws_exception() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        OptSolvXConfig.resolve(lp, "does-not-exist");
    }

    @Test
    public void resolves_oj_alias_to_ojalgo() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        LPSolverAdapter backend = OptSolvXConfig.resolve(lp, "oj");
        assertNotNull(backend);
        assertTrue(backend.getClass().getName().toLowerCase().contains("ojalgo"));
    }

    @Test
    public void default_solver_is_commons_math_when_no_name_given() {
        AbstractLPModel lp = new AbstractLPModel();
        lp.addVariable("x", 0.0d, 1.0d);
        lp.build();

        LPSolverAdapter backend = OptSolvXConfig.resolve(lp, null);
        assertNotNull(backend);
        assertTrue(backend.getClass().getName().toLowerCase().contains("commonsmath"));
    }
}
