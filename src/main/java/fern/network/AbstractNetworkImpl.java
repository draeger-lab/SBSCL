package fern.network;

import java.util.Map;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

/**
 * Base implementation for the {@link Network} interface. Implementing class only have to create all
 * the protected fields.
 *
 * @author Florian Erhard
 */
public abstract class AbstractNetworkImpl implements Network {


  /**
   * Stores the {@link PropensityCalculator} of the network.
   */
  protected PropensityCalculator propensitiyCalculator = null;
  /**
   * Stores the {@link AmountManager} of the network.
   */
  protected AmountManager amountManager = null;
  /**
   * Stores the {@link AnnotationManager} of the network.
   */
  protected AnnotationManager annotationManager = null;
  /**
   * Stores the adjacency lists of reactions to their products.
   */
  protected int[][] adjListPro = null;
  /**
   * Stores the adjacency lists of reactions to their reactants.
   */
  protected int[][] adjListRea = null;
  /**
   * Stores a mapping from species names to their indices.
   */
  protected Map<String, Integer> speciesIdToIndex = null;
  /**
   * Stores a mapping from species indices to their names.
   */
  protected String[] indexToSpeciesId = null;
  /**
   * Stores the network's identifier.
   */
  protected String name = null;


  /**
   * Reminds extending class to fill {@link AbstractNetworkImpl#annotationManager}.
   */
  protected abstract void createAnnotationManager();

  /**
   * Reminds extending class to fill {@link AbstractNetworkImpl#speciesIdToIndex} and {@link
   * AbstractNetworkImpl#indexToSpeciesId}.
   */
  protected abstract void createSpeciesMapping();

  /**
   * Reminds extending class to fill {@link AbstractNetworkImpl#adjListPro} and {@link
   * AbstractNetworkImpl#adjListRea}.
   */
  protected abstract void createAdjacencyLists();

  /**
   * Reminds extending class to fill {@link AbstractNetworkImpl#amountManager}.
   */
  protected abstract void createAmountManager();

  /**
   * Reminds extending class to fill {@link AbstractNetworkImpl#propensitiyCalculator}.
   */
  protected abstract void createPropensityCalculator() throws ModelOverdeterminedException;


  /**
   * Create the network and give it an identifier.
   *
   * @param name identifier for the network
   */
  public AbstractNetworkImpl(String name) {
    this.name = name;
  }

  public AmountManager getAmountManager() {
    return amountManager;
  }

  public PropensityCalculator getPropensityCalculator() {
    return propensitiyCalculator;
  }

  public AnnotationManager getAnnotationManager() {
    return annotationManager;
  }

  public int getNumReactions() {
    return adjListPro.length;
  }

  public int getNumSpecies() {
    return speciesIdToIndex.size();
  }

  public int[] getProducts(int reaction) {
    return adjListPro[reaction];
  }

  public int[] getReactants(int reaction) {
    return adjListRea[reaction];
  }

  public int getSpeciesByName(String name) {
    return speciesIdToIndex.getOrDefault(name, -1);
  }


  /**
   * Gets the mapping from species names to their indices.
   *
   * @return species mapping
   */
  public Map<String, Integer> getSpeciesMapping() {
    return speciesIdToIndex;
  }

  public String getSpeciesName(int index) {
    return indexToSpeciesId[index];
  }

  public String getReactionName(int index) {
    StringBuilder sb = new StringBuilder();
		for (int i : getReactants(index)) {
			sb.append(getSpeciesName(i) + "+");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
    sb.append("->");
		for (int i : getProducts(index)) {
			sb.append(getSpeciesName(i) + "+");
		}
		if (getProducts(index).length > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
    return sb.toString();
  }

  public String getName() {
    return name;
  }


}
