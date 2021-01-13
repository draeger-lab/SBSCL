package fern.analysis;

import fern.network.Network;

/**
 * Implementing classes can be used for a {@link NetworkSearchAction}s <code>checkReaction</code>,
 * <code>checkSpecies</code>, if the information whether or not to visit the nodes is not
 * accessible for the <code>NetworkSearchAction</code>.
 *
 * @author Florian Erhard
 */
public interface NodeChecker {

  boolean checkReactionNode(Network network, int reaction);

  boolean checkSpeciesNode(Network network, int species);
}
