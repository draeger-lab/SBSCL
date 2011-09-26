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
  protected ASTNodeInterpreterWithTime interpreter;
  
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
   * @return
   */
  public double compileDouble(double time) {
    
    if (this.time != time) {
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
    if (this.time != time) {
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
    switch (node.getType()) {
      /*
       * Numbers
       */
      case REAL:
        double real = node.getReal();
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
        doubleValue = interpreter.pow((ASTNodeObject) node.getLeftChild()
            .getUserObject(), (ASTNodeObject) node.getRightChild()
            .getUserObject(), time);
        break;
      case PLUS:
        doubleValue = interpreter.plus(node.getChildren(), time);
        break;
      case MINUS:
        if (node.getChildCount() == 1) {
          doubleValue = interpreter.uMinus((ASTNodeObject) node.getLeftChild()
              .getUserObject(), time);
        } else {
          doubleValue = interpreter.minus(node.getChildren(), time);
        }
        break;
      case TIMES:
        doubleValue = interpreter.times(node.getChildren(), time);
        break;
      case DIVIDE:
        if (node.getChildCount() != 2) { throw new SBMLException(
          String
              .format(
                "Fractions must have one numerator and one denominator, here %s elements are given.",
                node.getChildCount())); }
        doubleValue = interpreter.frac((ASTNodeObject) node.getLeftChild()
            .getUserObject(), (ASTNodeObject) node.getRightChild()
            .getUserObject(), time);
        break;
      case RATIONAL:
        doubleValue = interpreter.frac(node.getNumerator(), node.getDenominator());
        break;
      case NAME_TIME:
        doubleValue = interpreter.symbolTime(node.getName());
        break;
      case FUNCTION_DELAY:
        doubleValue = interpreter.delay(node.getName(), node.getLeftChild(),
          node.getRightChild(), node.getUnits());
        break;
      
      /*
       * Names of identifiers: parameters, functions, species etc.
       */
      case NAME:
        CallableSBase variable = node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            doubleValue = interpreter.functionDouble((FunctionDefinition) variable,
              node.getChildren(), time);
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
        if (node.getChildCount() == 2) {
          doubleValue = interpreter.log((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(), time);
        } else {
          doubleValue = interpreter.log((ASTNodeObject) node.getRightChild()
              .getUserObject(), time);
        }
        break;
      case FUNCTION_ABS:
        doubleValue = interpreter.abs((ASTNodeObject) node.getRightChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCOS:
        doubleValue = interpreter.arccos((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCOSH:
        doubleValue = interpreter.arccosh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCOT:
        doubleValue = interpreter.arccot((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCOTH:
        doubleValue = interpreter.arccoth((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCSC:
        doubleValue = interpreter.arccsc((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCCSCH:
        doubleValue = interpreter.arccsch((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCSEC:
        doubleValue = interpreter.arcsec((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCSECH:
        doubleValue = interpreter.arcsech((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCSIN:
        doubleValue = interpreter.arcsin((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCSINH:
        doubleValue = interpreter.arcsinh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCTAN:
        doubleValue = interpreter.arctan((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ARCTANH:
        doubleValue = interpreter.arctanh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_CEILING:
        doubleValue = interpreter.ceiling((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_COS:
        doubleValue = interpreter.cos((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_COSH:
        doubleValue = interpreter.cosh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_COT:
        doubleValue = interpreter.cot((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_COTH:
        doubleValue = interpreter.coth((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_CSC:
        doubleValue = interpreter.csc((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_CSCH:
        doubleValue = interpreter.csch((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_EXP:
        doubleValue = interpreter.exp((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_FACTORIAL:
        doubleValue = interpreter.factorial((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_FLOOR:
        doubleValue = interpreter.floor((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_LN:
        doubleValue = interpreter.ln((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_POWER:
        doubleValue = interpreter.pow((ASTNodeObject) node.getLeftChild()
            .getUserObject(), (ASTNodeObject) node.getRightChild()
            .getUserObject(), time);
        break;
      case FUNCTION_ROOT:
        ASTNode left = node.getLeftChild();
        if (node.getChildCount() == 2) {
          if (left.isInteger()) {
            int leftdoubleValue = left.getInteger();
            if (leftdoubleValue == 2) {
              doubleValue = interpreter.sqrt((ASTNodeObject) node.getRightChild()
                  .getUserObject(), time);
            } else {
              doubleValue = interpreter.root(leftdoubleValue, (ASTNodeObject)node.getRightChild().getUserObject(), time);
            }
          } else if (left.isReal()) {
            double leftdoubleValue = left.getReal();
            if (leftdoubleValue == 2d) {
              doubleValue = interpreter.sqrt((ASTNodeObject) node.getRightChild()
                  .getUserObject(), time);
            } else {
              doubleValue = interpreter.root(leftdoubleValue, (ASTNodeObject) node
                  .getRightChild().getUserObject(), time);
            }
          } else {
            doubleValue = interpreter.root((ASTNodeObject) left.getUserObject(),
              (ASTNodeObject) node.getRightChild().getUserObject(), time);
          }
        } else if (node.getChildCount() == 1) {
          doubleValue = interpreter.sqrt((ASTNodeObject) node.getRightChild()
              .getUserObject(), time);
        } else {
          doubleValue = interpreter.root((ASTNodeObject) left.getUserObject(),
            (ASTNodeObject) node.getRightChild().getUserObject(), time);
        }
        break;
      case FUNCTION_SEC:
        doubleValue = interpreter.sec((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_SECH:
        doubleValue = interpreter.sech((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_SIN:
        doubleValue = interpreter.sin((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_SINH:
        doubleValue = interpreter.sinh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_TAN:
        doubleValue = interpreter.tan((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION_TANH:
        doubleValue = interpreter.tanh((ASTNodeObject) node.getLeftChild()
            .getUserObject(), time);
        break;
      case FUNCTION: {
        variable = node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            doubleValue = interpreter.functionDouble((FunctionDefinition) variable,
              node.getChildren(), time);
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
          doubleValue = interpreter.functionDouble(node.getName(), node.getChildren());
        }
        break;
      }
      case FUNCTION_PIECEWISE:
        doubleValue = interpreter.piecewise(node.getChildren(), time);
        break;
      case LAMBDA:
        doubleValue = interpreter.lambdaDouble(node.getChildren(), time);
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
        /*
         * Logical and relational functions
         */
        case LOGICAL_AND:
          booleanValue = interpreter.and(node.getChildren(),time);
          break;
        case LOGICAL_XOR:
          booleanValue = interpreter.xor(node.getChildren(),time);
          break;
        case LOGICAL_OR:
          booleanValue = interpreter.or(node.getChildren(),time);
          break;
        case LOGICAL_NOT:
          booleanValue = interpreter.not((ASTNodeObject)node.getLeftChild().getUserObject(),time);
          break;
        case RELATIONAL_EQ:
          booleanValue = interpreter.eq((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
          break;
        case RELATIONAL_GEQ:
          booleanValue = interpreter.geq((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
          break;
        case RELATIONAL_GT:
          booleanValue = interpreter.gt((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
          break;
        case RELATIONAL_NEQ:
          booleanValue = interpreter.neq((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
          break;
        case RELATIONAL_LEQ:
          booleanValue = interpreter.leq((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
          break;
        case RELATIONAL_LT:
          booleanValue = interpreter.lt((ASTNodeObject)node.getLeftChild().getUserObject(), (ASTNodeObject)node.getRightChild().getUserObject(),time);
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
                node.getChildren(), time);
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
                node.getChildren(), time);
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
            booleanValue = interpreter.functionBoolean(node.getName(), node.getChildren());
          }
          break;
        }
        case LAMBDA:
          booleanValue = interpreter.lambdaBoolean(node.getChildren(), time);
          break;
        default:
          booleanValue=false;
          break;
      }
    }
  
}
