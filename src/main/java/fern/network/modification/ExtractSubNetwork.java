package fern.network.modification;

import cern.colt.bitvector.BitVector;
import fern.network.AmountManager;
import fern.network.AnnotationManager;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.simulation.Simulator;
import fern.tools.NumberTools;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Extracts some reactions / species from a given net to form a new network. As proposed by {@link
 * ModifierNetwork}, the network is not copied but the indices are redirected.
 * <p>
 * The subnet to be extracted has to be given by {@link BitVector}s containing a 1 for an index to
 * be in the extracted subnet.
 * <p>
 * This class can be used e.g. to extract only the autocatalytic set of an evolved network.
 *
 * @author Florian Erhard
 */
public class ExtractSubNetwork extends ModifierNetwork {

  private AmountManager amountManager = null;
  private AmountManager redirectingAmountManager = null;

  private int[] reactionsToOriginal = null;
  private int[] speciesToOriginal = null;
  private int[] speciesFromOriginal = null;

  /**
   * Extracts a given subnet from a network. The subnet has to be given by {@link BitVector}s
   * containing a 1 for each index which has to be in the extracted subnet.
   *
   * @param originalNet network containing the subnet
   * @param reactions   reactions of the subnet
   * @param species     species of the subnet
   */
  public ExtractSubNetwork(Network originalNet, BitVector reactions, BitVector species) {
    super(originalNet);
    reactionsToOriginal = NumberTools.getContentAsArray(reactions);
    speciesToOriginal = NumberTools.getContentAsArray(species);
    speciesFromOriginal = NumberTools.createInverse(speciesToOriginal);
    this.amountManager = new DefaultAmountManager(this);
    createRedirectingAmountManager();
  }

  private void createRedirectingAmountManager() {
    redirectingAmountManager = new DefaultAmountManager(null) {
      @Override
      public long getAmount(int species) {
        return amountManager.getAmount(speciesFromOriginal[species]);
      }
    };
  }

  /**
   * Redirects a reaction index from the subnet index space to the original index space
   *
   * @param reaction index in subnet index space
   * @return index in original index space
   */
  protected int getOriginalReaction(int reaction) {
    return reactionsToOriginal[reaction];
  }

  /**
   * Redirects a species index from the subnet index space to the original index space
   *
   * @param species index in subnet index space
   * @return index in original index space
   */
  protected int getOriginalSpecies(int species) {
    return speciesToOriginal[species];
  }

  private int[] translateAndTrimSpecies(int[] ori) {
    LinkedList<Integer> re = new LinkedList<Integer>();
    for (int i : ori) {
      if (speciesFromOriginal[i] >= 0) {
        re.add(speciesFromOriginal[i]);
      }
    }
    return NumberTools.toIntArray(re);
  }


  /**
   * Gets an {@link AnnotationManager} for the redirected index space.
   *
   * @return the <code>AnnotationManager</code> object
   */
  @Override
  public AnnotationManager getAnnotationManager() {
    final AnnotationManager ori = getParentNetwork().getAnnotationManager();
    return new AnnotationManager() {

      public boolean containsNetworkAnnotation(String typ) {
        return ori.containsNetworkAnnotation(typ);
      }

      public boolean containsReactionAnnotation(int reaction, String typ) {
        return ori.containsReactionAnnotation(getOriginalReaction(reaction), typ);
      }

      public boolean containsSpeciesAnnotation(int species, String typ) {
        return ori.containsSpeciesAnnotation(getOriginalSpecies(species), typ);
      }

      public String getNetworkAnnotation(String typ) {
        return ori.getNetworkAnnotation(typ);
      }

      public Collection<String> getNetworkAnnotationTypes() {
        return ori.getNetworkAnnotationTypes();
      }

      public String getReactionAnnotation(int reaction, String typ) {
        return ori.getReactionAnnotation(getOriginalReaction(reaction), typ);
      }

      public Collection<String> getReactionAnnotationTypes(int reaction) {
        return ori.getReactionAnnotationTypes(getOriginalReaction(reaction));
      }

      public String getSpeciesAnnotation(int species, String typ) {
        return ori.getSpeciesAnnotation(getOriginalSpecies(species), typ);
      }

      public Collection<String> getSpeciesAnnotationTypes(int species) {
        return ori.getSpeciesAnnotationTypes(getOriginalSpecies(species));
      }

      public void setNetworkAnnotation(String typ, String annotation) {
        ori.setNetworkAnnotation(typ, annotation);
      }

      public void setReactionAnnotation(int reaction, String typ,
          String annotation) {
        ori.setReactionAnnotation(getOriginalReaction(reaction), typ, annotation);
      }

      public void setSpeciesAnnotation(int species, String typ,
          String annotation) {
        ori.setSpeciesAnnotation(getOriginalSpecies(species), typ, annotation);
      }


    };
  }

  /**
   * Gets the number of reactions in the extracted subnet.
   *
   * @return number of reactions
   */
  @Override
  public int getNumReactions() {
    return reactionsToOriginal.length;
  }

  /**
   * Gets the number of species in the extracted subnet.
   *
   * @return number of species
   */
  @Override
  public int getNumSpecies() {
    return speciesToOriginal.length;
  }

  /**
   * Gets the products of a reaction.
   *
   * @param reaction the index of the reaction
   * @return indices of the products
   */
  @Override
  public int[] getProducts(int reaction) {
    int[] ori = getParentNetwork().getProducts(getOriginalReaction(reaction));
    return translateAndTrimSpecies(ori);
  }


  /**
   * Gets the reactants of a reaction.
   *
   * @param reaction the index of the reaction
   * @return indices of the reactants
   */
  @Override
  public int[] getReactants(int reaction) {
    int[] ori = getParentNetwork().getReactants(getOriginalReaction(reaction));
    return translateAndTrimSpecies(ori);
  }


  public long getInitialAmount(int species) {
    return getParentNetwork().getInitialAmount(getOriginalSpecies(species));
  }

  public void setInitialAmount(int species, long value) {
    getParentNetwork().setInitialAmount(getOriginalSpecies(species), value);
  }


  /**
   * Gets the name of the species with given index.
   *
   * @param index index of the species
   * @return name of the species
   */
  @Override
  public String getSpeciesName(int index) {
    return getParentNetwork().getSpeciesName(getOriginalSpecies(index));
  }

  /**
   * Gets the index of the species by its name.
   *
   * @param name name of the species
   * @return index of the species
   */
  @Override
  public int getSpeciesByName(String name) {
    return speciesFromOriginal[getParentNetwork().getSpeciesByName(name)];
  }

  /**
   * Gets the {@link AmountManager} for the extracted subnet.
   *
   * @return the amount manager
   */
  @Override
  public AmountManager getAmountManager() {
    return amountManager;
  }

  /**
   * Gets the {@link PropensityCalculator} for the extracted subnet.
   *
   * @return the propensity calculator
   */
  @Override
  public PropensityCalculator getPropensityCalculator() {
    final PropensityCalculator ori = getParentNetwork().getPropensityCalculator();
    return new PropensityCalculator() {
      public double calculatePropensity(int reaction,
          AmountManager amount, Simulator sim) {
        return ori
            .calculatePropensity(getOriginalReaction(reaction), redirectingAmountManager, sim);
      }
    };
  }

  /**
   * Gets a string representation of the reaction
   *
   * @param index index of the reaction
   * @return string representation of the reaction
   */
  @Override
  public String getReactionName(int index) {
    return getParentNetwork().getReactionName(getOriginalReaction(index));
  }


}
