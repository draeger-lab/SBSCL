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

import org.apache.commons.math.ode.nonstiff.HighamHall54Integrator;

/**
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class HighamHall54Solver extends FirstOrderSolver {
    
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2601862472447650296L;
	
	/**
	 * 
	 */
	public HighamHall54Solver() {
	    super();
	    absTol = 0.5d;
	    relTol = 0.01d;
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public HighamHall54Solver(double stepSize) {
		super(stepSize);
	    absTol = 0.5d;
	    relTol = 0.01d;
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative
	 */
	public HighamHall54Solver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	    absTol = 0.5d;
	    relTol = 0.01d;
	}
	
	/**
	 * 
	 * @param dormandPrinceSolver
	 */
	public HighamHall54Solver(HighamHall54Solver solver) {
		super(solver);
		this.integrator=solver.getIntegrator();
	}
	
	/* (non-Javadoc)
	 * @see org.simulator.math.odes.FirstOrderSolver#clone()
	 */
	public HighamHall54Solver clone() {
		return new HighamHall54Solver(this);
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.FirstOrderSolver#createIntegrator()
	 */
	protected void createIntegrator() {
		integrator=new HighamHall54Integrator(Math.min(1e-8,Math.min(1.0,getStepSize())), Math.min(1.0,getStepSize()), getAbsTol(), getRelTol());
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "Higham-Hall 54 solver";
	}
}
