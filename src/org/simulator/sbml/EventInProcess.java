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
 * simulation concerning events. An EventInProcess especially stands for an
 * event without delay, so it can only has one time of execution and one array
 * of values from trigger time at all.
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
  private ASTNodeObject priorityObject;
  private ASTNodeObject delayObject;
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
	 * 
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
	 * 
	 * @return
	 */
  public double getLastTimeFired() {
    return lastTimeFired;
  }
  
  /**
   * 
   * @return
   */
  public double getLastTimeExecuted() {
    return lastTimeExecuted;
  }

  /**
   * 
   * @param currentTime
   */
  public void refresh(double currentTime) {
  }
  
  /**
   * 
   */
  public void clearAssignments() {
    assignments.clear();
  }
  
  /**
   * 
   * @param index
   * @param value
   */
  public void addAssignment(int index, double value) {
    assignments.put(index, value);
  }
  
  /**
   * 
   * @return
   */
  public Map<Integer,Double> getAssignments() {
    return assignments;
  }

  /**
   * 
   * @param userObject
   */
  public void setTriggerObject(ASTNodeObject triggerObject) {
    this.triggerObject=triggerObject;
  }
  
  /**
   * 
   * @return
   */
  public ASTNodeObject getTriggerObject() {
    return triggerObject;
  }

  /**
   * 
   * @param userObject
   */
  public void setPriorityObject(ASTNodeObject priorityObject) {
    this.priorityObject=priorityObject;
  }
  
  /**
   * 
   * @return
   */
  public ASTNodeObject getPriorityObject() {
    return priorityObject;
  }

  /**
   * 
   * @param userObject
   */
  public void setDelayObject(ASTNodeObject delayObject) {
    this.delayObject=delayObject;
  }
  
  /**
   * 
   * @return
   */
  public ASTNodeObject getDelayObject() {
    return delayObject;
  }

  /**
   * 
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
   * @return
   */
  public List<AssignmentRuleObject> getRuleObjects() {
    return ruleObjects;
  }

  /**
   * 
   * @return
   */
  public boolean getUseValuesFromTriggerTime() {
    return useValuesFromTriggerTime;
  }
  
  /**
   * 
   * @param useValuesFromTriggerTime
   */
  public void setUseValuesFromTriggerTime(boolean useValuesFromTriggerTime) {
    this.useValuesFromTriggerTime=useValuesFromTriggerTime;
    
  }

  /**
   * 
   * @return
   */
  public int getNumEventAssignments() {
    return ruleObjects.size();
  }

  /**
   * 
   * @return
   */
  public boolean getPersistent() {
    return persistent;
  }
  
  public void setPersistent(boolean persistent) {
    this.persistent=persistent;
  }

  /**
   * 
   */
  public void clearRuleObjects() {
    if(ruleObjects!=null) {
      ruleObjects.clear();
    }
    
  }
}
