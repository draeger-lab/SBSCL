package fern.example;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.Observer;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * The LacZ/LacY model of procaryotic gene expression proposed by [1] is simulated. Here, the full
 * model (containing LacZ and LacY regulation) is simulated for one full cell cycle. Since it is
 * very time consuming with an exact method, the simulation progress is shown online in the
 * console.
 *
 * <p>
 * For references see [1] Kierzek A.M., Bioinformatics 18, 670 (2002)
 *
 * @author Florian Erhard
 */
public class LacYComplete {

  public static void main(String[] args) throws IOException, JDOMException {
    final PrintStream out = args.length == 0 ? System.out : new PrintStream(args[0]);
    /*
     * Load the network and print it out.
     */
    FernMLNetwork net = new FernMLNetwork(ExamplePath.find("lacy.xml"));

    if (args.length == 0) {
      NetworkTools.dumpNetwork(net);
    }

    Simulator ssa = args.length < 2 || args[1].equals("hybrid") ?
        new HybridMaximalTimeStep(net) : new GillespieEnhanced(net);

    /*
     * Create the necessary CellGrowthObserver and one to show the progress.
     */
    final long startTime = System.currentTimeMillis();
    ssa.addObserver(new CellGrowthObserver(ssa, 2100, 0));
    ssa.addObserver(new Observer(ssa) {
      long num = 0;

      @Override
      public void activateReaction(int mu, double tau, FireType fireType, int times) {
        num += times;
      }

      @Override
      public void finished() {
      }

      @Override
      public void started() {
        setTheta(0);
      }

      @Override
      public void step() {
      }

      @Override
      public void theta(double theta) {
        out.printf(Locale.US, "%s	%4.0f	%9d	%9f\n",
            dateFormat(System.currentTimeMillis() - startTime), theta, num,
            getSimulator().getAmount(getSimulator().getNet().getSpeciesByName("product")));
        setTheta(theta + 10);
      }

    });

    AmountIntervalObserver obs = new AmountIntervalObserver(ssa, 10, "product");
    ssa.addObserver(obs);

    ssa.start(2100);

    if (args.length == 0) {
      GnuPlot gp = new GnuPlot();
      gp.setDefaultStyle("with linespoints");
      obs.toGnuplot(gp);
      gp.setVisible(true);
      gp.plot();
      gp.clearData();
    }

  }


  private static String dateFormat(long l) {
    int ms = (int) (l % 1000);
    l /= 1000;
    int s = (int) (l % 60);
    l /= 60;
    int m = (int) (l % 60);
    l /= 60;
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%03d", l) + ":");
    sb.append(String.format("%02d", m) + ":");
    sb.append(String.format("%02d", s) + ".");
    sb.append(String.format("%-3d", ms));
    return sb.toString();
  }
}
