/*
 * $Id:  DelayValueHolder.java 16:42:37 keller $
 * $URL: DelayValueHolder.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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

import java.io.Serializable;

import org.apache.commons.math.ode.DerivativeException;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public interface DelayValueHolder extends Serializable {
  
  /**
   * 
   * @param time
   * @param id
   * @throws DerivativeException
   */
  public double computeDelayedValue(double time, String id) throws DerivativeException;

}
