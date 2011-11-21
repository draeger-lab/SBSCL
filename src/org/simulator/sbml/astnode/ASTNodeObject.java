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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.util.Maths;

/**
 * 
 * @author Roland Keller
 * @version $Rev: 22 $
 * @since 1.0
 */
public class ASTNodeObject {
  /**
   * 
   */
  protected double time;
  
  /**
   * 
   */
  protected boolean booleanValue;
  
  /**
   * 
   */
  protected double doubleValue;
  
  /**
   * 
   */
  protected boolean isDouble;
  
  
  /**
   * 
   */
  protected ASTNode node;
  
  /**
   * 
   */
  protected ASTNode.Type nodeType;
  
  
  /**
   * 
   */
  protected List<ASTNodeObject> children;
  
  /**
   * 
   */
  protected ASTNodeObject leftChild;
  
  /**
   * 
   */
  protected ASTNodeObject rightChild;
  
  
  
  /**
   * 
   */
  protected int numChildren;

  
  /**
   * 
   */
  protected ASTNodeInterpreterWithTime interpreter;
  
  /**
   * 
   */
  protected double real;
  
  /**
   * 
   */
  protected double mantissa;
  
  /**
   * 
   */
  protected int exponent;
  
  /**
   * 
   */
  protected int numerator;
  
  /**
   * 
   */
  protected int denominator;
  
  /**
   * 
   */
  protected String units;
  
  
  /**
   * 
   */
  public static final Logger logger = Logger.getLogger(ASTNodeObject.class.getName());
  
  /**
   * 
   * @param interpreter
   * @param node
   */
  public ASTNodeObject(ASTNodeInterpreterWithTime interpreter, ASTNode node) {
    this.interpreter=interpreter;
    this.node=node;
    this.nodeType=node.getType();
    if(nodeType==ASTNode.Type.REAL) {
      real=node.getReal();
    }
    else if(nodeType==ASTNode.Type.INTEGER){
      real=node.getInteger();
    }
    else {
      real=Double.NaN;
    }
    if(node.isSetUnits()) {
      units=node.getUnits();
    }
    if ((nodeType == Type.REAL) || nodeType == Type.REAL_E) {
      mantissa=node.getMantissa();
      exponent=node.getExponent();
    }
    if (nodeType == Type.RATIONAL) {
      numerator = node.getNumerator();
      denominator = node.getDenominator();
    }
    this.time=0.0;
    children=new ArrayList<ASTNodeObject>();
    if(node!=null) {  
      for(ASTNode childNode:node.getChildren()) {
        Object userObject = childNode.getUserObject();
        if(userObject!=null) {
          children.add((ASTNodeObject)userObject);
        }
      }
    }
    numChildren=children.size();
    if(numChildren>0) {
      leftChild=children.get(0);
      rightChild=children.get(numChildren-1);
    }
  }
  
  /**
   * 
   * @return
   */
  public double getTime() {
    return time;
  }
  
  /**
   * 
   */
  public void setTime(double time) {
    this.time=time;
  }
  
  
  /**
   * 
   * @param time
   * @return
   */
  public Object getValue(double time) {
    if(isDouble) {
      return compileDouble(time);
    }
    else {
      return compileBoolean(time);
    }
  }
  
  /**
   * 
   * @param time
   * @param forceCalculation
   * @return
   */
  public double compileDouble(double time) {
    if(this.time==time) {
      return doubleValue;
    }
    else {
      isDouble=true;
      this.time = time;
      computeDoubleValue();
    }
    return doubleValue;
  }
  
  /**
   * 
   * @param time
   * @return
   */
  public boolean compileBoolean(double time) {
    if(this.time==time) {
      return booleanValue;
    }
    else {
      isDouble=false;
      this.time = time;
      computeBooleanValue();
    }
    return booleanValue;
  }
  

  /**
   * 
   */
  protected void computeDoubleValue() {
    switch (nodeType) {
      /*
       * Numbers
       */
      case REAL:
        if (Double.isInfinite(real)) {
          doubleValue = (real > 0d) ? Double.POSITIVE_INFINITY
              : Double.NEGATIVE_INFINITY;
        } else {
          doubleValue = interpreter.compile(real, units);
        }
        break;
      case INTEGER:
        doubleValue = interpreter.compile(real, units);
        break;
      /*
       * Operators
       */
      case POWER:
        doubleValue = interpreter.pow(leftChild, rightChild, time);
        break;
      case PLUS:
        doubleValue = interpreter.plus(children, numChildren, time);
        break;
      case MINUS:
        if (numChildren == 1) {
          doubleValue = interpreter.uMinus(leftChild, time);
        } else {
          doubleValue = interpreter.minus(children, numChildren, time);
        }
        break;
      case TIMES:
        doubleValue = interpreter.times(children, numChildren, time);
        break;
      case DIVIDE:
        if (numChildren != 2) { throw new SBMLException(
          String
              .format(
                "Fractions must have one numerator and one denominator, here %s elements are given.",
                node.getChildCount())); }
        doubleValue = interpreter.frac(leftChild, rightChild, time);
        break;
      case RATIONAL:
        doubleValue = interpreter.frac(node.getNumerator(), node.getDenominator());
        break;
      case NAME_TIME:
        doubleValue = interpreter.symbolTime(node.getName());
        break;
      case FUNCTION_DELAY:
        doubleValue = interpreter.delay(node.getName(), leftChild,
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
        doubleValue = Maths.AVOGADRO;
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
      case FUNCTION_POWER:
        doubleValue = interpreter.pow(leftChild, rightChild, time);
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
   * 
   */
  protected void computeBooleanValue() {
    switch (node.getType()) {
        case LOGICAL_AND:
          booleanValue = interpreter.and(children,time);
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
          CallableSBase variable=node.getVariable();
          if (variable != null) {
            booleanValue = interpreter.compileBoolean(variable, time);
          }
          break;
        case LAMBDA:
          booleanValue = interpreter.lambdaBoolean(children, time);
          break;
        default:
          booleanValue=false;
          break;
      }
    }

  /**
   * 
   * @return
   */
  public boolean isName() {
    if(node!=null) {
      return node.isName();
    }
    else {
      return false;
    }
  }

  /**
   * 
   * @return
   */
  public String getName() {
    if(node!=null) {
      return node.getName();
    }
    else {
      return null;
    }
  }
}
