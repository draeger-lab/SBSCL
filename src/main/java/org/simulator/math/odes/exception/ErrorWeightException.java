package org.simulator.math.odes.exception;

public class ErrorWeightException extends RuntimeException{
    public ErrorWeightException(int i, Number value) {
        super(String.format("[lsoda] ewt[" + i + "] = " + value + " <= 0."));
    }
}
