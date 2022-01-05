/*
 * $Id: N_Metric.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/N_Metric.java$
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
package org.simulator.math;

import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * An implementation of an n-metric. An n-metric is basically the n<sup>th</sup> root of the sum of
 * the distances of every single element in two vectors (arrays), where this distance will always be
 * exponentiated by the value of n.
 *
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class N_Metric extends QualityMeasure {

  /**
   * Generated serial identifier.
   */
  private static final long serialVersionUID = -216525074796086162L;

  /**
   * The n of the metric.
   */
  protected double root;

  /**
   * Constructs a new NMetric with a default root of two. This will result in the Euclidean
   * distance. Other metrics can be used by either setting the root to another value or explicitly
   * using the distance function where the root must be given as an argument.
   */
  public N_Metric() {
    super();
    root = 3d;
  }

  /**
   * Constructs a new NMetric with a customized root. Depending on the values of root this results
   * in different metrics. Some are especially important:
   * <ul>
   * <li>one is the Manhattan norm or the city block metric.</li>
   * <li>two is the Euclidean metric.</li>
   * <li>Infinity is the maximum norm.</li>
   * </ul>
   *
   * @param root
   */
  public N_Metric(double root) {
    super(Double.NaN);
    this.root = root;
  }

  /**
   * @param x_i
   * @param y_i
   * @param root
   * @param defaultValue
   * @return
   */
  double additiveTerm(double x_i, double y_i, double root, double defaultValue) {
    return Math.pow(Math.abs(x_i - y_i), root);
  }

  /*
   *
   */
  @Override
  public double distance(Column x, Column y, double defaultValue) {
    if (root == 0d) {
      return defaultValue;
    }
    double d = 0;
    double x_i;
    double y_i;
    for (int i = 0; i != Math.min(x.getRowCount(), y.getRowCount()); i++) {
      x_i = x.getValue(i);
      y_i = y.getValue(i);
      if (computeDistanceFor(x_i, y_i, root, defaultValue)) {
        d += additiveTerm(x_i, y_i, root, defaultValue);
      }
    }
    return overallDistance(d, root, defaultValue);
  }

  /**
   * Returns the n of the metric.
   *
   * @return n
   */
  public double getRoot() {
    return root;
  }

  /**
   * Helper method, which can be overridden in extending classes.
   *
   * @param distance
   * @param root
   * @param defaultValue
   * @return computes the root the actual distance.
   */
  double overallDistance(double distance, double root, double defaultValue) {
    return Math.pow(distance, 1d / root);
  }

  /**
   * Sets the root.
   *
   * @param root
   */
  public void setRoot(double root) {
    this.root = root;
  }

  /**
   * @param x            expected
   * @param defaultValue
   * @return the distance of the column vector x to the point of origin.
   */
  public double distanceToZero(Column x, double defaultValue) {
    if (root == 0d) {
      return defaultValue;
    }
    double d = 0;
    double x_i;
    for (int i = 0; i != x.getRowCount(); i++) {
      x_i = x.getValue(i);
      if (computeDistanceFor(x_i, 0d, root, defaultValue)) {
        d += additiveTerm(x_i, 0d, root, defaultValue);
      }
    }
    return overallDistance(d, root, defaultValue);
  }
}
