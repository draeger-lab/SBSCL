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
package org.simulator.math.odes;

import org.apache.commons.math.ode.DerivativeException;

/**
 * This class represents an event-driven differential equation system
 *
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public interface EventDESystem extends DESystem {

  /**
   * Counts the number of events in this system.
   *
   * @return The number of events that are to be checked and potentially evaluated in each time
   * point.
   */
  int getEventCount();

  /**
   * Counts the number of rules to be evaluated in each time point.
   *
   * @return The number of rules in the system.
   */
  int getRuleCount();

  /**
   * Calculates the changes or assignments that are defined by all rules in the system at the given
   * simulation time point.
   *
   * @param t The current simulation time.
   * @param Y The current change of the system.
   * @return flag that is {@code true} if there has been a change in the Y vector caused by the
   * rules.
   * @throws DerivativeException
   */
  boolean processAssignmentRules(double t, double[] Y)
      throws DerivativeException;

  /**
   * Returns a list with event assignments for the events triggered either by the time t or by the
   * concentrations of the species stored in Y.
   *
   * @param t The current simulation time.
   * @param Y The current change of the system.
   * @return Returns a list with event assignments for the events triggered
   * @throws DerivativeException
   */
  EventInProgress getNextEventAssignments(double t, double previousTime, double[] Y)
      throws DerivativeException;

  /**
   * @return flag that is {@code true}, if the change vector is always zero in the system.
   */
  boolean getNoDerivatives();

}
