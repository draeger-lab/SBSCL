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

/**
 * A {@link RichDESystem} is a {@link DESystem} that provides additional
 * information besides the pure rate of change during its evaluation. For
 * instance, such a system also computes intermediate result that might be of
 * interest later on.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-08-31
 * @version $Rev$
 * @since 1.0
 */
public interface RichDESystem extends DESystem {

	/**
	 * This provides the column/row identifiers of all intermediate results: one
	 * identifier per result. This means, the length of the returned
	 * {@link String} array must equal the length of the array returned by
	 * {@link #getAdditionalValues(double, double[])}.
	 * 
	 * @return The identifiers of all intermediate results computed during
	 *         evaluation.
	 */
	public String[] getAdditionalValueIds();

	/**
	 * Computes and/or delivers the intermediate results at the given time and
	 * for the given results from the previous time step.
	 * 
	 * @param t
	 *            The time point for which intermediate results are to be
	 *            computed.
	 * @param Y
	 *            The result vector from the previous time step.
	 * @return An array of intermediate results. Simulators should call this
	 *         method after computation of the next time point ({@see
	 *         #getValue(double, double[], double[])}) to avoid unnecessary
	 *         re-computation. Implementing classes should store interesting
	 *         intermediate results to be accessible in case time and Y are the
	 *         same values than those just used for the actual computation.
	 * @throws IntegrationException
	 *             If the system cannot be solved for the given configuration or
	 *             no intermediate results can be computed in this step.
	 */
	public double[] getAdditionalValues(double t, double[] Y)
			throws IntegrationException;

	/**
	 * Gives the number of intermediate results that are computed by this class.
	 * The number returned here must equal the length of the arrays returned by
	 * the methods {@link #getAdditionalValueIds()} and
	 * {@link #getAdditionalValues(double, double[])}.
	 * 
	 * @return The number of intermediate values that are computed in each time
	 *         step.
	 */
	public int getNumAdditionalValues();

}
