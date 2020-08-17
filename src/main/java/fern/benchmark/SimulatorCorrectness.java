package fern.benchmark;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.tools.NumberTools;
import fern.tools.ConfigReader;
import fern.tools.gnuplot.GnuPlot;

/**
 * Benchmark the correctness of the built-in simulators for a given net. For this net and a
 * specified moment in time, the average histogram distance is calculated for given species of the
 * network. Additionally the histograms are plotted. The <code>benchmark</code> method can be
 * invoked repeatedly to calculate the average over many simulations.
 * <p>
 * For references, see Yang Cao, Linda Petzold, Accuracy limitations and the measurement of errors
 * in the stochastic simulation of chemically reacting systems, Journal of Computational Physics 212
 * (2006) 6�24.
 *
 * @author Florian Erhard
 */
public class SimulatorCorrectness extends SimulatorPerformance {

  private AmountAtMomentObserver[] obs;
  private GnuPlot gnuplot;
  private String[] speciesNames;

  /**
   * Creates the benchmark instance with given network, moment in time and species.
   *
   * @param net          the network to benchmark
   * @param moment       the moment in time at which the amounts are to be measured
   * @param speciesNames the names of the species of which the amounts are to be measured
   */
  public SimulatorCorrectness(Network net, double moment, String... speciesNames) {
    super(net);

    this.speciesNames = speciesNames;

    obs = new AmountAtMomentObserver[simulators.length];
    for (int i = 0; i < obs.length; i++) {
      obs[i] = new AmountAtMomentObserver(simulators[i], moment, speciesNames);
      simulators[i].addObserver(obs[i]);
      obs[i].setLabelFormat("%l - %a");
    }

    gnuplot = new GnuPlot();
    gnuplot.setDefaultStyle("with linespoints");
  }

  /**
   * Returns the {@link SimulationController} for the base class.
   *
   * @param i index of the <code>Simulator</code>
   * @return a <code>SimulationController</code> for the ith <code>Simulator</code>
   */
  @Override
  protected SimulationController getController(int i) {
    return obs[i];
  }

  /**
   * Gets called after some iterations of the method <code>benchmark</code> in the base class. A
   * gnuplot is created containing histograms for each species and simulator and the histogram
   * distances for the simulators is printed to stdout
   *
   * @see SimulatorPerformance#setShowSteps
   */
  @Override
  public void present() {
    gnuplot.setVisible(true);
    try {
      for (int j = 0; j < obs.length; j++) {
        if (simulators[j] != null) {
          obs[j].toGnuplot(gnuplot);
        }
      }
      gnuplot.plot();
    } catch (IOException e) {
    }
    gnuplot.clearData();

    String format = speciesNames.length > 1 ? "%.3f|%.3f\t" : "%.3f\t";
    double[][][] histogramDistance = calcHistoDistances();
    for (int i = 0; i < histogramDistance.length; i++) {
      for (int j = 0; j < histogramDistance[i].length; j++) {
        System.out.printf(format, NumberTools.avg(histogramDistance[i][j]),
            NumberTools.stddev(histogramDistance[i][j]));
      }
      System.out.println();
    }
    System.out.println();
  }

  private double[][][] calcHistoDistances() {
    double[][][] re = new double[simulators.length][simulators.length][speciesNames.length];
    for (int i = 0; i < re.length; i++) {
      for (int j = 0; j < re[i].length; j++) {
        for (int s = 0; s < speciesNames.length; s++) {
          re[i][j][s] = NumberTools
              .calculateHistogramDistance(obs[i].getHistogram(s), obs[j].getHistogram(s));
        }
      }
    }
    return re;
  }

  public static void main(String[] args) throws IOException, JDOMException {
    ConfigReader cfg = new ConfigReader("test/configs/s3.cfg");

    SimulatorPerformance sp = new SimulatorCorrectness(new FernMLNetwork(
        new File(cfg.getAsString("file"))), cfg.getAsDouble("moment"),
        cfg.getAsStringArr("species"));
    while (true) {
      sp.benchmark();
    }
  }


}
