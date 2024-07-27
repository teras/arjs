/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author teras
 */
public class MultiDecimalArg extends MultiTypedArg<BigDecimal> {

    /**
     * Create a new multiple decimal argument with given values
     *
     * @param val The values of the argument
     */
    public MultiDecimalArg(double... val) {
        this(convert(val));
    }

    /**
     * Create a new multiple decimal argument with given values
     *
     * @param val The values of the argument
     */
    public MultiDecimalArg(BigDecimal... val) {
        super(DecimalArg.conv, val);
    }

    private static BigDecimal[] convert(double[] val) {
        if (val == null || val.length == 0)
            return new BigDecimal[0];
        BigDecimal[] result = new BigDecimal[val.length];
        for (int i = 0; i < val.length; i++)
            result[i] = BigDecimal.valueOf(val[i]);
        return result;
    }

    /**
     * Get this argument as a list of floats
     *
     * @return The list of float values of this argument
     */
    public List<Float> getFloats() {
        return getValue().stream().map(BigDecimal::floatValue).collect(Collectors.toList());
    }

    /**
     * Get this argument as a list of doubles
     *
     * @return The list of double values of this argument
     */
    public List<Double> getDoubles() {
        return getValue().stream().map(BigDecimal::doubleValue).collect(Collectors.toList());
    }
}
