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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;

/**
 * This class computes and stores values of ASTNodes that refer to a local parameter.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class LocalParameterValue extends ASTNodeValue {
	
	/**
	 * The corresponding local parameter
	 */
	protected LocalParameter lp;

	/**
	 * 
	 * @param interpreter
	 * @param node
	 * @param lp
	 */
	public LocalParameterValue(ASTNodeInterpreter interpreter, ASTNode node,
			LocalParameter lp) {
		super(interpreter, node);
		this.lp = lp;
		doubleValue=lp.getValue();
		isDouble=true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.sbml.astnode.ASTNodeValue#compileDouble(double)
	 */
	@Override
	public double compileDouble(double time) {
		this.time=time;
		return doubleValue;
	}

}
