/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

/**
 * @author teras
 */
public class StringArg extends TypedArg<String> implements CharSequence {

    public StringArg() {
        this(null);
    }

    public StringArg(String val) {
        super(t -> t, val);
    }

    @Override
    public int length() {
        return getCheckedValue().length();
    }

    @Override
    public char charAt(int index) {
        return getCheckedValue().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return getCheckedValue().subSequence(start, end);
    }

    @Override
    public String toString() {
        return getCheckedValue();
    }

    private String getCheckedValue() {
        if (!isSet())
            throw new IndexOutOfBoundsException("Argument is not set");
        return getValue();
    }
}
