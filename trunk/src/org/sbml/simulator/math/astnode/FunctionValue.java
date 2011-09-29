/*
 * $Id: FunctionDefinitionValue.java 13:47:32 keller $ $URL:
 * FunctionDefinitionValue.java $
 * --------------------------------------------------------------------- This
 * file is part of SBMLsimulator, a Java-based simulator for models of
 * biochemical processes encoded in the modeling language SBML.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.SBMLException;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class FunctionValue extends ASTNodeObject {
  protected ASTNode math;
  
  /**
   * @param interpreter
   * @param node
   */
  public FunctionValue(ASTNodeInterpreterWithTime interpreter,
    ASTNode node, ASTNode math) {
    super(interpreter, node);
    CallableSBase variable = node.getVariable();
    if ((variable != null)) {
      if (variable instanceof FunctionDefinition) {
        this.math=math;
      } else {
        logger
            .warning("ASTNode of type FUNCTION but the variable is not a FunctionDefinition !! ("
                + node.getName() + ", " + node.getParentSBMLObject() + ")");
        throw new SBMLException(
          "ASTNode of type FUNCTION but the variable is not a FunctionDefinition !! ("
              + node.getName() + ", " + node.getParentSBMLObject() + ")");
        // doubleValue = compiler.compile(variable);
      }
      
    } else {
      logger.warning("ASTNode of type FUNCTION but the variable is null !! ("
          + node.getName() + ", " + node.getParentSBMLObject() + "). "
          + "Check that your object is linked to a Model.");
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    if (math != null) {
      doubleValue = interpreter.functionDouble(math, children, time);
    } else {
      doubleValue = Double.NaN;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeBooleanValue()
   */
  protected void computeBooleanValue() {
    if (math != null) {
      booleanValue = interpreter.functionBoolean(math, children, time);
    } else {
      booleanValue = false;
    }
  }
}
