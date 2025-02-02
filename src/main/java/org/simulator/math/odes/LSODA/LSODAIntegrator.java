package org.simulator.math.odes.LSODA;
import java.util.Arrays;

import org.apache.commons.math.ode.DerivativeException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DESystem;

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
                y[i] = common.getYh()[i][i];
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

    private void ewset(double[] y, double[] rtol, double[] atol, int neq, LSODACommon common) {
        double[] ewt = common.getEwt();

        for (int i = 1; i <= neq; i++) {
            ewt[i] = rtol[i - 1] * Math.abs(y[i]) + atol[i - 1];
        }

        for (int i = 1; i <= neq; i++) {
            ewt[i] = 1d / ewt[i];
        }

        common.setEwt(ewt);
    }

    private int stoda(LSODAContext ctx, double[]y, int jstart) {
        return 0; //implement stoda.c function, "stoda.c" in source code
    }

    private double vmnorm(int n, double[] v, double[] w) {
        double vm = 0d;

        for (int i = 0; i <= n; i++) {
            vm = Math.max(vm, Math.abs(v[i]) * w[i]);
        }
        return vm;
    }

    public int lsoda(LSODAContext ctx, double[] y, double[] t, double tout) {
        int jstart;

        LSODACommon common = ctx.getCommon();
        LSODAOptions opt = ctx.getOpt(); 
        LSODAFunction function = ctx.getFunction();

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
                    hardFailure(ctx, opt.toString(), "[lsoda] itask = 4 or 5 and tcrit behind tout%s");
                }
                if (h0 != 0. && (t[0] + h0 - tcrit) * h0 > 0.) {
                    h0 = tcrit - t[0];
                }
            }
            jstart = 0;
            common.setNq(1);
            
            if (function != null) {
                function.evaluate(t[0], yOffset, yhOffset, ctx.getData());
            }
            common.setNfe(1);

            for (int k = 0; k <= ctx.getNeq(); k++) {
                common.getYh()[1][k] = y[k];
            }
            common.setNfe(1);

            ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);

            for (i = 1; i <= neq; i++) {
                if (common.getEwt()[i] <= 0.) {
                    hardFailure(ctx, opt.toString(), "[lsoda] ewt[%d] = %g <= 0.", i, common.getEwt());
                }
            }

            if (h0 == 0d) {
                tdist = Math.abs(tout - t[0]);
                w0 = Math.max(Math.abs(t[0]), Math.abs(tout));
                if (tdist < 2. * common.getEta() * w0) { 
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
                tol = Math.max(tol, 100. * common.getEta());
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

            int kflag = stoda(ctx, y, jstart);

            if (ctx.getState() == 2 || ctx.getState() == 3) {
                ctx.nslast = common.getNst();

                switch (itask) {
                    case 1:
                        if ((common.getTn() - tout) * common.getH() >= 0d) {
                            intdyReturn(y, t, tout, itask);
                        }
                        break;

                    case 2:
                        break; //nothing provided in source code
                    
                    case 3:
                        tp = common.getTn() - common.getHu() * (1.0 + 100.0 * common.getEta());
                        if ((tp - tout) * common.getH() > 0d) {
                            hardFailure(ctx, opt.toString(), "[lsoda] itask = " + itask + " and tout behind tcur - " + common.getHu());
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
                            intdyReturn(y, t, tout, itask);
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
                        softFailure(ctx, -1, "[lsoda] " + opt.getMxstep() + " steps taken before reaching tout");
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
                        hardFailure(ctx, "[lsoda] -- at start of problem, too much accuracy\n requested for precision of machine, \n suggested scalilng factor = \n" + tolsf);
                    }
                    softFailure(ctx, -2, "[lsoda] -- at t = " + t[0] + ", too much accurary requested\n          for precision of machine, suggested\n           scaling factor = " + tolsf);
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

                kflag = stoda(ctx, y, jstart);
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
                            intdyReturn(y, t, tout, itask);
                        
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
                                intdyReturn(y, t, tout, itask);
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



}   