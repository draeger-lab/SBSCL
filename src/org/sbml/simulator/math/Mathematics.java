/*
 * $Id$
 * $URL$
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

/**
 * @author Marcel Kronfeld
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class Mathematics {

  /**
   * Scales a vector with the given scalar.
   * 
   * @param scale
   * @param vec
   */
  public static void scale(double scale, double[] vec) {
    for (int i = 0; i < vec.length; i++) {
      vec[i] *= scale;
    }
  }
  
	/**
   * Add vectors in place setting res = v1 + v2.
   * 
   * @param v1
   * @param v2
   * @return vector addition
   */
  public static void vvAdd(double[] v1, double[] v2, double[] res) {
    vvAddOffs(v1, 0, v2, 0, res, 0, v1.length);
  }
	
	/**
   * Add vectors in place setting with an offset within the target vector,
   * meaning that res[resOffs+i]=v1[v1Offs+i]+v2[v2Offs+i] for i in length.
   * 
   * @param v1
   * @param v2
   * @return vector addition
   */
  public static void vvAddOffs(double[] v1, int v1Offs, double[] v2,
      int v2Offs, double[] res, int resOffs, int len) {
    for (int i = 0; i < len; i++) {
      res[resOffs + i] = v1[v1Offs + i] + v2[v2Offs + i];
    }
  }
  
  /**
	 * Subtract vectors returning a new vector c = a - b.
	 * 
	 * @param a
	 * @param b
	 * @return a new vector c = a - b
	 */
	public static double[] vvSub(double[] a, double[] b) {
		double[] result = new double[a.length];
		vvSub(a, b, result);
		return result;
	}

  /**
	 * Subtract vectors returning a new vector c = a - b.
	 * 
	 * @param a
	 * @param b
	 * @return a new vector c = a - b
	 */
	public static void vvSub(double[] a, double[] b, double[] res) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] - b[i];
		}
	}
  
}
