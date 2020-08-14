package fern.example;

import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * The LacZ/LacY model of procaryotic gene expression proposed by [1] is simulated. Here, Fig. 1A of
 * [1] is reproduced, which shows the amount of LacZ over several cell cycles.
 *
 * <p>
 * For references see [1] Kierzek A.M., Bioinformatics 18, 670 (2002)
 *
 * @author Florian Erhard
 */
public class LacZ {

  public static void main(String[] args) throws IOException, JDOMException {
    /*
     * Load the network and print it out.
     */
    FernMLNetwork net = new FernMLNetwork(ExamplePath.find("lacz.xml"));
    NetworkTools.dumpNetwork(net);

    /*
     * Simulate it with an CellGrowthObserver which adjusts the volume accordingly
     * to the cell growth of E.Coli. Feel free to try other simulators!
     */
    Simulator ssa = new GillespieSimple(net);
    AmountIntervalObserver obs = new AmountIntervalObserver(ssa, 10, "LacZ");
    ssa.addObserver(obs);
    ssa.addObserver(new CellGrowthObserver(ssa, 2100, 0));

    GnuPlot gp = new GnuPlot();
    gp.setDefaultStyle("with linespoints");
    for (int i = 0; i < 1000; i++) {
      ssa.start(10 * 35 * 60);
      obs.toGnuplot(gp);
      gp.setVisible(true);
      gp.plot();
      gp.clearData();
    }


  }

}
