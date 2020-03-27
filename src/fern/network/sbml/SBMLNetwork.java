/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.network.sbml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.libsbmlConstants;

import fern.network.AbstractNetworkImpl;
import fern.network.AnnotationManagerImpl;
import fern.network.DefaultAmountManager;
import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.tools.NetworkTools;

/**
 * For specifications of the sbml format refer to http:\\www.sbml.org.
 * Not every feature is implemented in FERN (for a list please see the user guide).
 * <p>
 * When you want to use a sbml model with events included, you have to call 
 * <code>registerEvents</code>, since the event handling is treated by {@link SBMLEventHandlerObserver}s
 * which need to be attached to the {@link Simulator}.
 *  
 *  
 * @author Florian Erhard
 *
 */
public class SBMLNetwork extends AbstractNetworkImpl {

	/**
	 * The sbml model created by libsbml.
	 */
	protected Model model;
	private SBMLDocument document;
		
	private long[] initialAmount = null;
	private Collection<SBMLEventHandlerObserver> events = null;
	
	/**
	 * Creates a network from a sbmlfile. If the file contains features not supported by FERN,
	 * an exception will be thrown.
	 * 
	 * @param file	SBML file
	 * @throws FeatureNotSupportedException
	 */
	public SBMLNetwork(File file) throws FeatureNotSupportedException {
		this(file,false);
	}
	
	/**
	 * Creates a network from a sbml file. If the file contains features not supported by FERN,
	 * depending on ignoreExceptions they will be ignored or a exception is thrown.
	 * 
	 * @param file	SBML file
	 * @param ignoreExceptions	wheter or not exceptions should be thrown
	 * @throws FeatureNotSupportedException
	 */
	public SBMLNetwork(File file, boolean ignoreExceptions) throws FeatureNotSupportedException  {
		super(file.toString());
		
		System.loadLibrary("sbmlj");
		document = new SBMLReader().readSBML(file.toString());
		
		if (!ignoreExceptions) {
			if (document.getModel().getNumSpecies()>Integer.MAX_VALUE)
				throw new FeatureNotSupportedException("Too many species in network!");
			if (document.getModel().getNumReactions()>Integer.MAX_VALUE)
				throw new FeatureNotSupportedException("Too many reactions in network!");
			if (document.getModel().getNumRules()>0)
				throw new FeatureNotSupportedException("Rules are not allowed at the moment!");
			if (document.getModel().getNumConstraints()>0)
				throw new FeatureNotSupportedException("Constraints are not allowed at the moment!");
			if (document.getModel().getNumFunctionDefinitions()>0)
				throw new FeatureNotSupportedException("Function definitions are not allowed at the moment!");
			if (document.getModel().getNumInitialAssignments()>0)
				throw new FeatureNotSupportedException("Initial assignments are not allowed at the moment!");
			for (int s=0; s<document.getModel().getNumSpecies(); s++)
				if (!document.getModel().getSpecies(s).getHasOnlySubstanceUnits())
					throw new FeatureNotSupportedException("For each species the hasOnlySubstanceUnits flag has to be set!");
		}
		
		model = document.getModel();
		
		init();
	}
	
	/**
	 * Create a <code>SBMLNetwork</code> from an existing {@link Network}. The constant for 
	 * the rate reaction is obtained by the propensity calculator by setting
	 * each reactant species' amount to 1. 
	 * 
	 * @param net 	the network to create a <code>SBMLNetwork</code> from
	 */
	public SBMLNetwork(Network net) {
		super(net.getName());
		System.loadLibrary("sbmlj");
		
		
		document = createDocument(net);
		model = document.getModel();
		
		init();
	}
	
	private void init() {
		createAnnotationManager();
		createSpeciesMapping();
		createAdjacencyLists();
		createPropensityCalulator();
		createAmountManager();
		createEventHandlers();
		
		initialAmount = new long[getNumSpecies()];
		for (int i=0; i<initialAmount.length; i++)
			initialAmount[i] = (long) getSBMLModel().getSpecies(i).getInitialAmount();
	}
	
	protected void createEventHandlers() {
		events = new LinkedList<SBMLEventHandlerObserver>();
		for (int i=0; i<model.getNumEvents(); i++)
			events.add(new SBMLEventHandlerObserver(null, this, model.getEvent(i)));
	}
	
	/**
	 * Registers the {@link SBMLEventHandlerObserver} for each event in the sbml file to
	 * the Simulator.
	 * 
	 * @param sim the simulator
	 */
	public void registerEvents(Simulator sim) {
		for (SBMLEventHandlerObserver obs : events) { 
			obs.setSimulatorAsync(sim);
			sim.addObserver(obs);
		}
	}
	
	public long getInitialAmount(int species) {
		return (long) initialAmount[species];
	}
	
	public void setInitialAmount(int species, long value) {
		initialAmount[species] = value;
	}
	
	@Override
	protected void createAdjacencyLists() {
		adjListPro = new int[(int) model.getNumReactions()][];
		adjListRea = new int[(int) model.getNumReactions()][];
		int num;
		for (int i=0; i<adjListPro.length; i++) {
			Reaction r = model.getReaction(i);
			num = 0;
			for (int j=0; j<r.getNumProducts(); j++)
				num+=r.getProduct(j).getStoichiometry();
			
			adjListPro[i] = new int[num];
			for (int j=(int)r.getNumProducts()-1; j>=0; j--)
				for (int k=0; k<r.getProduct(j).getStoichiometry(); k++)
					adjListPro[i][--num] = getSpeciesByName(r.getProduct(j).getSpecies());
			
			num = 0;
			for (int j=0; j<r.getNumReactants(); j++)
				num+=r.getReactant(j).getStoichiometry();
			
			adjListRea[i] = new int[num];
			for (int j=(int)r.getNumReactants()-1; j>=0; j--)
				for (int k=0; k<r.getReactant(j).getStoichiometry(); k++)
					adjListRea[i][--num] = getSpeciesByName(r.getReactant(j).getSpecies());
		}
	}
	@Override
	protected void createAnnotationManager()  {
		annotationManager = new AnnotationManagerImpl();
		for (int i=0; i<model.getNumSpecies(); i++)
			if (model.getSpecies(i).getBoundaryCondition())
				annotationManager.setSpeciesAnnotation(i, "BoundaryCondition", "true");
	}
	@Override
	protected void createSpeciesMapping() {
		speciesIdToIndex = new HashMap<String,Integer>((int) model.getNumSpecies());
		indexToSpeciesId = new String[(int) model.getNumSpecies()];
		for (int i=0; i<model.getNumSpecies(); i++) {
			speciesIdToIndex.put(model.getSpecies(i).getId(),i);
			indexToSpeciesId[i] = model.getSpecies(i).getId();
		}
	}
	
	/**
	 * Gets the libsbml model.
	 * 
	 * @return sbml model
	 */
	public Model getSBMLModel() {
		return model;
	}
	@Override
	protected void createAmountManager() {
		amountManager = new DefaultAmountManager(this);
	}
	@Override
	protected void createPropensityCalulator() {
		propensitiyCalculator = new SBMLPropensityCalculator(this);		
	}
	
	/**
	 * Saves the current <code>SBMLNetwork</code> to a file.
	 * 
	 * @param file			the file to save the network in
	 * @throws IOException	if the file cannot be written
	 */
	public void saveToFile(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fw.write(document.toSBML());
		fw.flush();
		fw.close();
	}
	
	
	/**
	 * Saves the current <code>SBMLNetwork</code> to a sbml file with given
	 * level and version.
	 * 
	 * @param file			the file to save the network in
	 * @param level			sbml level
	 * @param version		sbml version
	 * @throws IOException	if the file cannot be written
	 */
	public void saveToFile(File file, long level, long version) throws IOException {
		long oldlevel = document.getLevel();
		long oldversion = document.getVersion();
		document.setLevelAndVersion(level, version);
		FileWriter fw = new FileWriter(file);
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fw.write(document.toSBML());
		fw.flush();
		fw.close();
		document.setLevelAndVersion(oldlevel, oldversion);
	}
	
	private SBMLDocument createDocument(Network net) {
		SBMLDocument doc = new SBMLDocument();
		Model re = doc.createModel(net.getName());
		
		
		Compartment comp = new Compartment("Cell");
		re.addCompartment(comp);
		
		for (int s=0; s<net.getNumSpecies(); s++) {
			Species species = new Species(net.getSpeciesName(s));
			species.setCompartment("Cell");
			species.setHasOnlySubstanceUnits(true);
			species.setInitialAmount(net.getInitialAmount(s));
			re.addSpecies(species);
		}
		
		for (int r=0; r<net.getNumReactions(); r++) {
			Reaction rea = new Reaction(net.getReactionName(r));
			rea.setReversible(false);
			for (int s=0; s<net.getReactants(r).length; s++)
				rea.addReactant(new SpeciesReference(net.getSpeciesName(net.getReactants(r)[s])));	
			for (int s=0; s<net.getProducts(r).length; s++)
				rea.addProduct(new SpeciesReference(net.getSpeciesName(net.getProducts(r)[s])));
			KineticLaw law = new KineticLaw(getASTTree(net,r));
			rea.setKineticLaw(law);
			re.addReaction(rea);
		}
		
		return doc;
	}

	private ASTNode getASTTree(Network net, int r) {
		int[] reactants = net.getReactants(r);
		
		ASTNode node = new ASTNode(libsbmlConstants.AST_REAL);
		node.setValue(NetworkTools.getConstantBySettingReactantsToStoich(net, r));
		
		int[] stoich = new int[net.getNumSpecies()];
		
		for (int reac=0; reac<reactants.length; reac++) {
			ASTNode times = new ASTNode(libsbmlConstants.AST_TIMES);
			ASTNode reacNode = new ASTNode(libsbmlConstants.AST_NAME);
			reacNode.setName(net.getSpeciesName(reactants[reac]));
			times.addChild(node);
			if(stoich[reactants[reac]]==0)
				times.addChild(reacNode);
			else {
				ASTNode minus = new ASTNode(libsbmlConstants.AST_MINUS);
				ASTNode stoichNode = new ASTNode(libsbmlConstants.AST_REAL);
				stoichNode.setValue(stoich[reactants[reac]]);
				minus.addChild(reacNode);
				minus.addChild(stoichNode);
				times.addChild(minus);
			}
			node = times;
			stoich[reactants[reac]]++;
		}
		
		for (int i=0; i<stoich.length; i++) {
			if (stoich[i]>1) {
				ASTNode times = new ASTNode(libsbmlConstants.AST_TIMES);
				ASTNode reacNode = new ASTNode(libsbmlConstants.AST_REAL);
				reacNode.setValue(1.0/stoich[i]);
				times.addChild(node);
				times.addChild(reacNode);
				node = times;
			}
		}
		
		return node;
	}
	
	
}
