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
package org.simulator.sedml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.ArchiveComponents;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.OneStep;
import org.jlibsedml.Output;
import org.jlibsedml.Range;
import org.jlibsedml.RepeatedTask;
import org.jlibsedml.SedML;
import org.jlibsedml.SetValue;
import org.jlibsedml.Simulation;
import org.jlibsedml.SteadyState;
import org.jlibsedml.SubTask;
import org.jlibsedml.Task;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.execution.AbstractSedmlExecutor;
import org.jlibsedml.execution.ArchiveModelResolver;
import org.jlibsedml.execution.ExecutionStatusElement;
import org.jlibsedml.execution.FileModelResolver;
import org.jlibsedml.execution.ExecutionStatusElement.ExecutionStatusType;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.jlibsedml.execution.ModelResolver;
import org.jlibsedml.execution.SedMLResultsProcesser2;
import org.jlibsedml.modelsupport.BioModelsModelsRetriever;
import org.jlibsedml.modelsupport.KisaoOntology;
import org.jlibsedml.modelsupport.KisaoTerm;
import org.jlibsedml.modelsupport.URLResourceRetriever;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.math.odes.EulerMethod;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;;

/**
 * This class extends an abstract class from jlibsedml, which provides various
 * support functions such as retrieving models, applying changes to models,
 * working out what tasks need to be executed to achieve an Output, and
 * post-processing of results.
 * <p>
 * Typical usage for this class is demonstrated in the
 * <a href="http://www.junit.org/" target="_blank">JUnit</a> test for this
 * class.<br/>
 * 
 * Models can be resolved either from local files, URLs, or
 * <a href="http://www.ebi.ac.uk/biomodels-main/" target="_blank">BioModels</a>
 * <a href="http://www.ebi.ac.uk/miriam/main/">MIRIAM</a>
 * URNs.<br/>
 * TO resolve models from different sources, see the documentation for
 * {@link AbstractSedmlExecutor} in the
 * <a href="http://jlibsedml.sourceforge.net" target="_blank">jlibsedml.jar</a> library.
 * 
 * @author Richard Adams
 * @author Shalin Shah
 * @version $Rev$
 * @since 1.1
 */
public class SedMLSBMLSimulatorExecutor extends AbstractSedmlExecutor {
	/*
	 * A list of KISAO Ids corresponding to supported algorithm types in SBMLSimulator.
	 *  These are used to determine if we are able to perform the simulation.
	 */
	final static String [] SupportedIDs = new String [] {"KISAO:0000033","KISAO:0000030", "KISAO:0000087", "KISAO:0000088", "KISAO:0000019"};

	/**
	 * Information for SBML interpreter about the species that an amount should be calculated for
	 */
	private Map<String, Boolean> amountHash;
	private ModelResolver modelResolver;
	private static final transient Logger logger = Logger.getLogger(SBMLinterpreter.class.getName());

	private static final double ONE_STEP_SIM_STEPS = 10;
	
	public SedMLSBMLSimulatorExecutor(SedML sedml, Output output) {
		super(sedml, output);
		this.modelResolver = new ModelResolver(sedml);
		// add extra model resolvers - only FileModelResolver is included by default.
		modelResolver.add(new FileModelResolver());
		modelResolver.add(new BioModelsModelsRetriever());
		modelResolver.add(new URLResourceRetriever());
	}

	/**
	 * @param sedml
	 * @param wanted
	 * @param amountHash
	 */
	public SedMLSBMLSimulatorExecutor(SedML sedml, Output wanted,
			Map<String, Boolean> amountHash) {
		this(sedml, wanted);
		this.amountHash = amountHash;
	}

	/**
	 * Enables models to be retrieved from a SED-ML archive format.<br/>
	 * This method must be called <b>before</b> {@link #runSimulations()}
	 * is called, if a SED-ML archive is to be used as a model source.
	 * @param ac A non-{@code null} {@link ArchiveComponents} object.
	 */
	public void setIsArchive (ArchiveComponents ac) {
		addModelResolver(new ArchiveModelResolver(ac));
	}

	/*
	 * test based on kisaoIDs that are available for solvers
	 * @see org.jlibsedml.execution.AbstractSedmlExecutor#canExecuteSimulation(org.jlibsedml.Simulation)
	 */
	@Override
	protected boolean canExecuteSimulation(Simulation sim) {
		String kisaoID = sim.getAlgorithm().getKisaoID();
		KisaoTerm wanted = KisaoOntology.getInstance().getTermById(kisaoID);
		for (String supported: SupportedIDs) {

			KisaoTerm offered = KisaoOntology.getInstance().getTermById(
					supported);
			// If the available type is, or is a subtype of the desired algorithm,
			//we can simulate.
			if (wanted != null & offered != null && offered.is_a(wanted)) {
				return true;
			}
		}
		return false;
	}

	/** This method performs the actual simulation, using the model and simulation configuration
	 that are passed in as arguments.It runs UniformTimeCourse simulation
	 @return An {@link IRawSedmlSimulationResults} object that is used for post-processing by the framework.
	  The actual implementation class in this implementation will be a {@link MultTableSEDMLWrapper}
	  which wraps a {@link MultiTable} of raw results.
	 */
	@Override
	protected IRawSedmlSimulationResults executeSimulation(String modelStr,
			UniformTimeCourse sim) {

		AbstractDESSolver solver = getSolverForKisaoID(sim.getAlgorithm().getKisaoID());
		File tmp = null;
		try {
			// get a JSBML object from the model string.
			tmp = File.createTempFile("Sim", "sbml");
			FileUtils.writeStringToFile(tmp, modelStr,"UTF-8");
			Model model = (new SBMLReader()).readSBML(tmp).getModel();
			// now run simulation
			SBMLinterpreter interpreter = null;
			if (amountHash != null) {
				interpreter = new SBMLinterpreter(model, 0, 0, 1,
						amountHash);
			}
			else {
				interpreter = new SBMLinterpreter(model);
			}
			solver.setIncludeIntermediates(false);
			solver.setStepSize((sim.getOutputEndTime() -sim.getOutputStartTime() )/ (sim.getNumberOfPoints()-1));
			MultiTable mts = solver.solve(interpreter, interpreter.getInitialValues(),
					sim.getOutputStartTime(),sim.getOutputEndTime());

			// adapt the MultiTable to jlibsedml interface.
			return new MultTableSEDMLWrapper(mts);


		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return null;
	}


	protected IRawSedmlSimulationResults executeSimulation(String modelStr,
			OneStep sim) {

		AbstractDESSolver solver = getSolverForKisaoID(sim.getAlgorithm().getKisaoID());
		File tmp = null;
		try {
			// get a JSBML object from the model string.
			tmp = File.createTempFile("Sim", "sbml");
			FileUtils.writeStringToFile(tmp, modelStr,"UTF-8");
			Model model = (new SBMLReader()).readSBML(tmp).getModel();
			// now run simulation
			SBMLinterpreter interpreter = null;
			if (amountHash != null) {
				interpreter = new SBMLinterpreter(model, 0, 0, 1,
						amountHash);
			}
			else {
				interpreter = new SBMLinterpreter(model);
			}
			solver.setIncludeIntermediates(false);
			// A step-size randomly taken since SED-ML L1V2 says simulator decides this
			// A better way to decide step size is essential
			solver.setStepSize(sim.getStep()/ONE_STEP_SIM_STEPS);
			MultiTable mts = solver.solve(interpreter, interpreter.getInitialValues(),
					0.0, sim.getStep());

			// adapt the MultiTable to jlibsedml interface.
			// return only 1 point for OneStep simulation: start and end
			return new MultTableSEDMLWrapper(mts.filter(new double[] {sim.getStep()}));


		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return null;
	}

	protected IRawSedmlSimulationResults executeSimulation(String modelStr,
			SteadyState sim) {

		AbstractDESSolver solver = getSolverForKisaoID(sim.getAlgorithm().getKisaoID());
		File tmp = null;
		try {
			// get a JSBML object from the model string.
			tmp = File.createTempFile("Sim", "sbml");
			FileUtils.writeStringToFile(tmp, modelStr,"UTF-8");
			Model model = (new SBMLReader()).readSBML(tmp).getModel();
			// now run simulation
			SBMLinterpreter interpreter = null;
			if (amountHash != null) {
				interpreter = new SBMLinterpreter(model, 0, 0, 1,
						amountHash);
			}
			else {
				interpreter = new SBMLinterpreter(model);
			}
			solver.setIncludeIntermediates(false);
			// TODO: implement something like AbstractDESSolver.getSteadyState method
			// to find when steadyState is reached instead of long simulation
			solver.setStepSize(1);
			MultiTable mts = solver.solve(interpreter, interpreter.getInitialValues(),
					0.0, 100000);

			// adapt the MultiTable to jlibsedml interface.
			return new MultTableSEDMLWrapper(mts);


		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return null;
	}

	/** This method is a wrapper to the runSimulations method from  {@link AbstractSedmlExecutor} to add 
	 *  additional support for repeatedTasks. It identifies the type of task, before running the
	 *  simulations.
	 */
	public Map<AbstractTask, IRawSedmlSimulationResults> run() {

		// Fetch all the tasks: Tasks + RepeatedTasks
		Map<AbstractTask, IRawSedmlSimulationResults> res = new HashMap<AbstractTask, IRawSedmlSimulationResults>();
		List<AbstractTask> tasksToExecute = sedml.getTasks();
		if (tasksToExecute.isEmpty()) {
			logger.error("No Tasks could be resolved from the required output.");
			return res;
		}

		// Iterate over task list for sequential execution
		// Handle AbstractTasks differently for Tasks and RepeatedTasks
		for (AbstractTask task : tasksToExecute) {
			if (task instanceof RepeatedTask) {
				// loop over all the subTasks
				// get all subTasks for repeatedTasks and sort them with order attribute
				RepeatedTask repTask = (RepeatedTask) task;
				Map<String, SubTask> subTasks = sortTasks(repTask.getSubTasks());
				Map<String, Range> range = repTask.getRanges();
				Map<AbstractTask, IRawSedmlSimulationResults> subTaskResults = new HashMap<AbstractTask, IRawSedmlSimulationResults>();
				
				// Store state of all the existing changes by subTasks
				List<SetValue> modelState = new ArrayList<SetValue>();

				// Find all the variable from listOfChanges and create tasks
				if (range.size() > 0 && subTasks.size() > 0) {

					// Iterate over master range
					Range masterRange = range.get(repTask.getRange());
					for(int element = 0; element < masterRange.getNumElements(); element++) {
						for(Entry<String, SubTask> st: subTasks.entrySet()) {
							SubTask subTask = st.getValue();
							AbstractTask relatedTask = sedml.getTaskWithId(subTask.getTaskId());

							// A subTask can also be a repeatedTask in which case
							// recurse all repeatedTasks subTasks to add all of them
							if (relatedTask instanceof RepeatedTask) {
								// TODO: Handle nested repeatedTask
								logger.warn("Warning! Nested repeatedTask found and ignored.");
							}else {
								// Load original model and update its state
								org.jlibsedml.Model curModel = sedml.getModelWithId(relatedTask.getModelReference());

								// 1. Check for resetModel, if so clear all the SetValues
								if(repTask.getResetModel()) {
									modelState.clear();
								}

								// 2. (optional) Check if any new changes are added
								modelState.addAll(repTask.getChanges());

								// Set model to previously stored state by applying all the current
								// changes. If resetModel=true then this will be empty and we get fresh
								// model with no modification
								if (modelState.size() > 0){
									for(SetValue change: modelState) {
										curModel.addChange(change);
									}
								}

								// 3. Execute subTasks in sorted order with the current state of model
								Simulation sim = sedml.getSimulation(relatedTask.getSimulationReference());
								IRawSedmlSimulationResults result = null;
								String changedModel = modelResolver.getModelString(curModel);

								// Quickly run error checks before final execution
								if (!supportsLanguage(curModel.getLanguage())) {
									logger.error("Language not supported: " + curModel.getLanguage());
									return res;
								}
								if (sim == null || !canExecuteSimulation(sim)) {
									logger.error("Cannot simulate task" + relatedTask.getId()
									+ "Either the simulation reference is corrupt or the simulation algorithm is not available.");
									return res;

								}
								if (changedModel == null) {
									logger.error("XML cannot be resolved");
									return res;
								}

								// Identify simulation type and run it. Store the results in a Map
								if(sim instanceof OneStep) {
									result = executeSimulation(changedModel, (OneStep) sim);    
								}else if(sim instanceof SteadyState) {
									result = executeSimulation(changedModel, (SteadyState) sim);
								}else if(sim instanceof UniformTimeCourse) {
									result = executeSimulation(changedModel, (UniformTimeCourse) sim);    
								}

								if (result == null) {
									logger.warn("Simulation failed during execution: "
											+ relatedTask.getSimulationReference() + " with model: "
											+ relatedTask.getModelReference());
								}else {
									//subTaskResults.put(relatedTask, result);
								}
							}
						}
					}
					// merge all the IRawSimulationResults into a big one and add it to results list
					// with the ID of repeatedTasks
					
				}
			}else {
				// Execute a simple Task 
				Task stdTask = (Task) task; 

				Simulation sim = sedml.getSimulation(stdTask.getSimulationReference());
				// Load original model and update its state
				org.jlibsedml.Model curModel = sedml.getModelWithId(stdTask.getModelReference());
				IRawSedmlSimulationResults results = null;
				String changedModel = modelResolver.getModelString(curModel);

				// Quickly run error checks before final execution
				if (!supportsLanguage(curModel.getLanguage())) {
					logger.error("Language not supported: " + curModel.getLanguage());
					return res;
				}
				if (sim == null || !canExecuteSimulation(sim)) {
					logger.error("Cannot simulate task" + stdTask.getId()
					+ "Either the simulation reference is corrupt or the simulation algorithm is not available.");
					return res;
				}
				if (changedModel == null) {
					logger.error("XML cannot be resolved");
					return res;
				}

				// Identify simulation type and run it. Store the results in a Map
				if(sim instanceof OneStep) {
					results = executeSimulation(changedModel, (OneStep) sim);    
				}else if(sim instanceof SteadyState) {
					results = executeSimulation(changedModel, (SteadyState) sim);
				}else if(sim instanceof UniformTimeCourse) {
					results = executeSimulation(changedModel, (UniformTimeCourse) sim);    
				}

				if (results == null) {
					logger.warn("Simulation failed during execution: "
							+ stdTask.getSimulationReference() + " with model: "
							+ stdTask.getModelReference());
				}
				res.put(stdTask, results);
			}
		}
		return res;

	}

	/** A helper function to sort subTasks by order.
	 * @param Map<String, SubTask>
	 * @return Map<String, SubTask>
	 */
	private static Map<String, SubTask> sortTasks(Map<String, SubTask> unsortMap) {

		// 1. Convert Map to List of Map
		List<Map.Entry<String, SubTask>> list = new LinkedList<Map.Entry<String, SubTask>>(unsortMap.entrySet());

		// 2. Sort list with Collections.sort(), provide a custom Comparator
		Collections.sort(list, new Comparator<Map.Entry<String, SubTask>>() {

			public int compare(Map.Entry<String, SubTask> o1,Map.Entry<String, SubTask> o2) {
				if(Double.parseDouble(o1.getValue().getOrder()) > Double.parseDouble(o2.getValue().getOrder()))
					return 1;
				else
					return 0;
			}
		});

		// 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
		Map<String, SubTask> sortedMap = new LinkedHashMap<String, SubTask>();
		for (Map.Entry<String, SubTask> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	/* SBMLSimulator can simulate SBML....
	 * (non-Javadoc)
	 * @see org.jlibsedml.execution.AbstractSedmlExecutor#supportsLanguage(java.lang.String)
	 */
	@Override
	protected boolean supportsLanguage(String language) {
		return language.contains("sbml") || language.contains("SBML");
	}

	/*
	 * Simple factory to return a solver based on the KISAO ID.
	 */
	AbstractDESSolver getSolverForKisaoID(String id) {
		if (SupportedIDs[0].equals(id)) {
			return new RosenbrockSolver();
		}else if (SupportedIDs[1].equals(id)) {
			return new EulerMethod();
		}else if (SupportedIDs[2].equals(id)) {
			return new DormandPrince54Solver();
		}else {
			return new RosenbrockSolver(); // default
		}
	}

	/**
	 * 
	 * @param wanted
	 * @param res
	 * @return
	 */
	public MultiTable processSimulationResults(Output wanted,
			Map<AbstractTask, IRawSedmlSimulationResults> res) {
		// here we post-process the results
		SedMLResultsProcesser2 pcsr2 =  new SedMLResultsProcesser2(sedml, wanted);
		pcsr2.process(res);

		// this does not necessarily have time as x-axis - another variable could be  the
		// independent variable.
		IProcessedSedMLSimulationResults prRes = pcsr2.getProcessedResult();


		// now we restore a MultiTable from the processed results. This basic example assumes a typical
		// simulation where time = xaxis - otherwise, if output is a Plot, we would need to analyse the x-axis
		// datagenerators
		MultiTable mt = createMultiTableFromProcessedResults(wanted, prRes);
		return mt;
	}

	// Here we need to check which of the results are the independent axis to create a MultiTable
	public MultiTable createMultiTableFromProcessedResults(Output wanted,
			IProcessedSedMLSimulationResults prRes) {
		String timeColName = findTimeColumn(prRes, wanted, sedml);

		// most of the rest of this code is concerned with adapting a processed result set
		// back to a multitable.

		double [] time = getTimeData(prRes, timeColName);
		// we need to get a new datset that does not contain the time-series dataset.
		double [][] data = getNonTimeData(prRes, timeColName);
		// now we ignore the time dataset
		String []hdrs = getNonTimeHeaders(prRes, timeColName);

		MultiTable mt = new MultiTable(time, data, hdrs);
		return mt;
	}

	private String[] getNonTimeHeaders(IProcessedSedMLSimulationResults prRes,
			String timeColName) {
		String []rc = new String [prRes.getNumColumns()-1];
		int rcIndx =0;
		for (String col:prRes.getColumnHeaders()) {
			if (!col.equals(timeColName)) {
				rc[rcIndx++]=col;
			}
		}
		return rc;

	}

	// gets the variable ( or non-time data )
	private double[][] getNonTimeData(IProcessedSedMLSimulationResults prRes,
			String timeColName) {
		double [][] data = prRes.getData();
		int indx = prRes.getIndexByColumnID(timeColName);
		double [][] rc = new double [prRes.getNumDataRows() ][prRes.getNumColumns()-1];
		for (int r = 0; r< data.length;r++) {
			int colIndx=0;
			for ( int c = 0; c< data[r].length;c++) {
				if (c!=indx) {
					rc[r][colIndx++]=data[r][c];
				}
			}
		}
		return rc;


	}

	//gets the time data from the processed result array.
	private double[] getTimeData(IProcessedSedMLSimulationResults prRes,
			String timeColName) {
		Double [] tim = prRes.getDataByColumnId(timeColName);


		double [] rc = new double[tim.length];
		int indx=0;
		for (Double d: tim) {
			rc[indx++]=d.doubleValue();
		}
		return rc;
	}

	// Identifies the time column's title. Raw results have column headers equal to the DataGenerator
	// id in the SEDML file.
	private String findTimeColumn(IProcessedSedMLSimulationResults prRes,
			Output wanted, SedML sedml2) {
		// TODO Auto-generated method stub
		List<String>dgIds = wanted.getAllDataGeneratorReferences();
		for (String dgID:dgIds) {
			DataGenerator dg = sedml.getDataGeneratorWithId(dgID);
			if (dg != null) {
				List<Variable> vars = dg.getListOfVariables();
				for (Variable v: vars) {
					if (v.isSymbol() && VariableSymbol.TIME.equals(v.getSymbol())) {
						return dgID;
					}
				}
			}
		}
		return null;
	}

}
