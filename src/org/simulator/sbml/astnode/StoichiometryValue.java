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
package org.simulator.sbml.astnode;

import java.util.Map;
import java.util.Set;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.simulator.sbml.SBMLValueHolder;

/**
 * Computes for a {@link SpeciesReference} with a stoichiometry occuring in some
 * {@link Reaction} the stoichiometry and the change of the corresponding
 * {@link Species} that is caused by the {@link Reaction}.
 * 
 * @author Roland Keller
 * @version $Rev$
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
	 * The index of the corresponding species in the Y vector of the value
	 * holder
	 */
	private int speciesIndex;

	/**
	 * The index of the corresponding species reference in the Y vector of the
	 * value holder (if any), -1 if not existing
	 */
	private int speciesRefIndex;

	/**
	 * Is the stoichiometry constant over time?
	 */
	private boolean constantStoichiometry;

	/**
	 * Is the corresponding species constant over time?
	 */
	private boolean constantQuantity;

	/**
	 * Is the boundaryCondition of the corresponding species set?
	 */
	private boolean boundaryCondition;

	/**
	 * Is the StoichiometryMath of the species reference set?
	 */
	private boolean isSetStoichiometryMath;

	/**
	 * The index of the compartment of the corresponding species
	 */
	private int compartmentIndex;

	/**
	 * The value holder that stores the current simulation results. 
	 */
	protected SBMLValueHolder valueHolder;

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
	 * The index of the reaction in vector v in the computeChange function
	 */
	private int reactionIndex;

	/**
	 * This flag is true if the corresponding species is a reactant in the
	 * reaction and false if it is a product.
	 */
	private boolean isReactant;

	/**
	 * This flag is <code>true</code> if the unit of the species is given in
	 * substance per size, which means that it has to be considered in the
	 * change rate that should always be only in substance per time
	 */
	private boolean inConcentration;

	/**
	 * Has the stoichiometry already been calculated? (important in the case of constant stoichiometry)
	 */
	private boolean stoichiometrySet;

	/**
	 * 
	 * @param sr
	 * @param speciesIndex
	 * @param speciesRefIndex
	 * @param compartmentIndex
	 * @param stoichiometricCoefHash
	 * @param valueHolder
	 * @param Y
	 * @param null
	 * @param reactionIndex
	 * @param inConcentrationSet
	 * @param isReactant
	 */
	public StoichiometryValue(SpeciesReference sr, int speciesIndex,
			int speciesRefIndex, int compartmentIndex, Map<String, Double> stoichiometricCoefHash, SBMLValueHolder valueHolder,
			double[] Y, ASTNodeValue stoichiometryMathValue, int reactionIndex,
			Set<String> inConcentrationSet, boolean isReactant) {
		this.isSetStoichiometryMath = sr.isSetStoichiometryMath();
		this.valueHolder=valueHolder;
		this.compartmentIndex=compartmentIndex;
		this.sr = sr;
		this.reactionIndex = reactionIndex;
		this.id = sr.getId();
		this.speciesIndex = speciesIndex;
		this.speciesRefIndex = speciesRefIndex;
		this.constantStoichiometry = false;
		if (sr.isSetConstant()) {
			constantStoichiometry = sr.getConstant();
		}
		else if ((!sr.isSetId()) && (!isSetStoichiometryMath)) {
			constantStoichiometry = true;
		}
		this.boundaryCondition = false;
		this.constantQuantity = false;
		this.inConcentration=false;
		Species s = sr.getSpeciesInstance();
		if (s != null) {
			if (s.getBoundaryCondition()) {
				this.boundaryCondition = true;
			}
			if (s.getConstant()) {
				this.constantQuantity = true;
			}
			if (inConcentrationSet.contains(s.getId())) {
				inConcentration=true;
			}
		}
		this.stoichiometricCoefHash = stoichiometricCoefHash;
		this.Y = Y;
		this.stoichiometryMathValue = stoichiometryMathValue;
		this.time = Double.NaN;
		this.isReactant = isReactant;

		computeStoichiometricValue();
	}

	/**
	 * Computes the change resulting for the corresponding {@link Species} in this
	 * reaction at the current time and stores it at the correct position in the
	 * changeRate array.
	 * 
	 * @param currentTime
	 * @param changeRate
	 * @param v
	 */
	public void computeChange(double currentTime, double[] changeRate, double[] v) {
		if ((constantStoichiometry == false) || (stoichiometrySet == false)) {
			compileDouble(currentTime);
		}
		double value;
		if (constantQuantity || boundaryCondition) {
			value = 0;
		} else if (isReactant) {
			value= - 1 * stoichiometry * v[reactionIndex];
		} else {
			value = stoichiometry * v[reactionIndex];
		}

		// When the unit of reacting species is given mol/volume
		// then it has to be considered in the change rate that should
		// always be only in mol/time
		if (inConcentration) {
			value = value
					/ valueHolder.getCurrentValueOf(compartmentIndex);
		}
		changeRate[speciesIndex] += value;

	}

	/**
	 * Computes the value of the stoichiometry at the current time if it has not been computed yet or is not constant.
	 * @param time
	 * @return
	 */
	private double compileDouble(double time) {
		if (this.time != time) {
			this.time = time;
			if (!constantStoichiometry || (time <= 0d) || !stoichiometrySet) {
				computeStoichiometricValue();
			}
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
  
}
