package com.panayotis.arjs;

public class BoolExclusiveArg extends BoolArg {
    private BoolExclusiveArg inverse;

    public BoolExclusiveArg() {
        this(false);
    }

    public BoolExclusiveArg(boolean status) {
        this(status, true);
    }

    private BoolExclusiveArg(Boolean status, boolean shouldMakeInverse) {
        super(status != null && status);
        if (shouldMakeInverse) {
            inverse = new BoolExclusiveArg(!get(), false);
            inverse.inverse = this;
        }
    }

    public BoolExclusiveArg getInverse() {
        return inverse;
    }

    @Override
    protected boolean set(String val) {
        setVal(Boolean.TRUE);
        inverse.setVal(Boolean.FALSE);
        return true;
    }
}
