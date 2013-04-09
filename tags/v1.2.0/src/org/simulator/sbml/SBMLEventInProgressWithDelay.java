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
package org.simulator.sbml;

import java.util.LinkedList;


/**
 * <p > 
 * This class represents a compilation of all information calculated during
 * simulation concerning events in SBML. An SBMLEventInProcessWithDelay especially stands
 * for an event with delay.
 * </p > 
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 0.9
 */
public class SBMLEventInProgressWithDelay extends SBMLEventInProgress {

	/**
	 * The previous times the event has been executed
	 */
	private LinkedList<Double >  previousExecutionTimes;
	
	/**
	 * The previous values with which the event has been executed
	 */
	private LinkedList<Double[] >  previousExecutionValues;

	/**
	 * Creates a new SBMLEventInProcessWithDelay with the given boolean value
	 * indicating whether or not it can fire at time point 0d.
	 * 
	 * @param fired
	 */
	public SBMLEventInProgressWithDelay(boolean fired) {
		super(fired);
		previousExecutionTimes = new LinkedList<Double > ();
		previousExecutionValues = new LinkedList<Double[] > ();
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.SBMLEventInProcess#refresh(boolean)
	 */
	@Override
	public void refresh(boolean fired) {
		super.refresh(fired);
		previousExecutionTimes = new LinkedList<Double > ();
		previousExecutionValues = new LinkedList<Double[] > ();
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.SBMLEventInProcess#aborted(double)
	 */
	@Override
	public void aborted(double time) {
		execTimes.poll();
		values.poll();
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.SBMLEventInProcess#addValues(java.lang.Double[], double)
	 */
	@Override
	public void addValues(Double[] values, double time) {
		int index;
		index = insertTime(time);
		this.execTimes.add(index, time);
		this.values.add(index, values);

	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.SBMLEventInProcess#executed(double)
	 */
	@Override
	public void executed(double time) {
		double theoreticalExecTime = execTimes.poll();
		previousExecutionValues.add(values.poll());
		previousExecutionTimes.add(theoreticalExecTime);
		lastTimeExecuted = time;
	}

	/**
	 * Due to the fact that events with delay can trigger multiple times before
	 * execution, the time of execution and the corresponding values have to be
	 * inserted at the chronological correct position in the list.
	 * 
	 * @param time
	 * @return the index where time has been inserted
	 */
	private int insertTime(double time) {
		if (execTimes.isEmpty()) {
			return 0;
		}

		for (int i = 0; i < execTimes.size(); i++) {
			if (time < execTimes.get(i)) {
				return i;
			}
		}

		return execTimes.size();

	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.SBMLEventInProcess#refresh(double)
	 */
	@Override
	public void refresh(double currentTime) {
		if ( lastTimeFired > currentTime) {
			this.execTimes.pollLast();
			this.values.pollLast();
			this.recovered(currentTime);
			this.lastTimeFired = -1;
		} else {
			while((previousExecutionTimes.peekLast() != null) && (previousExecutionTimes.peekLast() > currentTime)) {
				double time = previousExecutionTimes.pollLast();
				int index = insertTime(time);
				this.execTimes.add(index, time);
				this.values.add(index, previousExecutionValues.pollLast());
			}
			Double lastTime = previousExecutionTimes.peekLast();
			if (lastTime != null) {
				this.lastTimeExecuted = lastTime;  
			} else {
				this.lastTimeExecuted = -1;
			}
		} 
	}

}
