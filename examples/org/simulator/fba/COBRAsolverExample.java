/*
 * $Id$
 * $URL$
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
package org.simulator.fba;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

/**
 * A simple test class to demonstrate the capabilities of the FBA implementation
 * in Simulation Core Library. This is based on an installation of
 * <a href="http://www.ibm.com/software/commerce/optimization/cplex-optimizer/">CPLEX</a>.
 * In order to run this example, it is necessary to launch the JVM with the
 * argument {@code -Djava.library.path=/path/to/the/binaries/of/CPLEX/}.
 * Note that this project does not redistribute CPLEX.
 * 
 * @author Andreas Dr&auml;ger
 * @author Shalin Shah
 * @version 1.5
 */
public class COBRAsolverExample {

	/**
	 * 
	 */
	private static final Logger logger = Logger.getLogger(COBRAsolverExample.class);
	PrintWriter writer;
	/**
	 * 
	 * @param file can be a file or a directory.
	 * @throws SBMLException if the model is invalid or inappropriate for flux balance analysis.
	 * @throws XMLStreamException
	 *         if the file cannot be parsed into an {@link SBMLDocument}.
	 * @throws IOException
	 *         if the given path is invalid or cannot be read
	 * @throws ModelOverdeterminedException
	 *         if the model is over determined through {@link AlgebraicRule}s.
	 * @throws SBMLException
	 *         if the model is invalid or inappropriate for flux balance analysis.
	 */
	public COBRAsolverExample(File file) throws SBMLException, ModelOverdeterminedException, XMLStreamException, IOException {
		solve(file);
	}

	/**
	 * 
	 * @param file can be a file or a directory. In the latter case, the directory will be recursively queried.
	 * @throws SBMLException if the model is invalid or inappropriate for flux balance analysis.
	 * @throws XMLStreamException
	 *         if the file cannot be parsed into an {@link SBMLDocument}.
	 * @throws IOException
	 *         if the given path is invalid or cannot be read
	 * @throws ModelOverdeterminedException
	 *         if the model is over determined through {@link AlgebraicRule}s.
	 * @throws SBMLException
	 *         if the model is invalid or inappropriate for flux balance analysis.
	 */
	public void solve(File file) throws SBMLException, ModelOverdeterminedException, XMLStreamException, IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				System.out.println("attempting to solving model: " + f.getName());
				solve(f);
			}
		} else {
			logger.error(file.getName());
			try {
				COBRAsolver solver = new COBRAsolver(SBMLReader.read(file));
				if (solver.solve()) {
					System.out.println(file.getName());
					System.out.println("Objective value:\t" + solver.getObjetiveValue());
					System.out.println("Fluxes:\t" + Arrays.toString(solver.getValues()));
				}
			} catch (SBMLException exc) {
				logger.error(exc.getMessage());
			}
		}
	}


	/**
	 * Simple test function that reads and solves an SBML file in a flux balance
	 * constraints framework.
	 * 
	 * @param args
	 *        the path to a valid SBML file with fbc version 2.
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		PropertyConfigurator.configure("MyLog4j.properties");
		try {
			new COBRAsolverExample(new File(args[0]));
		} catch (Throwable exc) {
			exc.printStackTrace();
		}
	}

}
