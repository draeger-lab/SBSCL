/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
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
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Symbol;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class computes and stores values of {@link ASTNode}s that refer to a {@link Compartment} or
 * a {@link Parameter}.
 *
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
   * @param interpreter
   * @param node
   * @param sb
   * @param valueHolder
   * @param position
   */
  public CompartmentOrParameterValue(ASTNodeInterpreter interpreter, ASTNode node, Symbol sb,
      SBMLValueHolder valueHolder, int position) {
    super(interpreter, node);
    this.sb = sb;
    id = sb.getId();
    isConstant = sb.getConstant();
    this.valueHolder = valueHolder;
    this.position = position;
  }

  /* (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
   */
  @Override
  protected void computeDoubleValue(double delay) {
    if (delay == 0) {
      doubleValue = valueHolder.getCurrentValueOf(position);
    } else {
      double valueTime = interpreter.symbolTime() - delay;
      doubleValue = valueHolder.computeDelayedValue(valueTime, id, null, null, 0);
    }
    if (isConstant) {
      if ((valueHolder.getCurrentTime() > 0) && (delay == 0)) {
        alreadyProcessed = true;
      } else {
        alreadyProcessed = false;
      }
    }
  }
}
