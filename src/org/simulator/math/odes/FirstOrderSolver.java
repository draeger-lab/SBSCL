/*
 * $Id$ $URL:
 * https
 * ://sbml-simulator.svn.sourceforge.net/svnroot/sbml-simulator/trunk/src/org
 * /sbml/simulator/math/odes/FirstOrderSolver.java $
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

package org.simulator.math.odes;

import java.util.logging.Logger;

import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.util.FastMath;
import org.simulator.math.Mathematics;

/**
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public abstract class FirstOrderSolver extends AbstractDESSolver {
  
  /**
     * 
     */
  private static final Logger logger = Logger.getLogger(FirstOrderSolver.class
      .getName());
  
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  
  /**
	 * 
	 */
  
  /**
   * 
   */
  private double[] integrationResult;
  
  protected AbstractIntegrator integrator;
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.odes.AbstractDESSolver#setStepSize(double)
   */
  public void setStepSize(double stepSize) {
    super.setStepSize(stepSize);
    createIntegrator();
  }
  
  /**
	 * 
	 */
  public FirstOrderSolver() {
    super();
    createIntegrator();
    addHandler();
  }
  
  /**
   * @param stepSize
   */
  public FirstOrderSolver(double stepSize) {
    super(stepSize);
    createIntegrator();
    addHandler();
  }
  
  /**
   * @param stepSize
   * @param nonnegative
   */
  public FirstOrderSolver(double stepSize, boolean nonnegative) {
    super(stepSize, nonnegative);
    createIntegrator();
    addHandler();
  }
  
  /**
   * @param firstOrderSolver
   */
  public FirstOrderSolver(FirstOrderSolver firstOrderSolver) {
    super(firstOrderSolver);
    createIntegrator();
    addHandler();
    
  }
  
  /**
	 * 
	 */
  private void addHandler() {
    integrator.addEventHandler(this, 1, 1, 1);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.odes.AbstractDESSolver#clone()
   */
  @Override
  public abstract FirstOrderSolver clone();
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.odes.AbstractDESSolver#computeChange(org.sbml
   * .simulator.math.odes.DESystem, double[], double, double, double[])
   */
  @Override
  public double[] computeChange(DESystem DES, double[] y, double t,
    double stepSize, double[] change) throws IntegrationException {
    if(integrationResult==null) {
      integrationResult = new double[y.length];
    }
    
    double tstart = t;
    double tend = t + stepSize;
    if (FastMath.abs(tstart - tend) <= (1.0e-12 * FastMath.max(
      FastMath.abs(tstart), FastMath.abs(tend)))) {
      for (int i = 0; i != change.length; i++) {
        change[i] = 0;
      }
    } else {
      try {
        integrator.integrate(DES, tstart, y, tend, integrationResult);
        Mathematics.vvSub(integrationResult, y, change);
      } catch (Exception e) {
        setUnstableFlag(true);
        logger.fine(e.getLocalizedMessage());
      }
    }
    return change;
  }
  
  
  /**
	 * 
	 */
  protected abstract void createIntegrator();
  
  /**
   * @return integrator
   */
  public AbstractIntegrator getIntegrator() {
    return integrator;
  }
  
}
