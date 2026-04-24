package org.simulator.math.odes.exception;

public class TooMuchAccuracyException extends RuntimeException{
    public TooMuchAccuracyException(double tolsf) {
        super("[lsoda] at start of problem, too much accuracy requested \n" +
                            "for precision of machine, suggested scaling factor = " + tolsf);
    }

    public TooMuchAccuracyException(double t, double tolsf) {
        super("[lsoda] at t = " + t + " , too much accuracy requested\n" +
                            "for precision of machine, suggested scaling factor = " + tolsf);
    }
}
