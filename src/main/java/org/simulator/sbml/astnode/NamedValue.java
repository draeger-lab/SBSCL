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

/**
 * This class computes and stores values of variables occurring in a function.
 *
 * @author Roland Keller
 * @version $Rev$
 */
public class NamedValue extends ASTNodeValue {

  /**
   * The function the variable occurs in
   */
  private FunctionValue function;

  /**
   * The index of the variable in the arguments array of the corresponding {@link FunctionValue}.
   */
  private int index;

  /**
   * @param interpreter
   * @param node
   * @param function
   */
  public NamedValue(ASTNodeInterpreter interpreter, ASTNode node, FunctionValue function) {
    super(interpreter, node);
    this.function = function;
    index = function.getIndex(node.getName());
  }

  /* (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
   */
  @Override
  protected void computeDoubleValue(double delay) {
    doubleValue = function.getArgumentValues()[index];
  }
}
