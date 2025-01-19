package org.simulator.math.odes.LSODA;

class LSODAOptions {

    
    private int mxordn;
    private int mxords;
    private double hmax;
    private double hmxi;
    private double hmin;
    private int itask;
    private int ixpr;
    private int mxstep;
    private int mxhnil;
    private double[] atol;
    private double[] rtol;
    private double h0;

    public LSODAOptions() {
    }

    public int getMxordn() {
        return mxordn;
    }

    public void setMxordn(int mxordn) {
        this.mxordn = mxordn;
    }

    public int getMxords() {
        return mxords;
    }

    public void setMxords(int mxords) {
        this.mxords = mxords;
    }

    public double getHmax() {
        return hmax;
    }

    public void setHmax(double hmax) {
        this.hmax = hmax;
    }

    public double getHmxi() {
        return hmxi;
    }

    public void setHmxi(double hmxi) {
        this.hmxi = hmxi;
    }

    public double getHmin() {
        return hmin;
    }

    public void setHmin(double hmin) {
        this.hmin = hmin;
    }

    public int getItask() {
        return itask;
    }

    public void setItask(int itask) {
        this.itask = itask;
    }

    public int getIxpr() {
        return ixpr;
    }

    public void setIxpr(int ixpr) {
        this.ixpr = ixpr;
    }

    public int getMxstep() {
        return mxstep;
    }

    public void setMxstep(int mxstep) {
        this.mxstep = mxstep;
    }

    public int getMxhnil() {
        return mxhnil;
    }

    public void setMxhnil(int mxhnil) {
        this.mxhnil = mxhnil;
    }

    public double[] getAtol() {
        return atol;
    }

    public void setAtol(double[] atol) {
        this.atol = atol;
    }

    public double[] getRtol() {
        return rtol;
    }

    public void setRtol(double[] rtol) {
        this.rtol = rtol;
    }

    public double getH0() {
        return h0;
    }

    public void setH0(double h0) {
        this.h0 = h0;
    }
}