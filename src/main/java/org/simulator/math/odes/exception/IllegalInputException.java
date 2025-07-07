package org.simulator.math.odes.exception;

public class IllegalInputException extends RuntimeException{
    public IllegalInputException(String from, String variable, Number value) {
        super(String.format("[" + from + "] Illegal input: " + variable + " = " + value));
    }

    public IllegalInputException(String from, String variable, Number value, String message) {
        super(String.format("[" + from + "] Illegal input: " + variable + " = " + value + ", " + message));
    }

    public IllegalInputException(String from, String variable, String message) {
        super(String.format("[" + from + "] Illegal input: " + variable + ", " + message));
    }

    public IllegalInputException(String from, double hmax, String string) {
        //TODO Auto-generated constructor stub
    }
}
