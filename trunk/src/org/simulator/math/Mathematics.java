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

/**
 * This class contains a collection of mathematical functions
 * like the faculty, logarithms and several trigonometric functions.
 * 
 * @author Marcel Kronfeld
 * @author Andreas Dr&auml;ger
 * @author Diedonn&eacute; Motsuo Wouamba
 * @version $Rev$
 * @since 0.9
 */
public class Mathematics {
	
	/**
   * This method computes the factorial! function.
   *
   * @param n
   * @return result
   */
  public static final double factorial(double n) {
    if ((n == 0) || (n == 1)) {
    	return 1;
    }
    return n * factorial(n - 1);
  }
  

	/**
	 * Swaps a and b if a is greater then b.
	 * 
	 * @param a
	 * @param b
	 */
	public static final void swap(double a, double b) {
		if (a > b) {
			double swap = b;
			b = a;
			a = swap;
		}
	}
	
	/**
	 * This just computes the minimum of three integer values.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return Gives the minimum of three integers
	 */
	public static int min(int x, int y, int z) {
		if ((x < y) && (x < z)) {
			return x;
		}
		if (y < z) {
			return y;
		}
		return z;
	}
	
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
	 * Multiplies (scales) every element of the array v with s in place.
	 * 
	 * @param s
	 *            a scalar
	 * @param v
	 *            an array to be multiplied with s.
	 */
	public static void svMult(double s, double[] v, double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = v[i] * s;
		}
	}
  
	/**
	 * Add vectors scaled: res[i] = s*(v[i] + w[i])
	 * 
	 * @param s
	 * @param v
	 * @param w
	 */
	public static void  svvAddAndScale(double s, double[] v, double[] w,
			double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = s * (v[i] + w[i]);
		}
	}
	
	/**
	 * Add vectors scaled: res[i] = s*v[i] + w[i]
	 * 
	 * @param s
	 * @param v
	 * @param w
	 */
	public static void svvAddScaled(double s, double[] v, double[] w,
			double[] res) {
		for (int i = 0; i < v.length; i++) {
			res[i] = s * v[i] + w[i];
		}
	}
  
  /**
   * Add vectors in place setting res = v1 + v2.
   * 
   * @param v1
   * @param v2
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
	 */
	public static void vvSub(double[] a, double[] b, double[] res) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] - b[i];
		}
	}
  
}
