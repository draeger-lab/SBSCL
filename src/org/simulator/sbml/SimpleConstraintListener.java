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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.util.SBMLtools;

/**
 * This class represents a simple listener implementation to process the
 * violation of {@link Constraint}s during simulation by logging the violation
 * event in form of a warning.
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.3
 */
public class SimpleConstraintListener implements ConstraintListener {

	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(SimpleConstraintListener.class.getName());

	/* (non-Javadoc)
	 * @see org.simulator.sbml.ContraintListener#processViolation(org.simulator.sbml.ConstraintEvent)
	 */
	@Override
	public void processViolation(ConstraintEvent evt) {
		assert evt != null;
		String constraint = "null", message = "null";
		// Math must be set, otherwise this event would not have been triggered.
		constraint = evt.getSource().getMath().toFormula();
		message = SBMLtools.toXML(evt.getSource().getMessage());
		// TODO: Localize
		logger.log(Level.WARNING, MessageFormat.format(
				"[VIOLATION]\t{0} at time {1,number}: {2}",
				constraint, evt.getTime(), message));
	}

}
