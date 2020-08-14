/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.algorithm;

import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.controller.SimulationController;


/**
 * This is an enhanced version of the original Direct method developed by Gillespie. Just like the
 * algorithm of Gibson and Bruck it uses a dependency graph to know what propensities have to be
 * recalculated.
 * <p>
 * Take care with the option efficientlyAdaptSum: if it is true, it is possible that the sum become
 * numerically unstable and the simulation fails, but especially for sparse networks it should be
 * much more efficient.
 *
 * <p>
 * For references see Daniel T. Gillespie., A General Method for Numerically Simulating the
 * Stochastic Time Evolution of Coupled Chemical Reactions, J.Comp.Phys. 22, 403 (1976) and
 * M.A.Gibson and J.Bruck, Efficient Exact Stochastic Simulation of Chemical Systems with Many
 * Species and Many Channels, J.Phys.Chem.A., Vol 104, no 9, 2000
 *
 * @author Florian Erhard
 * @see GillespieSimple
 * @see DependencyGraph
 */
public class GillespieEnhanced extends Simulator {

  protected double a_sum = 0;
  protected DependencyGraph dep = null;
  protected boolean efficientlyAdaptSum = false;
  protected boolean changed = false;

  public GillespieEnhanced(Network net) {
    super(net);
  }

  @Override
  public void initialize() {
    super.initialize();

		if (dep == null) {
			dep = new DependencyGraph(getNet());
		}

    a_sum = 0;
    for (int i = 0; i < a.length; i++) {
      a_sum += a[i];
    }


  }

  @Override
  public void reinitialize() {
    changed = true;
  }

  @Override
  public void performStep(SimulationController control) {

    if (changed) {
      initializePropensities();
      a_sum = 0;
      for (int i = 0; i < a.length; i++) {
        a_sum += a[i];
      }
    }

    // obtain mu and tau by the direct method described in chapter 5A page 417ff
    double tau = directMCTau(a_sum);

    if (!Double.isInfinite(tau)) {
      changed = false;
			while (t <= getNextThetaEvent() && t + tau > getNextThetaEvent() && !changed) {
				thetaEvent();
			}

      if (changed) {
        performStep(control);
        return;

      }
      int mu = directMCReaction();

      fireReaction(mu, t + tau, FireType.GillespieEnhanced);

      for (int alpha : dep.getDependent(mu)) {
				if (efficientlyAdaptSum) {
					a_sum -= a[alpha];
				}
        a[alpha] = getPropensityCalculator().calculatePropensity(alpha, getAmountManager(), this);
				if (efficientlyAdaptSum) {
					a_sum += a[alpha];
				}
      }
    }
    if (!efficientlyAdaptSum) {
      a_sum = 0;
			for (int i = 0; i < a.length; i++) {
				a_sum += a[i];
			}
    }
    // advance in time
    t += tau;

		if (Double.isInfinite(tau)) {
			thetaEvent();
		}
  }

  /**
   * obtains a random (but following a specific distribution) reaction as described by the direct
   * method in chapter 5A page 417ff
   *
   * @return
   */
  private int directMCReaction() {
    double r2 = stochastics.getUnif();
    double test = r2 * a_sum;

    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];
			if (sum >= test) {
				return i;
			}
    }

    throw new RuntimeException("No reaction could be selected!");
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
    return "enhanced Gillespie";
  }

  public boolean isEfficientlyAdaptSum() {
    return efficientlyAdaptSum;
  }

  public void setEfficientlyAdaptSum(boolean efficientlyAdaptSum) {
    this.efficientlyAdaptSum = efficientlyAdaptSum;
  }
}
