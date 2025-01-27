package org.simulator.math.odes.LSODA;

@FunctionalInterface
public interface LSODAFunction {
    int evaluate(double t, double[] y, double[] ydot, Object data);
}