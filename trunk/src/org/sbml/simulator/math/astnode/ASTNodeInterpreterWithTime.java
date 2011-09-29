/*
 *---------------------------------------------------------------------
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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.Maths;
import org.sbml.simulator.math.SBMLinterpreter;
import org.sbml.simulator.math.ValueHolder;

/**
 * 
 * @author Roland Keller
 * @version $Rev: 22 $
 * @since 1.0
 */
public class ASTNodeInterpreterWithTime {
  /**
   * A logger.
   */
  public static final Logger logger = Logger.getLogger(SBMLinterpreter.class
      .getName());
  /**
   * This table is necessary to store the values of arguments when a function
   * definition is evaluated. For an identifier of the argument the
   * corresponding value will be stored.
   */
  private Map<String, Double> funcArgs;
  
  
  /**
     * 
     */
  private ValueHolder valueHolder;
  
  
  /**
   * 
   * @param valueHolder
   */
  public ASTNodeInterpreterWithTime(ValueHolder valueHolder) {
    this.valueHolder = valueHolder;
  }
  
  /**
     * 
     */
  private void clearFuncArgs() {
    funcArgs.clear();
  }
  
  
  /**
   * @param name
   * @return
   */
  private Double getFuncArg(String name) {
    if ((funcArgs != null) && funcArgs.containsKey(name)) {
      // replace the name by the associated value of the argument
      return funcArgs.get(name).doubleValue();
    }
    return null;
  }
  
  
  /**
   * @param argValues
   */
  private void setFuncArgs(Hashtable<String, Double> argValues) {
    this.funcArgs = argValues;
  }
  
  
  public final String toString(ASTNode value) {
    return value.toString();
  }
  
  
  public double compileDouble(String name, double time) {
    Double funcArg = getFuncArg(name);
    double value;
    if (funcArg != null) {
      value = funcArg;
    } else if (!Double.isNaN(valueHolder.getCurrentValueOf(name))) {
      value = valueHolder.getCurrentValueOf(name);
    } else {
      value = Double.NaN;
    }
    return value;
  }
  
  public double compileDouble(CallableSBase nsb, double time) throws SBMLException {
    if (nsb instanceof Species) {
      Species s = (Species) nsb;
      String id=s.getId();
      double compartmentValue = valueHolder.getCurrentCompartmentValueOf(id);
      if (compartmentValue == 0d) {
        return valueHolder.getCurrentSpeciesValue(id);
      }

      else if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
        return (valueHolder.getCurrentSpeciesValue(id) / compartmentValue);
        
      }

      else if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits()) {
        
        return (valueHolder.getCurrentSpeciesValue(id) * compartmentValue);
      } else {
        return valueHolder.getCurrentSpeciesValue(id);
      }
    }
    else if ((nsb instanceof Compartment) || (nsb instanceof Parameter)) {
      String id=nsb.getId();
      return valueHolder.getCurrentValueOf(id);
    } 
    else if (nsb instanceof LocalParameter) {
      LocalParameter p = (LocalParameter) nsb;
      return p.getValue();
      
      
    } else if (nsb instanceof Reaction) {
      Reaction r = (Reaction) nsb;
      if (r.isSetKineticLaw()) { ((ASTNodeObject)r.getKineticLaw()
          .getMath().getUserObject()).compileDouble(time); }
    }
    return Double.NaN;
  }
  
  public boolean compileBoolean(CallableSBase nsb, double time) throws SBMLException {
    if (nsb instanceof FunctionDefinition) { return functionBoolean(
      (FunctionDefinition) nsb, new LinkedList<ASTNodeObject>(), time); }
    return false;
  }
  
  public double functionDouble(FunctionDefinition function,
    List<ASTNodeObject> arguments, double time) throws SBMLException {
    ASTNode lambda = function.getMath();
    Hashtable<String, Double> argValues = new Hashtable<String, Double>();
    for (int i = 0; i < arguments.size(); i++) {
      argValues.put(compileString(lambda.getChild(i)),
        arguments.get(i).compileDouble(time));
    }
    setFuncArgs(argValues);
    double value = ((ASTNodeObject)lambda.getRightChild().getUserObject()).compileDouble(time);
    clearFuncArgs();
    return value;
  }
  
  public String compileString(ASTNode child) {
    if (child.isName()) {
      return child.getName();
    } else {
      return child.toString();
    }
  }
  
  public double lambdaDouble(List<ASTNodeObject> nodes, double time) throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = nodes.get(i).compileDouble(time);
    }
    // TODO: what happens with d?
    return nodes.get(nodes.size() - 1).compileDouble(time);
  }
  
  public boolean lambdaBoolean(List<ASTNodeObject> nodes, double time) throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = nodes.get(i).compileDouble(time);
    }
    // TODO: what happens with d?
    return nodes.get(nodes.size() - 1).compileBoolean(time);
    
  }
  
  public double piecewise(List<ASTNodeObject> nodes, double time) throws SBMLException {
    int i;
    for (i = 1; i < nodes.size() - 1; i += 2) {
      if (nodes.get(i).compileBoolean(time)) { return (nodes.get(i - 1).compileDouble(time)); }
    }
    return nodes.get(i - 1).compileDouble(time);
    
  }
  
  public double log(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.log(userObject.compileDouble(time));
  }
  
  public double log(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return Maths.log(left.compileDouble(time), right.compileDouble(time));
  }
  
  public double functionDouble(String functionDefinitionName,
    List<ASTNodeObject> args) throws SBMLException {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return Double.NaN;
  }
  
  public double tanh(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.tanh(userObject.compileDouble(time));
  }
  
  public double tan(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.tan(userObject.compileDouble(time));
  }
  
  public double sinh(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.sinh(userObject.compileDouble(time));
  }
  
  public double sin(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.sin(userObject.compileDouble(time));
  }
  
  public double sech(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.sech(userObject.compileDouble(time));
  }
  
  public double sec(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.sec(userObject.compileDouble(time));
  }
  
  public double root(ASTNodeObject rootExponent, ASTNodeObject radiant, double time)
    throws SBMLException {
    return root(rootExponent.compileDouble(time), radiant,time);
  }
  
  public double root(double rootExponent, ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.root(userObject.compileDouble(time), rootExponent);
  }
  
  public double ln(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.ln(userObject.compileDouble(time));
  }
  
  public double floor(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.floor(userObject.compileDouble(time));
  }
  
  public double factorial(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.factorial((int) Math.round(userObject.compileDouble(time)));
  }
  
  public double exp(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.exp(userObject.compileDouble(time));
  }
  
  public double csch(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.csch(userObject.compileDouble(time));
  }
  
  public double csc(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.csc(userObject.compileDouble(time));
  }
  
  public double coth(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.coth(userObject.compileDouble(time));
  }
  
  public double cot(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.cot(userObject.compileDouble(time));
  }
  
  public double cosh(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.cosh(userObject.compileDouble(time));
  }
  
  public double cos(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.cos(userObject.compileDouble(time));
  }
  
  public double ceiling(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.ceil(userObject.compileDouble(time));
  }
  
  public double arctanh(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.atan(userObject.compileDouble(time));
  }
  
  public boolean functionBoolean(String name, List<ASTNodeObject> children) {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return false;
  }
  
  public boolean functionBoolean(FunctionDefinition function,
    List<ASTNodeObject> arguments, double time) throws SBMLException {
    ASTNode lambda = function.getMath();
    
    Hashtable<String, Double> argValues = new Hashtable<String, Double>();
    for (int i = 0; i < arguments.size(); i++) {
      argValues.put(compileString(lambda.getChild(i)),
        arguments.get(i).compileDouble(time));
    }
    setFuncArgs(argValues);
    boolean value = ((ASTNodeObject)lambda.getRightChild().getUserObject()).compileBoolean(time);
    clearFuncArgs();
    return value;
  }
  
  public boolean lt(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) < right.compileDouble(time));
  }
  
  public boolean leq(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) <= right.compileDouble(time));
  }
  
  public boolean neq(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) != right.compileDouble(time));
  }
  
  public boolean gt(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) > right.compileDouble(time));
  }
  
  public boolean geq(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) >= right.compileDouble(time));
  }
  
  public boolean eq(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) == right.compileDouble(time));
  }
  
  public boolean not(ASTNodeObject node, double time) throws SBMLException {
    return node.compileBoolean(time) ? false : true;
  }
  
  public boolean or(List<ASTNodeObject> nodes, double time) throws SBMLException {
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i).compileBoolean(time)) { return true; }
    }
    return false;
  }
  
  public boolean xor(List<ASTNodeObject> nodes, double time) throws SBMLException {
    boolean value = false;
    int size=nodes.size();
    for (int i = 0; i < size; i++) {
      if (nodes.get(i).compileBoolean(time)) {
        if (value) {
          return false;
        } else {
          value = true;
        }
      }
    }
    return value;
  }
  
  public boolean and(List<ASTNodeObject> nodes, double time) throws SBMLException {
    int size=nodes.size();
    for (int i = 0; i < size; i++) {
      if (!(nodes.get(i)).compileBoolean(time)) { return false; }
    }
    return true;
  }
  
  public double arctan(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.atan(userObject.compileDouble(time));
  }
  
  public double arcsinh(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arcsinh(userObject.compileDouble(time));
  }
  
  public double arcsin(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.asin(userObject.compileDouble(time));
  }
  
  public double arcsech(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arcsech(userObject.compileDouble(time));
  }
  
  public double arcsec(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arcsec(userObject.compileDouble(time));
  }
  
  public double arccsch(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arccsch(userObject.compileDouble(time));
  }
  
  public double arccsc(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arccsc(userObject.compileDouble(time));
  }
  
  public double arccoth(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arccoth(userObject.compileDouble(time));
  }
  
  public double arccot(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arccot(userObject.compileDouble(time));
  }
  
  public double arccosh(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.arccosh(userObject.compileDouble(time));
  }
  
  public double arccos(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.acos(userObject.compileDouble(time));
  }
  
  public double abs(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.abs(userObject.compileDouble(time));
  }
  
  public double compile(double mantissa, int exponent, String units) {
    return (mantissa * Math.pow(10, exponent));
  }
  
  public double delay(String name, ASTNodeObject leftChild, ASTNodeObject rightChild,
    String units) {
    // TODO Auto-generated method stub
    return 0;
  }
  
  public double symbolTime(String name) {
    return (valueHolder.getCurrentTime());
  }
  
  public double frac(int numerator, int denominator) {
    return (numerator / denominator);
  }
  
  public double frac(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    return (left.compileDouble(time) / right.compileDouble(time));
  }
  
  public double times(List<ASTNodeObject> nodes, double time) throws SBMLException {
    int size = nodes.size();
    if (size == 0) {
      return (0d);
    } else {
      double value = 1d;
      
      for (int i = 0; i != size; i++) {
        value *= nodes.get(i).compileDouble(time);
      }
      return value;
    }
  }
  
  public double minus(List<ASTNodeObject> nodes, double time) throws SBMLException {
    int size = nodes.size();
    
    double value = 0d;
    if (size > 0) {
      value = nodes.get(0).compileDouble(time);
    }
    for (int i = 1; i < size; i++) {
      value -= nodes.get(i).compileDouble(time);
    }
    return value;
  }
  
  public double plus(List<ASTNodeObject> nodes, double time) throws SBMLException {
    int size = nodes.size();
    double value = 0d;
    for (int i = 0; i != size; i++) {
        value += nodes.get(i).compileDouble(time);
    }
      return value;
    }
  
  public double pow(ASTNodeObject left, ASTNodeObject right, double time) throws SBMLException {
    double l = left.compileDouble(time);
    double r = right.compileDouble(time);
    double result= Math.pow(l,r);
    return result;
  }
  
  public double compile(double value, String units) {
    // TODO: units!
    return value;
  }
  
  public double sqrt(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.sqrt(userObject.compileDouble(time));
  }
  
  public double uMinus(ASTNodeObject userObject, double time) throws SBMLException {
    return (-userObject.compileDouble(time));
  }
}