/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.controller;

import fern.simulation.Simulator;

/**
 * {@link Simulator}s are controlled by implementing classes. Each simulation step
 * the <code>goOn</code> method is called to check whether to go on or not.
 * 
 * @author Florian Erhard
 *
 */
public interface SimulationController {

	/**
	 * Returns whether or not to go on with the given simulation
	 * @param sim	simulation
	 * @return		whether or not to go on
	 */
	public boolean goOn(Simulator sim);
}
