package fern.network.modification;

import java.util.Collection;
import java.util.LinkedList;

import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.AmountManager;
import fern.network.AnnotationManager;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.network.creation.AutocatalyticNetwork;
import fern.simulation.Simulator;

/**
 * Doubles each reaction in a way that each original unidirectional reaction becomes reversible.As
 * proposed by {@link ModifierNetwork}, the reactions are not copied but the indices are
 * redirected.
 * <p>
 * Note: The reverse reactions do not have any annotation, unless in the original network the
 * reactions have annotation with fields ending with {@code REVERSIBLE_SUFFIX}. If such
 * annotations are present in the original network, the corresponding reactions in the reversible
 * network will not have these annotations but the corresponding reversible reactions will have
 * these annotations without this suffix (e.g., if you create an {@link AutocatalyticNetwork}, the
 * reactions have annotations {@code Catalyst} and {@code  CatalystReversible}, if you
 * create a {@code ReversibleNetwork} out of this, each reaction will have only the annotation
 * {@code Catalyst}).
 * <p>
 * For the new reactions you have to specify a new {@link PropensityCalculator} whose index space is
 * equal to the original network's index space. If you have, for instance, in your original network
 * two reactions, A + A &#8594; B, B &#8594; C, the {@code PropensityCalculatory} has to calculate the
 * propensity for A + A &#8592; B when index 0 is given, the propensity for B &#8592; C, when index 1 is given.
 *
 * @author Florian Erhard
 */
public class ReversibleNetwork extends ModifierNetwork {

  /**
   * Suffix which marks annotations for virtually created reverse reactions. If there are no
   * annotations with this suffix in the original network, the reverse reactions will have no
   * annotations.
   */
  public static final String REVERSIBLE_SUFFIX = "Reversible";

  private AmountManager amountManager = null;
  private PropensityCalculator reversiblePropensityCalculator = null;

  /**
   * Creates a new network from an original network and virtually creates for each reaction a new
   * inverse reaction. For the reverse reactions a {@link PropensityCalculator} has to be given.
   *
   * @param originalNet                    the original network
   * @param reversiblePropensityCalculator the {@code PropensityCalculator} for the reverse
   *                                       reactions
   */
  public ReversibleNetwork(Network originalNet,
    PropensityCalculator reversiblePropensityCalculator) {
    super(originalNet);
    this.reversiblePropensityCalculator = reversiblePropensityCalculator;
    amountManager = new DefaultAmountManager(this);
  }

  /**
   * Gets the {@link AmountManager} for the modified network.
   *
   * @return {@code AmountManager} for the modified network
   */
  @Override
  public AmountManager getAmountManager() {
    return amountManager;
  }

  /**
   * Gets the number of reactions in the modified network (which is 2*number of reaction in the
   * original network).
   *
   * @return number of reactions in the modified network
   */
  @Override
  public int getNumReactions() {
    return getParentNetwork().getNumReactions() * 2;
  }

  /**
   * Gets the products of a reaction
   *
   * @param reaction index of the reaction
   * @return indices of the products
   */
  @Override
  public int[] getProducts(int reaction) {
    return (reaction < getParentNetwork().getNumReactions()) ? getParentNetwork()
      .getProducts(reaction)
      : getParentNetwork().getReactants(reaction % getParentNetwork().getNumReactions());
  }

  /**
   * Gets the reactants of a reaction
   *
   * @param reaction index of the reaction
   * @return indices of the reactants
   */
  @Override
  public int[] getReactants(int reaction) {
    return (reaction < getParentNetwork().getNumReactions()) ? getParentNetwork()
      .getReactants(reaction)
      : getParentNetwork().getProducts(reaction % getParentNetwork().getNumReactions());
  }

  /**
   * Gets the {@link PropensityCalculator} for the modified network. If the original
   * {@code PropensityCalculator} and the one for the reverse reactions is a {@link
   * AbstractKineticConstantPropensityCalculator}, an {@code AbstractKineticConstantPropensityCalculator}
   * is returned.
   *
   * @return the {@code PropensityCalculator} for the modified network
   */
  @Override
  public PropensityCalculator getPropensityCalculator() {
    if ((getParentNetwork()
        .getPropensityCalculator() instanceof AbstractKineticConstantPropensityCalculator)
        && (reversiblePropensityCalculator instanceof AbstractKineticConstantPropensityCalculator)) {
      return new AbstractKineticConstantPropensityCalculator(new int[0][]) {

        @Override
        public double calculatePropensity(int reaction,
          AmountManager amount, Simulator sim) {
          if (reaction < getParentNetwork().getNumReactions()) {
            return getParentNetwork().getPropensityCalculator()
                .calculatePropensity(reaction, amount, sim);
          } else {
            return reversiblePropensityCalculator
                .calculatePropensity(reaction % getParentNetwork().getNumReactions(), amount, sim);
          }
        }

        @Override
        public double getConstant(int i) {

          if (i < getParentNetwork().getNumReactions()) {
            return ((AbstractKineticConstantPropensityCalculator) getParentNetwork()
                .getPropensityCalculator()).getConstant(i);
          } else {
            return ((AbstractKineticConstantPropensityCalculator) reversiblePropensityCalculator)
                .getConstant(i % getParentNetwork().getNumReactions());
          }
        }

        @Override
        public double calculatePartialDerivative(int reaction,
          AmountManager amount, int reactantIndex, double volume) {
          if (reaction < getParentNetwork().getNumReactions()) {
            return ((AbstractKineticConstantPropensityCalculator) getParentNetwork()
                .getPropensityCalculator())
                .calculatePartialDerivative(reaction, amount, reactantIndex, volume);
          } else {
            return ((AbstractKineticConstantPropensityCalculator) reversiblePropensityCalculator)
                .calculatePartialDerivative(reaction % getParentNetwork().getNumReactions(), amount,
                  reactantIndex, volume);
          }
        }

        @Override
        public double getConstantFromDeterministicRateConstant(
          double k, int reaction, double V) {
          if (reaction < getParentNetwork().getNumReactions()) {
            return ((AbstractKineticConstantPropensityCalculator) getParentNetwork()
                .getPropensityCalculator())
                .getConstantFromDeterministicRateConstant(k, reaction, V);
          } else {
            return ((AbstractKineticConstantPropensityCalculator) reversiblePropensityCalculator)
                .getConstantFromDeterministicRateConstant(k,
                  reaction % getParentNetwork().getNumReactions(), V);
          }
        }


      };
    } else {
      return new PropensityCalculator() {
        @Override
        public double calculatePropensity(int reaction, AmountManager amount, Simulator sim) {
          if (reaction < getParentNetwork().getNumReactions()) {
            return getParentNetwork().getPropensityCalculator()
                .calculatePropensity(reaction, amount, sim);
          } else {
            return reversiblePropensityCalculator
                .calculatePropensity(reaction % getParentNetwork().getNumReactions(), amount, sim);
          }
        }
      };
    }
  }

  /**
   * Gets the {@link AnnotationManager} for the modified network.
   *
   * @return {@code AnnotationManager} for the modified network.
   */
  @Override
  public AnnotationManager getAnnotationManager() {
    final AnnotationManager ori = getParentNetwork().getAnnotationManager();
    final int numReactions = getParentNetwork().getNumReactions();
    return new AnnotationManager() {


      @Override
      public boolean containsNetworkAnnotation(String typ) {
        return ori.containsNetworkAnnotation(typ);
      }

      @Override
      public boolean containsReactionAnnotation(int reaction, String typ) {
        typ = reaction < numReactions ? typ : typ + REVERSIBLE_SUFFIX;
        reaction = reaction % numReactions;
        return ori.containsReactionAnnotation(reaction, typ);
      }

      @Override
      public boolean containsSpeciesAnnotation(int species, String typ) {
        return ori.containsSpeciesAnnotation(species, typ);
      }

      @Override
      public String getNetworkAnnotation(String typ) {
        return ori.getNetworkAnnotation(typ);
      }

      @Override
      public Collection<String> getNetworkAnnotationTypes() {
        return ori.getNetworkAnnotationTypes();
      }

      @Override
      public String getReactionAnnotation(int reaction, String typ) {
        typ = reaction < numReactions ? typ : typ + REVERSIBLE_SUFFIX;
        reaction = reaction % numReactions;
        return ori.getReactionAnnotation(reaction, typ);
      }

      @Override
      public Collection<String> getReactionAnnotationTypes(int reaction) {
        Collection<String> oriList = ori.getReactionAnnotationTypes(reaction % numReactions);
        Collection<String> re = new LinkedList<>();
        for (String s : oriList) {
          if (s.endsWith(REVERSIBLE_SUFFIX) && (reaction >= numReactions)) {
            re.add(s.substring(0, s.length() - REVERSIBLE_SUFFIX.length()));
          } else if (!s.endsWith(REVERSIBLE_SUFFIX) && (reaction < numReactions)) {
            re.add(s);
          }
        }
        return re;
      }

      @Override
      public String getSpeciesAnnotation(int species, String typ) {
        return ori.getSpeciesAnnotation(species, typ);
      }

      @Override
      public Collection<String> getSpeciesAnnotationTypes(int species) {
        return ori.getSpeciesAnnotationTypes(species);
      }

      @Override
      public void setNetworkAnnotation(String typ, String annotation) {
        ori.setNetworkAnnotation(typ, annotation);
      }

      @Override
      public void setReactionAnnotation(int reaction, String typ,
        String annotation) {
        typ = reaction < numReactions ? typ : typ + REVERSIBLE_SUFFIX;
        reaction = reaction % numReactions;
        ori.setReactionAnnotation(reaction, typ, annotation);
      }

      @Override
      public void setSpeciesAnnotation(int species, String typ,
        String annotation) {
        ori.setSpeciesAnnotation(species, typ, annotation);
      }


    };
  }


  /**
   * Gets a string representation of the reaction in the modified network.
   *
   * @param index index of the reaction
   * @return string representation of the reaction
   */
  @Override
  public String getReactionName(int index) {
    StringBuilder sb = new StringBuilder();
    for (int i : getReactants(index)) {
      sb.append(getSpeciesName(i) + "+");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("->");
    for (int i : getProducts(index)) {
      sb.append(getSpeciesName(i) + "+");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }


}

