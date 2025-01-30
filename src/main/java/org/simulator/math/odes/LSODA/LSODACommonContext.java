package org.simulator.math.odes.LSODA;

public class LSODACommonContext {
    public double[][] yh;
    public double[][] wm;
    public double[] ewt;
    public double[] savf;
    public double[] acor;
    public int[] ipvt;
    public Object memory;

    public double h, hu, rc, tn;
    public double tsw, pdnorm;

    public double crate;
    public double[] el = new double[14];
    public double[][] elco = new double[13][14];
    public double[][] tesco = new double[13][4];
    public double hold, rmax;
    public double pdest, pdlast;

    public int ialth, ipup, nslp;
    public int icout, irflag;
    public int ixmer;
    public int illin, nhnil, nslast, jcur, meth, mused, nq, nst, ncf, nfe, nje, nqu, miter;

    public LSODACommonContext() {
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

    public int getIcout() {
        return icout;
    }

    public void setIcout(int icout) {
        this.icout = icout;
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
}
}