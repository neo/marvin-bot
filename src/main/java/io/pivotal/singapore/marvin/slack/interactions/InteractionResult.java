package io.pivotal.singapore.marvin.slack.interactions;

import java.util.HashMap;
import java.util.Map;

final public class InteractionResult {
    boolean isSuccess;
    Map<String, String> errors;
    Map<String, String> body;

    public static class Builder {
        boolean isSuccess;
        Map<String, String> errors = new HashMap();
        Map<String, String> body = new HashMap();


        public Builder isSuccess(boolean val) {
            isSuccess = val;
            return this;
        }

        public Builder error(String key, String value) {
            errors.put(key, value);
            return this;
        }

        public Builder body(String key, String value) {
            body.put(key, value);
            return this;
        }

        public InteractionResult build() {
            return new InteractionResult(this);
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private InteractionResult(Builder builder) {
        isSuccess = builder.isSuccess;
        errors = builder.errors;
        body = builder.body;
    }
}
