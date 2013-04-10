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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a listener to process the violation of constraints during simulation.
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 1.3
 */
public class ConstraintViolationListener implements ContraintListener{
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(ConstraintViolationListener.class.getName());

	/**
	 * 
	 */
	public ConstraintViolationListener() {
		super();
	}
	
	/**
	 * Processes the given ConstraintViolationEvent.
	 * 
	 * @param evt
	 */
	public void violationOccured(ConstraintViolationEvent evt){
		String constraint = "null", time = "null", message = "null";;
		if (evt != null) {
			constraint = evt.getSource().getMathMLString();
			time = String.valueOf(evt.getViolationTime());
			message = evt.getSource().getMessageString();
			logger.log(Level.WARNING, MessageFormat.format("[VIOLATION]\t{0} at {1} : {2}", constraint, time, message));
		}

	}

}
