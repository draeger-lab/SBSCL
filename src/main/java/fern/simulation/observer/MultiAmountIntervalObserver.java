/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.observer;

import fern.simulation.Simulator;
import fern.tools.NetworkTools;

/**
 * 
 * Observes amounts of molecule species repeatedly after certain intervals. The amount of the
 * given molecules is summed up.
 * <p>
 * This observer does take repeats into account. If you repeat the simulation, 
 * you will get an average over the results of each run.
 * 
 * @author Florian Erhard
 *
 */
public class MultiAmountIntervalObserver extends IntervalObserver {

	private int[] speciesIndices = null;
	
	/**
	 * Creates the observer for a given simulator, a given interval and given species indices
	 * @param sim			simulator
	 * @param interval		interval
	 * @param species		species indices
	 */
	public MultiAmountIntervalObserver(Simulator sim, String presentationName, double interval, int...species) {
		super(sim,interval,new String[] {presentationName});
		this.speciesIndices = species;
		
	}
	
	/**
	 * Creates the observer for a given simulator, a given interval and given species names.
	 * @param sim			simulator
	 * @param interval		interval
	 * @param speciesName	species names
	 */
	public MultiAmountIntervalObserver(Simulator sim, String presentationName, double interval, String...speciesName) {
		super(sim,interval,new String[] {presentationName});
		this.speciesIndices = NetworkTools.getSpeciesIndices(sim.getNet(), speciesName); 
		
	}
	
	/**
	 * Gets the actual amount of a species.
	 */
	protected double getEntityValue(int e) {
		double sum = 0;
		for (int i=0; i<speciesIndices.length; i++)
			sum += getSimulator().getAmount(speciesIndices[i]);
		return sum;
	}

	
	public String[] getStyles() {
		return null;
	}

		
	
	
	

	

}
