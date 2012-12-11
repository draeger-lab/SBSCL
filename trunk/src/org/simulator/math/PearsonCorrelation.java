/*
 * $Id: PearsonCorrelation.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/PearsonCorrelation.java$
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

import java.util.Iterator;

/**
 * Implementation of the Pearson correlation. 
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class PearsonCorrelation extends QualityMeasure {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -493779339080103217L;

	
	/**
	 * Default constructor. This sets the standard value for the parameter as
	 * given by the getStandardParameter() method. The default value is set to
	 * NaN.
	 */
	public PearsonCorrelation() {
		super();
		meanFunction=new ArithmeticMean();
	}

	/**
	 * Constructor, which allows setting the parameter value for default value.
	 * 
	 * @param defaultValue the default value
	 */
	public PearsonCorrelation(double defaultValue) {
		super(defaultValue);
		meanFunction=new ArithmeticMean();
	}
	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.Distance#distance(java.lang.Iterable, java.lang.Iterable, double)
	 */
	public double distance(Iterable<? extends Number> x,
			Iterable<? extends Number> y, double defaultValue) {
		Iterator<? extends Number> yIterator = y.iterator();
		
		MeanFunction meanF = new ArithmeticMean();
		double meanX = meanF.computeMean(x);
		double meanY = meanF.computeMean(y);
		
		double sumNumerator = 0d;
		double sumXSquared = 0d;
		double sumYSquared = 0d;
		
		for (Number number : x) {
			if (!yIterator.hasNext()) {
				break;
			}
			double x_i = number.doubleValue();
			double y_i = yIterator.next().doubleValue();
			sumNumerator+= (x_i-meanX)*(y_i-meanY);
			sumXSquared+= (x_i-meanX)*(x_i-meanX);
			sumYSquared+= (y_i-meanY)*(y_i-meanY);
		}
		
		double denominator=Math.sqrt(sumXSquared*sumYSquared);
		if(denominator!=0) {
			return sumNumerator/denominator;
		}
		else {
			return defaultValue;
		}
	}

}
