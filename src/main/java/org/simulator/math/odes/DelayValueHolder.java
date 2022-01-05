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
package org.simulator.math.odes;

import java.io.Serializable;

/**
 * This interface describes a value holder that can compute values with delay.
 *
 * @author Roland Keller
 * @version $Rev$
 */
public interface DelayValueHolder extends Serializable {

  /**
   * Returns the value for the element with the given id at a time point in the past, where the time
   * gives the amount of time in the past.
   *
   * @param time          the time point (in the past) at which the value is to be computed for the
   *                      element with the given id.
   * @param id            the id of the delayed value
   * @param DES           the DESystem
   * @param initialValues the initialValues of the DES
   * @param yIndex        the index corresponding to the id in the vector of the DES
   * @return the computed value for the element with the given identifier at the time point in the
   * past.
   */
  double computeDelayedValue(double time, String id, DESystem DES, double[] initialValues,
      int yIndex);
}
