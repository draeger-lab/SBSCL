/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.math.odes;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.Mathematics;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Runge-Kutta method.
 *
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.9
 */
public class RungeKutta_EventSolver extends AbstractDESSolver {

  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = -2034495479346567501L;

  /**
   * Logger for this class
   */
  private static final Logger logger = Logger.getLogger(RungeKutta_EventSolver.class.getName());

  /**
   * Enum representing supported RungeKutta methods
   */
  public static enum validMethod{
    RK2,
    RK4
  }
  
  /**
   * Stores temporary results for the fourth-order Runge-Kutta method.
   */
  transient protected double[][] kVals = null;

  /**
   * Helper variable for the k values.
   */
  transient protected double[] kHelp;

  /** 
   * Enum to store the Runge_Kutta Method
   */
  private final validMethod method;

  /**
   * default constructor
   */
  public RungeKutta_EventSolver() {
    super();
    this.method = validMethod.RK4;
    logger.log(Level.INFO, "No method passed. Defaulting to 'RK4'.");
  }

  /**
   * @param method
   */
  public RungeKutta_EventSolver(String method) {
    super();
    this.method = parseValidMethodOrDefault(method);
  }

  /**
   * @param stepSize
   */
  public RungeKutta_EventSolver(double stepSize) {
    super(stepSize);
    this.method = validMethod.RK4;
    logger.log(Level.INFO, "No method passed. Defaulting to 'RK4'.");
  }

  /**
   * @param stepSize
   * @param method
   */
  public RungeKutta_EventSolver(double stepSize, String method) {
    super(stepSize);
    this.method = parseValidMethodOrDefault(method);
  }

  /**
   * @param stepSize
   * @param nonnegative the nonnegative flag of the super class
   * @see AbstractDESSolver
   */
  public RungeKutta_EventSolver(double stepSize, boolean nonnegative) {
    super(stepSize, nonnegative);
    this.method = validMethod.RK4;
    logger.log(Level.INFO, "No method passed. Defaulting to 'RK4'.");
  }

  /**
   * @param stepSize
   * @param nonnegative the nonnegative flag of the super class
   * @param method
   * @see AbstractDESSolver
   */
  public RungeKutta_EventSolver(double stepSize, boolean nonnegative, String method) {
    super(stepSize, nonnegative);
    this.method = parseValidMethodOrDefault(method);
  }

  /**
   * clone constructor
   *
   * @param rkEventSolver
   */
  public RungeKutta_EventSolver(RungeKutta_EventSolver rkEventSolver) {
    super(rkEventSolver);
    this.method = rkEventSolver.method;
  }

  /**
   * Parses the string into a validMethod enum, defaults to RK4 if invalid.
   */
  private validMethod parseValidMethodOrDefault(String method) {
    try {
      return validMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      logger.log(Level.WARNING, "Unsupported method: {0}. Defaulting to 'RK4'.", method);
      return validMethod.RK4;
    }
  }

  /* (non-Javadoc)
   * @see org.simulator.math.odes.AbstractDESSolver#computeChange(org.simulator.math.odes.DESystem, double[], double, double, double[], boolean)
   */
  @Override
  public double[] computeChange(DESystem DES, double[] yTemp, double t, double h, double[] change,
      boolean steadyState)
      throws DerivativeException {

        switch(method) {
          case RK2:
            return computeRK2(DES, yTemp, t, h, change);
          
          case RK4:
            return computeRK4(DES, yTemp, t, h, change);

          default:
            throw new IllegalArgumentException("Unexpected method: " + method + ". Supported methods are: " + Arrays.toString(validMethod.values()));
        }
    }
  
  /**
   * Computes the change in state using second-order Runge-Kutta Heun's method
   * 
   * This method approximates the solution to an ODE system by evaluating the slope at the beginning and at the end of the interval, then averaging them to compute the next value.
   *
   * The formula used is:
   * k0 = h * f(t, y)
   * k1 = h * f(t + h, y + k0)
   * change = 0.5 * (k0 + k1)
   * 
   * @param DES     The differential equation system representing the model.
   * @param yTemp   The current values of system variables.
   * @param t       The current time point.
   * @param h       The integration step size.
   * @param change  An array to store the computed change.
   * @return        The array containing the computed change.
   * @throws DerivativeException
   */
  public double[] computeRK2(DESystem DES, double[] yTemp, double t, double h, double[] change)
      throws DerivativeException {

        int dim = DES.getDimension();

        double[][] kValues = new double[2][dim];
        double[] kTemp = new double[dim];
         
        // k0 = h * f(t, yTemp)
        DES.computeDerivatives(t, yTemp, kValues[0]);
        Mathematics.svMult(h, kValues[0], kValues[0]);

        // k1 = h * f(t + h, yTemp + k0)
        Mathematics.vvAdd(kValues[0], yTemp, kTemp);
        DES.computeDerivatives(t + h, kTemp, kValues[1]);
        Mathematics.svMult(h, kValues[1], kValues[1]);

        // Combine all k's
        Mathematics.svvAddAndScale(0.5, kValues[0], kValues[1], change);
        
        return change;

    }

  /**
   * Computes the change in state using the classical fourth-order Runge-Kutta method.
   *
   * RK4 evaluates the derivative at four points in each interval and combines them using a weighted average.
   *
   * The formula used is:
   * k0 = h * f(t, y)
   * k1 = h * f(t + h/2, y + k0/2)
   * k2 = h * f(t + h/2, y + k1/2)
   * k3 = h * f(t + h, y + k2)
   * change = (1/6) * (k0 + 2*k1 + 2*k2 + k3)
   *
   * @param DES     The differential equation system representing the model.
   * @param yTemp   The current values of system variables.
   * @param t       The current time point.
   * @param h       The integration step size.
   * @param change  An array to store the computed changes.
   * @return        The array containing the computed change in system state.
   * @throws DerivativeException 
   */
  public double[] computeRK4(DESystem DES, double[] yTemp, double t, double h, double[] change)
    throws DerivativeException {

      int dim = DES.getDimension();
      if ((kVals == null) || (kVals.length != 4) || (kVals[0].length != dim)) {
        // "static" vectors which are allocated only once
        kVals = new double[4][dim];
        kHelp = new double[dim];
      }

      double halfStep = h * 0.5;
      double tMid = t + halfStep;
      double tEnd = t + h;

      // k0 = h * f(t, yTemp)
      DES.computeDerivatives(t, yTemp, kVals[0]);
      Mathematics.svMult(h, kVals[0], kVals[0]);

      // k1 = h * f(t + h/2, y + k0/2)
      Mathematics.svvAddScaled(0.5, kVals[0], yTemp, kHelp);
      DES.computeDerivatives(tMid, kHelp, kVals[1]);
      Mathematics.svMult(h, kVals[1], kVals[1]);

      // k2 = h * f(t + h/2, y + k1/2)
      Mathematics.svvAddScaled(0.5, kVals[1], yTemp, kHelp);
      DES.computeDerivatives(tMid, kHelp, kVals[2]);
      Mathematics.svMult(h, kVals[2], kVals[2]);

      // k3 = h * f(t + h, y + k2)
      Mathematics.vvAdd(yTemp, kVals[2], kHelp);
      DES.computeDerivatives(tEnd, kHelp, kVals[3]);
      Mathematics.svMult(h, kVals[3], kVals[3]);

      // combining all k's
      for (int i = 0; i < dim; i++) {
        change[i] = (kVals[0][i] + (2 * (kVals[1][i] + kVals[2][i])) + kVals[3][i]) / 6d;
      }
      return change;

    }

  /* (non-Javadoc)
   * @see org.simulator.math.odes.AbstractDESSolver#getName()
   */
  @Override
  public String getName() {
    return "Runge-Kutta event solver (" + method + ")";
  }

  /* (non-Javadoc)
   * @see org.simulator.math.odes.AbstractDESSolver#clone()
   */
  @Override
  public RungeKutta_EventSolver clone() {
    return new RungeKutta_EventSolver(this);
  }

  /* (non-Javadoc)
   * @see org.simulator.math.odes.AbstractDESSolver#hasSolverEventProcessing()
   */
  @Override
  protected boolean hasSolverEventProcessing() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.simulator.math.odes.DESSolver#getKISAOTerm()
   */
  @Override
  public int getKiSAOterm() {
    return 64;
  }
}
