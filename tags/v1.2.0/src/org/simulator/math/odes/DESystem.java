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

import java.io.Serializable;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

/**
 * A differential equation system describes how to compute the rate of change at
 * a given state of the system.
 * 
 * @author Hannes Planatscher
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public interface DESystem extends Serializable, FirstOrderDifferentialEquations {

	/**
	 * Delivers an array of {@link String}s that describe the content of each
	 * dimension of the resulting array of this {@link DESystem}.
	 * 
	 * @return An array of {@link String}s which has the same length than the
	 *         number given by {@link #getDimension()}. Each
	 *         {@link String} describes the content of the given dimension.
	 */
	public String[] getIdentifiers();

	/**
	 * This method is used to check if this differential equation system contains
	 * any events or rules that would require a special treatment by the numerical
	 * solver.
	 * 
	 * @return flag that is {@code true} if any events or rules are contained
	 *         in the differential equation system.
	 */
	public boolean containsEventsOrRules();

	/**
	 * 
	 * @return the number of values in Y that have to be positive.
	 */
	public int getPositiveValueCount();

}
