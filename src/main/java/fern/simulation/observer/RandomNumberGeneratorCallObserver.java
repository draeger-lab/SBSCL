package fern.simulation.observer;

import java.util.HashSet;
import java.util.Set;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.tools.NumberTools;
import fern.tools.Stochastics;
import fern.tools.gnuplot.GnuPlot;

/**
 * Observes the calls to the random number generator and count the number of drawn random number of
 * different distributions. These calls are crucial for the stochastic algorithms because besides
 * algebraic calculations are these the most time consuming operations.
 * <p>
 * This observer does take repeats into account. If you repeat the simulation, you will get an
 * average over the results each run.
 *
 * @author Florian Erhard
 * @see Stochastics
 */
public class RandomNumberGeneratorCallObserver extends Observer implements GnuPlotObserver {

  private double[] numCalls = null;
  private Set<GnuPlot> addedTitles;

  /**
   * Creates the observer for the given simulator.
   *
   * @param sim simulator
   */
  public RandomNumberGeneratorCallObserver(Simulator sim) {
    super(sim);
    Stochastics.getInstance().setCountGenerations(true);
    addedTitles = new HashSet<GnuPlot>();
  }

  /**
   * Do nothing.
   */
  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
  }

  /**
   * Calculates the average.
   */
  @Override
  public void finished() {
		if (numCalls == null) {
			numCalls = NumberTools.convertIntToDouble(Stochastics.getInstance().getCounts());
		} else {
			int[] act = Stochastics.getInstance().getCounts();
			for (int i = 0; i < numCalls.length; i++) {
				numCalls[i] =
						(numCalls[i] * (double) getNumSimulations() + act[i]) / ((double) getNumSimulations()
								+ 1.0);
			}
		}

  }

  /**
   * Resets the count variables of the random number generator calls.
   *
   * @see Stochastics#resetCounts()
   */
  @Override
  public void started() {
    Stochastics.getInstance().resetCounts();
  }

  /**
   * Do nothing.
   */
  @Override
  public void step() {
  }

  /**
   * Do nothing.
   */
  @Override
  public void theta(double theta) {
  }


  public String[] getStyles() {
    return null;
  }


  public GnuPlot toGnuplot() {
    return toGnuplot(new GnuPlot());
  }

  public GnuPlot toGnuplot(GnuPlot gnuplot) {
		if (gnuplot == null) {
			return null;
		}
    if (!addedTitles.contains(gnuplot)) {
      gnuplot.addCommand(getTitlesCommand(Stochastics.getInstance().getNames()));
      addedTitles.add(gnuplot);
    }

    gnuplot.addData(new Object[]{NumberTools.getNumbersTo(numCalls.length - 1), numCalls}, true,
        applyLabelFormat(new String[1]), getStyles());
    return gnuplot;
  }


  @Override
  public String toString() {
    String[] rnds = Stochastics.getInstance().getNames();
    StringBuilder sb = new StringBuilder();
    sb.append("Random Number Generations - ");
    sb.append(getSimulator().getName() + ":\n");
    for (int i = 0; i < rnds.length; i++) {
      sb.append(rnds[i] + ": ");
      sb.append(String.format("%e ", numCalls[i]));
    }
    return sb.toString();
  }

}
