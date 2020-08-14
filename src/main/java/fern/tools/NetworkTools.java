package fern.tools;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.jdom.JDOMException;

import cern.colt.bitvector.BitVector;
import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.AnnotationManager;
import fern.network.FeatureNotSupportedException;
import fern.network.KineticConstantPropensityCalculator;
import fern.network.Network;
import fern.network.NetworkLoader;
import fern.network.sbml.MathTree;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import org.sbml.jsbml.ASTNode;

/**
 * Contains various helper methods dealing with the {@link Network} interface.
 *
 * @author Florian Erhard
 */
public class NetworkTools {

  /**
   * Gets the reaction rate constant by setting the reactant amounts to the stoichiometric
   * coefficient.
   *
   * @param net network
   * @param r   reaction index
   * @return reaction rate constant
   */
  public static double getConstantBySettingReactantsToStoich(Network net, int r) {
    int[] reactants = net.getReactants(r);
    int[] stoich = new int[net.getNumSpecies()];
		for (int i = 0; i < reactants.length; i++) {
			stoich[reactants[i]]++;
		}

    net.getAmountManager().save();

    // set amount to the stoich coeff
		for (int s = 0; s < reactants.length; s++) {
			net.getAmountManager().setAmount(reactants[s], stoich[reactants[s]]);
		}

    double re = net.getPropensityCalculator()
        .calculatePropensity(r, net.getAmountManager(), new GillespieSimple(net));

    net.getAmountManager().rollback();
    return re;
  }

  /**
   * Gets whether or not the two given networks contain the same species, the same reactions and
   * yield the same propensities when the amounts of each reactant are respectively 1, 5, 10, 100.
   *
   * @param a first network
   * @param b first network
   * @return whether or not a and b are equal
   */
  public static boolean areEqual(Network a, Network b) {
    // species
		for (int i = 0; i < a.getNumSpecies(); i++) {
			if (b.getSpeciesByName(a.getSpeciesName(i)) < 0) {
				return false;
			}
		}
		if (a.getNumSpecies() != b.getNumSpecies()) {
			return false;
		}

    // reactions
		if (a.getNumReactions() != b.getNumReactions()) {
			return false;
		}

    int[] checkAmounts = {1, 5, 10, 100};
    BitVector testedB = new BitVector(b.getNumReactions());

    a.getAmountManager().save();
    b.getAmountManager().save();
    Simulator dummyA = new GillespieSimple(a);
    Simulator dummyB = new GillespieSimple(b);
    boolean found = true;
    for (int i = 0; i < a.getNumReactions(); i++) {
      found = false;
      for (int j = 0; j < b.getNumReactions(); j++) {
        if (!testedB.get(j) && haveSameStoich(a, i, b, j) && yieldSameProp(a, i, b, j, checkAmounts,
            dummyA, dummyB)) {
          testedB.set(j);
          found = true;
          break;
        }
      }
			if (!found) {
				break;
			}
    }

    a.getAmountManager().rollback();
    b.getAmountManager().rollback();
    return found;
  }

  private static boolean yieldSameProp(Network a, int ra, Network b, int rb, int[] checkAmounts,
      Simulator dummyA, Simulator dummyB) {
    for (int amount : checkAmounts) {
			for (int i = 0; i < a.getReactants(ra).length; i++) {
				a.getAmountManager().setAmount(a.getReactants(ra)[i], amount);
			}
			for (int i = 0; i < b.getReactants(rb).length; i++) {
				b.getAmountManager().setAmount(b.getReactants(rb)[i], amount);
			}

			if (a.getPropensityCalculator().calculatePropensity(ra, a.getAmountManager(), dummyA) != b
					.getPropensityCalculator().calculatePropensity(rb, b.getAmountManager(), dummyB)) {
				return false;
			}
    }
    return true;
  }

  private static boolean haveSameStoich(Network a, int ra, Network b, int rb) {
		if (a.getReactants(ra).length != b.getReactants(rb).length) {
			return false;
		}
		if (a.getProducts(ra).length != b.getProducts(rb).length) {
			return false;
		}

    int[] aReactants = new int[a.getNumSpecies()];
    int[] aProducts = new int[a.getNumSpecies()];

		for (int k = 0; k < a.getReactants(ra).length; k++) {
			aReactants[a.getReactants(ra)[k]]++;
		}
		for (int k = 0; k < a.getProducts(ra).length; k++) {
			aProducts[a.getProducts(ra)[k]]++;
		}

		for (int k = 0; k < b.getReactants(rb).length; k++) {
			if (--aReactants[b.getReactants(rb)[k]] < 0) {
				return false;
			}
		}
		for (int k = 0; k < b.getProducts(rb).length; k++) {
			if (--aProducts[b.getProducts(rb)[k]] < 0) {
				return false;
			}
		}

    return true;
  }


  /**
   * Loads a network from file identifying the type (FernML/SBML).
   *
   * @param file network file
   * @throws JDOMException
   * @throws IOException
   * @throws FeatureNotSupportedException
   * @throws ClassNotFoundException
   * @return network object
   */
  public static Network loadNetwork(File file)
      throws IOException, JDOMException, FeatureNotSupportedException, ClassNotFoundException {
    return NetworkLoader.readNetwork(file);
  }

  /**
   * Gets the species names together with its actual amount in parentheses
   *
   * @param net     network
   * @param species species indices
   * @return species name with amount
   */
  public static String getSpeciesNameWithAmount(Network net, int... species) {
    StringBuilder sb = new StringBuilder();
		for (int s : species) {
			sb.append(net.getSpeciesName(s) + "(" + net.getAmountManager().getAmount(s) + ")\n");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
    return sb.toString();
  }

  /**
   * Gets a string representation of the given reactions and the amounts of each participating
   * species in parentheses.
   *
   * @param net       network
   * @param reactions reaction indices
   * @return reaction string representation with amounts of participating species
   */
  public static String getReactionNameWithAmounts(Network net, int... reactions) {
    StringBuilder sb = new StringBuilder();
    for (int reaction : reactions) {
			for (int i : net.getReactants(reaction)) {
				sb.append(getSpeciesNameWithAmount(net, i) + "+");
			}
      sb.deleteCharAt(sb.length() - 1);
      sb.append("->");
			for (int i : net.getProducts(reaction)) {
				sb.append(getSpeciesNameWithAmount(net, i) + "+");
			}
      sb.deleteCharAt(sb.length() - 1);
      sb.append("\n");
    }
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
    return sb.toString();
  }


  /**
   * Transforms the reaction indices in the given array to the corresponding names.
   *
   * @param net      the network
   * @param reaction reaction indices
   * @return names of the reactions
   */
  public static String[] getReactionNames(Network net, int[] reaction) {
    String[] re = new String[reaction.length];
		for (int i = 0; i < re.length; i++) {
			re[i] = net.getReactionName(reaction[i]);
		}
    return re;
  }

  /**
   * Transforms the species indices in the given array to the corresponding names.
   *
   * @param net     the network
   * @param species species indices
   * @return names of the species
   */
  public static String[] getSpeciesNames(Network net, int[] species) {
    String[] re = new String[species.length];
		for (int i = 0; i < re.length; i++) {
			re[i] = net.getSpeciesName(species[i]);
		}
    return re;
  }

  /**
   * Transforms the species names in the given array to the corresponding indices.
   *
   * @param net         the network
   * @param speciesName species names
   * @return indices of the species
   */
  public static int[] getSpeciesIndices(Network net, String[] speciesName) {
    int[] re = new int[speciesName.length];
    for (int i = 0; i < speciesName.length; i++) {
      re[i] = net.getSpeciesByName(speciesName[i]);
			if (re[i] == -1) {
				throw new IllegalArgumentException("Species " + speciesName[i] + " unknown!");
			}
    }
    return re;
  }

  /**
   * Gets the reaction whose products / reactants are in speciesName
   *
   * @param net         network
   * @param speciesName names of species
   * @return reaction indices
   */
  public static int[] getReactionsOf(Network net, String[] speciesName) {
    BitVector re = new BitVector(net.getNumReactions());
    BitVector species = NumberTools.getContentAsBitVector(getSpeciesIndices(net, speciesName));
    for (int r = 0; r < net.getNumReactions(); r++) {
			for (int reactant : net.getReactants(r)) {
				if (reactant < species.size() && species.get(reactant)) {
					re.set(r);
				}
			}
			for (int product : net.getProducts(r)) {
				if (product < species.size() && species.get(product)) {
					re.set(r);
				}
			}
    }
    return NumberTools.getContentAsArray(re);
  }

  /**
   * Dumps the network to stdout.
   *
   * @param net network
   */
  public static void dumpNetwork(Network net) {
    try {
      dumpNetwork(net, new PrintWriter(System.out));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Dumps the network to the given {@link Writer}.
   *
   * @param net network
   */
  public static void dumpNetwork(Network net, Writer writer) throws IOException {
    AnnotationManager prop = net.getAnnotationManager();
    KineticConstantPropensityCalculator konst = null;
		if (net.getPropensityCalculator() instanceof KineticConstantPropensityCalculator) {
			konst = (KineticConstantPropensityCalculator) net.getPropensityCalculator();
		}

    writer.append(" {");
		for (String typ : prop.getNetworkAnnotationTypes()) {
			writer.append(typ + "=" + prop.getNetworkAnnotation(typ) + ",");
		}
    writer.append("}\n");

    writer.append("\n");
    writer.append("Species:\n");
    for (int i = 0; i < net.getNumSpecies(); i++) {
      writer.append(i + " " + net.getSpeciesName(i));
      writer.append(" {");
			for (String typ : prop.getSpeciesAnnotationTypes(i)) {
				writer.append(typ + "=" + prop.getSpeciesAnnotation(i, typ) + ",");
			}
      writer.append("}\n");
    }
    writer.append("\n");
    writer.append("Reactions:\n");
    for (int i = 0; i < net.getNumReactions(); i++) {
      writer.append(i + " " + net.getReactionName(i));
			if (konst != null) {
				writer.append(" k=" + konst.getConstant(i));
			}
      writer.append(" {");
			for (String typ : prop.getReactionAnnotationTypes(i)) {
				writer.append(typ + "=" + prop.getReactionAnnotation(i, typ) + ",");
			}
      writer.append("}\n");
      writer.flush();
    }

    writer.append("Species: " + net.getNumSpecies() + "\n");
    writer.append("Reactions: " + net.getNumReactions() + "\n");
    writer.flush();
  }

  /**
   * Dumps the MathTree to stdout.
   *
   * @param tree mathtree
   */
  public static void dumpMathTree(MathTree tree) {
    try {
      dumpMathTree(tree, new PrintWriter(System.out));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Dumps the math tree to the given {@link Writer}.
   *
   * @param tree mathtree
   */
  public static void dumpMathTree(MathTree tree, Writer writer) throws IOException {
    dumpMathTreeNode(tree.getCopiedAST(), writer, new StringBuilder());
    writer.flush();
  }

  private static void dumpMathTreeNode(ASTNode astNode, Writer writer, StringBuilder indend)
      throws IOException {
    writer.write(indend.toString());
    writer.write(astNode.toString());
    writer.write("\n");
    indend.append(" ");
		for (ASTNode child : astNode.getChildren()) {
			dumpMathTreeNode(child, writer, indend);
		}
    indend.deleteCharAt(0);
  }

  /**
   * Copies the actual amount of the species in the network to its initial amount.
   *
   * @param net network.
   */
  public static void useActualAmountAsInitialAmount(Network net) {
		for (int i = 0; i < net.getNumSpecies(); i++) {
			net.setInitialAmount(i, net.getAmountManager().getAmount(i));
		}
  }


}
