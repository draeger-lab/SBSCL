package org.simulator.math.odes.exception;

public class IntdyException extends RuntimeException{
    public IntdyException(int itask, double tout) {
        super(String.format("[lsoda] trouble from intdy, itask = %d, tout = %g", itask, tout));
    }
}
