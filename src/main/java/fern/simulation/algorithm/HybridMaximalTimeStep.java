package fern.simulation.algorithm;

import cern.colt.bitvector.BitVector;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.controller.SimulationController;

/**
 * This is an modified version of Maximal Time Step Method by Puchalka and Kierzec. Reactions are
 * divided into slow and fast reaction. The slow ones are fired according to Gillespies Direct
 * Method as in {@link GillespieEnhanced}, the fast ones are handled by tau leaping. This method can
 * be a substancial improvement on speed when simulating a network composed of different realms of
 * magnitude (e.g. when simulating both gene regulation and metabolism).
 *
 * <p>
 * For references see J. Puchalka. and A.M. Kierzek, Briding the Gap betwwen Stochastic and
 * Determininistic Regimes in the Kinetic Simulations of the Biochemical Reaction Networks,
 * Biophysical 86, 1357-1372 (2004)
 *
 * @author Florian Erhard
 * @see GillespieEnhanced
 * @see DependencyGraph
 */
public class HybridMaximalTimeStep extends Simulator {

  protected double a_sum = 0;
  private DependencyGraph dep = null;
  private boolean changed = false;

  private double kappa = 1E-3;
  private double r = 1E-4;
  private int n = 100;
  private BitVector fast;
  private BitVector dependent;


  public HybridMaximalTimeStep(Network net) {
    super(net);
    fast = new BitVector(getNet().getNumReactions());
    dependent = new BitVector(getNet().getNumReactions());
    dep = new DependencyGraph(net);
  }

  @Override
  public void initialize() {
    super.initialize();

    a_sum = 0;
		for (int i = 0; i < a.length; i++) {
			a_sum += a[i];
		}

    partition();
  }

  @Override
  public void reinitialize() {
    changed = true;
  }

  @Override
  public void performStep(SimulationController control) {

		if (changed) {
			initialize();
		}

    a_sum = 0;
		for (int i = 0; i < a.length; i++) {
			if (!fast.getQuick(i)) {
				a_sum += a[i];
			}
		}

    // obtain mu and tau by the direct method described in chapter 5A page 417ff
    double tau = directMCTau(a_sum);
    double delta = fast.cardinality() == 0 ? tau : Math.min(kappa, tau);

    changed = false;
		while (delta < Double.POSITIVE_INFINITY && t <= getNextThetaEvent()
				&& t + delta > getNextThetaEvent() && !changed) {
			thetaEvent();
		}

    if (changed) {
      performStep(control);
      return;
    }

    dependent.clear();

    for (int i = 0; i < fast.size(); i++) {
      if (fast.getQuick(i)) {
        int k = stochastics.getPoisson(a[i] * delta);

        fireReaction(i, t, t + tau, k, FireType.TauLeapNonCritical);

				for (int alpha : dep.getDependent(i)) {
					dependent.set(alpha);
				}
      }
    }

    if (tau <= delta) {
      int mu = directMCReaction();

      fireReaction(mu, t + tau, FireType.GillespieEnhanced);

			for (int alpha : dep.getDependent(mu)) {
				dependent.set(alpha);
			}

    }

		for (int alpha = 0; alpha < dependent.size(); alpha++) {
			if (dependent.getQuick(alpha)) {
				a[alpha] = getPropensityCalculator().calculatePropensity(alpha, getAmountManager(), this);
			}
		}

    for (int i = 0; i < fast.size(); i++) {
      if (dependent.getQuick(i)) {
        if (fast.getQuick(i)) {
					if (!isFast(i)) {
						fast.clear(i);
					}
        } else {
					if (isFast(i)) {
						fast.set(i);
					}

        }
      }
    }

    // advance in time
    t += delta;

		if (Double.isInfinite(delta)) {
			thetaEvent();
		}
  }

  /**
   * obtains a random (but following a specific distribution) reaction as described by the direct
   * method in chapter 5A page 417ff
   *
   * @param reactions
   * @param a
   * @return
   */
  private int directMCReaction() {
    double r2 = stochastics.getUnif();
    double test = r2 * a_sum;

    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      if (!fast.getQuick(i)) {
        sum += a[i];
				if (sum >= test) {
					return i;
				}
      }
    }

    throw new RuntimeException("No reaction could be selected!");
  }

  private boolean isFast(int mu) {
    return condition2(mu) && condition1(mu);
  }

  private boolean condition1(int mu) {
    int[] r = getNet().getReactants(mu);
		for (int i = 0; i < r.length; i++) {
			if (getAmount(r[i]) < n) {
				return false;
			}
		}
    return true;
  }

  private boolean condition2(int mu) {
    return a[mu] / a_sum > r;
  }

  private void partition() {
    fast.clear();
		for (int i = 0; i < fast.size(); i++) {
			if (isFast(i)) {
				fast.set(i);
			}
		}
  }

  /**
   * obtains a random (but following a specific distribution) timestep as described by the direct
   * method in chapter 5A page 417ff
   *
   * @param sum sum of the propensities
   * @return tau
   */
  protected double directMCTau(double sum) {
    double r1 = stochastics.getUnif();
    return (1 / sum) * Math.log(1 / r1);
  }

  @Override
  public void setVolume(double volume) {
    super.setVolume(volume);
    changed = true;
  }

  @Override
  public void setAmount(int species, long amount) {
    super.setAmount(species, amount);
    changed = true;
  }

  @Override
  public String getName() {
    return "Maximal Time Step Method";
  }


}
