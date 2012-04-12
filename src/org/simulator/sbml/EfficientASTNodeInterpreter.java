/*
 * $Id: ASTNodeInterpreter.java 12 2011-08-17 14:41:59Z rolandkel $ $URL:
 * https:/
 * /sbml-simulator.svn.sourceforge.net/svnroot/sbml-simulator/trunk/src/org
 * /sbml/simulator/math/ASTNodeInterpreter.java $
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
package org.simulator.sbml;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
 * This class is a more efficient implementation of an ASTNodeCompiler. 
 * @author Roland Keller
 * @author Andreas Dr&auml;ger
 * @version $Rev: 12 $
 * @since 0.9
 */
public class EfficientASTNodeInterpreter implements ASTNodeCompiler {
  /**
   * A logger.
   */
  private static final Logger logger = Logger.getLogger(SBMLinterpreter.class
      .getName());
  /**
   * This table is necessary to store the values of arguments when a function
   * definition is evaluated. For an identifier of the argument the
   * corresponding value will be stored.
   */
  private Map<String, Double> funcArgs;
  
  /**
   * The value holder that is used for calculating the values.
   */
  private ValueHolder valueHolder;
  
  /**
   * Field for saving intermediate values.
   */
  private ASTNodeValue nodeValue;
  
  /**
   * Field for saving the currently processed variable.
   */
  private CallableSBase variable;
  
  /**
   * Constructs an interpreter with a specific value holder.
   * @param valueHolder
   */
  public EfficientASTNodeInterpreter(ValueHolder valueHolder) {
    this.valueHolder = valueHolder;
    this.nodeValue = new SpecialASTNodeValue(this);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue abs(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.abs(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#and(java.util.List)
   */
  public final ASTNodeValue and(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (!compileBoolean(node)) { return getConstantFalse(); }
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
    nodeValue.setValue(Math.acos(compileDouble(value)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccosh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccosh(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccosh(compileDouble(value)));
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
    double argument = compileDouble(value);
    nodeValue.setValue(Maths.arccot(argument));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccoth(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccoth(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccoth(compileDouble(value)));
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
    nodeValue.setValue(Maths.arccsc(compileDouble(value)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsch(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arccsch(ASTNode value) throws SBMLException {
    nodeValue.setValue(Maths.arccsch(compileDouble(value)));
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
    nodeValue.setValue(Maths.arcsec(compileDouble(value)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsech(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arcsech(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arcsech(compileDouble(node)));
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
    nodeValue.setValue(Math.asin(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsinh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arcsinh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arcsinh(compileDouble(node)));
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
    nodeValue.setValue(Math.atan(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctanh(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue arctanh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.arctanh(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#ceiling(org.sbml.jsbml.
   * ASTNode)
   */
  public final ASTNodeValue ceiling(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.ceil(compileDouble(node)));
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
        nodeValue.setValue(compileDouble(r.getKineticLaw().getMath()));
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
    nodeValue.setValue(Math.cos(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue cosh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.cosh(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue cot(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.cot(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue coth(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.coth(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue csc(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.csc(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue csch(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.csch(compileDouble(node)));
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
    nodeValue.setValue((compileDouble(left) == compileDouble(right)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue exp(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.exp(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#factorial(org.sbml.jsbml
   * .ASTNode)
   */
  public final ASTNodeValue factorial(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.factorial((int) Math.round(compileDouble(node))));
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
    nodeValue.setValue(Math.floor(compileDouble(node)));
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
    nodeValue.setValue(compileDouble(left) / compileDouble(right));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#frac(int, int)
   */
  public final ASTNodeValue frac(int numerator, int denominator) {
    nodeValue.setValue((double)numerator / denominator);
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
      argValues.put(compileString(lambda.getChild(i)),
        compileDouble(arguments.get(i)));
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
    nodeValue.setValue((compileDouble(nodeleft) >= compileDouble(noderight)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantAvogadro(java
   * .lang.String)
   */
  public final ASTNodeValue getConstantAvogadro(String name) {
    nodeValue.setValue(Maths.AVOGADRO_L3V1);
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
    nodeValue.setValue((compileDouble(left) > compileDouble(right)));
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
      d[i++] = compileDouble(nodes.get(i));
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
    nodeValue.setValue((compileDouble(left) <= compileDouble(right)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue ln(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.ln(compileDouble(node)));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue log(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.log(compileDouble(node)));
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
    nodeValue.setValue(Maths.log(compileDouble(right), compileDouble(left)));
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
    nodeValue.setValue((compileDouble(nodeleft) < compileDouble(noderight)));
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
      value = compileDouble(nodes.get(0));
    }
    for (int i = 1; i < nodes.size(); i++) {
      value -= compileDouble(nodes.get(i));
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
    nodeValue.setValue(compileDouble(left) != compileDouble(right));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue not(ASTNode node) throws SBMLException {
    return compileBoolean(node) ? getConstantFalse() : getConstantTrue();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#or(java.util.List)
   */
  public final ASTNodeValue or(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (compileBoolean(node)) { return getConstantTrue(); }
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
      if (compileBoolean(nodes.get(i))) {
        nodeValue.setValue(compileDouble(nodes.get(i - 1)));
        set = true;
        break;
      }
    }
    if (!set) {
      nodeValue.setValue(compileDouble(nodes.get(i - 1)));
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
      value += compileDouble(node);
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
    nodeValue.setValue(Math.pow(compileDouble(left), compileDouble(right)));
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
    return root(compileDouble(rootExponent), radiant);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(double,
   * org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue root(double rootExponent, ASTNode radiant)
    throws SBMLException {
    nodeValue.setValue(Maths.root(compileDouble(radiant), rootExponent));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
   */
  public final ASTNodeValue sec(ASTNode node) throws SBMLException {
    double argument = compileDouble(node);
    nodeValue.setValue(Maths.sec(argument));
    return nodeValue;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sech(ASTNode node) throws SBMLException {
    nodeValue.setValue(Maths.sech(compileDouble(node)));
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
    nodeValue.setValue(Math.sin(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sinh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.sinh(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue sqrt(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.sqrt(compileDouble(node)));
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
    nodeValue.setValue(Math.tan(compileDouble(node)));
    return nodeValue;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.jsbml.util.compilers.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode )
   */
  public final ASTNodeValue tanh(ASTNode node) throws SBMLException {
    nodeValue.setValue(Math.tanh(compileDouble(node)));
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
        value *= compileDouble(nodes.get(i));
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
    nodeValue.setValue(-compileDouble(node));
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
      if (compileBoolean(nodes.get(i))) {
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
  
  /**
   * Compiles an ASTNode to a double value.
   * @param the ASTNode
   * @return the interpreted double value
   * @throws SBMLException
   */
  public double compileDouble(ASTNode node) throws SBMLException {
    double value = Double.NaN;
    switch (node.getType()) {
      /*
       * Numbers
       */
      case REAL:
        double real = node.getReal();
        if (Double.isInfinite(real)) {
          value = (real > 0d) ? Double.POSITIVE_INFINITY
              : Double.NEGATIVE_INFINITY;
        } else {
          value = compileHelp(real, node.getUnits());
        }
        break;
      case INTEGER:
        value = compileHelp(node.getInteger(), node.getUnits());
        break;
      /*
       * Operators
       */
      case POWER:
        value = powHelp(node.getLeftChild(), node.getRightChild());
        break;
      case PLUS:
        value = plusHelp(node.getChildren());
        break;
      case MINUS:
        if (node.getChildCount() == 1) {
          value = uMinusHelp(node.getLeftChild());
        } else {
          value = minusHelp(node.getChildren());
        }
        break;
      case TIMES:
        value = timesHelp(node.getChildren());
        break;
      case DIVIDE:
        if (node.getChildCount() != 2) { throw new SBMLException(
          String
              .format(
                "Fractions must have one numerator and one denominator, here %s elements are given.",
                node.getChildCount())); }
        value = fracHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RATIONAL:
        value = fracHelp(node.getNumerator(), node.getDenominator());
        break;
      case NAME_TIME:
        value = symbolTimeHelp(node.getName());
        break;
      case FUNCTION_DELAY:
        value = delayHelp(node.getName(), node.getLeftChild(),
          node.getRightChild(), node.getUnits());
        break;
      
      /*
       * Names of identifiers: parameters, functions, species etc.
       */
      case NAME:
        variable=node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            value = functionDoubleHelp((FunctionDefinition) variable,
              node.getChildren());
          } else {
            value = compileDoubleHelp(variable);
          }
        } else {
          value = compileDoubleHelp(node.getName());
        }
        break;
      /*
       * Type: pi, e, true, false, Avogadro
       */
      case CONSTANT_PI:
        value = Math.PI;
        break;
      case CONSTANT_E:
        value = Math.E;
        break;
      
      case NAME_AVOGADRO:
        value = Maths.AVOGADRO_L3V1;
        break;
      case REAL_E:
        value = compileHelp(node.getMantissa(), node.getExponent(),
          node.isSetUnits() ? node.getUnits() : null);
        break;
      /*
       * Basic Functions
       */
      case FUNCTION_LOG:
        if (node.getChildCount() == 2) {
          value = logHelp(node.getLeftChild(), node.getRightChild());
        } else {
          value = logHelp(node.getRightChild());
        }
        break;
      case FUNCTION_ABS:
        value = absHelp(node.getRightChild());
        break;
      case FUNCTION_ARCCOS:
        value = arccosHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCCOSH:
        value = arccoshHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCCOT:
        value = arccotHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCCOTH:
        value = arccothHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCCSC:
        value = arccscHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCCSCH:
        value = arccschHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCSEC:
        value = arcsecHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCSECH:
        value = arcsechHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCSIN:
        value = arcsinHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCSINH:
        value = arcsinhHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCTAN:
        value = arctanHelp(node.getLeftChild());
        break;
      case FUNCTION_ARCTANH:
        value = arctanhHelp(node.getLeftChild());
        break;
      case FUNCTION_CEILING:
        value = ceilingHelp(node.getLeftChild());
        break;
      case FUNCTION_COS:
        value = cosHelp(node.getLeftChild());
        break;
      case FUNCTION_COSH:
        value = coshHelp(node.getLeftChild());
        break;
      case FUNCTION_COT:
        value = cotHelp(node.getLeftChild());
        break;
      case FUNCTION_COTH:
        value = cothHelp(node.getLeftChild());
        break;
      case FUNCTION_CSC:
        value = cscHelp(node.getLeftChild());
        break;
      case FUNCTION_CSCH:
        value = cschHelp(node.getLeftChild());
        break;
      case FUNCTION_EXP:
        value = expHelp(node.getLeftChild());
        break;
      case FUNCTION_FACTORIAL:
        value = factorialHelp(node.getLeftChild());
        break;
      case FUNCTION_FLOOR:
        value = floorHelp(node.getLeftChild());
        break;
      case FUNCTION_LN:
        value = lnHelp(node.getLeftChild());
        break;
      case FUNCTION_POWER:
        value = powHelp(node.getLeftChild(), node.getRightChild());
        break;
      case FUNCTION_ROOT:
        ASTNode left = node.getLeftChild();
        if (node.getChildCount() == 2) {
          if (left.isInteger()) {
            int leftValue = left.getInteger();
            if (leftValue == 2) {
              value = sqrtHelp(node.getRightChild());
            } else {
              value = rootHelp(leftValue, node.getRightChild());
            }
          } else if (left.isReal()) {
            double leftValue = left.getReal();
            if (leftValue == 2d) {
              value = sqrtHelp(node.getRightChild());
            } else {
              value = rootHelp(leftValue, node.getRightChild());
            }
          } else {
            value = rootHelp(left, node.getRightChild());
          }
        } else if (node.getChildCount() == 1) {
          value = sqrtHelp(node.getRightChild());
        } else {
          value = rootHelp(left, node.getRightChild());
        }
        break;
      case FUNCTION_SEC:
        value = secHelp(node.getLeftChild());
        break;
      case FUNCTION_SECH:
        value = sechHelp(node.getLeftChild());
        break;
      case FUNCTION_SIN:
        value = sinHelp(node.getLeftChild());
        break;
      case FUNCTION_SINH:
        value = sinhHelp(node.getLeftChild());
        break;
      case FUNCTION_TAN:
        value = tanHelp(node.getLeftChild());
        break;
      case FUNCTION_TANH:
        value = tanhHelp(node.getLeftChild());
        break;
      case FUNCTION: {
        variable=node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            value = functionDoubleHelp((FunctionDefinition) variable,
              node.getChildren());
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
          value = functionDoubleHelp(node.getName(), node.getChildren());
        }
        break;
      }
      case FUNCTION_PIECEWISE:
        value = piecewiseHelp(node.getChildren());
        break;
      case LAMBDA:
        value = lambdaDoubleHelp(node.getChildren());
        break;
      default:
        // TODO throw new
        break;
    }
    return value;
    
  }
  
  private double compileDoubleHelp(String name) {
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
  
  private double compileDoubleHelp(CallableSBase nsb) throws SBMLException {
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
        // return new ASTNodeValue(Y[symbolIndex], this);
        
        return (valueHolder.getCurrentSpeciesValue(id) * compartmentValue);
      } else {
        return valueHolder.getCurrentSpeciesValue(id);
        // return new ASTNodeValue(Y[symbolIndex]
        // / getCompartmentValueOf(nsb.getId()), this);
      }
    }
    else if ((nsb instanceof Compartment) || (nsb instanceof Parameter)) {
      String id=nsb.getId();
      return valueHolder.getCurrentValueOf(id);
    } 
    
//    double value=valueHolder.getCurrentValueOf(nsb.getId());
//    if(value!=Double.NaN) {
//      return value;
//    }
    else if (nsb instanceof LocalParameter) {
      LocalParameter p = (LocalParameter) nsb;
      // parent: list of parameter; parent of parent: kinetic law
      /*
      SBase parent = p.getParentSBMLObject().getParentSBMLObject();
      if (parent instanceof KineticLaw) {
        ListOf<LocalParameter> params = ((KineticLaw) parent)
            .getListOfLocalParameters();
        for (int i = 0; i < params.size(); i++) {
          if (id == params.get(i).getId()) { double value=params.get(i)
              .getValue(); 
            return value;
          }
        }
        */
      
        return p.getValue();
      
      
    } else if (nsb instanceof Reaction) {
      Reaction r = (Reaction) nsb;
      if (r.isSetKineticLaw()) { return compileDouble(r.getKineticLaw()
          .getMath()); }
    }
    return Double.NaN;
  }
  
  private boolean compileBooleanHelp(CallableSBase nsb) throws SBMLException {
    if (nsb instanceof FunctionDefinition) { return functionBooleanHelp(
      (FunctionDefinition) nsb, new LinkedList<ASTNode>()); }
    return false;
  }
  
  private double functionDoubleHelp(FunctionDefinition function,
    List<ASTNode> arguments) throws SBMLException {
    ASTNode lambda = function.getMath();
    Hashtable<String, Double> argValues = new Hashtable<String, Double>();
    for (int i = 0; i < arguments.size(); i++) {
      argValues.put(compileString(lambda.getChild(i)),
        compileDouble(arguments.get(i)));
    }
    setFuncArgs(argValues);
    double value = compileDouble(lambda.getRightChild());
    clearFuncArgs();
    return value;
  }
  
  private String compileString(ASTNode child) {
    if (child.isName()) {
      return child.getName();
    } else {
      return child.toString();
    }
  }
  
  private double lambdaDoubleHelp(List<ASTNode> nodes) throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = compileDouble(nodes.get(i));
    }
    // TODO: what happens with d?
    return compileDouble(nodes.get(nodes.size() - 1));
  }
  
  private boolean lambdaBooleanHelp(List<ASTNode> nodes) throws SBMLException {
    double d[] = new double[Math.max(0, nodes.size() - 1)];
    for (int i = 0; i < nodes.size() - 1; i++) {
      d[i++] = compileDouble(nodes.get(i));
    }
    // TODO: what happens with d?
    return compileBoolean(nodes.get(nodes.size() - 1));
    
  }
  
  private double piecewiseHelp(List<ASTNode> nodes) throws SBMLException {
    int i;
    for (i = 1; i < nodes.size() - 1; i += 2) {
      if (compileBoolean(nodes.get(i))) { return compileDouble(nodes.get(i - 1)); }
    }
    return compileDouble(nodes.get(i - 1));
    
  }
  
  private double logHelp(ASTNode node) throws SBMLException {
    return Math.log(compileDouble(node));
  }
  
  private double logHelp(ASTNode left, ASTNode right) throws SBMLException {
    return Maths.log(compileDouble(left), compileDouble(right));
  }
  
  private double functionDoubleHelp(String functionDefinitionName,
    List<ASTNode> args) throws SBMLException {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return Double.NaN;
  }
  
  private double tanhHelp(ASTNode node) throws SBMLException {
    return Math.tanh(compileDouble(node));
  }
  
  private double tanHelp(ASTNode node) throws SBMLException {
    return Math.tan(compileDouble(node));
  }
  
  private double sinhHelp(ASTNode node) throws SBMLException {
    return Math.sinh(compileDouble(node));
  }
  
  private double sinHelp(ASTNode node) throws SBMLException {
    return Math.sin(compileDouble(node));
  }
  
  private double sechHelp(ASTNode node) throws SBMLException {
    return Maths.sech(compileDouble(node));
  }
  
  private double secHelp(ASTNode node) throws SBMLException {
    return Maths.sec(compileDouble(node));
  }
  
  private double rootHelp(ASTNode rootExponent, ASTNode radiant)
    throws SBMLException {
    return rootHelp(compileDouble(rootExponent), radiant);
  }
  
  private double rootHelp(double rootExponent, ASTNode radiant)
    throws SBMLException {
    return Maths.root(compileDouble(radiant), rootExponent);
  }
  
  private double lnHelp(ASTNode node) throws SBMLException {
    return Maths.ln(compileDouble(node));
  }
  
  private double floorHelp(ASTNode node) throws SBMLException {
    return Math.floor(compileDouble(node));
  }
  
  private double factorialHelp(ASTNode node) throws SBMLException {
    return Maths.factorial((int) Math.round(compileDouble(node)));
  }
  
  private double expHelp(ASTNode node) throws SBMLException {
    return Math.exp(compileDouble(node));
  }
  
  private double cschHelp(ASTNode node) throws SBMLException {
    return Maths.csch(compileDouble(node));
  }
  
  private double cscHelp(ASTNode node) throws SBMLException {
    return Maths.csc(compileDouble(node));
  }
  
  private double cothHelp(ASTNode node) throws SBMLException {
    return Maths.coth(compileDouble(node));
  }
  
  private double cotHelp(ASTNode node) throws SBMLException {
    return Maths.cot(compileDouble(node));
  }
  
  private double coshHelp(ASTNode node) throws SBMLException {
    return Math.cosh(compileDouble(node));
  }
  
  private double cosHelp(ASTNode node) throws SBMLException {
    return Math.cos(compileDouble(node));
  }
  
  private double ceilingHelp(ASTNode node) throws SBMLException {
    return Math.ceil(compileDouble(node));
  }
  
  private double arctanhHelp(ASTNode node) throws SBMLException {
    return Maths.arctanh(compileDouble(node));
  }
  
  /**
   * Compiles an ASTNode to a boolean value.
   * @param the ASTNode
   * @return the interpreted boolean value
   * @throws SBMLException
   */
  public boolean compileBoolean(ASTNode node) throws SBMLException {
    boolean value = false;
    
    switch (node.getType()) {
      /*
       * Logical and relational functions
       */
      case LOGICAL_AND:
        value = andHelp(node.getChildren());
        break;
      case LOGICAL_XOR:
        value = xorHelp(node.getChildren());
        break;
      case LOGICAL_OR:
        value = orHelp(node.getChildren());
        break;
      case LOGICAL_NOT:
        value = notHelp(node.getLeftChild());
        break;
      case RELATIONAL_EQ:
        value = eqHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RELATIONAL_GEQ:
        value = geqHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RELATIONAL_GT:
        value = gtHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RELATIONAL_NEQ:
        value = neqHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RELATIONAL_LEQ:
        value = leqHelp(node.getLeftChild(), node.getRightChild());
        break;
      case RELATIONAL_LT:
        value = ltHelp(node.getLeftChild(), node.getRightChild());
        break;
      case CONSTANT_TRUE:
        value = true;
        break;
      case CONSTANT_FALSE:
        value = false;
        break;
      case NAME:
        variable=node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            value = functionBooleanHelp((FunctionDefinition) variable,
              node.getChildren());
          } else {
            value = compileBooleanHelp(variable);
          }
        }
        break;
      
      case FUNCTION: {
        variable=node.getVariable();
        
        if (variable != null) {
          if (variable instanceof FunctionDefinition) {
            value = functionBooleanHelp((FunctionDefinition) variable,
              node.getChildren());
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
          value = functionBooleanHelp(node.getName(), node.getChildren());
        }
        break;
      }
      case LAMBDA:
        value = lambdaBooleanHelp(node.getChildren());
        break;
    }
    return value;
  }
  
  private boolean functionBooleanHelp(String name, List<ASTNode> children) {
    // can not compile a function without an ASTNode representing its lambda
    // expression
    
    return false;
  }
  
  private boolean functionBooleanHelp(FunctionDefinition function,
    List<ASTNode> arguments) throws SBMLException {
    ASTNode lambda = function.getMath();
    
    Hashtable<String, Double> argValues = new Hashtable<String, Double>();
    for (int i = 0; i < arguments.size(); i++) {
      argValues.put(compileString(lambda.getChild(i)),
        compileDouble(arguments.get(i)));
    }
    setFuncArgs(argValues);
    boolean value = compileBoolean(lambda.getRightChild());
    clearFuncArgs();
    return value;
  }
  
  private boolean ltHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) < compileDouble(right));
  }
  
  private boolean leqHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) <= compileDouble(right));
  }
  
  private boolean neqHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) != compileDouble(right));
  }
  
  private boolean gtHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) > compileDouble(right));
  }
  
  private boolean geqHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) >= compileDouble(right));
  }
  
  private boolean eqHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) == compileDouble(right));
  }
  
  private boolean notHelp(ASTNode node) throws SBMLException {
    return compileBoolean(node) ? false : true;
  }
  
  private boolean orHelp(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (compileBoolean(node)) { return true; }
    }
    return false;
  }
  
  private boolean xorHelp(List<ASTNode> nodes) throws SBMLException {
    boolean value = false;
    for (int i = 0; i < nodes.size(); i++) {
      if (compileBoolean(nodes.get(i))) {
        if (value) {
          return false;
        } else {
          value = true;
        }
      }
    }
    return value;
  }
  
  private boolean andHelp(List<ASTNode> nodes) throws SBMLException {
    for (ASTNode node : nodes) {
      if (!compileBoolean(node)) { return false; }
    }
    return true;
  }
  
  private double arctanHelp(ASTNode node) throws SBMLException {
    return Math.atan(compileDouble(node));
  }
  
  private double arcsinhHelp(ASTNode node) throws SBMLException {
    return Maths.arcsinh(compileDouble(node));
  }
  
  private double arcsinHelp(ASTNode node) throws SBMLException {
    return Math.asin(compileDouble(node));
  }
  
  private double arcsechHelp(ASTNode value) throws SBMLException {
    return Maths.arcsech(compileDouble(value));
  }
  
  private double arcsecHelp(ASTNode value) throws SBMLException {
    return Maths.arcsec(compileDouble(value));
  }
  
  private double arccschHelp(ASTNode value) throws SBMLException {
    return Maths.arccsch(compileDouble(value));
  }
  
  private double arccscHelp(ASTNode value) throws SBMLException {
    return Maths.arccsc(compileDouble(value));
  }
  
  private double arccothHelp(ASTNode value) throws SBMLException {
    return Maths.arccoth(compileDouble(value));
  }
  
  private double arccotHelp(ASTNode value) throws SBMLException {
    return Maths.arccot(compileDouble(value));
  }
  
  private double arccoshHelp(ASTNode value) throws SBMLException {
    return Maths.arccosh(compileDouble(value));
  }
  
  private double arccosHelp(ASTNode value) throws SBMLException {
    return Math.acos(compileDouble(value));
  }
  
  private double absHelp(ASTNode node) throws SBMLException {
    return Math.abs(compileDouble(node));
  }
  
  private double compileHelp(double mantissa, int exponent, String units) {
    return (mantissa * Math.pow(10, exponent));
  }
  
  private double delayHelp(String name, ASTNode leftChild, ASTNode rightChild,
    String units) {
    // TODO Auto-generated method stub
    return 0;
  }
  
  private double symbolTimeHelp(String name) {
    return (valueHolder.getCurrentTime());
  }
  
  private double fracHelp(int numerator, int denominator) {
    return ((double) numerator / denominator);
  }
  
  private double fracHelp(ASTNode left, ASTNode right) throws SBMLException {
    return (compileDouble(left) / compileDouble(right));
  }
  
  private double timesHelp(List<ASTNode> nodes) throws SBMLException {
    int size = nodes.size();
    if (size == 0) {
      return (0d);
    } else {
      double value = 1d;
      
      for (int i = 0; i != size; i++) {
        value *= compileDouble(nodes.get(i));
      }
      return value;
    }
  }
  
  private double minusHelp(List<ASTNode> nodes) throws SBMLException {
    int size = nodes.size();
    
    double value = 0d;
    if (size > 0) {
      value = compileDouble(nodes.get(0));
    }
    for (int i = 1; i < size; i++) {
      value -= compileDouble(nodes.get(i));
    }
    return value;
  }
  
  private double plusHelp(List<ASTNode> nodes) throws SBMLException {
    int size = nodes.size();
    double value = 0d;
    for (int i = 0; i != size; i++) {
        value += compileDouble(nodes.get(i));
    }
      return value;
    }
  
  private double powHelp(ASTNode left, ASTNode right) throws SBMLException {
    double l = compileDouble(left);
    double r = compileDouble(right);
    double result= Math.pow(l,r);
    return result;
  }
  
  private double compileHelp(double value, String units) {
    // TODO: units!
    return value;
  }
  
  private double sqrtHelp(ASTNode node) throws SBMLException {
    return Math.sqrt(compileDouble(node));
  }
  
  private double uMinusHelp(ASTNode node) throws SBMLException {
    return (-compileDouble(node));
  }
}
