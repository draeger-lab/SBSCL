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
import java.util.List;
import java.util.Map;

import org.simulator.math.odes.EventInProgress;
import org.simulator.sbml.astnode.ASTNodeValue;
import org.simulator.sbml.astnode.AssignmentRuleValue;

/**
 * <p>
 * This class represents a compilation of all information calculated during
 * simulation concerning events in SBML. It can also contain the math of the trigger, the priority and the delay. 
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 0.9
 */
public class SBMLEventInProgress extends EventInProgress{

	/**
	 * The priority of the event
	 */
	protected double priority;
	
	/**
	 * The event assignments
	 */
	protected Map<Integer,Double> assignments;
	
	/**
	 * The trigger of the event
	 */
	protected ASTNodeValue triggerObject;
	
	/**
	 * The priority math of the event
	 */
	protected ASTNodeValue priorityObject;
	
	/**
	 * The delay math of the event
	 */
	protected ASTNodeValue delayObject;
	
	/**
	 * The assignment rules of the event
	 */
	protected List<AssignmentRuleValue> ruleObjects;
	
	/**
	 * Are the values used from trigger time?
	 */
	protected boolean useValuesFromTriggerTime;
	
	/**
	 * The persistent attribute of the event
	 */
	protected boolean persistent;

	/**
	 * Creates a new EventInProcess with the given boolean value indicating
	 * whether or not it can fire at the initial time point.
	 * 
	 * @param fired
	 */
	public SBMLEventInProgress(boolean fired) {
		super(fired);
		this.priority = Double.NEGATIVE_INFINITY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.EventInProcess#refresh(boolean)
	 */
	public void refresh(boolean fired) {
	  super.refresh(fired);
		this.priority = Double.NEGATIVE_INFINITY;
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
	 * Return the priority of the associated event.
	 * 
	 * @return priority
	 */
	public Double getPriority() {
		return priority;
	}
  
	/**
	 * Sets the math of the trigger to a specific ASTNodeObject.
	 * @param triggerObject
	 */
	public void setTriggerObject(ASTNodeValue triggerObject) {
		this.triggerObject = triggerObject;
	}
  
	/**
	 * 
	 * @return the trigger object of the event as an ASTNodeObject
	 */
	public ASTNodeValue getTriggerObject() {
		return triggerObject;
	}

	/**
	 * Sets the math of the priority to a specific ASTNodeObject.
	 * @param priorityObject
	 */
	public void setPriorityObject(ASTNodeValue priorityObject) {
		this.priorityObject = priorityObject;
	}

	/**
	 * 
	 * @return priorityObject the priority object of the event as an ASTNodeObject
	 */
	public ASTNodeValue getPriorityObject() {
		return priorityObject;
	}

	/**
	 * Sets the math of the delay to a specific ASTNodeObject.
	 * @param delayObject
	 */
	public void setDelayObject(ASTNodeValue delayObject) {
		this.delayObject = delayObject;
	}

	/**
	 * 
	 * @return delayObject the delay object of the event as an ASTNodeObject (null if there is no delay)
	 */
	public ASTNodeValue getDelayObject() {
		return delayObject;
	}

	/**
	 * Adds the math of an assignment rule as an AssignmentRuleObject.
	 * @param assignmentRuleObject
	 */
	public void addRuleObject(AssignmentRuleValue assignmentRuleObject) {
		if (ruleObjects == null) {
			ruleObjects = new LinkedList<AssignmentRuleValue>();
		}
		ruleObjects.add(assignmentRuleObject);
	}

	/**
	 * 
	 * @return ruleObjects the list of the assignment rules as AssignmentRuleObjects
	 */
	public List<AssignmentRuleValue> getRuleObjects() {
		return ruleObjects;
	}

	/**
	 * Returns true if the values of the assignments are calculated at the trigger time of the event, otherwise false.
	 * @return useValuesFromTriggerTime
	 */
	public boolean getUseValuesFromTriggerTime() {
		return useValuesFromTriggerTime;
	}

	/**
	 * Sets the useValuesFromTriggerTime value of the event.
	 * @param useValuesFromTriggerTime
	 */
	public void setUseValuesFromTriggerTime(boolean useValuesFromTriggerTime) {
		this.useValuesFromTriggerTime = useValuesFromTriggerTime;

	}

	/**
	 * 
	 * @return numAssignments the number of assignments of the event
	 */
	public int getNumEventAssignments() {
		return ruleObjects.size();
	}

	/**
	 * 
	 * @return persistent? the persistent flag of the event.
	 */
	public boolean getPersistent() {
		return persistent;
	}

	/**
	 * Sets the persistent flag of the event.
	 * @param persistent
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * Clears the assignment rule objects.
	 */
	public void clearRuleObjects() {
		if (ruleObjects != null) {
			ruleObjects.clear();
		}
	}

}
