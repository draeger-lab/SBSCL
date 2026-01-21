package org.simulator.fba;

import java.util.ArrayList;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.Expression;

import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.problems.LinearProgram;

/**
 * Pure Java implementation of {@link LinearProgramSolver} using ojAlgo as backend.
 *
 * This solver does not require any native libraries (no GLPK, no JNI).
 *
 * NOTE: The incremental constraint methods (addLinear*Constraint) are not
 * currently supported and will throw UnsupportedOperationException. SBSCL
 * only uses {@link #solve(LinearProgram)} with constraints already stored
 * in the {@link LinearProgram}.
 */
public class OjAlgoLinearProgramSolver implements LinearProgramSolver {

  /**
   * Optional time limit (seconds). Currently stored but not enforced.
   */
  private int timeConstraintSeconds = -1;

  @Override
  public double[] solve(LinearProgram lp) {

    if (lp == null) {
      throw new IllegalArgumentException("LinearProgram must not be null");
    }

    // Build an ojAlgo model
    final ExpressionsBasedModel model = new ExpressionsBasedModel();

    // Objective coefficients
    final double[] c = lp.getC();
    final int n = c.length;

    // Variable bounds (may or may not be set)
    final boolean hasBounds;
    final double[] lower;
    final double[] upper;
    try {
      hasBounds = lp.hasBounds();
      if (hasBounds) {
        lower = lp.getLowerbound();
        upper = lp.getUpperbound();
      } else {
        lower = null;
        upper = null;
      }
    } catch (NoSuchMethodError e) {
      // Very old SCPSolver versions would not have hasBounds(); not expected here.
      throw new IllegalStateException("SCPSolver LinearProgram is missing bounds methods", e);
    }

    // Integrality and boolean flags (may all be false in typical FBA)
    boolean[] integerVars = null;
    boolean[] booleanVars = null;
    try {
      integerVars = lp.getIsinteger();
    } catch (NoSuchMethodError e) {
      // ignore, treat as continuous
    }
    try {
      booleanVars = lp.getIsboolean();
    } catch (NoSuchMethodError e) {
      // ignore, treat as continuous
    }

    // Create variables
    final Variable[] vars = new Variable[n];
    for (int i = 0; i < n; i++) {
      Variable v = Variable.make("x" + i);

      if (hasBounds && lower != null && upper != null) {
        v.lower(lower[i]).upper(upper[i]);
      }

      boolean isBool = (booleanVars != null && i < booleanVars.length && booleanVars[i]);
      boolean isInt = (integerVars != null && i < integerVars.length && integerVars[i]);

      if (isBool) {
        v.lower(0.0).upper(1.0).integer(true);
      } else if (isInt) {
        v.integer(true);
      }

      vars[i] = v;
      model.addVariable(v);
    }

    // Objective: set variable weights and always minimise.
    // For maximisation problems, we negate the coefficients.
    final boolean isMin = lp.isMinProblem();
    for (int i = 0; i < n; i++) {
      double coeff = c[i];
      if (!isMin) {
        coeff = -coeff;
      }
      if (coeff != 0.0) {
        vars[i].weight(coeff);
      }
    }

    // Constraints from the LinearProgram
    final ArrayList<Constraint> constraints = lp.getConstraints();
    int constrIndex = 0;
    for (Constraint con : constraints) {
      if (!(con instanceof scpsolver.constraints.LinearConstraint)) {
        // Ignore non-linear constraints (not expected in FBA)
        continue;
      }

      scpsolver.constraints.LinearConstraint lc =
          (scpsolver.constraints.LinearConstraint) con;
      double[] coeff = lc.getC();
      double rhs = lc.getT();

      Expression expr = model.addExpression("c" + constrIndex++);
      for (int j = 0; j < coeff.length && j < n; j++) {
        if (coeff[j] != 0.0) {
          expr.set(vars[j], coeff[j]);
        }
      }

      if (con instanceof LinearEqualsConstraint) {
        expr.level(rhs);            // sum(coeff * x) = rhs
      } else if (con instanceof LinearBiggerThanEqualsConstraint) {
        expr.lower(rhs);            // sum(coeff * x) >= rhs
      } else if (con instanceof LinearSmallerThanEqualsConstraint) {
        expr.upper(rhs);            // sum(coeff * x) <= rhs
      } else {
        // Fallback: treat as equality
        expr.level(rhs);
      }
    }

    // Solve (minimise or maximise)
    Optimisation.Result result;
    try {
      result = model.minimise();
    } catch (Exception e) {
      // Any numerical/solver error: behave like "no solution"
      e.printStackTrace(System.err);
      return null;
    }

    if (result == null) {
      // No solution returned by ojAlgo
      return null;
    }

    // Extract solution vector
    double[] solution = new double[n];
    for (int i = 0; i < n; i++) {
      solution[i] = result.doubleValue(i);
    }

    return solution;
  }

  @Override
  public void addLinearBiggerThanEqualsConstraint(LinearBiggerThanEqualsConstraint c) {
    throw new UnsupportedOperationException(
        "Incremental constraint addition is not supported by OjAlgoLinearProgramSolver");
  }

  @Override
  public void addLinearSmallerThanEqualsConstraint(LinearSmallerThanEqualsConstraint c) {
    throw new UnsupportedOperationException(
        "Incremental constraint addition is not supported by OjAlgoLinearProgramSolver");
  }

  @Override
  public void addEqualsConstraint(LinearEqualsConstraint c) {
    throw new UnsupportedOperationException(
        "Incremental constraint addition is not supported by OjAlgoLinearProgramSolver");
  }

  @Override
  public String getName() {
    return "ojAlgo";
  }

  @Override
  public String[] getLibraryNames() {
    // No native libraries required
    return new String[0];
  }

  @Override
  public void setTimeconstraint(int seconds) {
    this.timeConstraintSeconds = seconds;
  }
}