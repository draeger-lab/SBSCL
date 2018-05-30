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
package org.simulator;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.log4j.Logger;
import org.sbml.jsbml.AbstractTreeNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.util.CompFlatteningConverter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.AddMetaInfo;
import org.simulator.sbml.SBMLinterpreter;

/**
 * A simple program that performs a simulation of containing hierarchical models.
 * 
 * @author Shalin Shah
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

		// Read the model and initialize solver
		File file = new File("files/comp/test51.xml");
		
		// Read original SBML file and add meta-info about original ID 
		SBMLDocument origDoc = SBMLReader.read(file);
		origDoc = AddMetaInfo.putOrigId(origDoc);
		
		// Flatten the model extra information in userObjects
		CompFlatteningConverter compFlatteningConverter = new CompFlatteningConverter();
	    SBMLDocument flatDoc = compFlatteningConverter.flatten(origDoc);
	    
	    // Execute the model using solver
		Model model = flatDoc.getModel();    
		DESSolver solver = new RosenbrockSolver();
		solver.setStepSize(stepSize);
		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		if (solver instanceof AbstractDESSolver) {
			((AbstractDESSolver) solver).setIncludeIntermediates(false);
		}

		// Compute the numerical solution of the initial value problem
		// TODO: Rel-Tolerance, Abs-Tolerance.
		MultiTable solution = solver.solve(interpreter, interpreter
				.getInitialValues(), 0d, timeEnd);
		
		if(solution.getColumnCount() > 1) {	
			LOGGER.warn("Output contains objects, trying to plot and extract ids:\n");
			// Map the output ids back to the original model
			for (int index = 1; index < solution.getColumnCount(); index++) {
				AbstractTreeNode node = (AbstractTreeNode) origDoc.getElementBySId(solution.getColumnIdentifier(index));
			
				if(node.isSetUserObjects()) {
					System.out.println("flat id: " + solution.getColumnIdentifier(index) + "\t old id:" + 
								node.getUserObject(AddMetaInfo.ORIG_ID) + "\t model enclosing it: " + node.getUserObject(AddMetaInfo.MODEL_ID));
					
				}
			}
			
			// Display simulation result to the user
			JScrollPane resultDisplay = new JScrollPane(new JTable(solution));
			resultDisplay.setPreferredSize(new Dimension(400, 400));
			JOptionPane.showMessageDialog(null, resultDisplay, "The solution of model "
					+ model.getId(), JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	
}
