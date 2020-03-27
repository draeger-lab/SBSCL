/*
 * Created on 03.03.2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.cellDesigner;

import java.util.HashMap;
import java.util.Map;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import fern.network.AbstractNetworkImpl;
import fern.network.AmountManager;
import fern.network.AnnotationManagerImpl;
import fern.network.DefaultAmountManager;
import fern.network.sbml.SBMLPropensityCalculator;

public class CellDesignerNetworkWrapper extends AbstractNetworkImpl {

	private PluginModel model;
	private Map<PluginSpecies, Integer> speciesToReference = new HashMap<PluginSpecies, Integer>();
	
	public CellDesignerNetworkWrapper(PluginModel model) {
		super(model.getName());
		this.model = model;
		
		createAnnotationManager();
		createSpeciesMapping();
		createAmountManager();
		createAdjacencyLists();
		createPropensityCalulator();
	}

	@Override
	protected void createAdjacencyLists() {
		adjListPro = new int[model.getNumReactions()][];
		adjListRea = new int[model.getNumReactions()][];
		for (int r=0; r<model.getNumReactions(); r++) {
			PluginReaction reaction = model.getReaction(r);
			adjListPro[r] = new int[reaction.getNumProducts()];
			for (int p=0; p<reaction.getNumProducts(); p++)
				adjListPro[r][p] = speciesIdToIndex.get(reaction.getProduct(p).getSpeciesInstance().getId());
			
			adjListRea[r] = new int[reaction.getNumReactants()];
			for (int p=0; p<reaction.getNumReactants(); p++)
				adjListRea[r][p] = speciesIdToIndex.get(reaction.getReactant(p).getSpeciesInstance().getId());
		}
	}

	@Override
	protected void createAmountManager() {
		amountManager = new DefaultAmountManager(this);
	}

	@Override
	protected void createAnnotationManager() {
		annotationManager = new AnnotationManagerImpl();
	}

	@Override
	protected void createPropensityCalulator() {
		propensitiyCalculator = new CellDesignerPropensityCalculator(model, this);
	}

	@Override
	protected void createSpeciesMapping() {
		speciesIdToIndex = new HashMap<String,Integer>(model.getNumSpecies());
		indexToSpeciesId = new String[model.getNumSpecies()];
		
		for (int s=0; s<model.getNumSpecies(); s++) {
			speciesIdToIndex.put(model.getSpecies(s).getId(), s);
			indexToSpeciesId[s] = model.getSpecies(s).getId();
		}
	}

	public long getInitialAmount(int species) {
		return (long) model.getSpecies(species).getInitialAmount();
	}

	public void setInitialAmount(int species, long value) {
		model.getSpecies(species).setInitialAmount(value);
	}

}
