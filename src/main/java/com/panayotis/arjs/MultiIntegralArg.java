/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigInteger;

/**
 *
 * @author teras
 */
public class MultiIntegralArg extends MultiTypedArg<BigInteger> {

    public MultiIntegralArg(long... val) {
        this(convert(val));
    }

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

}
