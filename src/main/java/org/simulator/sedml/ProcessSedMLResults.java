package org.simulator.sedml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Output;
import org.jlibsedml.Parameter;
import org.jlibsedml.SedML;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.execution.IModel2DataMappings;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.jlibsedml.execution.IXPathToVariableIDResolver;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.jmathml.ASTCi;
import org.jmathml.ASTNode;
import org.jmathml.ASTNumber;
import org.jmathml.EvaluationContext;
import org.simulator.math.odes.MultiTable;

import de.binfalse.bflog.LOGGER;

/**
 * Processes raw simulation results according to instructions specified in the
 * {@link DataGenerator} elements specified in the output. <br/>
 * This class is used to process results using information in dataGenerator
 * elements. It is similar to jlibsedml's ProcessSedMLResults2 with the added
 * support for working with repeatedTasks.
 * 
 * @author Shalin Shah
 * @since 1.5
 */
public class ProcessSedMLResults {
	private Output wanted;
	private SedML sedml;
	IProcessedSedMLSimulationResults prRes;

	public ProcessSedMLResults(SedML sedml, Output output) {
		// Check for nulls
		if (sedml == null || output == null) {
			throw new IllegalArgumentException();
		}
		this.sedml = sedml;
		this.wanted = output;

		// Check that required output exists in sedml
		boolean found = false;
		for (Output o : sedml.getOutputs()) {
			if (o.getId().equals(wanted.getId())) {
				found = true;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("Output [" + wanted.getId() + "] does not belong the SED-ML object. ");
		}
	}

	/**
	 * This method modifies jlibsedml's process method to support dataGenerators for
	 * repeatedTasks. Processed results can be extracted using getProcessedResult().
	 * 
	 * @param Map<AbstractTask,
	 *            List<IRawSedmlSimulationResults>>
	 */
	public void process(Map<AbstractTask, List<IRawSedmlSimulationResults>> res) {

		// Check for nulls
		if (res == null) {
			throw new IllegalArgumentException();
		}
		if (wanted.getAllDataGeneratorReferences().isEmpty()) {
			LOGGER.warn("Data generator list is empty!");
			throw new NullPointerException();
		}

		// calculate total number of rows in all the results
		Map<AbstractTask, double[][]> rawTask2Results = new HashMap<AbstractTask, double[][]>();
		makeDefensiveCopyOfData(res, rawTask2Results);

		// Iterate over all the data generators to process results
		List<double[]> processed = new ArrayList<double[]>();
		IXPathToVariableIDResolver variable2IDResolver = new SBMLSupport();
		
		for (String dgId : wanted.getAllDataGeneratorReferences()) {
			
			DataGenerator dg = sedml.getDataGeneratorWithId(dgId);
			if (dg == null) {
				LOGGER.warn("Empty data generator recevied. Correct SED-ML!");
				return;
			}

			List<Variable> vars = dg.getListOfVariables();
			List<Parameter> params = dg.getListOfParameters();
			Map<String, String> Var2Model = new HashMap<String, String>();
			Map<String, IRawSedmlSimulationResults> var2Result = new HashMap<String, IRawSedmlSimulationResults>();
			Map<String, double[][]> var2Data = new HashMap<String, double[][]>();
			String timeID = "";
			int numRows = 0;
			
			// map varIds to result, based upon task reference
			for (Variable variable : vars) {
				String modelID;

				if (variable.isVariable()) {
					// get the task from which this result variable was generated.
					modelID = variable2IDResolver.getIdFromXPathIdentifer(variable.getTarget());
					String taskRef = variable.getReference();
					AbstractTask t = sedml.getTaskWithId(taskRef);

					// get results list for this task. If it is repeatedTask then multiple results
					List<IRawSedmlSimulationResults> resList = res.get(t);

					// set up lookups to results, raw data and model ID
					if (resList.size() > 1) {
						// for a RepeatedTask concat results like tellurium
						IRawSedmlSimulationResults reducedResults = flattenResultsList(resList);
						var2Result.put(variable.getId(), reducedResults);
					} else {
						// Just a Task so get first element
						var2Result.put(variable.getId(), resList.get(0));
					}
					
					var2Data.put(variable.getId(), rawTask2Results.get(t));
					Var2Model.put(variable.getId(), modelID);
					numRows = var2Result.get(variable.getId()).getNumDataRows();
					
				} else if (variable.isSymbol() && variable.getSymbol().equals(VariableSymbol.TIME)) {

					// get the task from which this result variable was generated.
					String taskRef = variable.getReference();
					AbstractTask t = sedml.getTaskWithId(taskRef);

					// get results list for this task. If it is repeatedTask then multiple results
					List<IRawSedmlSimulationResults> resList = res.get(t);

					// it's a symbol
					timeID = variable.getId();
					// set up lookups to results, raw data and model ID
					if (resList.size() > 1) {
						// for a RepeatedTask concat results like tellurium
						IRawSedmlSimulationResults reducedResults = flattenResultsList(resList);
						var2Result.put(variable.getId(), reducedResults);
					} else {
						// Just a Task so get first element
						var2Result.put(variable.getId(), resList.get(0));
					}
					Var2Model.put(timeID, variable.getId());
					var2Data.put(timeID, rawTask2Results.get(t));
					
					numRows = var2Result.get(timeID).getNumDataRows();
				}
			}
			
			// number of rows should be constant for all the variables in one DG
			double[] mutated = new double[numRows];
			processed.add(mutated);
			
			// get Parameter values
			Map<String, Double> Param2Value = new HashMap<String, Double>();
			for (Parameter p : params) {
				Param2Value.put(p.getId(), p.getValue());
			}

			// now parse maths, and replace raw simulation results with
			// processed results.
			ASTNode node = dg.getMath();
			Set<ASTCi> identifiers = node.getIdentifiers();
			for (ASTCi var : identifiers) {
				if (var.isVector()) {
					String varName = var.getName();
					IModel2DataMappings coll = var2Result.get(varName).getMappings();
					int otherVarInx = coll.getColumnIndexFor(Var2Model.get(varName));
					if (otherVarInx < 0 || otherVarInx >= var2Result.get(varName).getNumColumns()) {
						LOGGER.warn("No data column for " + var);
						return;
					}
					EvaluationContext con = new EvaluationContext();
					Double[] data = var2Result.get(varName).getDataByColumnIndex(otherVarInx);

					con.setValueFor(varName, Arrays.asList(data));

					if (var.getParentNode() == null || var.getParentNode().getParentNode() == null) {
						LOGGER.warn("Could not evaluate [" + var + "] as symbol does not have parent element");
						return;
					}
					if (!var.getParentNode().canEvaluate(con)) {
						LOGGER.warn("Could not evaluate [" + var + "]");
						return;
					}
					ASTNumber num = var.getParentNode().evaluate(con);
					// replace vector operation with calculated value.
					var.getParentNode().getParentNode().replaceChild(var.getParentNode(), num);
				}
			}
			// identifiers.add(var.getSpId());
			if (identifiersMapToData(identifiers, Var2Model, Param2Value, var2Result, timeID)) {

				for (int i = 0; i < numRows; i++) {
					EvaluationContext con = new EvaluationContext();

					for (String id : Param2Value.keySet()) {
						con.setValueFor(id, Param2Value.get(id));
					}

					for (ASTCi var : identifiers) {
						// we've already resolved parameters
						if (Param2Value.get(var.getName()) != null) {
							continue;
						}
						int otherVarInx = 0;
						if (!var.getName().equals(timeID)) {
							IModel2DataMappings coll = var2Result.get(var.getName()).getMappings();
							otherVarInx = coll.getColumnIndexFor(Var2Model.get(var.getName()));
							if (otherVarInx < 0 || otherVarInx >= var2Result.get(var.getName()).getNumColumns()) {
								LOGGER.warn("No data column for " + var);
								return;
							}
						}

						con.setValueFor(var.getName(), var2Data.get(var.getName())[i][otherVarInx]);
					}

					if (node.canEvaluate(con)) {
						mutated[i] = node.evaluate(con).getValue();
					} else {
						LOGGER.warn("Math could not be executed for data generator " + dgId);
					}
				}
			} else {
				LOGGER.warn("Math could not be executed for data generator " + dgId);
				return;
			}
		}

		prRes = createData(processed);
	}

	// Helper method for processing simulation results as per dataGenerator
	// instructions
	// Borrowed from jlibsedml library to deal with repeatedTasks
	private boolean identifiersMapToData(Set<ASTCi> identifiers, Map<String, String> Var2Model,
			Map<String, Double> Param2Value, Map<String, IRawSedmlSimulationResults> var2Result, String timeID) {

		for (ASTCi var : identifiers) {
			boolean seen = false;
			if (Param2Value.get(var.getName()) != null) {
				seen = true;
			} else if (Var2Model.get(var.getName()) != null) {
				if (var.getName().equals(timeID)) {
					seen = true;
				} else {
					IModel2DataMappings coll = var2Result.get(var.getName()).getMappings();
					if (coll.hasMappingFor(Var2Model.get(var.getName()))
							&& coll.getColumnTitleFor(Var2Model.get(var.getName())) != null
							|| var.getName().equals(timeID)) {
						seen = true;
					}
				}
			}

			if (!seen) {
				return false;
			}

		}
		return true;
	}

	// Helper method for processing simulation results as per dataGenerator
	// instructions
	// Borrowed from jlibsedml library to deal with repeatedTasks
	private IProcessedSedMLSimulationResults createData(List<double[]> processed) {

		String[] hdrs = new String[processed.size()];
		int colInd = 0;
		for (Iterator<String> it = wanted.getAllDataGeneratorReferences().iterator(); it.hasNext();) {
			hdrs[colInd++] = it.next();
		}

		int max_size = 0;
		for(double[] curList: processed) {
			if(max_size < curList.length)
				max_size = curList.length;
		}
		
		double[][] data = new double[max_size][hdrs.length];
		for (int j = 0; j < processed.get(0).length; j++) {
			for (int i = 0; i < hdrs.length; i++) {
				if (j < processed.get(i).length)
					data[j][i] = processed.get(i)[j];
			}

		}
		return new IProcessedSedMLSimulationResultsWrapper(data, hdrs);

	}

	public IProcessedSedMLSimulationResults getProcessedResult() {
		return prRes;
	}

	// Copy 2D data from the IRawSimulationResults after flattening
	private void makeDefensiveCopyOfData(Map<AbstractTask, List<IRawSedmlSimulationResults>> results,
			Map<AbstractTask, double[][]> rawTask2Results) {

		// makes a defensive copy of all input data
		for (AbstractTask t : results.keySet()) {

			// Before copying 2D data array flatten task results
			// for a RepeatedTask concat results like tellurium
			List<IRawSedmlSimulationResults> result = results.get(t);
			IRawSedmlSimulationResults flat;
			if (result.size() > 1)
				flat = flattenResultsList(result);
			else
				flat = result.get(0);

			
			double[][] toCopy = flat.getData(); // for look-up of
			double[][] original = new double[toCopy.length][];

			int in = 0;
			for (double[] row : toCopy) {
				double[] copyRow = new double[row.length];
				System.arraycopy(row, 0, copyRow, 0, row.length);

				original[in++] = copyRow;
			}
			rawTask2Results.put(t, original);
		}
		return;
	}

	/**
	 * Flatten the raw results list.
	 * 
	 * @param List<IRawSedmlSimulationResults>
	 * @return IRawSedmlSimulationResults
	 */
	private IRawSedmlSimulationResults flattenResultsList(List<IRawSedmlSimulationResults> results) {
		IRawSedmlSimulationResults flat = results.stream()
				.reduce((a, b) -> new MultTableSEDMLWrapper(
						new MultiTable(
								mergeTimeCols(a, b),
								mergeDataCols(a.getData(), b.getData()), 
								results.get(0).getColumnHeaders()))).get();
			return flat;
	}

	/**
	 * Merge two 2D arrays into one 2D array in X-direction
	 * 
	 * @param double[][]
	 * @param double[][]
	 * @return double[][]
	 */
	private double[][] mergeDataCols(double[][] a, double[][] b) {
		
		double[][] aData = extractNonTimeData(a);
		double[][] bData = extractNonTimeData(b);
		
		double[][] merged = new double[aData.length + bData.length][];

		System.arraycopy(aData, 0, merged, 0, aData.length);
		System.arraycopy(bData, 0, merged, aData.length, bData.length);

		return merged;
	}

	private double[][] extractNonTimeData(double[][] data) {
		double[][] nonTimeCols = new double[data.length][data[0].length-1];
		
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[0].length-1; j++) {
				nonTimeCols[i][j] = data[i][j+1];
			}
		}
		
		return nonTimeCols;
	}

	/**
	 * Merge time columns from 2 multiTables
	 * 
	 * @param MultTableSEDMLWrapper
	 * @param MultTableSEDMLWrapper
	 * @return double[]
	 */
	private double[] mergeTimeCols(IRawSedmlSimulationResults a, IRawSedmlSimulationResults b) {
		// Get end time point for taskA
		double[] timeA = ((MultTableSEDMLWrapper)a).getMultiTable().getTimePoints();
		double timeBegin = timeA[timeA.length - 1];

		// Add end time point to taskB
		double[] timeB = Arrays.stream(((MultTableSEDMLWrapper)b).getMultiTable().getTimePoints()).map(row -> row + timeBegin).toArray();

		// merged all point to one longer double[]
		double[] merged = new double[timeA.length + timeB.length];
		System.arraycopy(timeA, 0, merged, 0, timeA.length);
		System.arraycopy(timeB, 0, merged, timeA.length, timeB.length);

		return merged;
	}

	/**
	 * Merge two 2D arrays into one 2D array in X-direction
	 * 
	 * @param double[][]
	 * @param double[][]
	 * @return double[][]
	 */
	private double[][] mergeTimeSymbol(double[][] a, double[][] b) {
		double[][] merged = new double[a.length + b.length][];

		System.arraycopy(a, 0, merged, 0, a.length);
		System.arraycopy(b, 0, merged, a.length, b.length);

		return merged;
	}
}
