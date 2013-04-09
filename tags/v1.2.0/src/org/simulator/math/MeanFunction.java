/*
 * $Id: MeanFunction.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/MeanFunction.java$
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * In this class functions for the computation of an overall distance based on the distance values determined for each column of a table are defined.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public abstract class MeanFunction implements Serializable {
	
	/**
	 * Generated serial identifier.
	 */
	private static final long serialVersionUID = -7272984374334773096L;
	
	/**
	 * Computes the overall distance
	 * @param values the distance values for the columns
	 * @return the computed value
	 */
	public abstract double computeMean(double... values);
	
	/**
	 * Computes the overall distance
	 * @param values the distance values for the columns
	 * @return the computed value
	 */
	public double computeMean(List<Double> values) {
		double[] val = new double[values.size()];
		for (int i = 0; i != val.length; i++) {
			val[i] = values.get(i);
		}
		return computeMean(val);
	}
	
	/**
	 * Computes the overall distance
	 * @param values the distance values for the columns
	 * @return the computed value
	 */
	public double computeMean(Iterable<? extends Number> values) {
		List<Double> val = new ArrayList<Double>();
		for (Number number : values) {
			val.add(number.doubleValue());
		}
		return computeMean(val);
	}

}
