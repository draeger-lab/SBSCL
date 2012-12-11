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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * <p>
 * This class represents a compilation of the values and execution times during
 * simulation concerning events. 
 * </p>
 * @author Roland Keller
 * @version $Rev$
 */
public class EventInProgress {

	protected boolean fired;
	protected double lastTimeFired;
	protected double lastTimeRecovered;
	protected double lastTimeExecuted;
	protected LinkedList<Double> execTimes;
	protected LinkedList<Double[]> values;
	protected Map<Integer,Double> assignments;

	/**
	 * Creates a new EventInProcess with the given boolean value indicating
	 * whether or not it can fire at the initial time point.
	 * 
	 * @param fired
	 */
	public EventInProgress(boolean fired) {
		this.fired = fired;
		this.execTimes = new LinkedList<Double>();
		this.values = new LinkedList<Double[]>();
		this.lastTimeFired=-1;
		this.lastTimeRecovered = -1;
		this.lastTimeExecuted = -1;
		this.assignments = new HashMap<Integer,Double>();
	}
	
	/**
	 * 
	 * @param fired
	 */
	public void refresh(boolean fired) {
	  this.fired = fired;
	  this.execTimes = new LinkedList<Double>();
	  this.values = new LinkedList<Double[]>();
	  this.lastTimeFired = -1;
	  this.lastTimeRecovered = -1;
	  this.lastTimeExecuted = -1;
	  this.assignments = new HashMap<Integer,Double>();
	}

	/**
	 * The event has been aborted between trigger and execution. For this class
	 * it has the same effect as the event has been executed.
	 */
	public void aborted(double time) {
		executed(time);
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
	 * The event associated with this class has been executed therefore reset
	 * some values.
	 */
	public void executed(double time) {
		this.execTimes.poll();
		this.values.poll();
		this.lastTimeExecuted = time;
	}
	

	/**
	 * Associated event has triggered therefore current value of fired to true
	 */
	public void fired(double time) {
		fired = true;
		this.lastTimeFired = time;
	}

	/**
	 * Returns a boolean value indication if the associated event has recently
	 * been triggered / fired
	 * 
	 * @return fireStatus
	 */
	public boolean getFireStatus(double time) {
		if ((lastTimeFired <= time) && (lastTimeRecovered <= time)) {
		  return fired;
		}
		else if ((lastTimeFired <= time) && (lastTimeRecovered > time)){
		  lastTimeRecovered = -1;
		  fired = true;
		  return true;
		}
		else if ((lastTimeFired>time) && (lastTimeRecovered <= time)) {
		  lastTimeFired = -1;
		  fired = false;
		  return false;
		}
		else {
		  lastTimeRecovered = -1;
		  lastTimeFired = -1;
		  return fired;
		}
	}

	/**
	 * Return the next time of execution of the associated event.
	 * 
	 * @return time
	 */
	public double getTime() {
		return execTimes.peek();
	}
	
	/**
	 * Returns true if the event is supposed to be executed at some time.
	 * @return executionTime?
	 */
  public boolean hasExecutionTime() {
    return execTimes.peek() != null;
  }

	/**
	 * Return the values used in the next execution of the associated event.
	 * 
	 * @return values
	 */
	public Double[] getValues() {
		return values.peek();
	}

	/**
	 * The trigger of the associated event has made a transition from true to
	 * false, so the event can be triggered again.
	 */
	public void recovered(double time) {
		fired = false;
		lastTimeRecovered = time;
	}

	/**
	 * Checks if this event has still assignments to perform for the given point
	 * in time
	 * 
	 * @param time
	 * @return moreArguments?
	 */
	public boolean hasMoreAssignments(double time) {
		
		if (execTimes.isEmpty()) {
			
			return false;
		}
		
		return execTimes.peek() <= time;
	}

	/**
	 * Returns the last time the event has been fired.
	 * @return time
	 */
	public double getLastTimeFired() {
		return lastTimeFired;
	}
  
	/**
	 * Returns the last time the event has been executed.
	 * @return time
	 */
	public double getLastTimeExecuted() {
		return lastTimeExecuted;
	}

	/**
	 * Refreshes the status of the event regarding the current time.
	 * @param currentTime
	 */
	public void refresh(double currentTime) {
	}
  
	/**
	 * Clears all event assignments.
	 */
	public void clearAssignments() {
		assignments.clear();
	}
  
	/**
	 * Adds an event assignment.
	 * @param index
	 * @param value
	 */
	public void addAssignment(int index, double value) {
		assignments.put(index, value);
	}
  
	/**
	 * Returns all event assignments as a map.
	 * @return assignments
	 */
	public Map<Integer,Double> getAssignments() {
		return assignments;
	}
	
}
