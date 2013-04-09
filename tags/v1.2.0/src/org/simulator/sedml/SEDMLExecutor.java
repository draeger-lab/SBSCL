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
 * using the jlibsedml API.
 * 
 * @author Richard Adams
 * @version $Rev$
 * @since 1.1
 */
public class SEDMLExecutor {
	
	/**
	 * EXecutes a SEDML file to produce the specified output. It's up to 
	 *  clients to ensure a valid {@link InputStream} is open to access the 
	 *  SED-ML file.
	 * @param outputID An id of a SED-ML Output element
	 * @param is A readable {@link InputStream} to the SED-ML 
	 * @return A {@link MultiTable} of the processed results.
	 * @throws ExecutionException
	 * @throws IOException if {@link InputStream} is not readable.
	 * @throws ExecutionException if execution is not possible
	 */
	public MultiTable execute(String outputID, InputStream is) throws ExecutionException,
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
			throw new ExecutionException("Error reading SED-ML: " + e.getMessage());
		}
    	SedML sed = doc.getSedMLModel();
    	Output out= sed.getOutputWithId(outputID);
    	if (out == null) {
    		throw new ExecutionException("No output with id [" + outputID +"]");
    	}
    	SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sed, out);
    	
		Map<Task, IRawSedmlSimulationResults> res = exe.runSimulations();
		return exe.processSimulationResults(out, res);
	}

}
