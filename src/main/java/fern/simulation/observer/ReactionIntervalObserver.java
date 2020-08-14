package fern.simulation.observer;

import java.util.Arrays;
import java.util.Map;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;

/**
 * Observes number of firings of reactions repeatedly after certain intervals.
 * <p>
 * This observer does take repeats into account. If you repeat the simulation, you will get an
 * average over the results of each run.
 *
 * @author Florian Erhard
 */
public class ReactionIntervalObserver extends IntervalObserver {

  private int[] observeReactions;
  private Map<Integer, Integer> reactionIndexToObserverIndex;
  private double recordStart = 0;

  /**
   * Creates the observer for a given simulator, a given interval and given neighbors.
   *
   * @param sim          simulator
   * @param interval     interval
   * @param speciesNames the names of the species, each neighboring reaction of any of these is
   *                     added
   */
  public ReactionIntervalObserver(Simulator sim, double interval, String... speciesNames) {
    this(sim, interval, NetworkTools.getReactionsOf(sim.getNet(), speciesNames));
  }

  /**
   * Creates the observer for a given simulator, a given interval and given reaction indices.
   *
   * @param sim       simulator
   * @param interval  interval
   * @param reactions reaction indices
   */
  public ReactionIntervalObserver(Simulator sim, double interval, int... reactions) {
    super(sim, interval, NetworkTools.getReactionNames(sim.getNet(), reactions));

    this.reactionIndexToObserverIndex = NumberTools.createInverseAsMap(reactions);
    this.observeReactions = new int[reactions.length];
  }

  /**
   * Record the firing of mu, if mu is registered.
   */
  @Override
  public void activateReaction(int mu, double t, FireType fireType, int times) {
		if (recordStart < t && reactionIndexToObserverIndex.containsKey(mu)) {
			observeReactions[reactionIndexToObserverIndex.get(mu)] += times;
		}
  }

  @Override
  public void started() {
    super.started();
    Arrays.fill(observeReactions, 0);
  }


  /**
   * Gets the moment in time where the recording starts
   *
   * @return the recordStart
   */
  public double getRecordStart() {
    return recordStart;
  }

  /**
   * Sets the moment in time where the recording starts
   *
   * @param recordStart the recordStart to set
   */
  public void setRecordStart(double recordStart) {
    this.recordStart = recordStart;
  }

  /**
   * Gets the actual number of firings of a reaction since the simulation started.
   */
  @Override
  protected double getEntityValue(int i) {
    return observeReactions[i];
  }

  public String[] getStyles() {
    return null;
  }


}
