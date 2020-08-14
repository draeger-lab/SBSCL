/*
 * Created on 03.08.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.algorithm;

import fern.network.ComplexDependenciesPropensityCalculator;
import fern.network.Network;
import fern.tools.NumberTools;
import java.util.LinkedList;
import java.util.Map;

/**
 * Implements a dependency graph. It is a directed graph whose vertices are the reactions of a
 * network. From vertex i to vertex j is an edge iff i=j or there is at least one species that
 * changes quantity when reaction i fires and is reactant of reaction j.
 * <p>
 * This definition implies that if a reaction i fires, the propensities of each descendant in the
 * dependency graph has to be recalculated and no other.
 *
 *
 * <p>
 * For reference see M.A.Gibson and J.Bruck, Efficient Exact Stochastic Simulation of Chemical
 * Systems with Many Species and Many Channels, J.Phys.Chem.A., Vol 104, no 9, 2000
 *
 * @author Florian Erhard
 * @see GillespieEnhanced
 * @see GibsonBruckSimulator
 */
public class DependencyGraph {

  private LinkedList<Integer>[] dependsOn = null;

  /**
   * Creates the dependency graph for a given network
   *
   * @param net the network
   */
  @SuppressWarnings("unchecked")
  public DependencyGraph(Network net) {
    dependsOn = new LinkedList[net.getNumReactions()];

    Map<Integer, Integer>[] reactants = new Map[net.getNumReactions()];
		for (int i = 0; i < reactants.length; i++) {
			reactants[i] = NumberTools.createHistogramAsMap(net.getReactants(i));
		}

    Map<Integer, Integer>[] products = new Map[net.getNumReactions()];
		for (int i = 0; i < products.length; i++) {
			products[i] = NumberTools.createHistogramAsMap(net.getProducts(i));
		}

    for (int i = 0; i < dependsOn.length; i++) {
      dependsOn[i] = new LinkedList();
      for (int j = 0; j < net.getNumReactions(); j++) {
				if (haveToCreateEdgeFromTo(i, j, net, reactants, products)) {
					dependsOn[i].add(j);
				}
      }
    }
  }

  /**
   * Gets the descendants of a reaction in the dependency graph
   *
   * @param reaction index of the reaction
   * @return list of dependent reactions
   */
  public LinkedList<Integer> getDependent(int reaction) {
    return dependsOn[reaction];
  }

  private boolean haveToCreateEdgeFromTo(int i, int j, Network net,
      Map<Integer, Integer>[] reactants, Map<Integer, Integer>[] products) {
    // if and only if there is at least one molecule that:
    //  1. changes quantity, when i is executed
    //  2. is reactant of j
    // or i = j
    // or we have a ComplexDependenciesPropensityCalculator:
    // one molecule that
    //  1. is dependent on j
    //  2. changes quantitiy
		if (i == j) {
			return true;
		}

    for (int j_reactant : net.getReactants(j)) {
      int i_reactant = reactants[i].containsKey(j_reactant) ? reactants[i].get(j_reactant) : 0;
      int i_product = products[i].containsKey(j_reactant) ? products[i].get(j_reactant) : 0;
			if (i_reactant != i_product) {
				return true;
			}
    }

		if (net.getPropensityCalculator() instanceof ComplexDependenciesPropensityCalculator) {
			for (int j_reactant : ((ComplexDependenciesPropensityCalculator) net
					.getPropensityCalculator()).getKineticLawSpecies(j)) {
				int i_reactant = reactants[i].containsKey(j_reactant) ? reactants[i].get(j_reactant) : 0;
				int i_product = products[i].containsKey(j_reactant) ? products[i].get(j_reactant) : 0;
				if (i_reactant != i_product) {
					return true;
				}
			}
		}

    return false;
  }


}
