package fern.analysis;

import fern.network.Network;

/**
 * Implementing classes of <code>NetworkSearchAction</code> are able to control/watch searches in
 * {@link AnalysisBase}. On the one hand they control by using <code>checkReaction</code>,
 * <code>checkSpecies</code> (control if a network node should be visited) and
 * <code>getAdditionalSpeciesNeighbors</code>, <code>getAdditionalSpeciesNeighbors</code> (if there
 * are other neighbors to visit e.g. catalysts of reactions). On the other hand they can watch the
 * searches by implementing <code>reactionDiscovered</code>, <code>reactionFinished</code> and
 * <code>speciesDiscovered</code>, <code>speciesFinished</code>.
 *
 * @author Florian Erhard
 */
public interface NetworkSearchAction {

  /**
   * Gets called when a reaction is inserted into the search structure.
   *
   * @param reaction index of the inserted reaction
   */
  public void reactionDiscovered(int reaction);

  /**
   * Gets called when a species is inserted into the search structure.
   *
   * @param species index of the inserted species
   */
  public void speciesDiscovered(int species);

  /**
   * Gets called when a reaction gets out of the search structure.
   *
   * @param reaction index of the reactions
   */
  public void reactionFinished(int reaction);

  /**
   * Gets called when a species gets out of the search structure.
   *
   * @param species index of the species
   */
  public void speciesFinished(int species);

  /**
   * Gets called, before the species is inserted into the search structure. If the implementing
   * instance returns false, the species is not inserted.
   *
   * @param species      species index
   * @param neighborType one of the NeighborTypes
   * @return true if the species should be inserted
   */
  public boolean checkSpecies(int species, NeighborType neighborType);

  /**
   * Gets called, before the reaction is inserted into the search structure. If the implementing
   * instance returns false, the reaction is not inserted.
   *
   * @param reaction     reaction index
   * @param neighborType one of the NeighborTypes
   * @return true if the reaction should be inserted
   */
  public boolean checkReaction(int reaction, NeighborType neighborType);

  /**
   * Gets called before anything is inserted into the search structure.
   *
   * @param net the network where the search is going to be performed
   */
  public void initialize(Network net);

  /**
   * Gets called, when the search is done.
   */
  public void finished();

  /**
   * Returns an iterator for additional neighbors of this reaction
   *
   * @param index reaction index
   * @return iterator of additional neighbors
   */
  public Iterable<Integer> getAdditionalSpeciesNeighbors(int index);

  /**
   * Returns an iterator for additional neighbors of this species
   *
   * @param index species index
   * @return iterator of additional neighbors
   */
  public Iterable<Integer> getAdditionalReactionNeighbors(int index);

  /**
   * Defines different types of neighborhoods in a <code>Network</code>.
   *
   * @author Florian Erhard
   */
  public enum NeighborType {
    Reactant, Product, Additional
  }
}
