package fern.benchmark;

import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.algorithm.CompositionRejection;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.Observer;
import fern.tools.NumberTools;

/**
 * Measures the performance of the different simulation algorithms for a given network. This is the
 * base class for different benchmarks and manages the simulators, the iteration of the simulators
 * and when results have to be presented. You just have to implement
 * <code>present</code> and <code>getController</code> and add some {@link Observer}s to the
 * <code>simulators</code>.
 *
 * @author Florian Erhard
 */
public abstract class SimulatorPerformance extends Benchmark {

  /**
   * Contains the <code>Simulator</code>s - use this field to attach <code>Observer</code>s.
   */
  protected Simulator[] simulators;
  /**
   * Contains the names of the simulators for using in the <code>present</code>-method of extending
   * classes.
   */
  protected String[] simulatorNames;
  /**
   * Contains the number of iterations done for using in the <code>present</code>-method of
   * extending classes.
   */
  protected int count = 0;

  private int showSteps = 100;
  private int[] indices;

  /**
   * Registers the six built-in simulators for the performance benchmarks.
   *
   * @param net the network to benchmark
   * @see GillespieSimple
   * @see GillespieEnhanced
   * @see GibsonBruckSimulator
   * @see TauLeapingAbsoluteBoundSimulator
   * @see TauLeapingRelativeBoundSimulator
   * @see TauLeapingSpeciesPopulationBoundSimulator
   */
  public SimulatorPerformance(Network net) {
    simulators = new Simulator[]{
        new GillespieSimple(net),
        new GillespieEnhanced(net),
        new GibsonBruckSimulator(net),
        new TauLeapingAbsoluteBoundSimulator(net),
        new TauLeapingRelativeBoundSimulator(net),
        new TauLeapingSpeciesPopulationBoundSimulator(net),
        new HybridMaximalTimeStep(net),
        new CompositionRejection(net)
    };

    simulatorNames = new String[simulators.length];
    for (int i = 0; i < simulatorNames.length; i++) {
      simulatorNames[i] = simulators[i].getName();
    }

    indices = new int[simulators.length];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = i;
    }
  }

  /**
   * Performs one benchmark for each of the six simulators. To minimize effects that could
   * compromise time benchmarks like caching, the order of the simulators is randomized each time.
   * The time needed for each algorithm is recorded and added to <code>Benchmark</code>'s data pool.
   * After each {@link SimulatorPerformance#getShowSteps()} iterations, <code>present</code> is
   * called.
   *
   * @see Benchmark#addData(double[])
   */
  public void benchmark() {

    NumberTools.shuffle(indices);

    double[] d = new double[simulators.length];
    for (int i = 0; i < indices.length; i++) {
      start();
      if (simulators[indices[i]] != null) {
        simulators[indices[i]].start(getController(indices[i]));
      }
      d[indices[i]] = end() * Math.pow(10, -9);
    }
    addData(d);

    count++;

    if (count % showSteps == 0) {
      present();
    }
  }


  /**
   * Gets the number of iterations between two <code>present</code>-calls.
   *
   * @return the showSteps
   */
  public int getShowSteps() {
    return showSteps;
  }

  /**
   * Sets the number of iterations between two <code>present</code>-calls.
   *
   * @param showSteps the showSteps to set
   */
  public void setShowSteps(int showSteps) {
    this.showSteps = showSteps;
  }

  /**
   * Gets the simulators used by this benchmark.
   *
   * @return simulators.
   */
  public Simulator[] getSimulators() {
    return simulators;
  }

  /**
   * Extending classes have to determine the {@link SimulationController} of each
   * <code>Simulator</code> here.
   *
   * @param i index of the simulator
   * @return a <code>SimulationController</code> for the ith simulator
   */
  protected abstract SimulationController getController(int i);


  /**
   * Is called after <code>getShowSteps</code> iterations. Implement your benchmark presentation
   * here.
   */
  protected abstract void present();


}
