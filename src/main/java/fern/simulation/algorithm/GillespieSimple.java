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
 * Implementation of Gillespie's Direct method. It is a simple Monte-Carlo algorithm which draws
 * from a from Gillspie derived distribution a reaction that will fire and a time at which the
 * reaction will fire.
 * <p>
 * For reference see Daniel T. Gillespie., A General Method for Numerically Simulating the
 * Stochastic Time Evolution of Coupled Chemical Reactions, J.Comp.Phys. 22, 403 (1976)
 *
 * @author Florian Erhard
 */
public class GillespieSimple extends Simulator {

  private double a_sum = 0;
  private boolean changed = false;

  public GillespieSimple(Network net) {
    super(net);

  }

  @Override
  public void reinitialize() {
    changed = true;
  }

  @Override
  public void performStep(SimulationController control) {
    a_sum = 0;
    // calc the h's described in (14) page 413 and the sum a (26) page 418
    for (int i = 0; i < a.length; i++) {
      a[i] = getPropensityCalculator().calculatePropensity(i, getAmountManager(), this);
      a_sum += a[i];
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

      fireReaction(mu, t + tau, FireType.GillespieSimple);
    }

    // advance the time
    t += tau;

		if (Double.isInfinite(tau)) {
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
  public String getName() {
    return "original Gillespie";
  }


}
