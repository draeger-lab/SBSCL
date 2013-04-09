/*
 * $Id: Relative_N_Metric.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/Relative_N_Metric.java$
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

import java.util.LinkedList;
import java.util.List;

/**
 * Computes the relative distance of two vectors based on the {@link N_Metric} distance.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class Relative_N_Metric extends QualityMeasure {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5066304615795368201L;
	
	/**
	 * Is default value NaN? (for faster computation)
	 */
	private boolean defaultNaN;
	
	/**
	 * The metric the relative distance is based on.
	 */
	protected N_Metric metric;

	/**
	 * Default constructor
	 */
	public Relative_N_Metric() {
		super(Double.NaN);
		metric = new N_Metric();
		defaultNaN = true;
	}
	
	/**
	 * Initialization with a certain n
	 * @param root
	 */
	public Relative_N_Metric(double root) {
		super(Double.NaN);
		metric = new N_Metric(root);
		defaultNaN = true;
	}

	/**
	 * Initialization with a given {@link N_Metric}
	 * @param metric
	 */
	public Relative_N_Metric(N_Metric metric) {
		super(Double.NaN);
		this.metric = metric;
		if(Double.isNaN(metric.getDefaultValue())) {
			defaultNaN = true;
		}
		else {
			defaultNaN = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.Distance#distance(java.lang.Iterable, java.lang.Iterable, double)
	 */
	public double distance(Iterable<? extends Number> x,
			Iterable<? extends Number> expected, double defaultValue) {
		double numerator=metric.distance(x, expected, defaultValue);
		List<Double> nullVector = new LinkedList<Double>();
		for(@SuppressWarnings("unused") Number n : expected) {
			nullVector.add(0d);
		}
		double denominator=metric.distance(expected,nullVector,defaultValue);
		double denominator2=metric.distance(x,nullVector,defaultValue);
		if ((denominator != 0) && (denominator2 != 0) ) {
			return numerator / denominator;
		} else if((denominator == 0) && (denominator2 == 0)){
			return numerator;
		} else if(defaultNaN) {
				return numerator;
		}
		else {
			return this.defaultValue;
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.QualityMeasure#setDefaultValue(double)
	 */
	public void setDefaultValue(double value) {
		super.setDefaultValue(value);
		if(Double.isNaN(defaultValue)) {
			defaultNaN = true;
		}
		else {
			defaultNaN = false;
		}
	}

	/**
	 * Sets the root
	 * @param root
	 */
	public void setRoot(double root) {
		metric.setRoot(root);
	}


}
