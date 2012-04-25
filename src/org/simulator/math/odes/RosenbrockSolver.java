/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of ODE Toolkit: a free application for solving
 * systems of ordinary differential equations.
 * 
 * Copyright (C) 2002-2011 Beky Kotcon, Samantha Mesuro, Daniel 
 * Rozenfeld, Anak Yodpinyanee, Andres Perez, Eric Doi, Richard 
 * Mehlinger, Steven Ehrlich, Martin Hunt, George Tucker, Peter 
 * Scherpelz, Aaron Becker, Eric Harley, and Chris Moore
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


import java.math.BigDecimal;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.Mathematics;
import org.simulator.math.MatrixOperations;
import org.simulator.math.MatrixOperations.MatrixException;

/**
 * An implementation of Rosenbrock's method to approximate ODE
 * solutions.
 * <p>
 * References: William H. Press, Saul A. Teukolsky, William T. Vetterling, and
 * Brian P. Flannery. Numerical recipes in C. Cambridge Univ. Press Cambridge,
 * 1992, pp. 738-747.
 * <p>
 * This solver has been adapted from ODE Toolkit: a free application for solving
 * systems of ordinary differential equations.
 * 
 * @author Chris Moore
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class RosenbrockSolver extends AdaptiveStepsizeIntegrator {


	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3446213991016212781L;

	/**
	 * Constants used to adapt the stepsize according to the error in the last
	 * step (see rodas.f)
	 */
	public static final double SAFETY = 0.9, fac1 = 1.0 / 6.0, fac2 = 5,
			PWR = 0.25;

	// The constants cX, dX, aXY, and cXY, are coefficients used in method
	// step()
	// Given in a webnote (see _Numerical Recipies_3rd ed) ----give
	// reference-----

	/** Constants for solving */
	public static final double c2 = 0.386, c3 = 0.21, c4 = 0.63;

	/** Constants for solving */
	public static final double a21 = 1.544000000000000,
			a31 = 0.9466785280815826, a32 = 0.2557011698983284,
			a41 = 3.314825187068521, a42 = 2.896124015972201,
			a43 = 0.9986419139977817, a51 = 1.221224509226641,
			a52 = 6.019134481288629, a53 = 12.53708332932087,
			a54 = -0.6878860361058950;

	/** Constants for solving */
	public static final double gam = 0.250;

	/** Constants for solving */
	public static final double c21 = -5.668800000000000,
			c31 = -2.430093356833875, c32 = -0.2063599157091915,
			c41 = -0.1073529058151375, c42 = -9.594562251023355,
			c43 = -20.47028614809616, c51 = 7.496443313967647,
			c52 = -10.24680431464352, c53 = -33.99990352819905,
			c54 = 11.70890893206160, c61 = 8.083246795921522,
			c62 = -7.981132988064893, c63 = -31.52159432874371,
			c64 = 16.31930543123136, c65 = -6.058818238834054;

	public static final double d1 = 0.25, d2 = 0.1043, d3 = 0.1035,
			d4 = -0.0362;

	/**
	 * the minimum acceptable value of relTol - attempts to obtain higher
	 * accuracy than this are usually very expensive
	 */
	public static final double RELMIN = 1.0E-12;


	/** maximum stepsize */
	private double hMax;
	/** minimum stepsize */
	private double hMin;

	/** the current value of the independent variable */
	private double t;

	/** the current step size */
	private double h;

	/** factor for calculating error value used in adjusting step size */
	double sk;
	/**
	 * factor used for adjusting the step size, divide current step size by
	 * hAdap to get new step size
	 */
	double hAdap;

	/** The number of equations */
	int numEqn;

	/** the current values of the dependent variables */
	private double[] y;

	/**
	 * Saving of older values of y.
	 */
	private double[] oldY;

	/** arrays to store derivative evaluations and intermediate steps */
	private double[] f1, f2, f3, f4, f5, f6, k1, k2, k3, k4, k5;

	/**
	 * array that is y with approximated errors added on, used for comparing y
	 * to y+yerr
	 */
	double[] yNew;

	/** array that holds approximate errors in the values in y */
	private double[] yerr;

	/** helper array to hold intermediate values */
	double[] yTemp, ya, yb, g0, g1, g2, g1x, g2x, DFDX;
	/** helper array to hold intermediate values */
	int[] indx;
	/** helper array to hold intermediate values */
	double[][] JAC, FAC, I;

	/** Keep track whether the thread is killed or not */
	boolean stop;

	/**
	 * 
	 */
	private double[] timePoints;

	/**
	 * NaNs that are set before the calculation are ignored.
	 */
	private boolean[] ignoreNaN;

	private static final double precisionEventsAndRules = 1E-7;
	
	/**
	 * default constructor
	 * @param size
	 * @param stepsize
	 */
	public RosenbrockSolver() {
		super();
	}

	/**
	 * 
	 * @param size
	 * @param stepsize
	 */
	public RosenbrockSolver(int size, double stepsize) {
		super(stepsize);
		init(size, stepsize, 2);
	}

	/**
	 * clone constructor
	 * @param solver
	 */
	public RosenbrockSolver(RosenbrockSolver solver) {
		super(solver);
		init(solver.getNumEquations(), solver.getStepSize(), 2);
	}

	/**
	 * initialization function
	 * @param size
	 * @param stepsize
	 * @param nTimepoints
	 */
	private void init(int size, double stepsize, int nTimepoints) {
		numEqn = size;

		hMin = 1E-14d;
		this.setStepSize(stepsize);
		hMax = Math.min(stepsize, 0.1d);

		stop = false;
		timePoints = new double[nTimepoints]; 
		// allocate arrays
		y = new double[numEqn];
		f1 = new double[numEqn];
		f2 = new double[numEqn];
		f3 = new double[numEqn];
		f4 = new double[numEqn];
		f5 = new double[numEqn];
		f6 = new double[numEqn];
		k1 = new double[numEqn];
		k2 = new double[numEqn];
		k3 = new double[numEqn];
		k4 = new double[numEqn];
		k5 = new double[numEqn];
		yNew = new double[numEqn];
		yerr = new double[numEqn];
		yTemp = new double[numEqn];
		oldY = new double[numEqn];
		ya = new double[numEqn];
		yb = new double[numEqn];
		g0 = new double[numEqn];
		g1 = new double[numEqn];
		g2 = new double[numEqn];
		g1x = new double[numEqn];
		g2x = new double[numEqn];
		DFDX = new double[numEqn];
		indx = new int[numEqn];
		JAC = new double[numEqn][numEqn];
		FAC = new double[numEqn][numEqn];
		I = new double[numEqn][numEqn];
		ignoreNaN=new boolean[numEqn];
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#clone()
	 */
	public AbstractDESSolver clone() {
		return new RosenbrockSolver(this.getNumEquations(),this.getStepSize());
	}

	
	/**
	 * This function tries to make a time step.
	 * @param the differential equation system
	 * @return the error
	 * @throws DerivativeException
	 */
	public double step(DESystem DES) throws DerivativeException {
		double largestError = 0;

		DES.computeDerivatives(t, y, g0);
		for (int j = 0; j < numEqn; j++) {
			System.arraycopy(y, 0, ya, 0, numEqn);
			ya[j] += h;
			System.arraycopy(y, 0, yb, 0, numEqn);
			yb[j] += 2 * h;
			DES.computeDerivatives(t, ya, g1);
			DES.computeDerivatives(t, yb, g2);
			for (int q = 0; q < numEqn; q++) {
				JAC[q][j] = (-3 * g0[q] + 4 * g1[q] - g2[q]) / (2 * h);
			}
		}
		for (int i = 0; i < numEqn; i++) {
			for (int j = 0; j < numEqn; j++) {
				if (i == j) {
					I[i][j] = 1;
				} else {
					I[i][j] = 0;
				}
			}
		}

		for (int i = 0; i < numEqn; i++)
			for (int j = 0; j < numEqn; j++)
				FAC[i][j] = I[i][j] / (gam * h) - JAC[i][j];

		// Forward difference approx for derivative of f
		// WRT the independent variable
		DES.computeDerivatives(t + h, y, g1x);
		DES.computeDerivatives(t + 2 * h, y, g2x);
		for (int i = 0; i < numEqn; i++)
			DFDX[i] = g0[i] * -3 / (2 * h) + g1x[i] * 2 / h + g2x[i] * -1
			/ (2 * h);

		// Here the work of taking the step begins
		// It uses the derivatives calculated above
		DES.computeDerivatives(t, yTemp, f1);
		for (int i = 0; i < numEqn; i++) {
			k1[i] = f1[i] + DFDX[i] * h * d1;
		}

		try {
			MatrixOperations.ludcmp(FAC, indx);
		} catch (MatrixException e) {
			throw new DerivativeException(
					"Rosenbrock solver returns an error due to singular matrix.");
		}

		MatrixOperations.lubksb(FAC, indx, k1);

		for (int i = 0; i < numEqn; i++) {
			yTemp[i] = y[i] + k1[i] * a21;
		}
		DES.computeDerivatives(t + c2 * h, yTemp, f2);
		for (int i = 0; i < numEqn; i++)
			k2[i] = f2[i] + DFDX[i] * h * d2 + k1[i] * c21 / h;
		MatrixOperations.lubksb(FAC, indx, k2);

		for (int i = 0; i < numEqn; i++) {
			yTemp[i] = y[i] + k1[i] * a31 + k2[i] * a32;
		}
		DES.computeDerivatives(t + c3 * h, yTemp, f3);
		for (int i = 0; i < numEqn; i++)
			k3[i] = f3[i] + DFDX[i] * h * d3 + k1[i] * c31 / h + k2[i] * c32
			/ h;
		MatrixOperations.lubksb(FAC, indx, k3);

		for (int i = 0; i < numEqn; i++) {
			yTemp[i] = y[i] + k1[i] * a41 + k2[i] * a42 + k3[i] * a43;
		}
		DES.computeDerivatives(t + c4 * h, yTemp, f4);
		for (int i = 0; i < numEqn; i++)
			k4[i] = f4[i] + DFDX[i] * h * d4 + k1[i] * c41 / h + k2[i] * c42
			/ h + k3[i] * c43 / h;
		MatrixOperations.lubksb(FAC, indx, k4);

		for (int i = 0; i < numEqn; i++) {
			yTemp[i] = y[i] + k1[i] * a51 + k2[i] * a52 + k3[i] * a53 + k4[i]
					* a54;
		}
		DES.computeDerivatives(t + h, yTemp, f5);
		for (int i = 0; i < numEqn; i++)
			k5[i] = f5[i] + k1[i] * c51 / h + k2[i] * c52 / h + k3[i] * c53 / h
			+ k4[i] * c54 / h;
		MatrixOperations.lubksb(FAC, indx, k5);

		for (int i = 0; i < numEqn; i++) {
			yTemp[i] += k5[i];
		}
		DES.computeDerivatives(t + h, yTemp, f6);
		for (int i = 0; i < numEqn; i++)
			yerr[i] = f6[i] + k1[i] * c61 / h + k2[i] * c62 / h + k3[i] * c63
			/ h + k4[i] * c64 / h + k5[i] * c65 / h;
		MatrixOperations.lubksb(FAC, indx, yerr);

		for (int i = 0; i < numEqn; i++) {
			yNew[i] = yTemp[i] + yerr[i];
		}

		for (int i = 0; i < numEqn; i++) {
			if(!ignoreNaN[i]) {
				sk = absTol + relTol * Math.max(Math.abs(y[i]), Math.abs(yNew[i]));
				largestError += Math.pow(yerr[i] / sk, 2);

				if ((Double.isInfinite(yTemp[i]) || Double.isNaN(yTemp[i])))
					return -1;
			}
		}
		largestError = Math.pow(largestError / numEqn, 0.5);
		return largestError;

	}

	/**
	 * Returns an approximation to the error involved with the current
	 * arithmetic implementation
	 * 
	 * @return the approximation as described above
	 */
	public double unitRoundoff() {
		double u;
		double one_plus_u;

		u = 1.0;
		one_plus_u = 1.0 + u;
		// Check to see if the number 1.0 plus some positive offset
		// computes to be the same as one.
		while (one_plus_u != 1.0) {
			u /= 2.0;
			one_plus_u = 1.0 + u;
		}
		u *= 2.0; // Go back one step

		return (u);
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#getName()
	 */
	public String getName() {
		return "Rosenbrock solver";
	}

	/**
	 * 
	 * @return the number of equations in the system
	 */
	public int getNumEquations() {
		return numEqn;
	}

	/* (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#computeChange(org.simulator.math.odes.DESystem, double[], double, double, double[], boolean)
	 */
	public double[] computeChange(DESystem DES, double[] y2, double time,
			double currentStepSize, double[] change, boolean steadyState) throws DerivativeException {
		if((y == null) || (y.length == 0) || (y.length != y2.length)) {
			init(DES.getDimension(), this.getStepSize(), 2);
		}
		this.hMax = currentStepSize;
		boolean hasDerivatives = true;

		if (DES instanceof EventDESystem) {
			EventDESystem EDES = (EventDESystem) DES;
			if(EDES.getNoDerivatives()) {
				hasDerivatives = false;
			}
		}

		double timeEnd = BigDecimal.valueOf(time).add(BigDecimal.valueOf(currentStepSize)).doubleValue();
		try {

			double localError = 0;
			int solutionIndex = 0;

			// temporary variable used when adjusting stepsize
			double tNew;

			// was the last step successful? (do we have to repeat the step
			// with a smaller stepsize?)
			boolean lastStepSuccessful = false;

			// Compute epsilon. This is the smallest double X such that
			// 1.0+X!=1.0
			double eps = unitRoundoff();
			// Restrict relative error tolerance to be at least as large as
			// 2*eps+RELMIN to avoid limiting precision difficulties arising
			// from impossible accuracy requests
			double relMin = 2.0d * eps + RELMIN;
			if (relTol < relMin) {
				relTol = relMin;
			}

			// set t to the initial independent value and y[] to the
			// initial dependent values
			t = time;
			timePoints[0] = t;

			if(y.length!=y2.length) {
				y=y2.clone();
				ignoreNaN=new boolean[y.length];
			}
			else {
				System.arraycopy(y2, 0, y, 0, y.length);
			}

			for(int i=0;i!=y.length;i++) {
				if(Double.isInfinite(y[i]) || (Double.isNaN(y[i]))) {
					ignoreNaN[i]=true;
				}
				else{
					ignoreNaN[i]=false;
				}
			}
			// add the initial conditions to the solution matrix and let all
			// point
			// ready listeners know about it

			// set initial stepsize - we want to try the maximum stepsize to
			// begin
			// with and move to smaller values if necessary
			h = hMax;
			stop = false;

			while (!stop) {

				// if the last step was successful (t was updated)...
				if (lastStepSuccessful) {

					// ... and the current t differs from the last recorded one
					// by
					// at least stepsize...
					if (Math.abs(timePoints[solutionIndex] - t) >= Math
							.abs(currentStepSize)) {

						// ...we want to record the current point in the
						// solution
						// matrix and notify all pointReadyListeners of the
						// point
						solutionIndex++;
						timePoints[solutionIndex] = t;

					}
				}

				// see if we're done
				if (t >= timeEnd) {
					if (DES instanceof EventDESystem) {
						EventDESystem EDES = (EventDESystem) DES;
						if (((EDES.getEventCount() > 0) && (!steadyState)) || (EDES.getRuleCount() > 0)) {
							processEventsAndRules(true, EDES, timeEnd, t-h, yTemp);
						}
						System.arraycopy(yTemp, 0, y, 0, numEqn);
					}
					Mathematics.vvSub(y, y2, change);
					break;
				}
				// copy the current point into yTemp
				System.arraycopy(y, 0, yTemp, 0, numEqn);
				try {
					// take a step
					if(hasDerivatives) {
						localError = step(DES);
					}
					else {
						localError = 0;
					}
				} catch (Exception ex) {
					stop = true;
				}
				//        if (localError == -1) {
				//          new Error("Infinity or NaN encountered by the RB solver... stopping solve");
				//          stop = true;
				//        }

				// good step
				if (((!Double.isNaN(localError)) && (localError!=-1) && (localError <= 1.0) && !stop)) {
					this.setUnstableFlag(false);



					System.arraycopy(y, 0, oldY, 0, numEqn);
					System.arraycopy(yTemp, 0, y, 0, numEqn);

					boolean changed=false;
					double newTime = BigDecimal.valueOf(t).add(BigDecimal.valueOf(h)).doubleValue();
					if ((DES instanceof EventDESystem) && (!steadyState)) {
						EventDESystem EDES = (EventDESystem) DES;
						if ((EDES.getEventCount() > 0) || (EDES.getRuleCount() > 0)) {
							changed=processEventsAndRules(true, EDES, Math.min(newTime,timeEnd), t, yTemp);
						}
					}

					if(changed) {
						//if(h/10>hMin) {
							if(h>precisionEventsAndRules)  {
								//h=h/10;
								h = Math.max(h / 10,precisionEventsAndRules);
								if(h - precisionEventsAndRules < precisionEventsAndRules) {
									h = precisionEventsAndRules;
								}
								System.arraycopy(oldY, 0, y, 0, numEqn);
							}
							else {
								System.arraycopy(yTemp, 0, y, 0, numEqn);
								t=Math.min(newTime,timeEnd);
								if(timeEnd-t-h<hMin) {
									h = timeEnd - t;
								}
								lastStepSuccessful = true;
							}
					}
					else {
						t=Math.min(newTime,timeEnd);
						// change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
						hAdap = Math.max(fac1,
								Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
						h = h / hAdap;
						if(timeEnd-t-h<hMin) {
							h = timeEnd - t;
						}
						lastStepSuccessful = true;
					}





				} else {

					// if we just tried to use the minimum stepsize and still
					// failed to achieve the desired accuracy, it's useless to
					// continue, so we stop
					if (Math.abs(h) <= Math.abs(hMin)) {
						throw new DerivativeException("Requested tolerance could not be achieved, even at the minumum stepsize.  Please increase the tolerance or decrease the minimum stepsize.");
					}

					// change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
					if((Double.isNaN(localError)) || (localError==-1) || (stop==true)) {
						hAdap=2;
					} 
					else {
						hAdap = Math.max(fac1,
								Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
					}
					h = h / hAdap;
					if(timeEnd-t-h<hMin) {
						h = timeEnd - t;
					}
					tNew = t + h;
					if (tNew == t) {
						throw new DerivativeException("Stepsize underflow in Rosenbrock solver");
					}
					lastStepSuccessful = false;
				}

				// check bounds on the new stepsize
				if (Math.abs(h) < hMin) {
					h = hMin;

				} else if (Math.abs(h) > hMax) {
					h = hMax;
				}

				stop = false;
			}

			//solveDone();
		} catch (OutOfMemoryError e) {
			throw new DerivativeException("Out of memory : try reducing solve span or increasing step size.");
		}


		return change;
	}

	/*
	 * (non-Javadoc)
	 * @see org.simulator.math.odes.AbstractDESSolver#hasSolverEventProcessing()
	 */
	protected boolean hasSolverEventProcessing() {
		return true;
	}


}
