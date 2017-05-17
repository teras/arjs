/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.argparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author teras
 */
public class Args {

    private final Map<String, Consumer<String>> defs = new HashMap<>();
    private final Map<String, String> info = new HashMap<>();
    private final Set<String> transitive = new HashSet<>();
    private final List<Set<Consumer<String>>> required = new ArrayList<>();
    private final List<Set<Consumer<String>>> unique = new ArrayList<>();
    private Consumer<List<String>> others = t -> {
    };
    private Consumer<String> error = err -> {
        throw new ArgumentException(err);
    };

    public Args def(String arg, BaseArg<?> result) {
        return def(arg, result::set, result.isTransitive());
    }

    public Args def(String arg, Consumer<String> result) {
        return def(arg, result == null ? t -> {
        } : result, true);
    }

    public Args def(String arg, Runnable found) {
        return def(arg, t -> {
            if (found != null)
                found.run();
        }, false);
    }

    private Args def(String arg, Consumer<String> result, boolean isTransitive) {
        arg = checkNotExists(arg);
        if (isTransitive)
            transitive.add(arg);
        defs.put(arg, result);
        return this;
    }

    public Args alias(String source, String dest) {
        source = checkExist(source);
        dest = checkNotExists(dest);
        defs.put(dest, defs.get(source));
        return this;
    }

    public Args info(String arg, String info) {
        arg = checkExist(arg);
        this.info.put(arg, info);
        return this;
    }

    public Args req(String... req) {
        return sets(req, required, "requirement");
    }

    public Args uniq(String... uniq) {
        return sets(uniq, unique, "uniquement");
    }

    private Args sets(String[] args, List<Set<Consumer<String>>> list, String type) {
        Set<Consumer<String>> items = new LinkedHashSet<>();
        for (String each : args)
            items.add(defs.get(checkExist(each)));
        if (items.size() < 1)
            throw new ArgumentException("Too few arguments are defined for " + type);
        list.add(items);
        return this;
    }

    public Args rest(Consumer<List<String>> others) {
        this.others = others == null ? t -> {
        } : others;
        return this;
    }

    public Args error(Consumer<String> error) {
        this.error = error == null ? t -> {
        } : error;
        return this;
    }

    public void parse(String... args) {
        if (args == null || args.length == 0)
            return;
        List<String> rest = new ArrayList<>();
        Set<Consumer<String>> found = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            Consumer<String> cons = defs.get(arg);
            if (cons != null) {
                found.add(cons);
                if (transitive.contains(arg)) {
                    i++;
                    if (i >= args.length) {
                        if (error != null)
                            error.accept("Too few arguments: unable to find value of argument " + arg);
                        break;
                    }
                    arg = args[i];
                }
                cons.accept(arg);
            } else
                rest.add(arg);
        }
        // Check Required
        for (Set<Consumer<String>> group : required)
            if (getCommon(group, found).isEmpty())
                error.accept("At least one of arguments " + getKeys(group) + " are required but none found");

        // Check Unique
        for (Set<Consumer<String>> group : unique) {
            Collection<Consumer<String>> list = getCommon(group, found);
            if (list.size() > 1)
                error.accept("Arguments " + getKeys(list) + " are unique and mutually exclusive");
        }

        if (!rest.isEmpty())
            others.accept(rest);
    }

    private String checkNotExists(String arg) throws ArgumentException {
        arg = checkValid(arg);
        if (defs.containsKey(arg))
            throw new ArgumentException("Argument " + arg + " already defined");
        return arg;
    }

    private String checkExist(String arg) throws ArgumentException {
        arg = checkValid(arg);
        if (!defs.containsKey(arg))
            throw new ArgumentException("Argument " + arg + " not found");
        return arg;
    }

    private String checkValid(String arg) throws ArgumentException {
        if (arg == null)
            arg = "";
        arg = arg.trim();
        if (arg.isEmpty())
            throw new ArgumentException("Null argument provided");
        if (arg.contains(" "))
            throw new ArgumentException("Argument could not contain space character");
        return arg;
    }

    private Collection<Consumer<String>> getCommon(Collection<Consumer<String>> group1, Collection<Consumer<String>> group2) {
        Collection<Consumer<String>> list = new ArrayList<>();
        for (Consumer<String> item : group1)
            if (group2.contains(item))
                list.add(item);
        return list;
    }

    private String getKeys(Consumer<String> cons) {
        StringBuilder out = new StringBuilder();
        for (String arg : defs.keySet())
            if (defs.get(arg) == cons)
                out.append("|").append(arg);
        return out.substring(1);
    }

    private String getKeys(Collection<Consumer<String>> col) {
        StringBuilder out = new StringBuilder();
        for (Consumer<String> cons : col)
            out.append(", ").append(getKeys(cons));
        return out.substring(2);
    }
}
