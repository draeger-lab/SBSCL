package fern.network;

import fern.simulation.Simulator;

/**
 * A <code>PropensityCalculator</code> is the way, a {@link Network} tells a {@link Simulator}
 * how to calculate propensities for the reactions. Each <code>Simulator</code> calls the <code>
 * calculatePropensity</code> method, when the amount of some reactants of a reactions has changed.
 * 
 * @author Florian Erhard
 *
 */
public interface PropensityCalculator {
	/**
	 * Calculates the propensity for a reaction given the amounts of the <code>AmountManager</code>.
	 * If a positive
	 * value for volume is given, it is assumed that the constants are deterministic rate
	 * constants and are hence to be transformed to specific reaction rate constants.
	 * 
	 * @param reaction		index of the reaction
	 * @param amount		AmountManager
	 * @param sim			Simulator
	 * @return				actual propensity of the reaction
	 */
	public double calculatePropensity(int reaction, AmountManager amount, Simulator sim);
}
