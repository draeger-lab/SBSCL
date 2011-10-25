/*
 * $Id:  RateRuleObject.java 15:31:57 keller $
 * $URL: RateRuleObject.java $
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
package org.sbml.simulator.math.astnode;

import java.util.List;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.simulator.math.ValueHolder;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class RateRuleObject extends RuleObject{
  private boolean isCompartment;
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
   * 
   * @param nodeObject
   * @param index
   * @param sp
   * @param compartmentIndex
   * @param valueHolder
   */
  public RateRuleObject(ASTNodeObject nodeObject, int index,
    Species sp, int compartmentIndex, ValueHolder valueHolder) {
    super(nodeObject, index, sp, compartmentIndex, valueHolder);
  }
  
  /**
   * 
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
   * 
   * @param changeRate
   * @param time
   * @throws SBMLException
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
