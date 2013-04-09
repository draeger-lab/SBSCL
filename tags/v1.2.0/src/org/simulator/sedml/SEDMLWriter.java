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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jdom.Element;
import org.jlibsedml.Algorithm;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Notes;
import org.jlibsedml.Plot2D;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.jlibsedml.modelsupport.SUPPORTED_LANGUAGE;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdamsBashforthSolver;
import org.simulator.math.odes.AdamsMoultonSolver;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.math.odes.DormandPrince853Solver;
import org.simulator.math.odes.EulerMethod;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.math.odes.RungeKutta_EventSolver;

/**
 * Writes a simulation configuration to SED-ML for export and sharing.<br/>
 * Usage:
 * <pre>
 * SEDMLWriter writer = new SEDMLWriter();
 * writer.setComment(comment); // optional note to annotate SED-ML
 * writer.saveExperimentToSEDML(start, end,
 *   stepsize, solver, model, 
 *	modelURI, outputStream);
 * </pre>
 * 
 * @author Richard Adams
 * @version $Rev$
 * @since 1.1
 */
public class SEDMLWriter {

	private String comment;
	/**
	 * Set an optional human-readable comment to be added to the SED-ML file.
	 * @param comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * 
	 * @param text
	 * @param sedml
	 */
	private void addNote(String text, SedML sedml) {
		Element el = new Element("p");
		el.setText(text);
		Notes n = new Notes(el);
		sedml.setNote(n);
	}

	/**
	 * Given a configured simulation, will write to SED-ML using the specified {@link OutputStream}. 
	 * It is up to the client to manage the OutputStream an ensure it is open and writeable. 
	 *
	 * @param start The start time for the desired output
	 * @param end The simulation end time
	 * @param stepsize The output step-size
	 * @param solver An {@link AbstractDESSolver}, not {@code null}.
	 * @param model A {@link Model}, not {@code null}
	 * @param modelURI	A URI pointing to the model location
	 * @param os A writeable, open {@link OutputStream}
	 * @throws IOException
	 */
	public void saveExperimentToSEDML(double start, double end,
			double stepsize, AbstractDESSolver solver, Model model, URI modelURI, OutputStream os) throws IOException {
		SEDMLDocument doc = Libsedml.createDocument();

		SedML sedml = doc.getSedMLModel();
		String modelName= extractModelName(model);
		if ((comment != null) && (comment.length() != 0)) {
			addNote(comment, sedml);
		}

		// model details
		org.jlibsedml.Model m = new org.jlibsedml.Model(modelName, modelName, SUPPORTED_LANGUAGE.SBML_GENERIC.getURN(),
				modelURI.toString());
		// time course info
		UniformTimeCourse utc = new UniformTimeCourse("sim1", "utc", 0, start, end, 
				(int)((end-start)/stepsize),
				new Algorithm(getKisaoIDForSolver(solver))); // 
		// link time course to model
		Task t1 = new Task("t1","TASK",m.getId(),utc.getId());
		sedml.addModel(m);
		sedml.addSimulation(utc);
		sedml.addTask(t1);
		ListOf<Species> los =  model.getListOfSpecies();
		// create fields for model variables
		for (Species s:los) {
			DataGenerator dg = new DataGenerator(s.getId()+"dg", s.getId(), Libsedml.parseFormulaString(s.getId()));
			SBMLSupport support= new SBMLSupport();
			Variable v = new Variable(s.getId(), s.getId(), t1.getId(),support.getXPathForSpecies(s.getId()));
			dg.addVariable(v);
			sedml.addDataGenerator(dg);
		}
		// create time datagenerator
		DataGenerator time = new DataGenerator("timedg", "Time",  Libsedml.parseFormulaString("Time"));
		Variable timeVar = new Variable("Time", "Time",t1.getId(),VariableSymbol.TIME);
		time.addVariable(timeVar);
		sedml.addDataGenerator(time);

		// now create outputs - e.g., a series of curves of time vs species
		Plot2D plot2d = new Plot2D("plot","Basic plot");
		sedml.addOutput(plot2d);
		int indx = 0;
		for (DataGenerator dg : sedml.getDataGenerators()) {
			// we don't want to plot time vs time. Equality checks are based on ID
			if (!dg.equals(time)) {
				Curve curve = new Curve("curve" + indx++ + "", null, false, false, time.getId(), dg.getId());
				plot2d.addCurve(curve);
			}

		}
		os.write(doc.writeDocumentToString().getBytes());
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	String extractModelName(Model model) {
		String modelName = model.getName();
		if (modelName==null || modelName.length()==0) {
			modelName=model.getId();
		}
		if (modelName==null || modelName.length()==0) {
			modelName="model1";
		}
		return modelName;
	}

	/**
	 * 
	 * @param SedML
	 */
	public void executeSedML(InputStream SedML) {
	}

	/**
	 * Simple factory to return a solver based on the KISAO ID.
	 * @param solver the solver for which a KISAO id is to be determined.
	 */
	String getKisaoIDForSolver (AbstractDESSolver solver) {
		if (solver instanceof EulerMethod) {
			return "KISAO_0000261";
		}
		else if (solver instanceof RungeKutta_EventSolver) {
			return "KISAO_0000064";
		} else if (solver instanceof RosenbrockSolver) {
			return "KISAO_0000033";
		} else if (solver instanceof AdamsBashforthSolver) {
			return "KISAO_0000279";
		} else if (solver instanceof AdamsMoultonSolver) {
			return "KISAO_0000280";
		} else if ((solver instanceof DormandPrince54Solver) ||
				(solver instanceof DormandPrince853Solver)) {
			return "KISAO_0000087";
		} else {
			return "KISAO_0000033"; // default
		}
	}

}
