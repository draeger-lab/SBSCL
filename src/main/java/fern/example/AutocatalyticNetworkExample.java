package fern.example;

import java.io.IOException;

import fern.analysis.AutocatalyticNetworkDetection;
import fern.analysis.ShortestPath;
import fern.analysis.ShortestPath.Path;
import fern.network.Network;
import fern.network.creation.AutocatalyticNetwork;
import fern.network.modification.CatalysedNetwork;
import fern.network.modification.ExtractSubNetwork;
import fern.network.modification.ReversibleNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.IntervalObserver;
import fern.simulation.observer.ReactionIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.Stochastics;
import fern.tools.functions.Probability;
import fern.tools.gnuplot.GnuPlot;

/**
 * Here, the evolution of a reaction network as proposed by [1] 
 * is performed. Then, the autocatalytic subset [1] is determined and extracted. 
 * This subnet is then simulated to examine the dynamic behaviour of autocatalytic 
 * reaction networks. For more information about the evolution and the detection algorithm, 
 * please refer {@link AutocatalyticNetwork} and {@link AutocatalyticNetworkDetection}.
 * <p>
 * References:
 * [1] Kauffmann S.A, The Origins of Order: Self-Organization and Selection in Evolution. New York: Oxford University Press, (1993)
 * 
 * @author Florian Erhard
 *
 */
public class AutocatalyticNetworkExample {

	public static void main(String[] args) throws IOException {

		
		Stochastics.getInstance().setSeed(1176374877921L);
		
		/*
		 * Create the autocatalytic network.
		 */
		AutocatalyticNetwork net = new AutocatalyticNetwork(
				new char[] {'A','B'},
				new Probability.ReactionProbability(1,0),
				new Probability.Constant(0.1),
				10
				);
		Network netR = new ReversibleNetwork(net, net.getReversePropensityCalculator());
		
		/*
		 * Detect the autocatalytic subset, annotate it and print it out
		 */
		AutocatalyticNetworkDetection detection = new AutocatalyticNetworkDetection(netR);
		detection.detect();
		detection.annotate("Autocatalytic", "yes");
		NetworkTools.dumpNetwork(netR);
		
		/*
		 * Extract the autocatalytic subnet and print it out.
		 * Then compute the shortest paths from the monomers to 
		 * each other species and print the paths.
		 */
		Network autoNet = new ExtractSubNetwork(netR,detection.getAutocatalyticReactions(), detection.getAutocatalyticSpecies());
		System.out.println();
		System.out.println("Autocatalytic Subnet");
		NetworkTools.dumpNetwork(autoNet);
		ShortestPath sp = new ShortestPath(autoNet);
		for (Path p : sp.computePaths("A","B"))
				System.out.println(p);
		
		/*
		 * Create a network that can be simulated and print it out.
		 */
		Network cataNet = new CatalysedNetwork(netR);
		System.out.println();
		System.out.println("Catalysed Net");
		NetworkTools.dumpNetwork(cataNet);
		
		System.out.println("Seed: "+Stochastics.getInstance().getSeed()+"L");
		Stochastics.getInstance().resetSeed();
		
		/*
		 * Simulate the network and show the trend of each species and of the
		 * firings of reactions in two different plots.
		 */
		Simulator sim = new GillespieSimple(cataNet);
		ReactionIntervalObserver obs = new AutocatalyticReactionsAllObserver(sim,1);
		IntervalObserver obs2 = new AmountIntervalObserver(sim,1,NumberTools.getNumbersTo(sim.getNet().getNumSpecies()-1));
		sim.addObserver(obs);
		sim.addObserver(obs2);
		
		GnuPlot gp = new GnuPlot();
		gp.setDefaultStyle("with lines");
		GnuPlot gp2 = new GnuPlot();
		gp2.setDefaultStyle("with lines");
		
		
		sim.start(200);
		obs.toGnuplot(gp);
		gp.setVisible(true);
		gp.plot();
		gp.clearData();
		obs2.toGnuplot(gp2);
		gp2.setVisible(true);
		gp2.plot();
		gp2.clearData();
		
		/*
		 * Print the final values of firings.
		 */
		obs.setLabelFormat("%l");
		System.out.println(obs.toString());
	}

	
	private static class AutocatalyticReactionsAllObserver extends ReactionIntervalObserver {
		
		public AutocatalyticReactionsAllObserver(Simulator sim, double interval) {
			super(sim, interval, NetworkTools.getSpeciesNames(sim.getNet(), NumberTools.getNumbersTo(sim.getNet().getNumSpecies()-1)));
			setLabelFormat("");
		}
		
		public String[] getStyles() {
			String[] styles = new String[getSimulator().getNet().getNumReactions()];
			for (int i=0; i<styles.length; i++)
				styles[i] = "with lines lt "+getReactionColorSpec(i);
			return styles;
			
		}
		
		private int getReactionColorSpec(int r) {
			if (getSimulator().getNet().getAnnotationManager().containsReactionAnnotation(r, "Autocatalytic"))
				return 1;
			else if (getSimulator().getNet().getAnnotationManager().containsReactionAnnotation(r, "Catalysts"))
				return 2;
			else 
				return 3;
		}
		
		
		
	}
}
