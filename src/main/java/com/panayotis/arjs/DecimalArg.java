/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 *
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

    public DecimalArg() {
        this(BigDecimal.ZERO);
    }

    public DecimalArg(float val) {
        this(BigDecimal.valueOf(val));
    }

    public DecimalArg(BigDecimal val) {
        super(conv, val);
    }

}
