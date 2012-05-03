/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. The Keio University, Japan
 * 3. The Harvard University, USA
 * 4. The University of Edinburgh
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

import org.apache.commons.math.ode.nonstiff.DormandPrince54Integrator;

/**
 * This class is a wrapper for the Dormand-Prince-54 solver in the Apache Math Library.
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class DormandPrince54Solver extends FirstOrderSolver {
    
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2601862472447650296L;
	
	/**
	 * default constructor
	 */
	public DormandPrince54Solver() {
	    super();
	}
	
	/**
	 * clone constructor
	 * @param dormandPrinceSolver
	 */
	public DormandPrince54Solver(DormandPrince54Solver princeSolver) {
		super(princeSolver);
		this.integrator=princeSolver.getIntegrator();
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public DormandPrince54Solver(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param the nonnegative flag of the super class @see org.sbml.simulator.math.odes.AbstractDESSolver
   */
	public DormandPrince54Solver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#clone()
	 */
	public DormandPrince54Solver clone() {
		return new DormandPrince54Solver(this);
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#createIntegrator()
	 */
	protected void createIntegrator() {
		integrator = new DormandPrince54Integrator(Math.min(1e-8,Math.min(1.0,getStepSize())), Math.min(1.0,getStepSize()), getAbsTol(), getRelTol());
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "Dormand-Prince 54 solver";
	}

}
