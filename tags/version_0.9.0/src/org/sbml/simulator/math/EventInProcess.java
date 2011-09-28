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
package org.sbml.simulator.math;

import java.util.LinkedList;

/**
 * <p>
 * This class represents a compilation of all information calculated during
 * simulation concerning events. An EventInProcess especially stands for an
 * event without delay, so it can only has one time of execution and one array
 * of values from trigger time at all.
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @date 2011-03-04
 * @version $Rev$
 * @since 1.0
 */
public class EventInProcess {

	private boolean fired;
	private double priority;
	protected LinkedList<Double> execTimes;
	protected LinkedList<Double[]> values;

	/**
	 * Creates a new EventInProcess with the given boolean value indicating
	 * whether or not it can fire at time point 0d.
	 * 
	 * @param fired
	 */
	EventInProcess(boolean fired) {
		this.fired = fired;
		this.execTimes = new LinkedList<Double>();
		this.values = new LinkedList<Double[]>();
		this.priority = Double.NEGATIVE_INFINITY;

	}

	/**
	 * The event has been aborted between trigger and execution. For this class
	 * it has the same effect as the event has been executed.
	 */
	public void aborted() {
		executed();
	}

	/**
	 * The event associated with this class has been triggered. Therefore set
	 * the time of execution and the values used at this point in time. Please
	 * note that values can be null when the event does not use values from
	 * trigger time.
	 * 
	 * @param values
	 * @param time
	 */
	public void addValues(Double[] values, double time) {
		this.execTimes.add(time);
		this.values.add(values);

	}

	/**
	 * Change the priority.
	 * 
	 * @param priority
	 */
	public void changePriority(double priority) {
		this.priority = priority;
	}

	/**
	 * The event associated with this class has been executed therefore reset
	 * some values.
	 */
	public void executed() {
		this.execTimes.poll();
		this.values.poll();
	}

	/**
	 * Associated event has triggered therefore current value of fired to true
	 */
	public void fired() {
		fired = true;
	}

	/**
	 * Returns a boolean value indication if the associated event has recently
	 * been triggered / fired
	 * 
	 * @return
	 */
	public boolean getFireStatus() {
		return fired;
	}

	/**
	 * Return the priority of the associated event.
	 * 
	 * @return
	 */
	public Double getPriority() {
		return priority;
	}

	/**
	 * Return the next time of execution of the associated event.
	 * 
	 * @return
	 */
	public double getTime() {
		return execTimes.peek();
	}

	/**
	 * Return the values used in the next execution of the associated event.
	 * 
	 * @return
	 */
	public Double[] getValues() {
		return values.peek();
	}

	/**
	 * The trigger of the associated event has made a transition from true to
	 * false, so the event can be triggered again.
	 */
	public void recovered() {
		fired = false;
	}

	/**
	 * Checks if this event has still assignments to perform for the given point
	 * in time
	 * 
	 * @param time
	 * @return
	 */
	public boolean hasMoreAssignments(double time) {
		
		if (execTimes.isEmpty()) {
			
			return false;
		}
		
		return execTimes.peek() <= time;
	}
}
