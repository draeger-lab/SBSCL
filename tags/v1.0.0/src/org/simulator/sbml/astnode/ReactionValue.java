/*
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;

/**
 * 
 * @author Roland Keller
 * @version $Rev: 22 $
 * @since 1.0
 */
public class ReactionValue extends ASTNodeObject {
  protected Reaction r;
  protected ASTNodeObject kineticLawUserObject;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param r
   */
  public ReactionValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    Reaction r) {
    super(interpreter, node);
    this.r = r;
    if (r.isSetKineticLaw()) {
      this.kineticLawUserObject = (ASTNodeObject) r.getKineticLaw().getMath()
          .getUserObject();
    } else {
      this.kineticLawUserObject = null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue = Double.NaN;
    if (kineticLawUserObject != null) {
      doubleValue = kineticLawUserObject.compileDouble(time);
    }
  }
}
