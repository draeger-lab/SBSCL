package org.simulator.math.odes.exception;

public class IllegalTException extends RuntimeException{
    private String errorMsg;
    public IllegalTException(int TExceptionType, double t, double tout, Number val) {

        switch (TExceptionType) {
            case 1:
                errorMsg = "[lsoda] tout = " + tout + " behind t = " + t +
                        "\n Integration direction is given by "+ val ;
                break;

            case 2:
                errorMsg = "[lsoda] itask = 4 or 5 and tcrit behind tout";
                break;

            case 3:
                errorMsg = "lsoda: itask = 4 or 5 and tcrit behind tcur";
                break;

            case 4:
                errorMsg = "[lsoda] tout too close to t to start integration";
                break;

            case 5:
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
