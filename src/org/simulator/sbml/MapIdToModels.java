/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, Durham, NC, US
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;

/**
 * A simple program that performs a simulation of containing hierarchical models.
 * 
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.5
 */
public class MapIdToModels{
	/**
	 * Return stack of Model ids containing current element
	 * @param flattened id
	 */
	private SBMLDocument doc;
	private Map<String, Stack<Submodel>> unflatMap = new HashMap<String, Stack<Submodel>>();
	private ListOf<ModelDefinition> modelDefs;
	private List<String> previousModelIDs;
	private List<String> previousModelMetaIDs;
	private Logger logger = Logger.getLogger(MapIdToModels.class.getName());
	
	public MapIdToModels(SBMLDocument doc){
		previousModelIDs = new ArrayList<String>();
		previousModelMetaIDs = new ArrayList<String>();
		this.doc = doc;
		
		if (doc.isPackageEnabled(CompConstants.shortLabel)) {
			unflattenIds();
		} else {
            logger.error("No comp package found in Document. Can not flatten.");
        }
	}

	private void unflattenIds() {
		Stack<Submodel> root = new Stack<Submodel>();
		if (doc.isSetModel() && doc.getModel().getExtension(CompConstants.shortLabel) != null) {
			CompSBMLDocumentPlugin docPlugin = (CompSBMLDocumentPlugin) doc.getExtension(CompConstants.shortLabel);
			modelDefs = docPlugin.getListOfModelDefinitions();
			CompModelPlugin plug = (CompModelPlugin) doc.getModel().getExtension(CompConstants.shortLabel);

			if(plug.getNumSubmodels() > 0) {
				traverse(plug, null, root);
			}
		} else {
			logger.warn("No comp package found in Model!");
		}
	}

	/**
	 * Recursively traverse all the subModels to create a map
	 * @param flattened id
	 */
	private void traverse(CompModelPlugin plug, String curId, Stack<Submodel> prevModels) {
		ListOf<Submodel> subModelList = plug.getListOfSubmodels();

		if(plug.getNumSubmodels() > 0) {
			// All the non-leaf nodes need to generate id
			for(Submodel sbModel: subModelList) {
				String id = sbModel.getId() + "_";
				String metaId = sbModel.getMetaId();
				while (previousModelIDs.contains(id)) {
					id += "_";
				}
				while (previousModelMetaIDs.contains(metaId)) {
					metaId += "_";
				}
				if(!this.previousModelIDs.isEmpty()){ // because libSBML does it
					id = previousModelIDs.get(previousModelIDs.size()-1) + id;
				}

				previousModelIDs.add(id);
				previousModelMetaIDs.add(metaId);
				prevModels.push(sbModel);
				unflatMap.put(id, prevModels);
				
				if (modelDefs != null && modelDefs.getExtension(CompConstants.shortLabel) != null) {
					traverse((CompModelPlugin) modelDefs.getExtension(CompConstants.shortLabel), id, prevModels);
				} else { 
					logger.warn("No model definition found in " + sbModel.getId() + ".") ;
				}

			}
		}else {
			// Reached leaf so add current id and stack elements to Map
			unflatMap.put(curId, prevModels);
			return;
		}
	}
	
	public Map<String, Stack<Submodel>> getIdMap() {
		return unflatMap;
	}
}
