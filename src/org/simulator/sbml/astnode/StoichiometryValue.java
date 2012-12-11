/*
 * $Id: StoichiometryValue.java 205 2012-05-05 11:57:39Z andreas-draeger $
 * $URL: http://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/sbml/astnode/StoichiometryValue.java $
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
package org.simulator.sbml.astnode;

import java.util.Map;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

/**
 * Computes for a {@link SpeciesReference} with a stoichiometry occuring in some
 * {@link Reaction} the stoichiometry.
 * 
 * @author Roland Keller
 * @version $Rev: 205 $
 */
public class StoichiometryValue {
	/**
	 * The value of the stoichiometry.
	 */
	private double stoichiometry;

	/**
	 * The current ASTNode time
	 */
	private double time;

	/**
	 * The index of the corresponding species reference in the Y vector of the
	 * value holder (if any), -1 if not existing
	 */
	private int speciesRefIndex;

	/**
	 * Is the StoichiometryMath of the species reference set?
	 */
	private boolean isSetStoichiometryMath;

	/**
	 * The id of the species reference
	 */
	private String id;

	/**
	 * The species reference
	 */
	private SpeciesReference sr;

	/**
	 * The map of the species references that are contained in rules with their values
	 */
	private Map<String, Double> stoichiometricCoefHash;

	/**
	 * The Y vector of the value holder
	 */
	private double[] Y;

	/**
	 * The value of the stoichiometry math
	 */
	private ASTNodeValue stoichiometryMathValue;

	/**
	 * Has the stoichiometry already been calculated? (important in the case of constant stoichiometry)
	 */
	private boolean stoichiometrySet;

	/**
	 * 
	 * @param sr
	 * @param speciesRefIndex
	 * @param stoichiometricCoefHash
	 * @param Y
	 * @param stoichiometryMathValue
	 */
	public StoichiometryValue(SpeciesReference sr, 
			int speciesRefIndex, Map<String, Double> stoichiometricCoefHash, 
			double[] Y, ASTNodeValue stoichiometryMathValue) {
		this.isSetStoichiometryMath = sr.isSetStoichiometryMath();
		this.sr = sr;
		this.id = sr.getId();
		this.speciesRefIndex = speciesRefIndex;
		this.stoichiometricCoefHash = stoichiometricCoefHash;
		this.Y = Y;
		this.stoichiometryMathValue = stoichiometryMathValue;
		this.time = Double.NaN;

		computeStoichiometricValue();
	}


	/**
	 * Computes the value of the stoichiometry at the current time if it has not been computed yet or is not constant.
	 * @param time
	 * @return doubleValue the value of the stoichiometry
	 */
	public double compileDouble(double time) {
		if (this.time != time) {
			this.time = time;
			computeStoichiometricValue();
		}
		return stoichiometry;
	}

	/**
	 * Computes the value of the stoichiometry.
	 */
	private void computeStoichiometricValue() {
		if (speciesRefIndex >= 0) {
			stoichiometry = Y[speciesRefIndex];
			stoichiometricCoefHash.put(id, stoichiometry);
			stoichiometrySet=true;
		} else if (stoichiometricCoefHash != null
				&& stoichiometricCoefHash.containsKey(id)) {
			stoichiometry = stoichiometricCoefHash.get(id);
			stoichiometrySet=true;
		} else {
			if (isSetStoichiometryMath) {
				stoichiometry = stoichiometryMathValue.compileDouble(time);
				stoichiometrySet=true;
			} else if ((!sr.isSetStoichiometry()) && (sr.getLevel() >= 3)) {
				stoichiometry = 1d;
				stoichiometrySet=false;
			} else {
				stoichiometry = sr.getCalculatedStoichiometry();
				if (id.equals("")) {
					stoichiometrySet=true;
				}
				else {
					stoichiometrySet=false;
				}
			}
		}
	}

	/**
	 * Refreshes the stoichiometry.
	 */
	public void refresh() {
		this.computeStoichiometricValue();
	}


	/**
	 * @return stoichiometrySet?
	 */
	public boolean getStoichiometrySet() {
		return stoichiometrySet;
	}


	/**
	 * @return stoichiometry
	 */
	public double getStoichiometry() {
		return stoichiometry;
	}
  
}
