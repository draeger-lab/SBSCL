package fern.example;

import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.CompositionRejection;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.controller.AmountLowerThanController;
import fern.simulation.controller.AndController;
import fern.simulation.controller.DefaultController;
import fern.simulation.controller.OrController;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.IntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * The most basic example uses the famous enzyme kinetics equation by Michaelis and Menten S + E <->
 * ES -> P to introduce fundamental loading and repeated simulation of reaction networks.
 * Furthermore, some advanced usage of the Gnuplot class is presented: Two plots are created from
 * the data of one Observer, one showing the average trend curve over all trajectories, the other
 * showing each trajectory individually. Both plots are updated after each simulation run.
 */
public class Mapk {

  public static void main(String[] args) throws IOException, JDOMException {

    /*
     * create network, simulator and observer
     */

    Network net =
        new FernMLNetwork(ExamplePath.find("mapk.xml"));      // load the network from the file
    NetworkTools
        .dumpNetwork(net, new PrintWriter(System.out));    // print out the network structure

    Simulator sim = new GillespieEnhanced(
        net);    // create a simulator; feel free to replace GillespieSimple with other simulator classes
    IntervalObserver amount =
        new AmountIntervalObserver(sim, 10, "E0*", "E1*", "E2*", "E3*", "E4*",
            "E5*");      // create the observer

    amount.setLabelFormat("");            // just a beautification: no titles
    sim.addObserver(amount);            // register the observer to the simulator

    /*
     * create 2 gnuplot objects, one to plot each trajectory, the other one to plot
     * the average trajectory
     */
    GnuPlot avg =
        new GnuPlot();          // create a gnuplot object to plot the observer's average data
    avg.setDefaultStyle(
        "with linespoints");    // beautification: set the plot style to linespoints
    avg.addCommand("set xrange [0:800]");      // beautification: set the xrange
    avg.setVisible(
        true);              // show the plot jframe - once it is visible and plot is called, the plot will be refreshed

    SimulationController amountController = new AmountLowerThanController(25, "E5*");
    SimulationController timeController = new DefaultController(800);
    /*
     * perform 50 simulation runs and plot the data interactively
     */
    for (int i = 0; i < 100; i++) {
      sim.start(new OrController(amountController,
          timeController));              // start the simulation up to 10 time units
      amount.toGnuplot(avg);            // add the average data to the other gnuplot object
      avg.plot();                  // invoke gnuplot and plot the data
      avg.clearData();              // clear the data (we want only the recent average trends)
    }


  }

}
