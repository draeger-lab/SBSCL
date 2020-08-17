package fern.network;

import fern.simulation.Simulator;

/**
 * The <code>AmountManager</code> is one of the most important connections between a {@link Network}
 * and a {@link Simulator}. Each <code>Simulator</code> calls the
 * <code>performReaction</code> method when it fires a reaction. The amount manager then
 * reflects the change of its reactant / product populations. Additionally, the {@link
 * PropensityCalculator} uses <code>getAmount</code> to calculate the propensity of a reaction.
 * <p>
 * It is also possible (and necessary for the tau leaping algorithms) to save the actual amount of
 * each species and, if some error happened, restore these saved values.
 * <p>
 * The amounts are stored in an array.
 *
 * @author Florian Erhard
 */
public interface AmountManager {

  /**
   * Reflects a (multiple) firing of a reaction by adjusting the populations of the reactants and
   * the products. If a population becomes negative, a <code> RuntimeException</code> is thrown.
   *
   * @param reaction the index of the reaction fired
   * @param times    the number of firings
   */
  public void performReaction(int reaction, int times);

  /**
   * Gets the current amount of a species.
   *
   * @param species index of the species
   * @return actual amount of the species
   */
  public long getAmount(int species);

  /**
   * Sets the current amount of a species.
   *
   * @param species index of the species
   */
  public void setAmount(int species, long amount);


  /**
   * Resets the amount of each species to the initial amount retrieved by the networks {@link
   * AnnotationManager}. This is called whenever a {@link Simulator} is started.
   */
  public void resetAmount();

  /**
   * Makes a copy of the amount array.
   */
  public void save();

  /**
   * Restore the amount array from the recently saved one.
   */
  public void rollback();

}
