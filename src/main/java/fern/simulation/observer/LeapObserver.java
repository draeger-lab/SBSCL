package fern.simulation.observer;

import java.io.IOException;
import java.util.LinkedList;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * Observes the amount of given molecule species at each <code>step</code>. It particularly makes
 * sense for the approximative leaping algorithms whose <code>step</code>s are precisely the leaps
 * and you want to know about amount differences after each leap.
 * <p>
 * This observer does not take repeats into account. If you repeat the simulation, you will just get
 * the results of the recent run.
 *
 * @author Florian Erhard
 */
public class LeapObserver extends Observer implements GnuPlotObserver {

  private LinkedList<double[]> leap;
  private int[] species;
  private String[] speciesNames;

  /**
   * Creates the observer with a given simulator and given species
   *
   * @param sim          simulator
   * @param speciesNames names of the species
   */
  public LeapObserver(Simulator sim, String... speciesNames) {
    super(sim);
    leap = new LinkedList<double[]>();
    this.species = NetworkTools.getSpeciesIndices(sim.getNet(), speciesNames);
    this.speciesNames = speciesNames;
  }

  /**
   * Do nothing.
   */
  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
  }

  /**
   * Do nothing.
   */
  @Override
  public void finished() {
  }

  /**
   * Clears the recorded data.
   */
  @Override
  public void started() {
    leap.clear();
  }

  /**
   * Record the time and amounts of the species.
   */
  @Override
  public void step() {
    double[] l = new double[speciesNames.length + 1];
    l[0] = getSimulator().getTime();
    for (int i = 0; i < speciesNames.length; i++) {
      l[i + 1] = getSimulator().getNet().getAmountManager().getAmount(species[i]);
    }
    leap.add(l);
  }

  /**
   * Do nothing.
   */
  @Override
  public void theta(double theta) {
  }

  public GnuPlot toGnuplot() throws IOException {
    return toGnuplot(new GnuPlot());
  }

  public GnuPlot toGnuplot(GnuPlot gnuplot) throws IOException {
    if (gnuplot == null) {
      return null;
    }
    String[] names = applyLabelFormat(speciesNames);
    gnuplot.addData(new LinkedList<double[]>(leap), names, getStyles());
    return gnuplot;
  }

  /**
   * Gets the number of the performed leaps
   *
   * @return number of leaps.
   */
  public int getNumLeaps() {
    return leap.size();
  }

  public String[] getStyles() {
    return null;
  }


  @Override
  public String toString() {
    GnuPlot x = new GnuPlot();
    x.addData(leap, null, null);
    return x.getData().get(0);
  }
}
