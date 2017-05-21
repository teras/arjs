/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.argparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author teras
 */
public class MultiStringArg extends BaseArg<List<String>> implements MultiArg, TransitiveArg {

    public MultiStringArg(String... val) {
        super(val == null || val.length < 1 ? new ArrayList<>() : new ArrayList<>(Arrays.asList(val)));
    }

    @Override
    protected void set(String val) {
        get().add(val);
    }

}
