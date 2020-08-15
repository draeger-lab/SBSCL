package fern.simulation.observer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * Observes the different types of firings.
 * <p>
 * If the simulation is repeated, the average numbers are calculated.
 *
 * @author Florian Erhard
 * @see FireType
 */
public class FireTypeObserver extends Observer implements GnuPlotObserver {

  private double[] numTypesAvg;
  private long[] numTypes;
  private Set<GnuPlot> addedTitles;

  /**
   * Create the observer for the given simulator.
   *
   * @param sim simulator
   */
  public FireTypeObserver(Simulator sim) {
    super(sim);
    numTypes = new long[FireType.values().length];
    numTypesAvg = new double[numTypes.length];
    addedTitles = new HashSet<GnuPlot>();
  }


  /**
   * Counts the firing for the given {@link FireType}
   */
  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
    numTypes[fireType.ordinal()] += times;
  }

  /**
   * Calculates the average over all runs of the simulator.
   */
  @Override
  public void finished() {
    for (int i = 0; i < numTypes.length; i++) {
      numTypesAvg[i] =
          (numTypesAvg[i] * getNumSimulations() + numTypes[i]) / (double) (getNumSimulations() + 1);
    }
  }

  /**
   * Do nothing.
   */
  @Override
  public void started() {
    Arrays.fill(numTypes, 0);
  }

  /**
   * Do nothing.
   */
  @Override
  public void step() {
  }

  @Override
  public String toString() {
    double sum = 0;
    for (int i = 0; i < numTypesAvg.length; i++) {
      sum += numTypesAvg[i];
    }

    StringBuilder sb = new StringBuilder();
    sb.append(getSimulator().getName());
    sb.append(":\n");
    for (int i = 0; i < numTypesAvg.length; i++) {
      sb.append(String
          .format("%s %d (%.4f) ", FireType.values()[i].toString(), (long) numTypesAvg[i],
              numTypesAvg[i] / sum));
    }
    return sb.toString();
  }


  /**
   * Do nothing.
   */
  @Override
  public void theta(double theta) {
  }

  public GnuPlot toGnuplot() {
    return toGnuplot(new GnuPlot());
  }

  public GnuPlot toGnuplot(GnuPlot gnuplot) {
    if (gnuplot == null) {
      return null;
    }
    if (!addedTitles.contains(gnuplot)) {
      gnuplot.addCommand(getTitlesCommand(getTitles()));
      addedTitles.add(gnuplot);
    }

    gnuplot
        .addData(new Object[]{NumberTools.getNumbersTo(numTypesAvg.length - 1), numTypesAvg}, true,
            applyLabelFormat(new String[1]), getStyles());
    return gnuplot;
  }

  private String[] getTitles() {
    FireType[] val = FireType.values();
    String[] re = new String[val.length];
    for (int i = 0; i < re.length; i++) {
      re[i] = val[i].toString();
    }
    return re;
  }

  public String[] getStyles() {
    return null;
  }


}
