package com.panayotis.arjs;

public enum ErrorStrategy {
    IGNORE(false) {
        @Override
        ArgResult getBehavior(Args args) {
            return error -> {
            };
        }
    }, THROW_EXCEPTION(false) {
        @Override
        ArgResult getBehavior(Args args) {
            return error -> {
                throw new ArgumentException(error);
            };
        }
    }, PRINT_HELP_AND_EXIT(true) {
        @Override
        ArgResult getBehavior(Args args) {
            return error -> {
                System.err.println("ERROR: " + error);
                System.exit(1);
            };
        }
    }, PRINT_HELP_AND_THROW_EXCEPTION(true) {
        @Override
        ArgResult getBehavior(Args args) {
            return error -> {
                throw new ArgumentException(error);
            };
        }
    };

    final boolean requiresHelp;

    ErrorStrategy(boolean requiresHelp) {
        this.requiresHelp = requiresHelp;
    }

    abstract ArgResult getBehavior(Args args);
}
