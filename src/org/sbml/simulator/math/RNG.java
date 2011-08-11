/*
 * $Id:  RNG.java 16:33:29 draeger$
 * $URL: RNG.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
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
package org.sbml.simulator.math;

import java.util.Random;
import java.util.logging.Logger;


/**
 * A Random Number Generator.
 * 
 * @author Marcel Kronfeld
 * @version $Rev$
 * @since 1.0
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
    RANDOM_SEED=System.currentTimeMillis();
    RANDOM=new Random(RANDOM_SEED);
  }
  
  /**
   * 
   * @param low
   * @param high
   * @return
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
