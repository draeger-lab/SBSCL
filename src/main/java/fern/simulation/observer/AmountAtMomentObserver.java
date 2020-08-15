package fern.simulation.observer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.controller.SimulationController;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;


/**
 * Observes the amount of some molecule species at a certain moment. In order to accomplish that,
 * this class also implements a {@link SimulationController} and registers a <code>theta</code> at
 * the simulator. The data produced after are histograms for each species representing the
 * distribution of amounts at theta.
 *
 * @author Florian Erhard
 * @see IntervalObserver#getTheta()
 */
public class AmountAtMomentObserver extends Observer implements SimulationController,
    GnuPlotObserver {

  private String[] speciesNames;
  private int[] species;
  private double moment;
  private boolean thetaReached;
  private Map<Integer, Integer>[] histogram;
  private int count = 0;

  /**
   * Creates the observer for the given simulator, the given theta and the given species
   *
   * @param sim         simulator
   * @param moment      theta
   * @param speciesName names of the species to observe
   */
  @SuppressWarnings("unchecked")
  public AmountAtMomentObserver(Simulator sim, double moment, String... speciesName) {
    super(sim);
    this.species = NetworkTools.getSpeciesIndices(sim.getNet(), speciesName);
    this.speciesNames = speciesName;
    this.moment = moment;
    histogram = new HashMap[species.length];
    for (int i = 0; i < histogram.length; i++) {
      histogram[i] = new HashMap<Integer, Integer>();
    }
  }

  /**
   * Creates the observer for the given simulator, the given theta and the given species
   *
   * @param sim            simulator
   * @param moment         theta
   * @param speciesIndices indices of the species to observe
   */
  public AmountAtMomentObserver(Simulator sim, double moment, int... speciesIndices) {
    this(sim, moment, NetworkTools.getSpeciesNames(sim.getNet(), speciesIndices));
  }

  /**
   * Causes the simulator to stop when <code>theta</code> has been passed and the amounts have been
   * recorded.
   */
  public boolean goOn(Simulator sim) {
    return !thetaReached;
  }

  /**
   * Do nothing
   */
  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
  }

  /**
   * Records the amounts of the species and puts it into the histogram.
   */
  @Override
  public void theta(double theta) {
    for (int i = 0; i < species.length; i++) {
      int amount = (int) getSimulator().getAmount(species[i]);
      if (!histogram[i].containsKey(amount)) {
        histogram[i].put(amount, 0);
      }
      histogram[i].put(amount, histogram[i].get(amount) + 1);
    }
    thetaReached = true;
    setTheta(Double.POSITIVE_INFINITY);
  }

  /**
   * Do nothing.
   */
  @Override
  public void finished() {
  }

  /**
   * Register theta at the simulator.
   */
  @Override
  public void started() {
    count++;
    thetaReached = false;
    setTheta(moment);
  }

  /**
   * Do nothing.
   */
  @Override
  public void step() {
  }

  public GnuPlot toGnuplot() throws IOException {
    return toGnuplot(new GnuPlot());
  }

  public GnuPlot toGnuplot(GnuPlot gnuplot) throws IOException {
    if (gnuplot == null) {
      return null;
    }
    int index = 0;

    for (Map<Integer, Integer> histo : histogram) {
      int max = NumberTools.max(histo.keySet());
      int min = NumberTools.min(histo.keySet());

      double[][] data = new double[max - min + 1][2];
      for (int i = min; i <= max; i++) {
        data[i - min][0] = i;
        data[i - min][1] = histo.containsKey(i) ? (double) histo.get(i) / (double) count : 0.0;
      }

      gnuplot.addData(data, new String[]{getLabel(speciesNames[index++])}, getStyles());
    }

    return gnuplot;
  }

  /**
   * Gets the histogram of the distribution for a species
   *
   * @param species species index (but not in network index space - i means the ith passed species)
   * @return histogram as map
   */
  public Map<Integer, Integer> getHistogram(int species) {
    return histogram[species];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < species.length; i++) {
      sb.append("Histogram of " + speciesNames[i] + ":\n");
      sb.append(NumberTools.getHistogramAsString(histogram[i]));
      sb.append("\n");
    }
    return sb.toString();
  }

  public String[] getStyles() {
    return null;
  }

}
