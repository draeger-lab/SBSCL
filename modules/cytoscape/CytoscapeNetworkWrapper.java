package fern.cytoscape;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import fern.network.AbstractNetworkImpl;
import fern.network.AmountManager;
import fern.network.ArrayKineticConstantPropensityCalculator;
import giny.model.Edge;
import giny.model.Node;
import giny.view.NodeView;

public class CytoscapeNetworkWrapper extends AbstractNetworkImpl {


  CyNetwork net = null;
  CyNetworkView cyView = null;
  CyAttributes nodeAttr = null;
  NetworkChecker checker = null;
  NodeView[] reactions = null;
  NodeView[] species = null;


  public CytoscapeNetworkWrapper(NetworkChecker checker, CyNetwork net, CyNetworkView cyView,
      CyAttributes nodeAttr) {
    super(net.getIdentifier());
    this.checker = checker;
    this.net = net;
    this.cyView = cyView;
    this.nodeAttr = nodeAttr;

    createAnnotationManager();
    createArrays();
    createSpeciesMapping();
    createAmountManager();
    createAdjacencyLists();
    createPropensityCalulator();

  }

  public long getInitialAmount(int species) {
//		return getNodeAttributeObject().getDoubleAttribute(getSpeciesName(species), INITIAL_AMOUNT_IDENTIFIER).longValue();
    return checker.getNodeParameter().getSpeciesInitialAmount(this.species[species].getNode());
  }

  public void setInitialAmount(int species, long value) {
//		getNodeAttributeObject().setAttribute(getSpeciesName(species), INITIAL_AMOUNT_IDENTIFIER, (double)value);
    checker.getNodeParameter().setSpeciesInitialAmount(this.species[species].getNode(), value);
  }

  @SuppressWarnings("unchecked")
  private void createArrays() {
//		CytoscapeAnnotationManager prop = (CytoscapeAnnotationManager)annotationManager;

    LinkedList<NodeView> reactionCreate = new LinkedList<NodeView>();
    LinkedList<NodeView> speciesCreate = new LinkedList<NodeView>();

    for (Iterator<NodeView> i = cyView.getNodeViewsIterator(); i.hasNext(); ) {
      NodeView nodeView = i.next();
      CyNode node = (CyNode) nodeView.getNode();
      //if (nodeAttr.getStringAttribute(node.getIdentifier(), prop.getTypeIdentifier() ).equals(prop.getSpeciesIdentifier()))
			if (checker.getNodeClassifier().isSpeciesNode(node)) {
				speciesCreate.add(nodeView);
			}
//        	else if (nodeAttr.getStringAttribute(node.getIdentifier(), prop.getTypeIdentifier()).equals(prop.getReactionIdentifier()))
			else if (checker.getNodeClassifier().isReactionNode(node)) {
				reactionCreate.add(nodeView);
			} else {
				throw new IllegalArgumentException("NodeType unknown");
			}
    }

    reactions = reactionCreate.toArray(new NodeView[reactionCreate.size()]);
    species = speciesCreate.toArray(new NodeView[speciesCreate.size()]);
  }

  @Override
  protected void createAdjacencyLists() {

//		CytoscapeAnnotationManager prop = (CytoscapeAnnotationManager) getAnnotationManager();

    adjListPro = new int[reactions.length][];
    adjListRea = new int[reactions.length][];
    for (int i = 0; i < reactions.length; i++) {
      // get cytoscape's internal indices
      int[] edges = net
          .getAdjacentEdgeIndicesArray(net.getIndex(reactions[i].getNode()), true, true, true);
      int[] pro = new int[edges.length];
      int[] rea = new int[edges.length];
      int pi = 0;
      int ri = 0;
      for (int j = 0; j < edges.length; j++) {
        Edge e = net.getEdge(edges[j]);
        int index = getSpeciesByName(getOpposite(e, reactions[i].getNode()).getIdentifier());
				if (index == -1) {
					throw new IllegalArgumentException("Edge " + e.getIdentifier() + " not permitted!");
				}
//				if (prop.isReactionToProductEdge(e))
				if (checker.getEdgeClassifier().isReactionToProductEdge(e)) {
					pro[pi++] = index;
				}
//				else if (prop.isReactionToReactantEdge(e))
				else if (checker.getEdgeClassifier().isReactionToReactantEdge(e)) {
					rea[ri++] = index;
				} else {
					throw new IllegalArgumentException(
							"Edge " + e.getIdentifier() + " cannot be identified!");
				}
      }
      adjListRea[i] = new int[ri];
      adjListPro[i] = new int[pi];
      System.arraycopy(rea, 0, adjListRea[i], 0, ri);
      System.arraycopy(pro, 0, adjListPro[i], 0, pi);
    }
  }

  private Node getOpposite(Edge e, Node n) {
    return e.getSource() == n ? e.getTarget() : e.getSource();
  }

  @Override
  protected void createAnnotationManager() {
    annotationManager = new CytoscapeAnnotationManager(this);
  }

  @Override
  protected void createAmountManager() {
    amountManager = new AmountManager(this);
  }

  @Override
  protected void createPropensityCalulator() {
    double[] coeff = new double[reactions.length];
		for (int i = 0; i < coeff.length; i++) {
			coeff[i] = checker.getNodeParameter().getReactionCoefficient(reactions[i].getNode());
		}
    propensitiyCalculator = new ArrayKineticConstantPropensityCalculator(adjListRea, coeff);
  }

  @Override
  protected void createSpeciesMapping() {
    speciesIdToIndex = new HashMap<String, Integer>(species.length);
    indexToSpeciesId = new String[species.length];
    for (int i = 0; i < species.length; i++) {
      speciesIdToIndex.put(species[i].getNode().getIdentifier(), i);
      indexToSpeciesId[i] = species[i].getNode().getIdentifier();
    }
  }

  public CyAttributes getNodeAttributeObject() {
    return nodeAttr;
  }

  public CyNetworkView getNetworkViewObject() {
    return cyView;
  }


  public NodeView getReactionView(int index) {
    return reactions[index];
  }

  public NodeView getSpeciesView(int index) {
    return species[index];
  }


  public NetworkChecker getNetworkChecker() {
    return checker;
  }


}
