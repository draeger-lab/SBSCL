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

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;

/**
 * This class contains static methods to extract meta information from userObjects hash of SBMLDocument.
 * 
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.5
 */
public class GetMetaInfo {
	/**
	 * A static method which extract some information of an element stored under userObjects map
	 * which is an element of AbstractTreeNode SBML class to store extra information
	 * @param SBMLDocument
	 */
	public static TreeNode getOrigId(SBMLDocument doc, String id) {
		Enumeration<TreeNode> children = doc.children();
		// Set the entire tree recursively for adding information
		while(children.hasMoreElements()) {
			TreeNode child = children.nextElement();
			
			// Check the entire subTree of current Root
			TreeNode subTreeOutput = recurse(child, id);
			if(subTreeOutput != null) {
				return subTreeOutput;
			}else {
			
				// May be it is root itself?
				if(child instanceof SBase) {
					SBase node = (SBase) child;
					if(node.isSetId() && node.getId().equals(id)) {
						return node;
					}
				}
			}
		}
		
		// The id wasn't found in any TreeNode
		return null;
	}
	
	/**
	 * A helper method to recurse all the nodes of a SBML tree
	 */
	private static TreeNode recurse(TreeNode treeNode, String id) {
		Enumeration<TreeNode> children = treeNode.children();
		// Set the entire tree recursively for adding information
		while(children.hasMoreElements()) {
			TreeNode child = children.nextElement();
			
			TreeNode subTreeOutput = recurse(child, id);
			if(subTreeOutput != null) {
				return subTreeOutput;
			}else {
			
				// May be it is root itself?
				if(child instanceof SBase) {
					SBase node = (SBase) child;
					if(node.isSetId() && node.getId().equals(id)) {
						return node;
					}
				}
			}
		}
		
		// The id wasn't found in any Subnode
		return null;
	}
}
