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
package org.simulator.sbml;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.util.Maths;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.sbml.jsbml.util.compilers.ASTNodeValue;

/**   
 * @author Roland Keller    
 * @author Andreas Dr&auml;ger    
 * @version $Rev$   
 * @since 0.9
 */
public class ASTNodeInterpreter implements ASTNodeCompiler {
  
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
  
  private ASTNodeValue nodeValue;
  
  /**
   * 
   * @param valueHolder
   */
  public ASTNodeInterpreter(ValueHolder valueHolder) {
    this.valueHolder = valueHolder;
    this.nodeValue = new ASTNodeValue(this);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue abs(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.abs(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#and(java.util.List)
   */
  public final ASTNodeValue and(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (!node.compile(this).toBoolean()) { return getConstantFalse(); }
    }
    return getConstantTrue();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arccos(ASTNode value) throws SBMLException {
    nodeValue.setValue(Math.acos(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccosh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccosh(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccosh(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arccot(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccot(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccoth(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccoth(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccoth(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arccsc(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccsc(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsch(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccsch(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccsch(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arcsec(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arcsec(value.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsech(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arcsech(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arcsech(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arcsin(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.asin(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsinh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arcsinh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arcsinh(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue arctan(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.atan(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctanh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arctanh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arctanh(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#ceiling(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue ceiling(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.ceil(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /**
     * 
     */
  private void clearFuncArgs() {
    funcArgs.clear();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(org.sbml.jsbml.
   * CallableSBase)
   */
  public ASTNodeValue compile(CallableSBase nsb) throws SBMLException {
    String id = nsb.getId();
    if (nsb instanceof Species) {
      Species s = (Species) nsb;
      double compartmentValue = valueHolder.getCurrentCompartmentValueOf(id);
      if (compartmentValue == 0d) {
        nodeValue.setValue(valueHolder.getCurrentSpeciesValue(id));
      }

      else if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
        nodeValue.setValue(valueHolder.getCurrentSpeciesValue(id)
            / compartmentValue);
        
      }

      else if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits()) {
        // return new ASTNodeValue(Y[symbolIndex], this);
        
        nodeValue.setValue(valueHolder.getCurrentSpeciesValue(id)
            * compartmentValue);
      } else {
        nodeValue.setValue(valueHolder.getCurrentSpeciesValue(id));
        // return new ASTNodeValue(Y[symbolIndex]
        // / getCompartmentValueOf(nsb.getId()), this);
      }
    }

    else if (nsb instanceof Compartment || nsb instanceof Parameter) {
      nodeValue.setValue(valueHolder.getCurrentValueOf(id));
    } else if (nsb instanceof LocalParameter) {
      LocalParameter p = (LocalParameter) nsb;
      // parent: list of parameter; parent of parent: kinetic law
      SBase parent = p.getParentSBMLObject().getParentSBMLObject();
      if (parent instanceof KineticLaw) {
        ListOf<LocalParameter> params = ((KineticLaw) parent)
            .getListOfLocalParameters();
        boolean set = false;
        for (int i = 0; i < params.size(); i++) {
          if (p.getId() == params.get(i).getId()) {
            nodeValue.setValue(params.get(i).getValue());
            set = true;
            break;
          }
        }
        if (!set) {
          nodeValue.setValue(valueHolder.getCurrentValueOf(id));
        }
        
      }
      
    } else if (nsb instanceof SpeciesReference) {
      nodeValue.setValue(valueHolder.getCurrentStoichiometry(id));
    } else if (nsb instanceof FunctionDefinition) {
      return function((FunctionDefinition) nsb, new LinkedList<ASTNode>());
    } else if (nsb instanceof Reaction) {
      Reaction r = (Reaction) nsb;
      if (r.isSetKineticLaw()) {
        return r.getKineticLaw().getMath().compile(this);
      } else {
        nodeValue.setValue(Double.NaN);
      }
    } else {
      nodeValue.setValue(Double.NaN);
    }
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(org.sbml.jsbml.
   * Compartment)
   */
  public final ASTNodeValue compile(Compartment c) {
    nodeValue.setValue(valueHolder.getCurrentCompartmentSize(c.getId()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(double, int,
   * java.lang.String)
   */
  public final ASTNodeValue compile(double mantissa, int exponent, String units) {
    nodeValue.setValue(mantissa * Math.pow(10, exponent));
    return nodeValue;
  }
  
  public final ASTNodeValue compile(double value, String units) {
    // TODO: units!
    nodeValue.setValue(value);
    return nodeValue;
  }
  
  public final ASTNodeValue compile(int value, String units) {
    // TODO: units!
    nodeValue.setValue(value);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(java.lang.String)
   */
  public final ASTNodeValue compile(String name) {
    Double funcArg = getFuncArg(name);
    if (funcArg != null) {
      nodeValue.setValue(funcArg);
    } else if (!Double.isNaN(valueHolder.getCurrentValueOf(name))) {
      nodeValue.setValue(valueHolder.getCurrentValueOf(name));
    } else {
      
      nodeValue.setValue(String.valueOf(name));
    }
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue cos(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.cos(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue cosh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.cosh(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue cot(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.cot(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue coth(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.coth(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue csc(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.csc(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue csch(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.csch(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#delay(java.lang.String,
   * org.sbml.jsbml.ASTNode, org.sbml.jsbml.ASTNode, java.lang.String)
   */
  public final ASTNodeValue delay(String delayName, ASTNode x, ASTNode delay,
    String timeUnits) throws SBMLException {
    // TODO Auto-generated method stub
    return null;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#eq(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue eq(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue((left.compile(this).toDouble() == right.compile(this)
        .toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue exp(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.exp(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#factorial(org.sbml.jsbml
   * .ASTNode)
   */
  public final ASTNodeValue factorial(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.factorial((int) Math.round(node.compile(this)
        .toDouble())));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue floor(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.floor(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode ,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue frac(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue(left.compile(this).toDouble()
        / right.compile(this).toDouble());
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#frac(int, int)
   */
  public final ASTNodeValue frac(int numerator, int denominator) {
    nodeValue.setValue(numerator / denominator);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#function(org.sbml.jsbml
   * .FunctionDefinition, java.util.List)
   */
  public final ASTNodeValue function(FunctionDefinition function,
    List<ASTNode> arguments) throws SBMLException {
    ASTNode lambda = function.getMath();
    Hashtable<String, Double> argValues = new Hashtable<String, Double>();
    for (int i = 0; i < arguments.size(); i++) {
      argValues.put(lambda.getChild(i).compile(this).toString(),
        arguments.get(i).compile(this).toDouble());
    }
    setFuncArgs(argValues);
    ASTNodeValue value = lambda.getRightChild().compile(this);
    clearFuncArgs();
    return value;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#function(java.lang.String,
   * java.util.List)
   */
  public final ASTNodeValue function(String functionDefinitionName,
    List<ASTNode> args) throws SBMLException {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return null;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#geq(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue geq(ASTNode nodeleft, ASTNode noderight)
    throws SBMLException {
    nodeValue.setValue((nodeleft.compile(this).toDouble() >= noderight.compile(
      this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantAvogadro(java
   * .lang.String)
   */
  public final ASTNodeValue getConstantAvogadro(String name) {
    nodeValue.setValue(Maths.AVOGADRO);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantE()
   */
  public final ASTNodeValue getConstantE() {
    nodeValue.setValue(Math.E);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantFalse()
   */
  public final ASTNodeValue getConstantFalse() {
    nodeValue.setValue(false);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantPi()
   */
  public final ASTNodeValue getConstantPi() {
    nodeValue.setValue(Math.PI);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantTrue()
   */
  public final ASTNodeValue getConstantTrue() {
    nodeValue.setValue(true);
    return nodeValue;
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
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getNegativeInfinity()
   */
  public final ASTNodeValue getNegativeInfinity() {
    nodeValue.setValue(Double.NEGATIVE_INFINITY);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getPositiveInfinity()
   */
  public final ASTNodeValue getPositiveInfinity() {
    nodeValue.setValue(Double.POSITIVE_INFINITY);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#gt(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue gt(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue((left.compile(this).toDouble() > right.compile(this)
        .toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#lambda(java.util.List)
   */
  public final ASTNodeValue lambda(List<ASTNode> nodes) throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = nodes.get(i).compile(this).toDouble();
    }
    // TODO: what happens with d?
    ASTNodeValue function = nodes.get(nodes.size() - 1).compile(this);
    return function;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#leq(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue leq(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue((left.compile(this).toDouble() <= right.compile(this)
        .toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue ln(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.ln(node.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue log(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.log(node.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue log(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue(Maths.log(left.compile(this).toDouble(),
      right.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#lt(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue lt(ASTNode nodeleft, ASTNode noderight)
    throws SBMLException {
    nodeValue.setValue((nodeleft.compile(this).toDouble() < noderight.compile(
      this).toDouble()));
    return nodeValue;
  }
  
  // /**
  // *
  // * @param lambda
  // * @param names
  // * @param d
  // * @return
  // */
  // private ASTNode replace(ASTNode lambda, Hashtable<String, Double>
  // args) {
  // String name;
  // for (ASTNode child : lambda.getListOfNodes())
  // if (child.isName() && args.containsKey(child.getName())) {
  // name = child.getName();
  // child.setType(ASTNode.Type.REAL);
  // child.setValue(args.get(name));
  // } else if (child.getNumChildren() > 0)
  // child = replace(child, args);
  // return lambda;
  // }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#minus(java.util.List)
   */
  public final ASTNodeValue minus(List<ASTNode> nodes) throws SBMLException {
    double value = 0d;
    if (nodes.size() > 0) {
      value = nodes.get(0).compile(this).toDouble();
    }
    for (int i = 1; i < nodes.size(); i++) {
      value -= nodes.get(i).compile(this).toDouble();
    }
    nodeValue.setValue(value);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#neq(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue neq(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue((left.compile(this).toDouble() != right.compile(this)
        .toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue not(ASTNode node) throws SBMLException {
    return node.compile(this).toBoolean() ? getConstantFalse()
        : getConstantTrue();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#or(java.util.List)
   */
  public final ASTNodeValue or(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (node.compile(this).toBoolean()) { return getConstantTrue(); }
    }
    return getConstantFalse();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#piecewise(java.util.List)
   */
  public final ASTNodeValue piecewise(List<ASTNode> nodes) throws SBMLException {
    int i;
    boolean set = false;
    for (i = 1; i < nodes.size() - 1; i += 2) {
      if (nodes.get(i).compile(this).toBoolean()) {
        nodeValue.setValue(nodes.get(i - 1).compile(this).toDouble());
        set = true;
        break;
      }
    }
    if (!set) {
      nodeValue.setValue(nodes.get(i - 1).compile(this).toDouble());
    }
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#plus(java.util.List)
   */
  public final ASTNodeValue plus(List<ASTNode> nodes) throws SBMLException {
    double value = 0d;
    for (ASTNode node : nodes) {
      value += node.compile(this).toDouble();
    }
    nodeValue.setValue(value);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue pow(ASTNode left, ASTNode right)
    throws SBMLException {
    nodeValue.setValue(Math.pow(left.compile(this).toDouble(),
      right.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode ,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue root(ASTNode rootExponent, ASTNode radiant)
    throws SBMLException {
    return root(rootExponent.compile(this).toDouble(), radiant);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(double,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue root(double rootExponent, ASTNode radiant)
    throws SBMLException {
    nodeValue.setValue(Maths.root(radiant.compile(this).toDouble(),
      rootExponent));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue sec(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.sec(node.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sech(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.sech(node.compile(this).toDouble()));
    return nodeValue;
    
  }
  
  /**
   * @param argValues
   */
  private void setFuncArgs(Hashtable<String, Double> argValues) {
    this.funcArgs = argValues;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sin(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue sin(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.sin(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sinh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.sinh(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sqrt(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.sqrt(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#symbolTime(java.lang.String )
   */
  public final ASTNodeValue symbolTime(String timeSymbol) {
    nodeValue.setValue(valueHolder.getCurrentTime());
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue tan(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.tan(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue tanh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.tanh(node.compile(this).toDouble()));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#times(java.util.List)
   */
  public final ASTNodeValue times(List<ASTNode> nodes) throws SBMLException {
    int size = nodes.size();
    if (size == 0) {
      nodeValue.setValue(0d);
    } else {
      double value = 1d;
      
      for (int i = 0; i != size; i++) {
        value *= nodes.get(i).compile(this).toDouble();
      }
      nodeValue.setValue(value);
    }
    return nodeValue;
  }
  
  public final String toString(ASTNode value) {
    return value.toString();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#uMinus(org.sbml.jsbml.ASTNode
   * )
   */
  public final ASTNodeValue uMinus(ASTNode node) throws SBMLException {
    nodeValue.setValue(-node.compile(this).toDouble());
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#unknownValue()
   */
  public final ASTNodeValue unknownValue() throws SBMLException {
    nodeValue.setValue(Double.NaN);
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#xor(java.util.List)
   */
  public final ASTNodeValue xor(List<ASTNode> nodes) throws SBMLException {
    boolean value = false;
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i).compile(this).toBoolean()) {
        if (value) {
          return getConstantFalse();
        } else {
          value = true;
        }
      }
    }
    nodeValue.setValue(value);
    return nodeValue;
  }
  
}
