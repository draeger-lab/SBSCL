package org.simulator.math;

import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * Computes the relative distance of two vectors based on the {@link MaxAbsDistance}
 * distance.
 */
public class RelativeMaxDistance extends QualityMeasure {

    /**
     * The metric the relative distance is based on
     */
    protected MaxAbsDistance metric;

    /**
     * Default Constructor
     */
    public RelativeMaxDistance() {
        super(Double.NaN);
        metric = new MaxAbsDistance();
    }

    /**
     * Initialization with a given {@link MaxAbsDistance}
     *
     * @param metric
     */
    public RelativeMaxDistance(MaxAbsDistance metric) {
        super(Double.NaN);
        this.metric = metric;
    }

    /* (non-Javadoc)
     * @see org.sbml.simulator.math.Distance#distance(java.lang.Iterable, java.lang.Iterable, double)
     */
    @Override
    public double distance(Column x, Column y, double defaultValue) {

        for (int i = 0; i < Math.min(x.getRowCount(), y.getRowCount()); i++) {
            if (y.getValue(i) != 0) {
                x.setValue((y.getValue(i) - x.getValue(i)) / y.getValue(i), i);
            } else {
                x.setValue(Double.POSITIVE_INFINITY, i);
            }
        }

        return metric.distanceToZero(x, defaultValue);
    }
}
