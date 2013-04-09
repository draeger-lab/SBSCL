/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
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
import org.simulator.sbml.SBMLinterpreter;

/**
 * This class computes and stores values of ASTNodes that refer to a reaction.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class ReactionValue extends ASTNodeValue {
	
  /**
   * The corresponding reaction
   */
  protected Reaction r;
  
  /**
   * The object hat refers to the kinetic law of the reaction
   */
  protected ASTNodeValue kineticLawUserObject;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param r
   */
  public ReactionValue(ASTNodeInterpreter interpreter, ASTNode node,
    Reaction r) {
    super(interpreter, node);
    this.r = r;
    if (r.isSetKineticLaw()) {
      this.kineticLawUserObject = (ASTNodeValue) r.getKineticLaw().getMath().getUserObject(SBMLinterpreter.TEMP_VALUE);
    } else {
      this.kineticLawUserObject = null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue = Double.NaN;
    if (kineticLawUserObject != null) {
      doubleValue = kineticLawUserObject.compileDouble(time);
    }
  }

}
