/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.Objects;

public abstract class BaseArg<T> {

    private T val;
    private boolean isSet = false;

    protected BaseArg(T val) {
        this.val = val;
    }

    public T getValue() {
        return val;
    }

    protected abstract boolean set(String val);

    protected final boolean setVal(T val) {
        if (val == null)
            return false;
        this.val = val;
        isSet = true;
        return true;
    }

    public boolean isSet() {
        return isSet;
    }

    @Override
    public String toString() {
        return val == null ? null : val.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.val);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BaseArg<?> other = (BaseArg<?>) obj;
        return Objects.equals(this.val, other.val);
    }
}
