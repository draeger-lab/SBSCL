/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
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

/**
 * This class computes and stores values of variables occuring in a function. 
 * @author Roland Keller
 * @version $Rev$
 */
public class NamedValue extends ASTNodeValue {
	/**
	 * The function the variable occurs in
	 */
  private FunctionValue function;
  
  /**
   * The index of the variable in the arguments array of the corresponding FunctionValue.
   */
  private int index;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param function
   */
  public NamedValue(ASTNodeInterpreterWithTime interpreter, ASTNode node, FunctionValue function) {
    super(interpreter, node);
    this.function = function;
    this.index = function.getIndex(node.getName());
  }
  
 /*
  * (non-Javadoc)
  * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
  */
  protected void computeDoubleValue() {
    doubleValue=function.getArgumentValues()[index];
  }
  
  
}
