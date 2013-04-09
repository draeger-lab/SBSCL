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
package org.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;

/**
 * * This class can test the simulation of all models from biomodels.org.
 * @author Roland Keller
 * @version $Rev$
 */
public class TestBiomodels {
	private static final Logger logger = Logger
			.getLogger(TestBiomodels.class.getName());
	
	
	/**
	 * Tests the models of biomodels.org using the {@link RosenbrockSolver} as integrator
	 * @param file
	 * @param from
	 * @param to
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void testBiomodels(String file, int from, int to)
			throws FileNotFoundException, IOException {
		int errors = 0;
		int nModels = 0;
		AbstractDESSolver solver = new RosenbrockSolver();

		for (int modelnr = from; modelnr <= to; modelnr++) {
			System.out.println("Biomodel " + modelnr);
			Model model = null;
			try {
				String modelFile = "";
				if (modelnr < 10) {
					modelFile = file + "BIOMD000000000" + modelnr + ".xml";
				} else if (modelnr < 100) {
					modelFile = file + "BIOMD00000000" + modelnr + ".xml";
				} else {
					modelFile = file + "BIOMD0000000" + modelnr + ".xml";
				}
				model = (new SBMLReader()).readSBML(modelFile).getModel();
			} catch (Exception e) {
				model = null;
				logger.warning("Exception while reading Biomodel " + modelnr);
				errors++;
			}
			if (model != null) {
				solver.reset();
				try {
					SBMLinterpreter interpreter = new SBMLinterpreter(model);

					if ((solver != null) && (interpreter != null)) {
						solver.setStepSize(0.1);

						// solve
						solver.solve(interpreter,
								interpreter.getInitialValues(), 0, 10);

						if (solver.isUnstable()) {
							logger.warning("unstable!");
							errors++;
						}
					}
				} catch (DerivativeException e) {
					logger.warning("Exception in Biomodel " + modelnr);
					errors++;
				} catch (ModelOverdeterminedException e) {
					logger.warning("OverdeterminationException in Biomodel "
							+ modelnr);
					errors++;
				}
			}
			nModels++;
		}
		System.out.println("Models: " + nModels);
		System.out.println("Models with errors in simulation: " + errors);
		System.out.println("Models with correct simulation: "
				+ (nModels - errors));
	}
	
	/**
	 * * Input:
	 * <ol>
	 * <li>directory with models (containing the biomodels),
	 * <li>first model to be simulated,
	 * <li>last model to be simulated,
	 * </ol>
	 * 
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		testBiomodels(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
	}
}
