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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author teras
 */
public class Args {

    private final Map<String, ArgResult> defs = new LinkedHashMap<>();
    private final Map<ArgResult, String> info = new HashMap<>();
    private final Set<ArgResult> transitive = new HashSet<>();
    private final Set<ArgResult> multi = new HashSet<>();
    private final Map<ArgResult, Set<ArgResult>> depends = new HashMap<>();
    private final List<Set<ArgResult>> required = new ArrayList<>();
    private final List<Set<ArgResult>> unique = new ArrayList<>();
    private ArgResult error = err -> {
        throw new ArgumentException(err);
    };

    public Args def(String arg, BaseArg<?> result) {
        return def(arg, result::set, result.isTransitive());
    }

    public Args def(String arg, ArgResult result) {
        return def(arg, result == null ? t -> {
        } : result, true);
    }

    public Args def(String arg, Runnable found) {
        return def(arg, t -> {
            if (found != null)
                found.run();
        }, false);
    }

    private Args def(String arg, ArgResult result, boolean isTransitive) {
        arg = checkNotExists(arg);
        if (isTransitive)
            transitive.add(result);
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
        if (info != null && !info.trim().isEmpty()) {
            arg = checkExist(arg);
            this.info.put(defs.get(arg), info);
        }
        return this;
    }

    public Args dep(String dependant, String... dependencies) {
        dependant = checkExist(dependant);
        depends.put(defs.get(dependant), sets(dependencies, 1, "dependency"));
        return this;
    }

    public Args req(String... req) {
        required.add(sets(req, 1, "requirement"));
        return this;
    }

    public Args uniq(String... uniq) {
        unique.add(sets(uniq, 2, "uniquement"));
        return this;
    }

    public Args multi(String multi) {
        this.multi.add(defs.get(checkExist(multi)));
        return this;
    }

    private Set<ArgResult> sets(String[] args, int minimum, String type) {
        Set<ArgResult> items = new LinkedHashSet<>();
        for (String each : args)
            items.add(defs.get(checkExist(each)));
        if (items.size() < minimum)
            throw new ArgumentException("Too few arguments are defined for " + type);
        return items;
    }

    public Args error(ArgResult error) {
        this.error = error == null ? t -> {
        } : error;
        return this;
    }

    public List<String> parse(String... args) {
        List<String> rest = new ArrayList<>();
        if (args == null || args.length == 0)
            return rest;
        Set<ArgResult> found = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            ArgResult cons = defs.get(arg);
            if (cons != null) {
                Set<ArgResult> reqDeps = depends.get(cons);
                if (reqDeps != null && !containsAny(reqDeps, found))
                    error.result("Argument " + getArg(cons) + " pre-requires one of missing arguments: " + getArgs(reqDeps));
                if (found.contains(cons)) {
                    if (!multi.contains(cons))
                        error.result("Argument " + getArg(cons) + " should appear only once");
                } else
                    found.add(cons);
                if (transitive.contains(cons)) {
                    i++;
                    if (i >= args.length) {
                        error.result("Too few arguments: unable to find value of argument " + arg);
                        break;
                    }
                    arg = args[i];
                }
                cons.result(arg);
            } else
                rest.add(arg);
        }
        // Check Required
        for (Set<ArgResult> group : required)
            if (getCommon(group, found).isEmpty())
                error.result("At least one of arguments " + getArgs(group) + " are required but none found");

        // Check Unique
        for (Set<ArgResult> group : unique) {
            Collection<ArgResult> list = getCommon(group, found);
            if (list.size() > 1)
                error.result("Arguments " + getArgs(list) + " are unique and mutually exclusive");
        }
        return rest;
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

    private <T> Collection<T> getCommon(Collection<T> group1, Collection<T> group2) {
        Collection<T> list = new ArrayList<>();
        for (T item : group1)
            if (group2.contains(item))
                list.add(item);
        return list;
    }

    private <T> boolean containsAny(Collection<T> base, Collection<T> request) {
        for (T item : request)
            if (base.contains(item))
                return true;
        return false;
    }

    private String getArg(ArgResult cons) {
        StringBuilder out = new StringBuilder();
        for (String arg : defs.keySet())
            if (defs.get(arg) == cons)
                out.append("|").append(arg);
        return out.substring(1);
    }

    private String getArgs(Collection<ArgResult> col) {
        if (col.size() == 1)
            return getArg(col.iterator().next());
        StringBuilder out = new StringBuilder();
        for (ArgResult cons : col)
            out.append(", ").append(getArg(cons));
        return "[" + out.substring(2) + "]";
    }

    @Override
    public String toString() {
        if (defs.isEmpty())
            return "[No arguments defined]";

        StringBuilder out = new StringBuilder();
        String NL = System.getProperty("line.separator", "\n");
        String inset = "  ";

        LinkedHashSet<ArgResult> otherArgs = new LinkedHashSet<>(defs.values());
        otherArgs.removeAll(info.keySet());
        LinkedHashSet<ArgResult> infoArgs = new LinkedHashSet<>(defs.values());
        infoArgs.removeAll(otherArgs);

        out.append("List of arguments:").append(NL);
        for (ArgResult infoed : infoArgs)
            out.append(inset).append(getArgWithParam(infoed)).append(" : ").append(info.get(infoed)).append(NL).append(NL);
        if (!otherArgs.isEmpty()) {
            if (!infoArgs.isEmpty())
                out.append("Other arguments:").append(NL);
            for (ArgResult bare : otherArgs)
                out.append(inset).append(getArgWithParam(bare)).append(NL);
            out.append(NL);
        }

        for (ArgResult m : multi)
            out.append("Argument ").append(getArg(m)).append(" can be used more than once.").append(NL);
        if (!multi.isEmpty())
            out.append(NL);

        for (ArgResult m : depends.keySet())
            out.append("Argument ").append(getArg(m)).append(" depends on the pre-existence of argument").append(getArgsWithPlural(depends.get(m))).append(".").append(NL);
        if (!depends.isEmpty())
            out.append(NL);

        for (Set<ArgResult> set : required)
            out.append("Any one of argument").append(getArgsWithPlural(set)).append(" is required.").append(NL);
        if (!required.isEmpty())
            out.append(NL);

        for (Set<ArgResult> set : unique)
            out.append("Only one of argument").append(getArgsWithPlural(set)).append(" could be used simultaneously; they are mutually exclusive.").append(NL);
        if (!unique.isEmpty())
            out.append(NL);

        return out.toString();
    }

    private String getArgWithParam(ArgResult arg) {
        return getArg(arg) + (transitive.contains(arg) ? " \"argument\"" : "");
    }

    private String getArgsWithPlural(Collection<ArgResult> list) {
        StringBuilder out = new StringBuilder();
        if (list.size() != 1)
            out.append("s");
        out.append(" ");
        out.append(getArgs(list));
        return out.toString();
    }

}
