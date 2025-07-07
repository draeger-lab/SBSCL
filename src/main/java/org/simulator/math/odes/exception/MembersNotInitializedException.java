package org.simulator.math.odes.exception;

public class MembersNotInitializedException extends RuntimeException{
    public MembersNotInitializedException(){
        super(String.format("[lsoda] Illegal common block: Did you call lsodaPrepare()"));
    }
}
