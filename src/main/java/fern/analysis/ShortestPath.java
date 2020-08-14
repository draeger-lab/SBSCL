package fern.analysis;

import java.util.Arrays;
import java.util.LinkedList;

import fern.network.Network;
import fern.tools.NumberTools;

/**
 * Computes shortest paths in the network by a bfs. Either the path from some source species to only
 * one species can be calculated (by using one of the
 * <code>computePath</code> methods) or paths to all species (by <code>computePaths</code>).
 * A {@link NodeChecker} can optionally be given for each method.
 *
 * @author Florian Erhard
 */
public class ShortestPath extends AnalysisBase {

  /**
   * Creates the class with the specified network.
   *
   * @param network the network where shortest paths shall be computed
   */
  public ShortestPath(Network network) {
    super(network);
  }

  /**
   * Compute all shortest paths from some source species.
   *
   * @param species the names of the source species
   * @return an array of paths
   * @see ShortestPath.Path
   */
  public Path[] computePaths(String... species) {
    return computePaths(null, species);
  }

  /**
   * Compute all shortest paths from some source species by only using parts of the network
   * specified by the {@link NodeChecker} <code>checker</code>.
   *
   * @param species the names of the source species
   * @param checker a NodeChecker for the search
   * @return an array of paths
   * @see ShortestPath.Path
   */
  public Path[] computePaths(NodeChecker checker, String... species) {
    int[] speciesIndex = getSpeciesIndices(species);
    ShortestPathAction action = new ShortestPathAction(checker);
    bfs(speciesIndex, new int[0], action);
    Path[] re = new Path[network.getNumSpecies()];
		for (int i = 0; i < re.length; i++) {
			re[i] = action.getPath(i);
		}
    return re;
  }

  /**
   * Compute the shortest paths from some source species to one species.
   *
   * @param toSpecies the name of the species where the shortest path should be computed to
   * @param species   the names of the source species
   * @return the shortest path
   * @see ShortestPath.Path
   */
  public Path computePath(String toSpecies, String... species) {
    return computePath(null, toSpecies, species);
  }

  /**
   * Compute the shortest paths from some source species to one species by only using parts of the
   * network specified by the {@link NodeChecker} <code>checker</code>.
   *
   * @param checker   a NodeChecker for the search
   * @param toSpecies the name of the species where the shortest path should be computed to
   * @param species   the names of the source species
   * @return the shortest path
   * @see ShortestPath.Path
   */
  public Path computePath(NodeChecker checker, String toSpecies, String... species) {
    int[] speciesIndex = getSpeciesIndices(species);
    ShortestPathAction action = new ShortestPathAction(checker);
    bfs(speciesIndex, new int[0], action);
    int toSpeciesIndex = network.getSpeciesByName(toSpecies);
		if (toSpeciesIndex < 0) {
			throw new IllegalArgumentException(toSpecies + " doesn't belong to the network!");
		}
    return action.getPath(toSpeciesIndex);
  }

  private int[] getSpeciesIndices(String[] species) {
    int[] speciesIndex = new int[species.length];
		for (int i = 0; i < species.length; i++) {
			speciesIndex[i] = network.getSpeciesByName(species[i]);
		}
    return speciesIndex;
  }

  /**
   * Encapsulates a path from one species to another.
   *
   * @author Florian Erhard
   */
  public class Path {

    int[] path;

    /**
     * Creates a path from an array. The array has to end with a species and has to be composed of
     * alternating indices of species and reactions.
     *
     * @param path Arrays containing the network indices of the path's components
     */
    public Path(int[] path) {
      this.path = path;
    }

    /**
     * Returns the raw path: Is ends with a species index and is composed of alternating indices of
     * species and reactions.
     *
     * @return Raw path of alternating indices of species and reactions ending at a species index.
     */
    public int[] getRawData() {
      return path;
    }

    /**
     * Returns all the species on this path.
     *
     * @return Indices of the species.
     */
    public int[] getSpecies() {
      int[] re = new int[(path.length + 1) / 2];
			for (int i = path.length % 2 == 0 ? 1 : 0; i < path.length; i += 2) {
				re[i / 2] = path[i];
			}
      return re;
    }

    /**
     * Returns all the reactions on this path.
     *
     * @return Indices of the reactions.
     */
    public int[] getReactions() {
      int[] re = new int[path.length / 2];
			for (int i = path.length % 2 == 0 ? 0 : 1; i < path.length; i += 2) {
				re[i / 2] = path[i];
			}
      return re;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < path.length; i++) {
        sb.append(i % 2 == 0 ? network.getSpeciesName(path[i]) : network.getReactionName(path[i]));
        sb.append(", ");
      }
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length());
			}
      return sb.toString();
    }

  }

  /**
   * <code>NetworkSearchAction</code> for computing shortest paths in a network.
   *
   * @author Florian Erhard
   */
  private class ShortestPathAction implements NetworkSearchAction {

    int[] reactionDist = null;
    int[] speciesDist = null;
    int[] reactionParent = null;
    int[] speciesParent = null;
    int dist = 0;
    int parent = -1;
    NodeChecker checker = null;

    public ShortestPathAction(NodeChecker checker) {
      this.checker = checker;
    }


    public int[] getReactionDistances() {
      return reactionDist;
    }

    public int[] getSpeciesDistances() {
      return speciesDist;
    }

    /**
     * Return the path starting at the given species and ending at either of the search' sources.
     *
     * @param species Index of the species where the path starts.
     * @return
     */
    public Path getPath(int species) {
      LinkedList<Integer> path = new LinkedList<Integer>();
      int reaction;
      while (species >= 0) {
        path.add(0, species);
        reaction = speciesParent[species];
				if (reaction < 0) {
					break;
				}
        path.add(0, reaction);
        species = reactionParent[reaction];
      }
      return new Path(NumberTools.toIntArray(path));
    }

    public void initialize(Network net) {
      reactionDist = new int[net.getNumReactions()];
      speciesDist = new int[net.getNumSpecies()];
      reactionParent = new int[net.getNumReactions()];
      speciesParent = new int[net.getNumSpecies()];
      Arrays.fill(reactionDist, -1);
      Arrays.fill(speciesDist, -1);
      Arrays.fill(reactionParent, -1);
      Arrays.fill(speciesParent, -1);
    }


    public void reactionDiscovered(int reaction) {
      reactionDist[reaction] = dist;
      reactionParent[reaction] = parent;
    }

    public void reactionFinished(int reaction) {
      dist = reactionDist[reaction] + 1;
      parent = reaction;
    }

    public void speciesDiscovered(int species) {
      speciesDist[species] = dist;
      speciesParent[species] = parent;
    }

    public void speciesFinished(int species) {
      dist = speciesDist[species] + 1;
      parent = species;
    }


    public void finished() {
    }


    public boolean checkReaction(int reaction, NeighborType neighborType) {
			if (neighborType != NeighborType.Reactant) {
				return false;
			} else {
				return checker == null ? true : checker.checkReactionNode(network, reaction);
			}
    }


    public boolean checkSpecies(int species, NeighborType neighborType) {
			if (neighborType != NeighborType.Product) {
				return false;
			} else {
				return checker == null ? true : checker.checkSpeciesNode(network, species);
			}
    }


    public Iterable<Integer> getAdditionalReactionNeighbors(int index) {
      return null;
    }


    public Iterable<Integer> getAdditionalSpeciesNeighbors(int index) {
      return null;
    }


  }
}
