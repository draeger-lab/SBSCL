package fern.example;

import java.io.IOException;
import java.util.Map;

import org.jdom.JDOMException;

import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.AndController;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * Demonstration of performance and accuracy differences of the different simulation algorithms. The
 * reaction network proposed in [1] are used to calculate histograms and the histogram distances [4]
 * of the amount of some molecular species at a special time point in order to compare results of
 * different algorithms. Here, Fig. 3 of [3] is reproduced and the gain of accuracy when using a
 * species bounded tau leaping procedure without loss of performance is shown.
 * <p>
 * For references see [1] Gillespie D.T., J. Comput. Phys. 22, 403 (1976), [2] D.Gillespie,
 * J.Chem.Phys. 115, 1716 (2001), [3] D.Gillespie and L.Petzold, J.Chem.Phys. 119, 8229 (2003), [4]
 * Cao Y. and Petzold L., J. Comp. Phys. 212, 6�24 (2006).
 *
 * @author Florian Erhard
 */
@SuppressWarnings("unchecked")
public class DecayingDimerizingInteractive {

  public static void main(String[] args) throws IOException, JDOMException {
    /*
     * Create the network from file, print it out and set the proposed initial parameters.
     */
    FernMLNetwork net = new FernMLNetwork(ExamplePath.find("decaydimer.xml"));
    NetworkTools.dumpNetwork(net);
    net.setInitialAmount(0, 4150);
    net.setInitialAmount(1, 39565);
    net.setInitialAmount(2, 3445);

    /*
     * Start the trajectories.
     */
    allTrajectory(net, 100000);

  }


  private static void allTrajectory(Network net, int times) throws IOException {
    /*
     * Create the simulators, gnuplot objects (for each species)
     * and histograms (for each species and each simulator)
     */
    Simulator[] sims = new Simulator[]{
        new TauLeapingAbsoluteBoundSimulator(net),
        new TauLeapingRelativeBoundSimulator(net),
        new TauLeapingSpeciesPopulationBoundSimulator(net),
        new HybridMaximalTimeStep(net),
        new GillespieEnhanced(net)
    };
    GnuPlot[] gp = new GnuPlot[3];
    for (int i = 0; i < 3; i++) {
      gp[i] = new GnuPlot();
      gp[i].setDefaultStyle("with linespoints");
    }
    Map[][] histos = new Map[sims.length][3];

    /*
     * Use each simulator to create times trajectories and store the resulting
     * histograms.
     */
    for (int s = 0; s < sims.length; s++) {
      System.out.println(sims[s].getName());
      histos[s] = trajectory(sims[s], gp, times);
      for (int i = 0; i < gp.length; i++) {
        gp[i].setVisible(true);
        gp[i].plot();
      }
      System.out.println();
    }
    System.out.println();

    /*
     * calculate the pairwise histogram distances for each histogram
     */
    for (int s = 0; s < 3; s++) {
      System.out.println("Histogram distances for S" + (s + 1) + ":");
      for (int i = 0; i < histos.length; i++) {
        for (int j = 0; j < histos.length; j++) {
          System.out
              .printf("%.4f\t", NumberTools.calculateHistogramDistance(histos[i][s], histos[j][s]));
        }
        System.out.println();
      }
    }
  }

  private static Map[] trajectory(Simulator sim, GnuPlot[] gp, int times) throws IOException {
    /*
     * Create the observers for the three different species S1, S2, S3
     */
    AmountAtMomentObserver[] obs = new AmountAtMomentObserver[3];
    for (int i = 0; i < 3; i++) {
      obs[i] = new AmountAtMomentObserver(sim, 10, "S" + (i + 1));
      obs[i].setLabelFormat("%a - %l");
      sim.addObserver(obs[i]);
    }
    /*
     * Stop the simulation after each species is recorded.
     */
    SimulationController c = new AndController(obs);

    /*
     * Perform the simulations and print a simple progressbar (one dot means 1% done)
     */
    long start = System.currentTimeMillis();
    for (int i = 1; i <= times; i++) {
      sim.start(c);
      if (i % (times / 100.0) == 0) {
        System.out.print(".");
      }
    }
    long end = System.currentTimeMillis();
    System.out.println();
    System.out.println(dateFormat(end - start));

    /*
     * Present and return the results.
     */
    for (int i = 0; i < 3; i++) {
      obs[i].toGnuplot(gp[i]);
    }
    Map[] re = new Map[obs.length];
    for (int i = 0; i < re.length; i++) {
      re[i] = obs[i].getHistogram(0);
    }
    return re;
  }

  private static String dateFormat(long l) {
    int ms = (int) (l % 1000);
    l /= 1000;
    int s = (int) (l % 60);
    l /= 60;
    int m = (int) (l % 60);
    l /= 60;
    StringBuilder sb = new StringBuilder();
    if (l > 0) {
      sb.append(l + "h ");
    }
    if (m > 0) {
      sb.append(m + "m ");
    }
    if (s > 0) {
      sb.append(s + "s ");
    }
    sb.append(ms + "ms ");
    return sb.toString();
  }
}
