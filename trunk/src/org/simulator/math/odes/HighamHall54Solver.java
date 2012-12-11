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

import org.apache.commons.math.ode.nonstiff.HighamHall54Integrator;

/**
 * This class is a wrapper for the Higham-Hall-54 solver in the Apache Math Library.
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
	 * default constructor
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
	 *            the nonnegative flag of the super class
	 * @param nonnegative
	 * @see AbstractDESSolver
	 */
	public HighamHall54Solver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	    absTol = 0.5d;
	    relTol = 0.01d;
	}
	
	/**
	 * clone constructor
	 * @param solver
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

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#getKISAOTerm()
	 */
	//@Override
	public int getKiSAOterm() {
		return 434;
	}
}
