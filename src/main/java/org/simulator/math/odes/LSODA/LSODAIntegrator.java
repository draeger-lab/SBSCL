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
    
    private int intdy(LSODAContext ctx, double t, int k, double[] dky) {
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
            logError(String.format("intdy -- t = &g illegal. t not in interval tcur - %d to tcur", t, common.getHu()), ctx.getData());
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

    private int intdyReturn(LSODAContext ctx, double[] y, double[] t, double tout, int itask) {
        int iflag = intdy(ctx, tout, 0, y);
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

    public static double vmnorm(int n, double[] v, double[] w) {
        double vm = 0d;

        for (int i = 0; i <= n; i++) {
            vm = Math.max(vm, Math.abs(v[i]) * w[i]);
        }
        return vm;
    }

    public static void cfode(LSODAContext ctx, int meth) {
        LSODACommon common = ctx.getCommon();
        int i, nq, nqm1, nqp1;
        double agamq, fnq, fnqm1, pint, ragq, rqfac, rq1fac, tsign, xpin;
        double[] pc = new double[13];
        
        
        if (meth == 1) {
            double[][] newElco = common.getElco();
            newElco[1][1] = 1d;
            newElco[1][2] = 1d;
            common.setElco(newElco);

            double[][] newTesco = common.getTesco();
            newTesco[1][1] = 1d;
            newTesco[1][2] = 2d;
            newTesco[2][1] = 1d;
            newTesco[12][3] = 0d;
            common.setTesco(newTesco);
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
                newElco = common.getElco();
                newElco[nq][1] = pint * rq1fac;
                newElco[nq][2] = 1d;
                common.setElco(newElco);

                for (i = 2; i <= nq; i++) {
                    newElco = common.getElco();
                    newElco[nq][i+1] = rq1fac * pc[i] / (double) i;
                }
                agamq = rqfac * xpin;
                ragq = 1d / agamq;
                newTesco = common.getTesco();
                newTesco[nq][2] = agamq;
                common.setTesco(newTesco);
                if (nq < 12) {
                    newTesco[nqp1][1] = ragq * rqfac / (double) nqp1;
                    common.setTesco(newTesco);
                }
                newTesco[nqm1][3] = ragq;
                common.setTesco(newTesco);
            }
            return;
        }
        if (meth == 2) {
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
                newElco = common.getElco();
                newElco[nq][i] = pc[i] / pc[2];
                common.setElco(newElco);
            }
            newElco[nq][2] = 1d;
            common.setElco(newElco);
            newTesco[nq][1] = rq1fac;
            newTesco[nq][2] = ((double) nqp1) / common.getElco()[nq][1];
            newTesco[nq][3] = ((double) (nq + 2)) / common.getElco()[nq][1];
            common.setTesco(newTesco);
            rq1fac /= fnq;
        }
        }
    }

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
    
    /*
     * in the following function, y, del, delp and m were passed as pass-by-references in the original C source code. 
     * In order to implement this in Java, I changed these parameters to be passed as arrays, with the intended value stored within the first index
     * of the array, which can then be modified.
     */
    public int correction(LSODAContext ctx, double[] y, double pnorm, double[] del, double[] delp, double told, int[] m) {
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
        ctx.getFunction().evaluate(common.getTn(), y[1], common.getSavf(), ctx.getData());
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
                for (i = 1; i <= neq; i++) {
                    double[] acor = common.getAcor();
                    acor[i] = 0d;
                    common.setAcor(acor);
                }
            }
            if (common.getMiter() == 0) {
                for (i = 1; i <= neq; i++) {
                    double[] savf = common.getSavf();
                    savf[i] = common.getH() * common.getSavf()[i] - common.getYh()[2][i];
                    common.setSavf(savf);
                    y[i] = savf[i] - common.getAcor()[i];
                }
                del[0] = vmnorm(neq, y, common.getEwt());
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getYh()[1][i] + common.getEl()[1] * common.getSavf()[i];
                    double[] acor = common.getAcor();
                    acor[i] = common.getSavf()[i];
                    common.setAcor(acor);
                }
            }
            else {
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getH() * common.getSavf()[i] - (common.getYh()[2][i] + common.getAcor()[i]);
                }
                solsy(ctx, y);
                del[0] = vmnorm(neq, y, common.getEwt());
                for (i = 1; i <= neq; i++) {
                    double[] acor = common.getAcor();
                    acor[i] += y[i];
                    common.setAcor(acor);
                    y[i] = common.getYh()[1][i] + common.getEl()[1] * common.getAcor()[i];
                }
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
                rate = 0d;
                del[0] = 0d;
                for (i = 1; i <= neq; i++) {
                    y[i] = common.getYh()[1][i];
                }
                ctx.getFunction().evaluate(common.getTn(), y[1], common.getSavf(), ctx.getData());
                common.setNfe(common.getNfe() + 1);
            }
            else {
                delp[0] = del[0];
                ctx.getFunction().evaluate(common.getTn(), y[1], common.getSavf(), ctx.getData());
            }
        }
        return 0;
    }
    
    private int solsy(LSODAContext ctx, double[] y) {
        int neq = ctx.getNeq();
        LSODACommon common = ctx.getCommon();
        if (common.getMiter() != 2) {
            System.exit(0); //I chose to use System.exit() here, as the original C code uses the C equivalent abort()
        }
        if (common.getMiter() == 2) {
            dgesl(common.getWm(), neq, common.getIpvt(), y, 0);
        }
        return 1;
    }
    
    private int corfailure(LSODAContext ctx, double told) {
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

    private void dgesl(double[][] a, int n, int[] ipvt, double[] b, int job) {

        int k, j;
        double t;

        if (job == 0) {
            for (k = 1; k <= n; k++) {
                t = ddot(k-1, a[k], b, 1, 1);
                b[k] = (b[k] - t) / a[k][k];
            }

            for (k = n - 1; k >= 1; k--) { // C passes a reference of a, and then modifies the k-th index, s.t. the original a is also modified...TODO: find solution in Java
                b[k] = b[k] + ddot(n -  k, Arrays.copyOfRange(a[k], k, a[k].length), Arrays.copyOfRange(b, k, b.length), 1, 1); // arr + int???
                j = ipvt[k];
                if (j != k) {
                    t = b[j];
                    b[j] = b[k];
                    b[k] = t;
                }
            }
            return;
        }

        for (k = 1; k <= n - 1; k++) {
            j = ipvt[k];
            t = b[j];
            if (j != k) {
                b[j] = b[k];
                b[k] = t;
            }
            daxpy(n-k, t, a[k], 1, 1, b); // arr + int???
        }

        for (k = n; k >= 1; k--) {
            b[k] = b[k] / a[k][k];
            t = -b[k];
            daxpy(k-1, t, a[k], 1, 1, b); // arr + int???
        }
    }

    private void daxpy(int n, double da, double[] dx, int incx, int incy, double[] dy) {
        int i, ix, iy, m;

        if (n < 0 || da == 0d) {
            return;
        }

        if (incx != incy || incy < 1) {
            ix = 1;
            iy = 1;
            if (incy > 0) {
                ix = (-n + 1) * incy + 1;
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

    private double ddot(int n, double[] dx, double[] dy, int incx, int incy) {
        double dotprod;
        int ix, iy, i;

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

    private int prja(LSODAContext ctx, double[] y) {
        int i, j; 
        int[] ier = new int[1]; //in the original code, ier was of type int and passed by reference. I changed it to simulate this logic.
        double fac, hl0, r, r0, yj;
        LSODACommon common = ctx.getCommon();
        int neq = ctx.getNeq();

        common.setNje(common.getNje() + 1);
        hl0 = common.getH() * common.getEl()[1];

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
                ctx.getFunction().evaluate(common.getTn(), y[1], common.getAcor(), ctx.getData());
                for (i = 1; i <= neq; i++) {
                    double[][] wm = common.getWm();
                    wm[i][j] = (common.getAcor()[i] - common.getSavf()[i]) * fac;
                    common.setWm(wm);
                }
                y[j] = yj;
            }
            common.setNfe(common.getNfe() + neq);

            common.setPdnorm(fnorm(neq, common.getWm(), common.getEwt()) / Math.abs(hl0));
            
            for (i = 1; i <= neq; i++) {
                double[][] wm = common.getWm();
                wm[i][i] += 1d;
                common.setWm(wm);
            }
            
            double[][] wm = common.getWm(); //I defined a separate variable here because my compiler was 
            //throwing an error when I tried to use common.getWm() directly in the dgefa function call, saying common.getWm() was int[] and not double[][].
            dgefa(wm, neq, common.getIpvt(), ier);
            if (ier[0] != 0) {
                return 0;
            }
        }
        return 1;
    }

    private void dgefa(double[][] a, int n, int[] ipvt, int[] info) {
        int j, k, i;
        double t;

        info[0] = 0;
        for (k = 1; k <= n - 1; k++) {
            j = idamax(n- k +1, Arrays.copyOfRange(a[k], k, a[k].length), 1) + k - 1;
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
            dscal(n-k, t, 1, Arrays.copyOfRange(a[k], k, a[k].length));

            for (i = k + 1; i <= n; i++) {
                t = a[i][j];
                if (j != k) {
                    a[i][j] = a [i][k];
                    a[i][k] = t;
                }

                daxpy(n - k, t, Arrays.copyOfRange(a[k], k, a[k].length), 1, 1, Arrays.copyOfRange(a[i], k, a[i].length));
            }
        }

        ipvt[n] = n;
        if (a[n][n] == 0d) {
            info[0] = n;
        }

    }

    private double fnorm(int n, double[][] a, double[] w) {

        int i, j;
        double an, sum;
        double[] ap1;

        an = 0d;
        for (i = 1; i <= n; i++) {
            sum = 0d;
            ap1 = a[i];
            for (j = 1; j <= n; j++) {
                sum += Math.abs(ap1[j]) / w[j];
            }
            an = Math.max(an, sum * w[i]);
        }

        return an;
    }

    private int idamax(int n, double[] dx, int incx) {

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

    private void dscal(int n, double da, int incx, double[] dx) {
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

    public int lsoda(LSODAContext ctx, double[] y, double[] t, double tout) {
        int jstart;

        LSODACommon common = ctx.getCommon();
        LSODAOptions opt = ctx.getOpt(); 
        LSODAFunction function = ctx.getFunction();
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

            if (function != null) {
                function.evaluate(t[0], y[1], common.getYh()[3], ctx.getData());
            }
            common.setNfe(1);

            for (int k = 0; k <= ctx.getNeq(); k++) {
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



}   