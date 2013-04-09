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

import java.util.logging.Logger;

import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.util.FastMath;
import org.simulator.math.Mathematics;

/**
 * This class is the superclass of the wrapper classes for the solvers of the Apache Math library.
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public abstract class FirstOrderSolver extends AdaptiveStepsizeIntegrator {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2671266540106066022L;

	/**
	 * A logger.
	 */
	private static final Logger logger = Logger.getLogger(FirstOrderSolver.class
			.getName());

	/**
	 * The result of the integration.
	 */
	private double[] integrationResult;

	/**
	 * The integrator used.
	 */
	protected AbstractIntegrator integrator;

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#setStepSize(double)
	 */
	@Override
	public void setStepSize(double stepSize) {
		super.setStepSize(stepSize);
		createIntegrator();
	}

	/**
	 * default constructor
	 */
	public FirstOrderSolver() {
		super();
		createIntegrator();
		addHandler();
	}

	/**
	 * @param stepSize
	 */
	public FirstOrderSolver(double stepSize) {
		super(stepSize);
		createIntegrator();
		addHandler();
	}

	/**
	 * @param stepSize
	 * @param nonnegative
	 *            the nonnegative flag of the super class
	 * @see AbstractDESSolver
	 */
	public FirstOrderSolver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
		createIntegrator();
		addHandler();
	}

	/**
	 * clone constructor
	 * @param firstOrderSolver
	 */
	public FirstOrderSolver(FirstOrderSolver firstOrderSolver) {
		super(firstOrderSolver);
		createIntegrator();
		addHandler();
	}

	/**
	 * 
	 */
	private void addHandler() {
		integrator.addEventHandler(this, 1, 1, 1);
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#clone()
	 */
	public abstract FirstOrderSolver clone();

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#computeChange(org.sbml.simulator.math.odes.DESystem, double[], double, double, double[])
	 */
	public double[] computeChange(DESystem DES, double[] y, double t,
			double stepSize, double[] change, boolean steadyState) throws DerivativeException {
		if((integrationResult==null)||(integrationResult.length!=y.length)) {
			integrationResult = new double[y.length];
		}

		double tstart = t;
		double tend = t + stepSize;
		if (FastMath.abs(tstart - tend) <= (1.0e-12 * FastMath.max(
				FastMath.abs(tstart), FastMath.abs(tend)))) {
			for (int i = 0; i != change.length; i++) {
				change[i] = 0;
			}
		} else {
			try {
				integrator.integrate(DES, tstart, y, tend, integrationResult);
				Mathematics.vvSub(integrationResult, y, change);
			} catch (Exception e) {
				setUnstableFlag(true);
				logger.fine(e.getLocalizedMessage());
			}
		}
		return change;
	}

	/**
	 * initialization function of the integrator
	 */
	protected abstract void createIntegrator();

	/**
	 * @return integrator
	 */
	public AbstractIntegrator getIntegrator() {
		return integrator;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#hasSolverEventProcessing()
	 */
	protected boolean hasSolverEventProcessing() {
		return false;
	}

}
