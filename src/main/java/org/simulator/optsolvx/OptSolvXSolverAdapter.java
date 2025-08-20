package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;

import java.util.Objects;
import java.text.MessageFormat;
import java.util.logging.Logger;

public final class OptSolvXSolverAdapter implements LPSolverAdapter {
    private static final Logger LOGGER =
            Logger.getLogger(OptSolvXSolverAdapter.class.getName());

    private final LPSolverAdapter backend;
    private final boolean debug;

    /**
     * @param backend concrete OptSolvX solver (e.g. CommonsMathSolver)
     * @param debug enable verbose logging
     */
    public OptSolvXSolverAdapter(LPSolverAdapter backend, boolean debug) {
        this.backend = Objects.requireNonNull(backend, "backend must not be null");
        this.debug = debug;
    }

    /** Convenience: debug disabled by default. */
    public OptSolvXSolverAdapter(LPSolverAdapter backend) {
        this(backend, false);
    }

    @Override
    public LPSolution solve(AbstractLPModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
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

        long t0 = System.nanoTime();
        LPSolution sol = backend.solve(model);
        long dtMs = (System.nanoTime() - t0) / 1_000_000L;

        if (debug) {
            LOGGER.info(MessageFormat.format(
                    "{0}: result feasible={1}, objective={2}, time={3} ms",
                    getClass().getSimpleName(),
                    sol.isFeasible(),
                    sol.getObjectiveValue(),
                    dtMs
            ));
        }
        return sol;
    }
}
