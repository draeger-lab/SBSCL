package fern.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.tools.NumberTools;

/**
 * Encapsulate test sets for histogram distance calculations.
 * 
 * @author Florian Erhard
 *
 */
public class HistogramDistanceTestSet {

	private Map<Integer,Integer> histogram;
	private double epsilon;
	private Simulator simulator;
	private int runs;
	private String species;
	private AmountAtMomentObserver observer;

	public HistogramDistanceTestSet(Simulator sim, double eps, int runs, double time, String species) {
		this.simulator = sim;
		this.species = species;
		this.runs = runs;
		this.epsilon = eps;
		observer = new AmountAtMomentObserver(sim,time,species);
		sim.addObserver(observer);
	}

	public void createHistogram() throws IOException {
		if (getFile().exists()) { 
			System.out.println("Load histogram from file "+getFile());
			histogram = NumberTools.loadHistogram(getFile());
		}
		else {
			if (simulator instanceof AbstractBaseTauLeaping)
				((AbstractBaseTauLeaping) simulator).setEpsilon(epsilon);
			
			System.out.println("Calculate histogram");
			for (int i=1; i<=runs; i++) {
				simulator.start(observer);
				if (i%(runs/1000.0)==0)
					System.out.print(".");
				if (i%(runs/10.0)==0)
					System.out.println();
			}
			histogram = observer.getHistogram(0);
			System.out.println("Done and saved to "+getFile());
			NumberTools.saveHistogram(histogram, getFile());
		}
	}

	public double[][] getHistoAsParallelArray(){
		int max = NumberTools.max(histogram.keySet());
		int min = NumberTools.min(histogram.keySet());
		
		double[][] data = new double[max-min+1][2];
		for (int i=min; i<=max; i++) {
			data[i-min][0] = i;
			data[i-min][1] = histogram.containsKey(i) ? (double)histogram.get(i)/runs : 0.0;
		}
		return data;
	}

	public File getFile() {
		String fn = simulator.getNet().getName()+"_"+
		simulator.getName()+"_eps"+epsilon+"_runs"+runs+"_species"+species+".hist";
		
		if (ExamplePath.exists(fn))
			return ExamplePath.find(fn);
		else 
			return new File(fn);
	}

	public Map<Integer, Integer> getHistogram() {
		return histogram;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public Simulator getSimulator() {
		return simulator;
	}

}
