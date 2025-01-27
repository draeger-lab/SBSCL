package org.simulator.math.odes.LSODA;
import java.util.Arrays;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.LSODA.LSODAOptions;

public class LSODAIntegrator extends AdaptiveStepsizeIntegrator {
    
    private int neq;
    private double[] yh;
    private double[] tn;
    private int state;
    
    public LSODAIntegrator(int neq) {
        super();
        this.neq = neq;
        this.yh = new double[neq + 1];
        this.state = 0;
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


    private void hardFailure(String message) {
        System.err.println(message);
        this.state = -3;
        throw new RuntimeException("LSODA hard failure: " + message);
    }

    private void softFailure(int code, String message, double[] y, double t) {
        System.err.println(message);
        for (int i = 1; i <= neq; i++) {
            y[i] = yh[i];
        }
        t = tn[0];
        this.state = code;
    }

    private int successReturn(double[] y, double[] t, int itask, double tcrit, boolean ihit) {
        for (int i = 1; i <= neq; i++) {
            y[i] = yh[i];
        }
        t[0] = tn[0];
        if((itask == 4 || itask == 5) && ihit) {
            t[0] = tcrit;
        }
        this.state = 2;
        return this.state;
    }

    private void logError(String message, Object... args) {
        System.err.printf(message, args);
    }

    private int intdy(double tout, int k, double[] y) {
        return 0; //i dont know what intdy is supposed to be
    }

    private int intdyReturn(double[] y, double[] t, double tout, int itask) {
        int iflag = intdy(tout, 0, y);
        if (iflag != 0) {
            logError("[lsoda] trouble from indty, itas = %d, tout = %g\n", itask, tout);
            for (int i = 1; i <= neq; i++) {
                y[i] = yh[i];
            }
        }
        t[0] = tout;
        this.state = 2;
        return this.state;
    }

    private boolean checkOpt(LSODAContext ctx, LSODAOptions opt) {
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
                double atoli = atol[i - 1];
                if (rtoli < 0d) {
                    logError("[lsoda] rtol = %g is less than 0.\n", rtoli);
                    return false;
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

    private boolean lsoda_prepare(LSODAContext ctx, LSODAOptions opt) {
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

    public LSODAContext lsodaCreateCtx() {
        LSODAContext ctx = new LSODAContext();
        ctx.setState(1);
        return ctx;
    }

    public LSODAOptions lsodaCreateOpt() {
        return new LSODAOptions();
    }

    private void ewset(double[] ycur, double[] rtol, double[] atol, int neq, LSODACommon common) {
        double[] ewt = common.getEwt();

        for (int i = 1; i <= neq; i++) {
            ewt[i] = rtol[i - 1] * Math.abs(ycur[i]) + atol[i - 1];
        }

        for (int i = 1; i <= neq; i++) {
            ewt[i] = 1d / ewt[i];
        }

        common.setEwt(ewt);
    }



    public int lsoda(LSODAContext ctx, double[] y, double[] t, double tout) {
        int kflag;
        int jstart;

        LSODACommon common = ctx.getCommon();
        LSODAOptions opt = ctx.getOpt(); 

        y = Arrays.copyOfRange(y, 1, y.length);
        
        int i = 0, ihit;
        final int neq = ctx.getNeq();
        double big, h0, hmx, rh, tcrit, tdist, tnext, tol, tolsf, tp, size, sum, w0;

        if (common == null) {
            // throw some error (which type???)
        }

        if (ctx.getState() == 1 || ctx.getState() == 3) {
            h0 = opt.getH0();
            if (ctx.getState() == 1) {
                if ((tout - t[0]) * h0 < 0.) {
                    //throw error (whicht type???)
                }
            }
        }

        final int itask = opt.getItask();

        if (ctx.getState() == 3) {
            jstart = -1;
        }

        final double[] rtol = Arrays.copyOfRange(opt.getRtol(), 1, opt.getRtol().length);
        final double[] atol = Arrays.copyOfRange(opt.getAtol(), 1, opt.getAtol().length);

        if (ctx.getState() == 1) {
            common.setMeth(1); // enum to define which method to use
            common.setTn(t[0]);
            common.setTsw(t[0]); //these three functions need to be defined when implementing common!
            if (itask == 4 || itask == 5) {
                tcrit = opt.getTcrit(); // need to define this method within LSODAOptions
                if ((tcrit - tout) * (tout - t[0]) < 0.) {
                    // throw some error again
                }
                if (h0 != 0. && (t[0] + h0 - tcrit) * h0 > 0.) {
                    h0 = tcrit - t[0];
                }
            }
            jstart = 0;
            common.setNq(1); //DONE: defined when implementing common
            
            ctx.getFunction() // define .getFuntion() within LSODAContext
                    .apply(t[0], Arrays.copyOfRange(y, 1, y.length),
                    common.getYh()[2], //DONE: will be defined when implementing "common"
                    ctx.getData()); //define .getData() within LSODAContext
            common.setNfe(1); //DONE: define when implementing common

            ewset(y); // implement ewset.c function; look for _C function in the original code

            for (i = 1; i <= neq; i++) {
                if (common.getEwt()[i] <= 0.) {
                    // throw error
                }
            }

            if (h0 == 0.) {
                tdist = Math.abs(tout - t[0]);
                w0 = Math.max(Math.abs(t[0]), Math.abs(tout));
                if (tdist < 2. * ETA * w0) { // ETA defined within "common"
                    // throw error
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
                tol = Math.max(tol, 100. * ETA); // ETA defined within "common"
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

            common.setH(h0);   //DONE: define when implementing common
            for (i = 1; i <= neq; i++) {
                common.getYh()[2][i] *= h0; //DONE: define when implementing common
            }

            if (ctx.getState() == 2 || ctx.getState() == 3) {
                ctx.nslast = ctx.nst; // probably to be defined in a different class

                switch (itask) {
                    case 1:
                        if ((ctx.tn - tout) * ctx.h >= 0d) {
                            intdyReturn(y, t, tout, itask);
                        }
                        break;

                    case 2:
                        break; //nothing provided in source code
                    
                    case 3:
                        double tp = ctx.tn - ctx.hu * (1.0 + 100.0 * ETA); //fill out later, figure out tp definitions
                        
                    case 4:
                        tcrit = opt.getTcrit();
                        if((ctx.tn[0] - tcrit) * ctx.h > 0d) {
                            hardFailure("[lsoda] itask = 4 or 5 and tcrit behind tcur\n");
                        }
                        if((tcrit - tout) * ctx.h < 0d) {
                            hardFailure("[lsoda] itask = 4 or 5 and tcrit behind tout\n");
                        }
                        if((ctx.tn[0] - tout) * ctx.h >= od) {
                            intdyReturn(y, t, tout, itask);
                        }
                        break;
                    
                    case 5:
                        if(itask == 5) {
                            tcrit = opt.getTcrit();
                            if ((ctx.tn[0] - tcrit) * ctx.h > 0d) {
                                hardFailure("[lsoda] itask = 4 or 5 and tcrit behind tcur\n");
                            }
                        }
                        
                        double hmx = Math.abs(ctx.tn[0]) + Math.abs(ctx.h); // finish later, figure out hmx definitions
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid itask value: " + itask);
                }
            }
        }
        return 0;

        
        
    }



}   