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
        super(Boolean.FALSE);
    }

    @Override
    protected void set(String val) {
        set(Boolean.TRUE);
    }

}
