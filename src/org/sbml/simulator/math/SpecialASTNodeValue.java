/*
 * $Id$   
 * $URL$    
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
package org.sbml.simulator.math;

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

  private double doubleValue;
  private boolean booleanValue;
  private boolean isDouble;
  private boolean isBoolean;
  
  private ASTNode node;
  private double time;
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
   * 
   */
  private void refreshValues() {
    isDouble=false;
    isBoolean=false;
  }
  
  /**
   * @throws SBMLException 
   * 
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
   * @throws SBMLException 
   * 
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
