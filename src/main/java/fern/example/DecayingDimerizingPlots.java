package fern.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * Demonstration of performance and accuracy differences of the different 
 * simulation algorithms. The reaction network proposed in [1] 
 * are used to calculate histograms and the histogram distances [4] of the amount
 * of some molecular species at a special time point in order to compare results 
 * of different algorithms. Here, Fig. 3 of [3] is reproduced and the gain 
 * of accuracy when using a species bounded tau leaping procedure without 
 * loss of performance is shown. 
 * <p>
 * For references see
 * [1] Gillespie D.T., J. Comput. Phys. 22, 403 (1976), 
 * [2] D.Gillespie, J.Chem.Phys. 115, 1716 (2001),
 * [3] D.Gillespie and L.Petzold, J.Chem.Phys. 119, 8229 (2003),
 * [4] Cao Y. and Petzold L., J. Comp. Phys. 212, 6–24 (2006).
 */
@Deprecated
public class DecayingDimerizingPlots {

	public static void main(String[] args) throws IOException, JDOMException {
		/*
		 * Create the network from file, print it out and set the proposed initial parameters.
		 */
		FernMLNetwork net = new FernMLNetwork(ExamplePath.find("decaydimer.xml"));
		net.setInitialAmount(0, 4150);
		net.setInitialAmount(1, 39565);
		net.setInitialAmount(2, 3445);
		
		/*
		 * Create the histograms (very time consuming) or load them from files
		 */
		Map<Integer,Integer> ssaHisto;
		Map<Integer,Integer> tlaHisto;
		Map<Integer,Integer> tlrHisto;
		Map<Integer,Integer> tlsHisto;
		if (ExamplePath.exists("decayssa.hist")) {
			 ssaHisto = NumberTools.loadHistogram(ExamplePath.find("decayssa.hist"));
		} else {
			ssaHisto = createHisto(new GibsonBruckSimulator(net), 100000);
			NumberTools.saveHistogram(ssaHisto, new File("decayssa.hist"));
		}
		
		if (ExamplePath.exists("decaytla.hist")) {
			tlaHisto = NumberTools.loadHistogram(ExamplePath.find("decaytla.hist"));
		} else {
			tlaHisto = createHisto(new TauLeapingAbsoluteBoundSimulator(net), 100000);
			NumberTools.saveHistogram(tlaHisto, new File("decaytla.hist"));
		}
		
		if (ExamplePath.exists("decaytlr.hist")) {
			tlrHisto = NumberTools.loadHistogram(ExamplePath.find("decaytlr.hist"));
		} else {
			tlrHisto = createHisto(new TauLeapingRelativeBoundSimulator(net), 100000);
			NumberTools.saveHistogram(tlrHisto, new File("decaytlr.hist"));
		}
		if (ExamplePath.exists("decaytls.hist")) {
			tlsHisto = NumberTools.loadHistogram(ExamplePath.find("decaytls.hist"));
		} else {
			tlsHisto = createHisto(new TauLeapingSpeciesPopulationBoundSimulator(net), 100000);
			NumberTools.saveHistogram(tlsHisto, new File("decaytls.hist"));
		}
		
		
		/*
		 * Plot the histograms into one gnuplot object.
		 */
		GnuPlot gp = new GnuPlot();
		gp.addData(getHistoAsParallelArray(ssaHisto), new String[] {"exact SSA"}, new String[] {"with linespoints"});
		gp.addData(getHistoAsParallelArray(tlaHisto), new String[] {"Absolute Bound 0.03"}, new String[] {"with linespoints"});
		gp.addData(getHistoAsParallelArray(tlrHisto), new String[] {"Relative Bound 0.03"}, new String[] {"with linespoints"});
		gp.addData(getHistoAsParallelArray(tlsHisto), new String[] {"Species Bound 0.03"}, new String[] {"with linespoints"});
		gp.setVisible(true);
		gp.plot();
		
		/*
		 * Calculate the histogram distances.
		 */
		System.out.println("Histogram distance exact SSA - ");
		System.out.println("Absolute Bound: "+NumberTools.calculateHistogramDistance(ssaHisto, tlaHisto));
		System.out.println("Relative Bound: "+NumberTools.calculateHistogramDistance(ssaHisto, tlrHisto));
		System.out.println("Species Bound:  "+NumberTools.calculateHistogramDistance(ssaHisto, tlsHisto));
		System.out.println("(each eps=0.03)");
	}
	
	private static double[][] getHistoAsParallelArray(Map<Integer,Integer> histo) throws IOException {
		int max = NumberTools.max(histo.keySet());
		int min = NumberTools.min(histo.keySet());
		
		double[][] data = new double[max-min+1][2];
		for (int i=min; i<=max; i++) {
			data[i-min][0] = i;
			data[i-min][1] = histo.containsKey(i) ? (double)histo.get(i)/100000.0 : 0.0;
		}
		return data;
	}

	@SuppressWarnings("unused")
	private static Map<Integer, Integer> createHisto(Simulator sim, int runs) {
		AmountAtMomentObserver obs = new AmountAtMomentObserver(sim,10,"S1");
		sim.addObserver(obs);
		
		for (int i=1; i<=runs; i++) {
			sim.start(obs);
			if (i%(runs/1000.0)==0)
				System.out.print(".");
			if (i%(runs/10.0)==0)
				System.out.println();
		}
		return obs.getHistogram(0);
	}

}
