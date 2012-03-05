/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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
import org.simulator.sbml.ValueHolder;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class AssignmentRuleObject extends RuleObject{
  private String speciesReferenceID;
  private Map<String,Double> stoichiometricCoefHash;

  
  /**
   * 
   * @param nodeObject
   * @param index
   */
  public AssignmentRuleObject(ASTNodeObject nodeObject, int index) {
    super(nodeObject, index);
  }

  /**
   * 
   * @param nodeObject
   * @param speciesReferenceID
   * @param stoichiometricCoefHash
   */
  public AssignmentRuleObject(ASTNodeObject nodeObject, String speciesReferenceID,
    Map<String, Double> stoichiometricCoefHash) {
    super(nodeObject,-1);
    this.speciesReferenceID = speciesReferenceID;
    this.stoichiometricCoefHash = stoichiometricCoefHash;
  }
  
  /**
   * 
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param valueHolder
   */
  public AssignmentRuleObject(ASTNodeObject nodeObject, int index,
    Species sp, int compartmentIndex, boolean hasZeroSpatialDimensions, ValueHolder valueHolder) {
    super(nodeObject, index, sp, compartmentIndex, hasZeroSpatialDimensions, valueHolder);
  }

  /**
   * 
   * @param Y
   * @param time
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
   * 
   * @return
   */
  public String getSpeciesReferenceID() {
    return speciesReferenceID;
  }
  
}
