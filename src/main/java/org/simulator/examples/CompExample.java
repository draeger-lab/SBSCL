/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, Durham, NC, US
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.examples;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.log4j.Logger;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.comp.CompSimulator;
import org.simulator.math.odes.MultiTable;

/**
 * A simple program that performs a simulation of SBML files containing hierarchical models.
 * 
 * @author Shalin Shah, Matthias König
 * @version $Rev$
 * @since 1.5
 */
public class CompExample {

	private static double stepSize = 0.1;
	private static double timeEnd = 100;
	private static Logger LOGGER = Logger.getLogger(CompExample.class.getName());
	/**
	 * Starts a simulation at the command line.
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws SBMLException
	 * @throws ModelOverdeterminedException
	 * @throws DerivativeException
	 */
	public static void main(String[] args) throws XMLStreamException,
	IOException, ModelOverdeterminedException, SBMLException,
	DerivativeException {

		if (args[0].isEmpty()) {
			LOGGER.warn("No file entered!");
			return;
		}
		
		// perform comp simulation
		File file = new File(args[0]);

		CompSimulator compSimulator = new CompSimulator(file);
		MultiTable solution = compSimulator.solve(stepSize=0.1, timeEnd=100.0);

		// Display simulation result to the user
		JScrollPane resultDisplay = new JScrollPane(new JTable(solution));
		resultDisplay.setPreferredSize(new Dimension(400, 400));
		JOptionPane.showMessageDialog(null, resultDisplay, "Comp Results",
				JOptionPane.INFORMATION_MESSAGE);
	}

}
