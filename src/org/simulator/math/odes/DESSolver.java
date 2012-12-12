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

import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.odes.MultiTable.Block;

/**
 * A {@link DESSolver} provides algorithm for the numerical simulation of given
 * {@link DESystem}s.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9 
 */
public interface DESSolver extends Cloneable, Serializable {

	/**
	 * Add PropertyChangedListener to this Solver
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * 
	 * @return the cloned solver
	 */
	public DESSolver clone();

	/**
	 * Tell each listener that property value changed. OldValue and newValue are
	 * the old and current time point of simulation, respectively.
	 * 
	 * @param oldValue
	 * @param newValue
	 */
	public void firePropertyChange(double oldValue, double newValue);

	/**
	 * For details about the Kinetic Simulation Algorithm Ontology (KiSAO) see
	 * <a href="http://biomodels.net/kisao/">http://biomodels.net/kisao/</a>.
	 * 
	 * @return the KiSAO term of the algorithm
	 */
	public int getKiSAOterm();

	/**
	 * Obtain the currently set integration step size.
	 * 
	 * @return the step size
	 */
	public double getStepSize();

	/**
	 * If this method returns {@code true}, intermediate results that may
	 * originate from a {@link RichDESystem} are included into the
	 * {@link MultiTable} that contains the result of a numerical integration.
	 * 
	 * @return the flag
	 */
	public boolean isIncludeIntermediates();

	/**
	 * Method to check whether the solution of the numerical integration
	 * procedure contains {@link Double#NaN} values.
	 * 
	 * @return the unstable flag
	 */
	public boolean isUnstable();

	/**
	 * remove PropertyChangedListener to this Solver
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Allows switching whether or not intermediate results should be included
	 * into the {@link MultiTable} of the result.
	 * 
	 * @param includeIntermediates
	 *        if {@code true}, intermediate results are included into the
	 *        result.
	 */
	public void setIncludeIntermediates(boolean includeIntermediates);

	/**
	 * Set the integration step size.
	 * 
	 * @param stepSize
	 */
	public void setStepSize(double stepSize);

	/**
	 * Solves the given differential equation system 
	 * 
	 * @param DES
	 *            The differential equation system to be solved.
	 * @param initialValues
	 *            Return value at the start point.
	 * @param timeBegin
	 *            
	 * @param timeEnd
	 *            
	 * @return A matrix containing the simulation results
	 * @throws DerivativeException
	 *             if something's wrong...
	 */
	public MultiTable solve(DESystem DES, double[] initialValues,
			double timeBegin, double timeEnd) throws DerivativeException;

	/**
	 * Solves the given differential equation system with the step size h and
	 * the number of steps as given starting at the value x.
	 * 
	 * @param DES
	 *            The differential equation system to be solved.
	 * @param initialValues
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
	 * @throws DerivativeException
	 *             if something's wrong...
	 */
	public MultiTable solve(DESystem DES, double[] initialValues, double x,
			double h, int steps) throws DerivativeException;

	/**
	 * Solves the given differential equation system with the step size h and
	 * the number of steps as given starting at the value x.
	 * 
	 * @param DES
	 *            The differential equation system to be solved.
	 * @param initialValues
	 *            Return value at the start point.
	 * @param timepoints
	 *           The timepoints for which the result should be returned 
	 * @return A matrix containing the simulation results.
	 * @throws DerivativeException
	 *             if something's wrong...
	 */
	public MultiTable solve(DESystem DES, double[] initialValues,
			double[] timepoints) throws DerivativeException;

	/**
	 * Solves the given {@link DESystem} using new initial conditions in each
	 * time step. The given {@link MultiTable} contains the expected
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
	 * @return A new {@link MultiTable} containing a time series of the
	 *         same dimension as given by the {@link DESystem} and simulated
	 *         values at each time point.
	 * @throws DerivativeException
	 */
	public MultiTable solve(DESystem DES,
			MultiTable.Block timeSeriesInitConditions,
			double[] initialValues) throws DerivativeException;

}
