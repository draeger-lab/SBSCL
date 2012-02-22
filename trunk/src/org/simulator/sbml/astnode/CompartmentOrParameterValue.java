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
import org.sbml.jsbml.Symbol;
import org.simulator.sbml.ValueHolder;

/**
 * 
 * @author Roland Keller
 * @version $Rev: 22 $
 * @since 1.0
 */
public class CompartmentOrParameterValue extends ASTNodeObject {
  protected Symbol sb;
  protected String id;
  protected ValueHolder valueHolder;
  protected int position;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param sb
   * @param valueHolder
   */
  public CompartmentOrParameterValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    Symbol sb, ValueHolder valueHolder, int position) {
    super(interpreter, node);
    this.sb = sb;
    this.id=sb.getId();
//    if(sb.getConstant()) {
//      isConstant = true;
//      doubleValue = sb.getValue();
//    }
    this.valueHolder = valueHolder;
    this.position=position;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue=valueHolder.getCurrentValueOf(position);
  }
}
