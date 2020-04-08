package fern.network;

public interface PartialDerivativePropensityCalculator extends PropensityCalculator {

	
	/**
	 * Calculates partial differentials of the propensity functions for the tau leaping
	 * methods. If a positive
	 * value for volume is given, it is assumed that the constants are deterministic rate
	 * constants and are hence transformed to specific reaction rate constants.
	 * 
	 * @param reaction 		the reaction index
	 * @param amount		the <code>AmountManager</code>
	 * @param reactantIndex the network index of the reactant to calculate the partial differential for
	 * @param volume		the volume of the reaction space
	 * @return			 	partial differential
	 */
	public double calculatePartialDerivative(int reaction, AmountManager amount, int reactantIndex, double volume);
	
}
