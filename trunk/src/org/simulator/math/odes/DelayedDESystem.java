/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
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

/**
 * This interface describes a differential equation system containing values with a delay function.
 * @author Roland Keller
 * @version $Rev$
 */
public interface DelayedDESystem extends DESystem {

  /**
   * 
   * @param the delay value holder to be registered
   */
  public void registerDelayValueHolder(DelayValueHolder dvh);
  
}
