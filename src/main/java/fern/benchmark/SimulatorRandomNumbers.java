package fern.benchmark;

import java.io.IOException;

import fern.network.Network;
import fern.simulation.controller.DefaultController;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.RandomNumberGeneratorCallObserver;
import fern.tools.gnuplot.GnuPlot;

/**
 * Check the number of random number creations of different distributions for a given net. The
 * random number creations are essential for the performance of the simulation algorithms. The
 * <code>benchmark</code> method can be invoked repeatedly to calculate the average over many
 * simulations.
 *
 * @author Florian Erhard
 */
public class SimulatorRandomNumbers extends SimulatorTime {

  private RandomNumberGeneratorCallObserver[] obs;
  private GnuPlot gnuplotRandom;
  private SimulationController controller;

  /**
   * Create the benchmark and defines the time each simulator has to run in one iteration.
   *
   * @param net  the network to benchmark
   * @param time running time for the simulators
   */
  public SimulatorRandomNumbers(Network net, double time) {
    super(net, time);
    this.controller = new DefaultController(time);

    obs = new RandomNumberGeneratorCallObserver[simulators.length];
    for (int i = 0; i < obs.length; i++) {
      obs[i] = new RandomNumberGeneratorCallObserver(simulators[i]);
      simulators[i].addObserver(obs[i]);
      obs[i].setLabelFormat("%a");
    }
    gnuplotRandom = new GnuPlot();

  }

  /**
   * Returns the {@link SimulationController} for the base class.
   *
   * @param i index of the <code>Simulator</code>
   * @return a <code>SimulationController</code> for the ith <code>Simulator</code>
   */
  @Override
  protected SimulationController getController(int i) {
    return controller;
  }

  /**
   * Present results of this benchmark is gnuplot and text to stdout.
   */
  @Override
  public void present() {
    super.present();

    for (int i = 0; i < obs.length; i++) {
      if (simulators[i] != null) {
        obs[i].toGnuplot(gnuplotRandom);
        System.out.println(obs[i].toString());
      }
    }

    gnuplotRandom.setVisible(true);
    try {
      gnuplotRandom.plot();
    } catch (IOException e) {
    }
    gnuplotRandom.clearData();
  }


}
