/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * @author teras
 */
public class DecimalArg extends TypedArg<BigDecimal> {

    static final Function<String, BigDecimal> conv = t -> {
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Not a decimal value");
        }
    };

    /**
     * Create a new decimal argument with value 0
     */
    public DecimalArg() {
        this(BigDecimal.ZERO);
    }

    /**
     * Create a new decimal argument with a specific value
     *
     * @param val The value of the argument
     */
    public DecimalArg(float val) {
        this(BigDecimal.valueOf(val));
    }

    /**
     * Create a new decimal argument with a specific value
     *
     * @param val The value of the argument
     */
    public DecimalArg(BigDecimal val) {
        super(conv, val);
    }

    /**
     * Get this argument as float
     *
     * @return The float value of this argument
     */
    public double getFloat() {
        return getValue().floatValue();
    }

    /**
     * Get this argument as double
     *
     * @return The double value of this argument
     */
    public double getDouble() {
        return getValue().doubleValue();
    }
}
