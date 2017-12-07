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
public class StringArg extends TypedArg<String> {

    public StringArg() {
        this(null);
    }

    public StringArg(String val) {
        super(t -> t, val);
    }

}
