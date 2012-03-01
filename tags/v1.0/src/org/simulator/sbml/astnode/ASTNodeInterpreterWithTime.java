/*
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

package org.simulator.sbml.astnode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
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
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.ValueHolder;

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
    this.funcArgs=new HashMap<String,Double>();
  }
  
  
  /**
   * 
   * @param value
   * @return
   */
  public final String toString(ASTNode value) {
    return value.toString();
  }
  
  /**
   * 
   * @param name
   * @param time
   * @return
   */
  public double compileDouble(String name, double time) {
    Double funcArg = funcArgs.get(name).doubleValue();
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
  
  /**
   * 
   * @param nsb
   * @param time
   * @return
   * @throws SBMLException
   */
  public double compileDouble(CallableSBase nsb, double time)
    throws SBMLException {
    if (nsb instanceof Species) {
      Species s = (Species) nsb;
      String id = s.getId();
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
    } else if ((nsb instanceof Compartment) || (nsb instanceof Parameter)) {
      String id = nsb.getId();
      return valueHolder.getCurrentValueOf(id);
    } else if (nsb instanceof LocalParameter) {
      LocalParameter p = (LocalParameter) nsb;
      return p.getValue();
      
    } else if (nsb instanceof Reaction) {
      Reaction r = (Reaction) nsb;
      if (r.isSetKineticLaw()) {
        ((ASTNodeObject) r.getKineticLaw().getMath().getUserObject())
            .compileDouble(time);
      }
    }
    return Double.NaN;
  }
  
  /**
   * 
   * @param nsb
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean compileBoolean(CallableSBase nsb, double time)
    throws SBMLException {
    if (nsb instanceof FunctionDefinition) {
      ASTNode math = ((FunctionDefinition) nsb).getMath();
      ASTNodeObject rightChild = (ASTNodeObject) math.getRightChild()
          .getUserObject();
      List<String> variables = new ArrayList<String>(math.getChildCount());
      for (ASTNode child : math.getChildren()) {
        variables.add(compileString(child));
      }
      return functionBoolean(rightChild, variables,
        new LinkedList<ASTNodeObject>(), new double[math.getChildCount()], time);
    }
    return false;
  }
  
  /**
   * 
   * @param rightChild
   * @param variables
   * @param arguments
   * @param nArguments
   * @param values
   * @param time
   * @return
   * @throws SBMLException
   */
  public double functionDouble(ASTNodeObject rightChild,
    List<String> variables, List<ASTNodeObject> arguments, int nArguments, double[] values, double time)
    throws SBMLException {
    for (int i = 0; i < nArguments; i++) {
      values[i] = arguments.get(i).compileDouble(time);
    }
    double value = rightChild.compileDouble(time);
    return value;
  }
  
  /**
   * 
   * @param child
   * @return
   */
  public String compileString(ASTNodeObject child) {
    if (child.isName()) {
      return child.getName();
    } else {
      return child.toString();
    }
  }
  
  /**
   * 
   * @param child
   * @return
   */
  public String compileString(ASTNode child) {
    if (child.isName()) {
      return child.getName();
    } else {
      return child.toString();
    }
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public double lambdaDouble(List<ASTNodeObject> nodes, double time)
    throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = nodes.get(i).compileDouble(time);
    }
    // TODO: what happens with d?
    return nodes.get(nodes.size() - 1).compileDouble(time);
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean lambdaBoolean(List<ASTNodeObject> nodes, double time)
    throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = nodes.get(i).compileDouble(time);
    }
    // TODO: what happens with d?
    return nodes.get(nodes.size() - 1).compileBoolean(time);
    
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public double piecewise(List<ASTNodeObject> nodes, double time)
    throws SBMLException {
    int i;
    for (i = 1; i < nodes.size() - 1; i += 2) {
      if (nodes.get(i).compileBoolean(time)) { return (nodes.get(i - 1)
          .compileDouble(time)); }
    }
    return nodes.get(i - 1).compileDouble(time);
    
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double log(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.log10(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public double log(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return Maths.log(right.compileDouble(time), left.compileDouble(time));
  }
  
  /**
   * 
   * @param functionDefinitionName
   * @param args
   * @param time
   * @return
   * @throws SBMLException
   */
  public double functionDouble(String functionDefinitionName,
    List<ASTNodeObject> args, double time) throws SBMLException {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return Double.NaN;
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double tanh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.tanh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double tan(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.tan(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double sinh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.sinh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double sin(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.sin(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double sech(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.sech(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double sec(ASTNodeObject userObject, double time) throws SBMLException {
    double argument = userObject.compileDouble(time);
    return Maths.sec(argument);
  }
  
  /**
   * 
   * @param rootExponent
   * @param radiant
   * @param time
   * @return
   * @throws SBMLException
   */
  public double root(ASTNodeObject rootExponent, ASTNodeObject radiant,
    double time) throws SBMLException {
    return root(rootExponent.compileDouble(time), radiant, time);
  }
  
  /**
   * 
   * @param rootExponent
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double root(double rootExponent, ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.root(userObject.compileDouble(time), rootExponent);
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double ln(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.ln(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double floor(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.floor(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double factorial(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.factorial((int) Math.round(userObject.compileDouble(time)));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double exp(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.exp(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double csch(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.csch(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double csc(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.csc(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double coth(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.coth(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double cot(ASTNodeObject userObject, double time) throws SBMLException {
    return Maths.cot(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double cosh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.cosh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double cos(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.cos(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double ceiling(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.ceil(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arctanh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arctanh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param name
   * @param children
   * @return
   */
  public boolean functionBoolean(String name, List<ASTNodeObject> children) {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return false;
  }
  
  /**
   * 
   * @param rightChild
   * @param variables
   * @param arguments
   * @param values
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean functionBoolean(ASTNodeObject rightChild,
    List<String> variables, List<ASTNodeObject> arguments, double[] values, double time)
    throws SBMLException {
    for (int i = 0; i < arguments.size(); i++) {
      values[i] = arguments.get(i).compileDouble(time);
    }
    boolean value = rightChild.compileBoolean(time);
    return value;
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean lt(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) < right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean leq(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) <= right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean neq(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) != right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean gt(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) > right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean geq(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) >= right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean eq(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) == right.compileDouble(time));
  }
  
  /**
   * 
   * @param node
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean not(ASTNodeObject node, double time) throws SBMLException {
    return node.compileBoolean(time) ? false : true;
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean or(List<ASTNodeObject> nodes, double time)
    throws SBMLException {
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i).compileBoolean(time)) { return true; }
    }
    return false;
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean xor(List<ASTNodeObject> nodes, double time)
    throws SBMLException {
    boolean value = false;
    int size = nodes.size();
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
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   * @throws SBMLException
   */
  public boolean and(List<ASTNodeObject> nodes, int size, double time)
    throws SBMLException {
    for (int i = 0; i < size; i++) {
      if (!(nodes.get(i)).compileBoolean(time)) { return false; }
    }
    return true;
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arctan(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.atan(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arcsinh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arcsinh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arcsin(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.asin(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arcsech(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arcsech(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arcsec(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arcsec(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccsch(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arccsch(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccsc(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arccsc(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @paam userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccoth(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arccoth(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccot(ASTNodeObject userObject, double time)
    throws SBMLException {
    double argument = userObject.compileDouble(time);
    return Maths.arccot(argument);
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccosh(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Maths.arccosh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double arccos(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.acos(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double abs(ASTNodeObject userObject, double time) throws SBMLException {
    return Math.abs(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param mantissa
   * @param exponent
   * @param units
   * @return
   */
  public double compile(double mantissa, int exponent, String units) {
    return (mantissa * Math.pow(10, exponent));
  }
  
  /**
   * 
   * @param delayName
   * @param x
   * @param delay
   * @param timeUnits
   * @return
   * @throws SBMLException
   */
  public final double delay(String delayName, ASTNodeObject x, ASTNodeObject delay,
    String timeUnits, double time) throws SBMLException {
    //TODO: Delay for arbitrary expressions.
    double delayTime = delay.compileDouble(time);
    if(delayTime == 0) {
      return x.compileDouble(time);
    }
    double valueTime=symbolTime(delayName)- delayTime;
    try {
      return valueHolder.computeDelayedValue(valueTime, compileString(x));
    } catch (DerivativeException e) {
      return Double.NaN;
    }
    
  }
  
  /**
   * 
   * @param name
   * @return
   */
  public double symbolTime(String name) {
    return (valueHolder.getCurrentTime());
  }
  
  /**
   * 
   * @param numerator
   * @param denominator
   * @return
   */
  public double frac(int numerator, int denominator) {
    return ((double)numerator / (double)denominator);
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public double frac(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    return (left.compileDouble(time) / right.compileDouble(time));
  }
  
  /**
   * 
   * @param nodes
   * @param size
   * @param time
   * @return
   * @throws SBMLException
   */
  public double times(List<ASTNodeObject> nodes, int size, double time)
    throws SBMLException {
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
  
  /**
   * 
   * @param nodes
   * @param size
   * @param time
   * @return
   * @throws SBMLException
   */
  public double minus(List<ASTNodeObject> nodes, int size, double time)
    throws SBMLException {
    
    double value = 0d;
    if (size > 0) {
      value = nodes.get(0).compileDouble(time);
    }
    for (int i = 1; i < size; i++) {
      value -= nodes.get(i).compileDouble(time);
    }
    return value;
  }
  
  /**
   * 
   * @param nodes
   * @param size
   * @param time
   * @return
   * @throws SBMLException
   */
  public double plus(List<ASTNodeObject> nodes, int size, double time)
    throws SBMLException {
    double value = 0d;
    for (int i = 0; i != size; i++) {
      value += nodes.get(i).compileDouble(time);
    }
    return value;
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   * @throws SBMLException
   */
  public double pow(ASTNodeObject left, ASTNodeObject right, double time)
    throws SBMLException {
    double l = left.compileDouble(time);
    double r = right.compileDouble(time);
    double result = Math.pow(Math.abs(l), r);
    if(l<0) {
      double sign=Math.pow(-1, r);
      if(Double.isNaN(sign)) {
        sign = -1;
        logger.warning("Power with negative base and non-integer exponent encountered.");
      }
      result = result*sign;
    }
    return result;
  }
  
  /**
   * 
   * @param value
   * @param units
   * @return
   */
  public double compile(double value, String units) {
    // TODO: units!
    return value;
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double sqrt(ASTNodeObject userObject, double time)
    throws SBMLException {
    return Math.sqrt(userObject.compileDouble(time));
  }
 
  /**
   * 
   * @param userObject
   * @param time
   * @return
   * @throws SBMLException
   */
  public double uMinus(ASTNodeObject userObject, double time)
    throws SBMLException {
    return (-userObject.compileDouble(time));
  }
}