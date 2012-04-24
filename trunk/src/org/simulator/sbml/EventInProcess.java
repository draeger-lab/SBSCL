/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerial simulation of biological models.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simulator.sbml.astnode.ASTNodeObject;
import org.simulator.sbml.astnode.AssignmentRuleObject;

/**
 * <p>
 * This class represents a compilation of all information calculated during
 * simulation concerning events. It can also contain the math of the trigger, the priority and the delay. 
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @date 2011-03-04
 * @version $Rev$
 * @since 0.9
 */
public class EventInProcess {

	protected boolean fired;
	protected double priority;
	protected double lastTimeFired;
	protected double lastTimeRecovered;
	protected double lastTimeExecuted;
	protected LinkedList<Double> execTimes;
	protected LinkedList<Double[]> values;
	protected Map<Integer,Double> assignments;
  protected ASTNodeObject triggerObject;
  protected ASTNodeObject priorityObject;
  protected ASTNodeObject delayObject;
  protected List<AssignmentRuleObject> ruleObjects;
  protected boolean useValuesFromTriggerTime;
  protected boolean persistent;

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
		this.lastTimeFired=-1;
		this.lastTimeRecovered=-1;
		this.lastTimeExecuted=-1;
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
    this.priority = Double.NEGATIVE_INFINITY;
    this.lastTimeFired=-1;
    this.lastTimeRecovered=-1;
    this.lastTimeExecuted=-1;
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
	public void executed(double time) {
		this.execTimes.poll();
		this.values.poll();
		this.lastTimeExecuted=time;
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
	 * @return
	 */
	public boolean getFireStatus(double time) {
		if((lastTimeFired<=time) && (lastTimeRecovered<=time)) {
		  return fired;
		}
		else if((lastTimeFired<=time) && (lastTimeRecovered>time)){
		  lastTimeRecovered=-1;
		  fired=true;
		  return true;
		}
		else if((lastTimeFired>time) && (lastTimeRecovered<=time)) {
		  lastTimeFired=-1;
		  fired=false;
		  return false;
		}
		else {
		  lastTimeRecovered=-1;
		  lastTimeFired=-1;
		  return fired;
		}
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
	 * Returns true if the event is supposed to be executed at some time.
	 * @return
	 */
  public boolean hasExecutionTime() {
    return execTimes.peek()!=null;
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
	public void recovered(double time) {
		fired = false;
		lastTimeRecovered = time;
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

	/**
	 * Returns the last time the event has been fired.
	 * @return
	 */
  public double getLastTimeFired() {
    return lastTimeFired;
  }
  
  /**
   * Returns the last time the event has been executed.
   * @return
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
   * @return
   */
  public Map<Integer,Double> getAssignments() {
    return assignments;
  }

  /**
   * Sets the math of the trigger to a specific ASTNodeObject.
   * @param userObject
   */
  public void setTriggerObject(ASTNodeObject triggerObject) {
    this.triggerObject=triggerObject;
  }
  
  /**
   * 
   * @return the trigger object of the event as an ASTNodeObject
   */
  public ASTNodeObject getTriggerObject() {
    return triggerObject;
  }

  /**
   * Sets the math of the priority to a specific ASTNodeObject.
   * @param userObject
   */
  public void setPriorityObject(ASTNodeObject priorityObject) {
    this.priorityObject=priorityObject;
  }
  
  /**
   * 
   * @return the priority object of the event as an ASTNodeObject
   */
  public ASTNodeObject getPriorityObject() {
    return priorityObject;
  }

  /**
   * Sets the math of the delay to a specific ASTNodeObject.
   * @param userObject
   */
  public void setDelayObject(ASTNodeObject delayObject) {
    this.delayObject=delayObject;
  }
  
  /**
   * 
   * @return the delay object of the event as an ASTNodeObject (null if there is no delay)
   */
  public ASTNodeObject getDelayObject() {
    return delayObject;
  }

  /**
   * Adds the math of an assignment rule as an AssignmentRuleObject.
   * @param assignmentRuleObject
   */
  public void addRuleObject(AssignmentRuleObject assignmentRuleObject) {
    if(ruleObjects==null) {
      ruleObjects=new LinkedList<AssignmentRuleObject>();
    }
    ruleObjects.add(assignmentRuleObject);
  }
  
  /**
   * 
   * @return the list of the assignment rules as AssignmentRuleObjects
   */
  public List<AssignmentRuleObject> getRuleObjects() {
    return ruleObjects;
  }

  /**
   * Returns true if the values of the assignments are calculated at the trigger time of the event, otherwise false.
   * @return 
   */
  public boolean getUseValuesFromTriggerTime() {
    return useValuesFromTriggerTime;
  }
  
  /**
   * Sets the useValuesFromTriggerTime value of the event.
   * @param useValuesFromTriggerTime
   */
  public void setUseValuesFromTriggerTime(boolean useValuesFromTriggerTime) {
    this.useValuesFromTriggerTime=useValuesFromTriggerTime;
    
  }

  /**
   * 
   * @return the number of assignments of the event
   */
  public int getNumEventAssignments() {
    return ruleObjects.size();
  }

  /**
   * 
   * @return the persistent flag of the event.
   */
  public boolean getPersistent() {
    return persistent;
  }
  
  /**
   * Sets the persistent flag of the event.
   * @param persistent
   */
  public void setPersistent(boolean persistent) {
    this.persistent=persistent;
  }

  /**
   * Clears the assignment rule objects.
   */
  public void clearRuleObjects() {
    if(ruleObjects!=null) {
      ruleObjects.clear();
    }
    
  }
}
