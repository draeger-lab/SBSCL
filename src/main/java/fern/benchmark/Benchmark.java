package fern.benchmark;

import java.util.Collection;
import java.util.LinkedList;

import cern.jet.random.AbstractDistribution;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

/**
 * Base class for all benchmark classes. Gives methods for measuring elapsed time
 * as well as methods for creating test sets and benchmark data handling.
 * @author Florian Erhard
 *
 */
public abstract class Benchmark {

	private long startTime;
	private Collection<double[]> data = new LinkedList<double[]>();
	private int numBins = 100;
	
	/**
	 * Gets the number of bins that are used for creating gnuplot histograms.
	 * Default is 100.
	 * 
	 * @return number of bins
	 */
	public int getNumBins() {
		return numBins;
	}
	
	/**
	 * Sets the number of bins that are used for creating gnuplot histograms.
	 * Default is 100.
	 * 
	 * @param numBins number of bins
	 */
	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}
	
	/**
	 * Adds benchmark data to the data pool.
	 * 
	 * @param d benchmark data
	 */
	public void addData(double[] d) {
		data.add(d);
	}
	
	/**
	 * Clears all collected benchmark data.
	 */
	public void clearData() {
		data.clear();
	}
	
	/**
	 * Adds the benchmark data without conversion to a new {@link GnuPlot} object.
	 * 
	 * @param dataLabels 	labels for the benchmark data
	 * @param styles 		styles for the benchmark data
	 * @return 				a <code>GnuPlot</code> object containing the benchmark data
	 * 
	 * @see GnuPlot
	 */
	public GnuPlot toGnuplot(String[] dataLabels, String[] styles) {
		return toGnuplot(new GnuPlot(),dataLabels,styles);
	}
	
	/**
	 * Adds the benchmark data without conversion to a given {@link GnuPlot} object.
	 * 
	 * @param dataLabels 	labels for the benchmark data
	 * @param styles 		styles for the benchmark data
	 * @return			 	a <code>GnuPlot</code> object containing the benchmark data
	 * 
	 * @see GnuPlot
	 */
	public GnuPlot toGnuplot(GnuPlot gnuplot, String[] dataLabels, String[] styles) {
		if (gnuplot==null) return null;
		gnuplot.addData(data, dataLabels, styles);
		return gnuplot;
	}
	
	/**
	 * Adds the benchmark data as histogram to a new {@link GnuPlot} object.
	 * 
	 * @param dataLabels 	labels for the benchmark data
	 * @param styles 		styles for the benchmark data
	 * @return				a <code>GnuPlot</code> object containing the benchmark data
	 * 
	 * @see GnuPlot
	 */
	public GnuPlot toGnuPlotAsHistogram(String[] dataLabels, String[] styles) {
		return toGnuPlotAsHistogram(new GnuPlot(),dataLabels,styles);
	}
	
	/**
	 * Adds the benchmark data as histogram to a given {@link GnuPlot} object.
	 * 
	 * @param dataLabels 	labels for the benchmark data
	 * @param styles 		styles for the benchmark data
	 * @return 				a <code>GnuPlot</code> object containing the benchmark data
	 * 
	 * @see GnuPlot
	 */
	public GnuPlot toGnuPlotAsHistogram(GnuPlot gnuplot, String[] dataLabels,String[] styles) {
		if (gnuplot==null) return null;
		gnuplot.addData(NumberTools.createHistogram(data,numBins), dataLabels, styles);
		return gnuplot;
	}

	/**
	 * sets a start time for the benchmark system
	 */
	public void start() {
		startTime = System.nanoTime();
	}
	
	/**
	 * Gets the elapsed time since the last call of <code>start</code> in nanoseconds
	 * @return elapsed time in ns
	 */
	public long end() {
		return System.nanoTime()-startTime;
	}
	
	/**
	 * Creates a test set containing <code>size</code> random numbers of the 
	 * distribution <code>dist</code>.
	 * @param size the size of the test set
	 * @param dist the probability distribution
	 * @return test set
	 */
	public double[] createRandomDoubleArray(int size, AbstractDistribution dist) {
		double[] re = new double[size];
		for (int i=0; i<size; i++)
			re[i] = dist.nextDouble();
		return re;
	}
}
