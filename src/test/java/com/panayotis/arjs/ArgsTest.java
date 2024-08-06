/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author teras
 */
public class ArgsTest {

    public ArgsTest() {
    }

    @Test
    public void testJoinedAndCondensed() {
        MultiBoolArg mboolA = new MultiBoolArg();
        MultiBoolArg mboolB = new MultiBoolArg();
        MultiStringArg mstring = new MultiStringArg();
        Args args = new Args("myapp", "");
        args.
                def("/a", mboolA).
                def("/b", mboolB).
                def("/s", mstring);

        args.setCondensed('/');
        args.setJoined(':');
        List<String> free = args.parse("/a", "/aa", "/aaasbbasa", "one", "two", "/s:three", "/b", "/a:another", "/s", "four", "/s:five", "/b");
        assertEquals((Integer) 8, mboolA.getValue(), "Condensed notation error with paramter /a,");
        assertEquals((Integer) 4, mboolB.getValue(), "Condensed notation error with paramter /b,");
        assertEquals("[one, two, three, four, five]", mstring.getValue().toString(), "Multi string with condensed mode");
        assertEquals("[/a:another]", free.toString(), "A free parameter should exist, since /a is not a transitive parameter,");
    }

    @Test
    public void testArgs() {
        BoolArg bool = new BoolArg();
        StringArg string = new StringArg();
        StringArg defstring = new StringArg("hey");
        MultiBoolArg mbool = new MultiBoolArg();
        MultiStringArg mstring = new MultiStringArg();
        MultiStringArg mdefstring = new MultiStringArg("hello", "hi");

        Args args = new Args("myapp", "");
        args.
                def("-b", bool).
                def("-s", string).
                def("-d", defstring).
                def("-B", mbool).
                def("-S", mstring).
                def("-D", mdefstring);

        args.parse("something", "else");
        assertFalse(bool.getValue(), "Default boolean should be false");
        assertNull(string.getValue(), "Default String should be null");
        assertEquals("hey", defstring.getValue(), "Default String with default,");
        assertEquals((Integer) 0, mbool.getValue(), "Default multi bool,");
        assertTrue(mstring.getValue().isEmpty(), "Default multi String should be empty");
        assertTrue(mdefstring.getValue().size() == 2 && mdefstring.getValue().contains("hello") && mdefstring.getValue().contains("hi"), "Default multi String with default should contain 'hello' and 'hi'");

        args.parse("something", "-b", "else", "-B", "-B", "-s", "one", "-d", "two", "-S", "S1", "-S", "S2", "-D", "D1");
        assertTrue(bool.getValue(), "Boolean value defined and should be true");
        assertEquals((Integer) 2, mbool.getValue(), "Multi bool value appeared twice,");
        assertEquals("one", string.getValue(), "String argument,");
        assertEquals("two", defstring.getValue(), "Default string argument,");
        assertEquals(2, mstring.getValue().size(), "Multy String size,");
        assertEquals(3, mdefstring.getValue().size(), "Default multy String size,");

        args.parse("-B", "-s", "three", "-S", "S3", "-S", "S4", "-D", "D2", "-D", "D3");
        assertEquals("three", string.getValue(), "String argument,");
        assertEquals(4, mstring.getValue().size(), "Multy String size,");
        assertEquals(5, mdefstring.getValue().size(), "Default multy String size,");
        assertEquals("[S1, S2, S3, S4]", mstring.getValue().toString(), "Multi String contents");
        assertEquals("[hello, hi, D1, D2, D3]", mdefstring.getValue().toString(), "Default multi String contents");
    }

    @Test
    public void testBooleanInverse() {
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("hello");
            assertFalse(t.getValue());
            assertTrue(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("hello");
            assertTrue(t.getValue());
            assertFalse(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-p");
            assertTrue(t.getValue());
            assertFalse(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-n");
            assertFalse(t.getValue());
            assertTrue(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-p", "-n");
            assertFalse(t.getValue());
            assertTrue(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-n", "-p");
            assertTrue(t.getValue());
            assertFalse(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-p", "-n");
            assertFalse(t.getValue());
            assertTrue(t.getInverse().getValue());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args("myapp", "").def("-p", t).def("-n", t.getInverse()).parse("-n", "-p");
            assertTrue(t.getValue());
            assertFalse(t.getInverse().getValue());
        }
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

        Args args = new Args("myapp", "")
                .def("-h", () -> help.set(true))
                .def("--multi", () -> multi.addAndGet(1))
                .multi("--multi")
                .alias("-h", "--help")
                .def("-o", output::set)
                .req("-o")
                .alias("-o", "--output")
                .def("--run", () -> run.set(true))
                .uniq("-o", "--run")
                .error(error::set);

        args.parse((String[]) null);
        args.parse();

        args.parse("-h");
        assertTrue(help.get(), "Help argument not found");
        System.out.println(error.get());
        assertNotNull("Should fail with missing argument", error.get());

        reset(help, run, output, error, multi);
        args.parse("-o", "something", "--run");
        assertTrue(error.get() != null && error.get().contains("uniq"), "Wrong type of error, expected \"uniq\"");
        assertTrue(error.get().contains("[--output, --run]"), "Error should contain '[--output, --run]' ");

        reset(help, run, output, error, multi);
        rest2 = args.parse("-h", "-h", "unknown", "--help");
        assertFalse(rest2.isEmpty(), "Should fail with rest argument non existent");
        assertEquals(1, rest2.size(), "Size of missing arguments does not match");
        assertEquals("unknown", rest2.get(0), "Missing argument does not match");

        reset(help, run, output, error, multi);
        args.parse("-h", "-o", "some", "--multi", "--multi");
        assertNull(error.get(), "No errors should be found");
        assertEquals("some", output.get(), "Output should be parsed");
        assertEquals(2, multi.get(), "Multiple parameter should be called twice");

        reset(help, run, output, error, multi);
        args.parse("-h", "-h", "-o");
        assertTrue(error.get() != null && error.get().contains("few"), "Wrong type of error, expected \"Too few arguments\"");
    }

    private void reset(AtomicBoolean help, AtomicBoolean run, AtomicReference<String> output, AtomicReference<String> error, AtomicInteger multi) {
        help.set(false);
        run.set(false);
        output.set(null);
        error.set(null);
        multi.set(0);
    }
}
