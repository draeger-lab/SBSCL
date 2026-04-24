package org.simulator.math.odes.exception;

import org.simulator.math.odes.LSODA.LSODAIntegrator.illegalTExceptionCases;

public class IllegalTException extends RuntimeException{
    private String errorMsg;
    public IllegalTException(illegalTExceptionCases TExceptionType, double t, double tout, Number val) {

        switch (TExceptionType) {
            case toutBehindT:
                errorMsg = "[lsoda] tout = " + tout + " behind t = " + t +
                        "\n Integration direction is given by "+ val ;
                break;

            case tcritBehindTout:
                errorMsg = "[lsoda] itask = 4 or 5 and tcrit behind tout";
                break;

            case tcritBehindTcur:
                errorMsg = "lsoda: itask = 4 or 5 and tcrit behind tcur";
                break;

            case toutToCloseToT:
                errorMsg = "[lsoda] tout too close to t to start integration";
                break;

            case toutBehindTcurMinusHu:
                errorMsg = "[lsoda] itask = " + val + " and tout behind tcur - hu";
                break;

            default:
                break;
        }
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
