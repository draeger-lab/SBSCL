/*
 * $Id:  ASTNodeInterpreter.java 15:53:40 draeger$
 * $URL: ASTNodeInterpreter.java $
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
 * @since 1.0
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

	/**
	 * 
	 * @param valueHolder
	 */
	public ASTNodeInterpreter(ValueHolder valueHolder) {
		this.valueHolder = valueHolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue abs(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.abs(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#and(java.util.List)
	 */
	public ASTNodeValue and(List<ASTNode> nodes) throws SBMLException {
		for (ASTNode node : nodes) {
			if (!node.compile(this).toBoolean()) {
				return getConstantFalse();
			}
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
	public ASTNodeValue arccos(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Math.acos(value.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccosh(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arccosh(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arccosh(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arccot(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arccot(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccoth(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arccoth(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arccoth(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arccsc(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arccsc(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsch(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arccsch(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arccsch(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arcsec(ASTNode value) throws SBMLException {
		return new ASTNodeValue(Maths.arcsec(value.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsech(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arcsech(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arcsech(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arcsin(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.asin(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsinh(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arcsinh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arcsinh(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arctan(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.atan(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctanh(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue arctanh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arctanh(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#ceiling(org.sbml.jsbml.
	 * ASTNode)
	 */
	public ASTNodeValue ceiling(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.ceil(node.compile(this).toDouble()), this);
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
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(org.sbml.jsbml.
	 * CallableSBase)
	 */
	public ASTNodeValue compile(CallableSBase nsb) throws SBMLException {
		if (nsb instanceof Species) {
			Species s = (Species) nsb;
			if (valueHolder.getCurrentCompartmentSize(nsb.getId()) == 0d) {
				return new ASTNodeValue(valueHolder.getCurrentSpeciesValue(nsb
						.getId()), this);
			}

			if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
				return new ASTNodeValue(
						valueHolder.getCurrentSpeciesValue(nsb.getId())
								/ valueHolder.getCurrentCompartmentValueOf(nsb
										.getId()), this);
			}

			if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits()) {
				// return new ASTNodeValue(Y[symbolIndex], this);

				return new ASTNodeValue(
						valueHolder.getCurrentSpeciesValue(nsb.getId())
								* valueHolder.getCurrentCompartmentValueOf(nsb
										.getId()), this);
			}

			return new ASTNodeValue(valueHolder.getCurrentSpeciesValue(nsb
					.getId()), this);
			// return new ASTNodeValue(Y[symbolIndex]
			// / getCompartmentValueOf(nsb.getId()), this);

		} else if (nsb instanceof SpeciesReference) {
			return new ASTNodeValue(valueHolder.getCurrentStoichiometry(nsb
					.getId()), this);
		}

		else if (nsb instanceof Compartment || nsb instanceof Parameter
				|| nsb instanceof LocalParameter) {
			if (nsb instanceof LocalParameter) {
				LocalParameter p = (LocalParameter) nsb;
				// parent: list of parameter; parent of parent: kinetic law
				SBase parent = p.getParentSBMLObject().getParentSBMLObject();
				if (parent instanceof KineticLaw) {
					ListOf<LocalParameter> params = ((KineticLaw) parent)
							.getListOfLocalParameters();
					for (int i = 0; i < params.size(); i++) {
						if (p.getId() == params.get(i).getId()) {
							return new ASTNodeValue(params.get(i).getValue(),
									this);
						}
					}
				}
			}

			return new ASTNodeValue(valueHolder.getCurrentValueOf(nsb.getId()),
					this);

		} else if (nsb instanceof FunctionDefinition) {
			return function((FunctionDefinition) nsb, new LinkedList<ASTNode>());
		} else if (nsb instanceof Reaction) {
			Reaction r = (Reaction) nsb;
			if (r.isSetKineticLaw()) {
				return r.getKineticLaw().getMath().compile(this);
			}
		}
		return new ASTNodeValue(Double.NaN, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(org.sbml.jsbml.
	 * Compartment)
	 */
	public ASTNodeValue compile(Compartment c) {
		return new ASTNodeValue(
				valueHolder.getCurrentCompartmentSize(c.getId()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(double, int,
	 * java.lang.String)
	 */
	public ASTNodeValue compile(double mantissa, int exponent, String units) {
		return new ASTNodeValue(mantissa * Math.pow(10, exponent), this);
	}

	public ASTNodeValue compile(double value, String units) {
		// TODO: units!
		return new ASTNodeValue(value, this);
	}

	public ASTNodeValue compile(int value, String units) {
		// TODO: units!
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#compile(java.lang.String)
	 */
	public ASTNodeValue compile(String name) {
		Double funcArg = getFuncArg(name);
		if (funcArg != null) {
			return new ASTNodeValue(funcArg, this);
		} else if (!Double.isNaN(valueHolder.getCurrentValueOf(name))) {
			return new ASTNodeValue(valueHolder.getCurrentValueOf(name), this);
		}
		return new ASTNodeValue(String.valueOf(name), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cos(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.cos(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue cosh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.cosh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cot(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.cot(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue coth(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.coth(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue csc(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.csc(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue csch(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.csch(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#delay(java.lang.String,
	 * org.sbml.jsbml.ASTNode, org.sbml.jsbml.ASTNode, java.lang.String)
	 */
	public ASTNodeValue delay(String delayName, ASTNode x, ASTNode delay,
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
	public ASTNodeValue eq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() == right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue exp(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.exp(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#factorial(org.sbml.jsbml
	 * .ASTNode)
	 */
	public ASTNodeValue factorial(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.factorial((int) Math.round(node.compile(
				this).toDouble())), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue floor(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.floor(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue frac(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble()
				/ right.compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#frac(int, int)
	 */
	public ASTNodeValue frac(int numerator, int denominator) {
		return new ASTNodeValue((double) numerator / (double) denominator, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#function(org.sbml.jsbml
	 * .FunctionDefinition, java.util.List)
	 */
	public ASTNodeValue function(FunctionDefinition function,
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
	public ASTNodeValue function(String functionDefinitionName,
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
	public ASTNodeValue geq(ASTNode nodeleft, ASTNode noderight)
			throws SBMLException {
		return new ASTNodeValue(nodeleft.compile(this).toDouble() >= noderight
				.compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantAvogadro(java
	 * .lang.String)
	 */
	public ASTNodeValue getConstantAvogadro(String name) {
		return new ASTNodeValue(Maths.AVOGADRO, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantE()
	 */
	public ASTNodeValue getConstantE() {
		return new ASTNodeValue(Math.E, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantFalse()
	 */
	public ASTNodeValue getConstantFalse() {
		return new ASTNodeValue(false, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantPi()
	 */
	public ASTNodeValue getConstantPi() {
		return new ASTNodeValue(Math.PI, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantTrue()
	 */
	public ASTNodeValue getConstantTrue() {
		return new ASTNodeValue(true, this);
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
	public ASTNodeValue getNegativeInfinity() {
		return new ASTNodeValue(Double.NEGATIVE_INFINITY, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getPositiveInfinity()
	 */
	public ASTNodeValue getPositiveInfinity() {
		return new ASTNodeValue(Double.POSITIVE_INFINITY, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#gt(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue gt(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() > right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#lambda(java.util.List)
	 */
	public ASTNodeValue lambda(List<ASTNode> nodes) throws SBMLException {
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
	public ASTNodeValue leq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() <= right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue ln(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.ln(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.log(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(Maths.log(left.compile(this).toDouble(), right
				.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#lt(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue lt(ASTNode nodeleft, ASTNode noderight)
			throws SBMLException {
		return new ASTNodeValue(nodeleft.compile(this).toDouble() < noderight
				.compile(this).toDouble(), this);
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
	public ASTNodeValue minus(List<ASTNode> nodes) throws SBMLException {
		double value = 0d;
		if (nodes.size() > 0) {
			value = nodes.get(0).compile(this).toDouble();
		}
		for (int i = 1; i < nodes.size(); i++) {
			value -= nodes.get(i).compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#neq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue neq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() != right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue not(ASTNode node) throws SBMLException {
		return node.compile(this).toBoolean() ? getConstantFalse()
				: getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#or(java.util.List)
	 */
	public ASTNodeValue or(List<ASTNode> nodes) throws SBMLException {
		for (ASTNode node : nodes) {
			if (node.compile(this).toBoolean()) {
				return getConstantTrue();
			}
		}
		return getConstantFalse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#piecewise(java.util.List)
	 */
	public ASTNodeValue piecewise(List<ASTNode> nodes) throws SBMLException {
		int i;
		for (i = 1; i < nodes.size() - 1; i += 2) {
			if (nodes.get(i).compile(this).toBoolean()) {
				return new ASTNodeValue(nodes.get(i - 1).compile(this)
						.toDouble(), this);
			}
		}
		return new ASTNodeValue(nodes.get(i - 1).compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#plus(java.util.List)
	 */
	public ASTNodeValue plus(List<ASTNode> nodes) throws SBMLException {
		double value = 0d;
		for (ASTNode node : nodes) {
			value += node.compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue pow(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(Math.pow(left.compile(this).toDouble(), right
				.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue root(ASTNode rootExponent, ASTNode radiant)
			throws SBMLException {
		return root(rootExponent.compile(this).toDouble(), radiant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(double,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue root(double rootExponent, ASTNode radiant)
			throws SBMLException {
		return new ASTNodeValue(Maths.root(radiant.compile(this).toDouble(),
				rootExponent), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sec(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.sec(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue sech(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.sech(node.compile(this).toDouble()), this);

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
	public ASTNodeValue sin(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sin(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue sinh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sinh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue sqrt(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sqrt(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#symbolTime(java.lang.String
	 * )
	 */
	public ASTNodeValue symbolTime(String timeSymbol) {
		return new ASTNodeValue(valueHolder.getCurrentTime(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue tan(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.tan(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue tanh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.tanh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#times(java.util.List)
	 */
	public ASTNodeValue times(List<ASTNode> nodes) throws SBMLException {
		if (nodes.size() == 0) {
			return new ASTNodeValue(0d, this);
		}
		double value = 1d;
		for (ASTNode node : nodes) {
			value *= node.compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	public String toString(ASTNode value) {
		return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#uMinus(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue uMinus(ASTNode node) throws SBMLException {
		return new ASTNodeValue(-node.compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#unknownValue()
	 */
	public ASTNodeValue unknownValue() throws SBMLException {
		return new ASTNodeValue(Double.NaN, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#xor(java.util.List)
	 */
	public ASTNodeValue xor(List<ASTNode> nodes) throws SBMLException {
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
		return new ASTNodeValue(value, this);
	}

}
