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

import org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegrator;

/**
 * This class is a wrapper for the Adams-Bashforth solver in the Apache Math Library.
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class AdamsBashforthSolver extends FirstOrderSolver {
	
	/**
	 * Generated serial version identifier. 
	 */
	private static final long serialVersionUID = -2601862472447650296L;
	
	/**
	 * default constructor
	 */
	public AdamsBashforthSolver() {
	    super();
	}
	
	/**
	 * Clone constructor
	 * @param adamsSolver
	 */
	public AdamsBashforthSolver(AdamsBashforthSolver adamsSolver) {
		super(adamsSolver);
		this.integrator = adamsSolver.getIntegrator();
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public AdamsBashforthSolver(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative the nonnegative flag of the super class
	 * @see AbstractDESSolver
	 */
	public AdamsBashforthSolver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#clone()
	 */
	public AdamsBashforthSolver clone() {
		return new AdamsBashforthSolver(this);
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#createIntegrator()
	 */
	public void createIntegrator() {
		integrator = new AdamsBashforthIntegrator(5, Math.min(1e-8d, Math.min(1d, getStepSize())), Math.min(1d, getStepSize()), getAbsTol(), getRelTol());
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "Adams-Bashforth solver";
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#getKISAOTerm()
	 */
	//@Override
	public int getKiSAOterm() {
		return 279;
	}

}
