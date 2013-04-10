/*
 * $$Id$$
 * $$URL$$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2013 jointly by the following organizations:
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

import java.util.EventObject;

import org.sbml.jsbml.Constraint;

/**
 * This class represents the violation of a constraint during simulation.
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 1.3
 */
public class ConstraintViolationEvent extends EventObject {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The point in time at which a violation occurred.
	 */
	private double violationTime;

	/**
	 * Creates a new ConstraintViolationEvent for the given constraint and the
	 * given point in time.
	 * 
	 * @param source
	 * @param violationTime
	 */
	public ConstraintViolationEvent(Constraint source, double violationTime) {
		super(source);
		this.violationTime = violationTime;
	}

	/**
	 * Returns the point in time at which this violation occurred.
	 * 
	 * @return
	 */
	public double getViolationTime() {
		return this.violationTime;
	}
	
	@Override
	public Constraint getSource(){
		return this.getSource();
		
	}

}
