/*
 * --------------------------------------------------------------------- This
 * file is part of SBMLsimulator, a Java-based simulator for models of
 * biochemical processes encoded in the modeling language SBML.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Species;
import org.simulator.sbml.ValueHolder;

/**
 * 
 * @author Roland Keller
 * @version $Rev: 22 $
 * @since 1.0
 */
public class SpeciesValue extends ASTNodeObject {
  protected Species s;
  protected String id;
  protected ValueHolder valueHolder;
  protected boolean isSetInitialAmount;
  protected boolean hasOnlySubstanceUnits;
  protected boolean isSetInitialConcentration;
  protected int position;
  protected int compartmentPosition;
  protected boolean zeroSpatialDimensions;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param s
   * @param valueHolder
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
   * 
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
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
