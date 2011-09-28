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
package org.sbml.simulator.math.odes;

/**
 * This {@link Exception} indicates that the integration process could not be
 * finished properly.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-08-25
 * @version $Rev$
 * @since 1.0
 */
public class IntegrationException extends Exception {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 2320641087420165567L;

	/**
	 * 
	 */
	public IntegrationException() {
		super();
	}

	/**
	 * @param message
	 */
	public IntegrationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IntegrationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IntegrationException(String message, Throwable cause) {
		super(message, cause);
	}

}
