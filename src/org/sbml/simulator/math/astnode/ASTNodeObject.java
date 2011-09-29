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
package org.sbml.simulator.math.astnode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.SBMLException;
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
    else {
      real=Double.NaN;
    }
    this.time=Double.NaN;
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
          doubleValue = interpreter.compile(real, node.getUnits());
        }
        break;
      case INTEGER:
        doubleValue = interpreter.compile(node.getInteger(), node.getUnits());
        break;
      /*
       * Operators
       */
      case POWER:
        doubleValue = interpreter.pow(children.get(0), children.get(numChildren-1), time);
        break;
      case PLUS:
        doubleValue = interpreter.plus(children, time);
        break;
      case MINUS:
        if (numChildren == 1) {
          doubleValue = interpreter.uMinus(children.get(0), time);
        } else {
          doubleValue = interpreter.minus(children, time);
        }
        break;
      case TIMES:
        doubleValue = interpreter.times(children, time);
        break;
      case DIVIDE:
        if (numChildren != 2) { throw new SBMLException(
          String
              .format(
                "Fractions must have one numerator and one denominator, here %s elements are given.",
                node.getChildCount())); }
        doubleValue = interpreter.frac(children.get(0), children.get(numChildren-1), time);
        break;
      case RATIONAL:
        doubleValue = interpreter.frac(node.getNumerator(), node.getDenominator());
        break;
      case NAME_TIME:
        doubleValue = interpreter.symbolTime(node.getName());
        break;
      case FUNCTION_DELAY:
        doubleValue = interpreter.delay(node.getName(), children.get(0),
          children.get(numChildren-1), node.getUnits());
        break;
      
      /*
       * Names of identifiers: parameters, functions, species etc.
       */
      case NAME:
        CallableSBase variable = node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            doubleValue = interpreter.functionDouble((FunctionDefinition) variable,
              children, time);
          } else {
            doubleValue = interpreter.compileDouble(variable, time);
          }
        } else {
          doubleValue = interpreter.compileDouble(node.getName(), time);
        }
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
        doubleValue = interpreter.compile(node.getMantissa(), node.getExponent(),
          node.isSetUnits() ? node.getUnits() : null);
        break;
      /*
       * Basic Functions
       */
      case FUNCTION_LOG:
        if (numChildren == 2) {
          doubleValue = interpreter.log(children.get(0), children.get(numChildren-1), time);
        } else {
          doubleValue = interpreter.log(children.get(numChildren-1), time);
        }
        break;
      case FUNCTION_ABS:
        doubleValue = interpreter.abs(children.get(numChildren-1), time);
        break;
      case FUNCTION_ARCCOS:
        doubleValue = interpreter.arccos(children.get(0), time);
        break;
      case FUNCTION_ARCCOSH:
        doubleValue = interpreter.arccosh(children.get(0), time);
        break;
      case FUNCTION_ARCCOT:
        doubleValue = interpreter.arccot(children.get(0), time);
        break;
      case FUNCTION_ARCCOTH:
        doubleValue = interpreter.arccoth(children.get(0), time);
        break;
      case FUNCTION_ARCCSC:
        doubleValue = interpreter.arccsc(children.get(0), time);
        break;
      case FUNCTION_ARCCSCH:
        doubleValue = interpreter.arccsch(children.get(0), time);
        break;
      case FUNCTION_ARCSEC:
        doubleValue = interpreter.arcsec(children.get(0), time);
        break;
      case FUNCTION_ARCSECH:
        doubleValue = interpreter.arcsech(children.get(0), time);
        break;
      case FUNCTION_ARCSIN:
        doubleValue = interpreter.arcsin(children.get(0), time);
        break;
      case FUNCTION_ARCSINH:
        doubleValue = interpreter.arcsinh(children.get(0), time);
        break;
      case FUNCTION_ARCTAN:
        doubleValue = interpreter.arctan(children.get(0), time);
        break;
      case FUNCTION_ARCTANH:
        doubleValue = interpreter.arctanh(children.get(0), time);
        break;
      case FUNCTION_CEILING:
        doubleValue = interpreter.ceiling(children.get(0), time);
        break;
      case FUNCTION_COS:
        doubleValue = interpreter.cos(children.get(0), time);
        break;
      case FUNCTION_COSH:
        doubleValue = interpreter.cosh(children.get(0), time);
        break;
      case FUNCTION_COT:
        doubleValue = interpreter.cot(children.get(0), time);
        break;
      case FUNCTION_COTH:
        doubleValue = interpreter.coth(children.get(0), time);
        break;
      case FUNCTION_CSC:
        doubleValue = interpreter.csc(children.get(0), time);
        break;
      case FUNCTION_CSCH:
        doubleValue = interpreter.csch(children.get(0), time);
        break;
      case FUNCTION_EXP:
        doubleValue = interpreter.exp(children.get(0), time);
        break;
      case FUNCTION_FACTORIAL:
        doubleValue = interpreter.factorial(children.get(0), time);
        break;
      case FUNCTION_FLOOR:
        doubleValue = interpreter.floor(children.get(0), time);
        break;
      case FUNCTION_LN:
        doubleValue = interpreter.ln(children.get(0), time);
        break;
      case FUNCTION_POWER:
        doubleValue = interpreter.pow(children.get(0), children.get(numChildren-1), time);
        break;
      case FUNCTION_ROOT:
        ASTNode left = node.getLeftChild();
        if (node.getChildCount() == 2) {
          if (left.isInteger()) {
            int leftdoubleValue = left.getInteger();
            if (leftdoubleValue == 2) {
              doubleValue = interpreter.sqrt(children.get(numChildren-1), time);
            } else {
              doubleValue = interpreter.root(leftdoubleValue, children.get(numChildren-1), time);
            }
          } else if (left.isReal()) {
            double leftdoubleValue = left.getReal();
            if (leftdoubleValue == 2d) {
              doubleValue = interpreter.sqrt(children.get(numChildren-1), time);
            } else {
              doubleValue = interpreter.root(leftdoubleValue, children.get(numChildren-1), time);
            }
          } else {
            doubleValue = interpreter.root(children.get(0),
              children.get(numChildren-1), time);
          }
        } else if (node.getChildCount() == 1) {
          doubleValue = interpreter.sqrt(children.get(numChildren-1), time);
        } else {
          doubleValue = interpreter.root(children.get(0),
            children.get(numChildren-1), time);
        }
        break;
      case FUNCTION_SEC:
        doubleValue = interpreter.sec(children.get(0), time);
        break;
      case FUNCTION_SECH:
        doubleValue = interpreter.sech(children.get(0), time);
        break;
      case FUNCTION_SIN:
        doubleValue = interpreter.sin(children.get(0), time);
        break;
      case FUNCTION_SINH:
        doubleValue = interpreter.sinh(children.get(0), time);
        break;
      case FUNCTION_TAN:
        doubleValue = interpreter.tan(children.get(0), time);
        break;
      case FUNCTION_TANH:
        doubleValue = interpreter.tanh(children.get(0), time);
        break;
      case FUNCTION: {
        variable = node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            doubleValue = interpreter.functionDouble((FunctionDefinition) variable,
              children, time);
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
          logger
              .warning("ASTNode of type FUNCTION but the variable is null !! ("
                  + node.getName() + ", " + node.getParentSBMLObject() + "). "
                  + "Check that your object is linked to a Model.");
          doubleValue = interpreter.functionDouble(node.getName(), children);
        }
        break;
      }
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
          booleanValue = interpreter.not(children.get(0),time);
          break;
        case RELATIONAL_EQ:
          booleanValue = interpreter.eq(children.get(0), children.get(numChildren-1),time);
          break;
        case RELATIONAL_GEQ:
          booleanValue = interpreter.geq(children.get(0), children.get(numChildren-1),time);
          break;
        case RELATIONAL_GT:
          booleanValue = interpreter.gt(children.get(0), children.get(numChildren-1),time);
          break;
        case RELATIONAL_NEQ:
          booleanValue = interpreter.neq(children.get(0), children.get(numChildren-1),time);
          break;
        case RELATIONAL_LEQ:
          booleanValue = interpreter.leq(children.get(0), children.get(numChildren-1),time);
          break;
        case RELATIONAL_LT:
          booleanValue = interpreter.lt(children.get(0), children.get(numChildren-1),time);
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
            if (variable instanceof FunctionDefinition) {
              booleanValue = interpreter.functionBoolean((FunctionDefinition) variable,
                children, time);
            } else {
              booleanValue = interpreter.compileBoolean(variable, time);
            }
          }
          break;
        
        case FUNCTION: {
          variable=node.getVariable();
          
          if (variable != null) {
            if (variable instanceof FunctionDefinition) {
              booleanValue = interpreter.functionBoolean((FunctionDefinition) variable,
                children, time);
            } else {
              logger
                  .warning("ASTNode of type FUNCTION but the variable is not a FunctionDefinition !! ("
                      + node.getName() + ", " + node.getParentSBMLObject() + ")");
              throw new SBMLException(
                "ASTNode of type FUNCTION but the variable is not a FunctionDefinition !! ("
                    + node.getName() + ", " + node.getParentSBMLObject() + ")");
              // value = compiler.compile(variable);
            }
          } else {
            logger
                .warning("ASTNode of type FUNCTION but the variable is null !! ("
                    + node.getName() + ", " + node.getParentSBMLObject() + "). "
                    + "Check that your object is linked to a Model.");
            booleanValue = interpreter.functionBoolean(node.getName(), children);
          }
          break;
        }
        case LAMBDA:
          booleanValue = interpreter.lambdaBoolean(children, time);
          break;
        default:
          booleanValue=false;
          break;
      }
    }
}
