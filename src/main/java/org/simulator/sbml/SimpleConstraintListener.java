/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
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
package org.simulator.sbml;

import java.text.MessageFormat;
import org.apache.log4j.Logger;

import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.util.SBMLtools;
import org.simulator.sbml.astnode.ASTNodeValue;

/**
 * This class represents a simple listener implementation to process the
 * violation of {@link Constraint}s during simulation by logging the violation
 * event in form of a warning.
 *
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.3
 */
public class SimpleConstraintListener implements ConstraintListener {

  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SimpleConstraintListener.class.getName());

  /**
   * Key to memorize user objects for logging the constraint violation
   */
  public static final String CONSTRAINT_VIOLATION_LOG = "CONSTRAINT_VIOLATION_LOG";

  public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

  /**
   * {@inheritDoc}
   */
  @Override
  public void processViolation(ConstraintEvent evt) {
    assert evt != null;
    String constraint = evt.getSource().getMath().toFormula();
    String message = SBMLtools.toXML(evt.getSource().getMessage());

    logger.warn(MessageFormat.format("[VIOLATION]\t{0} at time {1,number}: {2}", constraint, evt.getTime(), message));
    evt.getSource().putUserObject(CONSTRAINT_VIOLATION_LOG, Boolean.TRUE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processSatisfiedAgain(ConstraintEvent evt) {
      String constraint = evt.getSource().getMath().toFormula();
      logger.debug(MessageFormat.format("Constraint {0} satisfied again", constraint));
      evt.getSource().putUserObject(CONSTRAINT_VIOLATION_LOG, Boolean.FALSE);
  }
}
