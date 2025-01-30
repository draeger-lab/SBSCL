package org.simulator.math.odes.LSODA;

public class LSODACommon {
    
    public static final double ETA = 2.2204460492503131e-16;
    public static final double SQRTETA = 1.4901161193847656e-08;
    public static final double CCMAX = 0.3d;
    public static final int MAXCOR = 3;
    public static final int MSBP = 20;
    public static final int MXNCF = 10;
    public static final double RATIO = 5.0d;
    private LSODACommonContext commonCtx;

    public static double max(double a, double b) {
        return (a > b) ? a : b;
    }

    public static double min(double a, double b) {
        return (a < b) ? a : b;
    }

    // SM1 array, implementation from common.c
    private static final double[] SM1 = {
        0.0d, 0.5d, 0.575d, 0.55d, 0.45d, 0.35d, 0.25d, 0.2d, 0.15d, 0.1d, 0.075d, 0.05d, 0.025d
    };

    public static double getEta() {
        return ETA;
    }

    public static double getSqrteta() {
        return SQRTETA;
    }

    public static double getCcmax() {
        return CCMAX;
    }

    public static int getMaxcor() {
        return MAXCOR;
    }

    public static int getMsbp() {
        return MSBP;
    }

    public static int getMxncf() {
        return MXNCF;
    }

    public static double getRatio() {
        return RATIO;
    }

    public static double[] getSm1() {
        return SM1;
    }

    public LSODACommonContext getCommonCtx() {
        return commonCtx;
    }

    public void setCommonCtx(LSODACommonContext commonCtx) {
        this.commonCtx = commonCtx;
    }

    public LSODACommon(LSODACommonContext commonCtx) {
        this.commonCtx = commonCtx;
    }

}
