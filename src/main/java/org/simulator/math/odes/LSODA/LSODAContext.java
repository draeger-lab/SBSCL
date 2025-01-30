package org.simulator.math.odes.LSODA;

import java.util.Objects;

public class LSODAContext{

    private LSODAFunction function; // what is this supposed to be?
    private Object data;
    private int state;
    private int neq;
    private String error;
    private LSODAOptions opt;
    public Object nslast;
    private LSODACommon common;
    private LSODACommonContext commonCtx;

    public LSODAContext() {
    }

    public LSODAContext(LSODAContext ctx, LSODACommonContext commonCtx) {
        this.state = ctx.state;
        this.commonCtx = commonCtx;
        this.neq = ctx.neq;
        this.opt = ctx.opt;
    }

    public LSODACommonContext getCommonCtx() {
        return this.commonCtx;
    }

    public void setCommonCtx(LSODACommonContext commonCtx) {
        this.commonCtx = commonCtx;
    }

    public int getNeq() {
        return neq;
    }

    public void setNeq(int neq) {
        this.neq = neq;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public LSODAOptions getOpt() {
        return opt;
    }

    public void setOpt(LSODAOptions opt) {
        this.opt = opt;
    }

    public LSODACommon getCommon() {
        return common;
    }

    public void setCommon(LSODACommon common) {
        this.common = common;
    }

    public LSODAFunction getFunction() {
        return function;
    }
    
    public void setFunction(LSODAFunction function) {
        this.function = function;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "LSODAContext [state=" + state + ", neq=" + neq + ", opt=" + opt + "]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.state;
        hash = 71 * hash + this.neq;
        hash = 71 * hash + Objects.hashCode(this.opt);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LSODAContext other = (LSODAContext) obj;
        if (this.state != other.state) {
            return false;
        }
        if (this.neq != other.neq) {
            return false;
        }
        return Objects.equals(this.opt, other.opt);
    }

    @Override
    protected LSODAContext clone() throws CloneNotSupportedException {
        return new LSODAContext(this, this.getCommonCtx());
    }

    public Object getNslast() {
        return nslast;
    }

    public void setNslast(Object nslast) {
        this.nslast = nslast;
    }
}