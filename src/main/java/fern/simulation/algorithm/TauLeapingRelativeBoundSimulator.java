package fern.simulation.algorithm;

import fern.network.KineticConstantPropensityCalculator;
import fern.network.Network;

/**
 * There are some possibilities to bind the expected change of the propensities by a value epsilon
 * in order to fulfill the leap condition. Here the expected change is bound to individual
 * propensities.
 * <p>
 * Daniel T. Gillespie, Approximate accelerated stochastic simulation of chemically reacting
 * systems, Journal of chemical physics vol 115, nr 4 (2001); Cao et al., Efficient step size
 * selection for the tau-leaping simulation method, Journal of chemical physics 124, 044109 (2006)
 *
 * @author Florian Erhard
 */
public class TauLeapingRelativeBoundSimulator extends
    AbstractTauLeapingPropensityBoundSimulator {

  KineticConstantPropensityCalculator propCalc;

  public TauLeapingRelativeBoundSimulator(Network net) {
    super(net);
    if (!(getNet().getPropensityCalculator() instanceof KineticConstantPropensityCalculator)) {
      throw new RuntimeException(
          "Cannot use this tau leap method for not constant propensity calculators!");
    }

    propCalc = (KineticConstantPropensityCalculator) getNet().getPropensityCalculator();

  }

  @Override
  protected double getTop(int j) {
    double cj = getVolume() > 0 ? propCalc
        .getConstantFromDeterministicRateConstant(propCalc.getConstant(j), j, getVolume())
        : propCalc.getConstant(j);
    return Math.max(getEpsilon() * a[j], cj);
  }

  @Override
  public String getName() {
    return "Tau Leap Propensitiy Relative Bound";
  }
}
