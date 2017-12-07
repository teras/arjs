/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

/**
 *
 * @author teras
 */
public class BoolArg extends BaseArg<Boolean> {

    public BoolArg() {
        this(false);
    }

    public BoolArg(boolean status) {
        super(status);
    }

    @Override
    protected boolean set(String val) {
        return setVal(Boolean.TRUE);
    }

}
