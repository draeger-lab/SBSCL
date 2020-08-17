package fern.analysis;

import java.util.LinkedList;

import cern.colt.bitvector.BitVector;
import fern.network.Network;
import fern.network.creation.CatalystIterator;
import fern.tools.NumberTools;

/**
 * Detects the autocatalytic set of the given network if there is any. An autocatalytic set is
 * defined as a set of species that are produced by a path of reactions, starting at some food
 * molecules and fully catalyzed by members of the autocatalytic set.
 * <p>
 * The algorithm iterates over two modified breath first searches until none of them can exclude
 * species / reactions any more. The first bfs removes reactions that are not catalyzed in the
 * remaining network (and molecule species that are only produced by that reactions). The second bfs
 * removes species, that do not have a path from each necessary food molecule.
 *
 * @author Florian Erhard
 */
public class AutocatalyticNetworkDetection extends AnalysisBase {

  private BitVector removedReactions = null;
  private BitVector removedSpecies = null;
  private int[] numCatalysts = null;  // for each reaction -> how many catalysts are remaining
  private int[] numReactants = null;  // for each reaction -> how many reactants are remaining
  private int[] numCreated = null;  // for each species -> how many creating reactions are remaining

  private LinkedList<Integer>[] adjListAsCata = null;  // for each species -> list of reactions catalyzed
  private CatalystIterator cataIt = null;  // for each reaction -> list of catalysts

  /**
   * Creates the AutocatalyticDetection by using the in the network built in {@link
   * CatalystIterator}. The network has to implement <code>CatalystIterator</code>, otherwise an
   * <code>IllegalArgumentException</code> is thrown.
   *
   * @param network the network to detect the autocatalytic set in
   */
  public AutocatalyticNetworkDetection(Network network) {
    super(network);

    if (!(originalNetwork instanceof CatalystIterator)) {
      throw new IllegalArgumentException("Cannot find a CatalystIterator");
    } else {
      cataIt = (CatalystIterator) originalNetwork;
    }
  }

  /**
   * Creates the AutocatalyticDetection by using the second argument as {@link CatalystIterator}.
   *
   * @param network network the network to detect the autocatalytic set in
   * @param cataIt  a <code>CatalystIterator</code> for the network
   */
  public AutocatalyticNetworkDetection(Network network, CatalystIterator cataIt) {
    super(network);

    this.cataIt = cataIt;
  }

  /**
   * Performs the detection algorithm. The results can be retrieved by the methods
   * <code>getAutocatalyticReactions</code>,
   * <code>getAutocatalyticSpecies</code>,
   * <code>isAutocatalyticReaction</code>,
   * <code>isAutocatalyticSpecies</code>,
   * <code>annotate</code>
   *
   * @return number of iterations
   */
  public int detect() {
    removedReactions = new BitVector(network.getNumReactions());
    removedSpecies = new BitVector(network.getNumSpecies());

    int count = -1;
    int re = 0;
    ConnectedComponentIdentificationAction ccAction = new ConnectedComponentIdentificationAction();
    AutocatalyticSubsetDetectionAction asAction = new AutocatalyticSubsetDetectionAction();
    createAdjacencListsAsCata();
    preprocessingCounts();
    int[] monomers = getFoodSpecies();

    while (count < removedReactions.cardinality() + removedSpecies.cardinality()) {
      count = removedReactions.cardinality() + removedSpecies.cardinality();
      bfs(new int[0], getUncatalyzedReactions(), asAction);
//			NetworkTools.dumpNetwork(new ExtractSubNetwork(network,getAutocatalyticReactions(),getAutocatalyticSpecies()));
      bfs(monomers, new int[0], ccAction);
//			NetworkTools.dumpNetwork(new ExtractSubNetwork(network,getAutocatalyticReactions(),getAutocatalyticSpecies()));
      re++;
    }

    return re;
  }

  /**
   * Gets the autocatalytic reactions as {@link BitVector}. Throws a <code>RuntimeException</code>
   * if the detection algorithms has not been called.
   *
   * @return autocatalytic reactions
   */
  public BitVector getAutocatalyticReactions() {
    if (removedReactions == null) {
      throw new RuntimeException("Detection hasn't been called!");
    }
    BitVector re = removedReactions.copy();
    re.not();
    return re;
  }

  /**
   * Gets the autocatalytic species as {@link BitVector}. Throws a <code>RuntimeException</code> if
   * the detection algorithms has not been called.
   *
   * @return autocatalytic species
   */
  public BitVector getAutocatalyticSpecies() {
    if (removedSpecies == null) {
      throw new RuntimeException("Detection hasn't been called!");
    }
    BitVector re = removedSpecies.copy();
    re.not();
    return re;
  }

  /**
   * Returns true if the given reaction is autocatalytic. Throws a <code>RuntimeException</code> if
   * the detection algorithms has not been called.
   *
   * @param reaction the reaction index
   * @return if the reaction is autocatalytic
   */
  public boolean isAutocatalyticReaction(int reaction) {
    if (removedReactions == null) {
      throw new RuntimeException("Detection hasn't been called!");
    }
    return !removedReactions.get(reaction);
  }

  /**
   * Returns true if the given species is autocatalytic. Throws a <code>RuntimeException</code> if
   * the detection algorithms has not been called.
   *
   * @param species the species index
   * @return if the species is autocatalytic
   */
  public boolean isAutocatalyticSpecies(int species) {
    if (removedSpecies == null) {
      throw new RuntimeException("Detection hasn't been called!");
    }
    return !removedSpecies.get(species);
  }

  /**
   * Adds annotations to each autocatalytic reaction / species.
   *
   * @param field name of the annotation
   * @param value value of the annotation
   */
  public void annotate(String field, String value) {
    for (int i = 0; i < network.getNumReactions(); i++) {
      if (isAutocatalyticReaction(i)) {
        network.getAnnotationManager().setReactionAnnotation(i, field, value);
      }
    }
    for (int i = 0; i < network.getNumSpecies(); i++) {
      if (isAutocatalyticSpecies(i)) {
        network.getAnnotationManager().setSpeciesAnnotation(i, field, value);
      }
    }
  }

  /**
   * Gets the food molecules of the network. As default, monomers are the food molecules.
   *
   * @return indices of the food molecules
   */
  protected int[] getFoodSpecies() {
    LinkedList<Integer> re = new LinkedList<Integer>();
    for (int i = 0; i < network.getNumSpecies(); i++) {
      if (network.getSpeciesName(i).length() == 1) {
        re.add(i);
      }
    }
    return NumberTools.toIntArray(re);
  }

  /**
   * Gets the reactions that are not catalyzed in the remaining network as source for the first
   * search.
   *
   * @return reaction indices of not catalyzed reactions
   */
  private int[] getUncatalyzedReactions() {
    LinkedList<Integer> re = new LinkedList<Integer>();
    for (int i = 0; i < numCatalysts.length; i++) {
      if (numCatalysts[i] == 0 && !removedReactions.get(i)) {
        re.add(i);
      }
    }
    return NumberTools.toIntArray(re);
  }

  @SuppressWarnings("unchecked")
  private void createAdjacencListsAsCata() {
    adjListAsCata = new LinkedList[network.getNumSpecies()];
    for (int i = 0; i < adjListAsCata.length; i++) {
      adjListAsCata[i] = new LinkedList<Integer>();
    }
    for (int i = 0; i < network.getNumReactions(); i++) {
      for (int c : cataIt.getCatalysts(i)) {
        adjListAsCata[c].add(i);
      }
    }

  }

  private void preprocessingCounts() {
    numCatalysts = new int[network.getNumReactions()];
    for (int i = 0; i < network.getNumReactions(); i++) {
      if (!removedReactions.get(i)) {
        for (int c : cataIt.getCatalysts(i)) {
          if (!removedSpecies.get(c)) {
            numCatalysts[i]++;
          }
        }
      }
    }
    numReactants = new int[network.getNumReactions()];
    for (int i = 0; i < network.getNumReactions(); i++) {
      if (!removedReactions.get(i)) {
        for (int c : network.getReactants(i)) {
          if (!removedSpecies.get(c)) {
            numReactants[i]++;
          }
        }
      }
    }
    numCreated = new int[network.getNumSpecies()];
    for (int i = 0; i < network.getNumReactions(); i++) {
      if (!removedReactions.get(i)) {
        for (int c : network.getProducts(i)) {
          if (!removedSpecies.get(c)) {
            numCreated[c]++;
          }
        }
      }
    }
  }

  /**
   * NetworkSearchAction for the first search
   *
   * @author Florian Erhard
   */
  private class AutocatalyticSubsetDetectionAction implements NetworkSearchAction {


    public void initialize(Network net) {
    }

    public void finished() {
    }

    /**
     * Go from a species to a reaction in two case: Either if the species is a reactant of the
     * reaction -> the reaction cannot be any more or if the species catalyzes the reaction and
     * there is no other catalyst.
     */
    public boolean checkReaction(int reaction, NeighborType neighborType) {
      return !removedReactions.getQuick(reaction) &&
          ((neighborType == NeighborType.Additional && numCatalysts[reaction] == 0)
              || neighborType == NeighborType.Reactant);
    }

    /**
     * Walk from a reaction to a species, if the species is product and not created by another
     * reaction.
     */
    public boolean checkSpecies(int species, NeighborType neighborType) {
      return !removedSpecies.getQuick(species) && network.getSpeciesName(species).length() > 1 &&
          neighborType == NeighborType.Product && numCreated[species] == 0;
    }


    public void reactionFinished(int reaction) {
    }

    public void speciesFinished(int species) {
    }

    public void reactionDiscovered(int reaction) {
      removedReactions.putQuick(reaction, true);
      for (int p : network.getProducts(reaction)) {
        if (!removedSpecies.get(p)) {
          numCreated[p]--;
        }
      }
    }

    public void speciesDiscovered(int species) {
      removedSpecies.putQuick(species, true);
      for (int r : adjListAsCata[species]) {
        if (!removedReactions.get(r)) {
          numCatalysts[r]--;
        }
      }
      for (int r : adjListAsRea[species]) {
        if (!removedReactions.get(r)) {
          numReactants[r]--;
        }
      }
    }


    public Iterable<Integer> getAdditionalReactionNeighbors(int index) {
      return null;
    }

    public Iterable<Integer> getAdditionalSpeciesNeighbors(int index) {
      return adjListAsCata[index];
    }

  }

  /**
   * NetworkSearchAction for the second search
   *
   * @author Florian Erhard
   */
  private class ConnectedComponentIdentificationAction implements NetworkSearchAction {

    BitVector discoveredReactions;
    BitVector discoveredSpecies;

    int[] discoveredReactants;


    public void initialize(Network net) {
      discoveredReactions = new BitVector(removedReactions.size());
      discoveredSpecies = new BitVector(removedSpecies.size());
      discoveredReactants = new int[network.getNumReactions()];
    }

    public void finished() {

      for (int i = 0; i < discoveredReactions.size(); i++) {
        if (!removedReactions.get(i) && !discoveredReactions.get(i)) {
          for (int p : network.getProducts(i)) {
            if (!removedSpecies.get(p)) {
              numCreated[p]--;
            }
          }
        }
      }

      for (int i = 0; i < discoveredSpecies.size(); i++) {
        if (!removedSpecies.get(i) && !discoveredSpecies.get(i)) {
          for (int r : adjListAsCata[i]) {
            if (!removedReactions.get(r)) {
              numCatalysts[r]--;
            }
          }
          for (int r : adjListAsRea[i]) {
            if (!removedReactions.get(r)) {
              numReactants[r]--;
            }
          }
        }
      }

      discoveredReactions.not();
      discoveredSpecies.not();

      removedReactions.or(discoveredReactions);
      removedSpecies.or(discoveredSpecies);
    }

    /**
     * Walk from a species to a reaction if the species is reactant and all other reactants have
     * been visited.
     */
    public boolean checkReaction(int reaction, NeighborType neighborType) {
      return !removedReactions.getQuick(reaction) && neighborType == NeighborType.Reactant &&
          discoveredReactants[reaction] == numReactants[reaction];
    }

    /**
     * Walk from a reaction to a species if the species is a product.
     */
    public boolean checkSpecies(int species, NeighborType neighborType) {
      return !removedSpecies.getQuick(species) && neighborType == NeighborType.Product;
    }

    public void reactionDiscovered(int reaction) {
      discoveredReactions.putQuick(reaction, true);
    }


    public void speciesDiscovered(int species) {
      discoveredSpecies.putQuick(species, true);
      for (int r : adjListAsRea[species]) {
        if (!removedReactions.get(r)) {
          discoveredReactants[r]++;
        }
      }
    }

    public void speciesFinished(int species) {
    }

    public void reactionFinished(int reaction) {
    }


    public Iterable<Integer> getAdditionalReactionNeighbors(int index) {
      return null;
    }


    public Iterable<Integer> getAdditionalSpeciesNeighbors(int index) {
      return null;
    }

  }

}
