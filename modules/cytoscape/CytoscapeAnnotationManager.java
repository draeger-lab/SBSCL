package fern.cytoscape;

import java.util.Arrays;
import java.util.Collection;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import fern.network.AnnotationManager;

public class CytoscapeAnnotationManager implements AnnotationManager {

	
	
	
	CytoscapeNetworkWrapper network;
	public CytoscapeAnnotationManager(CytoscapeNetworkWrapper network) {
		this.network = network;
	}
	
	
	
//	public double[] getReactionCoefficients() {
//		CyAttributes na = network.getNodeAttributeObject();
//
////		if (na.getType(getReactionCoefficientIdentifier())==CyAttributes.TYPE_UNDEFINED)
////			throw new IllegalArgumentException(getReactionCoefficientIdentifier()+" is not specified in the net!");
//
//		double[] reactionCoeffient = new double[network.getNumReactions()];
//		for (int i=0; i<network.getNumReactions(); i++) {
////			Double val = na.getDoubleAttribute(network.getReactionView(i).getNode().getIdentifier(), getReactionCoefficientIdentifier());
////			if (val==null) throw new IllegalArgumentException(getReactionCoefficientIdentifier()+" is not specified for "+network.getReactionView(i).getNode().getIdentifier()+"!");
//			reactionCoeffient[i] = network.getNetworkChecker().getNodeParameter().;
//		}
//
//		return reactionCoeffient;
//	}
	
	private String getAttribute(CyAttributes attr, String id, String typ) {
		String val = null;
		switch (attr.getType(typ))  {
		case CyAttributes.TYPE_BOOLEAN:
			val = String.valueOf(attr.getBooleanAttribute(id, typ));
			break;
		case CyAttributes.TYPE_FLOATING:
			val = String.valueOf(attr.getDoubleAttribute(id, typ));
			break;
		case CyAttributes.TYPE_INTEGER:
			val = String.valueOf(attr.getIntegerAttribute(id, typ));
			break;
		case CyAttributes.TYPE_STRING:
			val = String.valueOf(attr.getStringAttribute(id, typ));
			break;
		case CyAttributes.TYPE_SIMPLE_LIST:
			val = Arrays.deepToString(attr.getListAttribute(id, typ).toArray());
			break;
		}
		return val;
	}
	
	
	public boolean containsNetworkAnnotation(String typ) {
		return getNetworkAnnotation(typ)!=null;
	}

	public boolean containsReactionAnnotation(int reaction, String typ) {
		return getReactionAnnotation(reaction, typ)!=null;
	}

	public boolean containsSpeciesAnnotation(int species, String typ) {
		return getSpeciesAnnotation(species, typ)!=null;
	}

	public String getNetworkAnnotation(String typ) {
		return getAttribute(Cytoscape.getNetworkAttributes(), network.net.getIdentifier(), typ);
	}

	public Collection<String> getNetworkAnnotationTypes() {
		return Arrays.asList(Cytoscape.getNetworkAttributes().getAttributeNames());
	}

	public String getReactionAnnotation(int reaction, String typ) {
		return getAttribute(Cytoscape.getNodeAttributes(),network.reactions[reaction].getNode().getIdentifier(),typ);
	}

	public Collection<String> getReactionAnnotationTypes(int reaction) {
		return Arrays.asList(Cytoscape.getNodeAttributes().getAttributeNames());
	}

	public String getSpeciesAnnotation(int species, String typ) {
		return getAttribute(Cytoscape.getNodeAttributes(),network.species[species].getNode().getIdentifier(),typ);
	}

	public Collection<String> getSpeciesAnnotationTypes(int species) {
		return Arrays.asList(Cytoscape.getNodeAttributes().getAttributeNames());
	}

	public void setNetworkAnnotation(String typ, String annotation) {
		Cytoscape.getNetworkAttributes().setAttribute(network.net.getIdentifier(), typ, annotation);
	}

	public void setReactionAnnotation(int reaction, String typ,
			String annotation) {
		Cytoscape.getNodeAttributes().setAttribute(network.reactions[reaction].getNode().getIdentifier(), typ, annotation);
	}

	public void setSpeciesAnnotation(int species, String typ, String annotation) {
		Cytoscape.getNodeAttributes().setAttribute(network.species[species].getNode().getIdentifier(), typ, annotation);	}

	
	
	
	
	

	

		
}
