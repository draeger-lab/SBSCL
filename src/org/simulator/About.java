/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
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
package org.simulator;

import java.awt.HeadlessException;

/**
 * This class displays a short about message for this library.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class About {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String message = 
				"This is the Simulation Core Library\n\n" +
				"Main authors: Andreas Dr\u00e4ger, Roland Keller, and Alexander D\u00f6rr\n" +
				"Copyright (C) 2007-2012 by the University of Tuebingen,\nT\u00fcbingen, Germany.\n\n" +
				"License: LGPL Version 3.1\n" +
				"For more information please see\n" +
				"http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsimulator/.";
		try {
			javax.swing.JOptionPane.showMessageDialog(null, message, "About Simulation Core Library", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		} catch (HeadlessException exc) {
			System.out.println(message);
		}
	}

}
