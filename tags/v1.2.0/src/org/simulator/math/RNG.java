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
package org.simulator.math;

import java.util.Random;
import java.util.logging.Logger;


/**
 * A Random Number Generator.
 * 
 * @author Marcel Kronfeld
 * @version $Rev$
 * @since 0.9
 */
public class RNG {
  /**
   * 
   */
  private static Random RANDOM;
  /**
   * 
   */
  private static long RANDOM_SEED;
  /**
   * 
   */
  private static final Logger logger = Logger.getLogger(RNG.class.getName());
  
  /**
   *
   */
  static {
    RANDOM_SEED = System.currentTimeMillis();
    RANDOM = new Random(RANDOM_SEED);
  }
  
  /**
   * Generates a random integer between first and second input value.
   * @param low
   * @param high
   * @return randomNumber
   */
  public static int randomInt(int low, int high) {
    if (high < low) {
      logger.fine("Invalid boundary values! Returning -1.");
      return -1;
    }
    int result = (Math.abs(RANDOM.nextInt()) % (high - low + 1)) + low;
    if ((result < low) || (result > high)) {
      logger.fine(String.format(
                "Error, invalid value %d in RNG.randomInt! boundaries were low = %d\thigh = %d",
                result, low, high));
      result = Math.abs(RANDOM.nextInt() % (high - low + 1)) + low;
    }
    return result;
  }
  
}
