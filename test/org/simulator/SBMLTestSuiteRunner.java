/*
 * $$Id${file_name} ${time} ${user} $$
 * $$URL${file_name} $$
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.junit.Assert;
import org.junit.Test;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.io.CSVImporter;
import org.simulator.math.QualityMeasure;
import org.simulator.math.RelativeEuclideanDistance;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sedml.MultTableSEDMLWrapper;
import org.simulator.sedml.SedMLSBMLSimulatorExecutor;



/**
 * This class can test the simulation of all models from the SBML test suite.
 * @author Andreas Dr&auml;ger
 * @version $$Rev$$
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class SBMLTestSuiteRunner {
	private static final Logger logger = Logger
			.getLogger(SBMLTestSuiteRunner.class.getName());
	/**
	 * An array of all available ordinary differential equation solvers.
	 */
	private static final Class<AbstractDESSolver> AVAILABLE_SOLVERS[];

	static {
		int i;
		String[] classes = new String[] {
				"org.simulator.math.odes.AdamsBashforthSolver",
				"org.simulator.math.odes.AdamsMoultonSolver",
				"org.simulator.math.odes.DormandPrince54Solver",
				"org.simulator.math.odes.DormandPrince853Solver",
				"org.simulator.math.odes.EulerMethod",
				"org.simulator.math.odes.GraggBulirschStoerSolver",
				"org.simulator.math.odes.HighamHall54Solver",
				"org.simulator.math.odes.RosenbrockSolver",
				"org.simulator.math.odes.RungeKutta_EventSolver" };
		AVAILABLE_SOLVERS = new Class[classes.length];
		for (i = 0; i < classes.length; i++) {
			try {
				AVAILABLE_SOLVERS[i] = (Class<AbstractDESSolver>) Class
						.forName(classes[i]);
			} catch (ClassNotFoundException exc) {
				logger.severe(exc.getLocalizedMessage());
			}
		}
	}

	/**
	 * Commannd-line options for this test program.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @version $Rev$
	 * @since 1.2
	 */
	public enum Options {
		all,
		sedml;
	}
	
	/**
	 * Input:
	 * <ol>
	 * <li>directory with models (containing the SBML test suite),
	 * <li>first model to be simulated,
	 * <li>last model to be simulated,
	 * <li>{@link Options#all} (for testing the models of the test suite with
	 * all given integrators) or {@link Options#sedml} (for testing the models
	 * of the test suite using the given SED-ML files) or nothing (for testing
	 * the models of the test suite with the {@link RosenbrockSolver} solver,
	 * should produce only successful tests)
	 * </ol>
	 * 
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		boolean onlyRosenbrock = true;
		boolean sedML = false;
		if ((args.length >= 4) && (args[3].equals(Options.all.toString()))) {
			onlyRosenbrock = false;
		}
		if ((args.length >= 4) && (args[3].equals(Options.sedml.toString()))) {
			onlyRosenbrock = true;
			sedML = true;
		}

		if (onlyRosenbrock && sedML) {
			testRosenbrockSolverWithSEDML(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
		} else if (onlyRosenbrock) {
			testRosenbrockSolver(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
		} else {
			statisticForSolvers(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
		}
	}
	
	/**
	 * Computes a statistic for the SBML test suite testing all provided integrators
	 * @param file
	 * @param from
	 * @param to
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void statisticForSolvers(String file, int from, int to)
			throws FileNotFoundException, IOException {
		String sbmlfile, csvfile, configfile;

		// initialize solvers
		List<AbstractDESSolver> solvers = new LinkedList<AbstractDESSolver>();
		for (Class<AbstractDESSolver> solverClass : AVAILABLE_SOLVERS) {
			try {
				// instantiate solver
				AbstractDESSolver solver = solverClass.newInstance();
				if (solver != null) {
					solvers.add(solver);
				}
			} catch (Exception e) {

			}
		}

		int[] highDistances = new int[solvers.size()];
		int[] errors = new int[solvers.size()];
		int[] correctSimulations = new int[solvers.size()];
		int nModels = 0;
		for (int i = 0; i != solvers.size(); i++) {
			highDistances[i] = 0;
			errors[i] = 0;
			correctSimulations[i] = 0;
		}

		for (int modelnr = from; modelnr <= to; modelnr++) {
			System.out.println("model " + modelnr);
			nModels++;

			StringBuilder modelFile = new StringBuilder();
			modelFile.append(modelnr);
			while (modelFile.length() < 5)
				modelFile.insert(0, '0');
			String path = modelFile.toString();
			modelFile.append('/');
			modelFile.append(path);
			modelFile.insert(0, file);
			path = modelFile.toString();
			csvfile = path + "-results.csv";
			configfile = path + "-settings.txt";

			Properties props = new Properties();
			props.load(new BufferedReader(new FileReader(configfile)));
			// int start = Integer.valueOf(props.getProperty("start"));
			double duration = Double.valueOf(props.getProperty("duration"));
			double start = Double.valueOf(props.getProperty("start"));
			int steps = Integer.valueOf(props.getProperty("steps"));
			
			Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
			String[] amounts = String.valueOf(props.getProperty("amount"))
					.trim().split(",");
			String[] concentrations = String.valueOf(
					props.getProperty("concentration")).split(",");
			// double absolute = Double.valueOf(props.getProperty("absolute"));
			// double relative = Double.valueOf(props.getProperty("relative"));

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
			// String[] sbmlFileTypes = { "-sbml-l1v2.xml", "-sbml-l2v1.xml",
			// "-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
			// "-sbml-l3v1.xml" };

			String[] sbmlFileTypes = { "-sbml-l1v2.xml", "-sbml-l2v1.xml",
					"-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
					"-sbml-l3v1.xml" };

			boolean[] highDistance = new boolean[solvers.size()];
			boolean[] errorInSimulation = new boolean[solvers.size()];
			for (int i = 0; i != solvers.size(); i++) {
				highDistance[i] = false;
				errorInSimulation[i] = false;
			}

			for (String sbmlFileType : sbmlFileTypes) {
				sbmlfile = path + sbmlFileType;
				Model model = null;
				try {
					model = (new SBMLReader()).readSBML(sbmlfile).getModel();
				} catch (Exception e) {
				}
				if (model != null) {
					// get timepoints
					
					
					CSVImporter csvimporter = new CSVImporter();
					MultiTable inputData = null;
					
					File f = new File(csvfile);
					if(f.exists()) {
						inputData = csvimporter.convert(model, csvfile);
					}
					int points=steps+1;
					double[] timepoints = new double[points];
					
					BigDecimal current = new BigDecimal(start);
					BigDecimal end = new BigDecimal(start).add(new BigDecimal(duration));
					
					BigDecimal step = null;
					try{
						step = new BigDecimal(duration).divide(new BigDecimal(steps));
					}
					catch(ArithmeticException e) {
						step = null;
					}
					if(step == null) {
						timepoints = inputData.getTimePoints();
					}
					else {
						for(int i=0; i!=timepoints.length; i++) {
							timepoints[i] = Math.min(current.doubleValue(), end.doubleValue());
							current = current.add(step);
						}
					}
					
					for (int i = 0; i != solvers.size(); i++) {
						AbstractDESSolver solver = solvers.get(i);
						solver.reset();
						try {
							MultiTable solution = testModel(solver, model,
									timepoints, duration / steps, amountHash);

							double dist = Double.NaN;
							if (solution != null) {
								dist = computeDistance(inputData, solution);
							}
							if (dist > 0.1) {
								logger.log(
										Level.INFO,
										sbmlFileType
												+ ": "
												+ "relative distance for model-"
												+ modelnr + " with solver "
												+ solver.getName());
								logger.log(Level.INFO, String.valueOf(dist));
								highDistance[i] = true;
							} else if (Double.isNaN(dist)) {
								errorInSimulation[i] = true;
							}
						} catch (DerivativeException e) {
							logger.warning("Exception in model " + modelnr);
							errorInSimulation[i] = true;
						} catch (ModelOverdeterminedException e) {
							logger.warning("OverdeterminationException in model "
									+ modelnr);
							errorInSimulation[i] = true;
						}
					}

				}
			}
			for (int i = 0; i != solvers.size(); i++) {
				if (highDistance[i]) {
					highDistances[i]++;
				}
				if (errorInSimulation[i]) {
					errors[i]++;
				}
				if ((!highDistance[i]) && (!errorInSimulation[i])) {
					correctSimulations[i]++;
				}
			}
		}
		for (int i = 0; i != solvers.size(); i++) {
			System.out.println(solvers.get(i).getName());
			System.out.println("Models: " + nModels);
			System.out
					.println("Models with too high distance to experimental data: "
							+ highDistances[i]);
			System.out
					.println("Models with errors in simulation: " + errors[i]);
			System.out.println("Models with correct simulation: "
					+ correctSimulations[i]);
			System.out.println();
		}

	}

	/**
	 * Computes a statistic for the SBML test suite using the Rosenbrock solver as integrator
	 * @param file
	 * @param from
	 * @param to
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void testRosenbrockSolver(String file, int from, int to)
			throws FileNotFoundException, IOException, URISyntaxException {
		String sbmlfile, csvfile, configfile;
		int highDistances = 0;
		int errors = 0;
		int nModels = 0;
		int correctSimulations = 0;
		AbstractDESSolver solver = new RosenbrockSolver();
		int[] numberOfModels = new int[6];
		double[] runningTimes = new double[6];
		for (int i = 0; i != 6; i++) {
			numberOfModels[i] = 0;
			runningTimes[i] = 0d;
		}

		for (int modelnr = from; modelnr <= to; modelnr++) {
			System.out.println("model " + modelnr);

			StringBuilder modelFile = new StringBuilder();
			modelFile.append(modelnr);
			while (modelFile.length() < 5)
				modelFile.insert(0, '0');
			String folder = modelFile.toString();
			modelFile.append('/');
			modelFile.append(folder);
			modelFile.insert(0, file);
			String path = modelFile.toString();
			csvfile = path + "-results.csv";
			configfile = path + "-settings.txt";

			Properties props = new Properties();
			props.load(new BufferedReader(new FileReader(configfile)));
			// int start = Integer.valueOf(props.getProperty("start"));
			double duration = Double.valueOf(props.getProperty("duration"));
			double start = Double.valueOf(props.getProperty("start"));
			
			int steps = Integer.valueOf(props.getProperty("steps"));
			Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
			String[] amounts = String.valueOf(props.getProperty("amount"))
					.trim().split(",");
			String[] concentrations = String.valueOf(
					props.getProperty("concentration")).split(",");
			// double absolute = Double.valueOf(props.getProperty("absolute"));
			// double relative = Double.valueOf(props.getProperty("relative"));

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

			String[] sbmlFileTypes = { "-sbml-l1v2.xml", "-sbml-l2v1.xml",
					"-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
					"-sbml-l3v1.xml" };
			boolean highDistance = false, errorInSimulation = false;
			for (String sbmlFileType : sbmlFileTypes) {
				sbmlfile = path + sbmlFileType;
				Model model = null;
				try {
					model = (new SBMLReader()).readSBML(sbmlfile).getModel();
				} catch (Exception e) {
				}
				if (model != null) {
					CSVImporter csvimporter = new CSVImporter();
					MultiTable inputData = null;
					
					File f = new File(csvfile);
					if(f.exists()) {
						inputData = csvimporter.convert(model, csvfile);
					}
					int points=steps+1;
					double[] timepoints = new double[points];
					
					BigDecimal current = new BigDecimal(start);
					BigDecimal end = new BigDecimal(start).add(new BigDecimal(duration));
					
					BigDecimal step = null;
					try{
						step = new BigDecimal(duration).divide(new BigDecimal(steps));
					}
					catch(ArithmeticException e) {
						step = null;
					}
					if(step == null) {
						timepoints = inputData.getTimePoints();
					}
					else {
						for(int i=0; i!=timepoints.length; i++) {
							timepoints[i] = Math.min(current.doubleValue(), end.doubleValue());
							current = current.add(step);
						}
					}
					
					solver.reset();
					try {
						double time1 = System.nanoTime();
						MultiTable solution = testModel(solver, model,
								timepoints, duration/ steps, amountHash);
						double time2 = System.nanoTime();

						if (sbmlFileType.equals("-sbml-l1v2.xml")) {
							numberOfModels[0]++;
							runningTimes[0] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v1.xml")) {
							numberOfModels[1]++;
							runningTimes[1] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v2.xml")) {
							numberOfModels[2]++;
							runningTimes[2] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v3.xml")) {
							numberOfModels[3]++;
							runningTimes[3] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v4.xml")) {
							numberOfModels[4]++;
							runningTimes[4] += (time2 - time1) / 1E9;
						} else {
							numberOfModels[5]++;
							runningTimes[5] += (time2 - time1) / 1E9;
						}

						double dist = Double.NaN;
						if ((solution != null) && (inputData != null)) {
							dist = computeDistance(inputData, solution);
						}

						if (dist > 0.1) {
							logger.log(Level.INFO, sbmlFileType + ": "
									+ "relative distance for model-" + modelnr
									+ " with solver " + solver.getName());
							logger.log(Level.INFO, String.valueOf(dist));
							highDistance = true;
						} else if (Double.isNaN(dist) && (inputData != null)) {
							errorInSimulation = true;
						}
					} catch (DerivativeException e) {
						logger.warning("Exception in model " + modelnr);
						errorInSimulation = true;
					} catch (ModelOverdeterminedException e) {
						logger.warning("OverdeterminationException in model "
								+ modelnr);
						errorInSimulation = true;
					}
				}
			}
			nModels++;
			if (highDistance) {
				highDistances++;
			}
			if (errorInSimulation) {
				errors++;
			}
			if ((!highDistance) && (!errorInSimulation)) {
				correctSimulations++;
			}
		}
		System.out.println("Models: " + nModels);
		System.out
				.println("Models with too high distance to experimental data: "
						+ highDistances);
		System.out.println("Models with errors in simulation: " + errors);
		System.out.println("Models with correct simulation: "
				+ correctSimulations);

		System.out.println("L1V2 - models: " + numberOfModels[0]);
		System.out.println("L1V2 - time: " + runningTimes[0] + " s");

		System.out.println("L2V1 - models: " + numberOfModels[1]);
		System.out.println("L2V1 - time: " + runningTimes[1] + " s");

		System.out.println("L2V2 - models: " + numberOfModels[2]);
		System.out.println("L2V2 - time: " + runningTimes[2] + " s");

		System.out.println("L2V3 - models: " + numberOfModels[3]);
		System.out.println("L2V3 - time: " + runningTimes[3] + " s");

		System.out.println("L2V4 - models: " + numberOfModels[4]);
		System.out.println("L2V4 - time: " + runningTimes[4] + " s");

		System.out.println("L3V1 - models: " + numberOfModels[5]);
		System.out.println("L3V1 - time: " + runningTimes[5] + " s");
	}

	/**
	 * Computes a statistic for the SBML test suite using the Rosenbrock solver as integrator and the SED-ML files
	 * @param file
	 * @param from
	 * @param to
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void testRosenbrockSolverWithSEDML(String file, int from, int to)
			throws FileNotFoundException, IOException, URISyntaxException {
		String sbmlfile, csvfile, configfile, sedmlfile;
		int highDistances = 0;
		int errors = 0;
		int nModels = 0;
		int correctSimulations = 0;
		int fileMissing = 0;
		AbstractDESSolver solver = new RosenbrockSolver();
		int[] numberOfModels = new int[6];
		double[] runningTimes = new double[6];
		for (int i = 0; i != 6; i++) {
			numberOfModels[i] = 0;
			runningTimes[i] = 0d;
		}

		for (int modelnr = from; modelnr <= to; modelnr++) {
			System.out.println("model " + modelnr);

			StringBuilder modelFile = new StringBuilder();
			modelFile.append(modelnr);
			while (modelFile.length() < 5)
				modelFile.insert(0, '0');
			String path = modelFile.toString();
			modelFile.append('/');
			modelFile.append(path);
			modelFile.insert(0, file);
			path = modelFile.toString();
			csvfile = path + "-results.csv";
			configfile = path + "-settings.txt";

			Properties props = new Properties();
			props.load(new BufferedReader(new FileReader(configfile)));
			// int start = Integer.valueOf(props.getProperty("start"));
			Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
			String[] amounts = String.valueOf(props.getProperty("amount"))
					.trim().split(",");
			String[] concentrations = String.valueOf(
					props.getProperty("concentration")).split(",");
			// double absolute = Double.valueOf(props.getProperty("absolute"));
			// double relative = Double.valueOf(props.getProperty("relative"));

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

			String[] sbmlFileTypes = { "-sbml-l1v2.xml", "-sbml-l2v1.xml",
					"-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
					"-sbml-l3v1.xml" };
			boolean highDistance = false, errorInSimulation = false, missingFile = false;
			for (String sbmlFileType : sbmlFileTypes) {
				sbmlfile = path + sbmlFileType;
				sedmlfile = sbmlfile.replace(".xml", "-sedml.xml");
				Model model = null;
				try {
					model = (new SBMLReader()).readSBML(sbmlfile).getModel();
				} catch (Exception e) {
				}
				if (model != null) {
					// get timepoints
					SEDMLDocument doc;
					SedML sedml = null;
					SedMLSBMLSimulatorExecutor exe;
					try {
						doc = Libsedml.readDocument(new File(sedmlfile));
						sedml = doc.getSedMLModel();

					} catch (XMLException e1) {
						logger.warning("SED-ML file not found for model " + modelnr);
						missingFile = true;
					}
					if (sedml != null) {
						String newPath = sbmlfile.replaceFirst(".*:", "file:");
						newPath = convertAbsoluteFilePathToURI(newPath);
						sedml.getModels().get(0).setSource(newPath);
						Output wanted = sedml.getOutputs().get(0);
						exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, amountHash);
						double time1 = System.nanoTime();
						Map<Task, IRawSedmlSimulationResults> res = exe
								.runSimulations();
						double time2 = System.nanoTime();

						if (res == null || res.isEmpty() || !exe.isExecuted()) {
							logger.warning("Exception in model " + modelnr
									+ ":" + exe.getFailureMessages().get(0));
							errorInSimulation = true;
						}
						
						MultiTable mt = null;
						for(Task t: res.keySet()) {
							mt = ((MultTableSEDMLWrapper) res.get(t)).getMultiTable();
							break;
						}
//						SedMLResultsProcesser2 pcsr2 = new SedMLResultsProcesser2(
//								sedml, wanted);
//						pcsr2.process(res);
//
//						// this does not necessarily have time as x-axis -
//						// another variable could be the
//						// independent variable.
//						IProcessedSedMLSimulationResults prRes = pcsr2
//								.getProcessedResult();
//
//						// now we restore a MultiTable from the processed
//						// results. This basic example assumes a typical
//						// simulation where time = xaxis - otherwise, if output
//						// is a Plot, we would need to analyse the x-axis
//						// datagenerators
//						mt = createMultiTableFromProcessedResults(
//								wanted, prRes, sedml);
						CSVImporter csvimporter = new CSVImporter();
						MultiTable inputData = csvimporter.convert(model,
								csvfile);
						String[] currentIdentifiers = mt.getBlock(0).getIdentifiers();
						String[] correctedIdentifiers = new String[currentIdentifiers.length];
						for(int i=0; i!= currentIdentifiers.length; i++) {
							correctedIdentifiers[i] = currentIdentifiers[i].replaceAll("_.*", "");
						}
						mt.getBlock(0).setIdentifiers(correctedIdentifiers);
						double dist = computeDistance(inputData, mt);

						if (sbmlFileType.equals("-sbml-l1v2.xml")) {
							numberOfModels[0]++;
							runningTimes[0] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v1.xml")) {
							numberOfModels[1]++;
							runningTimes[1] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v2.xml")) {
							numberOfModels[2]++;
							runningTimes[2] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v3.xml")) {
							numberOfModels[3]++;
							runningTimes[3] += (time2 - time1) / 1E9;
						} else if (sbmlFileType.equals("-sbml-l2v4.xml")) {
							numberOfModels[4]++;
							runningTimes[4] += (time2 - time1) / 1E9;
						} else {
							numberOfModels[5]++;
							runningTimes[5] += (time2 - time1) / 1E9;
						}

						if (dist > 0.1) {
							logger.log(Level.INFO, sbmlFileType + ": "
									+ "relative distance for model-" + modelnr
									+ " with solver " + solver.getName());
							logger.log(Level.INFO, String.valueOf(dist));
							highDistance = true;
						} else if (Double.isNaN(dist)) {
							errorInSimulation = true;
						}
					}
				}
			}
			nModels++;
			if (highDistance) {
				highDistances++;
			}
			if(missingFile) {
				fileMissing++;
			}
			if (errorInSimulation) {
				errors++;
			}
			if ((!highDistance) && (!errorInSimulation) && (!missingFile)) {
				correctSimulations++;
			}
		}
		System.out.println("Models: " + nModels);
		System.out
				.println("Models with too high distance to experimental data: "
						+ highDistances);
		System.out.println("Models with errors in simulation: " + errors);
		System.out.println("Models with missing SED-ML file: " + fileMissing);
		System.out.println("Models with correct simulation: "
				+ correctSimulations);

		System.out.println("L1V2 - models: " + numberOfModels[0]);
		System.out.println("L1V2 - time: " + runningTimes[0] + " s");

		System.out.println("L2V1 - models: " + numberOfModels[1]);
		System.out.println("L2V1 - time: " + runningTimes[1] + " s");

		System.out.println("L2V2 - models: " + numberOfModels[2]);
		System.out.println("L2V2 - time: " + runningTimes[2] + " s");

		System.out.println("L2V3 - models: " + numberOfModels[3]);
		System.out.println("L2V3 - time: " + runningTimes[3] + " s");

		System.out.println("L2V4 - models: " + numberOfModels[4]);
		System.out.println("L2V4 - time: " + runningTimes[4] + " s");

		System.out.println("L3V1 - models: " + numberOfModels[5]);
		System.out.println("L3V1 - time: " + runningTimes[5] + " s");
	}

	

	/**
	 * Tests one specific model
	 * @param solver
	 * @param model
	 * @param timePoints
	 * @param stepSize
	 * @param amountHash
	 * @return result
	 * @throws SBMLException
	 * @throws ModelOverdeterminedException
	 * @throws DerivativeException
	 */
	public static MultiTable testModel(AbstractDESSolver solver, Model model,
			double[] timePoints, double stepSize,
			Map<String, Boolean> amountHash) throws SBMLException,
			ModelOverdeterminedException, DerivativeException {
		// initialize interpreter
		SBMLinterpreter interpreter = new SBMLinterpreter(model, 0, 0, 1,
				amountHash);

		if ((solver != null) && (interpreter != null)) {
			solver.setStepSize(stepSize);

			// solve
			MultiTable solution = solver.solve(interpreter,
					interpreter.getInitialValues(), timePoints);
			if (solver.isUnstable()) {
				logger.warning("unstable!");
				return null;
			}
			return solution;
		} else {
			return null;
		}

	}

	/**
	 * 
	 * @param solution
	 * @param inputData
	 * @return
	 */
	private static double computeDistance(MultiTable solution,
			MultiTable inputData) {
		// compute distance
		QualityMeasure distance = new RelativeEuclideanDistance();
		double dist = distance.distance(solution, inputData);

		return dist;
	}

	/**
	 * Tests the models of the SBML test suite using Rosenbrock as integrator
	 * TEST_CASES must be set to the address of the folder "semantic" in the
	 * SBML test suite.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testModels() throws FileNotFoundException, IOException {
		String file = System.getenv("TEST_CASES");
		String sbmlfile, csvfile, configfile;
		for (int modelnr = 1; modelnr <= 1123; modelnr++) {
			System.out.println("model " + modelnr);

			StringBuilder modelFile = new StringBuilder();
			modelFile.append(modelnr);
			while (modelFile.length() < 5)
				modelFile.insert(0, '0');
			String path = modelFile.toString();
			modelFile.append('/');
			modelFile.append(path);
			modelFile.insert(0, file);
			path = modelFile.toString();

			csvfile = path + "-results.csv";
			configfile = path + "-settings.txt";

			Properties props = new Properties();
			props.load(new BufferedReader(new FileReader(configfile)));
			// int start = Integer.valueOf(props.getProperty("start"));
			double duration = Double.valueOf(props.getProperty("duration"));
			double steps = Double.valueOf(props.getProperty("steps"));
			Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
			String[] amounts = String.valueOf(props.getProperty("amount"))
					.trim().split(",");
			String[] concentrations = String.valueOf(
					props.getProperty("concentration")).split(",");
			// double absolute = Double.valueOf(props.getProperty("absolute"));
			// double relative = Double.valueOf(props.getProperty("relative"));

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

			String[] sbmlFileTypes = { "-sbml-l1v2.xml", "-sbml-l2v1.xml",
					"-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
					"-sbml-l3v1.xml" };

			for (String sbmlFileType : sbmlFileTypes) {
				sbmlfile = path + sbmlFileType;
				File sbmlFile = new File(sbmlfile);

				if ((sbmlFile != null) && (sbmlFile.exists())) {
					// read model
					Model model = null;
					boolean errorInModelReading = false;
					try {
						model = (new SBMLReader()).readSBML(sbmlFile)
								.getModel();
					} catch (Exception e) {
						errorInModelReading = true;
					}
					Assert.assertNotNull(model);
					Assert.assertFalse(errorInModelReading);

					AbstractDESSolver solver = new RosenbrockSolver();
					// initialize interpreter
					SBMLinterpreter interpreter = null;
					boolean exceptionInInterpreter = false;
					try {
						interpreter = new SBMLinterpreter(model, 0, 0, 1,
								amountHash);
					} catch (SBMLException e) {
						exceptionInInterpreter = true;
					} catch (ModelOverdeterminedException e) {
						exceptionInInterpreter = true;
					}
					Assert.assertNotNull(interpreter);
					Assert.assertFalse(exceptionInInterpreter);

					// get timepoints
					CSVImporter csvimporter = new CSVImporter();
					MultiTable inputData = csvimporter.convert(model, csvfile);
					double[] timepoints = inputData.getTimePoints();
					duration = timepoints[timepoints.length - 1]
							- timepoints[0];

					if ((solver != null) && (interpreter != null)) {
						System.out.println(sbmlFileType + " "
								+ solver.getName());
						solver.setStepSize(duration / steps);

						// solve
						MultiTable solution = null;
						boolean errorInSolve = false;
						try {
							solution = solver.solve(interpreter,
									interpreter.getInitialValues(), timepoints);
						} catch (DerivativeException e) {
							errorInSolve = true;
						}
						Assert.assertNotNull(solution);
						Assert.assertFalse(errorInSolve);
						Assert.assertFalse(solver.isUnstable());

						// compute distance
						QualityMeasure distance = new RelativeEuclideanDistance();
						double dist = distance.distance(solution, inputData);
						Assert.assertTrue(dist <= 0.2);

					}
				}

			}
		}
	}

	/**
	 * <ul>
	 * <li>Converts spaces to %20
	 * <li>Converts \ to /
	 * 
	 * @param path
	 *            A Windows absolute filepath
	 * @return A String of the filepath suitable for inclusion as a URI object.
	 */
	public static String convertAbsoluteFilePathToURI(String path) {
		String noSpaces = path.replaceAll(" ", "%20");
		return noSpaces.replaceAll("\\\\", "/");
	}

	// Here we need to check which of the results are the independent axis to
	// create a MultiTable
	private static MultiTable createMultiTableFromProcessedResults(
			Output wanted, IProcessedSedMLSimulationResults prRes, SedML sedml) {
		String timeColName = findTimeColumn(prRes, wanted, sedml);

		// most of the rest of this code is concerned with adapting a processed
		// result set
		// back to a multitable.

		double[] time = getTimeData(prRes, timeColName);
		// we need to get a new datset that does not contain the time-series
		// dataset.
		double[][] data = getNonTimeData(prRes, timeColName);
		// now we ignore the time dataset
		String[] hdrs = getNonTimeHeaders(prRes, timeColName);

		MultiTable mt = new MultiTable(time, data, hdrs);
		return mt;
	}

	// Identifies the time column's title. Raw results have column headers equal
	// to the DataGenerator
	// id in the SEDML file.
	private static String findTimeColumn(
			IProcessedSedMLSimulationResults prRes, Output wanted, SedML sedml) {
		List<String> dgIds = wanted.getAllDataGeneratorReferences();
		for (String dgID : dgIds) {
			DataGenerator dg = sedml.getDataGeneratorWithId(dgID);
			if (dg != null) {
				List<Variable> vars = dg.getListOfVariables();
				for (Variable v : vars) {
					if (v.isSymbol()
							&& VariableSymbol.TIME.equals(v.getSymbol())) {
						return dgID;
					}
				}
			}
		}
		return null;
	}

	// gets the variable ( or non-time data )
	private static double[][] getNonTimeData(
			IProcessedSedMLSimulationResults prRes, String timeColName) {
		double[][] data = prRes.getData();
		int indx = prRes.getIndexByColumnID(timeColName);
		double[][] rc = new double[prRes.getNumDataRows()][prRes
				.getNumColumns() - 1];
		for (int r = 0; r < data.length; r++) {
			int colIndx = 0;
			for (int c = 0; c < data[r].length; c++) {
				if (c != indx) {
					rc[r][colIndx++] = data[r][c];
				}
			}
		}
		return rc;

	}

	// gets the time data from the processed result array.
	private static double[] getTimeData(IProcessedSedMLSimulationResults prRes,
			String timeColName) {
		Double[] tim = prRes.getDataByColumnId(timeColName);

		double[] rc = new double[tim.length];
		int indx = 0;
		for (Double d : tim) {
			rc[indx++] = d.doubleValue();
		}
		return rc;
	}

	private static String[] getNonTimeHeaders(
			IProcessedSedMLSimulationResults prRes, String timeColName) {
		String[] rc = new String[prRes.getNumColumns() - 1];
		int rcIndx = 0;
		for (String col : prRes.getColumnHeaders()) {
			if (!col.equals(timeColName)) {
				rc[rcIndx++] = col;
			}
		}
		return rc;

	}
}
