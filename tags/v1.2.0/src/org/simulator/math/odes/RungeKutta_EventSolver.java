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
 * Runge-Kutta method.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class RungeKutta_EventSolver extends AbstractDESSolver {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -2034495479346567501L;

	/**
	 * Stores temporary results for the fourth-order Runge-Kutta method.
	 */
	transient protected double[][] kVals = null;
	/**
	 * Helper variable for the k values.
	 */
	transient protected double[] kHelp;

	/**
	 * default constructor
	 */
	public RungeKutta_EventSolver() {
		super();
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public RungeKutta_EventSolver(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative
	 *            the nonnegative flag of the super class
	 * @see AbstractDESSolver
	 */
	public RungeKutta_EventSolver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}
	
	/**
	 * clone constructor
	 * @param rkEventSolver
	 */
	public RungeKutta_EventSolver(RungeKutta_EventSolver rkEventSolver) {
		super(rkEventSolver);
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#computeChange(org.simulator.math.odes.DESystem, double[], double, double, double[], boolean)
	 */
	public double[] computeChange(DESystem DES, double[] yTemp, double t,
			double h, double[] change, boolean steadyState) throws DerivativeException {
		int dim = DES.getDimension();
		if ((kVals == null) || (kVals.length != 4) || (kVals[0].length != dim)) {
			// "static" vectors which are allocated only once
			kVals = new double[4][dim];
			kHelp = new double[dim];
		}

		// k0
		DES.computeDerivatives(t, yTemp, kVals[0]);
		Mathematics.svMult(h, kVals[0], kVals[0]);

		// k1
		Mathematics.svvAddScaled(0.5, kVals[0], yTemp, kHelp);
		DES.computeDerivatives(t + h / 2, kHelp, kVals[1]);
		Mathematics.svMult(h, kVals[1], kVals[1]);

		// k2
		Mathematics.svvAddScaled(0.5, kVals[1], yTemp, kHelp);
		DES.computeDerivatives(t + h / 2, kHelp, kVals[2]);
		Mathematics.svMult(h, kVals[2], kVals[2]);

		// k3
		Mathematics.vvAdd(yTemp, kVals[2], kHelp);
		DES.computeDerivatives(t + h, kHelp, kVals[3]);
		Mathematics.svMult(h, kVals[3], kVals[3]);

		// combining all k's
		Mathematics.svvAddScaled(2, kVals[2], kVals[3], kVals[3]);
		Mathematics.svvAddScaled(2, kVals[1], kVals[3], kVals[2]);
		Mathematics.svvAddAndScale(1d / 6d, kVals[0], kVals[2], change);

		return change;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "4th order Runge-Kutta event solver";
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#clone()
	 */
	public RungeKutta_EventSolver clone() {
		return new RungeKutta_EventSolver(this);
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
		return 64;
	}

}
