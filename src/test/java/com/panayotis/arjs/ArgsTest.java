/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

import org.junit.Test;

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
        Args args = new Args();
        args.
                def("/a", mboolA).
                def("/b", mboolB).
                def("/s", mstring);

        args.setCondensed('/');
        args.setJoined(':');
        List<String> free = args.parse("/a", "/aa", "/aaasbbasa", "one", "two", "/s:three", "/b", "/a:another", "/s", "four", "/s:five", "/b");
        assertEquals("Condensed notation error with paramter /a,", (Integer) 8, mboolA.get());
        assertEquals("Condensed notation error with paramter /b,", (Integer) 4, mboolB.get());
        assertEquals("Multi string with condensed mode", "[one, two, three, four, five]", mstring.get().toString());
        assertEquals("A free parameter should exist, since /a is not a transitive parameter,", "[/a:another]", free.toString());
    }

    @Test
    public void testArgs() {
        BoolArg bool = new BoolArg();
        StringArg string = new StringArg();
        StringArg defstring = new StringArg("hey");
        MultiBoolArg mbool = new MultiBoolArg();
        MultiStringArg mstring = new MultiStringArg();
        MultiStringArg mdefstring = new MultiStringArg("hello", "hi");

        Args args = new Args();
        args.
                def("-b", bool).
                def("-s", string).
                def("-d", defstring).
                def("-B", mbool).
                def("-S", mstring).
                def("-D", mdefstring);

        args.parse("something", "else");
        assertFalse("Default boolean should be false", bool.get());
        assertNull("Default String should be null", string.get());
        assertEquals("Default String with default,", "hey", defstring.get());
        assertEquals("Default multi bool,", (Integer) 0, mbool.get());
        assertTrue("Default multi String should be empty", mstring.get().isEmpty());
        assertTrue("Default multi String with default should contain 'hello' and 'hi'", mdefstring.get().size() == 2 && mdefstring.get().contains("hello") && mdefstring.get().contains("hi"));

        args.parse("something", "-b", "else", "-B", "-B", "-s", "one", "-d", "two", "-S", "S1", "-S", "S2", "-D", "D1");
        assertTrue("Boolean value defined and should be true", bool.get());
        assertEquals("Multi bool value appeared twice,", (Integer) 2, mbool.get());
        assertEquals("String argument,", "one", string.get());
        assertEquals("Default string argument,", "two", defstring.get());
        assertEquals("Multy String size,", 2, mstring.get().size());
        assertEquals("Default multy String size,", 3, mdefstring.get().size());

        args.parse("-B", "-s", "three", "-S", "S3", "-S", "S4", "-D", "D2", "-D", "D3");
        assertEquals("String argument,", "three", string.get());
        assertEquals("Multy String size,", 4, mstring.get().size());
        assertEquals("Default multy String size,", 5, mdefstring.get().size());
        assertEquals("Multi String contents", "[S1, S2, S3, S4]", mstring.get().toString());
        assertEquals("Default multi String contents", "[hello, hi, D1, D2, D3]", mdefstring.get().toString());
    }

    @Test
    public void testBooleanInverse() {
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args().def("-p", t).def("-n", t.getInverse()).parse("hello");
            assertFalse(t.get());
            assertTrue(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args().def("-p", t).def("-n", t.getInverse()).parse("hello");
            assertTrue(t.get());
            assertFalse(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-p");
            assertTrue(t.get());
            assertFalse(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-n");
            assertFalse(t.get());
            assertTrue(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-p", "-n");
            assertFalse(t.get());
            assertTrue(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg();
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-n", "-p");
            assertTrue(t.get());
            assertFalse(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-p", "-n");
            assertFalse(t.get());
            assertTrue(t.getInverse().get());
        }
        {
            BoolExclusiveArg t = new BoolExclusiveArg(true);
            new Args().def("-p", t).def("-n", t.getInverse()).parse("-n", "-p");
            assertTrue(t.get());
            assertFalse(t.getInverse().get());
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

        Args args = new Args()
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
        assertTrue("Help argument not found", help.get());
        System.out.println(error.get());
        assertNotNull("Should fail with missing argument", error.get());

        reset(help, run, output, error, multi);
        args.parse("-o", "something", "--run");
        assertTrue("Wrong type of error, expected \"uniq\"", error.get() != null && error.get().contains("uniq"));
        assertTrue("Error should contain '[--output, --run]' ", error.get().contains("[--output, --run]"));

        reset(help, run, output, error, multi);
        rest2 = args.parse("-h", "-h", "unknown", "--help");
        assertFalse("Should fail with rest argument non existent", rest2.isEmpty());
        assertEquals("Size of missing arguments does not match", 1, rest2.size());
        assertEquals("Missing argument does not match", "unknown", rest2.get(0));

        reset(help, run, output, error, multi);
        args.parse("-h", "-o", "some", "--multi", "--multi");
        assertNull("No errors should be found", error.get());
        assertEquals("Output should be parsed", "some", output.get());
        assertEquals("Multiple parameter should be called twice", 2, multi.get());

        reset(help, run, output, error, multi);
        args.parse("-h", "-h", "-o");
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
