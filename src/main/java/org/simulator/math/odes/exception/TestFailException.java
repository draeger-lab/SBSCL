package org.simulator.math.odes.exception;

public class TestFailException extends RuntimeException {
    private String errorMsg;
    public TestFailException(int testFaileType, double tn, double h){
        
        if(testFaileType == 1) {
            errorMsg = "[lsoda] at t = " + tn + " and step size h = " + h + ", the\n error test failed repeatedly or\n with Math.abs(h) = hmin\n";
        }
        else if(testFaileType == 2) {
            errorMsg = "[lsoda] at t = " + tn + " and step size h = " + h + ", the\n corrector convergence failed repeatedly or\n with Math.abs(h) = hmin\n";
        }
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
