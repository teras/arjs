/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.Objects;

public abstract class BaseArg<T> {

    private T val;

    protected BaseArg(T val) {
        this.val = val;
    }

    public T get() {
        return val;
    }

    protected void set(T val) {
        this.val = val;
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
        if (!Objects.equals(this.val, other.val))
            return false;
        return true;
    }

    protected abstract void set(String val);

}
