/**
 *
 */
package fern.simulation.algorithm;

import cern.colt.bitvector.BitVector;
import fern.network.Network;
import fern.network.PartialDerivativePropensityCalculator;
import fern.tools.NetworkTools;
import java.util.Arrays;

/**
 * There are some possibilities to bind the expected change of the propensities by a
 * value epsilon in order to fulfill the leap condition. This base class provides 
 * mathematics for binding it directly by examining the actual propensities.
 * <p>
 * Daniel T. Gillespie, Approximate accelerated stochastic simulation 
 * of chemically reacting systems, Journal of chemical physics vol 115, nr 4 (2001); Cao et al., Efficient
 * step size selection for the tau-leaping simulation method, Journal of chemical physics 124, 044109 (2006)
 *
 *
 *
 * @author Florian Erhard
 *
 */
public abstract class AbstractTauLeapingPropensityBoundSimulator extends AbstractBaseTauLeaping {


  protected double[][] f;
  protected double[] mu;
  protected double[] sigma;


  public AbstractTauLeapingPropensityBoundSimulator(Network net) {
    super(net);
    f = new double[net.getNumReactions()][net.getNumReactions()];
    mu = new double[net.getNumReactions()];
    sigma = new double[net.getNumReactions()];
  }


  @Override
  protected double chooseTauNonCriticals(BitVector criticals) {
    preprocessNonCriticals(criticals);

    String min = null;
    double top;
    double tau = Double.POSITIVE_INFINITY;
    for (int j = 0; j < getNet().getNumReactions(); j++) {

      top = getTop(j);

      tau = Math.min(tau, top / Math.abs(mu[j]));
      tau = Math.min(tau, top * top / sigma[j]);

      if (verbose) {
        System.out.println(NetworkTools.getReactionNameWithAmounts(getNet(), j) + " " + a[j]);
        System.out.println(
            "mu" + mu[j] + "\tsigma " + sigma[j] + "\tmu term=" + top / Math.abs(mu[j])
                + "\tsigma term=" + top * top / sigma[j]);
        System.out.println(Arrays.toString(f[j]));
        System.out.println();
        if (tau == top / Math.abs(mu[j]) || tau == top * top / sigma[j]) {
          min = getName() + " " + NetworkTools.getReactionNameWithAmounts(getNet(), j) + "\tmu "
              + mu[j] + "\tsigma " + sigma[j] + "\tmu term=" + (top / Math.abs(mu[j])
              + "\tsigma term=" + top * top / sigma[j]);
        }
      }

    }
    if (verbose) {
      System.out.println();
      System.out.println("Minimum at " + min);
    }

    return tau;


  }

  protected abstract double getTop(int j);


  private void preprocessNonCriticals(BitVector criticals) {

    if (!(getNet().getPropensityCalculator() instanceof PartialDerivativePropensityCalculator)) {
      throw new RuntimeException(
          "Cannot use this tau leap method for not constant propensity calculators!");
    }

    PartialDerivativePropensityCalculator propCalc = (PartialDerivativePropensityCalculator) getNet()
        .getPropensityCalculator();

    for (int j = 0; j < getNet().getNumReactions(); j++) {
      for (int js = 0; js < getNet().getNumReactions(); js++) {
        if (criticals.get(js)) {
          continue;
        }
        f[j][js] = 0;
        for (int species : reactantHistos[j].keySet()) {
          double v = getV(species, js);
          if (v != 0) {
            f[j][js] += v * propCalc
                .calculatePartialDerivative(j, getAmountManager(), species, getVolume());
          }
        }
//				for (int species : productHistos[js].keySet()) {
//					if (reactantHistos[js].containsKey(species)) continue;
//					double v = getV(species,js);
//					if (v!=0 && reactantHistos[j].containsKey(species))
//						f[j][js]+=v*propCalc.calculatePartialDerivative(j, getAmountManager(), species, getVolume());
//				}
      }
    }

    for (int j = 0; j < getNet().getNumReactions(); j++) {
      mu[j] = 0;
      sigma[j] = 0;
      for (int js = 0; js < getNet().getNumReactions(); js++) {
        if (criticals.get(js)) {
          continue;
        }
        mu[j] += f[j][js] * a[js];
        sigma[j] += f[j][js] * f[j][js] * a[js];
      }
    }
  }


}
