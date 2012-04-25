/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
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
 * This class computes and stores values of ASTNodes that refer to a root function.
 * @author Roland Keller
 * @version $Rev$
 */
public class RootFunctionValue extends ASTNodeValue{
	/**
	 * The value of the left child of the corresponding ASTNode (if applicable)
	 */
  private double leftDoubleValue;
  
  /**
   * This flag is true if the left child of the corresponding ASTNode is numeric.
   */
  private boolean leftChildrenNumeric;
  
  /**
   * 
   * @param interpreter
   * @param node
   */
  public RootFunctionValue(ASTNodeInterpreterWithTime interpreter, ASTNode node) {
    super(interpreter, node);
    ASTNode left = node.getLeftChild();
    leftChildrenNumeric=false;
    if (numChildren == 2) {
      if (left.isInteger()) {
        leftDoubleValue = left.getInteger();
        leftChildrenNumeric=true;
      } else if (left.isReal()) {
        leftDoubleValue = left.getReal();
        leftChildrenNumeric=true;
      }
    } 
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    if (numChildren == 2) {
      if (leftChildrenNumeric) {
        if (leftDoubleValue == 2) {
          doubleValue = interpreter.sqrt(rightChild, time);
        } else {
          doubleValue = interpreter.root(leftDoubleValue, rightChild, time);
        }
      } else {
        doubleValue = interpreter.root(leftChild,
          rightChild, time);
      }
    } else if (numChildren == 1) {
      doubleValue = interpreter.sqrt(rightChild, time);
    } else {
      doubleValue = interpreter.root(leftChild,
        rightChild, time);
    }
  }
  
}
