package org.simulator.math.odes.LSODA;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.exception.*;

public class LSODAIntegrator extends AdaptiveStepsizeIntegrator {

    /*
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(LSODAIntegrator.class.getName());
    
    public LSODAContext ctx;
    private double[] yOffset; // to store the result
    
    /*
     * LSODAIntegrator No argument constructor
     */
    public LSODAIntegrator() {
        super();
        this.ctx = new LSODAContext();
        LSODAOptions opt = new LSODAOptions();
        this.ctx.setOpt(opt);
    }

    /**
     * LSODAIntegrator constructor
     * <p>The same <code>relTol</code> and <code>absTol</code> value will be used for all the dependent variable calculations</p>
     * @param relTol Relative Tolerance
     * @param absTol Absolute Tolerance
     */
    public LSODAIntegrator(double relTol, double absTol) {
        super();
        this.ctx = new LSODAContext();
        LSODAOptions opt = new LSODAOptions();
        double[] aTol = {absTol};
        double[] rTol = {relTol};
        opt.setAtol(aTol);
        opt.setRtol(rTol);
        this.ctx.setOpt(opt);
        setAbsTol(absTol);
        setRelTol(relTol);
    }

    /**
     * LSODAIntegrator constructor
     * @param relTol Relative Tolerance
     * @param absTol Absolute Tolerance
     */
    public LSODAIntegrator(double[] relTol, double[] absTol) {
        super();
        this.ctx = new LSODAContext();
        LSODAOptions opt = new LSODAOptions();
        opt.setAtol(absTol);
        opt.setRtol(relTol);
        this.ctx.setOpt(opt);
    }

    public LSODAIntegrator(LSODAIntegrator integrator) {
        super(integrator);
        this.ctx = integrator.ctx;
        LSODAOptions opt = new LSODAOptions();
        opt.setAtol(integrator.ctx.getOpt().getAtol());
        opt.setRtol(integrator.ctx.getOpt().getAtol());
        this.ctx.setOpt(opt);
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
        double[] tArr = {t};
        this.lsoda(this.ctx, y, tArr, t + stepSize);
        
        for(int i=0; i<this.ctx.getNeq(); i++) {
            change[i] = yOffset[i+1] - y[i]; 
        }
        return change;
    }

    public static enum illegalTExceptionCases{
        toutBehindT,
        tcritBehindTout,
        tcritBehindTcur,
        toutToCloseToT,
        toutBehindTcurMinusHu
    };

    /**
     * hardFailure
     * @param ctx <code>LSODAContext</code> object
     * @return <code>LSODAContext</code> state, which is equal to -3
     */
    private int hardFailure(LSODAContext ctx) {
        ctx.setState(-3);
        return ctx.getState();
    }

    /**
     * softFailure
     * @param ctx <code>LSODAContext</code> object
     * @param code <code>LSODAContext</code> state 
     * @param t timePoint
     * @return <code>LSODAContext</code> state, which is the <code>code</code> itself
     */
    private int softFailure(LSODAContext ctx, int code, double[] t) {
        
        LSODACommon common = ctx.getCommon();
        double[] y = new double[common.getYh().length];

        if (common != null) {
            for (int i = 1; i <= ctx.getNeq(); i++) {
                y[i] = common.getYh()[1][i];
            }
            t[0] = common.getTn();
        }
        ctx.setState(code);
        return ctx.getState();
    }

    /**
     * successReturn
     * @param ctx   <code>LSODAContext</code> object
     * @param y     y array 
     * @param t     timePoint
     * @param itask task
     * @param ihit  ihit
     * @return <code>LSODAContext</code> state, which is 2 for successReturn
     */
    private int successReturn(LSODAContext ctx, double[] y, double[] t, int itask, boolean ihit) {
        LSODACommon common = ctx.getCommon();
        double tcrit = ctx.getOpt().getTcrit();
        for (int i = 1; i <= ctx.getNeq(); i++) {
            y[i] = common.getYh()[1][i];
        }
        t[0] = common.getTn();
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
     * This method evaluates the interpolating polynomial (or its derivatives) for the dependent variables at the given time {@code t},
     * using the history data stored in the {@code LSODACommon} object.
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
            logError(String.format("[intdy] t = %f illegal. t not in interval (tcur - _C(hu)) to tcur\n", t));
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

    /**
     * IntdyReturn
     * @param ctx {@code LSODAContext} object
     * @param y
     * @param t
     * @param tout
     * @param itask
     * @return {@code LSODAContext} state = 2, on successful return from intdy
     * @throws IntdyException 
     */
    public static int intdyReturn(LSODAContext ctx, double[] y, double[] t, double tout, int itask) {

        LSODACommon common = ctx.getCommon();
        int iflag = intdy(ctx, tout, 0, y);
    
        if (iflag != 0) {
            for (int i = 1; i <= ctx.getNeq(); i++) {
                y[i] = common.getYh()[1][i];
            }
            throw new IntdyException(itask, tout);
        }
        t[0] = tout;

        ctx.setState(2);

        return ctx.getState();

    }

    /**
     * checks the validity of {@code LSODAOptions} opt
     * @param ctx {@code LSODAContext} object
     * @param opt {@code LSODAOptions} object
     * @return True, if valid
     * @throws IllegalInputException
     */
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
            throw new IllegalInputException("lsodaPrepare","neq", ctx.getNeq(), "<= 0");
        }

        double[] rtol = opt.getRtol();
        double[] atol = opt.getAtol();

        if(rtol.length != ctx.getNeq()) {
            throw new IllegalInputException("lsodaPrepare","rtol length", rtol.length, "not equal to " + ctx.getNeq());
        }
        if(atol.length != ctx.getNeq()) {
            throw new IllegalInputException("lsodaPrepare","atol length = ", atol.length, "not equal to " + ctx.getNeq());
        }
        if (ctx.getState() == 1 || ctx.getState() == 3) {
            for (int i = 1; i <= ctx.getNeq(); i++) {
                double rtoli = rtol[i - 1];
                double atoli = atol[i - 1];
                if (rtoli < 0d) {
                    throw new IllegalInputException("lsodaPrepare","rtol[" + (i-1) + "]", rtoli, "less than 0");
                }
                if (atoli < 0d) {
                    throw new IllegalInputException("lsodaPrepare","atol[" + (i-1) + "]", atoli, "less than 0");
                }
            }
        }

        if (opt.getItask() == 0) opt.setItask(1);
        if (opt.getItask() < 1 || opt.getItask() > 5) {
            throw new IllegalInputException("lsodaPrepare","itask" , opt.getItask());
        }

        if (opt.getIxpr() < 0 || opt.getIxpr() > 1) {
            throw new IllegalInputException("lsodaPrepare","ixpr" , opt.getIxpr());
        }

        if (opt.getMxstep() < 0) {
            throw new IllegalInputException("lsodaPrepare","mxstep" , opt.getMxstep(), "less than 0");
        }

        if (opt.getMxstep() == 0) opt.setMxstep(mxstp0);

        if (opt.getMxhnil() < 0) {
            throw new IllegalInputException("lsodaPrepare","mxhnil" , opt.getMxhnil(), "less than 0");
        }

        if (ctx.getState() == 1) {
            if (opt.getMxordn() < 0) {
                throw new IllegalInputException("lsodaPrepare","mxordn" , opt.getMxordn(), "less than 0");
            }
            if (opt.getMxordn() == 0) opt.setMxordn(12);
            opt.setMxordn(Math.min(opt.getMxordn(), mord[1]));

            if (opt.getMxords() < 0) {
                throw new IllegalInputException("lsodaPrepare","mxords" , opt.getMxords(), "less than 0");
            }
            if (opt.getMxords() == 0) opt.setMxords(5);
            opt.setMxords(Math.min(opt.getMxords(), mord[2]));
        }

        if (opt.getHmax() < 0d) {
            throw new IllegalInputException("lsodaPrepare", "hMax", opt.getHmax(), "less than 0");
        }

        opt.setHmxi((opt.getHmax() > 0) ? 1d / opt.getHmax() : 0d);

        if (opt.getHmin() < 0d) {
            throw new IllegalInputException("lsodaPrepare", "hMin", opt.getHmin(), "less than 0");
        }

        return true;
    }

    /**
     * Initializes {@code LSODACommon} variabales with suitable values, and set it in {@code LSODAContext}
     * @param ctx    the {@code LSODAContext} object
     * @param mxords maximum order for stiff method
     * @param mxordn maximum order for nonstiff method
     * @@return {@code boolean} Returns true on successful allocation
     */
    public static boolean allocMemory(LSODAContext ctx, int mxords, int mxordn) {
        LSODACommon common = new LSODACommon(ctx.getNeq(), mxords, mxordn);
        ctx.setCommon(common);

        return true;
    }

    /**
     * Initializes and prepares an LSODA solver context for integration. 
     * 
     * <p>This function allocates and initializes all memory required for solving a system of ODEs
     * using the LSODA method. It also validates and sets up default solver options if they are not provided
     * explicitly in the {@code LSODAOptions} object.
     *
     * It must be called before calling {@code lsoda()} for the first time with a given {@code ctx} context.</p>
     * @param ctx the {@code LSODAContext} object
     * @param opt the {@code LSODAOptions} object
     * @return {@code boolean} Returns true on successful preparation
     * @throws IllegalInputException 
     */
    public boolean lsodaPrepare(LSODAContext ctx, LSODAOptions opt) {
        this.ctx = ctx;
        if(!checkOpt(ctx, opt)) {
            return false;
        }
        ctx.setOpt(opt);
        allocMemory(ctx, opt.getMxords(), opt.getMxordn());
        return true;
    }

    /**
     * Initializes and prepares an LSODA solver context for integration. 
     * 
     * <p>This function allocates and initializes all memory required for solving a system of ODEs
     * using the LSODA method. It also validates and sets up default solver options.
     *
     * It must be called before calling {@code solve()} for the first time with a given {@code SBMLInterpreter} interpreter.</p>
     * 
     * @param system the {@code DESystem} object, will be mostly {@code SBMLInterpreter} object
     * @param ixpr   1 to print messages, 
     *               0 to not print messages
     * @param itask  controls task performed by the solver. 
     * @param state  tells the solver how to proceed
     * 
     * @return {@code bool} Returns true on successful preparation
     * @throws IllegalInputException 
     */
    @Override
    public boolean prepare(DESystem system, int ixpr, int itask, int state) {
        this.ctx.setNeq(system.getDimension());
        LSODAOptions opt = this.ctx.getOpt();

        if((opt.getAtol().length == 1 || opt.getRtol().length ==1) && this.ctx.getNeq() != 1) {

            double[] atol = new double[ctx.getNeq()];
            double[] rtol = new double[ctx.getNeq()];
            double abstol = opt.getAtol()[0];
            double relTol = opt.getRtol()[0];
            for(int i=0; i<this.ctx.getNeq(); i++) {
                atol[i] = abstol;
                rtol[i] = relTol;
            }

            opt.setAtol(atol);
            opt.setRtol(rtol);
        }

        if(!checkOpt(this.ctx, opt)) {
            return false;
        }

        allocMemory(this.ctx, opt.getMxords(), opt.getMxordn());

        this.ctx.setOdeSystem(system);

        return true;
    }

    /**
     * Initializes and prepares an LSODA solver context for integration. 
     * 
     * <p>This function allocates and initializes all memory required for solving a system of ODEs
     * using the LSODA method. It also validates and sets up default solver options.
     *
     * It must be called before calling {@code solve()} for the first time with a given {@code SBMLInterpreter} interpreter.</p>
     * 
     * @param system the {@code DESystem} object, will be mostly {@code SBMLInterpreter} object
     * @param ixpr   1 to print messages, 
     *               0 to not print messages
     * @param itask  controls task performed by the solver. 
     * @param state  tells the solver how to proceed
     * @param mxstep the maximum number of steps allowed
     * 
     * @return {@code bool} Returns true on successful preparation
     * @throws IllegalInputException 
     */
    @Override
    public boolean prepare(DESystem system, int ixpr, int itask, int state, int mxstep) {
        this.ctx.setNeq(system.getDimension());
        LSODAOptions opt = this.ctx.getOpt();
        opt.setMxstep(mxstep);
        if((opt.getAtol().length == 1 || opt.getRtol().length ==1) && this.ctx.getNeq() != 1) {

            double[] atol = new double[ctx.getNeq()];
            double[] rtol = new double[ctx.getNeq()];
            double abstol = opt.getAtol()[0];
            double relTol = opt.getRtol()[0];
            for(int i=0; i<this.ctx.getNeq(); i++) {
                atol[i] = abstol;
                rtol[i] = relTol;
            }

            opt.setAtol(atol);
            opt.setRtol(rtol);
        }

        if(!checkOpt(this.ctx, opt)) {
            return false;
        }

        allocMemory(this.ctx, opt.getMxords(), opt.getMxordn());

        this.ctx.setOdeSystem(system);

        return true;
    }

    public void lsodaReset(LSODAContext ctx) {
        ctx.setState(1);
        ctx.setError(null);
    }

    /**
     * Frees the memory of {@Lcode SODAContext}
     * <p>JVM manages on its own</p>
     * @param ctx
     */
    public void lsodaFree(LSODAContext ctx) {

        // JVM manages memory for us.

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
                pint = pc[1];
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

                newTesco[nq][2] = ragq;
                
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
                double[] acor = common.getAcor();
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getYh()[1][i] + common.getEl()[1] * common.getSavf()[i];
                    acor[i] = common.getSavf()[i];
                }
                common.setAcor(acor);
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
            throw new IllegalStateException("[solsy] solsy called with miter != 2: miter = " + common.getMiter());
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
                    throw new IllegalArgumentException("[dgesl] Matrix is singular or nearly singular at index " + k);
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
     
            daxpy(n - k, t, a[k], 1, b, 1, k + 1, k + 1);
        }

          for (k = n; k >= 1; k--) {
            b[k] = b[k] / a[k][k];
            t = -b[k];
        
            daxpy(k-1, t, a[k], 1, b, 1, 1, 1);
            
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
     *      <li>For any other cases, a default iteration over the vector elements is used with <code>offsetX</code> and <code>offsetY</code>
     *          as starting iteration index in <code>dx</code> and <code>dy</code> respectively.</li>
     *  </ul>
     * </p>
     * 
     * 
     * @param n         The number of elements in the vectors to be processed;
     *                  if <code>n</code> is less than 0, no operation will be completed.
     * @param da        The scalar multiplier applied to each element of <code>dx</code>.
     * @param dx        The vector whose elements are scaled by <code>da</code> and then added to the corresponding elements in <code>dy</code>.
     * @param incx      The increment for accessing successive elements of <code>dx</code>.
     * @param dy        The destination vector that is updated in place with the result of the AXPY operation.
     * @param incy      The increment for accessing successive elements of <code>dy</code>.
     * @param offsetX   The start position for <code>dx</code>
     * @param offsetY   The start position for <code>dy</code>
    */
    public static void daxpy(int n, double da, double[] dx, int incx, double[] dy, int incy, int offsetX, int offsetY) {
        int i, ix, iy, m;

        if (n < 0 || da == 0d || (incx * (n - 1) + offsetX) >= dx.length || (incy * (n - 1) + offsetY) >= dy.length) {
            return;
        }
        
        if (incx != incy || incy < 1) {
            ix = offsetX;
            iy = offsetY;
            if (incx < 0) {
                ix = (-n + 1) * incx + offsetX;
            }
            if (incy < 0) {
                iy = (-n + 1) * incy + offsetY;
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
                    dy[i + offsetY - 1] += da * dx[i + offsetX - 1];
                }
                if (n < 4) {
                    return;
                }
            }
            for (i = m + 1; i <= n; i += 4) {
                dy[i + offsetY - 1] += da * dx[i + offsetX - 1];
                dy[i + offsetY] += da * dx[i + offsetX];
                dy[i + offsetY + 1] += da * dx[i + offsetX + 1];
                dy[i + offsetY + 2] += da * dx[i + offsetX + 2];
            }
            return;
        }

        for (i = 1; i <= n + incx; i += incx) {
            dy[i + offsetY - 1] += da * dx[i + offsetX - 1];
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
            throw new IndexOutOfBoundsException("[ddot] Array size too small for given incx and incy");
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
     * difference matrix <code>Wm</code>. The matrix is then modified by unitizing its diagonal elements and factorized 
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
     *              and ODE function evaluation informationthe LSODA context containing solver state, common data, tolerances (abosulte and relative),
     *              and ODE function evaluation information
     * @param y     the state vector of the ODE system that is used to compute the finite differences for Jacobian approximation
     * 
     * @return      1: if the Jacobian approximation and subsequent matrix factorization succeed;
     *              2: 0 if an error occurs (e.g., an unexpected iteration method)
     */
    public static int prja(LSODAContext ctx, double[] y) throws DerivativeException{
        int i, j; 
        int[] ier = new int[1]; 
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
                    wm[i][j] = (common.getAcor()[i] - common.getSavf()[i]) * fac; // -h*el[1]*J
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
            common.setWm(wm); // I - h*el[1]*J

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
            
            double[] temp1 = Arrays.copyOfRange(a[k], k-1, n+1);

            j = idamax(n - k + 1, temp1, 1) + k - 1;
            ipvt[k] = j;


            
            if (a[k][j] == 0d) {
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

            System.arraycopy(temp2, 1, a[k], k + 1, n - k);
            
            for (i = k + 1; i <= n; i++) {
                t = a[i][j];
                if (j != k) {
                    a[i][j] = a[i][k];
                    a[i][k] = t;
                }
            
                daxpy(n - k, t, a[k], 1, a[i], 1, k + 1, k + 1);
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

   /**
    * handles switching between nonstiff (Adams) and stiff (BDF) methods
    * <p>
    * Compares estimated performance and stability of the current method <code>meth</code> with the alternative method.
    * </p>
    * @param ctx    the LSODA context containing solver state, common data, options data and number of equations
    * @param dsm    normalized local-error estimate at current order
    * @param pnorm  predicted error norm
    * @param rh     output: the factor by which to multiply the current stepsize <code>h</code>
    */
    public static void methodSwitch(LSODAContext ctx, double dsm, double pnorm, double[] rh) {

        // cm1 and cm2 are precomputed step-size multipliers associated with going one order up or down in the current method family (Adams or BDF)
        double[] cm1 = {
            0x0p+0, 0x1p+1, 0x1.7ffffffffffffp+2, 0x1p+2, 
            0x1.9435e50d79434p+0, 0x1.c71c71c71c721p-2, 0x1.8eaf0473189ecp-4, 0x1.1df9ab7934513p-6, 
            0x1.5b6f81b154515p-9, 0x1.6e1dd3d149b81p-12, 0x1.54a9415f71629p-15, 0x1.1bcb8f930a98p-18, 
            0x1.ac0fa4b46f6c6p-22, };

        double[] cm2 = {
            0x0p+0, 0x1p+1, 0x1.8p+0, 0x1.5555555555556p-1, 
            0x1.aaaaaaaaaaaacp-3, 0x1.9999999999999p-5, 0x1.8eaf0473189ecp-4, 0x1.1df9ab7934513p-6, 
            0x1.5b6f81b154515p-9, 0x1.6e1dd3d149b81p-12, 0x1.54a9415f71629p-15, 0x1.1bcb8f930a98p-18, 
            0x1.ac0fa4b46f6c6p-22, };
            
        int lm1, lm1p1, lm2, lm2p1, nqm1, nqm2;
        double rh1, rh2, rh1it, exm2, dm2, exm1, dm1, alpha, exsm;
        double pdh;
        int neq = ctx.getNeq();
        int mxordn = ctx.getOpt().getMxordn();
        int mxords = ctx.getOpt().getMxords();
        LSODACommon common = ctx.getCommon();

        // currently using Adams method, considering switching to BDF 
        if(common.getMeth() == 1) {
            if(common.getNq() > 5) {
                return;
            }

            if(dsm <= (100d * pnorm * common.ETA) || common.getPdest() == 0d) {         // guard against polluted error estimates
                if(common.getIrflag() == 0) {
                    return;
                }

                rh2 = 2d;
                nqm2 = (int) common.min(common.getNq(), mxords);
            }
            else {
                
                // compute ideal stepsize factor for Adams method, rh1
                exsm = 1d / (double) (common.getNq() + 1);
                rh1 = 1d / (1.2 * Math.pow(dsm, exsm) + 0.0000012d);

                // guard against roundoff errors
                rh1it = 2d * rh1;
                pdh = common.getPdlast() * Math.abs(common.getH());
                if((pdh * rh1) > 0.00001d) {
                    rh1it = common.getSM1()[common.getNq()] / pdh;
                }
                rh1 = common.min(rh1, rh1it);

                // compute stepsize factor for BDF method, rh2
                if(common.getNq() > mxords) {
                    nqm2 = mxords;
                    lm2 = mxords + 1;
                    exm2 = 1d / (double) lm2;
                    lm2p1 = lm2 + 1;
                    dm2 = vmnorm(neq, common.getYh()[lm2p1], common.getEwt()) / cm2[mxords];
                    rh2 = 1d / (1.2d * Math.pow(dm2, exm2) + 0.0000012d);
                } 
                else {
                    dm2 = dsm * (cm1[common.getNq()] / cm2[common.getNq()]);
                    rh2 = 1d / (1.2d * Math.pow(dm2, exsm) + 0.0000012d);
                    nqm2 = common.getNq();
                }

                // switch only when new stepsize factor is more than ratio = 5 times the previous stepsize factor
                if(rh2 < (common.RATIO * rh1) ) {
                    return;
                }
            }

            // method switch test passed. Reset relevant quantities for BDF
            rh[0] = rh2;
            common.setIcount(20);
            common.setMeth(2);
            common.setMiter(2);
            common.setPdlast(0d);
            common.setNq(nqm2);
            return;
        }

        // currently using BDF method, considering switching to Adams 
        exsm = 1d / (double) (common.getNq() + 1);
        if(mxordn < common.getNq()) {
            nqm1 = mxordn;
            lm1 = mxordn + 1;
            exm1 = 1d / (double) lm1;
            lm1p1 = lm1 + 1;
            dm1 = vmnorm(neq, common.getYh()[lm1p1], common.getEwt()) / cm1[mxordn];
            rh1 = 1d / (1.2d * Math.pow(dm1, exm1) + 0.0000012d);
        } 
        else {
            dm1 = dsm * (cm2[common.getNq()] / cm1[common.getNq()]);
            rh1 = 1d / (1.2d * Math.pow(dm1, exsm) + 0.0000012d);
            nqm1 = common.getNq();
            exm1 = exsm;
        }

        // guard against roundoff errors
        rh1it = 2d * rh1;
        pdh = common.getPdnorm() * Math.abs(common.getH());
        if((pdh * rh1) > 0.00001d) {
            rh1it = common.getSM1()[nqm1] / pdh;
        }
        rh1 = common.min(rh1, rh1it);

        rh2 = 1d / (1.2d * Math.pow(dsm, exsm) + 0.0000012d);

        // switch only when new stepsize factor is more than previous stepsize factor
        if((rh1 * common.RATIO) < (5d * rh2)) {
            return;
        }

        alpha = common.max(0.001d, rh1);
        dm1 *= Math.pow(alpha, exm1);
        if(dm1 <= 1000. * common.ETA * pnorm) {
            return;
        }
            
        // method switch test passed. Reset relevant quantities for Adams
        rh[0] = rh1;
        common.setIcount(20);
        common.setMeth(1);
        common.setMiter(0);
        common.setPdlast(0d);
        common.setNq(nqm1);    
    }

    /**
     * detects whether to change the order of current method
     * <p>
     * This function is called by <code>stoda</code> after each successful or failed
     * step to evaluate whether to adjust the multistep method's order (nq) and/or the 
     * integration step size (h) based on local error estimates, stored derivative history, 
     * and stability constraints.
     * </p>
     * <p>
     * It computes three candidate step-size factors:
     *       <ul><li><code>rhdn</code> : for decreasing order (nq - 1)</li>
     *       <li><code>rhsm</code> : for keeping the same order (nq)</li>
     *       <li><code>rhup</code> : for increasing order (nq + 1)</li></ul>
     * and then chooses the order yielding the largest stable step size.
     * If order is increased, the corresponding additional backward difference
     *      <code>yh[nq+1]</code> is computed using the most recent corrector vector, <code>acor</code>.
     * </p>
     * @param ctx       the LSODA context containing solver state, common data, options data and number of equations
     * @param rhup      ideal stepsize factor, if one step up in order
     * @param dsm       normalized local-error estimate at current order
     * @param rh        output: the factor by which to multiply the current stepsize <code>h</code>
     * @param kflag     flag from previous step, negative if prior error test or correction failure
     * @param maxord    maximum allowed order in current family   
     * @return          0: Keep the same stepsize and order
     *                  1: Change in stepsize but same order
     *                  2: Change in both stepsize and order
     */
    public static int orderSwitch(LSODAContext ctx, double rhup, double dsm, double[] rh, int kflag, int maxord) {
        int newq, i;
        double exsm, rhdn, rhsm, ddn, exdn, r;
        double pdh = 0;

        LSODACommon common = ctx.getCommon();
        int neq = ctx.getNeq();

        // calculate rh-same factor, for staying at current order nq 
        exsm = 1d / (double) (common.getNq() + 1);
        rhsm = 1d / (1.2d * Math.pow(dsm, exsm) + 0.0000012d);

        // calculate rh-down factor, for dropping to order nq-1
        rhdn = 0d;
        if(common.getNq() != 1) {
            ddn = vmnorm(neq, common.getYh()[(common.getNq() + 1)], common.getEwt()) / common.getTesco()[common.getNq()][1];
            exdn = 1d / (double) common.getNq();
            rhdn = 1d / (1.3d * Math.pow(ddn, exdn) + 0.0000013d);
        }

        // caps for stability region restrictions when using the Adams method.
        if(common.getMeth() == 1) {
            pdh = common.max(Math.abs(common.getH()) * common.getPdlast(), 0.000001d);
            if(common.getNq() < maxord) {
                rhup = common.min(rhup, common.getSM1()[(common.getNq() + 1)] / pdh);
            }
            rhsm = common.min(rhsm, common.getSM1()[common.getNq()] / pdh);
            if(common.getNq() > 1) {
                rhdn = common.min(rhdn, common.getSM1()[common.getNq() - 1] / pdh);
            }
            common.setPdest(0d);;
        }

        if(rhsm >= rhup) {
            if(rhsm >= rhdn) {                  // stay at same order
                newq = common.getNq();
                rh[0] = rhsm;
            } 
            else {                              // drop one order
                newq = common.getNq() - 1;
                rh[0] = rhdn;
                if(kflag < 0 && rh[0] > 1d)     // failure detected, reset rh to 1
                    rh[0] = 1d;
            }
        } 
        else {
            if(rhup <= rhdn) {                  // drop one order
                newq = common.getNq() - 1;
                rh[0] = rhdn;
                if(kflag < 0 && rh[0] > 1.)     // failure detected, reset rh to 1
                    rh[0] = 1d;
            } 
            else {                              // propose order increment
                rh[0] = rhup;
                if(rh[0] >= 1.1d) {             // increase order by one, 10% minimum threshold to allow order increase satisfied (rhup >= 1.1).
                    r = common.getEl()[(common.getNq() + 1)] / (double) (common.getNq() + 1);
                    common.setNq(common.getNq() + 1);
                    double[][] tempYh = common.getYh();
                    for(i = 1; i <= neq; i++) {
                        tempYh[common.getNq() + 1][i] = common.getAcor()[i] * r;
                    }
                    common.setYh(tempYh);
                    return 2;              
                } 
                else {                          // stepsize factor too small, no need to increase order
                    common.setIalth(3);
                    return 0;
                }
            }
        }

        if(common.getMeth() == 1) {
            if((rh[0] * pdh * 1.00001d) < common.getSM1()[newq])
                if(kflag == 0 && rh[0] < 1.1) {
                    common.setIalth(3);;
                    return 0;
                }
        } 
        else {
            if(kflag == 0 && rh[0] < 1.1) {
                common.setIalth(3);;
                return 0;
            }
        }

        // in case of several failures, limit maximum stepsize growth to 0.2
        if(kflag <= -2) {
            rh[0] = common.min(rh[0], 0.2);
        }

        if(newq == common.getNq()) {
            return 1;
        }

        common.setNq(newq);
        return 2;
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

    /**
     * Main lsoda driver function for integrating a system of ODEs
     * 
     * This function numerically solves an initial value problem (IVP) of the form:
     * <pre>
     *     dy/dt = f(t, y)
     * </pre>
     * for a given initial condition {@code y(t0) = y0}, from time {@code t} towards {@code tout}, using the
     * LSODA algorithm.
     * @param ctx  {@code LSODAContext} object containing the problem setup, memory workspaces, and ODE system definition.
     * @param y    the array of dependent variables of size `neq`.
     * @param t    array of unit size containing the current timestep
     * @param tout the target output time for the integration.
     * @return int Status code (same as {@code ctx->state}):
     *   <ul><li> 2: Success. Result found at time {@code tout}. </li></ul>
     *   Negative values indicate failure:
     *   <ul><li> -1: Too much work attempted.</li>
     *       <li> -2: Too much accuracy requested (rtol/atol too small).</li>
     *       <li> -3: Illegal input detected.</li>
     *       <li> -4: Repeated local error test failures.</li>
     *       <li> -5: Repeated convergence failures.</li>
     *       <li> -6: Error weight {@code ewt[i]} became zero.</li>
     *       <li> -7: Insufficient workspace.</li></ul>
     *
     * @throws DerivativeException
     */
    public int lsoda(LSODAContext ctx, double[] y, double[] t, double tout) throws DerivativeException {

        int kflag = 0;
        int jstart = 0;

        LSODACommon common = ctx.getCommon();
        LSODAOptions opt = ctx.getOpt(); 
        DESystem odeSystem = ctx.getOdeSystem();
        LSODAStepper stepper = new LSODAStepper();

        if (common == null) {
            hardFailure(ctx);
            throw new MembersNotInitializedException();
        }
        
        int i;
        final int neq = ctx.getNeq();
        double big, hmx, rh, tcrit = 0, tdist, tnext, tol, tolsf, tp, size, sum, w0;
        hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
        boolean ihit = false;
        double h0 = opt.getH0();

        yOffset = new double[neq + 1];
        System.arraycopy(y, 0, yOffset, 1, y.length);

        /*
         * Block a.
         */


        /*
         * Block b.
         */

        if (ctx.getState() == 1 || ctx.getState() == 3) {
            h0 = opt.getH0();
            if (ctx.getState() == 1) {
                if ((tout - t[0]) * h0 < 0.) {
                    hardFailure(ctx);
                    throw new IllegalTException(illegalTExceptionCases.toutBehindT, t[0], tout, h0);
                }
            }
        }

        final int itask = opt.getItask(); 

        if (ctx.getState() == 3) {
            jstart = -1;
        }


        /*
         * Block c.
         */

        double[] rtol = new double[neq + 1];
        double[] atol = new double[neq + 1];
        System.arraycopy(opt.getRtol(), 0, rtol, 1, neq);
        System.arraycopy(opt.getAtol(), 0, atol, 1, neq);

        if (ctx.getState() == 1) {
            common.setMeth(1); 
            common.setTn(t[0]);
            common.setTsw(t[0]);
            if (itask == 4 || itask == 5) {
                tcrit = opt.getTcrit();
                if ((tcrit - tout) * (tout - t[0]) < 0.) {
                    hardFailure(ctx);
                    throw new IllegalTException(illegalTExceptionCases.tcritBehindTout, t[0], tout, tcrit);
                }
                if (h0 != 0. && (t[0] + h0 - tcrit) * h0 > 0.) {
                    h0 = tcrit - t[0];
                }
            }
            jstart = 0;
            common.setNhnil(0);
            common.setNst(0);
            common.setNje(0);
            common.setNslast(0);
            common.setHu(0);
            common.setNqu(0);
            common.setMused(0);
            common.setMiter(0);

            double[][] newYh = common.getYh();
            newYh[2] = findDerivatives(odeSystem, t[0], yOffset);
            common.setYh(newYh);

            common.setNfe(1);

            newYh = common.getYh();
            for (int k = 1; k <= neq; k++) {
                newYh[1][k] = yOffset[k];
            }
            common.setYh(newYh);

            common.setNq(1);
            common.setH(1);

            ewset(yOffset, rtol, atol, neq, common);

            for (i = 1; i <= neq; i++) {
                if (common.getEwt()[i] <= 0.) {
                    hardFailure(ctx);
                    throw new ErrorWeightException(i, common.getEwt()[i]);
                }
            }

            if (h0 == 0d) {
                tdist = Math.abs(tout - t[0]);
                w0 = Math.max(Math.abs(t[0]), Math.abs(tout));
                if (tdist < 2d * common.ETA * w0) { 
                    hardFailure(ctx);
                    throw new IllegalTException(illegalTExceptionCases.toutToCloseToT, t[0], tout, tdist);
                }
                tol = 0d;
                for (i = 1; i <= neq; i++) {
                    tol = Math.max(tol, rtol[i]);
                }
                if (tol <= 0d) {
                    for (i = 1; i <= neq; i++) {
                        double atoli = atol[i];
                        double ayi = Math.abs(yOffset[i]);
                        if (ayi != 0d) {
                            tol = Math.max(tol, atoli / ayi);
                        }
                    }
                }
                tol = Math.max(tol, 100d * common.ETA);
                tol = Math.min(tol, 0.001d);
                sum = vmnorm(neq, common.getYh()[2], common.getEwt());
                sum = 1d / (tol * w0 * w0) + tol * sum * sum;
                h0 = 1d / Math.sqrt(sum);
                h0 = Math.min(h0, tdist);
                if(tout - t[0] < 0d) {
                    h0 *= -1d;
                }
            }

            rh = Math.abs(h0) * opt.getHmxi();
            if (rh > 1.) {
                h0 /= rh;
            }

            common.setH(h0);
            newYh = common.getYh();
            for (i = 1; i <= neq; i++) {
                newYh[2][i] *= h0;
            }
            common.setYh(newYh);
        }


        /*
         * Block d.
         */

        if (ctx.getState() == 2 || ctx.getState() == 3) {
            jstart = 1;
            ctx.nslast = common.getNst();

            switch (itask) {
                case 1:
                    if ((common.getTn() - tout) * common.getH() >= 0d) {
                        return intdyReturn(ctx, yOffset, t, tout, itask);
                    }
                    break;

                case 2:
                    break; 
                
                case 3:
                    tp = common.getTn() - common.getHu() * (1d + 100d * common.ETA);
                    if ((tp - tout) * common.getH() > 0d) {
                        hardFailure(ctx);
                        throw new IllegalTException(illegalTExceptionCases.toutBehindTcurMinusHu, t[0], tout, itask);
                    }
                    if ((common.getTn() - tout) * common.getH() < 0d) {
                        break;
                    }
                    return successReturn(ctx, yOffset, t, itask, ihit);
                        
                case 4:
                    tcrit = opt.getTcrit();
                    if((common.getTn() - tcrit) * common.getH() > 0d) {
                        hardFailure(ctx);
                        throw new IllegalTException(illegalTExceptionCases.tcritBehindTcur, t[0], tout, null);
                    }
                    if((tcrit - tout) * common.getH() < 0d) {
                        hardFailure(ctx);
                        throw new IllegalTException(illegalTExceptionCases.tcritBehindTout, t[0], tout, null);
                    }
                    if((common.getTn() - tout) * common.getH() >= 0d) {
                        return intdyReturn(ctx, yOffset, t, tout, itask);
                    }
                    break;
                    
                case 5:
                    if(itask == 5) {
                        tcrit = opt.getTcrit();
                        if ((common.getTn() - tcrit) * common.getH() > 0d) {
                            hardFailure(ctx);
                            throw new IllegalTException(illegalTExceptionCases.tcritBehindTcur, t[0], tout, null);
                        }
                    }
                        
                        
                    hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
                    ihit = Math.abs(common.getTn() - tcrit) <= (100d * common.ETA * hmx);

                    if (ihit) {
                        t[0] = tcrit;
                        return successReturn(ctx, yOffset, t, itask, ihit);
                    }
                    tnext = common.getTn() + common.getH() * (1d + 4d * common.ETA);
                    if ((tnext - tcrit) * common.getH() <= 0d) {
                        break;
                    }
                    common.setH((tcrit - common.getTn()) * (1d - 4d * common.ETA));
                    if (ctx.getState() == 2) {
                        jstart = -2;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Invalid itask value: " + itask);
            }
        }


        /*
         * Block e.
         */

        while (true) {
            if (ctx.getState() != 1 || common.getNst() != 0) {
                if ((common.getNst() - common.getNslast()) >= opt.getMxstep()) {
                    softFailure(ctx, -1, t);
                    throw new MaxStepExceededException(opt.getMxstep());
                }

                ewset(common.getYh()[1], rtol, atol, neq, common);
                for (int j = 1; j <= neq; j++) {
                    if (common.getEwt()[j] <= 0d) {
                        softFailure(ctx, -6, t);
                        throw new ErrorWeightException(j, common.getEwt()[j]);
                    }
                }
            }
            tolsf = common.ETA * vmnorm(neq, common.getYh()[1], common.getEwt());
            if (tolsf > 0.01d) {
                tolsf = tolsf * 200d;
                if (common.getNst() == 0) {
                    hardFailure(ctx);
                    throw new TooMuchAccuracyException(tolsf);
                }
                softFailure(ctx, -2, t);
                throw new TooMuchAccuracyException(t[0], tolsf);
            }

            if ((common.getTn() + common.getH()) == common.getTn()) {
                common.setNhnil(common.getNhnil() + 1);
                if (common.getNhnil() <= opt.getMxhnil()) {
                    logError(String.format("lsoda -- warning..internal t = %f and h = %f are\n", common.getTn(), common.getH()));
                    logError(String.format("such that in the machine, t + %f = t on the next step\n", common.getH()));
                    logError("solver will continue anyway.\n");
                    if (common.getNhnil() == opt.getMxhnil()) {
                        logError(String.format("lsoda -- above warning has been issued %d times,\n", common.getNhnil()));
                        logError("it will not be issued again for this problem\n");
                    }
                }
            }

            kflag = stepper.stoda(ctx, yOffset, jstart);
            if (kflag == 0) {

                /*
                 * Block f.
                 */

                jstart = 1;

                if (common.getMeth() != common.getMused()) {
                    common.setTsw(common.getTn());
                    jstart = -1;

                    if (opt.getIxpr() != 0) {
                        if (common.getMeth() == 2) {
                            System.out.print("[lsoda] a swith to the stiff method has occured ");
                        }
                        if (common.getMeth() == 1) {
                            System.out.print("[lsoda] a switch to the non-stiff method has occured ");
                        }
                        System.out.printf("at t = %f, tentative step size h = %f, step nst = %d\n", common.getTn(), common.getH(), common.getNst());
                    } 
                }

                if(itask == 1) {
                    if ((common.getTn() - tout) * common.getH() < 0d) {
                        continue;
                    }
                    return intdyReturn(ctx, yOffset, t, tout, itask);
                }

                if(itask == 2) {
                    return successReturn(ctx, yOffset, t, itask, ihit);
                }

                if(itask == 3) {
                    if ((common.getTn() - tout) * common.getH() >= 0d) {
                        return successReturn(ctx, yOffset, t, itask, ihit);
                    }
                    continue; 
                }
                    
                if(itask == 4) {
                    tcrit = opt.getTcrit();
                    if ((common.getTn() - tout) * common.getH() >= 0d) {
                        return intdyReturn(ctx, yOffset, t, tout, itask);
                    } 
                    else {
                        hmx = Math.abs(common.getTn()) + Math.abs(common.getH());
                        ihit = Math.abs(common.getTn() - tcrit) <= (100d * common.ETA * hmx);
                        if (ihit) {
                            return successReturn(ctx, yOffset, t, itask, ihit);
                        }
                        tnext = common.getTn() + common.getH() * (1d + 4d + common.ETA);
                        if ((tnext - tcrit) * common.getH() <= 0d) {
                            continue;
                        }
                        common.setH((tcrit - common.getTn()) * (1d - 4d * common.ETA));
                        jstart = -2;
                        continue;
                    }
                }

                if(itask == 5) {
                    tcrit = opt.getTcrit();
                    hmx = Math.abs(common.getTn() + Math.abs(common.getH()));
                    ihit = Math.abs(common.getTn() - tcrit) <= (100d * common.ETA * hmx);
                    return successReturn(ctx, yOffset, t, itask, ihit);
                }
            }

            if (kflag == -1 || kflag == -2) {
                big = 0d;
                common.setIxmer(1);

                for (i = 0; i <= neq; i++) {
                    size = Math.abs(common.getAcor()[i] * common.getEwt()[i]);
                    if (big < size) {
                        big = size;
                        common.setIxmer(i);
                    }
                }
                if (kflag == -1) {
                    softFailure(ctx, -4, t);
                    throw new TestFailException(1, common.getTn(), common.getH());
                }
                if (kflag == -2) {
                    softFailure(ctx, -5, t);
                    throw new TestFailException(2, common.getTn(), common.getH());
                }
            }

        }
 
    }

    public LSODAContext getContext(){
        return this.ctx;
    }

    /**
     * Returns the result of integration
     * @return double array containing the result of the integration
     */
    public double[] getResult() {
        double[] result = new double[this.yOffset.length - 1];
        System.arraycopy(this.yOffset, 1, result, 0, yOffset.length - 1);
        return result;
    }

}