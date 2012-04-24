/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * for efficient numerial simulation of biological models.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
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

import java.util.List;
import org.sbml.jsbml.Species;
import org.simulator.sbml.ValueHolder;

/**
 * This class can compute and store the value of a rate rule together with the variable of the rule.
 * @author Roland Keller
 * @version $Rev$
 */
public class RateRuleObject extends RuleObject{
  /**
   * Is the variable a compartment?
   */
	private boolean isCompartment;
	
	/**
	 * The indexes in the Y vector of the value holder of the species that the compartment contains (if applicable)
	 */
  private List<Integer> speciesIndices;
  
  /**
   * 
   * @param nodeObject
   * @param index
   */
  public RateRuleObject(ASTNodeObject nodeObject, int index) {
    super(nodeObject, index);
  }

  
  /**
   * Constructor for a rule with a species as variable
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param hasZeroSpatialDimensions
   * @param valueHolder
   */
  public RateRuleObject(ASTNodeObject nodeObject, int index,
    Species sp, int compartmentIndex, boolean hasZeroSpatialDimensions, ValueHolder valueHolder) {
    super(nodeObject, index, sp, compartmentIndex, hasZeroSpatialDimensions, valueHolder);
  }
  
  /**
   * Constructor for a rule with a compartment as variable
   * @param nodeObject
   * @param index
   * @param speciesIndices
   * @param valueHolder
   */
  public RateRuleObject(ASTNodeObject nodeObject, int index,
    List<Integer> speciesIndices, ValueHolder valueHolder) {
    super(nodeObject, index);
    this.isCompartment = true;
    this.speciesIndices = speciesIndices;
  }
  
  /**
   * Processes the rule and saves the new value of the corresponding variable in the changeRate vector. 
   * @param changeRate
   * @param Y
   * @param time
   */
  public void processRule(double[] changeRate, double[] Y, double time) {
    changeRate[index] = processAssignmentVariable(time);
    
    // when the size of a compartment changes, the concentrations of the
    // species located in this compartment have to change as well
    if (isCompartment) {
      if(speciesIndices!=null) {
        for (int speciesIndex:speciesIndices) {
          changeRate[speciesIndex] = -changeRate[index]
                  * Y[speciesIndex] / Y[index];
        }
      }
    }
  }
}
