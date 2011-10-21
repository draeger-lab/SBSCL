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

import java.io.Serializable;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

/**
 * A differential equation system describes how to compute the rate of change at
 * a given state of the system.
 * 
 * @author Hannes Planatscher
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */

public interface DESystem extends Serializable, FirstOrderDifferentialEquations {

	/**
	 * Returns the number of dimensions of this ODE system.
	 * 
	 * @return Returns the number of dimensions of this ODE system.
	 */
	public int getDESystemDimension();

	/**
	 * Delivers an array of {@link String}s that describe the content of each
	 * dimension of the resulting array of this {@link DESystem}.
	 * 
	 * @return An array of {@link String}s which has the same length than the
	 *         number given by {@link #getDESystemDimension()}. Each
	 *         {@link String} describes the content of the given dimension.
	 */
	public String[] getIdentifiers();

	
	
	/**
	 * Returns the value of the ODE system at the time t given the current
	 * values of Y within resultVector.
	 * 
	 * @param t
	 * @param Y
	 * @param resultVector
	 * @throws IntegrationException
	 */
	public void getValue(double t, double[] Y, double[] resultVector)
			throws IntegrationException;

	/**
	 * 
	 * @return
	 */
  public boolean containsEventsOrRules();

  /**
   * 
   * @return the number of values in Y that have to be positive (concentrations, volumes, stoichiometries)
   */
  public int getNumPositiveValues();
}
