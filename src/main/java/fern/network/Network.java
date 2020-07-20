/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.network;

import fern.analysis.AnalysisBase;
import fern.network.fernml.FernMLNetwork;
import fern.network.modification.ModifierNetwork;
import fern.simulation.Simulator;

/**
 * 
 * The central interface of the whole framework. Each {@link Simulator} takes a <code>Network</code>
 * for simulation, as well as each analysis algorithm takes one. Additionally <code>Network</code>s
 * can be modified or saved as FernML-Files. 
 * <p>
 * Basically only the networks structure is stored here, other things like management of
 * the species populations (amounts), propensities or annotations is sourced out to separate
 * classes. The advantage is that you can reuse existing {@link AmountManager}s,... when
 * you are implementing your own network (reader, wrapper, evolver,...) when you think, one
 * of the existing ones is suitable.
 * 
 * @author Florian Erhard
 * 
 * @see AnalysisBase
 * @see FernMLNetwork#FernMLNetwork(Network)
 * @see FernMLNetwork#saveToFile(java.io.File)
 * @see ModifierNetwork
 */
public interface Network {
	
	/**
	 * Gets the {@link AmountManager} for this network.
	 * 
	 * @return the <code>AmountManager</code>
	 */
	public AmountManager getAmountManager();
	/**
	 * Gets the {@link PropensityCalculator} for this network.
	 * 
	 * @return the <code>PropensityCalculator</code>
	 */
	public PropensityCalculator getPropensityCalculator();
	/**
	 * Gets the {@link AnnotationManager} for this network.
	 * 
	 * @return the <code>AnnotationManager</code>
	 */
	public AnnotationManager getAnnotationManager();
	
	/**
	 * Gets the number of species within the network.
	 * 
	 * @return number of species
	 */
	public int getNumSpecies();
	/** 
	 * Gets the number of reaction within the network.
	 * 
	 * @return number of reactions
	 */
	public int getNumReactions();
	
	/**
	 * Gets the reactants of the specified reaction.
	 * 
	 * @param reaction 	index of the reaction
	 * @return			indices of the reactants
	 */
	public int[] getReactants(int reaction);
	/**
	 * Gets the products of the specified reaction.
	 * 
	 * @param reaction 	index of the reaction
	 * @return			indices of the products
	 */
	public int[] getProducts(int reaction);
	
	/**
	 * Gets the species index by name. If the argument is no valid species, -1 is returned.
	 * 
	 * @param name	name of the species
	 * @return		index of the species
	 */
	public int getSpeciesByName(String name);
	/**
	 * Gets the species name by index.
	 * 
	 * @param index	index of the species
	 * @return		name of the species
	 */
	public String getSpeciesName(int index);
	/**
	 * Gets a string representation of the reaction.
	 * 
	 * @param index	reaction index
	 * @return		string representation
	 */
	public String getReactionName(int index);
	/**
	 * Gets an identifier of the network.
	 * 
	 * @return	identifier of the network
	 */
	public String getName();
	
	/**
	 * Gets the initial amount of the specified molecule species.
	 * @param species	index of the species
	 * @return			initial amount of the species
	 */
	public long getInitialAmount(int species);
	/**
	 * Sets the initial amount of the specified molecule species.
	 * @param species	index of the species
	 * @param value		initial amount of the species
	 */
	public void setInitialAmount(int species, long value);

	
	
	
}
