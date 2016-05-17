package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.core.MessageType;
import lombok.Getter;

enum InteractionResultType {
    SUCCESS,
    ERROR,
    VALIDATION,
}

final public class InteractionResult {
    @Getter private final String message;
    @Getter private final MessageType messageType;
    @Getter private final InteractionResultType type;

    public boolean isSuccess() {
        return type == InteractionResultType.SUCCESS;
    }

    public static class Builder {
        String message;
        MessageType messageType;
        InteractionResultType type;

        public Builder error() {
            type = InteractionResultType.ERROR;
            return this;
        }

        public Builder message(String val) {
            message = val;
            return this;
        }

        public Builder messageType(MessageType val) {
            messageType = val;
            return this;
        }

        public Builder success() {
            type = InteractionResultType.SUCCESS;
            return this;
        }

        public Builder type(InteractionResultType val) {
            type = val;
            return this;
        }

        public Builder validationError() {
            type = InteractionResultType.VALIDATION;
            return this;
        }

        public InteractionResult build() {
            return new InteractionResult(this);
        }
    }

    private InteractionResult(Builder builder) {
        message = builder.message;
        messageType = builder.messageType;
        type = builder.type;
    }
}
