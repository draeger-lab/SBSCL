package org.simulator.sedml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.simulator.math.odes.MultiTable;


/**
 * Facade class to provide a jlibsedml-independent means to  execute a SEDML file.
 * Clients can use this class to execute a SEDML file without their code explicitly 
 * using the jlibsedml API. </br>
 * 
 * 
 * @author radams
 *
 */
public class SEDMLExecutor {
	
	
	/**
	 * 
	 * @param outputID An id of a SED-ML output element
	 * @param is A readable {@link InputStream} to the SED-ML 
	 * @return A {@link MultiTable} of the processed results.
	 * @throws ExecutionException
	 * @throws IOException if {@link InputStream} is not readable.
	 * @throws ExecutionException if execution is not possible
	 */
	MultiTable execute (String outputID, InputStream is) throws ExecutionException,
		IOException {
    	//read it with BufferedReader
    	BufferedReader br
        	= new BufferedReader(
        		new InputStreamReader(is));
 
    	StringBuilder sb = new StringBuilder();
 
    	String line;
    	while ((line = br.readLine()) != null) {
    		sb.append(line);
    	} 
 
    	SEDMLDocument doc;
		try {
			doc = Libsedml.readDocumentFromString(sb.toString());
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ExecutionException("Error reading SED-ML");
		}
    	SedML sed = doc.getSedMLModel();
    	Output out= sed.getOutputWithId(outputID);
    	if(out == null){
    		throw new ExecutionException("No output with id [" + outputID +"]");
    	}
    	SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sed, out);
    	
		Map<Task, IRawSedmlSimulationResults> res = exe.runSimulations();
		return exe.processSimulationResults(out, res);
	}
	

}
