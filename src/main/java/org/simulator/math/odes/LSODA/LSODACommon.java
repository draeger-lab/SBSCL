package org.simulator.math.odes.LSODA;

public class LSODACommon {
    
    public static final double ETA = 2.2204460492503131e-16;
    public static final double SQRTETA = 1.4901161193847656e-08;
    public static final double CCMAX = 0.3d;
    public static final int MAXCOR = 3;
    public static final int MSBP = 20;
    public static final int MXNCF = 10;
    public static final double RATIO = 5.0d;

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

    public static double[] getSM1() {
        return SM1.clone();
    }

    // lsoda_common_t struct from common.h 
    public static class LSODACommonContext {
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
    }

}
