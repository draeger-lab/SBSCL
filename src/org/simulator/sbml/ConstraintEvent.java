/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2014 jointly by the following organizations:
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
 * This class represents the violation of a {@link Constraint} during
 * simulation.
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.3
 */
public class ConstraintEvent extends EventObject {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -7217087217464285486L;
	 
	/**
	 * The point in time, at which a violation occurred.
	 */
	private double violationTime;

	/**
	 * Creates a new {@link ConstraintEvent} for the given {@link Constraint}
	 * and the given point in time.
	 * 
	 * @param source
	 *            the {@link Constraint}, whose condition has been violated.
	 * @param violationTime
	 *            the simulation time, at which the violation occurred.
	 */
	public ConstraintEvent(Constraint source, double violationTime) {
		super(source);
		this.violationTime = violationTime;
	}

	/**
	 * @return the point in time at which this violation occurred.
	 */
	public double getTime() {
		return this.violationTime;
	}
	
	/* (non-Javadoc)
	 * @see java.util.EventObject#getSource()
	 */
	@Override
	public Constraint getSource() {
		return (Constraint) super.getSource();
	}

}
