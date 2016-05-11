package io.pivotal.singapore.marvin.commands.arguments;

public interface Argument {
    ArgumentParsedResult parse(String rawCommand);

    String getName();
    void setName(String name);
    String getPattern();
    void setPattern(String pattern);
}

