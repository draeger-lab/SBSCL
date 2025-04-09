package org.simulator.math.odes.exception;

public class UnsupportedMethodException extends RuntimeException {
    public UnsupportedMethodException(String method) {
        super(String.format("Unsupported method %s, supported methods are [RK2, RK4].", method));
    }
}
