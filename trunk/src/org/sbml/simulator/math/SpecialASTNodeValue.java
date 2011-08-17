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

import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.sbml.jsbml.util.compilers.ASTNodeValue;
import org.w3c.dom.Node;

/**
 * Run-time efficient implementation of {@link ASTNodeValue}
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class SpecialASTNodeValue extends ASTNodeValue {

  private double doubleValue;
  private boolean booleanValue;
  private boolean isDouble;
  private boolean isBoolean;
  private int intValue;
  private boolean isInt;
  
  /**
   * 
   * @param compiler
   */
  public SpecialASTNodeValue(ASTNodeCompiler compiler) {
    super(compiler);
    refreshValues();
  }
  
  /**
   * 
   */
  private void refreshValues() {
    isDouble=false;
    isBoolean=false;
    isInt=false;
  }
  
  /**
   * 
   */
  public final void setValue(double value) {
    refreshValues();
    this.doubleValue = value;
    isDouble=true;
  }
  
  /**
   * 
   */
  public final void setValue(boolean value) {
    refreshValues();
    this.booleanValue = value;
    isBoolean=true;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(int)
   */
  public void setValue(int i) {
    refreshValues();
    this.intValue=i;
    isInt=true;
  }
  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#toDouble()
   */
  public final double toDouble() throws SBMLException {
    if(isDouble) {
      return doubleValue;
    }
    else if(isInt) {
      return intValue;
    }
    else {
      return super.toDouble();
    }
  }
  
 /*
  * (non-Javadoc)
  * @see org.sbml.jsbml.util.compilers.ASTNodeValue#toInteger()
  */
  public final int toInteger() throws SBMLException {
    if(isInt) {
      return intValue;
    }
    else {
      return super.toInteger();
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#toBoolean()
   */
  public final boolean toBoolean() throws SBMLException {
    if(isBoolean) {
      return booleanValue;
    }
    else {
      return super.toBoolean();
    }
  }
 
  
  
  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(java.lang.Boolean)
   */
  public void setValue(Boolean value) {
    refreshValues();
    super.setValue(value);
  }

  

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(long)
   */
  public void setValue(long l) {
    refreshValues();
    super.setValue(Long.valueOf(l));
  }

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(org.sbml.jsbml.CallableSBase)
   */
  public void setValue(CallableSBase value) {
    refreshValues();
    super.setValue(value);
  }

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(org.w3c.dom.Node)
   */
  public void setValue(Node value) {
    refreshValues();
    super.setValue(value);
  }

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(java.lang.Number)
   */
  public void setValue(Number value) {
    refreshValues();
    super.setValue(value);
  }

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(short)
   */
  public void setValue(short s) {
    refreshValues();
    super.setValue(Short.valueOf(s));
  }

  /*
   * (non-Javadoc)
   * @see org.sbml.jsbml.util.compilers.ASTNodeValue#setValue(java.lang.String)
   */
  public void setValue(String value) {
    refreshValues();
    super.setValue(value);
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
    else if(isInt) {
      return intValue;
    }
    else {
      return super.getValue();
    }
    
  }
}
