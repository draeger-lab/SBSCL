package org.simulator.math.odes.LSODA;

import org.apache.commons.math.ode.DerivativeException;

public class LSODAStepper {
    private LSODAContext ctx;
    private LSODACommon common;
    private double[] y;
    private int jstart;

    public LSODAStepper(LSODAContext ctx, double[] y, int jstart) {
        this.ctx = ctx;
        this.common = ctx.getCommon();
    }

    private void endStoda() {
        double r = 1d / common.getTesco()[common.getNqu()][2];
        double[] acor = common.getAcor();

        for (int i = 1; i <= ctx.getNeq(); i++) {
            acor[i] = acor[i] * r;
        }
        common.setAcor(acor);

        common.setHold(common.getH());
    }

    private void resetCoeff() {
        double el0 = common.getEl()[1];
        double[] newEl = common.getEl();
        for (int i = 1; i <= (common.getNq() + 1); i++) {
            newEl[i] = common.getElco()[common.getNq()][i];
        }
        common.setEl(newEl);
        double rc = common.getRc() * newEl[1] / el0;
        common.setRc(rc);
    }

    public int stoda(LSODAContext ctx, double[]y, int jstart) throws DerivativeException{
        int kflag;
        int[] m = new int[1];
        double[] del = new double[1];
        double[] delp = new double[1];
        double rh = 1d;
        double dsm, dup, exup, r, told;
        double pnorm;

        double hmin = ctx.getOpt().getHmin();
        int mxords = ctx.getOpt().getMxords();
        int mxordn = ctx.getOpt().getMxordn();
        int neq = ctx.getNeq();


        kflag = 0;
        told = common.getTn();
        common.setNcf(0);
        delp[0] = 0d;

        int maxord = mxordn;
        if (common.getMeth() == 2) {
            maxord = mxords;
        }
        if (jstart == 0) {
            common.setNq(1);
            common.setIalth(2);
            common.setRmax(10000d);
            common.setRc(0d);
            common.setCrate(0.7);
            common.setHold(common.getH());
            common.setNslp(0);
            common.setIpup(common.getMiter());
            double[] newEl = common.getEl();
            newEl[1] = 1d;
            common.setEl(newEl);

            // initialize switching parameters
            common.setIcount(20);
            common.setIrflag(0);
            common.setPdest(0d);
            common.setPdlast(0d);

            LSODAIntegrator.cfode(ctx, 1);
            resetCoeff();
        }

        if (jstart == -1) {
            common.setIpup(common.getMiter());
            if (common.getIalth() == 1) {
                common.setIalth(2);
            }
            if (common.getMeth() != common.getMused()) {
                LSODAIntegrator.cfode(ctx, common.getMeth());
                common.setIalth((common.getNq() + 1));
                resetCoeff();
            }
            if (common.getH() != common.getHold()) {
                rh = common.getH() / common.getHold();
                common.setH(common.getHold());
                LSODAIntegrator.scaleh(ctx, rh);
            }
        }

        if (jstart == -2) {
            if (common.getH() != common.getHold()) {
                rh = common.getH() / common.getHold();
                common.setH(common.getHold());
                LSODAIntegrator.scaleh(ctx, rh);
            }
        }

        dsm = 0d;
        while (true) {
            common.setJcur(0);

            while (true) {
                if (Math.abs(common.getRc() - 1d) > common.CCMAX) { //what is CCMAX???
                    common.setIpup(common.getMiter());
                }
                if (common.getNst() >= common.getNslp() + common.MSBP) {
                    common.setIpup(common.getMiter());
                }
                common.setTn(common.getTn() + common.getH());

                for (int j = common.getNq(); j >= 1; j--) {
                    for (int i1 = j; i1 <= common.getNq(); i1++) {
                        double[][] newYh = common.getYh();
                        for (int i = 1; i <= neq; i++) {
                            newYh[i1][i] = newYh[i1 + 1][i];
                        }
                        common.setYh(newYh);
                    }
                }

                pnorm = LSODAIntegrator.vmnorm(neq, common.getYh()[1], common.getEwt()); // fix vnorm definitions
                
                int corflag = LSODAIntegrator.correction(ctx, y, pnorm, del, delp, told, m); 

                if (corflag == 0) {
                    break;
                }
                if (corflag == 1) {
                    rh = Math.max(0.25, hmin / Math.abs(common.getH()));
                    LSODAIntegrator.scaleh(ctx, rh);
                    continue;
                }
                if (corflag == 2) {
                    kflag = -2;
                    common.setHold(common.getH());
                    jstart = 1;
                    return kflag;
                }
            }
            if (m[0] == 0) {
                dsm = del[0] / common.getTesco()[common.getNq()][2];
            }
            if (m[0] > 0) {
                dsm = LSODAIntegrator.vmnorm(neq, common.getAcor(), common.getEwt()) / common.getTesco()[common.getNq()][2];
            }
            if (dsm <= 1d) {
                kflag = 0;
                common.setNst(common.getNst() + 1);
                common.setHu(common.getH());
                common.setNqu(common.getNq());
                common.setMused(common.getMeth());
                
                for (int j = 1; j <= (common.getNq() + 1); j++) {
                    r = common.getEl()[j];
                    double[][] newYh = common.getYh();
                    for (int i = 1; i <= neq; i++) {
                        newYh[j][i] = newYh[j][i] + (common.getAcor()[i] * r);
                    }
                    common.setYh(newYh);
                }
                common.setIcount(common.getIcount() - 1);
                if (common.getIcount() < 0) {
                    // methodswitch(ctx, dsm, pnorm, &rh);
                    if (common.getMeth() != common.getMused()) {
                        rh = Math.max(rh, hmin / Math.abs(common.getH()));
                        LSODAIntegrator.scaleh(ctx, rh);
                        common.setRmax(10d);
                        endStoda();
                        break;
                    }
                }
                common.setIalth(common.getIalth() - 1);
                if (common.getIalth() == 0) {
                    double rhup = 0d;
                    if ((common.getNq() + 1) != maxord + 1) {
                        double[] savf = common.getSavf();
                        for (int i = 1; i <= neq; i++) {
                            savf[i] = common.getAcor()[i] - common.getYh()[maxord + 1][i];
                        }
                        common.setSavf(savf);
                        dup = LSODAIntegrator.vmnorm(neq, common.getSavf(), common.getEwt()) / common.getTesco()[common.getNq()][3];
                        exup = 1d / (double) ((common.getNq() + 1) + 1);
                        rhup = 1d / (1.4 * Math.pow(dup, exup) + 0.0000014);
                    }
                    int orderflag = 0; // orderswitch(ctx, rhup, dsm, &rh, kflag, maxord);

                    if (orderflag == 0) {
                        endStoda();
                        break;
                    }

                    if (orderflag == 1) {
                        rh = Math.max(rh, hmin / Math.abs(common.getH()));
                        LSODAIntegrator.scaleh(ctx, rh);
                        common.setRmax(10d);
                        endStoda();
                        break;
                    }

                    if (orderflag == 2) {
                        resetCoeff();
                        rh = Math.max(rh, (hmin / Math.abs(common.getH())));
                        LSODAIntegrator.scaleh(ctx, rh);
                        common.setRmax(10d);
                        endStoda();
                        break;
                    }
                }
                if (common.getIalth() > 1 || (common.getNq() + 1) == maxord + 1) {
                    endStoda();
                    break;
                }
                double[][] newYh = common.getYh();
                for (int i = 1; i <= neq; i++) {
                    newYh[maxord + 1][i] = common.getAcor()[i];
                }
                common.setYh(newYh);
                endStoda();
                break;

            }
            else {
                kflag -= 1;
                common.setTn(told);
                for (int j = common.getNq(); j >= 1; j--) {
                    for (int i = j; i <= common.getNq(); i++) {
                        double[][] newYh = common.getYh();
                        for (int i1 = 1; i <= neq; i++) {
                            newYh[i1][i] -= newYh[i1+1][i];
                        }
                        common.setYh(newYh);
                    }
                }
                common.setRmax(2d);
                if (Math.abs(common.getH()) <= hmin * 1.00001) {
                    kflag = -1;
                    common.setHold(common.getH());
                    jstart = 1;
                    break;
                }
                if (kflag > -3) {
                    int orderflag = 0; //should be: orderswitch(ctx, 0d, dsm, &rh, kflag, maxord);
                    if (orderflag == 1 || orderflag == 0) {
                        if (orderflag == 0) {
                            rh = Math.min(rh, 0.2d);
                        }
                        rh = Math.max(rh, (hmin / Math.abs(common.getH())));
                        LSODAIntegrator.scaleh(ctx, rh);
                    }
                    if (orderflag == 2) {
                        resetCoeff();
                        rh = Math.max(rh, (hmin / Math.abs(common.getH())));
                        LSODAIntegrator.scaleh(ctx, rh);
                    }
                    continue;
                }
                else {
                    if (kflag == -10) {
                        kflag = -1;
                        common.setHold(common.getH());
                        jstart = 1;
                        break;
                    } else {
                        rh = 0.1d;
                        rh = Math.max((hmin / Math.abs(common.getH())), rh);
                        common.setH(common.getH() * rh);
                        for (int i = 1; i <= neq; i++) {
                            y[i] = common.getYh()[1][i];
                        }
                        common.setSavf(LSODAIntegrator.findDerivatives(ctx.getOdeSystem(), common.getTn(), y));
                        common.setNfe(common.getNfe() + 1);
                        double[][] newYh = common.getYh();
                        for (int i = 1; i <= neq; i++) {
                            newYh[2][i] = common.getH() * common.getSavf()[i];
                        }
                        common.setYh(newYh);
                        common.setIpup(common.getMiter());
                        common.setIalth(5);
                        if (common.getNq() == 1) {
                            continue;
                        }
                        common.setNq(1);
                        resetCoeff();
                        continue;
                    }
                }
            }
        }
        return kflag;
    }

}
