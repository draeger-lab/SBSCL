package org.simulator.math.odes.exception;

public class IntdyException extends RuntimeException{
    public IntdyException(int itask, double tout) {
        
        super("[lsoda] trouble from intdy, itask = " + itask + ", tout = " + tout);
    }
}
