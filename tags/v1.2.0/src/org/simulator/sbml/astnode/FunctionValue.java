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
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.SBMLException;
import org.simulator.sbml.SBMLinterpreter;

/**
 * This class computes and stores values of {@link ASTNode}s that refer to the
 * application of a {@link FunctionDefinition}.
 * 
 * @author Roland Keller
 * @version $Rev$
 */
public class FunctionValue extends ASTNodeValue {
	/**
	 * The value of the evaluation block of the function stored in an ASTNodeObject.
	 */
	protected ASTNodeValue evaluationBlock;
	
	/**
	 * The variables of the function
	 */
	protected List<String> variables;
  
	/**
	 * The current values of the function arguments.
	 */
	protected double[] argumentValues;

	/**
	 * A map for storing the indexes of the arguments in the array argumentValues.
	 */
	protected Map<String,Integer> indexMap;

	/**
	 * The math of the function definition.
	 */
	protected ASTNode math;
  
	/**
	 * 
	 * @param interpreter
	 *            the interpreter
	 * @param node
	 *            the corresponding ASTNode
	 * @param variableNodes
	 *            the variables of the function as ASTNodes
	 */
	public FunctionValue(ASTNodeInterpreter interpreter,
			ASTNode node, List<ASTNode> variableNodes) {
		super(interpreter, node);
		CallableSBase variable = node.getVariable();
		if ((variable != null)) {
			if (variable instanceof FunctionDefinition) {
				this.variables=new ArrayList<String>(variableNodes.size());
				this.indexMap=new HashMap<String,Integer>();
				int index=0;
				for(ASTNode argument:variableNodes) {
					String argumentName=interpreter.compileString(argument);
					variables.add(argumentName);
					indexMap.put(argumentName, index);
					index++;
				}
				this.argumentValues=new double[variables.size()];
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
			logger.warning("ASTNode of type FUNCTION but the variable is null !! ("
					+ node.getName() + ", " + node.getParentSBMLObject() + "). "
					+ "Check that your object is linked to a Model.");
		}


	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
	 */
	@Override
	protected void computeDoubleValue() {
		if (math != null) {
			doubleValue = interpreter.functionDouble(evaluationBlock, variables, children, numChildren, argumentValues, time);
		} else {
			doubleValue = Double.NaN;
		}
	}

	/* (non-Javadoc)
	 * @see org.simulator.sbml.astnode.ASTNodeValue#computeBooleanValue()
	 */
	@Override
	protected void computeBooleanValue() {
		if (math != null) {
			booleanValue = interpreter.functionBoolean(evaluationBlock, variables, children, argumentValues, time);
		} else {
			booleanValue = false;
		}
	}

	/**
	 * Sets the math and evaluation block of the function definition.
	 * @param math
	 */
	public void setMath(ASTNode math) {
		this.math = math;
		this.evaluationBlock=(ASTNodeValue) math.getRightChild().getUserObject(SBMLinterpreter.TEMP_VALUE);
	}

	/**
	 * Returns the values of the arguments.
	 * @return argumentValues
	 */
	public double[] getArgumentValues() {
		return argumentValues;
	}

	/**
	 * Returns the index of a specific argument.
	 * @param argumentName
	 * @return index
	 */
	public int getIndex(String argumentName) {
		return indexMap.get(argumentName);
	}

}
