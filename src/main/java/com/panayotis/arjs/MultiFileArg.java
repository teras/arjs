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
public class MultiFileArg extends MultiTypedArg<File> {

    public MultiFileArg(File... val) {
        super(t -> new File(t), val);
    }

}
