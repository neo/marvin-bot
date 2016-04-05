package io.pivotal.singapore.marvin.commands.arguments;

public class ArgumentFactory {
    public static Argument getArgument(String name, String pattern) {

        if (RegexArgument.canParse(pattern)) {
            return new RegexArgument(name, pattern);
        } else if (TimestampArgument.canParse(pattern)) {
            return new TimestampArgument(name);
        } else {
            throw new IllegalArgumentException(String.format("Unknown factory for '%s'", pattern));
        }
    }

    public static Argument getWithEmptyArgument(String name, String pattern) {
        try {
            return getArgument(name, pattern);
        } catch (IllegalArgumentException e) {
            return new InvalidArgument(name, pattern);
        }
    }
}
