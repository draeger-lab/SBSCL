/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.observer;

import java.io.PrintWriter;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;


/**
 * Implementing classes are used to observe certain aspects of simulations. It is basically
 * just an interface of methods that get called during a simulation together with some 
 * common methods useful for many types of observers.
 * <p>
 * Some observers take repeats into account. If you repeat the simulations, they will
 * yield average data.
 * <p>
 * Each observer should override the <code>toString</code> method which should return 
 * a string representation of its data.
 * <p>
 * You can register a {@link PrintWriter} to the <code>Observer</code>. If you have, the 
 * return value of <code>toString</code> is printed if a simulation has finished. 
 * 
 * 
 * @author Florian Erhard
 *
 */
public abstract class Observer {
	
	private Simulator simulator;
	private String labelFormat = "%s";
	private PrintWriter pw = null;
	private int numSimulations = 0;
	
	/**
	 * Contains the theta for this observer.
	 * 
	 */
	private double theta = Double.POSITIVE_INFINITY;
	
	/**
	 * Creates an observer dedicated to one simulator. The observer is NOT registered at the
	 * simulator, you have to call {@link Simulator#addObserver(Observer)} in order to do this.
	 * 
	 * @param sim the simulator
	 */
	public Observer(Simulator sim) {
		this.simulator = sim;
	}
	
	/**
	 * Gets called when the simulation has started after the initialization and before 
	 * the termination condition is checked the first time.
	 */
	public abstract void started();
	/**
	 * Gets called after each termination check and before {@link Simulator#performStep(fern.simulation.controller.SimulationController)}
	 * is called.
	 */
	public abstract void step();
	/**
	 * Gets called when a simulation has finished, directly after the termination check.
	 */
	public abstract void finished();
	/**
	 * Gets called by simulators when a certain moment in time is reached. This moment in 
	 * time has to be registered by {@link Observer#getTheta()}
	 * 
	 * @param theta moment in time
	 */
	public abstract void theta(double theta);
	/**
	 * Gets called before a reaction fires.
	 * @param mu		the reaction which is supposed to fire
	 * @param tau		the time the reaction fires (at this time {@link Simulator#getTime()} does not necessarily yield the firing time)
	 * @param fireType	the type of the firing
	 * @param times TODO
	 */
	public abstract void activateReaction(int mu, double tau, FireType fireType, int times);
	/**
	 * Gets called after the <code>finish</code> call and prints the results of this
	 * observer if a {@link PrintWriter} is registered.
	 */
	public void print() {
		if (pw!=null) {
			pw.append(toString());
			pw.append("\n");
			pw.flush();
		}
		numSimulations++;
	}
	
	/**
	 * Gets the moment in time, where the simulator has to invoke {@link Observer#theta(double)}.
	 * 
	 * @return theta
	 */
	public double getTheta() {
		return theta;
	}
	
	/**
	 * Sets the moment in time, where the simulator has to invoke {@link Observer#theta(double)}.
	 * 
	 * @param theta the theta
	 */
	public void setTheta(double theta) {
		this.theta = Math.min(this.theta,theta);
		getSimulator().registerNewTheta(this,theta);
	}
	
	/**
	 * Gets the registered {@link PrintWriter}
	 * @return the <code>PrintWriter</code>
	 */
	public PrintWriter getPrintWriter() {
		return pw;
	}

	/**
	 * Registers a {@link PrintWriter}
	 * @param printWriter the <code>PrintWriter</code>
	 */
	public void setPrintWriter(PrintWriter printWriter) {
		this.pw = printWriter;
	}

	
	/**
	 * Sets the label format for {@link Observer#getLabel(String)}.
	 * 
	 * @param labelFormat 	the label format
	 * 
	 * @see Observer#getLabel(String)
	 */
	public void setLabelFormat(String labelFormat) {
		this.labelFormat = labelFormat;
	}
	
	/**
	 * Gets the label format of {@link Observer#getLabel(String)}.
	 * 
	 * @return labelFormat the label format
	 * 
	 * @see Observer#getLabel(String)
	 */
	public String getLabelFormat() {
		return labelFormat;
	}
	
	/**
	 * Applies the actual label format to the whole array (but does not change the 
	 * passed array, it return a new one)
	 * 
	 * @param name	array of labels
	 * @return		array of formatted labels 
	 */
	protected String[] applyLabelFormat(String[] name) {
		String[] re = new String[name.length];
		for (int i=0; i<re.length; i++)
			re[i] = getLabel(name[i]);
		return re;
	}
	
	/**
	 * Gets a gnuplot command if you want to plot a histogram and have
	 * the data labels for each value in an array
	 * 
	 * @param titles	data labels for each histogram value
	 * @return			gnuplot command to label the x axis
	 */
	protected String getTitlesCommand(String[] titles) {
		StringBuilder sb = new StringBuilder();
		sb.append("set xtics (");
		for (int i=0; i<titles.length; i++) {
			sb.append("\""+titles[i]);
			sb.append("\" ");
			sb.append(i+"");
			if (i<titles.length-1) sb.append(", ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Formats a label with the actual label format:
	 * <ul><li>%l is replaced by the passed argument</li>
	 * <li>%a is replaced by the simulator's name</li>
	 * <li>%n is replaced by the network's name</li>
	 * </ul>
	 * 
	 * @param label	label to format
	 * @return		formatted label
	 */
	protected String getLabel(String label) {
		String format = labelFormat.replace("%l", "%1$s").replace("%a", "%2$s").replace("%n", "%3$s");
		return String.format(format, label, simulator.getName(), simulator.getNet().getName());
	}

	/**
	 * Gets the simulator	
	 * @return	simulator
	 */
	public Simulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Sets the simulator. For internal use only. If you want to override it, be careful
	 * with the consistency between the simulator's observer list and the observer's simulator!
	 * 
	 * @param simulator the simulator
	 */
	protected void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * Gets the number of finished repeats of the simulation. The number is increased
	 * after the <code>finished</code> call, so within <code>finish</code> of an extending
	 * class it is the number of repeats without the actual simulation.
	 * 
	 * @return	number of repeats
	 */
	public int getNumSimulations() {
		return numSimulations;
	}
	
}
