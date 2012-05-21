/*
 * $Id:  PlusValue.java 14:41:35 keller $
 * $URL: PlusValue.java $
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

import org.sbml.jsbml.ASTNode;

/**
 * This class can compute and store the value of a plus node.
 * @author Roland Keller
 * @version $Rev$
 */
public class PlusValue extends ASTNodeValue {

	/**
	 * @param interpreter
	 * @param node
	 */
	public PlusValue(ASTNodeInterpreter interpreter, ASTNode node) {
		super(interpreter, node);
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
	 */
	protected void computeDoubleValue() {
		doubleValue = interpreter.plus(children, numChildren, time);
	}

}
