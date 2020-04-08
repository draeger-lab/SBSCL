package fern.cytoscape;

import giny.model.Edge;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.util.Iterator;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;

public class NetworkChecker {
	
	private CyAttributes nodeAttr;
	private CyNetwork network;
	private CyNetworkView view;
	
	private NodeClassifier nodeClassifier = null;
	private EdgeClassifier edgeClassifier = null;
	private NodeParameter nodeParameter = null;
	private Boolean valid = null;
	
	public String nodeType;
	public Object nodeTypeReaction;
	public Object nodeTypeSpecies;
	public String coefficient;
	public String initialAmount;
	
	public NetworkChecker()  {
		this(
				"sbml type",
				"reaction",
				"species",
				"reaction coefficient",
				"sbml initial amount"
		);
	}
	
	public NetworkChecker(String nodeType, Object nodeTypeReaction, Object nodeTypeSpecies, String coefficient, String initialAmount) {
		network = Cytoscape.getCurrentNetwork();
		view = Cytoscape.getCurrentNetworkView();
		nodeAttr = Cytoscape.getNodeAttributes();
		
		this.nodeType = nodeType;
		this.nodeTypeReaction = nodeTypeReaction;
		this.nodeTypeSpecies = nodeTypeSpecies;
		this.coefficient = coefficient;
		this.initialAmount = initialAmount;
	}
	
	public boolean isValid() {
		if (valid==null)
		try {
			check();
			return true;
		} catch (Exception e) {
			return false;
		}
		else return valid.booleanValue();
	}
	
	public void check() {
		valid = Boolean.FALSE;
		// create nodeClassifier
		if (nodeTypeReaction instanceof String)
			nodeClassifier = new NodeClassifierByAnnotation<String>(nodeType, (String)nodeTypeReaction, (String)nodeTypeSpecies);
		else if (nodeTypeReaction instanceof Integer)
			nodeClassifier = new NodeClassifierByAnnotation<Integer>(nodeType, (Integer)nodeTypeReaction, (Integer)nodeTypeSpecies);
		else if (nodeTypeReaction instanceof Double)
			nodeClassifier = new NodeClassifierByAnnotation<Double>(nodeType, (Double)nodeTypeReaction, (Double)nodeTypeSpecies);
		else throw new IllegalArgumentException("Only String, Integer and Double are permitted as types!");
		
		if (!nodeClassifier.isUsable())
			throw new IllegalArgumentException("Could not distinguish between reactions and species!\nTry to choose valid keys for the nodes.");
		
		edgeClassifier = new EdgeClassifierByIdentifier();
		if (!edgeClassifier.isUsable())
			edgeClassifier = new EdgeClassifierByDirection();
		if (!edgeClassifier.isUsable())
			throw new RuntimeException("Could not determine edge types!");
		
		nodeParameter = new NodeParameter(coefficient,initialAmount);
		
		if (!nodeParameter.isUsable())
			throw new IllegalArgumentException("The nodes do not contain reaction coefficients / initial amounts!");
		
		valid = Boolean.TRUE;
	}
	
	public NodeClassifier getNodeClassifier() {
		return nodeClassifier;
	}

	public EdgeClassifier getEdgeClassifier() {
		return edgeClassifier;
	}

	public NodeParameter getNodeParameter() {
		return nodeParameter;
	}

	
	public class NodeParameter  {
		
		String coeff;
		String initAm;
		
		public NodeParameter(String coeff, String initAm)  {
			this.coeff = coeff;
			this.initAm = initAm;
		}
		
		public double getReactionCoefficient(Node n) {
			Number re;
			switch (nodeAttr.getType(coeff)) {
			case CyAttributes.TYPE_FLOATING:
				re =  nodeAttr.getDoubleAttribute(n.getIdentifier(), coeff);
				break;
			case CyAttributes.TYPE_INTEGER:
				re =  nodeAttr.getIntegerAttribute(n.getIdentifier(), coeff);
				break;
			default:
				throw new RuntimeException(coeff+" has wrong type or is not present as identifier for the reaction coefficients in reaction nodes.\nOnly Double and Integer are permitted!\n\nTry to load node attributes or change the field name!");
			}
			if (re==null)
				throw new RuntimeException(coeff+" has wrong type or is not present as identifier for the reaction coefficients in reaction nodes.\nOnly Double and Integer are permitted!\n\nTry to load node attributes or change the field name!");
			return re.doubleValue();
		}
		
		public long getSpeciesInitialAmount(Node n){
			Number re;
			switch (nodeAttr.getType(initAm)) {
			case CyAttributes.TYPE_FLOATING:
				re = nodeAttr.getDoubleAttribute(n.getIdentifier(), initAm);
				break;
			case CyAttributes.TYPE_INTEGER:
				re = nodeAttr.getIntegerAttribute(n.getIdentifier(), initAm);
				break;
			default:
				throw new RuntimeException(initAm+" has wrong type or is not present as identifier for the initial amounts in species nodes.\nOnly Double and Integer are permitted!\n\nTry to load node attributes or change the field name!");
			}
			if (re==null)
				throw new RuntimeException(initAm+" has wrong type or is not present as identifier for the initial amounts in species nodes.\nOnly Double and Integer are permitted!\n\nTry to load node attributes or change the field name!");
			return re.longValue();
		}
		
		public void setSpeciesInitialAmount(Node n, long value) {
			switch (nodeAttr.getType(initAm)) {
			case CyAttributes.TYPE_FLOATING:
				nodeAttr.setAttribute(n.getIdentifier(), initAm,(double)value);
				break;
			case CyAttributes.TYPE_INTEGER:
				nodeAttr.setAttribute(n.getIdentifier(), initAm,(int)value);
				break;
			default:
				throw new RuntimeException(initAm+" has wrong type. Only Double and Integer are permitted!");
			}
		}
		
		@SuppressWarnings("unchecked")
		public boolean isUsable() { 
			for (Iterator<NodeView> i = view.getNodeViewsIterator(); i.hasNext();) {
	        	NodeView nodeView = i.next();
	        	CyNode node = (CyNode) nodeView.getNode();
	        	if (nodeClassifier.isReactionNode(node) && getReactionCoefficient(node)<=0)
	        		return false;
	        	if (nodeClassifier.isSpeciesNode(node) && getSpeciesInitialAmount(node)<0)
	        		return false;
			}
			return true;
		}
	
		
	}
	
	
	
	public interface NodeClassifier {
		public boolean isReactionNode(Node n);
		public boolean isSpeciesNode(Node n);
		public boolean isUsable();
	}
	
	public class NodeClassifierByAnnotation<T>  implements NodeClassifier {

		private String typeIdentifier;
		private T reactionType;
		private T speciesType;
		
		public NodeClassifierByAnnotation(String typeIdentifier, T reactionType, T speciesType) {
			this.typeIdentifier = typeIdentifier;
			this.reactionType = reactionType;
			this.speciesType = speciesType;
		}
		
		public boolean isReactionNode(Node n) {
			if (reactionType instanceof String)
				return reactionType.equals(nodeAttr.getStringAttribute(n.getIdentifier(), typeIdentifier));
			else if (reactionType instanceof Integer)
				return reactionType.equals(nodeAttr.getIntegerAttribute(n.getIdentifier(), typeIdentifier));
			else if (reactionType instanceof Double)
				return reactionType.equals(nodeAttr.getDoubleAttribute(n.getIdentifier(), typeIdentifier));
			else 
				throw new RuntimeException("Only String, Integer and Double allowed as node identifier type!");
		}

		public boolean isSpeciesNode(Node n) {
			if (speciesType instanceof String)
				return speciesType.equals(nodeAttr.getStringAttribute(n.getIdentifier(), typeIdentifier));
			else if (speciesType instanceof Integer)
				return speciesType.equals(nodeAttr.getIntegerAttribute(n.getIdentifier(), typeIdentifier));
			else if (speciesType instanceof Double)
				return speciesType.equals(nodeAttr.getDoubleAttribute(n.getIdentifier(), typeIdentifier));
			else 
				throw new RuntimeException("Only String, Integer and Double allowed as node identifier type!");
		}

		@SuppressWarnings("unchecked")
		public boolean isUsable() {
			for (Iterator<EdgeView> i = view.getEdgeViewsIterator(); i.hasNext();) {
				EdgeView edgeView = i.next();
	        	CyEdge edge = (CyEdge) edgeView.getEdge();
	        	if (!isSpeciesNode(edge.getSource()) && !isReactionNode(edge.getSource()))
	        		return false;
	        	if (!isSpeciesNode(edge.getTarget()) && !isReactionNode(edge.getTarget()))
	        		return false;
	        	if (isSpeciesNode(edge.getSource()) && isSpeciesNode(edge.getTarget()))
	        		return false;
	        	if (isReactionNode(edge.getSource()) && isReactionNode(edge.getTarget()))
	        		return false;
			}
			return true;
		}
		
	}
	
	
	public interface EdgeClassifier {
		public boolean isReactionToProductEdge(Edge e);
		public boolean isReactionToReactantEdge(Edge e);
		public boolean isUsable();
	}
	
	public class EdgeClassifierByIdentifier implements EdgeClassifier {
		public boolean isReactionToProductEdge(Edge e) {
			return e.getIdentifier().contains("product");
		}
		
		public boolean isReactionToReactantEdge(Edge e) {
			return e.getIdentifier().contains("reactant");
		}

		@SuppressWarnings("unchecked")
		public boolean isUsable() {
			for (Iterator<EdgeView> i = view.getEdgeViewsIterator(); i.hasNext();) {
				EdgeView edgeView = i.next();
	        	CyEdge edge = (CyEdge) edgeView.getEdge();
	        	if (!isReactionToProductEdge(edge) && !isReactionToReactantEdge(edge))
	        		return false;
			}
			return true;
		}
	}
	
	public class EdgeClassifierByDirection implements EdgeClassifier {

		public boolean isReactionToProductEdge(Edge e) {
			return nodeClassifier.isReactionNode(e.getSource()) && nodeClassifier.isSpeciesNode(e.getTarget());
		}

		public boolean isReactionToReactantEdge(Edge e) {
			return nodeClassifier.isSpeciesNode(e.getSource()) && nodeClassifier.isReactionNode(e.getTarget());
		}

		@SuppressWarnings("unchecked")
		public boolean isUsable() {
			for (Iterator<NodeView> i = view.getNodeViewsIterator(); i.hasNext();) {
	        	NodeView nodeView = i.next();
	        	CyNode node = (CyNode) nodeView.getNode();
	        	int index = network.getIndex(node);
	        	if (nodeClassifier.isReactionNode(node)) 
	        		if (network.getAdjacentEdgeIndicesArray(index,false,true,false).length<=0 ||network.getAdjacentEdgeIndicesArray(index, false, false, true).length<=0)
	        			return false;
			}
			return true;
		}
		
	}
	
	
}
