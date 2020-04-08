package fern.example;

import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.tools.NumberTools;
/**
 * The LacZ/LacY model of procaryotic gene expression proposed by [1]
 * is simulated. This reproduces the values [2] Fig. 4, which represent
 * histogram distances of the different procedures regarding the simulation
 * of time 1000 to 1001 of the cell cycle.
 *
 * <p>
 * For references see 
 * [1] Kierzek A.M., Bioinformatics 18, 670 (2002) and
 * [2] Cao Y., J. Chem. Phys. 124, 044109 (2006).
 * 
 * @author Florian Erhard
 *
 */

public class LacYHistogramDistances {

	public static void main(String[] args) throws IOException, JDOMException {
		String species = "LacZlactose";
		int runs = 100000;
		double time = 1;
		/*
		 * Create the network from file.
		 */
		FernMLNetwork net = new FernMLNetwork(ExamplePath.find("lacy1000.xml"));
	
		/*
		 * Create and set up simulators
		 */
		Simulator tla = new TauLeapingAbsoluteBoundSimulator(net);
		Simulator tlr = new TauLeapingRelativeBoundSimulator(net);
		Simulator tls = new TauLeapingSpeciesPopulationBoundSimulator(net);
		Simulator exact = new GillespieSimple(net);
		tla.addObserver(new CellGrowthObserver(tla,2100,1000));
		tlr.addObserver(new CellGrowthObserver(tlr,2100,1000));
		tls.addObserver(new CellGrowthObserver(tls,2100,1000));
		exact.addObserver(new CellGrowthObserver(exact,2100,1000));
		
		/*
		 * Create test sets.
		 */
		HistogramDistanceTestSet ssaSet = new HistogramDistanceTestSet(exact,0,runs,time,species);
		HistogramDistanceTestSet[][] tauLeapingSets = new HistogramDistanceTestSet[3][];
		double[] absEps = new double[] {0.01,0.015,0.02,0.025,0.03,0.035,0.040,0.045,0.05};
		double[] othEps = new double[] {0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1};
		tauLeapingSets[0] = new HistogramDistanceTestSet[absEps.length];
		tauLeapingSets[1] = new HistogramDistanceTestSet[othEps.length];
		tauLeapingSets[2] = new HistogramDistanceTestSet[othEps.length];
		for (int i=0; i<absEps.length; i++) 
			tauLeapingSets[0][i] = new HistogramDistanceTestSet(tla,absEps[i],runs,time,species);
		for (int i=0; i<othEps.length; i++) 
			tauLeapingSets[1][i] = new HistogramDistanceTestSet(tlr,othEps[i],runs,time,species);
		for (int i=0; i<othEps.length; i++) 
			tauLeapingSets[2][i] = new HistogramDistanceTestSet(tls,othEps[i],runs,time,species);
		
		/*
		 * Calculate / read histograms.
		 */
		ssaSet.createHistogram();
		for (int i=0; i<tauLeapingSets.length; i++) 
			for (int j=0; j<tauLeapingSets[i].length; j++) 
				tauLeapingSets[i][j].createHistogram();

		/*
		 * Calculate histogram distances.
		 */
		System.out.println("Histogram distances:");
		for (int i=0; i<tauLeapingSets.length; i++) { 
			for (int j=0; j<tauLeapingSets[i].length; j++) 
				System.out.println(tauLeapingSets[i][j].getSimulator().getName()+" Eps="+tauLeapingSets[i][j].getEpsilon()+": "+NumberTools.calculateHistogramDistance(ssaSet.getHistogram(), tauLeapingSets[i][j].getHistogram()));
			System.out.println();
		}
		
	}
	
}
