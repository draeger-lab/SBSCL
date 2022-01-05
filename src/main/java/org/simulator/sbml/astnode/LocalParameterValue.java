/*
 * $Id: LocalParameterValue.java 205 2012-05-05 11:57:39Z andreas-draeger $
 * $URL: http://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/sbml/astnode/LocalParameterValue.java $
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
import org.sbml.jsbml.LocalParameter;

/**
 * This class computes and stores values of {@link ASTNode}s that refer to a {@link
 * LocalParameter}.
 *
 * @author Roland Keller
 * @version $Rev: 205 $
 * @since 1.0
 */
public class LocalParameterValue extends ASTNodeValue {

  /**
   * The corresponding local parameter
   */
  protected LocalParameter lp;

  /**
   * @param interpreter
   * @param node
   * @param lp
   */
  public LocalParameterValue(ASTNodeInterpreter interpreter, ASTNode node, LocalParameter lp) {
    super(interpreter, node);
    this.lp = lp;
    doubleValue = lp.getValue();
    isDouble = true;
    isConstant = true;
  }

  /* (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#compileDouble(double)
   */
  @Override
  public double compileDouble(double time, double delay) {
    this.time = time;
    if (alreadyProcessed == false) {
      doubleValue = lp.getValue();
      alreadyProcessed = true;
    }
    return doubleValue;
  }
}
