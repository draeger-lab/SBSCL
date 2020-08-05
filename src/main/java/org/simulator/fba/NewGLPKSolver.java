package org.simulator.fba;

import org.gnu.glpk.GlpkSolver;
import scpsolver.constraints.*;
import scpsolver.lpsolver.GLPKSolver;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.problems.LinearProgram;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class for solving the linear programs.
 *
 * This class is added temporarily till issue of freeing the memory of the
 * instance of the GlpkSolver in the solve() method gets resolved in SCPSolver.
 *
 * Currently, while running the SBML Test Suite, it crashes in between due to
 * the memory allocation error for GlpkSolver class.
 *
 * The error: glp_free: memory allocation error
 *            Error detected in file env/alloc.c at line 72
 *
 * This happened as the memory of the GlpkSolver instance was not freed. So,
 * this class is the copy of the GLPKSolver class from the SCPSolver. The only
 * change here is that the `solver.deleteProb();` is called in the solve()
 * method after its use is done which deletes the object, i.e., frees the
 * memory.
 *
 * This class will be removed from SBSCL as this issue gets resolved in the
 * SCPSolver.
 *
 */
public class NewGLPKSolver implements LinearProgramSolver {

  GlpkSolver solver;
  int rowcount;
  int timeconstraint = -1;

  public NewGLPKSolver() {
  }

  /**
   * @return the timeconstraint
   */
  public int getTimeconstraint() {
    return timeconstraint;
  }

  /**
   * @param timeconstraint the timeconstraint to set
   */
  public void setTimeconstraint(int timeconstraint) {
    this.timeconstraint = timeconstraint;
  }


  public double[] solve(LinearProgram lp) {

    /* add variables */

    try {
      solver = new GlpkSolver();
    } catch (Error e) {
      System.err.println("Can't instantiate solver:");
      System.err.println(" ** " + e.getClass().getName() + ": " + e.getMessage());
      System.err.println(
          " ** java.library.path: " + System.getProperty("java.library.path"));
      System.err.println("Probably you don't have GLPK JNI properly installed.");
      return null;
    }

    //  solver.setRealParm(GlpkSolver.LPX_K_TMLIM, 100);
    // solver.setIntParm(GlpkSolver.LPX_K_TMLIM, 100);
    solver.enablePrints(false);  // turn this to "false" to prevent printouts
    if (lp.isMIP()) {
      solver.setClss(GlpkSolver.LPX_MIP);
    } else {
      solver.setClss(GlpkSolver.LPX_LP);
    }

    /* we usually excpect a max problem, but I have to think about that..*/

    solver.setObjDir((lp.isMinProblem())? GlpkSolver.LPX_MIN: GlpkSolver.LPX_MAX);

    /* set columns */


    double[] c = lp.getC();
    solver.addCols(c.length);

    for (int i = 0; i < c.length; i++) {
      solver.setColName(i+1,"x"+i);
      solver.setObjCoef(i+1, c[i]);
    }

    if (!lp.hasBounds()) {
      for (int i = 0; i < c.length; i++) {
        solver.setColBnds(i+1, GlpkSolver.LPX_FR, 0, 0);
      }
    } else {

      for (int i = 0; i < c.length; i++) {
        solver.setColBnds(i+1, GlpkSolver.LPX_DB, lp.getLowerbound()[i],lp.getUpperbound()[i]); //TODO
      }
    }

    /* add variable types */

    boolean[] integers = lp.getIsinteger();

    for (int i = 0; i < integers.length; i++) {
      solver.setColKind(i+1, (integers[i])?GlpkSolver.LPX_IV:GlpkSolver.LPX_CV);
    }

    boolean[] booleans = lp.getIsboolean();

    for (int i = 0; i < booleans.length; i++) {
      if (booleans[i]) {
        solver.setColKind(i+1, GlpkSolver.LPX_IV);
        solver.setColBnds(i+1, GlpkSolver.LPX_DB, 0,1); //TODO
      }
    }

    /* add constraints */

    transferConstraints(lp);

    if (timeconstraint > 0) {
      System.out.println("Setting time constraint to:" + timeconstraint + " seconds");
      solver.setRealParm(GlpkSolver.LPX_K_TMLIM, (double) timeconstraint );
    }

    double[] result = null;

    int res= solver.simplex();

    if (!lp.isMIP()) {

      //System.out.println("Maximum: " + solver.getObjVal());


      if (res != GlpkSolver.LPX_E_OK ||
          (solver.getStatus() != GlpkSolver.LPX_OPT &&
              solver.getStatus() != GlpkSolver.LPX_FEAS)) {
        System.err.println("simplex() failed");
      } else {
        result = new double[c.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = solver.getColPrim(i+1);
        }
      }
    } else {
      res = solver.integer();
      //  System.out.println("SOLVER STATUS: " + solver.getPrimStat());
      if (res != GlpkSolver.LPX_E_OK ||
          (solver.mipStatus() != GlpkSolver.LPX_I_OPT &&
              solver.mipStatus() != GlpkSolver.LPX_I_FEAS)) {

        System.err.println("integer() failed");
      } else {
        //		System.out.println("Maximum: " + solver.mipObjVal());
        //		System.out.println("MIP STATUS: " + solver.mipStatus());
        result = new double[c.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = solver.mipColVal(i+1);
          //	System.out.println("x" +(i+1) +": " + result[i]);
        }
      }
    }
    solver.deleteProb();
    return result;
  }

  public int[] getIntegerSolution() {
    int length = solver.getNumCols();
    int[] result = new int[length];
    for (int i = 1; i <= length; i++) {
      result[i-1] = (int) solver.mipColVal(i);
    }
    return result;
  }

  private void transferConstraints(LinearProgram lp) {
    ArrayList<Constraint> constraints = lp.getConstraints();
    rowcount = 0;

    /* add rows */
    solver.addRows(constraints.size());

    /* add row/names borders */
    for (Constraint constraint : constraints) {
      ((LinearConstraint) constraint).addToLinearProgramSolver(this);
    }

    int nonzeroa = 0;
    for (Constraint constraint : constraints) {

      double[] c =((LinearConstraint) constraint).getC();
      for (int i = 0; i < c.length; i++) {
        if (c[i] != 0.0) nonzeroa++;
      }
    }

    int[] ia = new int[nonzeroa+1];
    int[] ja = new int[nonzeroa+1];
    double[]  ar = new double[nonzeroa+1];

    rowcount = 0;
    nonzeroa = 0;

    for (Constraint constraint : constraints) {

      rowcount++;
      double[] c =((LinearConstraint) constraint).getC();

      //System.out.print(constraint.getName() + " ");
      for (int i = 0; i < c.length; i++) {
        if (c[i] != 0.0)  {
          nonzeroa++;
          ia[nonzeroa] = rowcount;
          ja[nonzeroa] = i+1;
          ar[nonzeroa] = c[i];
          //	System.out.print((i+1) + "(" + c[i] + ") ");
        }
      }
      //	System.out.println();

    }
    solver.loadMatrix(nonzeroa, ia, ja, ar);


  }


  public void addLinearBiggerThanEqualsConstraint(LinearBiggerThanEqualsConstraint c) {
    rowcount++;
    solver.setRowName(rowcount, c.getName());
    solver.setRowBnds(rowcount, GlpkSolver.LPX_LO, c.getT() , 0.0);
  }

  public void addLinearSmallerThanEqualsConstraint(LinearSmallerThanEqualsConstraint c) {
    rowcount++;
    solver.setRowName(rowcount, c.getName());
    solver.setRowBnds(rowcount, GlpkSolver.LPX_UP, 0.0, c.getT());
  }

  public void addEqualsConstraint(LinearEqualsConstraint c) {
    rowcount++;
    solver.setRowName(rowcount, c.getName());
    solver.setRowBnds(rowcount, GlpkSolver.LPX_FX, c.getT(), c.getT());

  }

  public String getName() {
    return "GLPK";
  }

  public String[] getLibraryNames() {
    return new String[]{"glpkjni"};
  }

  public static void main(String[] args) {
    LinearProgram lp = new LinearProgram(new double[]{10.0, 6.0, 4.0});
    lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{1.0,1.0,1.0}, 320,"p"));
    lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{10.0,4.0,5.0}, 650,"q"));
    lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{2.0,2.0,6.0}, 100,"r1"));

    lp.setLowerbound(new double[]{30.0,0.0,0.0});

    //lp.addConstraint(new LinearEqualsConstraint(new double[]{1.0,1.0,1.0}, 100,"t"));

    lp.setInteger(0);
    lp.setInteger(1);
    lp.setInteger(2);

    LinearProgramSolver solver = new GLPKSolver();

    System.out.println(solver.solve(lp)[0]);
    double[] sol = solver.solve(lp);
    ArrayList<Constraint> constraints = lp.getConstraints();
    for (Iterator<Constraint> iterator = constraints.iterator(); iterator.hasNext();) {
      Constraint constraint = (Constraint) iterator.next();
      if (constraint.isSatisfiedBy(sol)) System.out.println(constraint.getName() + " satisfied");
    }

  }


}
