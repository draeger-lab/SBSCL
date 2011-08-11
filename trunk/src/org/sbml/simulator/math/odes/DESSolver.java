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

import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.sbml.simulator.math.odes.MultiBlockTable.Block;

/**
 * A {@link DESSolver} provides algorithm for the numerical simulation of given
 * {@link DESystem}s.
 * 
 * @author Andreas Dr&auml;ger
 * @date Sep 10, 2007
 * @version $Rev$
 * @since 1.0 
 */
public interface DESSolver extends Serializable {

	/**
	 * Obtain the currently set integration step size.
	 * 
	 * @return
	 */
	public double getStepSize();

	/**
	 * Method to check whether the solution of the numerical integration
	 * procedure contains {@link Double.NaN} values.
	 * 
	 * @return
	 */
	public boolean isUnstable();

	/**
	 * Set the integration step size.
	 * 
	 * @param stepSize
	 */
	public void setStepSize(double stepSize);

	/**
	 * Solves the given {@link DESystem} using new initial conditions in each
	 * time step. The given {@link MultiBlockTable} contains the expected
	 * solution of the solver at certain time points. The solver has the task to
	 * re-initialize the integration procedure in each given time point using
	 * the initial values from this state.
	 * 
	 * @param DES
	 *            The {@link DESystem} to be simulated.
	 * @param timeSeriesInitConditions
	 *            A time series of initial conditions for each time point. In
	 *            some cases the dimension of the given {@link DESystem} may
	 *            exceed the number of columns in this given time-series. Thus,
	 *            for the initialization of the simulation a full vector of
	 *            initial values is required and must be passed to this method
	 *            as a separate double array.
	 * @param initialValues
	 *            An array of all initial values. This array may exceed the
	 *            number of columns in the given {@link Block} but its length
	 *            must equal the dimension of the given {@link DESystem}.
	 * @return A new {@link MultiBlockTable} containing a time series of the
	 *         same dimension as given by the {@link DESystem} and simulated
	 *         values at each time point.
	 * @throws IntegrationException
	 */
	public MultiBlockTable solve(DESystem DES,
			MultiBlockTable.Block timeSeriesInitConditions,
			double[] initialValues) throws IntegrationException;

	/**
	 * Solves the {@link DESystem} from the begin time till the end time
	 * according to the given step size.
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return
	 * @throws IntegrationException
	 */
	public MultiBlockTable solve(DESystem DES, double[] initialValues,
			double timeBegin, double timeEnd) throws IntegrationException;

	/**
	 * Solves the given differential equation system with the step size h and
	 * the number of steps as given starting at the value x.
	 * 
	 * @param DES
	 *            The differential equation system to be solved.
	 * @param initalValues
	 *            Return value at the start point.
	 * @param x
	 *            Start argument.
	 * @param h
	 *            Step size.
	 * @param steps
	 *            Number of steps.
	 * @return A matrix containing the values of x, x + h, x + h + steps/h... in
	 *         the rows and the columns contain the return values for the
	 *         arguments.
	 * @throws IntegrationException
	 *             if something's wrong...
	 */
	public MultiBlockTable solve(DESystem DES, double[] initalValues, double x,
			double h, int steps) throws IntegrationException;

	/**
	 * Solves the given {@link DESystem} at the given time points using the
	 * currently set integration step size and the given initial values.
	 * 
	 * @param DES
	 * @param initialvalue
	 * @param timepoints
	 * @return
	 * @throws IntegrationException
	 */
	public MultiBlockTable solve(DESystem DES, double[] initialvalue,
			double[] timepoints) throws IntegrationException;

	/**
	 * 
	 * @return
	 */
	public DESSolver clone();
	
	/**
	 * Add PropertyChangedListener to this Solver
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	/**
	 * remove PropertyChangedListener to this Solver
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
	/**
	 * Tell each listener that property value changed. OldValue and newValue are
	 * the old and current time poit of simulation, respectively.
	 * 
	 * @param oldValue
	 * @param newValue
	 */
	public void firePropertyChange(double oldValue, double newValue);
}
