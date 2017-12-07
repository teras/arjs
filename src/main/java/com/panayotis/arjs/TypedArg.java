/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.function.Function;

/**
 *
 * @author teras
 * @param <T>
 */
public class TypedArg<T> extends BaseArg<T> implements TransitiveArg {

    private final Function<String, T> converter;

    public TypedArg(Function<String, T> converter) {
        this(converter, null);
    }

    public TypedArg(Function<String, T> converter, T val) {
        super(val);
        if (converter == null)
            throw new NullPointerException("Converter could not be null");
        this.converter = converter;
    }

    @Override
    protected boolean set(String val) {
        return setVal(converter.apply(val));
    }

}
