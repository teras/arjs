/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.argparse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author teras
 */
public class ArgsTest {

    public ArgsTest() {
    }

    /**
     * Test of parse method, of class Args.
     */
    @Test
    public void testParse() {
        AtomicBoolean help = new AtomicBoolean();
        AtomicBoolean run = new AtomicBoolean();
        AtomicReference<String> output = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        AtomicInteger multi = new AtomicInteger();
        List<String> rest2;

        Args args = new Args()
                .def("-h", () -> help.set(true))
                .def("--multi", () -> multi.addAndGet(1))
                .multi("--multi")
                .alias("-h", "--help")
                .def("-o", out -> output.set(out))
                .req("-o")
                .alias("-o", "--output")
                .def("--run", () -> run.set(true))
                .uniq("-o", "--run")
                .error(err -> error.set(err));

        args.parse((String[]) null);
        args.parse(new String[]{});

        args.parse(new String[]{"-h"});
        assertTrue("Help argument not found", help.get());
        assertFalse("Should fail with missing argument", error.get() == null);

        reset(help, run, output, error, multi);
        args.parse(new String[]{"-o", "something", "--run"});
        System.out.println(error.get());
        assertTrue("Wrong type of error, expected \"uniq\"", error.get() != null && error.get().contains("uniq"));
        assertTrue("Error should contain ' -o|--output, --run' ", error.get().contains(" -o|--output, --run"));

        reset(help, run, output, error, multi);
        rest2 = args.parse(new String[]{"-h", "-h", "unknown", "--help"});
        assertFalse("Should fail with rest argument non existent", rest2.isEmpty());
        assertEquals("Size of missing arguments does not match", 1, rest2.size());
        assertEquals("Missing argument does not match", "unknown", rest2.get(0));

        reset(help, run, output, error, multi);
        args.parse(new String[]{"-h", "-o", "some", "--multi", "--multi"});
        assertTrue("No errors should be found", error.get() == null);
        assertEquals("Output should be parsed", "some", output.get());
        assertEquals("Multiple parameter should be called twice", 2, multi.get());

        reset(help, run, output, error, multi);
        args.parse(new String[]{"-h", "-h", "-o"});
        assertTrue("Wrong type of error, expected \"Too few arguments\"", error.get() != null && error.get().contains("few"));
    }

    private void reset(AtomicBoolean help, AtomicBoolean run, AtomicReference<String> output, AtomicReference<String> error, AtomicInteger multi) {
        help.set(false);
        run.set(false);
        output.set(null);
        error.set(null);
        multi.set(0);
    }
}
