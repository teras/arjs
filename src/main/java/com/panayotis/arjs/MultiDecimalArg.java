/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigDecimal;

/**
 *
 * @author teras
 */
public class MultiDecimalArg extends MultiTypedArg<BigDecimal> {

    public MultiDecimalArg(double... val) {
        this(convert(val));
    }

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

}
