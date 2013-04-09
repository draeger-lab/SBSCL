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
import org.sbml.jsbml.Symbol;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class computes and stores values of ASTNodes that refer to a compartment or a parameter.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class CompartmentOrParameterValue extends ASTNodeValue {
	/**
	 * The compartment or parameter the corresponding ASTNode is referring to
	 */
	protected Symbol sb;

	/**
	 * The id of the compartment or parameter.
	 */
	protected String id;

	/**
	 * The value holder that stores the current simulation results.
	 */
	protected SBMLValueHolder valueHolder;
  
	/**
	 * The position of the current compartment/parameter value in the Y vector of the value holder
	 */
	protected int position;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param sb
   * @param valueHolder
   * @param position
   */
  public CompartmentOrParameterValue(ASTNodeInterpreter interpreter, ASTNode node,
    Symbol sb, SBMLValueHolder valueHolder, int position) {
    super(interpreter, node);
    this.sb = sb;
    this.id=sb.getId();
//    if(sb.getConstant()) {
//      isConstant = true;
//      doubleValue = sb.getValue();
//    }
    this.valueHolder = valueHolder;
    this.position=position;
  }
  
  /*
   * (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
   */
  @Override
  protected void computeDoubleValue() {
    doubleValue=valueHolder.getCurrentValueOf(position);
  }

}
