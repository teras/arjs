/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.argparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author teras
 */
public class Args {

    private final static String NL = System.getProperty("line.separator", "\n");
    private final static String INSET = "  ";
    private final ArgResult HELP = t -> {
        System.out.print(toString());
        System.exit(0);
    };

    private final Map<String, ArgResult> defs = new LinkedHashMap<>();
    private final Map<ArgResult, String> info = new HashMap<>();
    private final Map<ArgResult, String> infoname = new HashMap<>();
    private final Set<ArgResult> transitive = new HashSet<>();
    private final Set<ArgResult> multi = new HashSet<>();
    private final Map<ArgResult, Set<ArgResult>> depends = new HashMap<>();
    private final List<Set<ArgResult>> required = new ArrayList<>();
    private final List<Set<ArgResult>> unique = new ArrayList<>();
    private final Set<ArgResult> nullable = new HashSet<>();
    private final Map<String, Set<ArgResult>> groups = new LinkedHashMap<>();
    private final List<List<String>> usages = new ArrayList<>();
    private ArgResult error = err -> {
        throw new ArgumentException(err);
    };
    private boolean supportEqual = false;
    private boolean supportCondenced = false;

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

    public Args defhelp(String... helpargs) {
        for (String arg : helpargs)
            defs.put(checkNotExists(arg), HELP);
        info.put(HELP, "application usage, this text");
        return this;
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
        return info(arg, info, null);
    }

    public Args info(String arg, String info, String argumentName) {
        arg = checkExist(arg);
        ArgResult ares = defs.get(arg);
        if (info != null) {
            info = info.trim();
            if (!info.isEmpty())
                this.info.put(ares, info);
        }
        if (argumentName != null) {
            argumentName = argumentName.trim();
            if (!argumentName.isEmpty()) {
                if (!transitive.contains(ares))
                    throw new ArgumentException("Trying to set argument value name " + argumentName + " to non transitive parameter " + getArg(ares));
                this.infoname.put(ares, argumentName);
            }
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

    public Args nullable(String... nullable) {
        this.nullable.addAll(sets(nullable, 1, "nullable parameters"));
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

    public Args group(String groupname, String... items) {
        if (groupname == null)
            groupname = "";
        groupname = groupname.trim();
        if (groupname.isEmpty())
            throw new ArgumentException("Group name should not be empty");
        groups.put(groupname, sets(items, 1, "grouping"));
        return this;
    }

    public Args setCondenced(boolean supportCondenced) {
        this.supportCondenced = supportCondenced;
        return this;
    }

    public Args setEqualSign(boolean supportEqual) {
        this.supportEqual = supportEqual;
        return this;
    }

    /**
     * usage
     *
     * @param args This is the only situation that an argument might not be
     * valid
     * @return self
     */
    public Args usage(String... args) {
        if (args == null || args.length == 0)
            throw new ArgumentException("Argument usage should have at least one argument");
        List<String> items = new ArrayList<>();
        for (String arg : args) {
            if (arg == null)
                arg = "";
            arg = arg.trim();
            if (arg.isEmpty())
                throw new ArgumentException("Argument usage could not be empty");
            items.add(arg);
        }
        usages.add(items);
        return this;
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
        Iterator<String> iterator = canonicalArgs(args);
        while (iterator.hasNext()) {
            String arg = iterator.next();
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
                    if (!iterator.hasNext()) {
                        error.result("Too few arguments: unable to find value of argument " + arg);
                        break;
                    }
                    arg = iterator.next();
                }
                if (!nullable.contains(cons) && arg.isEmpty())
                    error.result("Parameter " + getArg(cons) + " should not have an empty value");
                cons.result(arg);
            } else
                rest.add(arg);
        }
        // Check Required
        for (Set<ArgResult> group : required)
            if (getCommon(group, found).isEmpty())
                error.result("At least one of argument" + getArgsWithPlural(group) + " are required but none found");

        // Check Unique
        for (Set<ArgResult> group : unique) {
            Collection<ArgResult> list = getCommon(group, found);
            if (list.size() > 1)
                error.result("Argument" + getArgsWithPlural(list) + " are unique and mutually exclusive");
        }
        return rest;
    }

    @Override
    public String toString() {
        if (defs.isEmpty())
            return "[No arguments defined]" + NL;
        StringBuilder out = new StringBuilder();

        if (!usages.isEmpty()) {
            out.append("Usage:").append(NL);
            out.append(usages());
            out.append(NL);
        }

        groupArgs(new LinkedHashSet<>(defs.values()), out);

        if (!required.isEmpty()) {
            out.append(NL);
            for (Set<ArgResult> set : required)
                out.append("One of the argument").append(getArgsWithPlural(set)).append(" is required.").append(NL);
        }

        if (!unique.isEmpty()) {
            out.append(NL);
            for (Set<ArgResult> set : unique)
                out.append("Only one of argument").append(getArgsWithPlural(set)).append(" could be used simultaneously; they are mutually exclusive.").append(NL);
        }

        if (!depends.isEmpty()) {
            out.append(NL);
            for (ArgResult m : depends.keySet())
                out.append("Argument ").append(getArg(m)).append(" depends on the pre-existence of argument").append(getArgsWithPlural(depends.get(m))).append(".").append(NL);
        }

        if (!multi.isEmpty()) {
            out.append(NL);
            for (ArgResult m : multi)
                out.append("Argument ").append(getArg(m)).append(" can be used more than once.").append(NL);
        }

        if (supportCondenced) {
        }
        if (supportEqual) {
        }

        return out.toString();
    }

    private Iterator<String> canonicalArgs(String[] args) {
        List<String> source = Arrays.asList(args);
        Collection<String> argname = defs.keySet();
        Collection<String> trans = getArgsValues(this.transitive);
        if (supportEqual) {
            int eq = 0;
            List<String> result = new ArrayList<>();
            for (String item : source)
                if (item.startsWith("-") && (eq = item.indexOf("=")) > 0 && argname.contains(item.substring(0, eq))) {
                    result.add(item.substring(0, eq));
                    result.add(item.length() > (eq + 1) ? item.substring(eq + 1) : "");
                } else
                    result.add(item);
            source = result;
        }
        if (supportCondenced) {
            List<String> result = new ArrayList<>();
            ListIterator<String> it = source.listIterator();
            while (it.hasNext()) {
                String item = it.next();
                Collection<String> vals = new ArrayList<>();
                boolean correct = true;
                int rollback = 0;
                if (item.startsWith("-") && item.length() > 1 && item.charAt(1) != '-')
                    for (char argC : item.substring(1).toCharArray()) {
                        String arg = "-" + argC;
                        if (!argname.contains(arg)) {
                            correct = false;
                            break;
                        }
                        vals.add(arg);
                        if (trans.contains(arg)) {
                            if (!it.hasNext()) {
                                correct = false;
                                break;
                            }
                            rollback++;
                            vals.add(it.next());
                        }
                    }
                else
                    correct = false;
                if (!correct) {
                    for (int i = 0; i < rollback; i++)
                        it.previous();
                    result.add(item);
                } else
                    result.addAll(vals);
            }
            source = result;
        }
        return source.iterator();
    }

    private void groupArgs(Set<ArgResult> args, StringBuilder out) {
        List<List<String>> lefts = new ArrayList<>();
        List<List<String>> rights = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int max = 0;
        if (groups.isEmpty()) {
            names.add("Arguments");
            max = singleGroupCalc(args, lefts, rights);
        } else {
            Set<ArgResult> missing = new LinkedHashSet<>(defs.values());
            for (String name : groups.keySet()) {
                Set<ArgResult> groupItems = groups.get(name);
                missing.removeAll(groupItems);
                names.add(name);
                max = Math.max(max, singleGroupCalc(groupItems, lefts, rights));
            }
            names.add("Generic arguments");
            max = Math.max(max, singleGroupCalc(missing, lefts, rights));
        }
        max++;
        for (int i = 0; i < names.size(); i++) {
            if (i != 0)
                out.append(NL);
            singleGroupPrint(names.get(i), lefts.get(i), rights.get(i), max, out);
        }
    }

    private int singleGroupCalc(Set<ArgResult> args, List<List<String>> lefts, List<List<String>> rights) {
        int maxsize = 0;
        List<String> leftPart = new ArrayList<>();
        List<String> rightPart = new ArrayList<>();
        lefts.add(leftPart);
        rights.add(rightPart);
        for (ArgResult arg : args) {
            String left = getArgWithParam(arg, true);
            leftPart.add(left);
            String right = info.get(arg);
            rightPart.add(right == null ? "" : ": " + right);
            maxsize = Math.max(maxsize, left.length());
        }
        return maxsize;
    }

    private void singleGroupPrint(String title, List<String> leftPart, List<String> rightPart, int maxsize, StringBuilder out) {
        out.append(title).append(":").append(NL);
        for (int i = 0; i < leftPart.size(); i++)
            out.append(INSET).append(leftPart.get(i)).append(spaces(maxsize - leftPart.get(i).length())).
                    append(rightPart.get(i)).append(NL);
    }

    private String usages() {
        StringBuilder out = new StringBuilder();
        Collection<ArgResult> wrongParams = new LinkedHashSet<>();
        for (List<String> usage : usages) {
            StringBuilder line = new StringBuilder();
            List<ArgResult> trackUnique = new ArrayList<>();
            Set<ArgResult> upToNow = new HashSet<>();
            for (String arg : usage) {
                ArgResult res = defs.get(arg);
                if (res == null) {
                    line.append(" ").append(arg);
                    trackUnique.clear();
                } else {
                    trackUnique.add(res);
                    if (trackUnique.size() > 2)
                        trackUnique.remove(0);

                    Set<ArgResult> reqDeps = depends.get(res);
                    boolean missing = reqDeps != null && !containsAny(reqDeps, upToNow);
                    if (missing)
                        wrongParams.add(res);
                    upToNow.add(res);

                    boolean req = isInCollection(required, res);
                    line.append(isUnique(unique, trackUnique) ? "|" : " ").append(req ? "" : "[").append(getArgWithParam(res, false)).
                            append(req ? "" : "]").append(missing ? "^" : "");
                }
            }
            out.append(INSET).append(line.substring(1)).append(NL);
        }
        if (!wrongParams.isEmpty())
            out.append("Notes:").append(NL).append(INSET).append("^some pre-required arguments of the argument").
                    append(getArgsWithPlural(wrongParams)).append(" have been hidden for clarity").append(NL);
        return out.toString();
    }

    private Set<ArgResult> sets(String[] args, int minimum, String type) {
        Set<ArgResult> items = new LinkedHashSet<>();
        for (String each : args)
            items.add(defs.get(checkExist(each)));
        if (items.size() < minimum)
            throw new ArgumentException("Too few arguments are defined for " + type);
        return items;
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

    private <T> boolean isInCollection(Collection<Set<T>> collection, T arg) {
        for (Set<T> set : collection)
            if (set.contains(arg))
                return true;
        return false;
    }

    private <T> boolean isUnique(Collection<Set<T>> sets, Collection<T> toCheck) {
        if (toCheck.size() < 2)
            return false;
        for (Set<T> group : sets)
            if (getCommon(group, toCheck).size() == 2)
                return true;
        return false;
    }

    private String getArg(ArgResult cons) {
        return getArg(cons, false);
    }

    private String getArg(ArgResult cons, boolean full) {
        StringBuilder name = new StringBuilder();
        for (String arg : defs.keySet())
            if (defs.get(arg) == cons) {
                if (!full) {
                    if (arg.length() <= (name.length() - 1))
                        continue;
                    name.delete(0, name.length());
                }
                name.append("|").append(arg);
            }
        return name.substring(1);
    }

    private String getArgs(Collection<ArgResult> col) {
        if (col.size() == 1)
            return getArg(col.iterator().next());
        StringBuilder out = new StringBuilder();
        for (ArgResult cons : col)
            out.append(", ").append(getArg(cons));
        return "[" + out.substring(2) + "]";
    }

    private String getArgWithParam(ArgResult arg, boolean full) {
        if (!transitive.contains(arg))
            return getArg(arg, full);
        String name = infoname.get(arg);
        if (name == null) {
            name = "";
            for (String argname : defs.keySet())
                if (argname.startsWith("-") && defs.get(argname) == arg) {
                    while (argname.startsWith("-"))
                        argname = argname.substring(1);
                    if (name.length() < argname.length())
                        name = argname;
                }
            name = name.toUpperCase();
            if (name.length() < 3)
                name = "ARG";
        }
        return getArg(arg, full) + " " + name;
    }

    private Collection<String> getArgsValues(Collection<ArgResult> args) {
        Collection<String> res = new LinkedHashSet<>();
        for (String arg : defs.keySet())
            if (args.contains(defs.get(arg)))
                res.add(arg);
        return res;
    }

    private String getArgsWithPlural(Collection<ArgResult> list) {
        StringBuilder out = new StringBuilder();
        if (list.size() != 1)
            out.append("s");
        out.append(" ");
        out.append(getArgs(list));
        return out.toString();
    }

    private String spaces(int i) {
        char[] chars = new char[i];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

}
