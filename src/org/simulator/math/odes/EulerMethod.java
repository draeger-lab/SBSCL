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
package org.simulator.math.odes;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.Mathematics;


/**
 * In this class the Euler method for integration a differential equation system is implemented.
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class EulerMethod extends AbstractDESSolver {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 9094797527506196715L;

	/**
	 * default constructor
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
	 *            the nonnegative flag of the super class
	 * @see AbstractDESSolver
	 */
	public EulerMethod(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}

	/**
	 * clone constructor
	 * @param eulerMethod
	 */
	public EulerMethod(EulerMethod eulerMethod) {
		super(eulerMethod);
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "Euler's method";
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#computeChange(org.simulator.math.odes.DESystem, double[], double, double, double[], boolean)
	 */
	public double[] computeChange(DESystem DES, double[] yPrev, double t,
			double stepSize, double[] change, boolean steadyState) throws DerivativeException {
		DES.computeDerivatives(t, yPrev, change);
		Mathematics.scale(stepSize, change);
		return change;
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#clone()
	 */
	public EulerMethod clone() {
		return new EulerMethod(this);
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#hasSolverEventProcessing()
	 */
	protected boolean hasSolverEventProcessing() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#getKISAOTerm()
	 */
	//@Override
	public int getKiSAOterm() {
		return 261;
	}

}
