/*
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

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;

/**
 * This class contains static methods to add meta information to a model inside SBMLDocument.
 * This class is helpful to find add meta information to original model before it is flattened
 * for information extraction later.
 * 
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.5
 */
public class AddMetaInfo{

	/**
	 * string constant for SBase id of original model
	 */
	public static final String ORIG_ID = "ORIGINAL_ID";
	/**
	 * string constant for Submodel id
	 */
	public static final String SUB_MODEL_ID = "SUBMODEL_ID";
	
	/**
	 * A static method which add id information of each element to userObjects map
	 * which is an element of AbstractTreeNode SBML class to store extra information
	 * @param SBMLDocument
	 */
	public static SBMLDocument putOrigId(SBMLDocument doc) {
		int children = doc.getChildCount();
		// Set the entire tree recursively for adding information
		for(int i = 0 ; i < children; i++) {
			
			// Set ORIGINAL_ID for entire subTree of this node
			recurse(doc.getChildAt(i));
			
			if(doc.getChildAt(i) instanceof SBase) {
				SBase node = (SBase) doc.getChildAt(i);
				if(node.isSetId()) {
					// add meta information ORIGINAL_ID
					node.putUserObject(ORIG_ID, node.getId());
				}
			}
		}
		
		return doc;
	}
	
	/**
	 * A helper method to recurse all the nodes of a SBML tree
	 */
	private static void recurse(TreeNode treeNode) {
		int children = treeNode.getChildCount();
		for(int i = 0 ; i < children; i++) {
			// Check the entire subTree of current Root
			recurse(treeNode.getChildAt(i));
			
			if(treeNode.getChildAt(i) instanceof SBase) {
				SBase node = (SBase) treeNode.getChildAt(i);
				if(node.isSetId()) {
					node.putUserObject(ORIG_ID, node.getId());
				}
			}
		}
	}
}
