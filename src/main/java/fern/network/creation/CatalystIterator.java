package fern.network.creation;

import fern.analysis.AutocatalyticNetworkDetection;

/**
 * A <code>CatalystIterator</code> is used to enumerate the catalysts of a reaction in
 * an {@link AutocatalyticNetwork}. For instance the {@link AutocatalyticNetworkDetection}
 * uses it to be able to walk from reactions to their catalysts in its breadth first searches. 
 * 
 * @author Florian Erhard
 *
 */
public interface CatalystIterator {
	/**
	 * Gets the indices of the catalysts of a reaction.
	 * @param reaction 	the index of the reaction
	 * @return			catalysts of the reaction
	 */
	public Iterable<Integer> getCatalysts(int reaction); 
}
