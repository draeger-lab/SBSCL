package org.simulator.math.odes;

import java.util.Objects;

class LSODAContext {

    private int state;
    private int neq;
    private LSODAOptions opt;

    public LSODAContext() {
    }

    public LSODAContext(LSODAContext ctx) {
        this.state = ctx.state;
        this.neq = ctx.neq;
        this.opt = ctx.opt;

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
        return new LSODAContext(this);
    }
}