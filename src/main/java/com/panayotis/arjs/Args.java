/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import com.panayotis.jerminal.Jerminal;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static com.panayotis.arjs.CollectionUtils.*;
import static com.panayotis.arjs.ErrorStrategy.THROW_EXCEPTION;
import static com.panayotis.arjs.HelpUtils.combine;
import static com.panayotis.arjs.HelpUtils.spaces;
import static java.util.Objects.requireNonNull;

/**
 * @author teras
 */
public class Args {

    private static final String NL = System.getProperty("line.separator", "\n");
    private static final String INSET = "  ";
    private static final int LINELENGTH = Jerminal.getWidth();

    private final Map<String, ArgResult> defs = new LinkedHashMap<>();
    private final Map<ArgResult, String> info = new LinkedHashMap<>();
    private final Map<ArgResult, String> infoname = new LinkedHashMap<>();
    private final Set<ArgResult> transitive = new LinkedHashSet<>();
    private final Set<ArgResult> multi = new LinkedHashSet<>();
    private final Set<ArgResult> passthrough = new LinkedHashSet<>();
    private final Map<ArgResult, Set<ArgResult>> depends = new LinkedHashMap<>();
    private final Map<ArgResult, Set<ArgResult>> softdepends = new LinkedHashMap<>();
    private final List<Set<ArgResult>> required = new ArrayList<>();
    private final List<Set<ArgResult>> unique = new ArrayList<>();
    private final Map<String, Collection<String>> groups = new LinkedHashMap<>();
    private final Collection<String> helpArgs = new HashSet<>();
    private final String name;
    private final String description;
    private ArgResult errorArg;
    private ErrorStrategy errorStrategy;
    private char joinedChar = '\0';
    private char condensedChar = '\0';
    private boolean namesAsGiven = false;
    private String[] freeArguments = null; // the free arguments can be as many as we want by default
    private String execName;
    private boolean collapseCommon = false;

    public Args(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Define a new parameter
     *
     * @param arg    The name of the parameter
     * @param result A build-in parameter handler, which directly stores the
     *               value to the desired variable.
     * @return Self reference
     */
    @Nonnull
    public Args def(@Nonnull String arg, BaseArg<?> result) {
        return def(arg, result == null ? t -> {
        } : result::set, result instanceof TransitiveArg, result instanceof MultiArg);
    }


    /**
     * Define a new parameter
     *
     * @param arg    The name of the parameter
     * @param result A build-in parameter handler, which directly stores the
     *               value to the desired variable.
     * @param info   The information to display for this parameter
     * @return Self reference
     */
    @Nonnull
    public Args def(@Nonnull String arg, BaseArg<?> result, String info) {
        def(arg, result);
        return info(arg, info);
    }

    /**
     * Define a new parameter
     *
     * @param arg    The name of the parameter
     * @param result A general purpose handler for the specific parameter. The
     *               value of the parameter will be brought as an argument back here. Note
     *               this is a transitive parameter, i.e. a value after this parameter will
     *               always be required.
     * @return Self reference
     */
    @Nonnull
    public Args def(@Nonnull String arg, ArgResult result) {
        return def(arg, result == null ? t -> {
        } : result, true, false);
    }

    /**
     * Define a new parameter
     *
     * @param arg    The name of the parameter
     * @param result A general purpose handler for the specific parameter. The
     *               value of the parameter will be brought as an argument back here.
     * @param info   The information to display for this parameter
     * @return Self reference
     */
    public Args def(@Nonnull String arg, ArgResult result, String info) {
        def(arg, result);
        return info(arg, info);
    }

    /**
     * Define a new parameter
     *
     * @param arg   The name of the parameter
     * @param found A general purpose non-transitive handler for the specific
     *              parameter. Note that no parameter value will be required, thus no input
     *              value will be provided.
     * @return Self reference
     */
    @Nonnull
    public Args def(@Nonnull String arg, Runnable found) {
        return def(arg, t -> {
            if (found != null)
                found.run();
        }, false, false);
    }

    /**
     * Define a new parameter
     *
     * @param arg   The name of the parameter
     * @param found A general purpose non-transitive handler for the specific
     *              parameter. Note that no parameter value will be required, thus no input
     *              value will be provided.
     * @param info  The information to display for this parameter
     * @return Self reference
     */
    public Args def(@Nonnull String arg, Runnable found, String info) {
        def(arg, found);
        return info(arg, info);
    }

    private Args def(String arg, ArgResult result, boolean isTransitive, boolean isMultiArg) {
        arg = checkNotExists(arg);
        if (isTransitive)
            transitive.add(result);
        if (isMultiArg)
            multi.add(result);
        defs.put(arg, result);
        return this;
    }

    /**
     * Define a default help parameter
     *
     * @param helpargs List of parameters that will be used as help parameters
     * @return Self reference
     */
    @Nonnull
    public Args defhelp(@Nonnull String... helpargs) {
        for (String arg : helpargs) {
            checkNotExists(arg);
            helpArgs.add(arg);
        }
        return this;
    }

    /**
     * Define a new alias for a command
     *
     * @param original The original parameter reference
     * @param alias    The new parameter
     * @return Self reference
     */
    @Nonnull
    public Args alias(@Nonnull String original, @Nonnull String alias) {
        original = checkExist(original);
        alias = checkNotExists(alias);
        defs.put(alias, defs.get(original));
        return this;
    }

    /**
     * Provide information about a parameter
     *
     * @param arg  The name of the parameter
     * @param info The information to display for this parameter
     * @return Self reference
     */
    @Nonnull
    public Args info(@Nonnull String arg, @Nonnull String info) {
        return info(arg, info, null);
    }

    /**
     * Provide information about a parameter. This method has meaning only for
     * transitive parameters.
     *
     * @param arg          The name of the parameter
     * @param info         The information to display for this parameter
     * @param argumentName The name of the value, as displayed in help messages.
     *                     By default the name is upper-case the name of the longest parameter
     *                     itself, or ARG, if the name is too small.
     * @return Self reference
     */
    @Nonnull
    public Args info(@Nonnull String arg, String info, String argumentName) {
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

    /**
     * Define a strong dependency for this parameter. The dependency parameter should
     * already have been provided, for the option to ba valid.
     *
     * @param dependant    The name of the dependant parameter
     * @param dependencies The list of hard dependencies of this dependant parameter
     * @return Self reference
     * @see #depsoft(String, String...)
     */
    @Nonnull
    public Args dep(@Nonnull String dependant, @Nonnull String... dependencies) {
        dependant = checkExist(dependant);
        depends.put(defs.get(dependant), sets(dependencies, 1, "dependency"));
        return this;
    }

    /**
     * Define a soft dependency for this parameter. Although to use this parameter the
     * dependency is required, the definition of the dependency can follow the definition
     * of this parameter.
     *
     * @param dependant    The name of the dependant parameter
     * @param dependencies The list of soft dependencies of this dependant parameter
     * @return Self reference
     */
    @Nonnull
    public Args depsoft(@Nonnull String dependant, @Nonnull String... dependencies) {
        dependant = checkExist(dependant);
        softdepends.put(defs.get(dependant), sets(dependencies, 1, "dependency"));
        return this;
    }

    /**
     * Define a list of parameters as required.
     *
     * @param req The list of required parameters. This list could contain even
     *            only one parameter. If a required parameter could not be provided due to
     *            dependencies, then this parameter is not counted as a required parameter.
     *            This is to help complex dependencies and requirements, when one parameter
     *            is required only under specific conditions.
     * @return Self reference
     */
    @Nonnull
    public Args req(@Nonnull String... req) {
        required.add(sets(req, 1, "requirement"));
        return this;
    }

    /**
     * Only one of the items in this list could be used simultaneously.
     *
     * @param uniq A list of unique parameters. SHould be at least two.
     * @return Self reference
     */
    @Nonnull
    public Args uniq(@Nonnull String... uniq) {
        unique.add(sets(uniq, 2, "uniquement"));
        return this;
    }

    /**
     * List of parameters that could be used more than once. By default a
     * parameter could be used at most once, and an error is thrown if it is
     * used more than once. With this option the defined parameters are allowed
     * to be called more than once.
     *
     * @param multi List of parameters that could be called more than once.
     * @return Self reference
     */
    @Nonnull
    public Args multi(@Nonnull String... multi) {
        this.multi.addAll(sets(multi, 1, "multi parameters"));
        return this;
    }

    /**
     * List of parameters that should be passed through to the remaining
     * arguments. These arguments will be properly processed, but instead of
     * consumed, will be passed through as if the system didn't recognize them.
     * <p>
     * This option is useful if you want some parameters to not disappear but
     * appear instead for post-processing of some sort.
     *
     * @param passthrough The pass-through parameters
     * @return Self reference
     */
    @Nonnull
    public Args passthrough(@Nonnull String... passthrough) {
        this.passthrough.addAll(sets(passthrough, 1, "passthrough parameters"));
        return this;
    }

    /**
     * Define a group of parameters. The group is characterized by a name, and a
     * set of parameters. The group name is used as the first argument of the application.
     * If a parameter is not defined in any group, then it is considered as a generic
     * parameter, and can be used in any group. Otherwise, a parameter cannot be used outside
     * the group it is defined in.
     *
     * @param groupname The name of the group.
     * @param items     The list of the grouped parameters. At least one parameter
     *                  is needed.
     * @return Self reference
     */
    @Nonnull
    public Args group(String groupname, @Nonnull String... items) {
        if (groupname == null)
            groupname = "";
        groupname = groupname.trim();
        if (groupname.isEmpty())
            throw new ArgumentException("Group name should not be empty");
        if (groups.containsKey(groupname))
            throw new ArgumentException("Group " + groupname + " already defined");
        if (items.length == 0)
            throw new ArgumentException("Group " + groupname + " should contain at least one parameter");
        for (String item : items)
            checkExist(item);
        groups.put(groupname, Arrays.asList(items));
        return this;
    }

    /**
     * Turn on condensed mode. By default all parameters are exactly one word
     * and they are always separated by spaces. With condensed mode, single
     * letter parameters, that are prefixed with the condensed parameters, could
     * be grouped together with no space between them. Transitive parameters
     * will use the next available argument as input. If more than one
     * transitive parameters are grouped, then the corresponding parameters that
     * follow will be used as input.
     * <br>
     * As an example, let's say {@code -b} is a valid non transient parameter
     * and {@code -s} is a valid transient parameter, with the minus sign as the
     * condensed character. Then this is a valid sequence of parameters:
     * {@code -bbsbsb hello world} , which practically could be understood as
     * this series of parameters: {@code -b -b -s hello -b -s world -b}
     *
     * @param condensedChar The condensed prefix character, usually the minus
     *                      sign, '-'
     * @return Self reference
     */
    @Nonnull
    public Args setCondensed(char condensedChar) {
        this.condensedChar = condensedChar;
        return this;
    }

    /**
     * Turn on joined notation. By default transitive parameters are separated
     * by space with their corresponding value. By turning on joined notation,
     * then a transient parameter is allowed to accept its value just after the
     * joined character, i.e. the parameter and its value is separated by one
     * instance of the joined parameter.
     * <br>
     * For instance, if the equal sign is the joined character and a valid
     * parameter {@code --param} exists, then the expression
     * {@code --param value} could be also written as {@code --param=value}
     *
     * @param joinedChar The joined character, usually an equal sign '='
     * @return Self reference
     */
    @Nonnull
    public Args setJoined(char joinedChar) {
        this.joinedChar = joinedChar;
        return this;
    }

    /**
     * Set the name of the executable. This is used in the help text to display
     * the name of the executable. By default, the name of the executable is
     * taken from application name.
     *
     * @param execName The name of the executable
     * @return Self reference
     */
    @Nonnull
    public Args execName(@Nonnull String execName) {
        requireNonNull(execName, "Executable name should not be null");
        this.execName = execName;
        return this;
    }

    /**
     * Error callback. By default the system throws an error. By using a custom
     * callback we can override this behavior.
     *
     * @param error The callback to use
     * @return Self reference
     */
    @Nonnull
    public Args error(ArgResult error) {
        this.errorArg = error;
        this.errorStrategy = null;
        return this;
    }

    @Nonnull
    public Args error(ErrorStrategy strategy) {
        this.errorArg = null;
        this.errorStrategy = strategy;
        return this;
    }


    @Nonnull
    public Args freeArgs(String... freeArgs) {
        for (String arg : freeArgs)
            if (arg == null)
                throw new NullPointerException("Free argument name cannot be null");
            else if (arg.trim().isEmpty())
                throw new ArgumentException("Free argument name cannot be empty");
        this.freeArguments = freeArgs;
        return this;
    }

    /**
     * When displaying the help message, collapse common parameters. By default all properties are displayed.
     */
    public Args collapseCommon() {
        collapseCommon = true;
        return this;
    }

    /**
     * Whether to display biggest name or first name of an argument.
     * By default, the biggest name of an argument is displayed when parsing arguments.
     * With this option it is possible to change this and display the first defined property instead.
     *
     * @param asGiven false, to display the biggest argument name, true to display the first. Defaults to false
     * @return Self reference
     */
    @Nonnull
    public Args setNamesAsGiven(boolean asGiven) {
        namesAsGiven = asGiven;
        return this;
    }

    /**
     * Parse command line arguments.
     *
     * @param args The given command line arguments
     * @return A list of arguments not belonging to any defined argument, i.e.
     * free arguments.
     */
    public List<String> parse(String... args) {
        return parse(null, args);
    }

    /**
     * Parse command line arguments.
     *
     * @param argumentChecker A callback to check the validity of the arguments. Could be null.
     * @param args            The given command line arguments
     * @return A list of arguments not belonging to any defined argument, i.e.
     * free arguments.
     */
    @Nonnull
    public List<String> parse(Supplier<Boolean> argumentChecker, String... args) {
        List<String> rest = new ArrayList<>();
        Set<ArgResult> found = new LinkedHashSet<>();
        String groupName = null;
        List<String> cArgs = new ArrayList<>(canonicalArgs(args));
        if (cArgs.removeAll(helpArgs)) {
            // A help argument is found
            if (!cArgs.isEmpty() && !groups.isEmpty()) {
                groupName = cArgs.get(0);
                if (!groups.containsKey(groupName))
                    execError("The subcommand " + cArgs.get(0) + " is not defined", null);
            }
            execHelp(groupName);
        }
        Iterator<String> iterator = cArgs.iterator();

        Map<String, ArgResult> argPool;
        // First find the group, if any
        if (!groups.isEmpty()) {
            if (!iterator.hasNext())
                execError("The first argument should be the name of the subcommand", null);
            groupName = iterator.next();
            if (!groups.containsKey(groupName))
                execError("The subcommand " + groupName + " is not defined", null);
            // Group and common parameters are allowed
            argPool = findMyNamedGroupArgs(groupName);
            argPool.putAll(findRemainingNamedGroupArgs());
        } else
            argPool = defs;
        while (iterator.hasNext()) {
            String arg = iterator.next();
            ArgResult cons = argPool.get(arg);
            if (cons != null) {
                Set<ArgResult> reqDeps = depends.get(cons);
                if (reqDeps != null && !containsAny(reqDeps, found))
                    execError("Argument " + getArg(cons) + " pre-requires one of missing arguments: " + getArgs(reqDeps), groupName);
                if (found.contains(cons)) {
                    if (!multi.contains(cons))
                        execError("Argument " + getArg(cons) + " should appear only once", groupName);
                } else
                    found.add(cons);
                if (passthrough.contains(cons))
                    rest.add(arg);
                if (transitive.contains(cons)) {
                    if (!iterator.hasNext()) {
                        execError("Too few arguments: unable to find value of argument " + arg, groupName);
                        break;
                    }
                    arg = iterator.next();
                }
                try {
                    cons.result(arg);
                } catch (Exception ex) {
                    execError("Invalid parameter '" + getArg(cons) + "' using value '" + arg + "': " + ex.getMessage(), groupName);
                }
            } else
                rest.add(arg);
        }

        // Check soft dependencies
        for (ArgResult cons : found) {
            Set<ArgResult> softDeps = softdepends.get(cons);
            if (softDeps != null && !containsAny(softDeps, found))
                execError("Argument " + getArg(cons) + " requires one of missing arguments: " + getArgs(softDeps), groupName);
        }

        // Check Required
        for (Set<ArgResult> items : required)
            if (areArgsMissing(items, found))
                if (items.size() == 1)
                    execError("Argument " + getArg(items.iterator().next()) + " is required but not found", groupName);
                else
                    execError("At least one of arguments " + getArgs(items) + " are required but none found", groupName);

        // Check Unique
        for (Set<ArgResult> items : unique) {
            Collection<ArgResult> list = getCommon(items, found);
            if (list.size() > 1)
                execError("Argument" + getArgsWithPlural(list) + " are unique and mutually exclusive", groupName);
        }

        // Check how many free arguments are allowed
        if (freeArguments != null && rest.size() != freeArguments.length)
            execError("The number of free arguments required are different than supported. Supported:" + freeArguments.length + " Found:" + rest.size(), groupName);
        if (argumentChecker != null && !argumentChecker.get())
            execError("Invalid arguments found", groupName);
        return rest;
    }

    private boolean areArgsMissing(Collection<ArgResult> required, Collection<ArgResult> found) {
        for (ArgResult req : required) {
            if (found.contains(req))    // found one of the requirements
                return false;
            // Still missing, but maybe it is missing due to dependencies.
            Collection<ArgResult> allDependencies = combine(depends.get(req), softdepends.get(req));   // get all dependencies
            if (!allDependencies.isEmpty()   // indeed, it has a dependency
                    && getCommon(allDependencies, found).isEmpty())     // the dependency is missing
                return false;      // not really missing since the requirements are not fulfilled
        }
        return true;   // none of the possible instances of this requirement could be fulfilled
    }

    private String usageAsString(String group) {
        StringBuilder out = new StringBuilder();
        out.append(Jerminal.getEmph(name)).append("  ").append(description).append(NL);
        if (defs.isEmpty())
            return out.toString();
        out.append(NL);
        Collection<ArgResult> remainingArgs = findRemainingArgs();
        getUsage(out, group, remainingArgs);
        groupArgs(out, group, remainingArgs);

        StringBuilder outInfo = new StringBuilder();
        if (!required.isEmpty())
            for (Set<ArgResult> set : required)
                print(outInfo, (set.size() == 1 ? "Argument" : "One of the argument") + getArgsWithPlural(set) + " is required.");

        if (!unique.isEmpty())
            for (Set<ArgResult> set : unique)
                print(outInfo, "Only one of argument" + getArgsWithPlural(set) + " could be used simultaneously; they are mutually exclusive.");

        printDependencies(outInfo, depends, true);
        printDependencies(outInfo, softdepends, true);

        if (!multi.isEmpty())
            for (ArgResult m : multi)
                print(outInfo, "Argument " + getArg(m) + " can be used more than once.");

        if (outInfo.length() > 0) {
            out.append(NL);
            out.append(outInfo);
        }

        if (condensedChar != '\0') {
            List<String> single = new ArrayList<>();
            List<Object> trans = new ArrayList<>();
            int idx = 1;
            for (String key : defs.keySet())
                if (key.length() == 2 && key.charAt(0) == condensedChar) {
                    ArgResult arg = defs.get(key);
                    if (multi.contains(defs.get(key))) {
                        single.add(key.substring(1));
                        trans.add(transitive.contains(arg) ? " ARG" + (idx++) : "");
                    }
                    single.add(key.substring(1));
                    trans.add(transitive.contains(arg) ? " ARG" + (idx++) : "");
                    if (single.size() > 1)
                        break;
                }
            if (single.size() > 1) {    // Parameters permit it
                out.append(NL);
                print(out, "Single letter arguments that start with the character `" + condensedChar
                        + "` could be grouped together. For example the '" + condensedChar
                        + single.get(0) + trans.get(0) + " " + condensedChar + single.get(1) + trans.get(1)
                        + "' arguments could be written as '" + condensedChar
                        + single.get(0) + single.get(1) + trans.get(0) + trans.get(1) + "'.");
            }
        }

        if (joinedChar != '\0' && !transitive.isEmpty()) {
            out.append(NL);
            String param = getArg(transitive.iterator().next());
            print(out, "Arguments with values are allowed to use the `" + joinedChar
                    + "` character to assign their value. For example the '" + param
                    + " ARG1' argument could be written as '" + param + joinedChar + "ARG'.");
        }

        return out.toString();
    }

    private void printDependencies(StringBuilder out, Map<ArgResult, Set<ArgResult>> cdeps, boolean strong) {
        if (!cdeps.isEmpty()) {
            Map<Set<ArgResult>, Collection<ArgResult>> depmap = new LinkedHashMap<>();    // dependencies, dependents
            for (ArgResult m : cdeps.keySet()) {
                // Reconstruct multiple dependencies into groups of the same type
                Set<ArgResult> dependencies = cdeps.get(m);
                depmap.computeIfAbsent(dependencies, k -> new LinkedHashSet<>()).add(m);
            }
            for (Set<ArgResult> dependencies : depmap.keySet()) {
                Collection<ArgResult> dependents = depmap.get(dependencies);
                print(out, (dependents.size() == 1
                        ? "Argument " + getArg(dependents.iterator().next())
                        : "Each one of arguments " + getArgs(dependents))
                        + " depends on the " + (strong ? "pre-" : "") + "existence of argument" + getArgsWithPlural(dependencies) + ".");
            }
        }
    }

    private void print(StringBuilder out, String message) {
        print(out, message, "", "");
    }

    private void print(StringBuilder out, String message, String firstInset, String allInsets) {
        String inset = firstInset;
        for (String s : HelpUtils.split(message, false, LINELENGTH - firstInset.length(), LINELENGTH - allInsets.length())) {
            out.append(inset).append(s).append(NL);
            inset = allInsets;
        }
    }

    private List<String> canonicalArgs(String[] args) {
        List<String> source = args == null ? Collections.emptyList() : Arrays.asList(args);
        Collection<String> argname = defs.keySet();
        Collection<String> trans = getArgValues(this.transitive);
        if (joinedChar != '\0') {
            int eq;
            List<String> result = new ArrayList<>();
            for (String item : source)
                if ((eq = item.indexOf(joinedChar)) > 0
                        && argname.contains(item.substring(0, eq))
                        && transitive.contains(defs.get(item.substring(0, eq)))) {
                    result.add(item.substring(0, eq));
                    result.add(item.length() > (eq + 1) ? item.substring(eq + 1) : "");
                } else
                    result.add(item);
            source = result;
        }
        if (condensedChar != '\0') {
            List<String> result = new ArrayList<>();
            ListIterator<String> it = source.listIterator();
            while (it.hasNext()) {
                String item = it.next();
                Collection<String> vals = new ArrayList<>();
                boolean correct = true;
                int rollback = 0;
                if (item.length() > 2 && item.charAt(0) == condensedChar && item.charAt(1) != condensedChar)
                    for (char argC : item.substring(1).toCharArray()) {
                        String arg = condensedChar + "" + argC;
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
        return source;
    }

    private Map<String, ArgResult> findMyNamedGroupArgs(String groupName) {
        if (groups.isEmpty())
            return defs;
        // It is not enough to filter arguments by their name, since we want all names pointing to
        // a specific argument (even if they are aliases). So we first gather all unique arguments,
        // and then use the arguments as a filter to gather the arguements, even if they have an alias.
        Collection<ArgResult> groupArgs = getValuesByKeys(defs, groups.get(groupName));
        return filterMatchingValues(defs, groupArgs);
    }

    private Collection<ArgResult> findMyArgs(String groupName) {
        if (groups.isEmpty())
            return new LinkedHashSet<>(defs.values());
        return getValuesByKeys(defs, groups.get(groupName));
    }

    private Collection<ArgResult> findRemainingArgs() {
        if (groups.isEmpty())
            return Collections.emptySet();
        Collection<ArgResult> allGroupedArgs = getValuesByKeys(defs, gatherAllValues(groups));
        return findRemainingValues(defs, allGroupedArgs);
    }

    private Map<String, ArgResult> findRemainingNamedGroupArgs() {
        if (groups.isEmpty())
            return Collections.emptyMap();
        // To get all common arguments, first we need to gather all arguments that belong to any group.
        // Then we filter all arguments that do not belong in this group.
        Collection<ArgResult> allGroupedArgs = getValuesByKeys(defs, gatherAllValues(groups));
        return filterNotMatchingValues(defs, allGroupedArgs);
    }

    private void groupArgs(StringBuilder out, String currentGroup, Collection<ArgResult> remainingArgs) {
        Collection<NamedGroupArgs> namedGroups = new ArrayList<>();
        if (groups.isEmpty())
            namedGroups.add(new NamedGroupArgs(null, new LinkedHashSet<>(defs.values())));
        else {
            if (currentGroup != null)
                namedGroups.add(new NamedGroupArgs("Subcommand '" + currentGroup + "'", findMyArgs(currentGroup)));
            else
                for (Map.Entry<String, Collection<String>> groupSet : groups.entrySet())
                    namedGroups.add(new NamedGroupArgs("Subcommand '" + groupSet.getKey() + "'", findMyArgs(groupSet.getKey())));
            // Find remaining groups
            if (!remainingArgs.isEmpty())
                namedGroups.add(new NamedGroupArgs("Common arguments", remainingArgs));
        }

        List<List<String>> lefts = new ArrayList<>();
        List<List<String>> rights = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int max = 0;
        for (NamedGroupArgs it : namedGroups) {
            names.add(it.name);
            max = Math.max(max, singleGroupCalc(it.args, lefts, rights));
        }

        max++;
        String secondLineInset = spaces(max + 4);
        for (int i = 0; i < names.size(); i++) {
            out.append(NL);
            singleGroupPrint(names.get(i), lefts.get(i), rights.get(i), max, secondLineInset, out);
        }
    }

    private int singleGroupCalc(Collection<ArgResult> args, List<List<String>> lefts, List<List<String>> rights) {
        int maxsize = 0;
        List<String> leftPart = new ArrayList<>();
        List<String> rightPart = new ArrayList<>();
        lefts.add(leftPart);
        rights.add(rightPart);
        for (ArgResult arg : args) {
            String left = getArgWithParam(arg, true);
            leftPart.add(left);
            String right = info.get(arg);
            rightPart.add(right == null ? "" : right);
            maxsize = Math.max(maxsize, left.length());
        }
        return maxsize;
    }

    private void singleGroupPrint(String title, List<String> leftPart, List<String> rightPart, int maxsize, String secondLineInset, StringBuilder out) {
        if (title == null)
            title = "Arguments";
        print(out, title + ":", "", "");
        for (int i = 0; i < leftPart.size(); i++) {
            String left = INSET + leftPart.get(i) + spaces(maxsize - leftPart.get(i).length()) + (rightPart.get(i).isEmpty() ? "" : ": ");
            print(out, rightPart.get(i), left, secondLineInset);
        }
    }

    private String findUsageLine(String groupName, Collection<ArgResult> wrongParams, Collection<ArgResult> commonArgs) {
        List<ArgResult> trackUnique = new ArrayList<>();
        Set<ArgResult> upToNow = new LinkedHashSet<>();
        StringBuilder line = new StringBuilder();

        if (groupName != null)
            line.append(" ").append(groupName);
        Collection<ArgResult> myArgs = findMyArgs(groupName);
        if (!collapseCommon)
            myArgs.addAll(commonArgs);
        for (ArgResult res : myArgs) {

            trackUnique.add(res);
            if (trackUnique.size() > 2)
                trackUnique.remove(0);

            Set<ArgResult> reqDeps = depends.get(res);
            boolean missing = reqDeps != null && !containsAny(reqDeps, upToNow);
            if (missing)
                wrongParams.add(res);
            else {
                reqDeps = softdepends.get(res);
                missing = reqDeps != null && !containsAny(reqDeps, upToNow);
                if (missing)
                    wrongParams.add(res);
            }
            upToNow.add(res);

            boolean req = isInCollection(required, res);
            boolean morethanonce = multi.contains(res);
            line.append(isUnique(unique, trackUnique) ? "|" : " ").append(req ? "" : "[").append(getArgWithParam(res, false)).
                    append(req ? "" : "]").
                    append(morethanonce ? "..." : "").
                    append(missing ? "^" : "");
        }
        if (collapseCommon && !commonArgs.isEmpty())
            line.append(" COMMON_ARGS...");
        if (freeArguments == null)
            line.append(" ARGS...");
        else
            for (String arg : freeArguments)
                line.append(" ").append(arg);
        return line.toString();
    }

    private void getUsage(StringBuilder out, String group, Collection<ArgResult> commonArgs) {
        Collection<ArgResult> wrongParams = new LinkedHashSet<>();
        out.append("Usage:").append(NL);
        if (group == null && !groups.isEmpty())
            for (String it : groups.keySet())
                print(out, execName + findUsageLine(it, wrongParams, commonArgs), INSET + INSET, "");
        else
            print(out, execName + findUsageLine(group, wrongParams, commonArgs), INSET, "");
        if (!wrongParams.isEmpty()) {
            out.append("Notes:").append(NL);
            print(out, "^some pre-required arguments of the argument" + getArgsWithPlural(wrongParams) + " have been hidden for clarity",
                    INSET, "");
        }
    }

    private Set<ArgResult> sets(String[] args, int minimum, String type) {
        if (args == null || args.length < minimum)
            throw new ArgumentException("Too few arguments are defined for " + type);
        Set<ArgResult> items = new LinkedHashSet<>();
        for (String each : args)
            items.add(defs.get(checkExist(each)));
        return items;
    }

    private String checkNotExists(String arg) throws ArgumentException {
        arg = checkValid(arg);
        if (defs.containsKey(arg) || helpArgs.contains(arg))
            throw new ArgumentException("Argument " + arg + " already defined");
        return arg;
    }

    private String checkExist(String arg) throws ArgumentException {
        arg = checkValid(arg);
        if (!(defs.containsKey(arg) || helpArgs.contains(arg)))
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

    private String getArg(ArgResult cons) {
        return getArg(cons, false);
    }

    private String getArg(ArgResult cons, boolean full) {
        StringBuilder name = new StringBuilder();
        String last = "";
        for (String arg : defs.keySet())
            if (defs.get(arg) == cons) {
                if (full) {
                    last = arg;
                    name.append('|').append(last);
                } else if (namesAsGiven)
                    return arg;
                else if (arg.length() > last.length())
                    last = arg;
            }
        return last.length() > 0 ? (full ? name.substring(1) : last) : "";
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

    private Collection<String> getArgValues(Collection<ArgResult> args) {
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

    private void execHelp(String group) {
        System.out.print(usageAsString(group));
        System.out.flush();
        System.exit(0);
    }

    private void execError(String errorMessage, String group) {
        if (errorStrategy != null && errorStrategy.requiresHelp)
            System.out.println(usageAsString(group));
        (errorArg == null ? ((errorStrategy == null ? THROW_EXCEPTION : errorStrategy).getBehavior(this)) : errorArg).result(errorMessage);
    }
}

class NamedGroupArgs {
    final String name;
    final Collection<ArgResult> args;

    NamedGroupArgs(String name, Collection<ArgResult> args) {
        this.name = name;
        this.args = args;
    }
}

class GroupedArgsWithParams {
    final Map<String, ArgResult> grouped;
    final Map<String, ArgResult> free;

    GroupedArgsWithParams(Map<String, ArgResult> grouped, Map<String, ArgResult> free) {
        this.grouped = grouped;
        this.free = free;
    }
}