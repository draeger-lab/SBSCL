package org.simulator.math.odes.exception;

import java.util.Arrays;
import org.simulator.math.odes.RungeKutta_EventSolver.validMethod;

public class UnsupportedMethodException extends RuntimeException {
    public UnsupportedMethodException(String method) {
        super(String.format("Unsupported method %s. Supported methods are: %s", method, Arrays.toString(validMethod.values())));
    }
}
