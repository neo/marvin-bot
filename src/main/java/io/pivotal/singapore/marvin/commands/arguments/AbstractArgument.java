package io.pivotal.singapore.marvin.commands.arguments;

import lombok.Getter;
import lombok.Setter;

abstract public class AbstractArgument implements Argument {
    @Getter @Setter protected String name;
    @Getter @Setter protected String pattern;

    @Override
    abstract public ArgumentParsedResult parse(String rawCommand);
}
