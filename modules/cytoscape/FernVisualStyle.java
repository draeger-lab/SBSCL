package fern.cytoscape;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.visual.Arrow;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.LabelPosition;
import cytoscape.visual.LineType;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.ShapeNodeRealizer;
import cytoscape.visual.VisualStyle;
import giny.model.Edge;
import giny.model.Node;

public class FernVisualStyle extends VisualStyle {

	private NetworkChecker networkChecker = null;
	private Map<Node,Color> colorMapping = null;
	public static ColorCalculator colorCalculator = new ColorCalculator();
	
	public FernVisualStyle() {
		super("FERN");
		
		colorMapping = new HashMap<Node, Color>();
		networkChecker = new NetworkChecker();
		
		setNodeAppearanceCalculator(createNodeAppearanceCalculator());
		setEdgeAppearanceCalculator(createEdgeAppearanceCalculator());
		setGlobalAppearanceCalculator(createGlobalAppearanceCalculator());
	}
	
	private GlobalAppearanceCalculator createGlobalAppearanceCalculator() {
		return Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator();
	}
	
	private NodeAppearanceCalculator createNodeAppearanceCalculator() {
		return new NodeAppearanceCalculator() {
			
			@Override
			public void calculateNodeAppearance(NodeAppearance appr, Node node,CyNetwork network) {
				networkChecker.isValid();
				if (networkChecker.getNodeClassifier()!=null && networkChecker.getNodeClassifier().isReactionNode(node)) {
					appr.setShape(ShapeNodeRealizer.DIAMOND);
					appr.setLabel("");
				} else if (networkChecker.getNodeClassifier()!=null && networkChecker.getNodeClassifier().isSpeciesNode(node)){
					appr.setShape(ShapeNodeRealizer.ELLIPSE);
					appr.setLabel(node.getIdentifier());
				} else {
					appr.setShape(ShapeNodeRealizer.RECT);
					appr.setLabel(node.getIdentifier());
				}
				appr.setSize(20);
				appr.setWidth(20);
				appr.setHeight(20);
				appr.setToolTip(node.getIdentifier());
				appr.setBorderColor(Color.black);
				appr.setBorderLineType(LineType.LINE_1);
				appr.setFont(new Font("Arial",Font.PLAIN,10));
				appr.setLabelColor(Color.black);
				appr.setLabelPosition(new LabelPosition());
				appr.setNodeSizeLocked(true);
				if (colorMapping.containsKey(node))
					appr.setFillColor(colorMapping.get(node));
				else
					appr.setFillColor(Color.white);
			}
			
			@Override
			public NodeAppearance calculateNodeAppearance(Node node,CyNetwork network) {
				NodeAppearance app = new NodeAppearance();
				calculateNodeAppearance(app, node, network);
				return app;
			}
		};
	}
	
	private EdgeAppearanceCalculator createEdgeAppearanceCalculator() {
		return new EdgeAppearanceCalculator() {
			@Override
			public void calculateEdgeAppearance(EdgeAppearance appr, Edge edge,CyNetwork network) {
				appr.setSourceArrow(Arrow.NONE);
				appr.setTargetArrow(Arrow.NONE);
				if (networkChecker.getEdgeClassifier()!=null && networkChecker.getNodeClassifier()!=null) {
					if (networkChecker.getEdgeClassifier().isReactionToProductEdge(edge)) {
						if (networkChecker.getNodeClassifier().isSpeciesNode(edge.getSource()))
								appr.setSourceArrow(Arrow.BLACK_ARROW);
						else if (networkChecker.getNodeClassifier().isSpeciesNode(edge.getTarget()))
							appr.setTargetArrow(Arrow.BLACK_ARROW);
					}
					if (networkChecker.getEdgeClassifier().isReactionToReactantEdge(edge)) {
						if (networkChecker.getNodeClassifier().isReactionNode(edge.getSource()))
								appr.setSourceArrow(Arrow.BLACK_ARROW);
						else if (networkChecker.getNodeClassifier().isReactionNode(edge.getTarget()))
							appr.setTargetArrow(Arrow.BLACK_ARROW);
					}
				}
				appr.setColor(Color.black);
				appr.setLabel("");
				appr.setLineType(LineType.LINE_1);
				appr.setToolTip(edge.getSource().getIdentifier()+"\n->\n"+edge.getTarget().getIdentifier());
			}
			
			@Override
			public EdgeAppearance calculateEdgeAppearance(Edge edge,CyNetwork network) {
				EdgeAppearance app = new EdgeAppearance();
				calculateEdgeAppearance(app, edge, network);
				return app;
			}
		};
	}

	public void setNetworkChecker(NetworkChecker networkChecker) {
		this.networkChecker = networkChecker;
		Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
	}

	public void setValue(Node node, double val, double max) {
		colorMapping.put(node, colorCalculator.getColor(val,max));
	}
	
	public void setReactionFire(Node node) {
		colorMapping.put(node, colorCalculator.getReactionColor());
	}
	
	public void setReactionUnFire(Node node) {
		colorMapping.remove(node);
	}
	
	public void resetColors() {
		colorMapping.clear();
		Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
	}

}
