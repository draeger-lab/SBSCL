package fern.benchmark;

import java.io.IOException;

import fern.network.Network;
import fern.simulation.controller.DefaultController;
import fern.simulation.controller.SimulationController;
import fern.tools.gnuplot.GnuPlot;

public class SimulatorTime extends SimulatorPerformance {

  private GnuPlot gnuplotTime;
  private SimulationController controller;

  /**
   * Create the benchmark and defines the time each simulator has to run in one iteration.
   *
   * @param net  the network to benchmark
   * @param time running time for the simulators
   */
  public SimulatorTime(Network net, double time) {
    super(net);

    this.controller = new DefaultController(time);
    gnuplotTime = new GnuPlot();
    gnuplotTime.setDefaultStyle("with boxes");
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

    toGnuPlotAsHistogram(gnuplotTime, simulatorNames, null);
    gnuplotTime.setVisible(true);
    try {
      gnuplotTime.plot();
    } catch (IOException e) {
    }
    gnuplotTime.clearData();
  }


}
