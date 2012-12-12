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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;
import org.simulator.math.Mathematics;
import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * This Class represents an abstract solver for event-driven DES
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @author Hannes Planatscher
 * @author Philip Stevens
 * @author Max Zwie&szlig;ele
 * @version $Rev$
 * @since 0.9
 */
public abstract class AbstractDESSolver implements DelayValueHolder, DESSolver, EventHandler {

	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(AbstractDESSolver.class.getName());
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1859418461410763939L;

	/**
	 * @return the serial version uid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Switches the inclusion of intermediate results on or off. This feature is
	 * important if the given {@link DESystem} is an instance of
	 * {@link RichDESystem}. Setting this switch to false speeds up the
	 * computation and lowers the memory consumption.
	 */
	private boolean includeIntermediates;

	/**
	 * factor describing an interval for the progress of a certain simulation
	 * intervalFactor = 100 / (endTime - startTime)
	 */
	private double intervalFactor;

	/**
	 * {@link List} of {@link PropertyChangeListener}s (for threading purpose)
	 */
	List<PropertyChangeListener> listenerList;

	/**
	 * Flag to indicate whether or not negative values within the solution
	 * should be set to zero.
	 */
	private boolean nonnegative;

	/**
	 * The integration step size.
	 */
	private double stepSize;

	/**
	 * Flag to indicate whether at some time point during the simulation NaN
	 * values occur within the solution.
	 */
	private boolean unstableFlag;

	/**
	 * 
	 */
	private MultiTable data;
	
	/**
	 * A cloned version of this object
	 */
	private AbstractDESSolver clonedSolver;

	/**
	 * Initialize with default integration step size and non-negative attribute
	 * {@code true}.
	 */
	public AbstractDESSolver() {
		stepSize = 0.01d;
		nonnegative = false;
		unstableFlag = false;
		includeIntermediates = true;
		this.intervalFactor = 0d;
		this.listenerList = new LinkedList<PropertyChangeListener>();
	}

	/**
	 * Initialize with default integration step size and non-negative attribute
	 * {@code true}.
	 */
	public void reset() {
		stepSize = 0.01d;
		nonnegative = false;
		unstableFlag = false;
		includeIntermediates = true;
		this.intervalFactor = 0d;
		this.listenerList = new LinkedList<PropertyChangeListener>();
	}


	/**
	 * Clone constructor.
	 * 
	 * @param solver
	 */
	public AbstractDESSolver(AbstractDESSolver solver) {
		this(solver.getStepSize(), solver.isNonnegative());
		setIncludeIntermediates(solver.isIncludeIntermediates());
		unstableFlag = solver.isUnstable();
	}

	/**
	 * Initialize with given integration step size.
	 * 
	 * @param stepSize
	 */
	public AbstractDESSolver(double stepSize) {
		this();
		setStepSize(stepSize);
	}

	/**
	 * Initialize with given step size and a flag whether or not negative values should be allowed.
	 * @param stepSize
	 * @param nonnegative
	 */
	public AbstractDESSolver(double stepSize, boolean nonnegative) {
		this(stepSize);
		setNonnegative(nonnegative);
	}

	/**
	 * Compute additional result values
	 * 
	 * @param DES
	 *            the differential equation system
	 * @param t
	 *            the current time
	 * @param yTemp
	 *            the vector yTemp
	 * @param data
	 *            the data as multi table
	 * @param rowIndex
	 *            the index of the row
	 * @return an array of additional (intermediate) results.
	 * @throws DerivativeException
	 */
	protected double[] additionalResults(DESystem DES, double t, double[] yTemp,
			MultiTable data, int rowIndex) throws DerivativeException {
		if (includeIntermediates && (DES instanceof RichDESystem)) {
			MultiTable.Block block = data.getBlock(1);
			double v[] = ((RichDESystem) DES).getAdditionalValues(t, yTemp).clone();
			block.setRowData(rowIndex, v);
			return v;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#addPropertyChangedListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!listenerList.contains(listener)) {
			this.listenerList.add(listener);
		}
	}

	/**
	 * If option nonnegative is set all elements of the given vector smaller
	 * than zero are set to zero.
	 * 
	 * @param yTemp the vector yTemp
	 */
	private void checkNonNegativity(double[] yTemp) {
		if (nonnegative) {
			for (int k = 0; k < yTemp.length; k++) {
				if (yTemp[k] < 0) {
					yTemp[k] = 0;
				}
			}
		}
	}

	/**
	 * Checks whether or not the given current state contains {@link Double#NaN} values.
	 * In this case the solution of the current state is considered unstable and
	 * the corresponding flag of the solver will be set accordingly.
	 * 
	 * @param currentState
	 *            The current state of the system during a simulation.
	 * @return flag that is true if {@link Double#NaN} values are contained
	 */
	boolean checkSolution(double[] currentState) {
		for (int k = 0; k < currentState.length; k++) {
			if (Double.isNaN(currentState[k])) {
				unstableFlag = true;
				currentState[k] = 0;
			}
		}
		return !unstableFlag;
	}

	/**
	 * Checks whether or not the given current state contains {@link Double#NaN}
	 * values. In this case the solution of the current state is considered
	 * unstable and the corresponding flag of the solver will be set
	 * accordingly.
	 * 
	 * @param currentChange
	 *            the current change of the system during a simulation.
	 * @param yPrev
	 *            the previous state of the system
	 * @return flag that is true if {@link Double#NaN} values are contained in
	 *         the change vector that have not been contained at the
	 *         corresponding position in the previous state
	 */
	boolean checkSolution(double[] currentChange, double[] yPrev) {
		for (int k = 0; k < currentChange.length; k++) {
			if (Double.isNaN(currentChange[k])) {
				if (!Double.isNaN(yPrev[k]) &&!Double.isInfinite(yPrev[k])) {
					unstableFlag = true;
				}
				else if (Double.isInfinite(yPrev[k])) {
					currentChange[k]=0;
				}
			}
		}
		return !unstableFlag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public abstract AbstractDESSolver clone();

	/**
	 * Computes the change for a given system at the current time with the
	 * current setting for the integration step size.
	 * 
	 * @param DES
	 *            The system to be simulated.
	 * @param y
	 *            The current state of the system.
	 * @param t
	 *            The current simulation time.
	 * @param stepSize 
	 *            The current integration step size.
	 * @param change
	 *            The vector for the resulting change of the system.
	 * @param steadyState 
	 * @return The change.
	 * @throws Exception
	 */
	public abstract double[] computeChange(DESystem DES, double[] y, double t,
			double stepSize, double[] change, boolean steadyState) throws DerivativeException;

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DelayValueHolder#computeValue(double, java.lang.String)
	 */
	public double computeDelayedValue(double time, String id) {
		//get interval
		double[] timepoints=data.getTimePoints();
		int leftIndex=-1;
		int rightIndex=-1;
		for(int i=0;i!=timepoints.length;i++) {
			if (timepoints[i]>=time) {
				rightIndex=i;
				if ((i>0) && (timepoints[i]>time)) {
					leftIndex=i-1;
				}
				break;
			}
		}
		if ((leftIndex==-1) && (rightIndex==-1)) {
			leftIndex=timepoints.length-1;
		}

		//get values and do an interpolation if necessary
		double leftValue = Double.NaN;
		double rightValue = Double.NaN;

		Column c=data.getColumn(id);
		if (leftIndex!=-1) {
			leftValue=c.getValue(leftIndex);
		}
		if (rightIndex!=-1) {
			rightValue=c.getValue(rightIndex);
		}

		if (leftIndex==-1) {
			return rightValue;
		}
		else if (rightIndex==-1) {
			return leftValue;
		}
		else {
			return leftValue + (rightValue-leftValue)*((time-timepoints[leftIndex])/(timepoints[rightIndex]-timepoints[leftIndex]));
		}
	}

	/**
	 * 
	 * @param DES
	 *            the differential equation system
	 * @param t
	 *            the current time
	 * @param stepSize
	 *            stepSize
	 * @param yPrev
	 *            the previous y vector
	 * @param change
	 *            the change vector
	 * @param yTemp
	 *            the current y vector to be filled
	 * @param increase
	 *            whether or not to increase the given time by the given step
	 *            size.
	 * @param steadyState
	 * @return the time increased by the step size
	 * @throws DerivativeException
	 */
	double computeNextState(DESystem DES, double t, double stepSize,
			double[] yPrev, double[] change, double[] yTemp, boolean increase, boolean steadyState)
					throws DerivativeException {
		double previousTime=t;
		computeChange(DES, yPrev, t, stepSize, change, steadyState);
		checkSolution(change, yPrev);
		Mathematics.vvAdd(yPrev, change, yTemp);
		checkNonNegativity(yTemp);
		if (increase) {
			t = BigDecimal.valueOf(stepSize).add(BigDecimal.valueOf(t)).doubleValue();
		}
		if(!steadyState) {
			processEventsAndRules(false, DES, t, previousTime, yTemp);
		}
		return t;
	}

	/**
	 * 
	 * @param DES
	 *            the differential equation system
	 * @param result
	 *            the result vector
	 * @param timeBegin
	 *            the current time
	 * @return the computed steady state
	 * @throws DerivativeException
	 */
	protected double[] computeSteadyState(FastProcessDESystem DES,
			double[] result, double timeBegin) throws DerivativeException {
		double[] oldValues = new double[result.length];
		double[] newValues = new double[result.length];
		double[] change = new double[result.length];
		System.arraycopy(result, 0, newValues, 0, result.length);
		double ft = timeBegin;
		((FastProcessDESystem) DES).setFastProcessComputation(true);

		// TODO what if there is oscillation, so no state with no change will be
		// reached
		int step=0;
		while (!noChange(oldValues, newValues, step)) {
			System.arraycopy(newValues, 0, oldValues, 0, newValues.length);
			ft = computeNextState(DES, ft, stepSize, oldValues, change,
					newValues, true, true);
			step++;
		}
		((FastProcessDESystem) DES).setFastProcessComputation(false);
		return oldValues;

	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math.ode.events.EventHandler#eventOccurred(double, double[], boolean)
	 */
	public int eventOccurred(double t, double[] y, boolean increasing)
			throws EventException {
		return STOP;
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#firePropertyChanged(double, double)
	 */
	public void firePropertyChange(double oldValue, double newValue /*, double[] currResult, double[] additionalResult*/) {
		if (!this.listenerList.isEmpty()) {
			PropertyChangeEvent evt = new PropertyChangeEvent(this, "progress",
					oldValue, newValue);
			// logger.info(String.format("Progress: %s %%", StringTools.toString(newValue)));
			for (PropertyChangeListener listener : this.listenerList) {
				listener.propertyChange(evt);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math.ode.events.EventHandler#g(double, double[])
	 */
	public double g(double t, double[] y) throws EventException {
		if (Double.isNaN(t)) {
			return -1d;
		}
		return checkSolution(y) ? 1d : -1d;
	}

	/**
	 * This gives a human-readable name of this solver that can be displayed in
	 * a graphical user interface.
	 * 
	 * @return A name that describes the underlying algorithm.
	 */
	public abstract String getName();

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#getStepSize()
	 */
	public double getStepSize() {
		return this.stepSize;
	}

	/**
	 * 
	 * @return Does the solver do the event processing itself?
	 */
	protected abstract boolean hasSolverEventProcessing();

	/**
	 * Computes the number of necessary steps between two time steps.
	 * 
	 * @param lastTime
	 * @param nextTime
	 * @param stepSize
	 * @return steps the number of steps between the given time points
	 */
	public int inBetweenSteps(double lastTime, double nextTime, double stepSize) {
		return (int) (Math.floor((nextTime - lastTime) / stepSize) /* + 1 */);
	}

	/**
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return table the initialized {@link MultiTable}
	 */
	protected MultiTable initResultMatrix(DESystem DES,
			double initialValues[], double timeBegin, double timeEnd) {
		return initResultMatrix(DES, initialValues, timeBegin, numSteps(
				timeBegin, timeEnd));
	}

	/**
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param numSteps
	 * @return table the initialized {@link MultiTable}
	 */
	protected MultiTable initResultMatrix(DESystem DES,
			double[] initialValues, double timeBegin, int numSteps) {
		int dim = DES.getDimension();
		if (dim != initialValues.length) {
			throw new IllegalArgumentException(
					"The number of initial values must equal the dimension of the DE system.");
		}
		double timePoints[] = new double[numSteps];
		for (int i = 0; i < timePoints.length; i++) {
			timePoints[i] = BigDecimal.valueOf(timeBegin).add(BigDecimal.valueOf(i).multiply(BigDecimal.valueOf(stepSize))).doubleValue();
		}
		return initResultMatrix(DES, initialValues, timePoints);
	}

	/**
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timePoints
	 * @return table the initialized {@link MultiTable}
	 */
	protected MultiTable initResultMatrix(DESystem DES,
			double[] initialValues, double[] timePoints) {
		double result[][] = new double[timePoints.length][initialValues.length];
		System.arraycopy(initialValues, 0, result[0], 0, initialValues.length);
		data = new MultiTable(timePoints, result, DES
				.getIdentifiers());
		data.getBlock(0).setName("Values");
		if (includeIntermediates && (DES instanceof RichDESystem)) {
			data.addBlock(((RichDESystem) DES).getAdditionalValueIds());
			data.getBlock(data.getBlockCount() - 1).setName("Additional values");
		}
		unstableFlag = false;
		return data;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#isIncludeIntermediates()
	 */
	public boolean isIncludeIntermediates() {
		return includeIntermediates;
	}

	/**
	 * @return the nonnegative flag
	 */
	public boolean isNonnegative() {
		return nonnegative;
	}

	/**
	 * @return the unstableFlag
	 */
	public boolean isUnstable() {
		return unstableFlag;
	}

	/**
	 * 
	 * @param newValues
	 * @param oldValues
	 * @param step 
	 * @return
	 */
	private boolean noChange(double newValues[], double oldValues[], int step) {
		for (int i = 0; i < newValues.length; i++) {
			double distance = Math.abs(newValues[i]-oldValues[i]);
			double relativeDistance = 0;
			if(!(newValues[i] == 0) || !(oldValues[i] == 0)) {
				relativeDistance = Math.abs((newValues[i]-oldValues[i])/Math.max(newValues[i],oldValues[i]));
			}
			if(((distance > 1E-5) || (relativeDistance > 1E-5)) &&  (step < 10000)) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Calculates and returns the number of steps for given start and end time
	 * using the currently set interval size of integration steps.
	 * 
	 * @param timeBegin
	 * @param timeEnd
	 * @return
	 */
	int numSteps(double timeBegin, double timeEnd) {
		if (timeBegin > timeEnd) {
			throw new IllegalArgumentException(
					"End time point must be greater than start time point.");
		}
		return (int) Math.round(((timeEnd - timeBegin) / stepSize) + 1);
	}

	/**
	 * Processes sudden changes in the system due to events in the EDES
	 * 
	 * @param EDES
	 *            the differential equation system with events
	 * @param time
	 *            the current time
	 * @param previousTime
	 *            the time this function has been called previously
	 * @param yTemp
	 *            the vector Ytemp
	 * @return a flag that is true if an event has been fired
	 * @throws DerivativeException
	 */
	public boolean processEvents(EventDESystem EDES, double time, double previousTime, double[] yTemp)
			throws DerivativeException {
		boolean hasNewEvents=false;
		EventInProgress event;
		event = EDES.getNextEventAssignments(
				time, previousTime, yTemp);

		if (event != null) {
			hasNewEvents=true;
		}
		while ((event != null) && ((event.getLastTimeExecuted() == time) || (event.getFireStatus(time)))) {
			if ((EDES instanceof FastProcessDESystem)) {
				FastProcessDESystem FDES = (FastProcessDESystem) EDES;
				if(FDES.containsFastProcesses()) {
					double[] yTemp2 = new double[yTemp.length];
					System.arraycopy(yTemp, 0, yTemp2, 0, yTemp.length);
					if(clonedSolver == null) {
						clonedSolver = this.clone();
					}
					double[] result = clonedSolver.computeSteadyState(FDES,
							yTemp2, 0);
					System.arraycopy(result, 0, yTemp, 0, yTemp.length);
				}
			}
			
			for (int index: event.getAssignments().keySet()) {
				yTemp[index] = event.getAssignments().get(index);
			}

			event = EDES.getNextEventAssignments(
					time, previousTime, yTemp);
		}
		return hasNewEvents;

	}

	/**
	 * Function for processing the events and rules at a certain time step.
	 * 
	 * @param forceProcessing
	 *            flag that is true if the events should be processed even if
	 *            the solver has its own event processing
	 * @param DES
	 *            the differential equation system with events
	 * @param t
	 *            the current time
	 * @param previousTime
	 *            the time this function has been called previously
	 * @param yTemp
	 *            the vector Ytemp
	 * @return a flag that is true if there has been a change caused by a rule
	 *         or an event has been fired
	 * @throws DerivativeException
	 */
	public boolean processEventsAndRules(boolean forceProcessing, DESystem DES, double t, double previousTime, double yTemp[])
			throws DerivativeException {
		boolean change=false;
		if (DES instanceof EventDESystem) {
			EventDESystem EDES = (EventDESystem) DES;
			if (EDES.getRuleCount() > 0) {
				processRules(EDES, t, yTemp);
			}
			if ((forceProcessing || (!this.hasSolverEventProcessing())) && (EDES.getEventCount() > 0)) {
				change=processEvents(EDES, t, previousTime, yTemp);
			}
			if (EDES.getRuleCount() > 0) {
				processRules(EDES, t, yTemp);
			}
		}
		return change;
	}

	/**
	 * Function for processing the rules at a certain time step.
	 * 
	 * @param EDES
	 *            the differential equation system with events
	 * @param time
	 *            the current time
	 * @param Ytemp
	 *            the vector Ytemp
	 * @return a flag that is true if there has been a change caused by a rule
	 * @throws DerivativeException
	 */
	public boolean processRules(EventDESystem EDES, double time, double[] Ytemp)
			throws DerivativeException {
		return EDES.processAssignmentRules(time, Ytemp);
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#removePropertyChangedListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listenerList.contains(listener)) {
			this.listenerList.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math.ode.events.EventHandler#resetState(double, double[])
	 */
	public void resetState(double t, double[] y) throws EventException {
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#setIncludeIntermediates(boolean)
	 */
	public void setIncludeIntermediates(boolean includeIntermediates) {
		this.includeIntermediates = includeIntermediates;
	}

	/**
	 * @param nonnegative
	 *            the nonnegative to set
	 */
	public void setNonnegative(boolean nonnegative) {
		this.nonnegative = nonnegative;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#setStepSize(double)
	 */
	public void setStepSize(double stepSize) {
		if (stepSize < Double.MIN_VALUE) {
			throw new IllegalArgumentException(
					"The integration step size must be a positive, non-zero value.");
		}
		this.stepSize = stepSize;
	}

	/**
	 * @param unstableFlag
	 */
	public void setUnstableFlag(boolean unstableFlag) {
		this.unstableFlag = unstableFlag;
	}

	/**
	 * 
	 * @param DES the differential equation system
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @return result as multi table
	 */
	public MultiTable solve(DESystem DES, double[] initialValues,
			double timeBegin, double timeEnd) throws DerivativeException {
		if (DES instanceof DelayedDESystem) {
			((DelayedDESystem)DES).registerDelayValueHolder(this);
		}
		this.intervalFactor = 100d / (timeEnd - timeBegin);
		MultiTable data = initResultMatrix(DES, initialValues, timeBegin,
				timeEnd);
		double result[][] = data.getBlock(0).getData();
		double change[] = new double[initialValues.length];
		double yTemp[] = new double[initialValues.length];
		double yPrev[] = new double[initialValues.length];
		double t = timeBegin;
		double v[] = additionalResults(DES, t, result[0], data, 0);
		boolean fastFlag = false;

		if (DES instanceof FastProcessDESystem) {
			fastFlag = ((FastProcessDESystem) DES).containsFastProcesses();
		}

		if (fastFlag) {
			result[0] = computeSteadyState(((FastProcessDESystem) DES),
					result[0], timeBegin);
		}

		// execute events that trigger at 0.0
		processEvents((EventDESystem) DES, 0d, 0d, result[0]);
		System.arraycopy(result[0], 0, yTemp, 0, yTemp.length);
		for (int i = 1; (i < result.length)
				&& (!Thread.currentThread().isInterrupted()); i++) {
			double oldT = t;
			System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
			t = computeNextState(DES, t, stepSize, yPrev, change,
					yTemp, true, false);
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			if (i == 1) {
				System.arraycopy(yPrev, 0, result[0], 0, yPrev.length);
			}

			if (fastFlag) {
				yTemp = computeSteadyState(((FastProcessDESystem) DES),
						result[i], timeBegin);
				System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			}
			v = additionalResults(DES, t - stepSize, result[i - 1], data, i);
			firePropertyChange(oldT * intervalFactor, t * intervalFactor);
		}

		return data;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#solve(org.simulator.math.odes.DESystem, double[], double, double, int)
	 */
	public MultiTable solve(DESystem DES, double[] initialValues,
			double x, double h, int steps) throws DerivativeException {
		double[] timeVector = new double[steps];
		for (int i = 0; i < steps; i++) {	
			timeVector[i] = x + i * h;
		}
		return solve(DES, initialValues, timeVector);
	}

	/**
	 * 
	 * @param DES
	 *            differential equation system
	 * @param initialValues
	 * @param timePoints
	 *            the time points
	 * @return result as a multi table
	 */
	public MultiTable solve(DESystem DES, double[] initialValues,
			double[] timePoints) throws DerivativeException {
		if (DES instanceof DelayedDESystem) {
			((DelayedDESystem)DES).registerDelayValueHolder(this);
		}
		MultiTable data = initResultMatrix(DES, initialValues, timePoints);
		double result[][] = data.getBlock(0).getData();
		double change[] = new double[initialValues.length];
		double yPrev[] = new double[initialValues.length];
		double yTemp[] = new double[initialValues.length];
		double steady[] = new double[initialValues.length];
		double t = timePoints[0];
		double h = stepSize;

		boolean fastFlag = false;

		double v[] = additionalResults(DES, t, result[0], data, 0);

		if (DES instanceof FastProcessDESystem) {
			fastFlag = ((FastProcessDESystem) DES).containsFastProcesses();
		}

		if (fastFlag) {
			result[0] = computeSteadyState(((FastProcessDESystem) DES),
					result[0], timePoints[0]);
		}

		// execute events that trigger at 0.0
		processEvents((EventDESystem) DES, 0d, 0d, result[0]);
		System.arraycopy(result[0], 0, yTemp, 0, result[0].length);

		for (int i = 1; (i < timePoints.length)
				&& (!Thread.currentThread().isInterrupted()); i++) {
			
			h = stepSize;

			// h = h / 10;
			int steps=inBetweenSteps(timePoints[i - 1],
					timePoints[i], h);
			for(int j=1;j<=steps;j++) {
				System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
				t = computeNextState(DES, t, h, yPrev, change, yTemp, true, false);
				if ((i==1) && (j==1)) {
					System.arraycopy(yPrev, 0, result[0], 0, yPrev.length);
				}
			}
			h = BigDecimal.valueOf(timePoints[i]).subtract(BigDecimal.valueOf(t)).doubleValue();
			if (h>1E-14) {
				System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
				t = computeNextState(DES, t, h, yTemp, change, yTemp, true, false);
			}
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			if (fastFlag) {
				steady = computeSteadyState(((FastProcessDESystem) DES),
						result[i], timePoints[0]);
				System.arraycopy(steady, 0, result[i], 0, yTemp.length);
				System.arraycopy(steady, 0, yTemp, 0, yTemp.length);
			}

			v = additionalResults(DES, t, yTemp, data, i);
			firePropertyChange(timePoints[i-1] * intervalFactor, timePoints[i] * intervalFactor);

			t = timePoints[i];
		}
		return data;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.DESSolver#solve(org.simulator.math.odes.DESystem, org.simulator.math.odes.MultiTable.Block, double[])
	 */
	public MultiTable solve(DESystem DES,
			MultiTable.Block initConditions, double[] initialValues)
					throws DerivativeException {
		if (DES instanceof DelayedDESystem) {
			((DelayedDESystem)DES).registerDelayValueHolder(this);
		}
		double[] timePoints = initConditions.getTimePoints();
		// of items to be simulated, this will cause a problem!

		HashMap<String, Integer> idIndex = new HashMap<String, Integer>();
		HashSet<String> missingIds = new HashSet<String>();
		int i, j, k;
		String ids[] = DES.getIdentifiers();
		for (i = 0; i < ids.length; i++) {
			if (!initConditions.containsColumn(ids[i])) {
				missingIds.add(ids[i]);
			}
			idIndex.put(ids[i], Integer.valueOf(i));
		}

		for (int col = 0; col < initConditions.getColumnCount(); col++) {
			String columnName=initConditions.getColumnIdentifier(col);
			if (columnName!=null) {
				Integer index=idIndex.get(initConditions.getColumnIdentifier(col));
				if (index!=null) {
					initialValues[index.intValue()] = initConditions
							.getValueAt(0, col + 1);
				}
			}
		}

		MultiTable data = initResultMatrix(DES, initialValues, timePoints);

		double[][] result = data.getBlock(0).getData();
		double[] yTemp = new double[DES.getDimension()];
		double[] change = new double[DES.getDimension()];
		double t = timePoints[0];
		double v[] = additionalResults(DES, t, result[0], data, 0);
		for (i = 1; (i < timePoints.length) && (!Thread.currentThread().isInterrupted()); i++) {
			double h = stepSize;
			if (!missingIds.isEmpty()) {
				for (k = 0; k < initConditions.getColumnCount(); k++) {
					yTemp[idIndex.get(initConditions.getColumnIdentifier(k)).intValue()] = initConditions.getValueAt(i - 1, k + 1);
				}
				for (String key : missingIds) {
					k = idIndex.get(key).intValue();
					yTemp[k] = result[i - 1][k];
				}
			} else {
				System.arraycopy(initConditions.getRow(i - 1), 0, yTemp, 0,
						yTemp.length);
			}
			for (j = 0; j < inBetweenSteps(timePoints[i - 1], timePoints[i], h); j++) {
				computeChange(DES, yTemp, t, h, change, false);
				checkSolution(change, yTemp);
				Mathematics.vvAdd(yTemp, change, yTemp);
				t = BigDecimal.valueOf(h).add(BigDecimal.valueOf(t)).doubleValue();
			}
			h = BigDecimal.valueOf(timePoints[i]).subtract(BigDecimal.valueOf(t)).doubleValue();
			if (h > 1E-14d) {
				computeChange(DES, yTemp, t, h, change, false);
				checkSolution(change);
				Mathematics.vvAdd(yTemp, change, yTemp);
			}
			checkNonNegativity(yTemp);
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);

			v = additionalResults(DES, t, yTemp, data, i);
			firePropertyChange(timePoints[i-1] * intervalFactor, timePoints[i] * intervalFactor);
			
			t = timePoints[i];
		}

		return data;
	}

}
