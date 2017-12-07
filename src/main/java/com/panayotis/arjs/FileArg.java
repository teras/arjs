/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.io.File;

/**
 *
 * @author teras
 */
public class FileArg extends TypedArg<File> {

    public FileArg() {
        this((File) null);
    }

    public FileArg(String val) {
        this(val == null ? null : new File(val));
    }

    public FileArg(File val) {
        super(f -> new File(f), val);
    }

}
