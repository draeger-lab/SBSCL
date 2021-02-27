package fern.example;

import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.IntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * The most basic example uses the famous enzyme kinetics equation by Michaelis and Menten S + E ⇌
 * ES → P to introduce fundamental loading and repeated simulation of reaction networks.
 * Furthermore, some advanced usage of the Gnuplot class is presented: Two plots are created from
 * the data of one Observer, one showing the average trend curve over all trajectories, the other
 * showing each trajectory individually. Both plots are updated after each simulation run.
 */
public class MichaelisMentenKinetic {

  public static void main(String[] args) throws IOException, JDOMException {

    /*
     * create network, simulator and observer
     */

    Network net = new FernMLNetwork(
      ExamplePath.find("mm.xml"));      // load the network from the file
    NetworkTools.dumpNetwork(
      net, new PrintWriter(System.out));    // print out the network structure

    Simulator sim = new GillespieSimple(
      net);    // create a simulator; feel free to replace GillespieSimple with other simulator classes
    IntervalObserver amount =
        new AmountIntervalObserver(
          sim, 0.1, "S", "E", "ES", "P");      // create the observer

    amount.setLabelFormat("");            // just a beautification: no titles
    sim.addObserver(amount);            // register the observer to the simulator

    /*
     * create 2 gnuplot objects, one to plot each trajectory, the other one to plot
     * the average trajectory
     */
    GnuPlot all = new GnuPlot();          // create a gnuplot object to plot the observer's data
    all.setDefaultStyle("with lines");        // beautification: set the plot style to lines
    all.addCommand("set xrange [0:10]");      // beautification: set the xrange
    all.setVisible(
      true);              // show the plot jframe - once it is visible and plot is called, the plot will be refreshed

    GnuPlot avg = new GnuPlot();          // create a gnuplot object to plot the observer's average data
    avg.setDefaultStyle("with linespoints");    // beautification: set the plot style to linespoints
    avg.addCommand("set xrange [0:10]");      // beautification: set the xrange
    avg.setVisible(
      true);              // show the plot jframe - once it is visible and plot is called, the plot will be refreshed

    /*
     * perform 50 simulation runs and plot the data interactively
     */
    for (int i = 0; i < 50; i++) {
      sim.start(10);                // start the simulation up to 10 time units
      amount.toGnuplotRecent(all);        // add the recent data to the gnuplot object
      amount.toGnuplot(avg);            // add the average data to the other gnuplot object
      //			avg.merge(all);								// merges the plots
      avg.plot();                  // invoke gnuplot and plot the data
      avg.clearData();              // clear the data (we want only the recent average trends)
      all.plot();
    }


  }

}
