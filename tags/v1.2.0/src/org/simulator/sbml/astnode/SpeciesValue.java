/*
 * $Id: SpeciesValue.java 205 2012-05-05 11:57:39Z andreas-draeger $
 * $URL: http://svn.code.sf.net/p/simulation-core/code/trunk/src/org/simulator/sbml/astnode/SpeciesValue.java $
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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Species;
import org.simulator.sbml.SBMLValueHolder;

/**
 * This class computes and stores values of ASTNodes that refer to a species.
 * @author Roland Keller
 * @version $Rev: 205 $
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
  protected SBMLValueHolder valueHolder;
  
  /**
   * Is the Y value of the species referring to an amount?
   */
  protected boolean isAmount;
  
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
  public SpeciesValue(ASTNodeInterpreter interpreter, ASTNode node,
    Species s, SBMLValueHolder valueHolder, int position, int compartmentPosition, boolean zeroSpatialDimensions, boolean isAmount) {
    super(interpreter, node);
    this.s = s;
    this.id = s.getId();
    this.valueHolder = valueHolder;
    this.isAmount = isAmount;
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
		if (isAmount && !hasOnlySubstanceUnits) {
			double compartmentValue = valueHolder
					.getCurrentValueOf(compartmentPosition);
			if ((compartmentValue == 0d) || zeroSpatialDimensions) {
				doubleValue = valueHolder.getCurrentValueOf(position);
			} else {
				doubleValue = valueHolder.getCurrentValueOf(position)
						/ compartmentValue;

			}
		} else if (!isAmount && hasOnlySubstanceUnits) {
			double compartmentValue = valueHolder
					.getCurrentValueOf(compartmentPosition);
			if ((compartmentValue == 0d) || zeroSpatialDimensions) {
				doubleValue = valueHolder.getCurrentValueOf(position);
			} else {
				doubleValue = valueHolder.getCurrentValueOf(position)
						* compartmentValue;
			}
		} else {
			doubleValue = valueHolder.getCurrentValueOf(position);

		}
	}
  
}
