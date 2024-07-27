/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author teras
 */
public class MultiTypedArg<T> extends BaseArg<List<T>> implements MultiArg, TransitiveArg {

    private final Function<String, T> converter;

    public MultiTypedArg(Function<String, T> converter, T... val) {
        super(getSafeList(val));
        if (converter == null)
            throw new NullPointerException("Converter could not be null");
        this.converter = converter;
    }

    @Override
    protected boolean set(String val) {
        T v = converter.apply(val);
        if (v == null)
            return false;
        getValue().add(v);
        return true;
    }

    private static <Q> List<Q> getSafeList(Q[] values) {
        List<Q> result = new ArrayList<>();
        if (values != null && values.length > 0)
            for (Q item : values)
                if (item != null)
                    result.add(item);
        return result;
    }

}
