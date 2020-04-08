package fern.network.modification;

import fern.network.AmountManager;
import fern.network.AnnotationManager;
import fern.network.Network;
import fern.network.PropensityCalculator;

/**
 * 
 * Base class for modified networks, which implements the full {@link Network} interface.
 * Extending classes should override each method which is
 * different in the modified network (e.g. in the {@link ReversibleNetwork} only reactions are
 * virtually doubled, so each method concerning the species do not change). 
 * <p>
 * Note: An extending class should definitely not copy the whole network but should rather
 * redirect indices to the original network when possible in order to minimize the 
 * necessary memory.
 * 
 * @author Florian Erhard
 *
 */
public abstract class ModifierNetwork implements Network {

	private Network originalNet = null;
	
	/**
	 * Creates a <code>ModifierNetwork</code> from an original network.
	 * 
	 * @param originalNet	the network to modify
	 */
	public ModifierNetwork(Network originalNet) {
		this.originalNet = originalNet;
	}
	
	/**
	 * Gets the name of the original network
	 * 
	 * @return 	name of the original network.
	 */
	public String getName() {
		return originalNet.getName();
	}
	
	/**
	 * Gets the {@link AnnotationManager} of the original network
	 * 
	 * @return <code>AnnotationManager</code> of the original network
	 */
	public AnnotationManager getAnnotationManager() {
		return originalNet.getAnnotationManager();
	}

	/**
	 * Gets the number of reaction in the original network.
	 * 
	 * @return  number of reactions in the original network
	 */
	public int getNumReactions() {
		return originalNet.getNumReactions();
	}

	/**
	 * Gets the number of species in  the original network.
	 * 
	 * @return	number of species in the original network
	 */
	public int getNumSpecies() {
		return originalNet.getNumSpecies();
	}

	/**
	 * Gets the products of a reaction in the original network.
	 * 
	 * @param reaction 	index of the reaction in the original network
	 * @return 			indices of the products in the original network
	 */
	public int[] getProducts(int reaction) {
		return originalNet.getProducts(reaction); 
	}

	/**
	 * Gets the reactants of a reaction in the original network.
	 * 
	 * @param reaction 	index of the reaction in the original network
	 * @return 			indices of the reactants in the original network
	 */
	public int[] getReactants(int reaction) {
		return originalNet.getReactants(reaction);
	}

	/**
	 * Gets the name of the species by index in the original network.
	 * 
	 * @param index	index of the species the original network
	 * @return 		name of the species
	 */
	public String getSpeciesName(int index) {
		return originalNet.getSpeciesName(index);
	}

	/**
	 * Gets the index of the species by its name in the original network.
	 * 
	 * @param name	name of the species
	 * @return 		index of the species in the original network
	 */
	public int getSpeciesByName(String name) {
		return originalNet.getSpeciesByName(name);
	}

	/**
	 * Gets the {@link AmountManager} of the original network.
	 * @return <code>AmountManager</code> of the the original network
	 */
	public AmountManager getAmountManager() {
		return originalNet.getAmountManager();
	}

	/**
	 * Gets the {@link PropensityCalculator} of the original network.
	 * @return <code>PropensityCalculator</code> of the the original network
	 */
	public PropensityCalculator getPropensityCalculator() {
		return originalNet.getPropensityCalculator();
	}

	/**
	 * Gets a string representation of the reactio in the original network.
	 * 
	 * @param index	index of the reaction in the original network
	 * @return 		string represenation of the reaction
	 */
	public String getReactionName(int index) {
		return originalNet.getReactionName(index);
	}
	
	public long getInitialAmount(int species) {
		return originalNet.getInitialAmount(species);
	}
	
	public void setInitialAmount(int species, long value) {
		originalNet.setInitialAmount(species, value);
	}

	/**
	 * Gets the original network. If there is a higher hierarchy of modified networks 
	 * (e.g. a <code>ExtractSubNetwork</code> of a <code>ReversibleNetwork</code> of a
	 * <code>AutocatalyticNetworkExample</code>), the highest network (here the <code>AutocatalyticNetworkExample</code>
	 * is returned.
	 * 
	 * @return 	the original network
	 */
	public Network getOriginalNetwork() {
		if (originalNet instanceof ModifierNetwork)
			return ((ModifierNetwork)originalNet).getOriginalNetwork();
		else 
			return originalNet;
	}
	
	/**
	 * Gets the parent network. If there is a higher hierarchy of modified networks 
	 * (e.g. a <code>ExtractSubNetwork</code> of a <code>ReversibleNetwork</code> of a
	 * <code>AutocatalyticNetworkExample</code>), the parental network (here the <code>ReversibleNetwork</code>
	 * is returned. 
	 * 
	 * @return 	the parent network
	 */
	public Network getParentNetwork() {
		return originalNet;
	}
	
	
}
