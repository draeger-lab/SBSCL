package org.simulator.math.odes.LSODA;

// @FunctionalInterface
public class LSODAFunction {
    public int evaluate(double t, double[] y, double[] ydot, Object data) {
        return 1;
    }

    public LSODAFunction(){
    }
}