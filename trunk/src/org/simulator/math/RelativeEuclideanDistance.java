/*
 * $Id: RelativeEuclideanDistance.java 246 2012-10-04 14:39:55Z "keller"$
 * $URL: svn+ssh://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/math/RelativeEuclideanDistance.java$
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
 * Class for computation of the relative Euclidean distance of two vectors.
 * @author Roland Keller
 * @version $Rev$
 * @since
 */
public class RelativeEuclideanDistance extends Relative_N_Metric {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -7488743431449082030L;

	/**
	 * Initializes a new instance
	 */
	public RelativeEuclideanDistance() {
		super(new EuclideanDistance());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.Relative_N_Metric#setRoot(double)
	 */
	public void setRoot(double root) {
		//root should not be changed
	}
	
}
