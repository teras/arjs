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
public class MultiBoolArg extends BaseArg<Integer> implements MultiArg {

    public MultiBoolArg() {
        super(0);
    }

    @Override
    protected void set(String val) {
        set(get() + 1);
    }

}
