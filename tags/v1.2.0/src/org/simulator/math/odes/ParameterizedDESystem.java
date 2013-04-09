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

/**
 * This class defines a differential equation system that contains parameters
 * whose values influence its behavior. Parameters can be any double values, but
 * it is expected to have an identical number of {@link String} identifiers and
 * double values, i.e., each parameter value being associated with a unique
 * identifier. Implementing classes provide methods to manipulate the current
 * parameter configuration of the system.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.2
 */
public interface ParameterizedDESystem extends DESystem {

	/**
	 * Gives the number of parameters in the {@link ParameterizedDESystem}. The
	 * number returned here must be identical to the length of the arrays
	 * returned by the methods {@link #getParameterIdentifiers()} AND
	 * {@link #getParameterValues()}. Otherwise, the implementation might show
	 * unexpected behavior.
	 * 
	 * @return parameterCount the number of parameters in the system that influence its
	 *         behavior
	 * @see #getParameterIdentifiers()
	 * @see #getParameterValues()
	 */
	public int getParameterCount();
	
	/**
	 * It is assumed that each parameter value in the system is associated with
	 * a unique identifier, which can be used to address it in numerical
	 * calculations. This method returns an array of all parameters in the
	 * {@link ParameterizedDESystem}.
	 * 
	 * @return identifiers
	 * @see #getParameterCount()
	 * @see #getParameterValues()
	 */
	public String[] getParameterIdentifiers();
	
	/**
	 * This method allows you to access all current values of the parameters
	 * within the {@link ParameterizedDESystem} in form of a double array. The
	 * length of the returned array must be identical with the value returned by
	 * {@link #getPositiveValueCount()} and also with the number of identifiers
	 * given by {@link #getParameterIdentifiers()}
	 * 
	 * @return values the current values of all parameters in the system in form of a
	 *         double array
	 * 
	 * @see #getParameterCount()
	 * @see #getParameterIdentifiers()
	 */
	public double[] getParameterValues();
	
	/**
	 * With this method it is possible to change the values of particular
	 * parameters. The length of the given array must be identical to the number
	 * returned by {@link #getParameterCount()}, but this does not mean that all
	 * parameters have to be changed when calling this method. For instance, you
	 * can obtain the current values of all parameters by calling
	 * {@link #getParameterValues()}. Within this array you could change some
	 * values of interest and subsequently pass the array to this method.
	 * 
	 * @param values the new values for all parameters.
	 * @see #getParameterCount()
	 */
	public void setParameters(double values[]);
	
}
