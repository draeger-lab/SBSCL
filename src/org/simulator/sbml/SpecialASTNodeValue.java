/*
 * $Id$   
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
package org.simulator.sbml;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.util.compilers.ASTNodeValue;

/**
 * Run-time efficient implementation of {@link ASTNodeValue}
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class SpecialASTNodeValue extends ASTNodeValue{

	/**
	 * The double value (if any)
	 */
  private double doubleValue;
  
  /**
	 * The boolean value (if any)
	 */
  private boolean booleanValue;
  
  /**
   * Flag that is true if the current object represents a double value
   */
  private boolean isDouble;
  
  /**
   * Flag that is true if the current object represents a boolean value
   */
  private boolean isBoolean;
  
  /**
   * The node that is connected to the current object
   */
  private ASTNode node;
  
  /**
   * The current time
   */
  private double time;
  
  /**
   * The interpreter for calculating the values
   */
  private EfficientASTNodeInterpreter interpreter;
  
  /**
   * 
   * @param compiler
   */
  public SpecialASTNodeValue(EfficientASTNodeInterpreter interpreter) {
    super(interpreter);
    this.interpreter=interpreter;
    refreshValues();
  }
  
  /**
   * Refreshes the object
   */
  private void refreshValues() {
    isDouble=false;
    isBoolean=false;
  }
  
  /**
   * Compiles the ASTNode at the current time to a double value.
   * @param the current time
   * @return the double value
   * @throws SBMLException
   */
  public final double compileDouble(double time) throws SBMLException {
    if(this.time==time) {
     return doubleValue;
    }
    else {
      doubleValue=interpreter.compileDouble(node);
    }
    isBoolean=false;
    isDouble=true;
    this.time=time;
    return doubleValue;
  }
  
  /**
   * Compiles the ASTNode at the current time to a boolean value.
   * @param the current time
   * @return the double value
   * @throws SBMLException
   */
  public final boolean compileBoolean(double time) throws SBMLException {
    if(this.time==time) {
     return booleanValue;
    }
    else {
      booleanValue=interpreter.compileBoolean(node);
    }
    isBoolean=true;
    isDouble=false;
    this.time=time;
    return booleanValue;
  }
  
  
  
  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#getValue()
   */
  public Object getValue() {
    if(isDouble) {
      return doubleValue;
    }
    else if(isBoolean) {
      return booleanValue;
    }
    else {
      return super.getValue();
    }
    
  }
}
