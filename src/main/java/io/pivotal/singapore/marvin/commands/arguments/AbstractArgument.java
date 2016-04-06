package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.Pair;
import lombok.Getter;
import lombok.Setter;

abstract public class AbstractArgument implements Argument {
    @Getter @Setter protected String name;
    @Getter @Setter protected String pattern;

    @Override
    abstract public Pair<Integer, String> parse(String rawCommand);
}
