package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.Pair;

public class InvalidArgument extends AbstractArgument {

    public InvalidArgument() {
    }

    public InvalidArgument(String name, String pattern) {
        setName(name);
        setPattern(pattern);
    }

    @Override
    public Pair<Integer, String> parse(String rawCommand) throws ArgumentParseException {
        throw new ArgumentParseException("InvalidArgument can't parse.");
    }
}
