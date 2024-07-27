/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author teras
 */
public class MultiIntegralArg extends MultiTypedArg<BigInteger> {

    /**
     * Create a new multiple integral argument with given values
     *
     * @param val The values of the argument
     */
    public MultiIntegralArg(long... val) {
        this(convert(val));
    }

    /**
     * Create a new multiple integral argument with given values
     *
     * @param val The values of the argument
     */
    public MultiIntegralArg(BigInteger... val) {
        super(IntegralArg.conv, val);
    }

    private static BigInteger[] convert(long[] val) {
        if (val == null || val.length == 0)
            return new BigInteger[0];
        BigInteger[] result = new BigInteger[val.length];
        for (int i = 0; i < val.length; i++)
            result[i] = BigInteger.valueOf(val[i]);
        return result;
    }

    /**
     * Get this argument as a list of bytes
     *
     * @return The list of byte values of this argument
     */
    public List<Byte> getBytes() {
        return getValue().stream().map(BigInteger::byteValue).collect(Collectors.toList());
    }

    /**
     * Get this argument as a list of shorts
     *
     * @return The list of short values of this argument
     */
    public List<Short> getShorts() {
        return getValue().stream().map(BigInteger::shortValue).collect(Collectors.toList());
    }

    /**
     * Get this argument as a list of integers
     *
     * @return The list of integer values of this argument
     */
    public List<Integer> getInts() {
        return getValue().stream().map(BigInteger::intValue).collect(Collectors.toList());
    }

    /**
     * Get this argument as a list of longs
     *
     * @return The list of long values of this argument
     */
    public List<Long> getLongs() {
        return getValue().stream().map(BigInteger::longValue).collect(Collectors.toList());
    }
}
