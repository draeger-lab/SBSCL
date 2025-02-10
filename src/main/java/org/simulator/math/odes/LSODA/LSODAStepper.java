package org.simulator.math.odes.LSODA;

import javax.naming.Context;

import org.semanticweb.owlapi.util.CommonBaseIRIMapper;

public class LSODAStepper {
    private LSODAContext ctx;
    private LSODACommon common = ctx.getCommon();
    private double[] y;
    private int jstart;

    public LSODAStepper(LSODAContext ctx, double[] y, int jstart) {
        this.ctx = ctx;
    }

    public int stoda(double[] y, int jstart) {
        this.y = y;
        this.jstart = jstart;

        int kflag = 0;
        double told = common.getTn();

        return kflag;

    }

    private void endStoda() {
        double r = 1d / common.getTesco()[ctx.getNeq()][1];
        double[] acor = common.getAcor();

        for (int i = 1; i <= ctx.getNeq(); i++) {
            double acorR = acor[i] * r;
            acor[i] = acorR;
            common.setAcor(acor);

            common.setHold(common.getH());
        }
    }

    private void resetCoeff() {
        double el0 = common.getEl()[0];
        double[] el = common.getEl();

        for (int i = 1; i <= (common.getNq() + 1); i++) {
            double newEl = common.getEl()[i];
            el[i] = newEl;
            //common.setEl() = common.getElco()[common.getNq()][i]; figure out this definition
        }
    }

    public int stoda(LSODAContext ctx, double[]y, int jstart) {
        int kflag;
        int m;
        double del, delp, dsm, dup, exup, r, rh, told;
        double pnorm;

        double hmin = ctx.getOpt().getHmin();
        int mxords = ctx.getOpt().getMxords();
        int mxordn = ctx.getOpt().getMxordn();
        int neq = ctx.getNeq();


        kflag = 0;
        told = common.getTn();
        common.setNcf(0);
        delp = 0d;

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
            common.setEl(newEl);

            // initialize switching parameters
            common.setIcout(20);
            common.setIrflag(0);
            common.setPdest(0d);
            common.setPdlast(0d);

            // implement function cfode(ctx, 1);
            resetCoeff();
        }

        if (jstart == -1) {
            common.setIpup(common.getMiter());
            if (common.getIalth() == 1) {
                common.setIalth(2);
            }
            if (common.getMeth() != common.getMused()) {
                // cfode(ctx, common.getMeth());
                common.setIalth((common.getNq() + 1));
                resetCoeff();
            }
            if (common.getH() != common.getHold()) {
                rh = common.getH() / common.getHold();
                common.setH(common.getHold());
                // scaleh(ctx, rh);
            }
        }

        if (jstart == -2) {
            if (common.getH() != common.getHold()) {
                rh = common.getH() / common.getHold();
                common.setH(common.getHold());
                // scaleh(ctx, rh);
            }
        }

        dsm = 0d;
        while (true) { 
            common.setJcur(0);

            while (true) {
                if (Math.abs(common.getRc() - 1d) > CCMAX) { //what is CCMAX???
                    common.setIpup(common.getMiter());
                }
                if (common.getNst() >= common.getNslp() + MSBP) {
                    common.setIpup(common.getMiter());
                }
                common.setTn(common.getTn() + common.getH());

                for (int j = common.getNq(); j >= 1; j--) {
                    for (int i1 = j; i1 <= common.getNq(); i1++) {
                        for (int i = 1; i <= neq; i++) {
                            double[][] newYh = common.getYh();
                            newYh[i1][i] = newYh[i1 + 1][i];
                            common.setYh(newYh);
                        }
                    }
                }

                pnorm = 0d; //vmnorm(neq, common.getYh()[1], common.getEwt()); // fix vnorm definitions
                
                int corflag = 0; //correction(ctx, y, pnorm, &del, &delp, told, &m); //implement correction

                if (corflag == 0) {
                    break;
                }
                if (corflag == 1) {
                    rh = Math.max(0.25, hmin / Math.abs(common.getH()));
                    // scaleh(ctx, rh);
                    continue;
                }
                if (corflag == 2) {
                    kflag = -2;
                    common.setHold(common.getH());
                    jstart = 1;
                    return kflag;
                }
            }
            if (m == 0) {
                dsm = del / common.getTesco()[common.getNq()][2];
            }
            if (m > 0) {
                dsm = 0d; //vmnorm(neq, common.getAcor(), common.getEwt() / common.getTesco()[common.getNq()][2]);
            }
            if (dsm <= 1d) {
                kflag = 0;
                common.setNst(common.getNst() + 1);
                common.setHu(common.getH());
                common.setNqu(common.getNq());
                common.setMused(common.getMeth());
                
                for (int j = 1; j <= (common.getNq() + 1); j++) {
                    r = common.getEl()[j];
                    for (int i = 1; i <= neq; i++) {
                        double[][] newYh = common.getYh();
                        newYh[j][i] = newYh[j][i] + (common.getAcor()[i] * r);
                        common.setYh(newYh);
                    }
                }
                common.setIcount(common.getIcount() - 1);
                if (common.getIcount < 0) {
                    // methodswitch(ctx, dsm, pnorm, &rh);
                    if (common.getMeth() != common.getMused()) {
                        rh = Math.max(rh, hmin / Math.abs(common.getH()));
                        // scaleh(ctx, rh);
                        common.setRmax(10d);
                        endStoda();
                        break;
                    }
                }
                common.setIalth(common.getIalth() - 1);
                if (common.getIalth() == 0) {
                    double rhup = 0d;
                    if ((common.getNq() + 1) != maxord + 1) {
                        for (int i = 1; i <= neq; i++) {
                            double[] savf = common.getSavf();
                            savf[i] = common.getAcor()[i] - common.getYh()[maxord + 1][i];
                            common.setSavf(savf);
                        }
                        dup = 0d; //vmnorm(neq, common.getSavf(), (common.getEwt()) / common.getTesco()[common.getNq()][2]);
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
                        // sclaeh(ctx, rh);
                    }
                }
            }
        }
    }

}
