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
public class MultiStringArg extends MultiTypedArg<String> {

    public MultiStringArg(String... val) {
        super(t -> t, val);
    }

}
