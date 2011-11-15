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
package org.simulator.math.odes;

import org.simulator.math.Mathematics;


/**
 * Runge-Kutta method.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class RKEventSolver extends AbstractDESSolver {

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
	 * 
	 */
	public RKEventSolver() {
		super();
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public RKEventSolver(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative
	 */
	public RKEventSolver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}
	
	/**
	 * 
	 * @param rkEventSolver
	 */
	public RKEventSolver(RKEventSolver rkEventSolver) {
		super(rkEventSolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.tools.math.des.AbstractDESSolver#computeChange(eva2.tools.math.des
	 * .DESystem, double[], double, double, double[])
	 */
	@Override
	public double[] computeChange(DESystem DES, double[] yTemp, double t,
			double h, double[] change) throws IntegrationException {
		int dim = DES.getDESystemDimension();
		if ((kVals == null) || (kVals.length != 4) || (kVals[0].length != dim)) {
			// "static" vectors which are allocated only once
			kVals = new double[4][dim];
			kHelp = new double[dim];
		}

		// k0
		DES.getValue(t, yTemp, kVals[0]);
		Mathematics.svMult(h, kVals[0], kVals[0]);

		// k1
		Mathematics.svvAddScaled(0.5, kVals[0], yTemp, kHelp);
		DES.getValue(t + h / 2, kHelp, kVals[1]);
		Mathematics.svMult(h, kVals[1], kVals[1]);

		// k2
		Mathematics.svvAddScaled(0.5, kVals[1], yTemp, kHelp);
		DES.getValue(t + h / 2, kHelp, kVals[2]);
		Mathematics.svMult(h, kVals[2], kVals[2]);

		// k3
		Mathematics.vvAdd(yTemp, kVals[2], kHelp);
		DES.getValue(t + h, kHelp, kVals[3]);
		Mathematics.svMult(h, kVals[3], kVals[3]);

		// combining all k's
		Mathematics.svvAddScaled(2, kVals[2], kVals[3], kVals[3]);
		Mathematics.svvAddScaled(2, kVals[1], kVals[3], kVals[2]);
		Mathematics.svvAddAndScale(1d / 6d, kVals[0], kVals[2], change);

		return change;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.AbstractDESSolver#getName()
	 */
	@Override
	public String getName() {
		return "4th order Runge-Kutta event solver";
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#clone()
	 */
	@Override
	public RKEventSolver clone() {
		return new RKEventSolver(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#hasSolverEventProcessing()
	 */
  @Override
  protected boolean hasSolverEventProcessing() {
    return false;
  }

}
