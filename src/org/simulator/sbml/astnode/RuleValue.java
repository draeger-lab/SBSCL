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

import org.sbml.jsbml.Species;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class can compute and store the value of a rule together with the variable of the rule.
 * @author Roland Keller
 * @version $Rev$
 */
public class RuleValue {
	
	/**
	 * Object that refers to the math of the rule
	 */
	protected ASTNodeValue nodeObject;

	/**
	 * The calculated value of the rul
	 */
	protected double value;
  
  /**
   * Flag that is true if the variable is referring to a species, otherwise false
   */
  protected boolean isSpecies;
  
  /**
   * The value holder that stores the current simulation results
   */
  protected SBMLValueHolder valueHolder;
  
  /**
   * The index of the compartment of the species (if applicable)
   */
  protected int compartmentIndex;
  
  /**
   * The hasOnlySubstanceUnits attribute of the species (if applicable)
   */
  protected boolean hasOnlySubstanceUnits;
  
  /**
   * The isSetInitialAmount attribute of the species (if applicable)
   */
  protected boolean isSetInitialAmount;
  
  /**
   * The isSetInitialConcentration attribute of the species (if applicable)
   */
  protected boolean isSetInitialConcentration;
  
  /**
   * This flag is true if the variable is a species and its compartment has no spatial dimensions
   */
  protected boolean hasZeroSpatialDimensions;
  
  /**
   * The index of the variable in the Y vector of the value holder
   */
  protected int index;
  
  /**
   * 
   * @param nodeObject
   * @param index
   */
  public RuleValue(ASTNodeValue nodeObject, int index) {
    this.nodeObject = nodeObject;
    this.index = index;
    this.isSpecies = false;
  }
  
  /**
   * Constructor for rules that refer to a species.
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param hasZeroSpatialDimensions
   * @param valueHolder
   */
  public RuleValue(ASTNodeValue nodeObject, int index,
    Species sp, int compartmentIndex, boolean hasZeroSpatialDimensions, SBMLValueHolder valueHolder) {
    this.nodeObject = nodeObject;
    this.index = index;
    this.isSpecies = true;
    this.compartmentIndex = compartmentIndex;
    this.hasOnlySubstanceUnits = sp.getHasOnlySubstanceUnits();
    this.isSetInitialAmount = sp.isSetInitialAmount();
    this.isSetInitialConcentration = sp.isSetInitialConcentration();
    this.hasZeroSpatialDimensions = hasZeroSpatialDimensions;
    this.valueHolder = valueHolder;
  }

  /**
   * Calculates the math of the rule and returns the new value of the variable.
   * @param time
   * @return value the computed value of the variable
   */
  protected double processAssignmentVariable(double time) {
    value = nodeObject.compileDouble(time);
    if(isSpecies && !hasZeroSpatialDimensions) {
      double compartmentValue = valueHolder
          .getCurrentValueOf(compartmentIndex);
      if (isSetInitialAmount && !hasOnlySubstanceUnits) {
        value = value * compartmentValue;
        
      }
      else if (isSetInitialConcentration && hasOnlySubstanceUnits) {
        value = value / compartmentValue;
      } 
    }
    return value;
  }
  
  /**
   * Returns the value of the rule.
   * @return value
   */
  public double getValue() {
    return value;
  }
  
  /**
   * Returns the index of the variable in the Y vector of the value holder.
   * @return index
   */
  public int getIndex() {
    return index;
  }
}
