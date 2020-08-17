/**
 *
 */
package fern.simulation.algorithm;

import cern.colt.bitvector.BitVector;
import fern.network.Network;
import fern.tools.NetworkTools;


/**
 * There are some possibilities to bind the expected change of the propensities by a
 * value epsilon in order to fulfill the leap condition. Here it is bound indirectly
 * by the reactant species of each reaction which gives the same leaps in a more
 * efficient manner.
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
public class TauLeapingSpeciesPopulationBoundSimulator extends
    AbstractBaseTauLeaping {

  // g[i]=-2 -> some second order reaction requires two i molecules
  // g[i]=-3 -> some third order reaction requires three i molecules
  // g[i]=-4 -> some third order reaction requires two i molecules
  private int[] g;
  private double[] mu;
  private double[] sigma;


  public TauLeapingSpeciesPopulationBoundSimulator(Network net) {
    super(net);

    calculateG();
    mu = new double[net.getNumSpecies()];
    sigma = new double[net.getNumSpecies()];
  }


  @Override
  protected double chooseTauNonCriticals(BitVector criticals) {
    preprocessNonCriticals(criticals);

    double tau = Double.POSITIVE_INFINITY;

    for (int i = 0; i < getNet().getNumSpecies(); i++) {

      if (g[i] == 0) {
        continue;
      }
      double gi = g[i];
      if (gi == -2) {
        gi = 2.0 + 1.0 / (getAmountManager().getAmount(i) - 1.0);
      } else if (gi == -3) {
        gi = 3.0 + 1.0 / (getAmountManager().getAmount(i) - 1.0) + 2.0 / (
            getAmountManager().getAmount(i) - 2.0);
      } else if (gi == -4) {
        gi = 1.5 * (2.0 + 1.0 / (getAmountManager().getAmount(i) - 1.0));
      }

      double max = Math.max(getEpsilon() * getAmountManager().getAmount(i) / gi, 1);
      tau = Math.min(tau, max / Math.abs(mu[i]));
      tau = Math.min(tau, max * max / sigma[i]);
      if (verbose) {
        System.out.print(NetworkTools.getSpeciesNameWithAmount(getNet(), i));
        System.out.println(
            " mu " + mu[i] + "\tsigma " + sigma[i] + "\tmu term=" + (max / Math.abs(mu[i])
                + "\tsigma term=" + max * max / sigma[i]));
      }
    }
    return tau;
  }

  private void preprocessNonCriticals(BitVector criticals) {
    for (int i = 0; i < getNet().getNumSpecies(); i++) {
      mu[i] = 0;
      sigma[i] = 0;
      if (g[i] == 0) {
        continue;
      }
      for (int j = 0; j < getNet().getNumReactions(); j++) {
        if (criticals.get(j) || getV(i, j) == 0) {
          continue;
        }
        mu[i] += getV(i, j) * a[j];
        sigma[i] += getV(i, j) * getV(i, j) * a[j];
      }
    }
  }


  @SuppressWarnings("unchecked")
  private void calculateG() {
    Network net = getNet();

    g = new int[net.getNumSpecies()];

    int[] HOR = new int[net.getNumSpecies()];
    for (int r = 0; r < net.getNumReactions(); r++) {
      int[] reactants = net.getReactants(r);
      for (int i : reactants) {
        HOR[i] = Math.max(HOR[i], reactants.length);
      }
    }

    for (int i = 0; i < g.length; i++) {
      g[i] = HOR[i];
    }

    for (int r = 0; r < net.getNumReactions(); r++) {
      for (int reactantSpecies : reactantHistos[r].keySet()) {
        if (HOR[reactantSpecies] == 2 && reactantHistos[r].size() == 1
            && reactantHistos[r].get(reactantSpecies) == 2) {
          g[reactantSpecies] = -2;
        } else if (HOR[reactantSpecies] == 3 && reactantHistos[r].size() == 2
            && reactantHistos[r].get(reactantSpecies) == 2) {
          g[reactantSpecies] = -4;
        } else if (HOR[reactantSpecies] == 3 && reactantHistos[r].size() == 1
            && reactantHistos[r].get(reactantSpecies) == 3) {
          g[reactantSpecies] = -3;
        }
      }
    }
  }

  @Override
  public String getName() {
    return "Tau Leap Species Population Bound";
  }
}
