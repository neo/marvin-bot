package io.pivotal.singapore.marvin.commands.arguments;

import lombok.Getter;

public class ArgumentParseException extends Exception {
    @Getter
    Object thrower;

    public ArgumentParseException(Throwable cause, Object thrower) {
        this("", cause, thrower);
    }

    public ArgumentParseException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ArgumentParseException(String message) {
        this(message, null, null);
    }

    public ArgumentParseException(String message, Throwable cause, Object thrower) {
        super(message, cause);
        this.thrower = thrower;
    }
}
