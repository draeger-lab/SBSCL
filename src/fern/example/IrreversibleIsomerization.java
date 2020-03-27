package fern.example;

import java.io.IOException;
import java.util.Locale;

import org.jdom.JDOMException;

import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.LeapObserver;
import fern.tools.gnuplot.GnuPlot;

/**
 * Uses the Irreversible-isomerization model to show effects of different
 * choices for epsilon. The number of leaps is slightly greater than proposed
 * in the paper because in the paper tau wasn't bound by sigma yet (yielding 
 * sometimes to lower tau and hence more leaps)
 * <p>
 * For references see D.Gillespie, J.Chem.Phys. 115, 1716 (2001)
 * 
 * @author Florian Erhard
 *
 */
public class IrreversibleIsomerization {

	public static void main(String[] args) throws IOException, JDOMException {
		double endTime = 12.67; // as proposed in the paper
		
		/*
		 * Create the network and the gnuplot
		 */
		Network net = new FernMLNetwork(ExamplePath.find("isomerization.xml"));
		GnuPlot gp = new GnuPlot();
		gp.addCommand("set xrange [0:5]");
		gp.setVisible(true);
		
		/*
		 * First create a line in the plot representing the exact SSA done by a 
		 * GibsonBruckSimulator
		 */
		Simulator ssa = new GibsonBruckSimulator(net);
		AmountIntervalObserver amount = (AmountIntervalObserver) ssa.addObserver(new AmountIntervalObserver(ssa,0.01, "S1"));
		ssa.start(endTime);
		amount.setLabelFormat("exact SSA");
		amount.toGnuplot(gp);
		gp.getAxes().get(0).setStyle(1, "with lines");
		gp.plot();
		
		/*
		 * Now create a tau leap simulator and perform the simulation for different values
		 * of epsilon. Feel free to try different Tau leaping procedures.
		 */
		AbstractBaseTauLeaping sim = new TauLeapingAbsoluteBoundSimulator(net);
		sim.setUseSimpleFactor(0);
		LeapObserver obs = (LeapObserver) sim.addObserver(new LeapObserver(sim,"S1"));
		
		for (double eps : new double[] {0.03, 0.15, 0.5}) {
			sim.setEpsilon(eps);
			sim.start(endTime);
			obs.setLabelFormat(String.format(Locale.US,"%.2f",eps));
			obs.toGnuplot(gp);
			gp.plot();
			System.out.println("Number of leaps for eps="+eps+": "+obs.getNumLeaps());
		}
	}

}
