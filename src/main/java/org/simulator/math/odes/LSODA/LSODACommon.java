package org.simulator.math.odes.LSODA;

import java.util.Arrays;

public class LSODACommon {
    
    public final double ETA = 2.2204460492503131e-16;
    public final double SQRTETA = 1.4901161193847656e-08;
    public final double CCMAX = 0.3d; // definition, see source code line 170
    public final int MAXCOR = 3;
    public final int MSBP = 20;
    public final int MXNCF = 10;
    public final double RATIO = 5.0d;
    // private LSODACommonContext commonCtx;

    // LSODACommonContext variables
    private double[][] yh;
    private double[][] wm;
    private double[] ewt;
    private double[] savf;
    private double[] acor;
    private int[] ipvt;
    private Object memory;

    private double h, hu, rc, tn;
    private double tsw, pdnorm;

    private double crate;
    private double[] el = new double[14];
    private double[][] elco = new double[13][14];
    private double[][] tesco = new double[13][4];
    private double hold, rmax;
    private double pdest, pdlast;

    private int ialth, ipup, nslp;
    private int icount, irflag;
    private int ixmer;
    private int illin, nhnil, nslast, jcur, meth, mused, nq, nst, ncf, nfe, nje, nqu, miter;

    // end of LSODACommonContext variables

    public double max(double a, double b) {
        return (a > b) ? a : b;
    }

    public double min(double a, double b) {
        return (a < b) ? a : b;
    }

    // SM1 array, implementation from common.c
    private static final double[] SM1 = {
        0.0d, 0.5d, 0.575d, 0.55d, 0.45d, 0.35d, 0.25d, 0.2d, 0.15d, 0.1d, 0.075d, 0.05d, 0.025d
    };

    public LSODACommon() {
        this.yh = new double[14][];
        this.wm = new double[14][];
        this.ewt = new double[0];
        this.savf = new double[0];
        this.acor = new double[0];
        this.ipvt = new int[0];
    }

    @Override
    public String toString() {
        return "LSODACommonContext{" + 
            "h=" + h +
            ", hu=" + hu +
            ", tn=" + tn +
            ", tsw=" + tsw +
            ", meth=" + meth +
            ", nst=" + nst +
            ", nfe=" + nfe +
            "}";
    }

    public double[] getSM1() {
        return Arrays.copyOf(SM1, SM1.length);
    }

    public double[][] getYh() {
        return yh;
    }

    public void setYh(double[][] yh) {
        this.yh = yh;
    }

    public double[][] getWm() {
        return wm;
    }

    public void setWm(double[][] wm) {
        this.wm = wm;
    }

    public double[] getEwt() {
        return ewt;
    }

    public void setEwt(double[] ewt) {
        this.ewt = ewt;
    }

    public double[] getSavf() {
        return savf;
    }

    public void setSavf(double[] savf) {
        this.savf = savf;
    }

    public double[] getAcor() {
        return acor;
    }

    public void setAcor(double[] acor) {
        this.acor = acor;
    }

    public int[] getIpvt() {
        return ipvt;
    }

    public void setIpvt(int[] ipvt) {
        this.ipvt = ipvt;
    }

    public Object getMemory() {
        return memory;
    }

    public void setMemory(Object memory) {
        this.memory = memory;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getHu() {
        return hu;
    }

    public void setHu(double hu) {
        this.hu = hu;
    }

    public double getRc() {
        return rc;
    }

    public void setRc(double rc) {
        this.rc = rc;
    }

    public double getTn() {
        return tn;
    }

    public void setTn(double tn) {
        this.tn = tn;
    }

    public double getTsw() {
        return tsw;
    }

    public void setTsw(double tsw) {
        this.tsw = tsw;
    }

    public double getPdnorm() {
        return pdnorm;
    }

    public void setPdnorm(double pdnorm) {
        this.pdnorm = pdnorm;
    }

    public double getCrate() {
        return crate;
    }

    public void setCrate(double crate) {
        this.crate = crate;
    }

    public double[] getEl() {
        return el;
    }

    public void setEl(double[] el) {
        this.el = el;
    }

    public double[][] getElco() {
        return elco;
    }

    public void setElco(double[][] elco) {
        this.elco = elco;
    }

    public double[][] getTesco() {
        return tesco;
    }

    public void setTesco(double[][] tesco) {
        this.tesco = tesco;
    }

    public double getHold() {
        return hold;
    }

    public void setHold(double hold) {
        this.hold = hold;
    }

    public double getRmax() {
        return rmax;
    }

    public void setRmax(double rmax) {
        this.rmax = rmax;
    }

    public double getPdest() {
        return pdest;
    }

    public void setPdest(double pdest) {
        this.pdest = pdest;
    }

    public double getPdlast() {
        return pdlast;
    }

    public void setPdlast(double pdlast) {
        this.pdlast = pdlast;
    }

    public int getIalth() {
        return ialth;
    }

    public void setIalth(int ialth) {
        this.ialth = ialth;
    }

    public int getIpup() {
        return ipup;
    }

    public void setIpup(int ipup) {
        this.ipup = ipup;
    }

    public int getNslp() {
        return nslp;
    }

    public void setNslp(int nslp) {
        this.nslp = nslp;
    }

    public int getIcount() {
        return icount;
    }

    public void setIcount(int icount) {
        this.icount = icount;
    }

    public int getIrflag() {
        return irflag;
    }

    public void setIrflag(int irflag) {
        this.irflag = irflag;
    }

    public int getIxmer() {
        return ixmer;
    }

    public void setIxmer(int ixmer) {
        this.ixmer = ixmer;
    }

    public int getIllin() {
        return illin;
    }

    public void setIllin(int illin) {
        this.illin = illin;
    }

    public int getNhnil() {
        return nhnil;
    }

    public void setNhnil(int nhnil) {
        this.nhnil = nhnil;
    }

    public int getNslast() {
        return nslast;
    }

    public void setNslast(int nslast) {
        this.nslast = nslast;
    }

    public int getJcur() {
        return jcur;
    }

    public void setJcur(int jcur) {
        this.jcur = jcur;
    }

    public int getMeth() {
        return meth;
    }

    public void setMeth(int meth) {
        this.meth = meth;
    }

    public int getMused() {
        return mused;
    }

    public void setMused(int mused) {
        this.mused = mused;
    }

    public int getNq() {
        return nq;
    }

    public void setNq(int nq) {
        this.nq = nq;
    }

    public int getNst() {
        return nst;
    }

    public void setNst(int nst) {
        this.nst = nst;
    }

    public int getNcf() {
        return ncf;
    }

    public void setNcf(int ncf) {
        this.ncf = ncf;
    }

    public int getNfe() {
        return nfe;
    }

    public void setNfe(int nfe) {
        this.nfe = nfe;
    }

    public int getNje() {
        return nje;
    }

    public void setNje(int nje) {
        this.nje = nje;
    }

    public int getNqu() {
        return nqu;
    }

    public void setNqu(int nqu) {
        this.nqu = nqu;
    }

    public int getMiter() {
        return miter;
    }

    public void setMiter(int miter) {
        this.miter = miter;
    }

    // public LSODACommonContext getCommonCtx() {
    //     return commonCtx;
    // }

    // public void setCommonCtx(LSODACommonContext commonCtx) {
    //     this.commonCtx = commonCtx;
    // }

    // public LSODACommon(LSODACommonContext commonCtx) {
    //     this.commonCtx = commonCtx;
    // }

}
