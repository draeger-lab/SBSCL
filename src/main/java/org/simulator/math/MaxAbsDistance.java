package org.simulator.math;

import org.simulator.math.odes.MultiTable;

/**
 * An implementation of maxAbsDistance. A maxAbsDistance is basically the maximum
 * absolute distance of the distances of every single element in the two vectors
 * (arrays).
 */
public class MaxAbsDistance extends QualityMeasure {


    @Override
    public double distance(MultiTable.Block.Column x, MultiTable.Block.Column y, double defaultValue) {
        double x_i;
        double y_i;
        double d = Double.MIN_VALUE;

        for (int z = 0 ; z < Math.min(x.getRowCount(), y.getRowCount()); z++){
            x_i = x.getValue(z);
            y_i = y.getValue(z);
            if (!Double.isNaN(y_i) && !Double.isNaN(x_i) && (y_i != x_i)){
                d = Math.max(d, Math.abs(x_i - y_i));
            }
        }

        return d;
    }

}
