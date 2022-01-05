/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
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
 * This class contains static methods to add meta information to a model inside SBMLDocument. This
 * class is helpful to find add meta information to original model before it is flattened for
 * information extraction later.
 *
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.5
 */
public class AddMetaInfo {

  /**
   * string constant for SBase id of original model
   */
  public static final String ORIG_ID = "ORIGINAL_ID";

  /**
   * string constant for (Sub-)Model id
   */
  public static final String MODEL_ID = "MODEL_ID";

  /**
   * A static method which add id information of each element to userObjects map which is an element
   * of AbstractTreeNode SBML class to store extra information
   *
   * @param doc
   */
  public static SBMLDocument putOrigId(SBMLDocument doc) {
    doc = (SBMLDocument) recurse(doc);
    return doc;
  }

  /**
   * A helper method to recurse all the nodes of a SBML tree
   */
  private static TreeNode recurse(TreeNode treeNode) {
    Enumeration<TreeNode> children = (Enumeration<TreeNode>) treeNode.children();
    // Set the entire tree recursively for adding information
    while (children.hasMoreElements()) {
      TreeNode child = children.nextElement();
      // Set ORIGINAL_ID for entire subTree of this node
      child = recurse(child);
      if (child instanceof SBase) {
        SBase node = (SBase) child;
        if (node.isSetId()) {
          // add meta information model id and parent model id
          node.putUserObject(ORIG_ID, node.getId());
          node.putUserObject(MODEL_ID, node.getModel().getId());
        }
      }
    }
    return treeNode;
  }
}
