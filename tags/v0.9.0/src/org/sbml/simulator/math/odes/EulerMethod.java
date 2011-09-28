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

package org.sbml.simulator.math.odes;

import org.sbml.simulator.math.Mathematics;


/**
 * @author Andreas Dr&auml;ger
 * @date 14:37:21, 2010-08-03
 * @version $Rev$
 * @since 1.0
 */
public class EulerMethod extends AbstractDESSolver {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 9094797527506196715L;

	/**
	 * 
	 */
	public EulerMethod() {
		super();
	}

	/**
	 * 
	 * @param stepSize
	 */
	public EulerMethod(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative
	 */
	public EulerMethod(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}

	/**
	 * 
	 * @param eulerMethod
	 */
	public EulerMethod(EulerMethod eulerMethod) {
		super(eulerMethod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.AbstractDESSolver#getName()
	 */
	@Override
	public String getName() {
		return "Euler's method";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.tools.math.des.AbstractDESSolver#computeChange(eva2.tools.math.des
	 * .DESystem, double[], double, double, double[])
	 */
	public double[] computeChange(DESystem DES, double[] yPrev, double t,
			double stepSize, double[] change) throws IntegrationException {
		DES.getValue(t, yPrev, change);
		Mathematics.scale(stepSize, change);
		return change;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#clone()
	 */
	public EulerMethod clone() {
		return new EulerMethod(this);
	}
}
