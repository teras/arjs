/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigInteger;
import java.util.function.Function;

/**
 *
 * @author teras
 */
public class IntegralArg extends TypedArg<BigInteger> {

    static final Function<String, BigInteger> conv = t -> {
        try {
            return new BigInteger(t);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Not an integral value");
        }
    };

    public IntegralArg() {
        this(BigInteger.ZERO);
    }

    public IntegralArg(long val) {
        this(BigInteger.valueOf(val));
    }

    public IntegralArg(BigInteger val) {
        super(conv, val);
    }

}
