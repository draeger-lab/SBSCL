/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models of biochemical processes encoded in the modeling language SBML.
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
package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.simulator.math.ValueHolder;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class SpeciesReferenceValue extends ASTNodeObject {

  private ValueHolder valueHolder;
  private String id;

  /**
   * @param interpreter
   * @param node
   */
  public SpeciesReferenceValue(ASTNodeInterpreterWithTime interpreter,
    ASTNode node, SpeciesReference sr, ValueHolder valueHolder) {
    super(interpreter, node);
    this.id=sr.getId();
    this.valueHolder=valueHolder;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue = valueHolder.getCurrentStoichiometry(id);
  }
}
