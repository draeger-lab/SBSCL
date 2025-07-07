package org.simulator.math.odes.exception;

public class MaxStepExceededException extends RuntimeException{
    public MaxStepExceededException(int mxstep) {
        super(String.format("[lsoda] " + mxstep + " steps taken before reaching tout\n Try increasing mxstep"));
    }
}
