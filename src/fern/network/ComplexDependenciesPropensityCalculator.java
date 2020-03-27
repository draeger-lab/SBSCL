package fern.network;

import java.util.List;

import fern.simulation.algorithm.DependencyGraph;

/**
 * 
 * In a SBML network, the propensities of reaction are calculated by using the 
 * kineticLaw tag, which contains a MathML expression. Since therein arbitrary species can 
 * be included, there must be a way to fetch dependencies between the reactions in order to
 * create the {@link DependencyGraph}.
 * 
 * @author Florian Erhard
 *
 */
public interface ComplexDependenciesPropensityCalculator extends
		PropensityCalculator {

	/**
	 * Gets the indices of the species that are included in the calculation of 
	 * the given reaction.
	 * 
	 *  @param reaction index of the reaction
	 * @return	indices of the species included in the reaction's kinetic law
	 */
	public List<Integer> getKineticLawSpecies(int reaction);
}
