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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class SBMLTestSuiteWrapper {
	private static final Logger logger = Logger
			.getLogger(SBMLTestSuiteWrapper.class.getName());

	/**
	 * Computes a statistic for the SBML test suite using the {@link RosenbrockSolver} as integrator
	 * @param path
	 * @param modelnr
	 * @param outputPath
	 * @param level
	 * @param version
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void testRosenbrockSolver(String path, int modelnr, String outputPath, int level, int version) 
			throws FileNotFoundException, IOException, URISyntaxException {
		String sbmlfile, csvfile, configfile;
		AbstractDESSolver solver = new RosenbrockSolver();


		StringBuilder fileBuilder = new StringBuilder();
		fileBuilder.append(modelnr);
		while (fileBuilder.length() < 5)
			fileBuilder.insert(0, '0');
		String folder = fileBuilder.toString();
		fileBuilder.append('/');
		fileBuilder.append(folder);
		fileBuilder.insert(0, path);
		String modelFile = fileBuilder.toString();
		csvfile = modelFile + "-results.csv";
		configfile = modelFile + "-settings.txt";

		Properties props = new Properties();
		props.load(new BufferedReader(new FileReader(configfile)));
		double duration = Double.valueOf(props.getProperty("duration"));
		double start = Double.valueOf(props.getProperty("start"));

		int steps = Integer.valueOf(props.getProperty("steps"));
		Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
		String[] amounts = String.valueOf(props.getProperty("amount"))
				.trim().split(",");
		String[] concentrations = String.valueOf(
				props.getProperty("concentration")).trim().split(",");
		String[] variables = String.valueOf(props.getProperty("variables"))
				.trim().split(",");


		for (String s : amounts) {
			s = s.trim();
			if (!s.equals("")) {
				amountHash.put(s, true);
			}
		}

		for (String s : concentrations) {
			s = s.trim();
			if (!s.equals("")) {
				amountHash.put(s, false);
			}
		}

		for (int i=0; i!=variables.length; i++) {
			variables[i] = variables[i].trim();
		}

		sbmlfile = modelFile + "-sbml-l" + level + "v" + version + ".xml";
		Model model = null;
		try {
			model = (new SBMLReader()).readSBML(sbmlfile).getModel();
		} catch (Exception e) {

		}
		if (model != null) {
			CSVImporter csvimporter = new CSVImporter();
			MultiTable inputData = csvimporter.convert(model, csvfile);

			double[] timepoints = inputData.getTimePoints();

			MultiTable solution = null;
			solver.reset();
			try {
				solution = SBMLTestSuiteRunner.testModel(solver, model,
						timepoints, duration/ steps, amountHash);
			} catch (DerivativeException e) {
				logger.warning("Exception in model " + modelnr);
				solution = null;
			} catch (ModelOverdeterminedException e) {
				logger.warning("OverdeterminationException in model "
						+ modelnr);
				solution = null;
			}
			if(solution != null) {
				writeMultiTableToFile(path+"/" + folder + ".csv", variables, solution);
			}
		}
		else {
			logger.warning("The model does not exist");
		}
	}

	/**
	 * 
	 * @param outputFile
	 * @param variables
	 * @param solution
	 * @throws IOException
	 */
	private static void writeMultiTableToFile(String outputFile, String[] variables, MultiTable solution) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		writer.append("time");
		writer.append(",");
		for (int i=0; i!=variables.length; i++) {
			writer.append(variables[i]);
			if (i < variables.length - 1) {
				writer.append(",");
			}
		}

		for (int row=0; row!=solution.getTimePoints().length; row++) {
			writer.newLine();
			writer.append(String.valueOf(solution.getTimePoint(row)));
			writer.append(",");
			for (int i=0; i!=variables.length; i++) {
				writer.append(String.valueOf(solution.getColumn(variables[i]).getValue(row)));
				if (i < variables.length - 1) {
					writer.append(",");
				}
			}
		}
		writer.close();
	}

	/**
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, IOException, URISyntaxException {
		testRosenbrockSolver(args[0], Integer.valueOf(args[1]), args[2], Integer.valueOf(args[3]), Integer.valueOf(args[4])); 
	}

}
