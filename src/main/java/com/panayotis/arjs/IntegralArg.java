/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigInteger;
import java.util.function.Function;

/**
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

    /**
     * Create a new integral argument with value 0
     */
    public IntegralArg() {
        this(BigInteger.ZERO);
    }

    /**
     * Create a new integral argument with a specific value
     *
     * @param val The value of the argument
     */
    public IntegralArg(long val) {
        this(BigInteger.valueOf(val));
    }

    /**
     * Create a new integral argument with a specific value
     *
     * @param val The value of the argument
     */
    public IntegralArg(BigInteger val) {
        super(conv, val);
    }

    /**
     * Get this argument as byte
     *
     * @return The byte value of this argument
     */
    public byte getByte() {
        return getValue().byteValue();
    }

    /**
     * Get this argument as short
     *
     * @return The short value of this argument
     */
    public short getShort() {
        return getValue().shortValue();
    }

    /**
     * Get this argument as int
     *
     * @return The int value of this argument
     */
    public int getInt() {
        return getValue().intValue();
    }

    /**
     * Get this argument as long
     *
     * @return The long value of this argument
     */
    public long getLong() {
        return getValue().longValue();
    }
}
