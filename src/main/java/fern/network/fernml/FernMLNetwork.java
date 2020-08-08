package fern.network.fernml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.AbstractNetworkImpl;
import fern.network.AnnotationManager;
import fern.network.ArrayKineticConstantPropensityCalculator;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.tools.NetworkTools;


/**
 * A <code>FernMLNetwork</code> is usually loaded from a file. For specifications see
 * the included FernMLSchema.xsd or the examples. Additionally, a <code>FernMLNetwork</code>
 * can be created out of an arbitrary {@link Network}. By using the <code>saveToFile</code>
 * method, every <code>Network</code> can be saved as a fernml-File.
 * 
 * 
 * @author Florian Erhard
 *
 */
public class FernMLNetwork extends AbstractNetworkImpl {
	
	private Document 	document 	= null;
	private int 		numReaction = 0;
	private int 		numSpecies 	= 0;
	private long[] 		initialAmount 	= null;
	
	
	/**
	 * Creates a <code>FernMLNetwork</code> from a file.
	 * @param file				file containing the network
	 * @throws IOException		if the file cannot be read
	 * @throws JDOMException	if the file is malformed
	 */
	public FernMLNetwork(File file) throws IOException, JDOMException  {
		super(file.getName());

		File schemeFile = new File("FernMLSchema.xsd");
		String schemeFilePath = schemeFile.getAbsolutePath();

		SAXBuilder sax = new SAXBuilder(true);
		sax.setFeature("http://apache.org/xml/features/validation/schema", true);
		sax.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage","http://www.w3.org/2001/XMLSchema" );
		sax.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",schemeFilePath);;
		document = sax.build(file);
		
		init();
	}
	
	/**
	 * Create a <code>FernMLNetwork</code> from an existing {@link Network}. If the
	 * network's {@link PropensityCalculator} is not an {@link AbstractKineticConstantPropensityCalculator}, 
	 * the constant for the rate reaction is obtained by the propensity calculator by setting
	 * each reactant species' amount to 1. If the stoichiometry of some reactant is greater than 1 the value
	 * is set accordingly. 
	 * 
	 * @param net 	the network to create a <code>FernMLNetwork</code> from
	 */
	public FernMLNetwork(Network net) {
		super(net.getName());
//		if (!(net.getPropensityCalculator() instanceof AbstractKineticConstantPropensityCalculator))
//			throw new IllegalArgumentException("net's PropensitiyCalculator is not a AbstractKineticConstantPropensityCalculator! Use FernMLNetwork(Network,double[]) instead!");
		 document = createDocument(net,null);
		 init();
	}
	
	/**
	 * Creates a FernMLNetwork out of an existing network (e.g. to save it to a fernml file)
	 * using explicitly given kineticConstants (when <dode>net</code> doesn't use <code>KineticConstantPropensityCalculator</code>
	 * If <code>kineticConstants</code> is <code>null</code> or to short, a default value of 1 is taken.
	 * @param net An existing network
	 * @param kineticConstants kinetic constants for each reaction in <code>net</code>
	 */
	public FernMLNetwork(Network net, double[] kineticConstants) {
		super(net.getName());
		document = createDocument(net, kineticConstants);
		init();
	}
	
	private void init() {
		createAnnotationManager();
		createSpeciesMapping();
		createAmountManager();
		createAdjacencyLists();
		createPropensityCalculator();
	}
	

	@Override
	public ArrayKineticConstantPropensityCalculator getPropensityCalculator() {
		return (ArrayKineticConstantPropensityCalculator) super.getPropensityCalculator();
	}
	
	@Override
	public int getNumReactions() {
		return numReaction;
	}
	
	@Override
	public int getNumSpecies() {
		return numSpecies;
	}

	@SuppressWarnings("unchecked")
	public void setInitialAmount(int species, long value) {
		List<Element> speciesList = document.getRootElement().getChild("listOfSpecies").getChildren();
		speciesList.get(species).setAttribute("initialAmount", value+"");
		
		initialAmount[species] = value;
	}
	
	public long getInitialAmount(int species) {
		return initialAmount[species];
	}
	
	@Override
	protected void createAmountManager() {
		amountManager = new DefaultAmountManager(this);
	}

	/**
	 * Creates the adjacency lists by parsing the jdom tree.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void createAdjacencyLists() {
		List<Element> listOfReactions = document.getRootElement().getChild("listOfReactions").getChildren();
		
		adjListPro = new int[numReaction][];
		adjListRea = new int[numReaction][];
		double[] constants = new double[numReaction];
		
		int index = 0;
		for (Element reaction : listOfReactions) {
			boolean reversible = reaction.getAttribute("kineticConstantReversible")!=null;
			
			constants[index] = Double.parseDouble(reaction.getAttributeValue("kineticConstant"));
			if (reversible)
				constants[index+1] = Double.parseDouble(reaction.getAttributeValue("kineticConstantReversible"));
			
			
			List<Element> reactants = reaction.getChild("listOfReactants").getChildren();
			List<Element> products = reaction.getChild("listOfProducts").getChildren();
			int[] rea = createSpeciesReferences(reactants);
			int[] pro = createSpeciesReferences(products);
			
			adjListRea[index] = rea;
			adjListPro[index] = pro;
			
			if (reversible) {
				adjListRea[index+1] = pro;
				adjListPro[index+1] = rea;
			}
			
			index+=reversible ? 2 : 1;
		}
		
		propensitiyCalculator = new ArrayKineticConstantPropensityCalculator(adjListRea,constants);
	}

	/**
	 * Does nothing, the {@link PropensityCalculator} is created in <code>createAdjacencyLists</code>
	 * because the reactions constants are already parsed there.
	 */
	@Override
	protected void createPropensityCalculator() {
		// done in createAdjacencyLists
	}

	private int[] createSpeciesReferences(List<Element> speciesReference) {
		int[] re = new int[speciesReference.size()];
		int index = 0;
		for (Element e : speciesReference) 
			re[index++] = getSpeciesByName(e.getAttributeValue("name"));
		return re;
	}

	/**
	 * Creates the {@link AnnotationManager} as a {@link FernMLAnnotationManager}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void createAnnotationManager() {
		// read number of species/reactions
		List<Element> reactions = document.getRootElement().getChild("listOfReactions").getChildren();
		numReaction = 0;
		for (Element r : reactions) {
			boolean reversible = r.getAttribute("kineticConstantReversible")!=null;
			numReaction  += reversible ? 2 : 1;
		}
		
		numSpecies = document.getRootElement().getChild("listOfSpecies").getChildren().size();
		
		annotationManager = new FernMLAnnotationManager(document.getRootElement());
	}

	/**
	 * Creates the species mapping by parsing the jdom tree.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void createSpeciesMapping() {
		List<Element> listOfSpecies = document.getRootElement().getChild("listOfSpecies").getChildren();
		
		speciesIdToIndex = new HashMap<String,Integer>(listOfSpecies.size());
		indexToSpeciesId = new String[listOfSpecies.size()];
		initialAmount = new long[indexToSpeciesId.length];
		int index = 0;
		for (Element species : listOfSpecies) {
			String name = species.getAttributeValue("name");
			long initialAmount = (long) Double.parseDouble(species.getAttributeValue("initialAmount"));
			speciesIdToIndex.put(name, index);
			indexToSpeciesId[index] = name;
			this.initialAmount[index] = initialAmount;
			index++;
		}
		
	}
	
	/**
	 * Saves the actual <code>FernMLNetwork</code> to a file.
	 * 
	 * @param file			the file to save the network in
	 * @throws IOException	if the file cannot be written
	 */
	public void saveToFile(File file) throws IOException {
		XMLOutputter output = new XMLOutputter();
		output.output(document, new FileWriter(file));
	}
	
	
	@SuppressWarnings("unchecked")
	private Document createDocument(Network net, double[] kineticConstants) {
		AnnotationManager prop = net.getAnnotationManager();
		
		AbstractKineticConstantPropensityCalculator kin = null;
		if (net.getPropensityCalculator() instanceof AbstractKineticConstantPropensityCalculator) 
			kin = (AbstractKineticConstantPropensityCalculator) net.getPropensityCalculator();
			
		
		
		// create root
		Document doc = new Document();
		doc.setRootElement(new Element("fernml"));
		Element root = doc.getRootElement();;
		root.setAttribute("version", "1.0");
		
		// create network annotations if present
		Collection<String> annotations = prop.getNetworkAnnotationTypes();
		if (annotations!=null && annotations.size()>0) {
			Element annotationsRoot = new Element("listOfAnnotations");
			for (String key : annotations) 
				annotationsRoot.getChildren().add(createAnnotation(key, prop.getNetworkAnnotation(key)));
			root.getChildren().add(annotationsRoot);
		}
		
		// create listofspecies
		Element listOfSpecies = new Element("listOfSpecies");
		root.getChildren().add(listOfSpecies);
		for (int i=0; i<net.getNumSpecies(); i++) {
			String name = net.getSpeciesName(i);
			double initialAmount = net.getInitialAmount(i);
			Collection<String> speciesAnnotations = prop.getSpeciesAnnotationTypes(i);
			
			Element s = new Element("species");
			s.setAttribute("name",name);
			s.setAttribute("initialAmount", String.valueOf(initialAmount));
			if (speciesAnnotations!=null && speciesAnnotations.size()>0) {
				Element annotationsRoot = new Element("listOfAnnotations");
				for (String key : speciesAnnotations) 
					annotationsRoot.getChildren().add(createAnnotation(key, prop.getSpeciesAnnotation(i, key)));
				s.getChildren().add(annotationsRoot);
			}
			listOfSpecies.getChildren().add(s);
		}
		
		// create listOfReactions
		Element listOfReactions = new Element("listOfReactions");
		root.getChildren().add(listOfReactions);
		for (int i=0; i<net.getNumReactions(); i++) {
			Collection<String> reactionAnnotations = prop.getReactionAnnotationTypes(i);
			double constant;
			
			if (kineticConstants!=null && i<kineticConstants.length) 
				constant = kineticConstants[i];
			else if (kin!=null) 
				constant = kin.getConstant(i);
			else 
				constant = NetworkTools.getConstantBySettingReactantsToStoich(net,i);
			
			Element r = new Element("reaction");
			r.setAttribute("kineticConstant", String.valueOf(constant));
			if (reactionAnnotations!=null && reactionAnnotations.size()>0) {
				Element annotationsRoot = new Element("listOfAnnotations");
				for (String key : reactionAnnotations) 
					annotationsRoot.getChildren().add(createAnnotation(key, prop.getReactionAnnotation(i, key)));
				r.getChildren().add(annotationsRoot);
			}
			
			// reactants
			Element loR = new Element("listOfReactants");
			r.getChildren().add(loR);
			for (int j=0; j<net.getReactants(i).length; j++) {
				Element reactant = new Element("speciesReference");
				reactant.setAttribute("name", net.getSpeciesName(net.getReactants(i)[j]));
				loR.getChildren().add(reactant);
			}
			
			// products
			Element loP = new Element("listOfProducts");
			r.getChildren().add(loP);
			for (int j=0; j<net.getProducts(i).length; j++) {
				Element product = new Element("speciesReference");
				product.setAttribute("name", net.getSpeciesName(net.getProducts(i)[j]));
				loP.getChildren().add(product);
			}
			listOfReactions.getChildren().add(r);
		}
		
		return doc;
	}
	
	

	private Element createAnnotation(String typ, String content) {
		Element re = new Element("annotation");
		re.setAttribute("name", typ);
		re.setText(content);
		return re;
	}
	
	
	
}
