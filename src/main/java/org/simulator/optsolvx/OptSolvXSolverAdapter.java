package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class OptSolvXSolverAdapter implements LPSolverAdapter {
    private static final Logger LOGGER =
            Logger.getLogger(OptSolvXSolverAdapter.class.getName());

    private final LPSolverAdapter backend;
    private final boolean debug;

    /**
     * @param backend concrete OptSolvX solver (e.g. CommonsMathSolver)
     * @param debug enable verbose logging
     */
    public OptSolvXSolverAdapter(LPSolverAdapter backend, boolean debug) {
        this.backend = backend;
        this.debug = debug;
    }

    @Override
    public LPSolution solve(AbstractLPModel model) {
        if (!model.isBuilt()) {
            throw new IllegalStateException("Model must be built() before solving.");
        }

        if (debug) {
            LOGGER.info(MessageFormat.format(
                    "{0}: solving with {1} (vars={2}, cons={3})",
                    getClass().getSimpleName(),
                    backend.getClass().getSimpleName(),
                    model.getVariables().size(),
                    model.getConstraints().size()
            ));
        }

        LPSolution sol = backend.solve(model);

        if (debug) {
            LOGGER.info(MessageFormat.format(
                    "{0}: result feasible={1}, objective={2}",
                    getClass().getSimpleName(),
                    sol.isFeasible(),
                    sol.getObjectiveValue()
            ));
        }

        return sol;
    }
}
