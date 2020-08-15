/*
 * Created on 03.08.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.algorithm;

import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.controller.SimulationController;


/**
 * Implementation of the algorithm of Gibson and Bruck. They made two major improvements to
 * Gillespie's Direct method ({@link GillespieSimple}):
 * <ol>
 * 	<li>using a dependency graph</li>
 *  <li>using a indexed priority queue</li>
 * </ol>
 * The first describes, which propensities have to be recalculates after some reaction
 * has fired, the latter puts an end to the time consuming search for the Direct
 * which fires after one firing. The priority queue just yields the next reaction to fire (from
 * which the name "Next reaction method" is derived). The priority queue holds for each
 * reaction the time of its next firing.
 * <p>
 * In order to do that the perspective of viewing the simulators time has to be changed
 * from relative to absolute. As a consequence, a new random number has to be drawn for
 * each reaction whose propensity has changed by a firing. To avoid this inefficient permanent
 * discarding of random numbers (drawing a random number is a time expensive operation)
 * Gibson and Bruck developed a method for reusing unused random numbers.
 * <p>
 * One pitfall remains: What happens, when a propensity decreases to 0 and after a time
 * ceases to be 0. Then the propensity from the pre-0 state can be reused which is tricky
 * to implement. Some implementations (including Dizzy) just draw a new random number which
 * is always valid but slow. Actually may these extra random numbers spoil the whole
 * improvement of the indexed priority queue in certain networks and it would be faster to
 * use the Direct method with the dependency graph (as in {@link GillespieEnhanced} ).
 *
 *
 * <p>
 * For reference see M.A.Gibson and J.Bruck, Efficient Exact Stochastic Simulation of Chemical
 * Systems with Many Species and Many Channels, J.Phys.Chem.A., Vol 104, no 9, 2000
 *
 * @author Florian Erhard
 * @see GillespieSimple
 * @see GillespieEnhanced
 * @see DependencyGraph
 * @see IndexedPriorityQueue
 */
public class GibsonBruckSimulator extends Simulator {

  private boolean changed = false;
  private DependencyGraph dep = null;
  private IndexedPriorityQueue queue = null;
  private double[] tBeforeAlphaBecomesNull = null;
  private double[] aBeforeAlphaBecomesNull = null;

  public GibsonBruckSimulator(Network net) {
    super(net);
  }

  @Override
  public void reinitialize() {
    changed = true;
  }

  public void performStep(SimulationController control) {

    if (changed) {
      initialize();
      changed = false;
    }

    double tau = queue.getMinKey();

    if (!Double.isInfinite(tau)) {

      changed = false;
      while (t <= getNextThetaEvent() && tau > getNextThetaEvent() && !changed) {
        thetaEvent();
      }

      if (changed) {
        performStep(control);
        return;
      }

      int mu = queue.getMin();

      fireReaction(mu, tau, FireType.GibsonBruck);

      for (int alpha : dep.getDependent(mu)) {
        double a_alphaold = a[alpha];
        a[alpha] = getPropensityCalculator().calculatePropensity(alpha, getAmountManager(), this);

        double tau_alpha;

        if (a[alpha] == 0) {
          tau_alpha = Double.POSITIVE_INFINITY;
          if (a_alphaold > 0) {
            tBeforeAlphaBecomesNull[alpha] = queue.getKey(alpha) - tau;
            aBeforeAlphaBecomesNull[alpha] = alpha == mu ? 0 : a_alphaold;
          }
        } else {
          if (a_alphaold == 0 && a[alpha] > 0) {
            // alpha != mu
            if (aBeforeAlphaBecomesNull[alpha] == 0) {
              tau_alpha = stochastics.getExponential(a[alpha]) + tau;
            } else {
              tau_alpha =
                  (aBeforeAlphaBecomesNull[alpha] / a[alpha]) * (tBeforeAlphaBecomesNull[alpha])
                      + tau;
            }

          } else {
            if (alpha != mu) {
              tau_alpha = (a_alphaold / a[alpha]) * (queue.getKey(alpha) - tau) + tau;
            } else {
              tau_alpha = stochastics.getExponential(a[alpha]) + tau;
            }
          }
        }

        queue.update(alpha, tau_alpha);


      }
    }

    t = tau;

    if (Double.isInfinite(tau)) {
      thetaEvent();
    }
  }

  @Override
  public void initialize() {
    super.initialize();

    if (dep == null) {
      dep = new DependencyGraph(getNet());
    }

    // create initial values for tau
    double[] t = new double[getNet().getNumReactions()];
    for (int i = 0; i < t.length; i++) {
      t[i] = stochastics.getExponential(a[i]) + getTime();
    }

    // and store them in the indexedpriorityqueue
    queue = new IndexedPriorityQueue(t);

    aBeforeAlphaBecomesNull = new double[a.length];
    tBeforeAlphaBecomesNull = new double[a.length];
  }

  @Override
  public String getName() {
    return "Gibson-Bruck";
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


}
