package org.simulator.math;

import org.simulator.math.odes.MultiTable.Block.Column;

public class MaxDivergenceTolerance extends QualityMeasure{

    /**
     * The metric the relative distance is based on
     */
    protected MaxAbsDistance metric;

    /**
     * Absolute tolerance for the reaction
     */
    protected double absTol;

    /**
     * Relative tolerance for the reaction
     */
    protected double relTol;

    /**
     * Default Constructor
     */
    public MaxDivergenceTolerance(double absTol, double relTol) {
        super(Double.NaN);
        metric = new MaxAbsDistance();
        this.absTol = absTol;
        this.relTol = relTol;
    }

    /* (non-Javadoc)
     * @see org.sbml.simulator.math.Distance#distance(java.lang.Iterable, java.lang.Iterable, double)
     */
    @Override
    public double distance(Column x, Column y, double defaultValue) {

        for (int i=0;i<Math.min(x.getRowCount(), y.getRowCount()); i++){

            double p = Math.abs(y.getValue(i) - x.getValue(i));
            double q = this.absTol + (this.relTol * Math.abs(y.getValue(i)));

            if (q != 0) {
                x.setValue(p / q, i);
            }else {
                x.setValue(Double.POSITIVE_INFINITY, i);
            }

        }

        return metric.distanceToZero(x, defaultValue);
    }

}
