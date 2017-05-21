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
public class StringArg extends BaseArg<String> implements TransitiveArg {

    public StringArg() {
        this(null);
    }

    public StringArg(String val) {
        super(val);
    }

    @Override
    protected void set(String val) {
        super.set(val);
    }

}
