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
package org.simulator.sbml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBaseWithDerivedUnit;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.ASTNode.Type;

/**
 * This class converts the algebraic rules of a model to assignment rules based
 * on the given matching
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 0.9
 */
public class AlgebraicRuleConverter {

	/**
	 * This class represents a subterm in the equation of algebraic rules
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class EquationObject {
		/**
		 * Contains the mathematical expression of the subterm
		 */
		private ASTNode node;
		/**
		 * Indicates whether the subterm has to be negativ or not
		 */
		private boolean isNegative;

		/**
		 * 
		 * @param node
		 * @param isNegative
		 */
		public EquationObject(ASTNode node, boolean isNegative) {
			this.node = node;
			this.isNegative = isNegative;
		}

		/**
		 * Returns the subterm
		 * 
		 * @return 
		 */
		public ASTNode getNode() {
			return node;
		}

		/**
		 * Returns a boolean whether the subterm has to be negative or not
		 * 
		 * @return
		 */
		public boolean isNegative() {
			return isNegative;
		}

	}

	/**
	 * {@link Map} representing the current matching with value of the left node
	 * -> value of the right node
	 */
	private Map<SBase, SBase> matching;
	/**
	 * The given SBML model
	 */
	private Model model;
	/**
	 * ASTNodes for the node of the variable an algebraic refers to and its
	 * parent
	 */
	private ASTNode variableNodeParent, variableNode;
	/**
	 * A boolean that states if the variable of an algebraic rule is linked
	 * additive
	 */
	private boolean additive = true;
	/**
	 * A boolean that states if the variable of an algebraic rule can remain on
	 * its side
	 */
	private boolean remainOnSide = true;
	/**
	 * The depth of nesting of the current analysed MathML expression
	 */
	private int nestingDepth;
	/**
	 * The depth of nesting of the current analysed MathML expression
	 */
	private ArrayList<ArrayList<EquationObject>> equationObjects;
	/**
	 * The container that holds the current rule.
	 */
	private MathContainer pso;

	/**
	 * Creates a new AlgebraicRuleConverter for the given matching and model
	 * 
	 * @param model
	 */
	public AlgebraicRuleConverter(Map<SBase, SBase> map, Model model) {
		this.matching = map;
		this.model = model;
	}

	/**
	 * Creates an equation for the current algebraic rule on the basis of the
	 * evaluation and the sorted EquationObjects
	 * 
	 * @return
	 */
	private ASTNode buildEquation() {
		//System.out.println("rebuilding equation...");
		//System.out.println("additive: " + additive);
		//System.out.println("remainOnSide: " + remainOnSide);
		EquationObject eo;
		ArrayList<EquationObject> addition = equationObjects.get(0);
		ArrayList<EquationObject> multiplication = equationObjects.get(1);
		ASTNode add = new ASTNode(Type.PLUS, pso);
		ASTNode multiply = new ASTNode(Type.TIMES, pso);
		ASTNode divide = new ASTNode(Type.DIVIDE, pso);
		ASTNode node = null;

		if (additive) {
			if (!remainOnSide) {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					} else {
						add.addChild(eo.getNode());
					}

				}
				node = add;
				if (multiplication.size() > 0) {
					for (int i = 0; i < multiplication.size(); i++) {
						multiply.addChild(multiplication.get(i).getNode());
					}
					multiply.addChild(add);

					node = multiply;
				}

			} else {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						add.addChild(eo.getNode());
					} else {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					}
				}

				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {

						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(add);
						divide.addChild(multiply);
					} else {
						divide.addChild(add);
						divide.addChild(multiplication.get(0).getNode());
					}

					node = divide;
				}

			}
		} else {
			if (!remainOnSide) {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					} else {
						add.addChild(eo.getNode());
					}

				}
				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {

						divide.addChild(multiplication.get(0).getNode());
						divide.addChild(add);
					} else {

						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(multiply);
						divide.addChild(add);

					}

					node = divide;
				}
			}

			else {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						add.addChild(eo.getNode());
					} else {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					}
				}

				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {
						divide.addChild(add);
						divide.addChild(multiplication.get(0).getNode());

					} else {
						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(add);
						divide.addChild(multiply);

					}

					node = divide;
				}
			}
		}

		return node;

	}

	/**
	 * Creates an assignment rule out of the given ASTNode and the Id of its
	 * algebraic rule
	 * 
	 * @param node
	 * @param rule
	 * @return
	 */
	private AssignmentRule createAssignmentRule(ASTNode node, Rule rule) {
		Variable variable;
		AssignmentRule as = null;

		// Search for the corresponding variable in the matching
		variable = (Variable) matching.get(rule);

		// Evaluate and reorganize the equation of the given ASTNode
		if (variable != null) {
			this.variableNodeParent = null;
			this.variableNode = null;
			try {
				//System.out.println("before: " + node.toFormula());
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			//System.out.println("variable: " + variable);
			as = new AssignmentRule();
			as.setVariable(variable);
			// Set pointer to algebraic rule's variable node and to its parent
			setNodeWithVariable(node, variable);
			nestingDepth = 0;
			evaluateEquation(variableNodeParent);
			//System.out.println("nesting depth: " + nestingDepth);

			equationObjects = new ArrayList<ArrayList<EquationObject>>();
			equationObjects.add(new ArrayList<EquationObject>());
			equationObjects.add(new ArrayList<EquationObject>());

			deleteVariable();
			sortEquationObjects(node, false, false, false, nestingDepth);
			as.setMath(buildEquation());

			try {
				//System.out.println("after: " + as.getMath().toFormula());
			} catch (SBMLException e) {
				e.printStackTrace();
			}
		}

		return as;
	}

	/**
	 * Before sorting the equation of the algebraic rule, the variable of the
	 * rule has to be deleted.
	 */
	private void deleteVariable() {
		int index;
		// Node with variable has 2 children
		if ((variableNodeParent.getChildCount() == 2)
				&& (variableNodeParent.getType() == Type.TIMES)) {
			// Variable has a negativ sign / other child is -1
			if (variableNodeParent.getLeftChild().isMinusOne() || (variableNodeParent.getLeftChild().isUMinus() && variableNodeParent.getLeftChild().getLeftChild().isOne())
					|| variableNodeParent.getRightChild().isMinusOne() || (variableNodeParent.getRightChild().isUMinus() && variableNodeParent.getRightChild().getLeftChild().isOne())) {

				index = variableNodeParent.getParent().getIndex(
						variableNodeParent);
				variableNodeParent.getParent().removeChild(index);
			}
			// Other child is not -1
			else {
				index = variableNodeParent.getIndex(variableNode);
				variableNodeParent.removeChild(index);
			}

		}
		// Node with variable has multiple childs
		else {
			index = variableNodeParent.getIndex(variableNode);
			variableNodeParent.removeChild(index);
		}
	}

	/**
	 * Checks if the variable of the algebraic equation has to be moved to the
	 * other side of the equation or not and if its connection to the rest of
	 * the equation is additive or multiplicative.
	 * 
	 * 
	 * @param node
	 * @return
	 */
	private void evaluateEquation(ASTNode node) {
		if (node != null) {
			if (node.getType() == Type.TIMES) {
				if (node.getChildCount() == 2) {
				  if (node.getLeftChild().isMinusOne() || (node.getLeftChild().isUMinus() && node.getLeftChild().getLeftChild().isOne())
		          || node.getRightChild().isMinusOne() || (node.getRightChild().isUMinus() && node.getRightChild().getLeftChild().isOne())) {
						remainOnSide = false;
					} else {
						additive = false;
						nestingDepth++;
					}

				} else {
					additive = false;
					nestingDepth++;
				}

			} else if (node.getType() == Type.DIVIDE) {
				additive = false;
				remainOnSide = false;
				nestingDepth++;
			}

			evaluateEquation((ASTNode) node.getParent());
		}

	}

	/**
	 * Creates a list an assignment rule for every algebraic rule in the given
	 * model
	 * 
	 * @return
	 */
	public List<AssignmentRule> getAssignmentRules() {
		ArrayList<AssignmentRule> assignmentRules = new ArrayList<AssignmentRule>();
		AssignmentRule as;
		if (matching != null) {
			// TODO: What is happening here?
//			for (Map.Entry<SBase, SBase> entry : matching.entrySet()) {
//				//System.out.println(entry.getKey() + " -> " + entry.getValue());
//			}
		} else {
			System.out.println("no matching found");
		}

		
		//determine matchings for algebraic rules
		
		
		
		// create for every algebraic rule an adequate assignment rule
		for (int i = 0; i < model.getRuleCount(); i++) {
			Rule r = model.getRule(i);
			if (r instanceof AlgebraicRule) {
				AlgebraicRule ar = (AlgebraicRule) r;

				ASTNode node = ar.getMath().clone();
				
				// substitute function definitions
				if (model.getFunctionDefinitionCount() > 0) {
					node = substituteFunctions(node, 0);
				}
				pso = node.getParentSBMLObject();
				as = createAssignmentRule(node, ar);

				// when assignment rule created add to the list
				if (as != null) {
					assignmentRules.add(as);
					as = null;
				}
			}
		}

		return assignmentRules;
	}

	/**
	 * Replaces the names of given ASTNode's childern with the value stored in
	 * the given HashMaps if there is an entry in any of the HashMaps
	 * 
	 * 
	 * @param node
	 * @param varibales
	 */
	private void replaceNames(ASTNode node, Map<String, String> varibales,
			Map<String, Double> numberHash, Map<String, ASTNode> nodeHash) {

		if (node.isString()) {
			if (varibales.get(node.getName()) != null) {
				node.setName(varibales.get(node.getName()));
			} else if (numberHash.get(node.getName()) != null) {
				node.setValue(numberHash.get(node.getName()));
      } else if ((nodeHash.get(node.getName()) != null) && !node.isRoot()) {
        ASTNode parent = (ASTNode) node.getParent();
        parent.replaceChild(parent.getIndex(node), nodeHash.get(node.getName()));
      }
		}
		// proceed with the children
		for (int i = 0; i < node.getChildCount(); i++) {
			replaceNames(node.getChild(i), varibales, numberHash, nodeHash);
		}
	}

	/**
	 * Searches in the given ASTNode for a node with the same name as the given
	 * String. Afterwards the variables variableNode and variable are set.
	 * 
	 * @param node
	 * @param variable
	 */
	private void setNodeWithVariable(ASTNode node, Variable variable) {
		Enumeration<?> nodes = node.children();
		ASTNode subnode;

		while (nodes.hasMoreElements()) {
			subnode = (ASTNode) nodes.nextElement();
			if (subnode.isString()) {
				if (subnode.getName() == variable.getId()) {
					variableNodeParent = node;
					variableNode = subnode;
				}
			} else {
				setNodeWithVariable((ASTNode) subnode, variable);
			}
		}

	}

	/**
	 * Creates EquationObjects for subterm of the rules equation and sorts them
	 * into ArrayLists
	 * 
	 * @param node
	 * @param plus
	 * @param times
	 * @param divide
	 */
	private void sortEquationObjects(ASTNode node, boolean plus, boolean times,
			boolean divide, int depth) {
	  
		// Reached an operator
		if ((depth>=0) && (node.isOperator())) {
			if (node.getType() == Type.PLUS) {
				for (int i = 0; i < node.getChildCount(); i++) {
				  sortEquationObjects(node.getChild(i), true, false, false, depth-1);
				}
			} else if (node.getType() == Type.MINUS) {

				for (int i = 0; i < node.getChildCount(); i++) {
					sortEquationObjects(node.getChild(i), true, true, false, depth-1);
				}
			} else if (node.getType() == Type.TIMES) {
				if (node.getChildCount() == 2) {
				  if (((node.getLeftChild().isMinusOne()) || ((node.getLeftChild().isUMinus()) && (node.getLeftChild().getLeftChild().isOne())))
              && (! (node.getRightChild().isMinusOne() || ((node.getRightChild().isUMinus()) && (node.getRightChild().getLeftChild().isOne()))))) {
				    sortEquationObjects(node.getRightChild(), true, true,
								false, depth-1);
					}

					else if (!((node.getLeftChild().isMinusOne()) || ((node.getLeftChild().isUMinus()) && (node.getLeftChild().getLeftChild().isOne())))
              && ((node.getRightChild().isMinusOne()) || ((node.getRightChild().isUMinus()) && (node.getRightChild().getLeftChild().isOne())))) {
						sortEquationObjects(node.getLeftChild(), true, true,
								false, depth-1);
					} else {
						equationObjects.get(1).add(
								new EquationObject(node.clone(), false));
					}

				} else {
					equationObjects.get(1).add(
							new EquationObject(node.clone(), false));
				}

			}

		}
		// Reached a variable
		else {
			if (plus && times) {
				equationObjects.get(0).add(
						new EquationObject(node.clone(), true));
			} else if (plus) {
				equationObjects.get(0).add(
						new EquationObject(node.clone(), false));
			}
		}

	}

	/**
	 * Replaces all functions in the given ASTNode with the function definition
	 * and its arguments
	 * 
	 * @param node
	 * @param indexParent
	 */
	private ASTNode substituteFunctions(ASTNode node, int indexParent) {
		NamedSBaseWithDerivedUnit variable;
		// Check if node is a function
		if (node.isString()) {
			FunctionDefinition fd = model.getFunctionDefinition(node.getName());
			// Node represents a function definiton in the model
			if (fd != null) {
				ASTNode function = fd.getMath();
				HashMap<String, String> nameHash = new HashMap<String, String>();
				HashMap<String, Double> numberHash = new HashMap<String, Double>();
				HashMap<String, ASTNode> nodeHash = new HashMap<String, ASTNode>();

				ASTNode parent;

				// Hash its variables to the parameter
				for (int i = 0; i < node.getChildCount(); i++) {
					variable = null; 
					if (node.getChild(i).isString()) {
						variable = node.getChild(i).getVariable();
						if(variable == null) {
							variable = model.findNamedSBaseWithDerivedUnit(node.getName());
						}
					}
					if (variable instanceof FunctionDefinition) {
						nodeHash.put(function.getChild(i).getName(), node
								.getChild(i).clone());
					} else if (node.getChild(i).isOperator()) {
						nodeHash.put(function.getChild(i).getName(), node
								.getChild(i).clone());
					} else if (node.getChild(i).isString()) {
						nameHash.put(function.getChild(i).getName(), node
								.getChild(i).getName());
					} else if (node.getChild(i).isNumber()) {
						if (node.getChild(i).isInteger()) {
							numberHash.put(function.getChild(i).getName(),
									(double) node.getChild(i).getInteger());
						} else {
							numberHash.put(function.getChild(i).getName(),
									(double) node.getChild(i).getReal());
						}
					}
				}

				parent = (ASTNode) node.getParent();
				// Function definition is child
				if (parent != null) {
					// System.out.println(parent.getType());

					// Replace the reference to a function definition with the
					// function definition itself
					parent.replaceChild(indexParent, function.getRightChild()
							.clone());
					// Substitute the variables with the parameter
					replaceNames(parent.getChild(indexParent), nameHash,
							numberHash, nodeHash);

					// Replaced arguments could contain additional functions, so
					// start at the parent again
					node = parent;

				}
				// Function definiton is root
				else {
					// Replace the reference to a function definition with the
					// function definiton itself
					node = function.getRightChild().clone();
					// Substitute the variables with the parameter
					replaceNames(node, nameHash, numberHash, nodeHash);
				}
			}
		}

		// Move on with its children
		for (int i = 0; i < node.getChildCount(); i++) {
			substituteFunctions(node.getChild(i), i);
		}

		return node;
	}

}
