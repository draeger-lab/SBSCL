package org.sbml.simulator.math.odes;

import java.io.Serializable;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

/**
 * A differential equation system describes how to compute the rate of change at
 * a given state of the system.
 * 
 * @author Hannes Planatscher
 * @author Andreas Dr&auml;ger
 * @version 1.0
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
	 * values of Y
	 * 
	 * @param t
	 * @param Y
	 * @return
	 * @throws IntegrationException
	 * @deprecated use getValue(double t, double[] Y, double[] res) to avoid
	 *             array reallocations and gain speed
	 */
	public double[] getValue(double t, double[] Y) throws IntegrationException;

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
}
