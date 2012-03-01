/*
 * $Id:  RuleObject.java 15:49:33 keller $
 * $URL: RuleObject.java $
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

import org.sbml.jsbml.Species;
import org.simulator.sbml.ValueHolder;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class RuleObject {
  protected ASTNodeObject nodeObject;
  protected double value;
  protected boolean isSpecies;
  protected ValueHolder valueHolder;
  protected int compartmentIndex;
  protected boolean hasOnlySubstanceUnits;
  protected boolean isSetInitialAmount;
  protected boolean isSetInitialConcentration;
  protected boolean hasZeroSpatialDimensions;
  protected int index;
  
  /**
   * 
   * @param nodeObject
   * @param index
   */
  public RuleObject(ASTNodeObject nodeObject, int index) {
    this.nodeObject = nodeObject;
    this.index = index;
    this.isSpecies = false;
  }
  
  /**
   * 
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param valueHolder
   */
  public RuleObject(ASTNodeObject nodeObject, int index,
    Species sp, int compartmentIndex, boolean hasZeroSpatialDimensions, ValueHolder valueHolder) {
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
   * 
   * @param time
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
   * 
   * @return
   */
  public double getValue() {
    return value;
  }
  
  /**
   * 
   * @return
   */
  public int getIndex() {
    return index;
  }
}
