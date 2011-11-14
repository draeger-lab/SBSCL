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
package org.simulator.math.odes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;
import org.simulator.math.Mathematics;

/**
 * This Class represents an Solver for event-driven DES
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @author Philip Stevens
 * @author Max Zwießele
 * @date 2010-02-04
 * @version $Rev$
 * @since 0.9
 */
public abstract class AbstractDESSolver implements DESSolver, EventHandler {

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
	 * factor describing an interval for the progress of a certain simulation
	 * intervalFactor = 100 / (endTime - startTime)
	 */
	private double intervalFactor;

	/**
	 * list of propertychange listeners (for threading purpose)
	 */
	ArrayList<PropertyChangeListener> listenerList;

	/**
	 * Initialize with default integration step size and non-negative attribute
	 * true.
	 */
	public AbstractDESSolver() {
		stepSize = 0.01;
		nonnegative = false;
		unstableFlag = false;
		includeIntermediates = false;
		this.intervalFactor = 0d;
		this.listenerList = new ArrayList<PropertyChangeListener>();
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
	 * 
	 * @param stepSize
	 * @param nonnegative
	 */
	public AbstractDESSolver(double stepSize, boolean nonnegative) {
		this(stepSize);
		setNonnegative(nonnegative);
	}

	/**
	 * If option nonnegative is set all elements of the given vector smaller
	 * than zero are set to zero.
	 * 
	 * @param yTemp
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
	 * Checks whether or not the given current state contains Double.NaN values.
	 * In this case the solution of the current state is considered unstable and
	 * the corresponding flag of the solver will be set accordingly.
	 * 
	 * @param currentState
	 *            The current state of the system during a simulation.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
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
	 * @param The
	 *            current integration step size.
	 * @param change
	 *            The vector for the resulting change of the system.
	 * @return The change.
	 * @throws Exception
	 */
	public abstract double[] computeChange(DESystem DES, double[] y, double t,
			double stepSize, double[] change) throws IntegrationException;

	/**
	 * 
	 * @param DES
	 * @param t
	 * @param stepSize
	 * @param yPrev
	 * @param change
	 * @param yTemp
	 * @param increase
	 *            whether or not to increase the given time by the given step
	 *            size.
	 * @return The time increased by the step size
	 * @throws IntegrationException
	 */
	double computeNextState(DESystem DES, double t, double stepSize,
			double[] yPrev, double[] change, double[] yTemp, boolean increase)
			throws IntegrationException {
	  double previousTime=t;
	  computeChange(DES, yPrev, t, stepSize, change);
		checkSolution(change);
		Mathematics.vvAdd(yPrev, change, yTemp);
		checkNonNegativity(yTemp);
		if (increase) {
		  t = BigDecimal.valueOf(stepSize).add(BigDecimal.valueOf(t)).doubleValue();
		}
		processEventsAndRules(DES, t, previousTime,yTemp);

		return t;
	}

	/**
	 * This gives a human-readable name of this solver that can be displayed in
	 * a graphical user interface.
	 * 
	 * @return A name that describes the underlying algorithm.
	 */
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.DESSolver#getStepSize()
	 */
	public double getStepSize() {
		return this.stepSize;
	}

	/**
	 * Computes the number of necessary steps between two time steps.
	 * 
	 * @param lastTime
	 * @param nextTime
	 * @param stepSize
	 * @return
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
	 * @return
	 */
	protected MultiBlockTable initResultMatrix(DESystem DES,
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
	 * @return
	 */
	protected MultiBlockTable initResultMatrix(DESystem DES,
			double[] initialValues, double timeBegin, int numSteps) {
		int dim = DES.getDESystemDimension();
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
	 * @return
	 */
	protected MultiBlockTable initResultMatrix(DESystem DES,
			double[] initialValues, double[] timePoints) {
		double result[][] = new double[timePoints.length][initialValues.length];
		System.arraycopy(initialValues, 0, result[0], 0, initialValues.length);
		MultiBlockTable data = new MultiBlockTable(timePoints, result, DES
				.getIdentifiers());
		data.getBlock(0).setName("Values");
		if (includeIntermediates && (DES instanceof RichDESystem)) {
			data.addBlock(((RichDESystem) DES).getAdditionalValueIds());
			data.getBlock(data.getBlockCount() - 1)
					.setName("Additional values");
		}
		unstableFlag = false;
		return data;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isIncludeIntermediates() {
		return includeIntermediates;
	}

	/**
	 * @return the nonnegative
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
	 * @param time
	 * @param Ytemp
	 * @param change
	 * @return
	 * 
	 * @throws IntegrationException
	 */
	public void processEvents(EventDESystem EDES, double time, double previousTime, double[] yTemp)
			throws IntegrationException {
		int index;
		List<DESAssignment> assignments;
		assignments = (LinkedList<DESAssignment>) EDES.getEventAssignments(
				time, previousTime, yTemp);

		while (assignments != null) {
			for (DESAssignment assignment : assignments) {
				index = assignment.getIndex();
				yTemp[index] = assignment.getValue();
			}

			assignments = (LinkedList<DESAssignment>) EDES.getEventAssignments(
					time, time, yTemp);
		}

	}

	/**
	 * 
	 * @param DES
	 * @param t
	 * @param yTemp
	 * @param change
	 * @throws IntegrationException
	 */
	public void processEventsAndRules(DESystem DES, double t, double previousTime, double yTemp[])
			throws IntegrationException {
		if (DES instanceof EventDESystem) {
			EventDESystem EDES = (EventDESystem) DES;
			if (EDES.getNumEvents() > 0) {
				processEvents(EDES, t, previousTime, yTemp);
			}
			if (EDES.getNumRules() > 0) {
				processRules(EDES, t, yTemp);
			}
		}
	}

	/**
	 * @param EDES
	 * @param time
	 * @param Ytemp
	 * @return
	 * @throws IntegrationException
	 */
	public void processRules(EventDESystem EDES, double time, double[] Ytemp)
			throws IntegrationException {
		for (DESAssignment assignment : EDES
				.processAssignmentRules(time, Ytemp)) {
			Ytemp[assignment.getIndex()] = assignment.getValue();
		}
	}

	/**
	 * 
	 * @param DES
	 * @param t
	 * @param yTemp
	 * @param data
	 * @param rowIndex
	 * @throws IntegrationException
	 */
	protected void additionalResults(DESystem DES, double t, double[] yTemp,
			MultiBlockTable data, int rowIndex) throws IntegrationException {
		if (includeIntermediates && (DES instanceof RichDESystem)) {
			MultiBlockTable.Block block = data.getBlock(1);
			double v[] = ((RichDESystem) DES).getAdditionalValues(t, yTemp);
			block.setRowData(rowIndex, v.clone());
		}
	}

	/**
	 * 
	 * @param includeIntermediates
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.DESSolver#setStepSize(double)
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
	 * @param DES
	 * @param initialValues
	 * @param timeBegin
	 * @param timeEnd
	 * @param includeTimes
	 *            Switch to whether or not include a time column in the
	 *            resulting matrix.
	 * @return
	 */
	public MultiBlockTable solve(DESystem DES, double[] initialValues,
			double timeBegin, double timeEnd) throws IntegrationException {
		this.intervalFactor = 100d / (timeEnd - timeBegin);
		MultiBlockTable data = initResultMatrix(DES, initialValues, timeBegin,
				timeEnd);
		double result[][] = data.getBlock(0).getData();
		double change[] = new double[initialValues.length];
		double yTemp[] = new double[initialValues.length];
		double yPrev[] = new double[initialValues.length];
		double t = timeBegin;
		additionalResults(DES, t, result[0], data, 0);
		boolean fastFlag = false;

		if (DES instanceof FastProcessDESystem) {
			fastFlag = ((FastProcessDESystem) DES).containsFastProcesses();
		}

		if (fastFlag) {
			result[0] = computeSteadyState(((FastProcessDESystem) DES),
					result[0], timeBegin);
		}

		// execute events that trigger at 0.0
		processEvents((EventDESystem) DES, 0.0, 0.0,result[0]);
		System.arraycopy(result[0], 0, yTemp, 0, yTemp.length);
		for (int i = 1; i < result.length; i++) {
			double oldT = t;
			System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
			t = computeNextState(DES, t, stepSize, yPrev, change,
					yTemp, true);
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			if(i==1) {
			  System.arraycopy(yPrev, 0, result[0], 0, yPrev.length);
			}
			firePropertyChange(oldT * intervalFactor, t * intervalFactor);

			if (fastFlag) {
				yTemp = computeSteadyState(((FastProcessDESystem) DES),
						result[i], timeBegin);
				System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			}
			additionalResults(DES, t - stepSize, result[i - 1], data, i);
		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.DESSolver#solve(eva2.tools.math.des.DESystem,
	 * double[], double, double, int)
	 */
	public MultiBlockTable solve(DESystem DES, double[] initialValues,
			double x, double h, int steps) throws IntegrationException {
		double[] timeVector = new double[steps];
		for (int i = 0; i < steps; i++) {	
			timeVector[i] = x + i * h;
		}
		return solve(DES, initialValues, timeVector);
	}

	/**
	 * When set to <code>TRUE</code>, <code>includeTimes</code> will make the
	 * solver to return a matrix with the first column containing the times. By
	 * default the result of the ODE solver just returns the values for Y.
	 * 
	 * @param DES
	 * @param initialValues
	 * @param timePoints
	 *            Sorted time points!!
	 * @throws Exception
	 */
	public MultiBlockTable solve(DESystem DES, double[] initialValues,
			double[] timePoints) throws IntegrationException {
		MultiBlockTable data = initResultMatrix(DES, initialValues, timePoints);
		double result[][] = data.getBlock(0).getData();
		double change[] = new double[initialValues.length];
		double yPrev[] = new double[initialValues.length];
		double yTemp[] = new double[initialValues.length];
		double steady[] = new double[initialValues.length];
		double t = timePoints[0];
		double h = stepSize;

		boolean fastFlag = false;

		additionalResults(DES, t, result[0], data, 0);

		if (DES instanceof FastProcessDESystem) {
			fastFlag = ((FastProcessDESystem) DES).containsFastProcesses();
		}

		if (fastFlag) {
			result[0] = computeSteadyState(((FastProcessDESystem) DES),
					result[0], timePoints[0]);
		}

		// execute events that trigger at 0.0
		processEvents((EventDESystem) DES, 0.0, 0.0,result[0]);
		System.arraycopy(result[0], 0, yTemp, 0, result[0].length);
		
		for (int i = 1; i < timePoints.length; i++) {
			firePropertyChange(timePoints[i-1] * intervalFactor, timePoints[i] * intervalFactor);

			h = stepSize;

			// h = h / 10;
			int steps=inBetweenSteps(timePoints[i - 1],
        timePoints[i], h);
			for(int j=1;j<=steps;j++) {
			  System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
				t = computeNextState(DES, t, h, yPrev, change, yTemp, true);
				if((i==1) && (j==1)) {
				  System.arraycopy(yPrev, 0, result[0], 0, yPrev.length);
				}
			}
			h = BigDecimal.valueOf(timePoints[i]).subtract(BigDecimal.valueOf(t)).doubleValue();
			if(h>1E-12) {
			  System.arraycopy(yTemp, 0, yPrev, 0, yTemp.length);
			  t = computeNextState(DES, t, h, yTemp, change, yTemp, false);
			}
			if (fastFlag) {
				steady = computeSteadyState(((FastProcessDESystem) DES),
						result[i], timePoints[0]);
				System.arraycopy(steady, 0, result[i], 0, yTemp.length);
			}

			additionalResults(DES, t, yTemp, data, i);
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);
			t=timePoints[i];

		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eva2.tools.math.des.DESSolver#solveAtTimePointsWithInitialConditions(
	 * eva2.tools.math.des.DESystem, double[][], double[])
	 */
	public MultiBlockTable solve(DESystem DES,
			MultiBlockTable.Block initConditions, double[] initialValues)
			throws IntegrationException {
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
      initialValues[idIndex.get(initConditions.getColumnName(col)).intValue()] = initConditions
          .getValueAt(0, col + 1);
    }
		
		MultiBlockTable data = initResultMatrix(DES, initialValues, timePoints);
		
		double[][] result = data.getBlock(0).getData();
		double[] yTemp = new double[DES.getDESystemDimension()];
		double[] change = new double[DES.getDESystemDimension()];
		double t = timePoints[0];
		additionalResults(DES, t, result[0], data, 0);
		for (i = 1; i < timePoints.length; i++) {
			firePropertyChange(timePoints[i-1] * intervalFactor, timePoints[i] * intervalFactor);
			double h = stepSize;
      if (!missingIds.isEmpty()) {
        for (k = 0; k < initConditions.getColumnCount(); k++) {
          yTemp[idIndex.get(initConditions.getColumnName(k)).intValue()] = initConditions
              .getValueAt(i - 1, k + 1);
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
				computeChange(DES, yTemp, t, h, change);
				checkSolution(change);
				Mathematics.vvAdd(yTemp, change, yTemp);
				t = BigDecimal.valueOf(h).add(BigDecimal.valueOf(t)).doubleValue();
			}
			h = BigDecimal.valueOf(timePoints[i]).subtract(BigDecimal.valueOf(t)).doubleValue();
			if(h>1E-12) {
			  computeChange(DES, yTemp, t, h, change);
			  checkSolution(change);
			  Mathematics.vvAdd(yTemp, change, yTemp);
			}
			checkNonNegativity(yTemp);
			System.arraycopy(yTemp, 0, result[i], 0, yTemp.length);

			additionalResults(DES, t, yTemp, data, i);

 			t=timePoints[i];
		}
		
		
		return data;
	}

	/**
	 * 
	 * @param DES
	 * @param result
	 * @param timeBegin
	 * @return
	 * @throws IntegrationException
	 */
	private double[] computeSteadyState(FastProcessDESystem DES,
			double[] result, double timeBegin) throws IntegrationException {
		double[] oldValues = new double[result.length];
		double[] newValues = new double[result.length];
		double[] change = new double[result.length];
		System.arraycopy(result, 0, newValues, 0, result.length);
		double ft = timeBegin;
		((FastProcessDESystem) DES).setFastProcessComputation(true);

		// TODO what if there is oscillation, so no state with no change will be
		// reached
		while (!noChange(oldValues, newValues)) {
			System.arraycopy(newValues, 0, oldValues, 0, newValues.length);
			newValues = new double[result.length];
      //			double oldFt = ft;
			ft = computeNextState(DES, ft, stepSize, oldValues, change,
					newValues, true);
			

		}
		((FastProcessDESystem) DES).setFastProcessComputation(false);
		return oldValues;

	}

	/**
	 * 
	 * @param newValues
	 * @param oldValues
	 * @return
	 */
	private boolean noChange(double newValues[], double oldValues[]) {
		for (int i = 0; i < newValues.length; i++) {
			if (Math.abs(newValues[i] - oldValues[i]) > 1e-15) {

				return false;
			}
		}
		return true;

	}

	/**
	 *
	 */
	public double g(double t, double[] y) throws EventException {
		if (Double.isNaN(t)) {
			return -1d;
		}
		return checkSolution(y) ? 1d : -1d;
	}

	/**
	 * 
	 */
	public int eventOccurred(double t, double[] y, boolean increasing)
			throws EventException {
		return STOP;
	}

	/**
	 * 
	 */
	public void resetState(double t, double[] y) throws EventException {
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#addPropertyChangedListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!listenerList.contains(listener))
			this.listenerList.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#removePropertyChangedListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listenerList.contains(listener))
			this.listenerList.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.DESSolver#firePropertyChanged(double, double)
	 */
	public void firePropertyChange(double oldValue, double newValue) {
		if (!this.listenerList.isEmpty()) {
			PropertyChangeEvent evt = new PropertyChangeEvent(this, "progress",
					oldValue, newValue);
			for (PropertyChangeListener listener : this.listenerList) {
				listener.propertyChange(evt);
			}
		}
	}
}
