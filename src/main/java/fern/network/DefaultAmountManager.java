package fern.network;

import fern.simulation.Simulator;

/**
 * Default implementation of an amount manager, that stores the amounts in arrays.
 * @author erhard
 *
 */
public class DefaultAmountManager implements AmountManager {

	private Network net;
	private long[] amount;
	private long[] save = null;
	private boolean[] boundaryConditionSpecies;
	
	/**
	 * Creates an <code>AmountManager</code> for a given network
	 * @param net	the network
	 */
	public DefaultAmountManager(Network net)  {
		this.net = net;
		if (net!=null) {
			amount = new long[net.getNumSpecies()];
			save = new long[amount.length];
			boundaryConditionSpecies = new boolean[amount.length];
			for (int i=0; i<net.getNumSpecies(); i++) {
				boundaryConditionSpecies[i] = net.getAnnotationManager().containsSpeciesAnnotation(i, "BoundaryCondition");
			}
		}
	}

	/**
	 * Reflects a (multiple) firing of a reaction by adjusting the populations of the
	 * reactants and the products. If a population becomes negative, a <code>
	 * RuntimeException</code> is thrown.
	 * @param reaction	the index of the reaction fired
	 * @param times		the number of firings
	 */
	public void performReaction(int reaction, int times) {

		int[] p = net.getProducts(reaction);
		for (int i = 0; i < p.length; i++) {
			amount[p[i]] += boundaryConditionSpecies[p[i]] ? 0 : times;
		}

		int[] r = net.getReactants(reaction);
		for (int i = 0; i < r.length; i++) {
			amount[r[i]] -= boundaryConditionSpecies[r[i]] ? 0 : times;
			if (amount[r[i]] < 0) {
				throw new RuntimeException("Negative amount!");
			}
		}
	}

	/**
	 * Gets the current amount of a species.
	 * @param species	index of the species
	 * @return			actual amount of the species
	 */
	public long getAmount(int species) {
		return amount[species];
	}
	
	/**
	 * Sets the current amount of a species.
	 * @param species	index of the species
	 */
	public void setAmount(int species, long amount) {
		this.amount[species] = amount;
	}
	
	/**
	 * Resets the amount of each species to the initial amount retrieved by the networks
	 * {@link AnnotationManager}. This is called whenever a {@link Simulator} is started.
	 */
	public void resetAmount() {
		for (int i=0; i<amount.length; i++) {
			setAmount(i,net.getInitialAmount(i));
		}
	}
	
	/**
	 * Makes a copy of the amount array.
	 */
	public void save() {
		System.arraycopy(amount, 0, save, 0, save.length);
	}
	
	/**
	 * Restore the amount array from the recently saved one.
	 */
	public void rollback() {
		System.arraycopy(save, 0, amount, 0, amount.length);
	}
	
}
