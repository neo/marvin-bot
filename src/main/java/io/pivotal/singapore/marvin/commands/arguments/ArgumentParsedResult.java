package io.pivotal.singapore.marvin.commands.arguments;

import com.google.common.base.Preconditions;
import lombok.Getter;

enum ArgumentParsedResultType {
    SUCCESS,
    FAILURE
}

public class ArgumentParsedResult {
    // compulsory
    @Getter private final ArgumentParsedResultType type;

    // optional with defaults
    @Getter private String argumentName;
    @Getter private String pattern;
    @Getter private int matchOffset;
    @Getter private String matchResult;

    private ArgumentParsedResult(Builder builder) {
        type = builder.type;
        argumentName = builder.argumentName;
        pattern = builder.pattern;
        matchOffset = builder.matchOffset;
        matchResult = builder.matchResult;
    }

    public boolean isFailure() {
        return type == ArgumentParsedResultType.FAILURE;
    }

    public static class Builder {
        private String argumentName = "";
        private int matchOffset;
        private String pattern = "";
        private String matchResult = "";
        private ArgumentParsedResultType type;

        public Builder argumentName(String argumentName) {
            this.argumentName = argumentName;
            return this;
        }

        public Builder matchOffset(int matchOffset) {
            this.matchOffset = matchOffset;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder matchResult(String matchResult) {
            this.matchResult = matchResult;
            return this;
        }

        public Builder type(ArgumentParsedResultType type) {
            this.type = type;
            return this;
        }

        public Builder success() {
            return type(ArgumentParsedResultType.SUCCESS);
        }

        public Builder failure() {
            return type(ArgumentParsedResultType.FAILURE);
        }

        public ArgumentParsedResult build() {
            Preconditions.checkNotNull(type);
            return new ArgumentParsedResult(this);
        }
    }
}
