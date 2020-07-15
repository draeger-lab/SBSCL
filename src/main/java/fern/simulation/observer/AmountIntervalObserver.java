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
 * Observes amounts of molecule species repeatedly after certain intervals.
 * <p>
 * This observer does take repeats into account. If you repeat the simulation, 
 * you will get an average over the results of each run.
 * 
 * @author Florian Erhard
 *
 */
public class AmountIntervalObserver extends IntervalObserver {

	private int[] speciesIndices = null;
	
	/**
	 * Creates the observer for a given simulator, a given interval and given species indices
	 * @param sim			simulator
	 * @param interval		interval
	 * @param species		species indices
	 */
	public AmountIntervalObserver(Simulator sim, double interval, int...species) {
		super(sim,interval,NetworkTools.getSpeciesNames(sim.getNet(), species));
		this.speciesIndices = species;
		
	}
	
	/**
	 * Creates the observer for a given simulator, a given interval and given species names.
	 * @param sim			simulator
	 * @param interval		interval
	 * @param speciesName	species names
	 */
	public AmountIntervalObserver(Simulator sim, double interval, String...speciesName) {
		super(sim,interval,speciesName);
		this.speciesIndices = NetworkTools.getSpeciesIndices(sim.getNet(), speciesName); 
		
	}

	/**
	 * Creates the observer for a given simulator, a given interval and given species names with
	 * total simulation time.
	 * @param sim
	 * @param interval
	 * @param duration
	 * @param speciesName
	 */
	public AmountIntervalObserver(Simulator sim, double interval, int duration, String...speciesName) {
		super(sim,interval, duration,speciesName);
		this.speciesIndices = NetworkTools.getSpeciesIndices(sim.getNet(), speciesName);
	}
	
	/**
	 * Gets the actual amount of a species.
	 */
	protected double getEntityValue(int i) {
		return getSimulator().getAmount(speciesIndices[i]);
	}

	
	public String[] getStyles() {
		return null;
	}

		
	
	
	

	

}
