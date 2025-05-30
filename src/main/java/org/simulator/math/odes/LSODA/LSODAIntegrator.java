package org.simulator.math.odes.LSODA;
import java.util.Arrays;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DESystem;

public class LSODAIntegrator extends AdaptiveStepsizeIntegrator {
    
    public static LSODAContext ctx;
    private int neq;
    private double[] yh;
    private double[] tn;
    private int state;
    
    public LSODAIntegrator(LSODAContext ctx) {
        super();
        this.ctx = ctx;
    }

    public LSODAIntegrator(LSODAIntegrator integrator) {
        super(integrator);
    }

    @Override
    public AbstractDESSolver clone() {
        return new LSODAIntegrator(this);
    }

    @Override
    public int getKiSAOterm() {
        return 0;
    }

    @Override
    public String getName() {
        return "LSODA Integrator";
    }

    @Override
    protected boolean hasSolverEventProcessing() {
        return false;
    }

    @Override
    public double[] computeChange(DESystem DES, double[] y, double t, double stepSize, double[] change, boolean steadyState) throws DerivativeException {
        return null;
    }


    private int hardFailure(LSODAContext ctx, String format, Object... args) {
        System.err.printf(format + "%n", args);
        ctx.setState(-3);
        return ctx.getState();
    }

    private int softFailure(LSODAContext ctx, int code, String format, Object... args) {
        System.err.printf(format + "%n", args);

        LSODACommon common = ctx.getCommon();
        double[] y = new double[common.getYh().length];

        if (common != null) {
            for (int i = 1; i <= ctx.getNeq(); i++) {
                y[i] = common.getYh()[1][i];
            }
            double t = common.getTn();
        }
        ctx.setState(code);
        return ctx.getState();
    }

    private int successReturn(LSODAContext ctx, double[] y, double[] t, int itask, boolean ihit) {
        int neq = ctx.getNeq(); //redundant for the moment, will become relevant when variable definitions in line 12-15 are fixed!
        double tcrit = ctx.getOpt().getTcrit();
        for (int i = 1; i <= neq; i++) {
            y[i] = yh[i];
        }
        t[0] = tn[0];
        if((itask == 4 || itask == 5) && ihit) {
            t[0] = tcrit;
        }
        ctx.setState(2);
        return ctx.getState();
    }

    private static void logError(String message, Object... args) {
        System.err.printf(message, args);
    }
    
    /**
     * Computes the k-th derivative of the solution at a given time t ({@code t}).
     * <p>
     * This method evaluates the interpolating polznomial (or its derivatives) for the dependent variables at the given time {@code t},
     * using the historz data stored in the {@code LSODACommon} object.
     * The result is returned in the arrray {@code dky}, which represents the k-th derivative of y(t).
     * The function performs bounds checking on both {@code k} and {@ code t} to ensure interpolation is valid.
     * 
     * The interpolation is based on Newton-form divided differences and applies a nested multiplication scheme to evaluate the polynomial.
     * The result is scaled appropriately if {@code k} > 0.
     * </p>
     * 
     * @param ctx   the {@code LSODAContext} object containing the integration state, history arrays and control parameters
     * @param t     the time at which interpolation is to be performed (must lie within the last time interval)
     * @param k     the order of the derivative to be computed (must be in range 0 ≤ k ≤ nq)
     * @param dky   the output array into which the k-th derivative of y(t) will be written (must have at least length neq + 1)
     * @return      0 if successful
     *              <li>-1 if {@code k} is out of range</li>
     *              <li>-2 if {@code t} is outside the valid interpolation interval</li>
     */
    public static int intdy(LSODAContext ctx, double t, int k, double[] dky) {
        int i, ic, j, jj, jp1;
        double c, r, s, tp;
        
        LSODACommon common = ctx.getCommon();
        int neq = ctx.getNeq();
        
        if (k < 0 || k > common.getNq()) {
            logError(String.format("[intdy] k = %d illegal", k), ctx.getData());
            return -1;
        }
        
        tp = common.getTn() - common.getHu() - 100d * common.ETA * (common.getTn() + common.getHu());

        if ((t - tp) * (t - common.getTn()) > 0d) {
            logError(String.format("intdy -- t = %f illegal. t not in interval (tcur - _C(hu)) to tcur", t, t, ctx.getData()));
            return -2;
        }

        s = (t - common.getTn()) / common.getH();
        ic = 1;

        for (jj = (common.getNq() + 1) - k; jj <= common.getNq(); jj++) {
            ic *= jj;
        }

        c = (double) ic;

        for (i = 1; i <= neq; i++) {
            dky[i] = c * common.getYh()[common.getNq() + 1][i];
        }

        for (j = common.getNq() - 1; j >= k; j--) {
            jp1 = j + 1;
            ic = 1;
            for (jj = jp1 - k; jj <= j; jj++) {
                ic *= jj;
            }
            c = (double) ic;
            for (i = 1; i <= neq; i++) {
                dky[i] = c * common.getYh()[jp1][i] + s * dky[i];
            }
        }

        if (k == 0) {
            return 0;
        }

        r = Math.pow(common.getH(), (double) (-k));

        for (i = 1; i <= neq; i++) {
            dky[i] *= r;
        }
        return 0;
    }

    public static int intdyReturn(LSODAContext ctx, double[] y, double[] t, double tout, int itask) {
        System.out.println("[intdyReturn] Starting execution");
        System.out.println("[intdyReturn] Inputs: y = " + Arrays.toString(y) + ", t = " + Arrays.toString(t) +
                           ", tout = " + tout + ", itask = " + itask);

        LSODACommon common = ctx.getCommon();

        System.out.println("[intdyReturn] LSODACommon obtained, yh array: " + Arrays.deepToString(common.getYh()));

        System.out.println("[intdyReturn] Calling intdy function...");
        System.out.println("Y before intdy: " + Arrays.toString(y));
        int iflag = intdy(ctx, tout, 0, y);
        System.out.println("y after intdy: " + Arrays.toString(y));
        System.out.println("[intdyReturn] intdy returned iflag: " + iflag);


        if (iflag != 0) {
            System.out.println("[intdyReturn] Error detected (iflag != 0), logging error...");
            logError("[lsoda] trouble from indty, itas = %d, tout = %g\n", itask, tout);
            System.out.println("[intdyReturn] Copying backup values from common.getYh()[1] to y:");
            for (int i = 1; i <= ctx.getNeq(); i++) {
                y[i] = common.getYh()[1][i];
                System.out.println("  [intdyReturn] y[" + i + "] updated to backup value: " + y[i]);
            }
        } else {
            System.out.println("[intdyReturn] No error detected; skipping backup value copying.");
        }
        t[0] = tout;
        System.out.println("[intdyReturn] t[0] set to tout: " + t[0]);

        ctx.setState(2);
        System.out.println("[intdyReturn] Context state set to: " + ctx.getState());

        System.out.println("[intdyReturn] Execution complete, returning state: " + ctx.getState());
        return ctx.getState();

    }

    public static boolean checkOpt(LSODAContext ctx, LSODAOptions opt) {
        final int mxstp0 = 500;
        final int[] mord = {0, 12, 5};

        if (ctx.getState() == 0) ctx.setState(1);
        if (ctx.getState() == 1) {
            opt.setH0(0d);
            opt.setMxordn(mord[1]);
            opt.setMxords(mord[2]);
        }

        if (ctx.getNeq() <= 0) {
            logError("[lsoda] neq = %d is less than 1\n", ctx.getNeq());
            return false;
        }

        double[] rtol = opt.getRtol();
        double[] atol = opt.getAtol();

        if (ctx.getState() == 1 || ctx.getState() == 3) {
            for (int i = 1; i <= ctx.getNeq(); i++) {
                double rtoli = rtol[i - 1];
                // System.out.println("Rtoli: " + rtoli);
                double atoli = atol[i - 1];
                if (rtoli < 0d) {
                    logError("[lsoda] rtol = %g is less than 0.\n", rtoli);
                    continue;
                }
                if (atoli < 0d) {
                    logError("[lsoda] atol = %g is less than 0.\n", atoli);
                    return false;
                }
            }
        }

        if (opt.getItask() == 0) opt.setItask(1);
        if (opt.getItask() < 1 || opt.getItask() > 5) {
            logError("[lsoda] illegal itask = %d\n", opt.getItask());
            return false;
        }

        if (opt.getIxpr() < 0 || opt.getIxpr() > 1) {
            logError("[lsoda] ixpr = %d is illegal\n", opt.getIxpr());
            return false;
        }

        if (opt.getMxstep() < 0) {
            logError("[lsoda] mxstep < 0\n");
            return false;
        }

        if (opt.getMxstep() == 0) opt.setMxstep(mxstp0);

        if (opt.getMxhnil() < 0) {
            logError("[lsoda] mxhnil < 0\n");
            return false;
        }

        if (ctx.getState() == 1) {
            if (opt.getMxordn() < 0) {
                logError("[lsoda] mxordn = %d is less than 0\n", opt.getMxordn());
                return false;
            }
            if (opt.getMxordn() == 0) opt.setMxordn(100);
            opt.setMxordn(Math.min(opt.getMxordn(), mord[1]));

            if (opt.getMxords() < 0) {
                logError("[lsoda] mxords = %d is less than 0\n", opt.getMxords());
                return false;
            }
            if (opt.getMxords() == 0) opt.setMxords(100);
            opt.setMxords(Math.min(opt.getMxords(), mord[2]));
        }

        if (opt.getHmax() < 0d) {
            logError("[lsoda] hmax < 0.\n");
            return false;
        }

        opt.setHmxi((opt.getHmax() > 0) ? 1d / opt.getHmax() : 0d);

        if (opt.getHmin() < 0d) {
            logError("[lsoda] hmin < 0.\n");
            return false;
        }

        return true;
    }

    public static boolean lsoda_prepare(LSODAContext ctx, LSODAOptions opt) {
        if(!checkOpt(ctx, opt)) {
            return false;
        }
        ctx.setOpt(opt);
        return true;
    }

    public void lsodaReset(LSODAContext ctx) {
        ctx.setState(1);
        ctx.setError(null);
    }

    public void lsodaFree(LSODAContext ctx) {
        ctx.setCommon(null);
        ctx.setOpt(null);
    }

    /**
     * Computes and sets the error weight vector used for controlling local error.
     * <p>
     * This method computes a vector ({@code ewt}) based on the current estimate {@code ycur}, relative tolerance ({@code rtol})
     * and absolute tolerance ({@code atol}) with the formula:
     * <pre>
     *      ewt[i] = 1 / (rtol[i] * |ycur[i]| + atol[i]) for i = 1, ..., neq
     * </pre>
     * The computed vector is stored in the {@code LSODACommon} object.
     * </p>
     * 
     * @param ycur      the current solution vector
     * @param rtol      the relative tolerance vector
     * @param atol      the absolute tolerance vector
     * @param neq       the number of equations in the system
     * @param common    the {@code LSODACommon} object holding the error weight vector and other shared data
     */
    public static void ewset(double[] ycur, double[] rtol, double[] atol, int neq, LSODACommon common) {
        double[] ewt = new double[neq + 1];
        
        System.out.println("Neq" + neq);
        System.out.println("Ewt" + common.getEwt().length);
        
        for (int i = 1; i <= neq; i++) {
            ewt[i] = rtol[i] * Math.abs(ycur[i]) + atol[i];
        }

        for (int i = 1; i <= neq; i++) {
            ewt[i] = 1d / ewt[i];
        }

        common.setEwt(ewt);
    }

    /**
     * Computes the weighted maximum norm of a vector v of length n.
     * The norm is computed using the formula:
     * <p>
     *      vmnorm = max(i=0,...,n) (|v[i]| * w[i])
     * </p>
     * where 'w' is a weight vector of the same length as 'v'.
     * 
     * @param n the length of the vector v (index range: 1 to n) 
     * @param v the vector whose weighted max-norm is to be computed
     * @param w the weight vector of the same length as v
     * @return the computed weightes max-norm of the vector
     */
    public static double vmnorm(int n, double[] v, double[] w) {
        double vm = 0d;

        if(v.length == 0 || w.length == 0) {
            return vm;
        }

        for (int i = 1; i <= n; i++) {
            vm = Math.max(vm, Math.abs(v[i]) * w[i]);
        }
        return vm;
    }

    /**
     * Initializes method-specific coefficients used in the LSODA algorithm.
     * <p>
     * This method pre-computes and fills the coefficient tables ({@code elco} and {@code tesco}) based on
     * the chosen integration method. These coefficient tables are required for evaluating predictor and
     * corrector formulas in the Adams or BDF methods.
     * 
     * For Adams method ({@code meth} == 1), the fucntion calculates:
     * <ul>
     *      <li>{@code elco[nq][*]}: Coefficients for the polynomial history in the predictor step</li>
     *      <li>{@code tesco[nq][*]}: Time-step and error estimation related constants</li>
     * </ul>
     * 
     * For BDF method ({@code meth} != 1), it computes a simpler version of the same tables for use in the BDF formula.
     * </p>
     * 
     * @param ctx   the {@code LSODAContext} object containing integration parameters, method state and history arrays
     * @param meth  the integration method identifiert (1 for Adams, 2 for BDF)
     */
    public static void cfode(LSODAContext ctx, int meth) {
        LSODACommon common = ctx.getCommon();
        int i, nq, nqm1, nqp1;
        double agamq, fnq, fnqm1, pint, ragq, rqfac, rq1fac, tsign, xpin;
        double[] pc = new double[13];
        
        
        if (meth == 1) {
            double[][] newElco = common.getElco();
            newElco[1][1] = 1d;
            newElco[1][2] = 1d;

            double[][] newTesco = common.getTesco();
            newTesco[1][1] = 0d;
            newTesco[1][2] = 2d;
            newTesco[2][1] = 1d;
            newTesco[12][3] = 0d;
            
            pc[1] = 1d;
            rqfac = 1d;
            for (nq = 2; nq <= 12; nq++) {
                rq1fac = rqfac;
                rqfac = rqfac / (double) nq;
                nqm1 = nq - 1;
                fnqm1 = (double) nqm1;
                nqp1 = nq + 1;

                pc[nq] = 0d;
                for (i = nq; i >= 2; i--) {
                    pc[i] = pc[i - 1] + fnqm1 * pc[i];
                }
                pc[1] = fnqm1 * pc[1];
                pint = pc[i];
                xpin = pc[1] / 2d;
                tsign = 1d;
                for (i = 2; i <= nq; i++) {
                    tsign = -tsign;
                    pint += tsign * pc[i] / (double) i;
                    xpin += tsign * pc[i] / (double) (i + 1);
                }
    
                newElco[nq][1] = pint * rq1fac;
                newElco[nq][2] = 1d;

                for (i = 2; i <= nq; i++) {
                    newElco[nq][i+1] = rq1fac * pc[i] / (double) i;
                }
                common.setElco(newElco);

                agamq = rqfac * xpin;
                ragq = 1d / agamq;

                newTesco[nq][2] = agamq;
                
                if (nq < 12) {
                    newTesco[nqp1][1] = ragq * rqfac / (double) nqp1;
                }
                newTesco[nqm1][3] = ragq;
                common.setTesco(newTesco);
            }
            return;
        }

        pc[1] = 1d;
        rq1fac = 1d;
        double[][] newElco = common.getElco();
        double[][] newTesco = common.getTesco();

        for (nq = 1; nq <= 5; nq++) {
            fnq = (double) nq;
            nqp1 = nq + 1;

            pc[nqp1] = 0d;
            for (i = nq + 1; i >= 2; i--) {
                pc[i] = pc[i-1] + fnq * pc[i];
            }
            pc[1] *= fnq;

            for (i = 1; i <= nqp1; i++) {
                newElco[nq][i] = pc[i] / pc[2];
            }
            newElco[nq][2] = 1d;
            newTesco[nq][1] = rq1fac;
            newTesco[nq][2] = ((double) nqp1) / newElco[nq][1];
            newTesco[nq][3] = ((double) (nq + 2)) / newElco[nq][1];
            common.setElco(newElco);
            common.setTesco(newTesco);
            rq1fac /= fnq;
        }
    }

    /**
     * Scales the integration step size and the higher-order method history arrays.
     * <p>
     * Used to rescale the step size {@code rh} and associated history terms after a change
     * in the integration step size is requested. It ensures numerical stability by scaling
     * the multi-step method history array {@code zh}.
     * If the method used is Adams ({@code meth} == 1). The function also performs additional
     * checks and restricts the scaling factor ({@code rh}) to maintain accuracy.
     * </p>
     * 
     * @param ctx   the {@code LSODAContext} containing integration parameters, method state and history arrays
     * @param rh    proposed scaling factor for the integration step size
     */
    public static void scaleh(LSODAContext ctx, double rh) {
        double r;
        int j, i;
        LSODACommon common = ctx.getCommon();
        int neq = ctx.getNeq();
        double hmxi = ctx.getOpt().getHmxi();

        rh = Math.min(rh, common.getRmax());
        rh = rh / Math.max(1d, Math.abs(common.getH()) * hmxi * rh);

        if (common.getMeth() == 1) {
            common.setIrflag(0);
            double pdh = Math.max(Math.abs(common.getH()) * common.getPdlast(), 0.000001d);
            if ((rh * pdh * 1.00001d) >= common.getSM1()[common.getNq()]) {
                rh = common.getSM1()[common.getNq()] / pdh;
                common.setIrflag(1);
            }
        }
        r = 1d;
        for (j = 2; j <= (common.getNq() + 1); j++) {
            r *= rh;
            for (i = 1; i <= neq; i++) {
                double[][] newYh = common.getYh();
                newYh[j][i] *= r;
                common.setYh(newYh);
            }
        }
        common.setH(common.getH() * rh);
        common.setRc(common.getRc() * rh);
        common.setIalth(common.getNq() + 1);
    }
    
    /**
     * Perform the nonlinear corrector step for the current integration step.
     * <p>
     * This method applies the Newton iteration or the functional iteration (depending on the method and solver state) to compute a corrected solution vector {@code y} for the current step. 
     * The correction is applied using the method defined by {@code miter}, and convergence is assessed using the weighted norm of the correction vector {@code del}.
     * </p>
     * 
     * <p>
     * If convergence fails within the maximum number of allwed iterations, a failure is reported.
     * If necessary, the Jacobian is recomputed during the first corrector iteration.
     * </p>
     * 
     * @param ctx   the LSODA context object holding the solver configuration, state, memory arrays and user functions
     * @param y     the working solution vector to be corrected; update in-place with the final corrected solution
     * @param pnorm the norm of the predicted solution (used in convergence check)
     * @param del   the output parameter (length-1 array) for the weighted norm of the current correction
     * @param delp  the output parameter (length-1 array) for the previous occrection norm (used in convergence rate estimation)
     * @param told  the value of t at the beginning of the current step (in case of correction failure)
     * @param m     the output parameter (length-1 array) for the number of correction interations used
     * @return      0 if the correction process converges successfully; nonzero if it failed
     **/
    public static int correction(LSODAContext ctx, double[] y, double pnorm, double[] del, double[] delp, double told, int[] m) throws DerivativeException{
        LSODACommon common = ctx.getCommon();
        int i;
        double rm, rate, dcon;
        int neq = ctx.getNeq();

        m[0] = 0;
        rate = 0d;
        del[0] = 0d;
        for (i = 1; i <= neq; i++) {
            y[i] = common.getYh()[1][i];
        }

        common.setSavf(findDerivatives(ctx.getOdeSystem(), common.getTn(), y)); 
        common.setNfe(common.getNfe() + 1);
        
        while (true) { 
            if (m[0] == 0) {
                if (common.getIpup() > 0) {
                    int ierpj = prja(ctx, y);
                    common.setJcur(1);
                    common.setIpup(0);
                    common.setRc(1d);
                    common.setNslp(common.getNst());
                    common.setCrate(0.7d);
                    if (ierpj == 0) {
                        return corfailure(ctx, told);
                    }
                }

                double[] acor = common.getAcor();
                for (i = 1; i <= neq; i++) {
                    acor[i] = 0d;
                }
                common.setAcor(acor);
            }

            if (common.getMiter() == 0) {
                double[] savf = common.getSavf();
                for (i = 1; i <= neq; i++) {
                    savf[i] = common.getH() * savf[i] - common.getYh()[2][i];
                    y[i] = savf[i] - common.getAcor()[i];
                }
                common.setSavf(savf);

                del[0] = vmnorm(neq, y, common.getEwt());
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getYh()[1][i] + common.getEl()[1] * common.getSavf()[i];
                }
                common.setAcor(common.getSavf());
            }
            else {
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getH() * common.getSavf()[i] - (common.getYh()[2][i] + common.getAcor()[i]);
                }
                solsy(ctx, y);
                del[0] = vmnorm(neq, y, common.getEwt());
                double[] acor = common.getAcor();
                for (i = 1; i <= neq; i++) {
                    acor[i] += y[i];
                    y[i] = common.getYh()[1][i] + common.getEl()[1] * acor[i];
                }
                common.setAcor(acor);
            }
            if (del[0] <= 100d * pnorm * common.ETA) {
                break;
            }
            if (m[0] != 0 || common.getMeth() != 1) {
                if (m[0] != 0) {
                    rm = 1024d;
                    if (del[0] <= (1024d * delp[0])) {
                        rm = del[0] / delp[0];
                    }
                    rate = Math.max(rate, rm);
                    common.setCrate(Math.max(0.2 * common.getCrate(), rm));
                }
                double conit = 0.5 / (double) (common.getNq() + 2);
                dcon = del[0] * Math.min(1d, 1.5 * common.getCrate()) / (common.getTesco()[common.getNq()][2] * conit);
                if (dcon <= 1d) {
                    common.setPdest(Math.max(common.getPdest(), rate / Math.abs(common.getH() * common.getEl()[1])));
                    if (common.getPdest() != 0d) {
                        common.setPdlast(common.getPdest());
                    }
                    break;
                }
            }
            m[0]++;
            if (m[0] == common.MAXCOR || (m[0] >= 2 && del[0] > 2d * delp[0])) {
                if (common.getMiter() == 0 || common.getJcur() == 1) {
                    return corfailure(ctx, told);
                }
                common.setIpup(common.getMiter());
                m[0] = 0;
                rate = 0d;
                del[0] = 0d;
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getYh()[1][i];
                }
                
            }
            else {
                delp[0] = del[0];
            }

            common.setSavf(findDerivatives(ctx.getOdeSystem(), common.getTn(), y));  
            common.setNfe(common.getNfe() + 1);
        }
        return 0;
    }
    
    /**
     * Solves a linear system arising during the numerical integration process.
     * This method is specifically made for the case where the method indicator ('miter') is equal to 2,
     * which means that the system is solved linearly using LU decomposition with partial pivoting.
     * <p>
     * The system solved is of the form>
     * <pre>
     *      P * L * U * x = y
     * </pre>
     * where the LU decomposition and pivoting are already performed and stored in the context.
     * The result of the solution replaces the contents of the input vector {@code y}.
     * </p>
     * 
     * @param ctx   the {@code LSODAContext} holding the system's dimension, method flags, working memory
     *              LU factorization and pivot indices (i.e. {@code wm} and {@code ipvt}).
     * @param y     the right hand side vector of the linear system; upon return, {@code y} is overwritten with the solution.
     * @return      1, if the system was successfully solved.\n
     * @throws      IllegalStateException if the method flag {@code miter} is not equal to 2.
     */
    public static int solsy(LSODAContext ctx, double[] y) {
        int neq = ctx.getNeq();
        LSODACommon common = ctx.getCommon();
        if (common.getMiter() != 2) {
            throw new IllegalStateException("Solsy called with miter != 2: miter = " + common.getMiter());
        }
        if (common.getMiter() == 2) {
            dgesl(common.getWm(), neq, common.getIpvt(), y, 0);
        }
        return 1;
    }
    
    /**
     * Handles correction failure.
     * <p>
     * Updates the solver's internal state when a correction failure occurs during integration.
     * Following key operations are performed:
     *  <ul>
     *      <li>Increment the correction failure counter <code>Ncf</code>.</li>
     *      <li>Sets maximum residual factor <code>Rmax</code> to 2 and resets the current time <code>Tn</code> to the
     *          provided <code>told</code> value.</li>
     *      <li>Adjusts the correction history matrix <code>Yh</code> by computing the difference between successive rows.</li>
     *      <li>Checks whether the absolute value of the step size <code>H</code> is less than or equal to a slightly scaled
     *          minimum step size (<code>hmin * 1.0001</code>), or if the number of correction failures has reached the maximum
     *          allowed <code>MXNCF</code>. If either condition holds, the function returns <code>2</code>, indicating a fatal failure.</li>
     *      <li>If non of the fatal conditions are met, the preconditioner type <code>Ipup</code> is updated based on the current
     *          iteration method <code>Miter</code>, and the function returns <code>1</code> to signal the failure is recoverable.</li>
     *  </ul>
     * </p>
     * 
     * @param ctx   Contains the current solver state, internal counters, matrices and configuation parameters.
     * @param told  The previous time value to which the solver reverts upon a correction failure.
     * @return      <code>1</code> if the correction failure is recoverable and the solver can continue;
     *              <code>2</code> if the failure is fatal.
    */
    public static int corfailure(LSODAContext ctx, double told) {
        LSODACommon common = ctx.getCommon();
        int j, i1, i;
        int neq = ctx.getNeq();
        double hmin = ctx.getOpt().getHmin();
        common.setNcf(common.getNcf() + 1);
        common.setRmax(2d);
        common.setTn(told);

        for (j = common.getNq(); j >= 1; j--) {
            for (i1 = j; i1 <= common.getNq(); i1++) {
                for (i = 1; i <= neq; i++) {
                    double[][] yh = common.getYh();
                    yh[i1][i] -= yh[i1 + 1][i];
                    common.setYh(yh);
                }
            }
        }
        if (Math.abs(common.getH()) <= hmin * 1.0001d || common.getNcf() == common.MXNCF) {
            return 2;
        }
        common.setIpup(common.getMiter());
        return 1;
    }

    /**
     * Solves a system of linear equations using an LU-factorized matrix.
     * <p>
     * This function solvers either <code>A * x = b</code> or <code>A<sup>T</sup> * x = b</code>, depending on the value of <code>job</code>.
     * The matrix <code>A</code> is assumed to have previously factorized into its LU components, with pivot indices stored in <code>ipvt</code>.
     * The solution is computed in place, with <code>b</code> being overwritte by the solution vector.
     * </p>
     * <p>
     * When <code>job == 0</code>, the routine performs a forward substitution followed by a back substiution to solve <code>A * x = b</code>.
     * In the forward phase, a dot product (using <code>ddot()</code>) is used to add contribution from previous vectors, and the algorithm
     * checks for singular or nearly singular pivot elements.
     * In the back substitution phase, additional dot product computations are performed and pivot interchages are applied as per <code>ipvt</code>.
     * </p>
     * <p>
     * When <code>job != 0</code> (will typicall be 1), the function solves the transpoed system <code>A<sup>T</sup> * x = b</code>.
     * This method uses temporary array copies to simulate the pass-by-reference behavior of the original C implementation.
     * It applies pivot interchanges and performs scaled vector updates (vie <code>daxpy()</code>).
     * 
     * @param a     The LU-factorized matrix of order <code>n</code>, stored as a 2D array with 1-based indexing (index 0 is unused).
     * @param n     The order of the matrix <code>a</code>.
     * @param ipvt  The pivot indices resulting from the LU factorization; 
     *              <code>ipvt</code> indicates the pivot row for column <code>k</code>.
     * @param b     The right-hand side vector; on output, <code>b</code> is overwritten with the solution vector.
     * @param job   Indicates the system to be solved: 
     *              <ul><li><code>job == 0</code> for solving <code>A * x = b</code></li>
     *                  <li><code>job != 0</code> for solving <code>A<sup>T</sup> * x = b</code></ul>
     */
    public static void dgesl(double[][] a, int n, int[] ipvt, double[] b, int job) {

        int k, j;
        double t;


        if (job == 0) {
            for (k = 1; k <= n; k++) {
                t = ddot(k-1, a[k], b, 1, 1);
                if (Math.abs(a[k][k]) < 1e-12) {
                    throw new IllegalArgumentException("Matrix is singular or nearly singular at index " + k);
                }
                b[k] = (b[k] - t) / a[k][k];
            }

            for (k = n - 1; k >= 1; k--) { 
                double[] aRowK = Arrays.copyOfRange(a[k], k, a[k].length);
                double[] bSub = Arrays.copyOfRange(b, k, b.length);
    
                double ddotResult = ddot(n - k, aRowK, bSub, 1, 1);

                b[k] += ddotResult;
                j = ipvt[k];
                if (j != k) {
                    t = b[j];
                    b[j] = b[k];
                    b[k] = t;
                }
            }
            return;
        }

        /* The following two loops use a system of array copies to simulate pass-by-reference behavios in the original C code. */
        for (k = 1; k <= n - 1; k++) {
            j = ipvt[k];
            t = b[j];

            if (j != k) {
                b[j] = b[k];
                b[k] = t;
            }

            double[] tempSubB = Arrays.copyOfRange(b, k, n + 1);
            double[] tempSubA = Arrays.copyOfRange(a[k], k, n + 1);
     
            daxpy(n - k, t, tempSubA, 1, 1, tempSubB);
            System.arraycopy(tempSubB, 1, b, k + 1, n - k);
        }

          for (k = n; k >= 1; k--) {
            b[k] = b[k] / a[k][k];
            t = -b[k];
        
            daxpy(k-1, t, a[k], 1, 1, b);
            
        }
    }

    /**
     * Performs a constant times vector plus vector operation (AXPY) on double precision arrays.
     * <p>
     * This function computes the operation:
     * <pre>
     *      dy = da * dx + dy
     * </pre>
     * where <code>dx</code> and <code>dy</code> are vectors and <code>da</code> is a scalar multiplier.
     * The computation is performed on <code>n</code> elements of the vectors, with the the ability to account 
     * for non-unit increments in accessing elements from both <code>dx</code> and <code>dy</code> (set by 
     * <code>incx</code> and <code>incy</code>).
     * </p>
     * <p>
     * The function first checks if the number of elements (<code>n</code>) is negative or if the scalar <code>da</code>
     * is zero; in both cases, no operation is performed and the functino terminates.
     * </p>
     * <p>
     * The function is set to handle several cases:
     *  <ul>
     *      <li>If <code>incx</code> differs from <code>incy</code> or if <code>incy</code> <= 1, the function computes effective
     *          indices for the <code>dx</code> and <code>dy</code> arrays (adjusting for negative increments if necessary) and
     *          perform the AXPY operation in a simple loop.</li>
     *      <li>If the increment for <code>dx</code> is 1, optimized looping is applied. In this case, the function first processes
     *          any remaining elements (<code>n % 4</code>) individually, and the updates the remaining elements in blocks of four
     *          to enhance performance.</li>
     *      <li>For any other cases, a default iteration over the vector elements is used.</li>
     *  </ul>
     * </p>
     * 
     * 
     * @param n     The number of elements in the vectors to be processed;
     *              if <code>n</code> is less than 0, no operation will be completed.
     * @param da    The scalar multiplier applied to each element of <code>dx</code>.
     * @param dx    The vector whose elements are scaled by <code>da</code> and then added to the corresponding elements in <code>dy</code>.
     * @param incx  The increment for accessing successive elements of <code>dx</code>.
     * @param incy  The increment for accessing successive elements of <code>dy</code>.
     * @param dy    The destination vector that is updated in place with the result of the AXPY operation.
    */
    public static void daxpy(int n, double da, double[] dx, int incx, int incy, double[] dy) {
        int i, ix, iy, m;

        if (n < 0 || da == 0d) {
            return;
        }
        
        if (incx != incy || incy < 1) {
            ix = 1;
            iy = 1;
            if (incx < 0) {
                ix = (-n + 1) * incx + 1;
            }
            if (incy < 0) {
                iy = (-n + 1) * incy + 1;
            }

            for (i = 1; i <= n; i++) {
                dy[iy] += da * dx[ix];
                ix += incx;
                iy += incy;
            }
            return;
        }

        if (incx == 1) {
            m = n % 4;
            if (m != 0) {
                for (i = 1; i <= m; i++) {
                    dy[i] += da * dx[i];
                }
                if (n < 4) {
                    return;
                }
            }
            for (i = m + 1; i <= n; i += 4) {
                dy[i] += da * dx[i];
                dy[i + 1] += da * dx[i + 1];
                dy[i + 2] += da * dx[i + 2];
                dy[i + 3] += da * dx[i + 3];
            }
            return;
        }

        for (i = 1; i <= n + incx; i += incx) {
            dy[i] += da * dx[i];
        }
        return;
    
    }

    /**
     * Computes the dot product of two vectors dx and dy of length n, with given increments incx and incy.
     * The dot product is computed using the formula:
     * <pre>
     *    dotprod = sum(i=0,...,n-1) dx[start_x + i * incx] * dy[start_y + i * incy]
     * </pre>
     * where `start_x` and `start_y` depend on the increment direction.
     * 
     * @param n the number of elements to include in the dot product computation
     * @param dx the first vector
     * @param dy the second vector
     * @param incx the increment for accessing elements in dx
     * @param incy the increment for accessing elements in dy
     * @return the computed dot product of the two vectors
     * @throws IndexOutOfBoundsException if dx or dy are too small for the given increments
     */
    public static double ddot(int n, double[] dx, double[] dy, int incx, int incy) {
        double dotprod;
        int ix, iy, i;

        if (dx.length < (1 + (n - 1) * Math.abs(incx)) || dy.length < (1 + (n - 1) * Math.abs(incy))) {
            throw new IndexOutOfBoundsException("Array size too small for given incx and incy");
        }

        dotprod = 0d;
        if (n <= 0) {
            return dotprod;
        }

        if (incx != incy || incx < 1) {
            ix = 1; 
            iy = 1;
            if (incx < 0) {
                ix = (-n + 1) * incx + 1;
            }
            if (incy < 0) {
                iy = (-n + 1) * incy + 1;
            }
            for (i = 1; i <= n; i++) {
                dotprod += dx[ix] * dy[iy];
                ix += incx;
                iy += incy;
            }
            return dotprod;
        }

        if (incx == 1) {

            for (i = 1; i<= n; i++) {
                dotprod += dx[i] * dy[i];
            }
            return dotprod;
        }

        for (i = 1; i <= n * incx; i += incx) {
            dotprod += dx[i] * dy[i];
        }
        return dotprod;
    }

    /** 
     * Approximates and factorizes the Jacobian matrix used for preconditioning in the ODE solver.
     * <p>
     * This function computes a finite difference approximation to the Jacobian matrix for the current state of the ODE system.
     * I perturbs the state vector <code>y</code> and evaluates the system function to estimate the directional derivatives.
     * The resulting differences, scaled by the step size, tolerance and a computed factor, are used to update the weighted
     * difference matrix <code>Wm</code>. The matrix is then modifiec by unitizing its diagonal elements and factorized 
     * via LU decomposition (by calling <code>dgefa()</code>). This makes it ready for use as a preconditioner in the
     * iterative solution process.
     * </p>
     * <p>
     * The algorithm is executed only if the iteration method <code>miter</code> = 2. In cases where this is not true, an error
     * is logged and the function returns a failure status.
     * Additionally, counters for Jacobian evaluations (<code>nje</code>) and function evaluations (<code>nfe</code>) are updated accordingly.
     * </p>
     * 
     * @param ctx   the LSODA context containing solver state, common data, tolerances (abosulte and relative),
     *              and ODE function evaluation information
     * @param y     the state vector of the ODE system that is used to compute the finite differences for Jacobian approximation
     * 
     * @return      1: if the Jacobian approximation and subsequent matrix factorization succeed;
     *              2: 0 if an error occurs (e.g., an unexpected iteration method)
     */
    public static int prja(LSODAContext ctx, double[] y) throws DerivativeException{
        int i, j; 
        int[] ier = new int[1]; //in the original code, ier was of type int and passed by reference. I changed it to simulate this logic.
        double fac, hl0, r, r0, yj;
        LSODACommon common = ctx.getCommon();
        int neq = ctx.getNeq();

        common.setNje(common.getNje() + 1);
        hl0 = common.getH() * common.getEl()[1];

        if (neq == 0) {
            return 1;
        }

        if (common.getMiter() != 2) {
            logError(String.format("[prja] miter != 2, miter = %d", common.getMiter()), ctx.getData());
            return 0;
        }
        if (common.getMiter() == 2) {
            fac = vmnorm(neq, common.getSavf(), common.getEwt());
            r0 = 1000d * Math.abs(common.getH()) * common.ETA * ((double) neq) * fac;
            if (r0 == 0d) {
                r0 = 1d;
            }
            
            for (j = 1; j <= neq; j++) {  
                yj = y[j];
                r = Math.max(common.SQRTETA * Math.abs(yj), r0 / common.getEwt()[j]);
                y[j] += r;
                fac = -hl0 / r;

                common.setAcor(findDerivatives(ctx.getOdeSystem(), common.getTn(), y));

                double[][] wm = common.getWm();
                for (i = 1; i <= neq; i++) {
                    wm[i][j] = (common.getAcor()[i] - common.getSavf()[i]) * fac;
                }
                common.setWm(wm);
                y[j] = yj;
            }
            
            common.setNfe(common.getNfe() + neq);
            
            common.setPdnorm(fnorm(neq, common.getWm(), common.getEwt()) / Math.abs(hl0));

            double[][] wm = common.getWm();
            for (i = 1; i <= neq; i++) {
                wm[i][i] += 1d;
            }
            common.setWm(wm);

            dgefa(common.getWm(), neq, common.getIpvt(), ier);

            if (ier[0] != 0) {
                return 0;
            }
        }
        return 1;
    }

    /**
     * Perform an LU factorization of a square matrix using Gaussian elimination **with** partial pivoting.
     * <p>
     * This function factorizes the matrix <code>a</code> in place into a product of a lower triangular matrix L
     * and an upper triangular matrix U (with the unit diagonal of L bein implicit). The factorization is computed
     * per Gaussian elimination with partial pivoting, i.e. for each column the pivot is selected based on the 
     * maximum absolute value in the current column segment (utilizing <code>idamax()</code>)
     * </p>
     * <p>
     * For each column index <code>k</code> from 1 to <code>n - 1</code>, the algorithm perform the following steps:
     * <ol>
     *   <li>A helper column vector is constructed from <code>a[k..n][k]</code> to replicate pointer logic of the original
     *       C code, or better said, to simulate pass-by-reference from the C code. The pivot index is then determined by
     *       <code>idamax()</code></li>
     *   <li>The pivot index is stored in the <code>ipvt</code> array. If the pivot element is zero, which indicates that
     *       the matrix is singular, the rountine sets <code>info[0]</code> to <code>k</code> and terminates.</li>
     *   <li>If the pivot is not already in position <code>k</code>, the corresponding rows are swapped.</li>
     *   <li>The pivot row is scaled by computing a factor of <code>(-1) / a[k][k]</code> (using <code>dscal()</code>). The
     *       submatrix is then updaated by calling on <code>daxpy</code>.</li>
     * </ol>
     * </p>
     * 
     * @param a     The square matrix to be factorized, stored as a 2D array; it is modified in place to contain
     *              the L and U factors. Note: the implementation uses 1-based indexing as in the original C code!
     * @param n     The order of the matrix (number of rows and columns).
     * @param ipvt  An integer array in which the pivot indices are recorded;
     *              On output, <code>ipvt[k]</code> holds the index of the pivot element for column <code>k</code>.
     * @param info  An integer array of length >= 1 used as an output flag;
     *              <code>info[0]</code> is set to 0 if the factorization is successful. If a zero pivot is encountered
     *              at column <code>k</code>, <code>info[0]</code> is set to <code>k</code> and the routine terminates.
     */
    public static void dgefa(double[][] a, int n, int[] ipvt, int[] info) {
        
        int j, k, i;
        double t;
        
        info[0] = 0;
        
        for (k = 1; k <= n - 1; k++) {
            
            //since in the original C code a pointer to the first column vector was passed, this needs to be replicated using a helper array in Java
            double[] temp1 = Arrays.copyOfRange(a[k], k-1, n+1);

            j = idamax(n - k + 1, temp1, 1) + k - 1;
            ipvt[k] = j;


            
            if (a[j][k] == 0d) {
                info[0] = k;
                continue;
            }
            
            if (j != k) {
                
                t = a[k][j];
                a[k][j] = a[k][k];
                a[k][k] = t;

            }
            
            t = -1d / a[k][k];

            double[] temp2 = Arrays.copyOfRange(a[k], k, n+1);
            dscal(n-k, t, 1, temp2);

            for(int it = k; it <= n+1; it++){
                a[k][it] = temp2[it - k];
            }
            
            for (i = k + 1; i <= n; i++) {
                t = a[i][k];
                if (j != k) {
                    a[i][j] = a [i][k];
                    a[i][k] = t;
                }
                
                double[] temp3 = Arrays.copyOfRange(a[k], k, n+1);
                double[] temp4 = Arrays.copyOfRange(a[i], k, n+1);
                daxpy(n - k, t, temp3, 1, 1, temp4);

                for(int it = k; it<= n+1; it++){
                    a[i][it] = temp4[it - k];
                }
            }

        }
   
        ipvt[n] = n;
        if (a[n][n] == 0d) {
            info[0] = n;
        }

    }

    /**
     * Computes the Frobenius norm of an n x n matrix a, which is consistent with the weighted max-norm on vectors.
     * The norm is calculated using the formula:
     * <pre>
     *    fnorm = max(i=1,...,n) (w[i] * sum(j=1,...,n) fabs(a[i][j]) / w[j])
     * </pre>
     * 
     * where 'w' is a weight vector.
     * 
     * @param n the size of the (square) matrix
     * @param a the n x n matrix whose norm is to be calculated
     * @param w the weight vector of length n used for normalization
     * @return the computed weighted norm of the matrix
     */
    public static double fnorm(int n, double[][] a, double[] w) {

        double an = 0d, sum;

        for (int i = 1; i <= n; i++) {
            sum = 0d;
            for (int j = 1; j <= n; j++) {
                sum += Math.abs(a[i][j]) / w[j];
            }
            an = Math.max(an, sum * w[i]);
        }

        return an;
    }

    /**
     * Finds the index of the element with the maxiumum absolute value in the vector dx.
     * 
     * <p>
     * The method scans a vector of double precision numbers over <code>n</code> elements using a specified increment <code>incx</code>.
     * It returns the 1-indexed position of the element whose absolute value is the largest.
     * 
     * If <code>n</code> is less than or equal to 0, the method returns 0.
     * If <code>n</code> is 1 or if <code>incx</code> is not positive, the method returns 1 (i.e. the first element is the maximum by default).
     * When <code>incx</code> is not equal to 1, the method steps through the vector by <code>incx</code> increments;
     * otherwise, it processes the vector sequentially for better performance.
     * </p>
     * 
     * @param n     the number of elements in dx to be examined
     * @param dx    the input vector of double values
     * @param incx  the increment for iterating through elements in dx
     * @return      the 1-indexed position of the element with the larges absolute value, of 0 if n is less that or equal to 0
     */
    public static int idamax(int n, double[] dx, int incx) {

        double dmax, xmag;
        int i, ii, xindex;

        xindex = 0;
        if (n <= 0) {
            return xindex;
        }
        xindex = 1;
        if (n <= 1 || incx <= 0) {
            return xindex;
        }

        if (incx != 1) {
            dmax = Math.abs(dx[1]);
            ii = 2;
            for (i = 1 + incx; i <= n * incx; i += incx) {
                xmag = Math.abs(dx[i]);
                if (xmag > dmax) {
                    xindex = ii;
                    dmax = xmag;
                }
                i++;
            }
            return xindex;
        }

        dmax = Math.abs(dx[1]);
        for (i = 2; i <= n; i++) {
            xmag = Math.abs(dx[i]);
            if (xmag > dmax) {
                xindex = i;
                dmax = xmag;
            }
        }
        return xindex;
    }

    /**
     * Scales a vector by a constant factor.
     * <p>
     * The function multiplies <code>n</code> elements of the input vector <code>dx</code> by the scalar <code>da</code>.
     * The elements to be scaled are iterated using <code>incx</code>.
     * </p>
     * <p>
     * When <code>incx</code> is not equal to 1, the function iterates over the vector with steps of size <code>incx</code>.
     * If <code>incx</code> = 1, the remaining elements (<code>m = n % 5</code>) are first scaled individually, and then the
     * remaining elements are processed in blocks of five.
     * </p>
     * 
     * @param n     The number of elements in the vector <code>dx</code> to be scaled;
     *              if <code>n</code> <= 0, no operation is performed and the function terminates.
     * @param da    The scalar value by which each element of <code>dx</code> is multiplied.
     * @param incx  The increment by which elements in the vector <code>dx</code> are accessed.
     * @param dx    The vector to be scaled; the selected elements are modified in place.
     */
    public static void dscal(int n, double da, int incx, double[] dx) {
        int m, i;

        if (n <= 0) {
            return;
        }

        if (incx != 1) {
            for (i = 1; i <= n * incx; i += incx) {
                dx[i] = da * dx[i];
            }
            return;
        }

        m = n % 5;
        if (m != 0) {
            for (i = 1; i <= m; i++) {
                dx[i] = da * dx[i];
            }
            if (n < 5) {
                return;
            }
        }
        for (i = m + 1; i <= n; i += 5) {
            dx[i] *= da;
            dx[i + 1] *= da;
            dx[i + 2] *= da;
            dx[i + 3] *= da;
            dx[i + 4] *= da;
        }
        return;
    }

    /*
     * Helper function to compute derivatives
     */
    public static double[] findDerivatives(DESystem odeSystem, double t, double[] y) throws DerivativeException{
        int n = odeSystem.getDimension();
        double[] ydot = new double[n];
        odeSystem.computeDerivatives(t, Arrays.copyOfRange(y, 1, n+1), ydot);
        double[] ydot1 = new double[n + 1];
        System.arraycopy(ydot, 0, ydot1, 1, n);
        return ydot1;
    }

    public int lsoda(LSODAContext ctx, double[] y, double[] t, double tout) throws DerivativeException {
        int jstart;

        LSODACommon common = ctx.getCommon();
        LSODAOptions opt = ctx.getOpt(); 
        DESystem odeSystem = ctx.getOdeSystem();
        LSODAStepper stepper = new LSODAStepper(ctx, null, 0);

        if (common == null) {
            return hardFailure(ctx, opt.toString(), "[lsoda] illegal common block, did you call lsoda_pre");
        }

        y = Arrays.copyOfRange(y, 1, y.length);
        
        int i;
        double big, hmx, rh, tcrit, tdist, tnext, tol, tolsf, tp, size, sum, w0;
        hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
        boolean ihit = Math.abs(common.getTn() - opt.getTcrit()) <= (100d * common.ETA * hmx);
        double h0 = opt.getH0();

        final int itask = opt.getItask();
        final double[] rtol = Arrays.copyOfRange(opt.getRtol(), 1, opt.getRtol().length);
        final double[] atol = Arrays.copyOfRange(opt.getAtol(), 1, opt.getAtol().length);

        double[] yOffset = new double[y.length - 1];
        System.arraycopy(y, 1, yOffset, 0, yOffset.length);

        double[][] yh = common.getYh();
        double[] yhOffset = new double[yh[2].length - 1];
        System.arraycopy(yh[2], 1, yhOffset, 0, yhOffset.length);

        if (ctx.getState() == 1 || ctx.getState() == 3) {
            h0 = opt.getH0();
            if (ctx.getState() == 1) {
                if ((tout - t[0]) * h0 < 0.) {
                    hardFailure(ctx, opt.toString(), "[lsoda] tout = g behind t = %g. integration direction is given by %g");
                }
            }
        }

        if (ctx.getState() == 3) {
            jstart = -1;
        }

        if (ctx.getState() == 1) {
            common.setMeth(1); // enum to define which method to use
            common.setTn(t[0]);
            common.setTsw(t[0]);
            if (itask == 4 || itask == 5) {
                tcrit = opt.getTcrit();
                if ((tcrit - tout) * (tout - t[0]) < 0.) {
                    hardFailure(ctx, opt.toString(), "[lsoda] itask = 4 or 5 and tcrit behind tout");
                }
                if (h0 != 0. && (t[0] + h0 - tcrit) * h0 > 0.) {
                    h0 = tcrit - t[0];
                }
            }
            jstart = 0;
            common.setNq(1);

            double[][] newYh = common.getYh();
            newYh[2] = findDerivatives(odeSystem, t[0], y);
            common.setYh(newYh);

            common.setNfe(1);

            for (int k = 1; k <= ctx.getNeq(); k++) {
                common.getYh()[1][k] = y[k];
            }
            common.setNfe(1);

            ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);

            for (i = 1; i <= neq; i++) {
                if (common.getEwt()[i] <= 0.) {
                    hardFailure(ctx, opt.toString(), String.format("[lsoda] ewt[%d] = %g <= 0.", i, common.getEwt()));
                }
            }

            if (h0 == 0d) {
                tdist = Math.abs(tout - t[0]);
                w0 = Math.max(Math.abs(t[0]), Math.abs(tout));
                if (tdist < 2. * common.ETA * w0) { 
                    hardFailure(ctx, opt.toString(), "[lsoda] tOut too close to t to start integrations");
                }
                tol = 0.;
                for (i = 1; i <= neq; i++) {
                    tol = Math.max(tol, rtol[i]);
                }
                if (tol <= 0.) {
                    for (i = 1; i <= neq; i++) {
                        double atoli = atol[i];
                        double ayi = Math.abs(y[i]);
                        if (ayi != 0.) {
                            tol = Math.max(tol, atoli / ayi);
                        }
                    }
                }
                tol = Math.max(tol, 100. * common.ETA);
                tol = Math.min(tol, 0.001);
                sum = vmnorm(neq, common.getYh()[2], common.getEwt());
                sum = 1. / (tol * w0 * w0) + tol * sum * sum;
                h0 = 1. / Math.sqrt(sum);
                h0 = Math.min(h0, tdist);
                h0 = h0 * ((tout - t[0] >= 0.) ? 1. : -1.);
            }

            rh = Math.abs(h0) * opt.getHmxi();
            if (rh > 1.) {
                h0 /= rh;
            }

            common.setH(h0);
            for (i = 1; i <= neq; i++) {
                common.getYh()[2][i] *= h0;
            }

            int kflag = stepper.stoda(ctx, y, jstart);

            if (ctx.getState() == 2 || ctx.getState() == 3) {
                ctx.nslast = common.getNst();

                switch (itask) {
                    case 1:
                        if ((common.getTn() - tout) * common.getH() >= 0d) {
                            intdyReturn(ctx, y, t, tout, itask);
                        }
                        break;

                    case 2:
                        break; //nothing provided in source code
                    
                    case 3:
                        tp = common.getTn() - common.getHu() * (1.0 + 100.0 * common.ETA);
                        if ((tp - tout) * common.getH() > 0d) {
                            hardFailure(ctx, opt.toString(), String.format("[lsoda] itask = %g and tout behind tcur = %f", itask, common.getHu()));
                        }
                        if ((common.getTn() - tout) * common.getH() < 0d) break;
                        //return success
                        
                    case 4:
                        tcrit = opt.getTcrit();
                        if((common.getTn() - tcrit) * common.getH() > 0d) {
                            hardFailure(ctx, "[lsoda] itask = 4 or 5 and tcrit behind tcur");
                        }
                        if((tcrit - tout) * common.getH() < 0d) {
                            hardFailure(ctx, "[lsoda] itask = 4 or 5 and tcrit behind tout");
                        }
                        if((common.getTn() - tout) * common.getH() >= 0d) {
                            intdyReturn(ctx, y, t, tout, itask);
                        }
                        break;
                    
                    case 5:
                        if(itask == 5) {
                            tcrit = opt.getTcrit();
                            if ((common.getTn() - tcrit) * common.getH() > 0d) {
                                hardFailure(ctx, "[lsoda] itask = 4 or 5 and tcrit behind tcur");
                            }
                        }
                        
                        
                        hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
                        ihit = Math.abs(common.getTn() - opt.getTcrit()) <= (100d * common.ETA * opt.getHmax());

                        if (ihit) {
                            t[0] = opt.getTcrit();
                            //return success
                        }
                        tnext = common.getTn() + common.getH() * (1d + 4d + common.ETA);
                        if ((tnext - opt.getTcrit()) * common.getH() <= 0d) {
                            break;
                        }
                        common.setH((opt.getTcrit() - common.getTn()) * (1d - 4d * common.ETA));
                        if (ctx.getState() == 2) {
                            jstart = -2;
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid itask value: " + itask);
                }
            }
            int k = 0;
            while (k == 0) {
                if (ctx.getState() != 1 || common.getNst() != 0) {
                    if ((common.getNst() - common.getNslast()) >= opt.getMxstep()) {
                        softFailure(ctx, -1, String.format("[lsoda] %f steps taken before reaching tout.", opt.getMxstep()));
                    }
                    ewset(y, rtol, atol, ctx.getNeq(), common);
                    for (int j = 1; j <= ctx.getNeq(); j++) {
                        if (common.getEwt()[j] <= 0d) {
                            softFailure(ctx, -6, "[lsoda] ewt[" + j + "] = " + common.getEwt()[j] + " <= 0.");
                        }
                    }
                }
                tolsf = common.ETA * vmnorm(ctx.getNeq(), common.getYh()[1], common.getEwt());
                if (tolsf > 0.01d) {
                    tolsf = tolsf * 200d;
                    if (common.getNst() == 0) {
                        hardFailure(ctx, String.format("[lsoda] -- at start of problem, too much accuracy\n requested for precision of machine, \n suggested scalilng factor = %f", tolsf));
                    }
                    softFailure(ctx, -2, String.format("[lsoda] -- at t = %f , too much accurary requested for precision of machine, suggested scaling factor = %f", t[0], tolsf));
                }

                if ((common.getTn() + common.getH()) == common.getTn()) {
                    common.setNhnil(common.getNhnil() + 1);
                    if (common.getNhnil() <= opt.getMxhnil()) {
                        logError(String.format("[lsoda] -- warning..internal t = %f and h = %f are\n", common.getTn(), common.getH()));
                        logError(String.format("[lsoda] -- such that in the machine, t + %f = t on the next step\n", common.getH()));
                        logError("[lsoda] -- solver will continue anyway.\n");
                        if (common.getNhnil() == opt.getMxhnil()) {
                            logError(String.format("[lsoda] -- above warning has been issued %d times,\n", common.getNhnil()));
                            logError("[lsoda] -- it will not be issued again for this problem\n");
                        }
                    }
                }

                kflag = stepper.stoda(ctx, y, jstart);
                if (kflag == 0) {
                    jstart = 1;

                    if (common.getMeth() != common.getMused()) {
                        common.setTsw(common.getTn());
                        jstart = -1;

                        if (opt.getIxpr() != 0) { // lsoda.c, line 787 say if(opt.getIxpr() != null), not available in Java --> still correct implementation?
                            if (common.getMeth() == 2) {
                                logError("[lsoda] a swith to the stiff method has occured.");
                            }
                            if (common.getMeth() == 1) {
                                logError("[lsoda] a switch to the non-stiff method has occured.");
                            }
                            logError(String.format("[lsoda] at t = %f and tentative step size h = %f, step = %d\n", common.getTn(), common.getH(), common.getNst()));
                        } 
                    }
                    
                    switch (itask) {
                        case 1:
                            if ((common.getTn() - tout) * common.getH() < 0d) {
                                continue;
                            }
                            intdyReturn(ctx, y, t, tout, itask);
                        
                        case 2:
                            successReturn(ctx, y, t, itask, ihit);

                        case 3:
                            if ((common.getTn() - tout) * common.getH() >= 0d) {
                                successReturn(ctx, y, t, itask, ihit);
                            }
                            continue; 

                        case 4:
                            tcrit = opt.getTcrit();
                            if ((common.getTn() - tout) * common.getH() >= 0d) {
                                intdyReturn(ctx, y, t, tout, itask);
                            } else {
                                hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
                                ihit = Math.abs(common.getTn() - tcrit) <= (100d * common.ETA * hmx);
                                if (ihit) {
                                    successReturn(ctx, y, t, itask, ihit);
                                }
                                tnext = common.getTn() + common.getH() * (1d + 4d + common.ETA);
                                if ((tnext - tcrit) * common.getH() <= 0d) {
                                    continue;
                                }
                                common.setH((tcrit - common.getTn()) * (1d - 4d * common.ETA));
                                jstart = -2;
                                continue;
                            }
                        
                        case 5:
                            tcrit = opt.getTcrit();
                            hmx = Math.abs(common.getTn() + Math.abs(common.getH()));
                            ihit = Math.abs(common.getTn() - tcrit) <= (100d * common.ETA * hmx);
                            successReturn(ctx, y, t, itask, ihit);
                    }
                }
            }
            if (kflag == -1 || kflag == -2) {
                big = 0d;
                common.setIxmer(1);

                for (i = 0; i <= ctx.getNeq(); i++) {
                    size = Math.abs(common.getAcor()[i] * common.getEwt()[i]);
                    if (big < size) {
                        big = size;
                        common.setIxmer(i);
                    }
                }
                if (kflag == -1) {
                    softFailure(ctx, -4, String.format("[lsoda] -- at t = %f and step size h = %f, the\n      error test failed repeatedly of\n      with Math.abs(h) = hmin\n", common.getTn(), common.getH()));
                }
                if (kflag == -2) {
                    softFailure(ctx, -5, String.format("[lsoda] -- at t = %f and step size h = %f, the\n      corrector convergence failed repeatedly\n     with Math.abs(h) = hmin\n", common.getTn(), common.getH()));
                }
            }

        }
        return 0;

        
        
    }

    public LSODAContext getCtx() {
        return ctx;
    }

    public void setCtx(LSODAContext ctx) {
        this.ctx = ctx;
    }

    public int getNeq() {
        return neq;
    }

    public void setNeq(int neq) {
        this.neq = neq;
    }

    public double[] getYh() {
        return yh;
    }

    public void setYh(double[] yh) {
        this.yh = yh;
    }

    public double[] getTn() {
        return tn;
    }

    public void setTn(double[] tn) {
        this.tn = tn;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}   