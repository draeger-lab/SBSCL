/*
 * $Id: RelativeSquaredError.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/RelativeSquaredError.java$
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
 * An implementation of the relative squared error with a default value to avoid
 * division by zero. Actually, the exponent in this error function is 2 (squared
 * error). Irrespectively, it is possible to set the exponent to different
 * values.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class RelativeSquaredError extends N_Metric {

	/**
	 * Generated serial identifier.
	 */
	private static final long serialVersionUID = 1643317436479699973L;

	/**
	 * Constructs a new RelativeSquaredError. Here
	 * the root is the default value to be returned by the distance function.
	 */
	public RelativeSquaredError() {
		super(2d);
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.math.Distance#additiveTerm(double, double, double, double)
	 */
	@Override
	double additiveTerm(double x, double y, double root, double defaultValue) {
		return (y != 0d) ? Math.pow(Math.abs(x - y) / y, root) : Math.abs(x - y);
	}

	
	/* (non-Javadoc)
	 * @see org.sbml.squeezer.math.NMetric#overallDistance()
	 */
	@Override
	double overallDistance(double distance, double root, double defaultValue) {
		return distance;
	}

}
