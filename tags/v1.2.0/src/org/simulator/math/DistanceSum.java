/*
 * $Id: DistanceSum.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/DistanceSum.java$
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

/**
 * Sets the overall distance to the sum of the column distances.
 * @author Roland Keller
 * @version $Rev$
 */
public class DistanceSum extends MeanFunction {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 7739137402966833880L;

  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.MeanFunction#computeMean(double[])
   */
  public double computeMean(double... values) {
    double result = 0d;
    for (double value : values) {
      result += value;
    }
    return result;
  }
  
}
