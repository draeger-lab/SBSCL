package fern.example;

import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.tools.NumberTools;

/**
 * Demonstration of performance and accuracy differences of the different simulation algorithms. The
 * reaction network proposed in [1] are used to calculate histograms and the histogram distances [4]
 * of the amount of some molecular species at a special time point in order to compare results of
 * different algorithms. This reproduces the values of Fig. 10 of [3]. Running it the first time
 * will cost some time, the results are saved and then loaded in further runs.
 * <p>
 * For references see [1] Gillespie D.T., J. Comput. Phys. 22, 403 (1976), [2] D.Gillespie,
 * J.Chem.Phys. 115, 1716 (2001), [3] Cao Y., J. Chem. Phys. 124, 044109 (2006) , [4] Cao Y. and
 * Petzold L., J. Comp. Phys. 212, 6�24 (2006).
 *
 * @author Florian Erhard
 */
public class DecayingDimerizingHistogramDistances {

  public static void main(String[] args) throws IOException, JDOMException {
    String species = "S1";
    int runs = 100000;
    double time = 10;

    /*
     * Create the network from file, print it out and set the proposed initial parameters.
     */
    FernMLNetwork net = new FernMLNetwork(ExamplePath.find("decaydimer.xml"));
    net.setInitialAmount(0, 4150);
    net.setInitialAmount(1, 39565);
    net.setInitialAmount(2, 3445);

    /*
     * Set up the simulators
     */
    Simulator tla = new TauLeapingAbsoluteBoundSimulator(net);
    Simulator tlr = new TauLeapingRelativeBoundSimulator(net);
    Simulator tls = new TauLeapingSpeciesPopulationBoundSimulator(net);

    /*
     * Create the test sets
     */
    HistogramDistanceTestSet ssaSet = new HistogramDistanceTestSet(new GibsonBruckSimulator(net), 0,
        runs, time, species);
    HistogramDistanceTestSet[][] tauLeapingSets = new HistogramDistanceTestSet[3][];
    double[] absEps = new double[]{0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04};
    double[] othEps = new double[]{0.03, 0.04, 0.05, 0.06, 0.07, 0.08};
    tauLeapingSets[0] = new HistogramDistanceTestSet[absEps.length];
    tauLeapingSets[1] = new HistogramDistanceTestSet[othEps.length];
    tauLeapingSets[2] = new HistogramDistanceTestSet[othEps.length];
    for (int i = 0; i < absEps.length; i++) {
      tauLeapingSets[0][i] = new HistogramDistanceTestSet(tla, absEps[i], runs, time, species);
    }
    for (int i = 0; i < othEps.length; i++) {
      tauLeapingSets[1][i] = new HistogramDistanceTestSet(tlr, othEps[i], runs, time, species);
    }
    for (int i = 0; i < othEps.length; i++) {
      tauLeapingSets[2][i] = new HistogramDistanceTestSet(tls, othEps[i], runs, time, species);
    }

    /*
     * Create / read the histograms.
     */
    ssaSet.createHistogram();
    for (int i = 0; i < tauLeapingSets.length; i++) {
      for (int j = 0; j < tauLeapingSets[i].length; j++) {
        tauLeapingSets[i][j].createHistogram();
      }
    }

    /*
     * Calculate histogram distances
     */
    System.out.println("Histogram distances:");
    for (int i = 0; i < tauLeapingSets.length; i++) {
      for (int j = 0; j < tauLeapingSets[i].length; j++) {
        System.out.println(
            tauLeapingSets[i][j].getSimulator().getName() + " Eps=" + tauLeapingSets[i][j]
                .getEpsilon() + ": " + NumberTools.calculateHistogramDistance(ssaSet.getHistogram(),
                tauLeapingSets[i][j].getHistogram()));
      }
      System.out.println();
    }
  }

}
