/*
 * $Id: StoichiometryObject.java 15:12:31 keller $ $URL:
 * StoichiometryObject.java $
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

import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.simulator.sbml.EfficientASTNodeInterpreter;
import org.simulator.sbml.ValueHolder;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class StoichiometryObject {
  private double stoichiometry;
  private double time;
  private int speciesIndex;
  private int speciesRefIndex;
  private boolean constantStoichiometry;
  private boolean constantQuantity;
  private boolean boundaryCondition;
  private boolean isSetStoichiometryMath;
  private int compartmentIndex;
  protected ValueHolder valueHolder;
  private ASTNode math;
  private String id;
  private SpeciesReference sr;
  private Map<String, Double> stoichiometricCoefHash;
  private double[] Y;
  private EfficientASTNodeInterpreter nodeInterpreter;
  private int reactionIndex;
  private boolean isReactant;
  private boolean inConcentration;
  private boolean stoichiometrySet;
  
  /**
   * 
   * @param sr
   * @param speciesIndex
   * @param speciesRefIndex
   * @param stoichiometricCoefHash
   * @param Y
   * @param nodeInterpreter
   * @param kineticLawObject
   * @param isReactant
   */
  @SuppressWarnings("deprecation")
  public StoichiometryObject(SpeciesReference sr, int speciesIndex,
    int speciesRefIndex, int compartmentIndex, Map<String, Double> stoichiometricCoefHash, ValueHolder valueHolder,
    double[] Y, EfficientASTNodeInterpreter nodeInterpreter, int reactionIndex,
    Set<String> inConcentrationSet, boolean isReactant) {
    this.isSetStoichiometryMath = sr.isSetStoichiometryMath();
    this.valueHolder=valueHolder;
    this.compartmentIndex=compartmentIndex;
    if (isSetStoichiometryMath) {
      math = sr.getStoichiometryMath().getMath();
    }
    this.sr = sr;
    this.reactionIndex = reactionIndex;
    this.id = sr.getId();
    this.speciesIndex = speciesIndex;
    this.speciesRefIndex = speciesRefIndex;
    this.constantStoichiometry = false;
    if(sr.isSetConstant()) {
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
      if(inConcentrationSet.contains(s.getId())) {
        inConcentration=true;
      }
    }
    this.stoichiometricCoefHash = stoichiometricCoefHash;
    this.Y = Y;
    this.nodeInterpreter = nodeInterpreter;
    this.time = Double.NaN;
    this.isReactant = isReactant;
    
    computeStoichiometricValue();
  }
  
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
    if(inConcentration) {
      value = value
          / valueHolder.getCurrentValueOf(compartmentIndex);
    }
    changeRate[speciesIndex]+=value;
    
  }
  
  /**
   * 
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
   * 
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
        stoichiometry = nodeInterpreter.compileDouble(math);
        stoichiometrySet=true;
      } else if ((!sr.isSetStoichiometry()) && (sr.getLevel() >= 3)) {
        stoichiometry = 1d;
        stoichiometrySet=false;
      } else {
        stoichiometry = sr.getCalculatedStoichiometry();
        if(id.equals("")) {
          stoichiometrySet=true;
        }
        else {
          stoichiometrySet=false;
        }
      }
    }
    
    
  }
  
  /**
   * 
   * @return
   */
  public int getSpeciesIndex() {
    return speciesIndex;
  }
  
  /**
   * 
   * @return
   */
  public boolean isReactant() {
    return isReactant;
  }
  
  /**
   * 
   */
  public void refresh() {
    this.computeStoichiometricValue();
  }
  
}
