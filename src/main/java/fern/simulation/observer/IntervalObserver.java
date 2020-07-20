package fern.simulation.observer;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;
import fern.tools.gnuplot.TransposedArrayMatrixAxes;


/**
 * Base class for observing certain aspects repeatedly after a given interval.
 * Two methods can be used to observe intervals:
 * <ul><li><code>step</code>: check after each step if a interval range has been passed
 * </li><li><code>theta</code>: register thetas for the interval ranges</li></ul>
 *
 * <p>
 * The type of the used method can be set by using {@link IntervalObserver#setThetaMethod(boolean)}.
 * <p>
 * Extending classes just have to implement {@link IntervalObserver#getEntityValue(int)} and to
 * pass some indices and names to to constructor.
 * 
 * @author Florian Erhard
 *
 */
public abstract class IntervalObserver extends Observer  implements GnuPlotObserver{

	private double recentStep;
	private double[][] avgLog = null;
	private int[] quality = null;
	private boolean plotQuality = false;
	private boolean thetaMethod = true;
	private double interval;
	private String[] entityName;
	private LinkedList<double[]> log;
	private int duration;
	
	
	
	/**
	 * Create the observer for a given simulator, a given interval, given indices with names.
	 * @param sim			simulator
	 * @param interval		interval
	 * @param entityName	names of the entities
	 */
	public IntervalObserver(Simulator sim, double interval, String[] entityName) {
		super(sim);
		this.interval = interval;
		
		if (entityName.length==0)
			throw new IllegalArgumentException("At least one entity has to be specified!");
		
		this.entityName = entityName;
		
		
		log = new LinkedList<double[]>();
		
		avgLog = new double[this.entityName.length+1][0];
		quality = new int[0];
	}

	public IntervalObserver(Simulator sim, double interval, int duration, String[] entityName) {
		super(sim);
		this.interval = interval;
		this.duration = duration;

		if (entityName.length==0)
			throw new IllegalArgumentException("At least one entity has to be specified!");

		this.entityName = entityName;


		log = new LinkedList<double[]>();

		avgLog = new double[this.entityName.length+1][this.duration + 1];
		quality = new int[0];
	}
	
	/**
	 * Gets the actual value of the entity.
	 * 
	 * @param i	index of the entity
	 * @return	value of the entity
	 */
	protected abstract double getEntityValue(int i);
	
	/**
	 * Clears the recorded values and registers (if needed) <code>theta</code>
	 */
	@Override
	public void started() {
		log.clear();
		if (thetaMethod)
			setTheta(0);
		recentStep = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	public void activateReaction(int mu, double tau, FireType fireType, int times) {}
	
	/**
	 * Adds the recorded data to the average data pool. Moreover a linear interpolation
	 * is done to the recorded values in order to guarantee that the time axis matches
	 * exactly the intervals. If the theta method has been used no interpolation is performed.
	 */
	@Override
	public void finished() {
		step();
		if (log.size()==0) return;
		
		double[][] actLog = getAsArray(log);
		
		if (!thetaMethod) {
			for (int i=actLog.length-1; i>=0; i--)
				actLog[i] = NumberTools.interpolateLinear(interval, actLog[0], actLog[i]);
			
			// workaround: correct last timeindex
			if( actLog[0].length>1)
				actLog[0][actLog[0].length-1] = actLog[0][actLog[0].length-2]+interval; 
		}

		double[][] newAvgLog = new double[entityName.length+1][Math.max(avgLog[0].length,actLog[0].length)];
		for (int i=0; i<newAvgLog.length; i++){
			for (int j=0; j<newAvgLog[i].length; j++){
				if (j<actLog[i].length && j<avgLog[i].length)	
					newAvgLog[i][j] = (actLog[i][j]+avgLog[i][j]*getNumSimulations())/(getNumSimulations()+1);
				else if (j<actLog[i].length)	
					newAvgLog[i][j] = actLog[i][j];
				else {
					newAvgLog[i][j] = avgLog[i][j];
					if ((i == 0) && (j >= 1)){
						newAvgLog[i][j] = newAvgLog[i][j-1] + 1;
					}
				}
			}
		}
		int[] newQuality = new int[Math.max(avgLog[0].length,actLog[0].length)];
		System.arraycopy(quality, 0, newQuality, 0, Math.min(newQuality.length, quality.length));
		for (int i=0; i<actLog[0].length; i++)
			newQuality[i]++;
		
		quality = newQuality;
		for (int i = 0; i < avgLog.length; i++){
			System.arraycopy(newAvgLog[i], 0, avgLog[i], 0, avgLog[0].length);
		}
		
	}
	
	/**
	 * If the <code>step</code> method is used, the data is recorded here.
	 */
	@Override
	public void step() {
		if (!thetaMethod && (int)(getSimulator().getTime()/interval)>(int)(recentStep/interval)) {
			double[] l = new double[entityName.length+1];
			l[0]=getSimulator().getTime();
			for (int i=0; i<entityName.length; i++)
				l[i+1] = getEntityValue(i);
			log.add(l);
		}
		recentStep = getSimulator().getTime();
	}
	
	/**
	 * If the <code>theta</code> method is used, the data is recorded here.
	 */
	@Override
	public void theta(double theta) {
		if (thetaMethod){
			double[] l = new double[entityName.length+1];
			l[0]=theta;
			for (int i=0; i<entityName.length; i++)
				l[i+1] = getEntityValue(i);
			log.add(l);
			setTheta(theta+interval);
		}
	}
	
	
	
	/**
	 * Using theta method means that the time intervals are registered at the simulator and
	 * the simulator invokes at this moment the method theta. Otherwise the algorithm steps
	 * are used and the values are interpolated 
	 * @return the thetaMethod
	 */
	public boolean isThetaMethod() {
		return thetaMethod;
	}

	/**
	 * Using theta method means that the time intervals are registered at the simulator and
	 * the simulator invokes at this moment the method theta. Otherwise the algorithm steps
	 * are used and the values are interpolated
	 * @param thetaMethod the thetaMethod to set
	 */
	public void setThetaMethod(boolean thetaMethod) {
		this.thetaMethod = thetaMethod;
	}

	public GnuPlot toGnuplot() throws IOException {
		return toGnuplot(new GnuPlot());
	}
	
	public GnuPlot toGnuplot(GnuPlot gnuplot) throws IOException {
		GnuPlot gp = toGnuplot(gnuplot, avgLog);
		if (plotQuality) {
			gp.getAxes().get(gp.getAxes().size()-1).addAxes(new TransposedArrayMatrixAxes(
					new int[][] {quality},
					new String[] {"quality"},
					null
			));
		}
		return gp;
	}
	
	/**
	 * Gets the recorded data of the most recent simulation run.
	 * 
	 * @return data as double array [step][entity index]
	 */
	public double[][] getRecentData() {
		double[][] re = new double[log.size()][];
		int index = 0;
		for (double[] rec : log)
			re[index++] = rec;
		return re;
	}

	/**
	 * Creates a new {@link GnuPlot} object and passes the recent observer data to it.
	 * Recent means that not the average data is used but only the recently produced.
	 * 
	 * @return				the created <code>GnuPlot</code> object	
	 * @throws IOException	if gnuplot could not be accessed
	 */
	public GnuPlot toGnuplotRecent() throws IOException {
		return toGnuplotRecent(new GnuPlot());
	}
	
	/**
	 * Passes the recent observer data to a {@link GnuPlot} object.
	 * Recent means that not the average data is used but only the recently produced.
	 * 
	 * @param gnuplot		the <code>GnuPlot</code> object to pass the data to
	 * @return				the <code>GnuPlot</code> object 
	 * @throws IOException	if gnuplot could not be accessed
	 */
	public GnuPlot toGnuplotRecent(GnuPlot gnuplot) throws IOException {
		return toGnuplot(gnuplot, new LinkedList<double[]>(log));
	}
	
	/**
	 * Sets whether or not to plot quality data. If set to true, 
	 * a call to <code>toGnuplot</code> adds not only the values but also one
	 * column in which the number of experiments (from how many values is the average of
	 * the regarding row calculated) is
	 * 
	 * @param plotQuality whether or not to plot qualities
	 */
	public void setPlotQuality(boolean plotQuality) {
		this.plotQuality = plotQuality;
	}
	
	/**
	 * Gets whether or not to plot quality data. If set to true, 
	 * a call to <code>toGnuplot</code> adds not only the values but also one
	 * column in which the number of experiments (from how many values is the average of
	 * the regarding row calculated) is
	 * 
	 * @return whether or not to plot qualities
	 */
	public boolean isPlotQuality() {
		return plotQuality;
	}
	
	/**
	 * Gets the last value of the given entity.
	 * 
	 * @param entityIndex	index of the entity
	 * @return				last recorded value
	 */
	public double getFinalValue(int entityIndex) {
		return avgLog[entityIndex+1][avgLog[entityIndex+1].length-1];
	}
	
	@Override
	public String toString() {
		String[] names = applyLabelFormat(entityName);
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<names.length; i++)
			sb.append(names[i]+": "+getFinalValue(i)+"\n");
		return sb.toString();
	}
	
	private GnuPlot toGnuplot(GnuPlot gnuplot, double[][] values) throws IOException {
		if (gnuplot==null) return null;
		
		String[] names = applyLabelFormat(entityName);
		gnuplot.addData(values,true, names, getStyles());
		return gnuplot;
	}
	
	
	private GnuPlot toGnuplot(GnuPlot gnuplot, Collection<double[]> values) throws IOException {
		if (gnuplot==null) return null;
		String[] names = applyLabelFormat(entityName);
		gnuplot.addData(values, names, getStyles());
		return gnuplot;
	}
	

	private double[][] getAsArray(LinkedList<double[]> l) {
		double[][] re = new double[l.get(0).length][l.size()];
		int i=0;
		for (double[] t : l){ 
			for (int j=0; j<t.length; j++) 
				re[j][i] = t[j];
			i++;
		}
		return re;
	}

	public double[][] getAvgLog() {
		return avgLog;
	}
}
