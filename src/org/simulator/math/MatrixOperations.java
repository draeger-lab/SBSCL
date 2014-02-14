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
package org.simulator.math;

/**
 * <p>
 * Class used to perform matrix operations, focusing on finding vector solutions
 * to the vector equation F(x) = 0.
 * <p>
 * TODO: Add calculation of eigenvectors. This isn't hard to implement as we've
 * already calculated the eigenvalues and this means all we need to do is solve
 * for x in the matrix equation (A-lambda*I)*x = 0. However, if we have complex
 * eigenvalues then we have a problem. Java doesn't have complex number support
 * natively.
 * <h1>
 * Class method summary:
 * </h1>
 * <b>
 * Numerical Methods. Only call these directly if you know what you're doing.
 * </b>
 * <ul>
 * <li>mnewt - Uses a straightforward Newton-Rhapson method to compute the
 * equilibrium solution iteratively starting at a reasonably close guess.
 * </li>
 * <li>ludcmp - Performs LU decomposition of a matrix (A = L*U) to prepare it for
 * solving the linear equation A*x=B for x.
 * </li>
 * <li>lubksb - Performs back substitution on an LU decomposed matrix to solve the
 * linear equation A*x=B for x.
 * </li>
 * <li>fdjac - Numerically approximates the jacobian for a system of equations using
 * the method of forward differences.
 * </li>
 * <li>fmin - Not currently used. Computes 1/2 the dot product of a vector function
 * with itself at a specified x vector.
 * </li>
 * <li>elmhes - Reduces the given matrix to upper Hessenberg form.
 * </ul>
 * 
 * <h1>
 * Utility Functions. Call these methods!
 * </h1>
 * <p>
 * Notes: The majority of the code in this class comes from Numerical Recipes in
 * C, 2nd Edition. The code was translated from C to Java by Eric Harley. Mostly
 * this amounted to figuring out how to deal with function pointers to user
 * defined functions and re-indexing things starting at 0 instead of 1.
 * <p>
 * References: _Numerical Recipies in C_ (2nd Edition) by Press, Teutolsky,
 * Vetterling, and Flannery Cambridge University Press, 1992
 * 
 * @author Chris Moore
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class MatrixOperations {

  /*
   * Numerical Recipes Constants Some of these have been tweaked because we're
   * running on the JVM using 32 bit doubles instead of 16 bit floats.
   */

  /** Ensures sufficient decrease in function value */
  public static final double ALF = 1.0e-4d;

  /** Approximate square root of the JVM precision */
  public static final double EPS = 1.0e-8d;

  /** Scaled maximum step length allowed in line searches */
  public static final double STPMX = 100.0d;

  /** Extremely small value. Nigh zero. */
  public static final double TINY = 1.0e-20d;

  /** Convergence criterion on function values */
  public static final double TOLF = 1.0e-4d;

  /** Criterion deciding whether spurious convergence to a minimum of min */
  public static final double TOLMIN = 1.0e-6d;

  /** Convergence criterion on delta X */
  public static final double TOLX = 1.0e-7d;

  private static double[] vv;

  /**
   * Given a matrix a[1..n][1..n], this routine replaces it by the LU
   * decomposition of a rowwise permutation of itself. a and n are input. a is
   * output,l arrand as in equation (2.3.14) above; indx[1..n] is an output
   * vector that records the row permutation effected by the partial pivoting;
   * d (return value) is output +/- 1 depending on whether the number of row
   * interchanges was even or odd respectively. This routine is used in
   * combination with lubksb to solve linear equations or invert a matrix.
   * 
   * @param a
   *            the matrix to be decomposed
   * @param indx
   *            the array to put the return index into
   * @return +1 or -1 signifying whether the number of row interchanges is odd
   *         or even
   */
  public static double ludcmp(double a[][], int indx[])
      throws MatrixException {

    int n = a.length;

    int i = 0, imax = 0, j = 0, k = 0;

    double big, dum, sum, temp;
    double d = 1;

    if ((vv==null)||vv.length!=n) {
      vv = new double[n];
    }
    for (i = 0; i < n; i++) {
      big = 0.0;
      for (j = 0; j < n; j++) {
        temp = a[i][j];
        if (temp < 0) {
          temp *= -1;
        }
        if (temp > big) {
          big = temp;
        }
      }
      if (big == 0.0) {
        // no non-zero largest element

        throw new MatrixException(
            "Error: Singular linearized system. Computation cannot proceed.");

      }
      vv[i] = 1.0 / big;
    }

    for (j = 0; j < n; j++) {
      for (i = 0; i < j; i++) {
        sum = a[i][j];
        for (k = 0; k < i; k++) {
          sum -= a[i][k] * a[k][j];
        }
        a[i][j] = sum;
      }
      big = 0.0;
      for (i = j; i < n; i++) {
        sum = a[i][j];
        for (k = 0; k < j; k++) {
          sum -= a[i][k] * a[k][j];
        }
        a[i][j] = sum;
        dum = vv[i] * sum;
        if (sum < 0) {
          dum *= -1;
        }
        if (dum >= big) {
          big = dum;
          imax = i;
        }
      }
      if (j != imax) {
        for (k = 0; k < n; k++) {
          dum = a[imax][k];
          a[imax][k] = a[j][k];
          a[j][k] = dum;
        }
        d = -d;
        vv[imax] = vv[j];
      }
      indx[j] = imax;
      if (a[j][j] == 0.0) {
        // replace zero values with a nigh zero value so that
        // we don't get any divisions by zero.
        a[j][j] = TINY;
      }

      if (j != n) {
        dum = 1.0 / (a[j][j]);
        for (i = j + 1; i < n; i++) {
          a[i][j] *= dum;
        }
      }
    }
    return d;
  }

  /**
   * Solves the set of n linear equations AX = B. Here a[1..n][1..n] is input,
   * not as the matrix A but rather as its LU decomposition, determined by the
   * routine ludcmp. indx[1..n] is input as the permutation vector returned by
   * ludcmp. b[1..n] is input as the right hand side vector B, and returns
   * with the solution vector X. a, and indx are not modified by this routine
   * and can be left in place for successive calls with different right-hand
   * sides b. This routine takes into account the possibility that b will
   * begin with many zero elements, so it is efficient for use in matrix
   * inversion.
   * 
   * @param a
   *            the matrix to be solved as described
   * @param indx
   *            the array returned by ludcmp
   * @param b
   *            the vector to be solbed as described
   */

  public static void lubksb(double a[][], int indx[], double[] b) {
    int ii = 0, ip = 0;

    double sum = 0.0;

    int n = a.length;

    for (int i = 1; i <= n; i++) {

      ip = indx[i - 1] + 1;
      sum = b[ip - 1];
      b[ip - 1] = b[i - 1];

      if (ii != 0) {

        for (int j = ii; j <= i - 1; j++) {
          sum -= a[i - 1][j - 1] * b[j - 1];
        }

      } else if (sum != 0.0) {
        ii = i;
      }

      b[i - 1] = sum;
    }

    for (int i = n; i >= 1; i--) {

      sum = b[i - 1];

      for (int j = i + 1; j <= n; j++) {
        sum -= a[i - 1][j - 1] * b[j - 1];
      }

      b[i - 1] = sum / a[i - 1][i - 1];
    }

  }

  /**
   * Given a matrix a[1..n][1..n], this routine replaces it by a balanced
   * matrix with identical eigenvalues. A symmetric matrix is already balanced
   * and is unaffected by this procedure. The parameter RADIX should be the
   * machine's floating-point radix.
   * 
   * @param a
   *            the matrix to be balanced
   */
  public static void balance(double a[][]) {
    // The JVM is a binary machine, ie radix base 2
    final int RADIX = 2;

    int last = 0;

    int n = a.length;

    double s = 0.0, r = 0.0, g = 0.0, f = 0.0, c = 0.0;

    double sqrdx = RADIX * RADIX;

    while (last == 0) {
      last = 1;
      for (int i = 1; i <= n; i++) {

        r = 0.0;
        c = 0.0;

        for (int j = 1; j <= n; j++) {
          if (j != i) {
            c += Math.abs(a[j - 1][i - 1]);
            r += Math.abs(a[i - 1][j - 1]);
          }
        }

        if ((c != 0.0) && (r != 0.0)) {

          g = r / RADIX;
          f = 1.0;
          s = c + r;

          while (c < g) {
            f *= RADIX;
            c *= sqrdx;
          }

          g = r * RADIX;

          while (c > g) {
            f /= RADIX;
            c /= sqrdx;
          }

          if ((c + r) / f < 0.95 * s) {
            last = 0;
            g = 1.0 / f;
            for (int j = 1; j <= n; j++) {
              a[i - 1][j - 1] *= g;
            }
            for (int j = 1; j <= n; j++) {
              a[j - 1][i - 1] *= f;
            }
          }

        }

      }
    }
  }

  /**
   * Reduces a[1..n][1..n] to upper Hessenberg form
   * 
   * @param a
   *            the matrix to be reduced
   */
  public static void elmhes(double a[][]) {
    int m, j, i;

    int n = a.length;

    double y, x;

    for (m = 2; m < n; m++) {

      x = 0.0;
      i = m;

      for (j = m; j <= n; j++) {
        if (Math.abs(a[j - 1][m - 2]) > Math.abs(x)) {

          x = a[j - 1][m - 2];
          i = j;

        }
      }

      if (i != m) {
        double temp;
        for (j = m - 1; j <= n; j++) {
          temp = a[i - 1][j - 1];
          a[i - 1][j - 1] = a[m - 1][j - 1];
          a[m - 1][j - 1] = temp;

        }

        for (j = 1; j <= n; j++) {
          temp = a[j - 1][i - 1];
          a[j - 1][i - 1] = a[j - 1][m - 1];
          a[j - 1][m - 1] = temp;

        }
      }

      if (x != 0.0) {
        for (i = m + 1; i <= n; i++) {
          if ((y = a[i - 1][m - 2]) != 0.0) {

            y /= x;
            a[i - 1][m - 2] = y;

            for (j = m; j <= n; j++) {
              a[i - 1][j - 1] -= y * a[m - 1][j - 1];
            }

            for (j = 1; j <= n; j++) {
              a[j - 1][m - 1] += y * a[j - 1][i - 1];
            }

          }
        }
      }
    }
  }

  /**
   * Returns the value of a or |a| with the same sign as b
   * 
   * @param a
   *            the input as specified above
   * @param b
   *            the input as specified above
   * @return the value of a or |a| with the same sign as b
   */
  public static double sign(double a, double b) {
    if (b > 0.0) {
      return Math.abs(a);
    } else {
      return -1 * Math.abs(a);
    }
  }

  /**
   * Finds all eigenvalues of an upper Hessenberg matrix a[1..n][1..n]. On
   * input a can be exactly as output from elmhes (� 11.5); on output it is
   * destroyed. The real and imaginary parts of the eigenvalues are returned
   * in wr[1..n] and wi[1..n], respectively.
   * 
   * @param a
   *            the input matrix
   * @param wr
   *            the array specified in the function description
   * @param wi
   *            the array specified in the function description
   * @return 0 iff success
   */
  public static int hqr(double a[][], double wr[], double wi[])
      throws MatrixException {

    // Initialize variables

    int nn = 0, m = 0, l = 0, k = 0, j = 0, its = 0, i = 0, mmin = 0;

    int n = a.length;

    double z = 0.0, y = 0.0, x = 0.0, w = 0.0, v = 0.0, u = 0.0, t = 0.0, s = 0.0, r = 0.0, q = 0.0, p = 0.0, anorm = 0.0;

    anorm = Math.abs(a[0][0]);
    for (i = 2; i <= n; i++) {
      for (j = i - 1; j <= n; j++) {
        anorm += Math.abs(a[i - 1][j - 1]);
      }
    }

    nn = n;
    t = 0.0;

    while (nn >= 1) {

      its = 0;

      do {
        for (l = nn; l >= 2; l--) {
          s = Math.abs(a[l - 2][l - 2]) + Math.abs(a[l - 1][l - 1]);
          if (s == 0.0) {
            s = anorm;
          }
          if ((Math.abs(a[l - 1][l - 2]) + s) == s) {
            a[l - 1][l - 2] = 0.0;
            break;
          }
        }
        x = a[nn - 1][nn - 1];
        if (l == nn) {
          wr[nn - 1] = x + t;
          wi[nn - 1] = 0.0;
          nn--;
        } else {
          y = a[nn - 2][nn - 2];
          w = a[nn - 1][nn - 2] * a[nn - 2][nn - 1];
          if (l == (nn - 1)) {
            p = 0.5 * (y - x);
            q = p * p + w;
            z = Math.sqrt(Math.abs(q));
            x += t;
            if (q >= 0.0) {
              z = p + sign(z, p);
              wr[nn - 2] = wr[nn - 1] = x + z;
              if (z != 0.0) {
                wr[nn - 1] = x - w / z;
              }
              wi[nn - 2] = wi[nn - 1] = 0.0;
            } else {
              wr[nn - 2] = wr[nn - 1] = x + p;
              wi[nn - 2] = -(wi[nn - 1] = z);
            }
            nn -= 2;
          } else {
            if (its == 30) {
              // Too many iterations in hqr
              throw new MatrixException(
                "Error: Could not find acceptable equilibrium point in "
                    + its
                    + " iterations. Please try another initial guess.");
            }

            if (its == 10 || its == 20) {
              t += x;
              for (i = 1; i <= nn; i++) {
                a[i - 1][i - 1] -= x;
              }
              s = Math.abs(a[nn - 1][nn - 2])
                  + Math.abs(a[nn - 2][nn - 3]);
              y = x = 0.75 * s;
              w = -0.4375 * s * s;
            }
            ++its;
            for (m = (nn - 2); m >= l; m--) {
              z = a[m - 1][m - 1];
              r = x - z;
              s = y - z;
              p = (r * s - w) / a[m][m - 1] + a[m - 1][m];
              q = a[m][m] - z - r - s;
              r = a[m + 1][m];
              s = Math.abs(p) + Math.abs(q) + Math.abs(r);
              p /= s;
              q /= s;
              r /= s;
              if (m == l) {
                break;
              }
              u = Math.abs(a[m - 1][m - 2])
                  * (Math.abs(q) + Math.abs(r));
              v = Math.abs(p)
                  * (Math.abs(a[m - 2][m - 2]) + Math.abs(z) + Math
                      .abs(a[m][m]));
              if ((u + v) == v) {
                break;
              }
            }
            for (i = m + 2; i <= nn; i++) {
              a[i - 1][i - 3] = 0.0;
              if (i != (m + 2)) {
                a[i - 1][i - 4] = 0.0;
              }
            }
            for (k = m; k <= nn - 1; k++) {
              if (k != m) {
                p = a[k - 1][k - 2];
                q = a[k][k - 2];
                r = 0.0;
                if (k != (nn - 1)) {
                  r = a[k + 1][k - 2];
                }
                if ((x = Math.abs(p) + Math.abs(q)
                    + Math.abs(r)) != 0.0) {
                  p /= x;
                  q /= x;
                  r /= x;
                }
              }

              if ((s = sign(Math.sqrt(p * p + q * q + r * r), p)) != 0.0) {
                if (k == m) {
                  if (l != m) {
                    a[k - 1][k - 2] = -a[k - 1][k - 2];
                  }
                } else {
                  a[k - 1][k - 2] = -s * x;
                }
                p += s;
                x = p / s;
                y = q / s;
                z = r / s;
                q /= p;
                r /= p;
                for (j = k; j <= nn; j++) {
                  p = a[k - 1][j - 1] + q * a[k][j - 1];
                  if (k != (nn - 1)) {
                    p += r * a[k + 1][j - 1];
                    a[k + 1][j - 1] -= p * z;
                  }
                  a[k][j - 1] -= p * y;
                  a[k - 1][j - 1] -= p * x;
                }

                if (nn < k + 3) {
                  mmin = nn;
                } else {
                  mmin = k + 3;
                }

                for (i = l; i <= mmin; i++) {
                  p = x * a[i - 1][k - 1] + y * a[i - 1][k];
                  if (k != (nn - 1)) {
                    p += z * a[i - 1][k + 1];
                    a[i - 1][k + 1] -= p * r;
                  }
                  a[i - 1][k] -= p * q;
                  a[i - 1][k - 1] -= p;
                }
              }
            }
          }
        }
      } while (l < nn - 1);
    }
    return 0;
  }

  /**
   * This exception is thrown when errors in the computation of matrix-related
   * solutions, their eigenvalues or eigenvectors. The message stored in the
   * exception indicates where the computation went wrong and should be
   * instructive to the user hoping to re-complete the calculation
   */
  @SuppressWarnings("serial")
  public static class MatrixException extends Exception {
    /**
     * Constructor for MatrixException
     */
    public MatrixException() {
      super();
    }

    /**
     * Constructor for MatrixException with message
     */
    public MatrixException(String message) {
      super(message);
    }

  }

}
