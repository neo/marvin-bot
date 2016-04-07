package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.Pair;

public interface Argument {
    static Boolean canParse(String capture) {
        return false;
    }

    Pair<Integer, String> parse(String rawCommand) throws ArgumentParseException;

    String getName();
    void setName(String name);
    String getPattern();
    void setPattern(String pattern);
}

