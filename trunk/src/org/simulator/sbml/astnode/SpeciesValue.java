/* 
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Species;
import org.simulator.sbml.ValueHolder;

/**
 * This class computes and stores values of ASTNodes that refer to a species.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class SpeciesValue extends ASTNodeValue {
  /**
   * The corresponding species
   */
	protected Species s;
	
	/**
	 * The id of the species
	 */
  protected String id;
  
  /**
   * The value holder that stores the current simulation results
   */
  protected ValueHolder valueHolder;
  
  /**
   * Has the species an initial amount set?
   */
  protected boolean isSetInitialAmount;
  
  /**
   * The hasOnlySubstanceUnits attribute of the species
   */
  protected boolean hasOnlySubstanceUnits;
  
  /**
   * Has the species an initial concentration set?
   */
  protected boolean isSetInitialConcentration;
  
  /**
   * The position of the species value in the Y vector of the value holder
   */
  protected int position;
  
  /**
   * The position of the compartment value of the species in the Y vector of the value holder
   */
  protected int compartmentPosition;
  
  /**
   * Has the compartment of the species no spatial dimensions?
   */
  protected boolean zeroSpatialDimensions;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param s
   * @param valueHolder
   * @param position
   * @param compartmentPosition
   * @param zeroSpatialDimensions
   */
  public SpeciesValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    Species s, ValueHolder valueHolder, int position, int compartmentPosition, boolean zeroSpatialDimensions) {
    super(interpreter, node);
    this.s = s;
    this.id = s.getId();
    this.valueHolder = valueHolder;
    this.isSetInitialAmount = s.isSetInitialAmount();
    this.isSetInitialConcentration = s.isSetInitialConcentration();
    this.hasOnlySubstanceUnits = s.getHasOnlySubstanceUnits();
    this.position = position;
    this.compartmentPosition = compartmentPosition;
    this.zeroSpatialDimensions = zeroSpatialDimensions;
  }
  
  /*
   * (non-Javadoc)
   * @see org.simulator.sbml.astnode.ASTNodeValue#computeDoubleValue()
   */
  @Override
  protected void computeDoubleValue() {
    double compartmentValue = valueHolder
        .getCurrentValueOf(compartmentPosition);
    if ((compartmentValue == 0d) || zeroSpatialDimensions) {
      doubleValue = valueHolder.getCurrentValueOf(position);
    } else if (isSetInitialAmount && !hasOnlySubstanceUnits) {
      doubleValue = valueHolder.getCurrentValueOf(position) / compartmentValue;
      
    }
    else if (isSetInitialConcentration && hasOnlySubstanceUnits) {
      doubleValue = valueHolder.getCurrentValueOf(position) * compartmentValue;
    } else {
      doubleValue = valueHolder.getCurrentValueOf(position);
      
    }
  }
  
}
