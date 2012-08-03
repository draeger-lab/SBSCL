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
  public RootFunctionValue(ASTNodeInterpreter interpreter, ASTNode node) {
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
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
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
