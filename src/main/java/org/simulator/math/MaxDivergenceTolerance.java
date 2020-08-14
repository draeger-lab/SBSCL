package org.simulator.math;

import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * An implementation of core comparison metric of the simulator's
 * result with the pre-defined results. MaxDivergenceTolerance class
 * basically calculates the LHS of the below given metric.
 * <p>
 * Metric Formula:
 *
 * <math xmlns="http://www.w3.org/1998/Math/MathML" display="block">
 *   <mfrac>
 *     <mrow>
 *       <mo>|</mo>
 *       <msub>
 *         <mi>c</mi>
 *         <mrow class="MJX-TeXAtom-ORD">
 *           <mi>i</mi>
 *           <mi>j</mi>
 *         </mrow>
 *       </msub>
 *       <mo>&#x2212;<!-- − --></mo>
 *       <msub>
 *         <mi>u</mi>
 *         <mrow class="MJX-TeXAtom-ORD">
 *           <mi>i</mi>
 *           <mi>j</mi>
 *         </mrow>
 *       </msub>
 *       <mo>|</mo>
 *     </mrow>
 *     <mrow>
 *       <msub>
 *         <mi>T</mi>
 *         <mi>a</mi>
 *       </msub>
 *       <mo>+</mo>
 *       <msub>
 *         <mi>T</mi>
 *         <mi>r</mi>
 *       </msub>
 *       <mo>&#x22C5;<!-- ⋅ --></mo>
 *       <mrow>
 *         <mo>|</mo>
 *         <msub>
 *           <mi>c</mi>
 *           <mrow class="MJX-TeXAtom-ORD">
 *             <mi>i</mi>
 *             <mi>j</mi>
 *           </mrow>
 *         </msub>
 *         <mo>|</mo>
 *       </mrow>
 *     </mrow>
 *   </mfrac>
 *   <mo>&#x2264;<!-- ≤ --></mo>
 *   <mn>1</mn>
 * <mfrac>
 */
public class MaxDivergenceTolerance extends QualityMeasure {

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
   * Default constructor. Initializes the metric {@link MaxAbsDistance} and sets the absolute and
   * relative tolerances.
   *
   * @param absTol
   * @param relTol
   */
  public MaxDivergenceTolerance(double absTol, double relTol) {
    // sets the default value in case any error occurs
    super(Double.NaN);
    metric = new MaxAbsDistance();
    this.absTol = absTol;
    this.relTol = relTol;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double distance(Column x, Column y, double defaultValue) {
    for (int i = 0; i < Math.min(x.getRowCount(), y.getRowCount()); i++) {
      double p = Math.abs(y.getValue(i) - x.getValue(i));
      double q = this.absTol + (this.relTol * Math.abs(y.getValue(i)));
      x.setValue((q != 0) ? (p / q) : Double.POSITIVE_INFINITY, i);
    }
    return metric.distanceToZero(x, defaultValue);
  }
}
