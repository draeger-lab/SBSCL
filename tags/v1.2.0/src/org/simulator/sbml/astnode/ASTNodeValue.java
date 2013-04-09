/*
 * $Id: ASTNodeValue.java 205 2012-05-05 11:57:39Z andreas-draeger $
 * $URL: http://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/sbml/astnode/ASTNodeValue.java $
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

import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.util.Maths;
import org.simulator.sbml.SBMLinterpreter;

/**
 * This class can compute and store the interpreted value (double or boolean) of an ASTNode at the current time. A new computation is only done if the time has changed.
 * So the computation is time-efficient.
 * @author Roland Keller
 * @version $Rev: 205 $
 * @since 1.0
 */
public class ASTNodeValue {
  /**
   * The time of the last computation
   */
  protected double time;
  
  /**
   * Flag that is true if the value of the ASTNode is constant.
   */
  protected boolean isConstant;
  
  /**
   * Flag that is true if the value is positive/negative infinity
   */
  protected boolean isInfinite;
  
  /**
   * Boolean value of the node (if the type of the value is boolean)
   */
  protected boolean booleanValue;
  
  /**
   * Double value of the node (if the type of the value is double)
   */
  protected double doubleValue;
  
  /**
   * Flag that is true if the value has double as type and false if the value is boolean
   */
  protected boolean isDouble;
  
  /**
   * The ASTNode this object is referring to
   */
  protected ASTNode node;
  
  /**
   * The node type of the corresponding ASTNode
   */
  protected ASTNode.Type nodeType;
  
  /**
   * The ASTNodeObjects of the child nodes of the corresponding ASTNode
   */
  protected ASTNodeValue[] children;
  
  /**
   * The ASTNodeObject of the left child of the corresponding ASTNode
   */
  protected ASTNodeValue leftChild;
  
  /**
   * The ASTNodeObject of the right child of the corresponding ASTNode
   */
  protected ASTNodeValue rightChild;
  
  /**
   * The name of the corresponding ASTNode
   */
  protected String name;
  
  /**
   * The number of children of the corresponding ASTNode
   */
  protected int numChildren;

  /**
   * Flag that tells whether a calculation of the value has already been done (important in the case of constant values)
   */
  protected boolean alreadyProcessed;
  
  /**
   * The interpreter for calculating the value
   */
  protected ASTNodeInterpreter interpreter;
  
  /**
   * The real value of the corresponding ASTNode
   */
  protected double real;
  
  /**
   * The mantissa of the corresponding ASTNode
   */
  protected double mantissa;
  
  /**
   * The exponent of the corresponding ASTNode
   */
  protected int exponent;
  
  /**
   * The numerator of the corresponding ASTNode
   */
  protected int numerator;
  
  /**
   * The denominator of the corresponding ASTNode
   */
  protected int denominator;
  
  /**
   * The units of the corresponding ASTNode
   */
  protected String units;
  
  /**
   * Resets the node
   */
  public void reset() {
    alreadyProcessed=false;
  }
  
  /**
   * A {@link Logger} for this class.
   */
  public static final Logger logger = Logger.getLogger(ASTNodeValue.class.getName());
  
  /**
   * 
   * @param interpreter
   * @param node
   */
  public ASTNodeValue(ASTNodeInterpreter interpreter, ASTNode node) {
    this.interpreter = interpreter;
    this.node = node;
    this.nodeType = node.getType();
    this.isConstant = false;
    this.alreadyProcessed = false;
    if (nodeType == ASTNode.Type.REAL) {
      real = node.getReal();
      isInfinite = Double.isInfinite(real);
      isConstant = true;
    }
    else if (nodeType==ASTNode.Type.INTEGER){
      real = node.getInteger();
      isConstant = true;
    }
    else {
      real = Double.NaN;
    }
    if (node.isSetUnits()) {
      units = node.getUnits();
    }
    if ((nodeType == ASTNode.Type.REAL) || (nodeType == ASTNode.Type.REAL_E)) {
      mantissa = node.getMantissa();
      exponent = node.getExponent();
    }
    if (nodeType == ASTNode.Type.RATIONAL) {
      numerator = node.getNumerator();
      denominator = node.getDenominator();
      isConstant = true;
    }
    
    if (node.isName()) {
      name = node.getName();
    }
    
    this.time = 0d;
    children = new ASTNodeValue[node.getChildCount()];
    if (node != null) {  
      for (int i=0; i!= node.getChildCount(); i++) {
        ASTNode childNode = node.getChild(i);
    	  Object userObject = childNode.getUserObject(SBMLinterpreter.TEMP_VALUE);
        if (userObject != null) {
          children[i]=(ASTNodeValue)userObject;
        }
      }
    }
    numChildren = node.getChildCount();
    if (numChildren>0) {
      leftChild = children[0];
      rightChild = children[numChildren-1];
    }
  }
  
  /**
   * 
   * @return time the time of the last computation of the value
   */
  public double getTime() {
    return time;
  }
  
  /**
   * 
   * @param time
   */
  public void setTime(double time) {
    this.time = time;
  }
  
  /**
   * 
   * @return constant?
   */
  public boolean getConstant() {
    return isConstant;
  }
  
  /**
   * 
   * @return node the corresponding ASTNode
   */
  public ASTNode getNode() {
	  return node;
  }
  
  /**
   * Returns the value as an object (double or boolean)
   * @param time
   * @return value the double or boolean value of the node
   */
  public Object getValue(double time) {
    if (isDouble) {
      return compileDouble(time);
    }
    else {
      return compileBoolean(time);
    }
  }
  
  /**
   * Computes the double value if the time has changed and otherwise returns the already computed value
   * @param time
   * @return doubleValue the double value of the node
   */
  public double compileDouble(double time) {
    if ((this.time==time) || (isConstant && alreadyProcessed)) {
      return doubleValue;
    } else {
      isDouble = true;
      alreadyProcessed = true;
      this.time = time;
      computeDoubleValue();
    }
    return doubleValue;
  }
  
  
  /**
   * Computes the boolean value if the time has changed and otherwise returns the already computed value
   * @param time
   * @return booleanValue the boolean value of the node
   */
  public boolean compileBoolean(double time) {
    if ((this.time==time) || (isConstant && alreadyProcessed)) {
      return booleanValue;
    }
    else {
      isDouble = false;
      alreadyProcessed = true;
      this.time = time;
      computeBooleanValue();
    }
    return booleanValue;
  }
  

  /**
   * Computes the double value of the node.
   */
  protected void computeDoubleValue() {
    switch (nodeType) {
      /*
       * Numbers
       */
      case REAL:
        if (isInfinite) {
          doubleValue = (real > 0d) ? Double.POSITIVE_INFINITY
              : Double.NEGATIVE_INFINITY;
        } else {
          doubleValue = interpreter.compile(real, units);
        }
        break;
      /*
       * Operators
       */
      case RATIONAL:
        doubleValue = interpreter.frac(numerator, denominator);
        break;
      case NAME_TIME:
        doubleValue = interpreter.symbolTime(name);
        break;
      case FUNCTION_DELAY:
        doubleValue = interpreter.delay(name, leftChild,
          rightChild, units, time);
        break;
      /*
       * Type: pi, e, true, false, Avogadro
       */
      case CONSTANT_PI:
        doubleValue = Math.PI;
        break;
      case CONSTANT_E:
        doubleValue = Math.E;
        break;
      case NAME_AVOGADRO:
        doubleValue = Maths.AVOGADRO_L3V1;
        break;
      case REAL_E:
        doubleValue = interpreter.compile(mantissa, exponent,
          units);
        break;
      /*
       * Basic Functions
       */
      case FUNCTION_LOG:
        if (numChildren == 2) {
          doubleValue = interpreter.log(leftChild, rightChild, time);
        } else {
          doubleValue = interpreter.log(rightChild, time);
        }
        break;
      case FUNCTION_ABS:
        doubleValue = interpreter.abs(rightChild, time);
        break;
      case FUNCTION_ARCCOS:
        doubleValue = interpreter.arccos(leftChild, time);
        break;
      case FUNCTION_ARCCOSH:
        doubleValue = interpreter.arccosh(leftChild, time);
        break;
      case FUNCTION_ARCCOT:
        doubleValue = interpreter.arccot(leftChild, time);
        break;
      case FUNCTION_ARCCOTH:
        doubleValue = interpreter.arccoth(leftChild, time);
        break;
      case FUNCTION_ARCCSC:
        doubleValue = interpreter.arccsc(leftChild, time);
        break;
      case FUNCTION_ARCCSCH:
        doubleValue = interpreter.arccsch(leftChild, time);
        break;
      case FUNCTION_ARCSEC:
        doubleValue = interpreter.arcsec(leftChild, time);
        break;
      case FUNCTION_ARCSECH:
        doubleValue = interpreter.arcsech(leftChild, time);
        break;
      case FUNCTION_ARCSIN:
        doubleValue = interpreter.arcsin(leftChild, time);
        break;
      case FUNCTION_ARCSINH:
        doubleValue = interpreter.arcsinh(leftChild, time);
        break;
      case FUNCTION_ARCTAN:
        doubleValue = interpreter.arctan(leftChild, time);
        break;
      case FUNCTION_ARCTANH:
        doubleValue = interpreter.arctanh(leftChild, time);
        break;
      case FUNCTION_CEILING:
        doubleValue = interpreter.ceiling(leftChild, time);
        break;
      case FUNCTION_COS:
        doubleValue = interpreter.cos(leftChild, time);
        break;
      case FUNCTION_COSH:
        doubleValue = interpreter.cosh(leftChild, time);
        break;
      case FUNCTION_COT:
        doubleValue = interpreter.cot(leftChild, time);
        break;
      case FUNCTION_COTH:
        doubleValue = interpreter.coth(leftChild, time);
        break;
      case FUNCTION_CSC:
        doubleValue = interpreter.csc(leftChild, time);
        break;
      case FUNCTION_CSCH:
        doubleValue = interpreter.csch(leftChild, time);
        break;
      case FUNCTION_EXP:
        doubleValue = interpreter.exp(leftChild, time);
        break;
      case FUNCTION_FACTORIAL:
        doubleValue = interpreter.factorial(leftChild, time);
        break;
      case FUNCTION_FLOOR:
        doubleValue = interpreter.floor(leftChild, time);
        break;
      case FUNCTION_LN:
        doubleValue = interpreter.ln(leftChild, time);
        break;
      case FUNCTION_SEC:
        doubleValue = interpreter.sec(leftChild, time);
        break;
      case FUNCTION_SECH:
        doubleValue = interpreter.sech(leftChild, time);
        break;
      case FUNCTION_SIN:
        doubleValue = interpreter.sin(leftChild, time);
        break;
      case FUNCTION_SINH:
        doubleValue = interpreter.sinh(leftChild, time);
        break;
      case FUNCTION_TAN:
        doubleValue = interpreter.tan(leftChild, time);
        break;
      case FUNCTION_TANH:
        doubleValue = interpreter.tanh(leftChild, time);
        break;
      case FUNCTION_PIECEWISE:
        doubleValue = interpreter.piecewise(children, time);
        break;
      case LAMBDA:
        doubleValue = interpreter.lambdaDouble(children, time);
        break;
      default: // UNKNOWN:
        doubleValue = Double.NaN;
        break;
    }
  }
  
  /**
   * Computes the boolean value of the node.
   */
  protected void computeBooleanValue() {
    switch (nodeType) {
        case LOGICAL_AND:
          booleanValue = interpreter.and(children,numChildren, time);
          break;
        case LOGICAL_XOR:
          booleanValue = interpreter.xor(children,time);
          break;
        case LOGICAL_OR:
          booleanValue = interpreter.or(children,time);
          break;
        case LOGICAL_NOT:
          booleanValue = interpreter.not(leftChild,time);
          break;
        case RELATIONAL_EQ:
          booleanValue = interpreter.eq(leftChild, rightChild,time);
          break;
        case RELATIONAL_GEQ:
          booleanValue = interpreter.geq(leftChild, rightChild,time);
          break;
        case RELATIONAL_GT:
          booleanValue = interpreter.gt(leftChild, rightChild,time);
          break;
        case RELATIONAL_NEQ:
          booleanValue = interpreter.neq(leftChild, rightChild,time);
          break;
        case RELATIONAL_LEQ:
          booleanValue = interpreter.leq(leftChild, rightChild,time);
          break;
        case RELATIONAL_LT:
          booleanValue = interpreter.lt(leftChild, rightChild,time);
          break;
        case CONSTANT_TRUE:
          booleanValue = true;
          break;
        case CONSTANT_FALSE:
          booleanValue = false;
          break;
        case NAME:
          CallableSBase variable = node.getVariable();
          if (variable != null) {
            booleanValue = interpreter.compileBoolean(variable, time);
          }
          break;
        case LAMBDA:
          booleanValue = interpreter.lambdaBoolean(children, time);
          break;
        default:
          booleanValue = false;
          break;
      }
    }

  /**
   * Returns true if the corresponding ASTNode is of type name.
   * @return name?
   */
  public boolean isName() {
    if (node!=null) {
      return node.isName();
    }
    else {
      return false;
    }
  }

  /**
   * Returns the name of the corresponding ASTNode.
   * @return name
   */
  public String getName() {
    if (node != null) {
      return node.getName();
    } else {
      return null;
    }
  }

}
