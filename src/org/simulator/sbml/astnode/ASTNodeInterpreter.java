/*
 * $Id$
 * $URL$
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

import java.util.ArrayList;
import java.util.HashMap;
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
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.Maths;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class is an efficient ASTNodeInterpreter that takes the time of the
 * calculation into account. It contains functions similar to the
 * {@link ASTNodeCompiler} interface, which have the current time as additional
 * argument.
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class ASTNodeInterpreter {
  /**
   * A logger.
   */
  public static final Logger logger = Logger.getLogger(SBMLinterpreter.class.getName());
  
  /**
   * This table is necessary to store the values of arguments when a function
   * definition is evaluated. For an identifier of the argument the
   * corresponding value will be stored.
   */
  private Map<String, Double> funcArgs;
  
  /**
   * The value holder that stores the current simulation results.
   */
  private SBMLValueHolder valueHolder;
  
  /**
   * 
   * @param valueHolder
   */
  public ASTNodeInterpreter(SBMLValueHolder valueHolder) {
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
   */
  public double compileDouble(CallableSBase nsb, double time) {
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
        ((ASTNodeValue) r.getKineticLaw().getMath().getUserObject(SBMLinterpreter.TEMP_VALUE))
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
   */
  public boolean compileBoolean(CallableSBase nsb, double time) {
    if (nsb instanceof FunctionDefinition) {
      ASTNode math = ((FunctionDefinition) nsb).getMath();
      ASTNodeValue rightChild = (ASTNodeValue) math.getRightChild().getUserObject(SBMLinterpreter.TEMP_VALUE);
      List<String> variables = new ArrayList<String>(math.getChildCount());
      for (ASTNode child : math.getChildren()) {
        variables.add(compileString(child));
      }
      return functionBoolean(rightChild, variables,
        new LinkedList<ASTNodeValue>(), new double[math.getChildCount()], time);
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
   */
  public double functionDouble(ASTNodeValue rightChild,
    List<String> variables, List<ASTNodeValue> arguments, int nArguments, double[] values, double time) {
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
  public String compileString(ASTNodeValue child) {
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
   */
  public double lambdaDouble(List<ASTNodeValue> nodes, double time) {
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
   */
  public boolean lambdaBoolean(List<ASTNodeValue> nodes, double time) {
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
   */
  public double piecewise(List<ASTNodeValue> nodes, double time) {
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
   */
  public double log(ASTNodeValue userObject, double time) {
    return Math.log10(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public double log(ASTNodeValue left, ASTNodeValue right, double time) {
    return Maths.log(right.compileDouble(time), left.compileDouble(time));
  }
  
  /**
   * 
   * @param functionDefinitionName
   * @param args
   * @param time
   * @return
   */
  public double functionDouble(String functionDefinitionName,
    List<ASTNodeValue> args, double time) {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return Double.NaN;
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double tanh(ASTNodeValue userObject, double time) {
    return Math.tanh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double tan(ASTNodeValue userObject, double time) {
    return Math.tan(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double sinh(ASTNodeValue userObject, double time) {
    return Math.sinh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double sin(ASTNodeValue userObject, double time) {
    return Math.sin(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double sech(ASTNodeValue userObject, double time) {
    return Maths.sech(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double sec(ASTNodeValue userObject, double time) {
    double argument = userObject.compileDouble(time);
    return Maths.sec(argument);
  }
  
  /**
   * 
   * @param rootExponent
   * @param radiant
   * @param time
   * @return
   */
  public double root(ASTNodeValue rootExponent, ASTNodeValue radiant, double time) {
    return root(rootExponent.compileDouble(time), radiant, time);
  }
  
  /**
   * 
   * @param rootExponent
   * @param userObject
   * @param time
   * @return
   */
  public double root(double rootExponent, ASTNodeValue userObject, double time) {
    return Maths.root(userObject.compileDouble(time), rootExponent);
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double ln(ASTNodeValue userObject, double time) {
    return Maths.ln(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double floor(ASTNodeValue userObject, double time) {
    return Math.floor(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double factorial(ASTNodeValue userObject, double time) {
    return Maths.factorial((int) Math.round(userObject.compileDouble(time)));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double exp(ASTNodeValue userObject, double time) {
    return Math.exp(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double csch(ASTNodeValue userObject, double time) {
    return Maths.csch(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double csc(ASTNodeValue userObject, double time) {
    return Maths.csc(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double coth(ASTNodeValue userObject, double time) {
    return Maths.coth(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double cot(ASTNodeValue userObject, double time) {
    return Maths.cot(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double cosh(ASTNodeValue userObject, double time) {
    return Math.cosh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double cos(ASTNodeValue userObject, double time) {
    return Math.cos(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double ceiling(ASTNodeValue userObject, double time) {
    return Math.ceil(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arctanh(ASTNodeValue userObject, double time) {
    return Maths.arctanh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param name
   * @param children
   * @return
   */
  public boolean functionBoolean(String name, List<ASTNodeValue> children) {
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
   */
  public boolean functionBoolean(ASTNodeValue rightChild,
    List<String> variables, List<ASTNodeValue> arguments, double[] values, double time) {
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
   */
  public boolean lt(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) < right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public boolean leq(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) <= right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public boolean neq(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) != right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public boolean gt(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) > right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public boolean geq(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) >= right.compileDouble(time));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public boolean eq(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) == right.compileDouble(time));
  }
  
  /**
   * 
   * @param node
   * @param time
   * @return
   */
  public boolean not(ASTNodeValue node, double time) {
    return node.compileBoolean(time) ? false : true;
  }
  
  /**
   * 
   * @param nodes
   * @param time
   * @return
   */
  public boolean or(List<ASTNodeValue> nodes, double time) {
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
   */
  public boolean xor(List<ASTNodeValue> nodes, double time) {
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
   */
  public boolean and(List<ASTNodeValue> nodes, int size, double time) {
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
   */
  public double arctan(ASTNodeValue userObject, double time) {
    return Math.atan(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arcsinh(ASTNodeValue userObject, double time) {
    return Maths.arcsinh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arcsin(ASTNodeValue userObject, double time) {
    return Math.asin(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arcsech(ASTNodeValue userObject, double time) {
    return Maths.arcsech(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arcsec(ASTNodeValue userObject, double time) {
    return Maths.arcsec(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccsch(ASTNodeValue userObject, double time) {
    return Maths.arccsch(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccsc(ASTNodeValue userObject, double time) {
    return Maths.arccsc(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccoth(ASTNodeValue userObject, double time) {
    return Maths.arccoth(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccot(ASTNodeValue userObject, double time) {
    double argument = userObject.compileDouble(time);
    return Maths.arccot(argument);
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccosh(ASTNodeValue userObject, double time) {
    return Maths.arccosh(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double arccos(ASTNodeValue userObject, double time) {
    return Math.acos(userObject.compileDouble(time));
  }
  
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double abs(ASTNodeValue userObject, double time) {
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
   * @param time
   * @return
   */
  public final double delay(String delayName, ASTNodeValue x, ASTNodeValue delay,
    String timeUnits, double time) {
    //TODO: Delay for arbitrary expressions.
    double delayTime = delay.compileDouble(time);
    if (delayTime == 0) {
      return x.compileDouble(time);
    }
    double valueTime = symbolTime(delayName) - delayTime;
    return valueHolder.computeDelayedValue(valueTime, compileString(x));
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
    return (numerator / ((double) denominator));
  }
  
  /**
   * 
   * @param left
   * @param right
   * @param time
   * @return
   */
  public double frac(ASTNodeValue left, ASTNodeValue right, double time) {
    return (left.compileDouble(time) / right.compileDouble(time));
  }
  
  /**
   * 
   * @param nodes
   * @param size
   * @param time
   * @return
   */
  public double times(List<ASTNodeValue> nodes, int size, double time) {
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
   */
  public double minus(List<ASTNodeValue> nodes, int size, double time) {
    
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
   */
  public double plus(List<ASTNodeValue> nodes, int size, double time) {
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
   */
  public double pow(ASTNodeValue left, ASTNodeValue right, double time) {
    double l = left.compileDouble(time);
    double r = right.compileDouble(time);
    double result = Math.pow(Math.abs(l), r);
    if (l < 0) {
      double sign = Math.pow(-1, r);
      if (Double.isNaN(sign)) {
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
   */
  public double sqrt(ASTNodeValue userObject, double time) {
    return Math.sqrt(userObject.compileDouble(time));
  }
 
  /**
   * 
   * @param userObject
   * @param time
   * @return
   */
  public double uMinus(ASTNodeValue userObject, double time) {
    return (-userObject.compileDouble(time));
  }

}
