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
import org.sbml.jsbml.Species;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class can compute and store the value of an AssignmentRule together with the variable of the rule.
 * @author Roland Keller
 * @version $Rev$
 */
public class AssignmentRuleValue extends RuleValue{
  /**
   * The id of the species reference the rule is referring to (if applicable)
   */
	private String speciesReferenceID;
	
	/**
	 * The map of the values of the species references that are contained in rules
	 */
  private Map<String,Double> stoichiometricCoefHash;

  
  /**
   * 
   * @param nodeObject
   * @param index
   */
  public AssignmentRuleValue(ASTNodeValue nodeObject, int index) {
    super(nodeObject, index);
  }

  /**
   * Constructor for rules that refer to a species reference
   * @param nodeObject
   * @param speciesReferenceID
   * @param stoichiometricCoefHash
   */
  public AssignmentRuleValue(ASTNodeValue nodeObject, String speciesReferenceID,
    Map<String, Double> stoichiometricCoefHash) {
    super(nodeObject,-1);
    this.speciesReferenceID = speciesReferenceID;
    this.stoichiometricCoefHash = stoichiometricCoefHash;
  }
  
  /**
   * Constructor for rules that refer to a species
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param hasZeroSpatialDimensions
   * @param valueHolder
   */
  public AssignmentRuleValue(ASTNodeValue nodeObject, int index,
    Species sp, int compartmentIndex, boolean hasZeroSpatialDimensions, SBMLValueHolder valueHolder) {
    super(nodeObject, index, sp, compartmentIndex, hasZeroSpatialDimensions, valueHolder);
  }

  /**
   * Processes the rule and saves the new value of the corresponding variable in the Y vector if changeY is set to true.
   * @param Y
   * @param time
   * @param changeY
   * @return Has there been a change in the Y vector caused by the rule?
   */
  public boolean processRule(double[] Y, double time, boolean changeY) {
    processAssignmentVariable(time);
    if(index>=0) {
      double oldValue=Y[index];
      if(changeY) {
        Y[index] = value;
      }
      if(oldValue!=value) {
        return true;
      }
    }
    else if(speciesReferenceID!=null) {
      Double v=stoichiometricCoefHash.get(speciesReferenceID);
      stoichiometricCoefHash.put(speciesReferenceID, value);
      
      if((v!=null) && (v.doubleValue()!=value)) {
        return true;
      }
    }
    return false;
    
  }
  
  /**
   * Returns the id of the species reference (if present), null otherwise.
   * @return id
   */
  public String getSpeciesReferenceID() {
    return speciesReferenceID;
  }
  
}
