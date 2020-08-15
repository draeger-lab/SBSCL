package fern.network.modification;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.AmountManager;
import fern.network.AnnotationManager;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.network.creation.AutocatalyticNetwork;
import fern.tools.NumberTools;


/**
 * Modifies the network by adding reactions X+C -> Y+C (where C is each catalyst of the original
 * reaction). If a reaction has n catalysts, there will be n+1 reactions generated. It is only
 * possible to create a <code>CatalysedNetwork</code> out of a {@link AutocatalyticNetwork} (or at
 * least of a {@link ModifierNetwork} whose original network is a <code>AutocatalyticNetwork</code>).
 * <p>
 * The {@link AmountManager} automatically monitors the food molecules amounts and whenever it
 * changes, it is reset to the initial value (given by {@link AutocatalyticNetwork#getMonomerAmount()}.
 * <p>
 * The {@link PropensityCalculator}'s constant is {@link AutocatalyticNetwork#getCatalyzedKineticConstant()}
 * for each reaction with a catalyst and {@link AutocatalyticNetwork#getUncatalyzedKineticConstant()}
 * for the other ones.
 * <p>
 * The {@link AnnotationManager} uses the underlying one but removes the {@link
 * AutocatalyticNetwork#CATALYSTS_FIELD} and the field <code>Autocatalytic</code> from not catalyzed
 * reactions.
 *
 * @author Florian Erhard
 */
public class CatalysedNetwork extends ModifierNetwork {


  private int[] reactionToOriginal;
  private int[] catalyst;
  private PropensityCalculator propensityCalculator = null;
  private AmountManager amountManager = null;
  private int[][] adjListRea;
  private int[][] adjListPro;

  /**
   * Create a catalyzed network from an original network.
   *
   * @param originalNet the original network
   */
  public CatalysedNetwork(Network originalNet) {
    super(originalNet);

    if (!(getOriginalNetwork() instanceof AutocatalyticNetwork)) {
      throw new IllegalArgumentException("Original network must be an AutocatalyticNetwork!");
    }

    AnnotationManager anno = originalNet.getAnnotationManager();
    LinkedList<Integer> rTOCreate = new LinkedList<Integer>();
    LinkedList<Integer> cataCreate = new LinkedList<Integer>();
    for (int i = 0; i < originalNet.getNumReactions(); i++) {
      rTOCreate.add(i);
      cataCreate.add(-1);
      if (anno.containsReactionAnnotation(i, AutocatalyticNetwork.CATALYSTS_FIELD)) {
        for (String cata : anno.getReactionAnnotation(i, AutocatalyticNetwork.CATALYSTS_FIELD)
            .split(" ")) {
          rTOCreate.add(i);
          cataCreate.add(originalNet.getSpeciesByName(cata));
        }
      }
    }

    reactionToOriginal = NumberTools.toIntArray(rTOCreate);
    catalyst = NumberTools.toIntArray(cataCreate);

    adjListRea = new int[getNumReactions()][];
    for (int i = 0; i < adjListRea.length; i++) {
      if (catalyst[i] == -1) {
        adjListRea[i] = super.getReactants(reactionToOriginal[i]);
      } else {
        int[] ori = super.getReactants(reactionToOriginal[i]);
        int[] re = new int[ori.length + 1];
        System.arraycopy(ori, 0, re, 0, ori.length);
        re[re.length - 1] = catalyst[i];
        adjListRea[i] = re;
      }
    }

    adjListPro = new int[getNumReactions()][];
    for (int i = 0; i < adjListPro.length; i++) {
      if (catalyst[i] == -1) {
        adjListPro[i] = super.getProducts(reactionToOriginal[i]);
      } else {
        int[] ori = super.getProducts(reactionToOriginal[i]);
        int[] re = new int[ori.length + 1];
        System.arraycopy(ori, 0, re, 0, ori.length);
        re[re.length - 1] = catalyst[i];
        adjListPro[i] = re;
      }
    }

    propensityCalculator = new AbstractKineticConstantPropensityCalculator(adjListRea) {
      public double getConstant(int i) {
        return catalyst[i] != -1 ? ((AutocatalyticNetwork) getOriginalNetwork())
            .getCatalyzedKineticConstant()
            : ((AutocatalyticNetwork) getOriginalNetwork()).getUncatalyzedKineticConstant();
      }
    };

    amountManager = new DefaultAmountManager(this) {
      @Override
      public void performReaction(int reaction, int times) {
        super.performReaction(reaction, times);
        for (int i = 0; i < ((AutocatalyticNetwork) getOriginalNetwork()).getNumMonomers(); i++) {
          setAmount(i, ((AutocatalyticNetwork) getOriginalNetwork()).getMonomerAmount());
        }
      }
    };
  }

  @Override
  public int getNumReactions() {
    return reactionToOriginal.length;
  }

  @Override
  public int[] getReactants(int reaction) {
    return adjListRea[reaction];
  }

  @Override
  public int[] getProducts(int reaction) {
    return adjListPro[reaction];
//		if (catalyst[reaction]==-1) return super.getProducts(reactionToOriginal[reaction]);
//		else {
//			int[] ori = super.getProducts(reactionToOriginal[reaction]);
//			int[] re = new int[ori.length+1];
//			System.arraycopy(ori, 0, re, 0, ori.length);
//			re[re.length-1] = catalyst[reaction];
//			return re;
//		}
  }

  private int getOriginalReaction(int reaction) {
    return reactionToOriginal[reaction];
  }

  @Override
  public PropensityCalculator getPropensityCalculator() {
    return propensityCalculator;
  }

  @Override
  public AmountManager getAmountManager() {
    return amountManager;
  }

  @Override
  public AnnotationManager getAnnotationManager() {
    final AnnotationManager ori = getParentNetwork().getAnnotationManager();
    return new AnnotationManager() {

      public boolean containsNetworkAnnotation(String typ) {
        return ori.containsNetworkAnnotation(typ);
      }

      public boolean containsReactionAnnotation(int reaction, String typ) {
        boolean re = ori.containsReactionAnnotation(getOriginalReaction(reaction), typ);
        if (typ.equals(AutocatalyticNetwork.CATALYSTS_FIELD) || typ.equals("Autocatalytic")) {
          return catalyst[reaction] != -1 && re;
        } else {
          return re;
        }
      }

      public boolean containsSpeciesAnnotation(int species, String typ) {
        return ori.containsSpeciesAnnotation((species), typ);
      }

      public String getNetworkAnnotation(String typ) {
        return ori.getNetworkAnnotation(typ);
      }

      public Collection<String> getNetworkAnnotationTypes() {
        return ori.getNetworkAnnotationTypes();
      }

      public String getReactionAnnotation(int reaction, String typ) {
        String re = ori.getReactionAnnotation(getOriginalReaction(reaction), typ);
        if (!containsReactionAnnotation(reaction, typ)) {
          return null;
        } else {
          return re;
        }
      }

      public Collection<String> getReactionAnnotationTypes(int reaction) {
        Collection<String> re = ori.getReactionAnnotationTypes(getOriginalReaction(reaction));
        Iterator<String> it = re.iterator();
        while (it.hasNext()) {
          if (!containsReactionAnnotation(reaction, it.next())) {
            it.remove();
          }
        }
        return re;
      }

      public String getSpeciesAnnotation(int species, String typ) {
        return ori.getSpeciesAnnotation((species), typ);
      }

      public Collection<String> getSpeciesAnnotationTypes(int species) {
        return ori.getSpeciesAnnotationTypes((species));
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
        ori.setSpeciesAnnotation((species), typ, annotation);
      }


    };
  }

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
