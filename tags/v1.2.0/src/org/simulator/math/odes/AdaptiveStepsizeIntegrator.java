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

/**
 * This is an abstract class for solvers with adaptive stepsizes and given relative and absolute tolerances.
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.1
 */
public abstract class AdaptiveStepsizeIntegrator extends AbstractDESSolver {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5411228466445964211L;
	/**
	 * Default absolute allowable vectorial tolerance.
	 */
	protected double absTol = 1E-10d;
	/**
	 * Default relative allowable vectorial tolerance.
	 */
	protected double relTol = 1E-5d;
	
	/**
	 * 
	 */
	public AdaptiveStepsizeIntegrator() {
		super();
	}
	
	/**
	 * clone constructor
	 * @param adaptiveStepSizeIntegrator
	 */
	public AdaptiveStepsizeIntegrator(AdaptiveStepsizeIntegrator adaptiveStepSizeIntegrator) {
		super(adaptiveStepSizeIntegrator);
		absTol = adaptiveStepSizeIntegrator.getAbsTol();
		relTol = adaptiveStepSizeIntegrator.getRelTol();
	}

	/**
	 * 
	 * @param stepSize
	 */
	public AdaptiveStepsizeIntegrator(double stepSize) {
		super(stepSize);
	}

	/**
	 * 
	 * @param stepSize
	 * @param nonnegative the nonnegative flag of the super class
	 * @see AbstractDESSolver
	 */
	public AdaptiveStepsizeIntegrator(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
	}

	/**
	 * @return the absolute tolerance
	 */
	public double getAbsTol() {
		return absTol;
	}

	/**
	 * @return the relative tolerance
	 */
	public double getRelTol() {
		return relTol;
	}

	/**
	 * @param absTol the absolute tolerance to set
	 */
	public void setAbsTol(double absTol) {
		this.absTol = absTol;
	}

	/**
	 * @param relTol the relative tolerance to set
	 */
	public void setRelTol(double relTol) {
		this.relTol = relTol;
	}

}
