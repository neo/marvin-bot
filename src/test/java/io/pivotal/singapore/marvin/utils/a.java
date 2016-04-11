package io.pivotal.singapore.marvin.utils;

import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;

import java.util.HashMap;
import java.util.Map;

public class a {
    public static DefaultResponseBuilder defaultResponse = new DefaultResponseBuilder();

    public static class DefaultResponseBuilder {
        private Map<String, String> responses = new HashMap<>();

        @SuppressWarnings("unchecked")
        DefaultResponseBuilder() {
        }

        DefaultResponseBuilder(Map<String, String> responses) {
            this.responses = responses;
        }

        public DefaultResponseBuilder w(String key, String value) {
            responses.put(key, value);

            return new DefaultResponseBuilder(responses);
        }

        public DefaultResponse build() {
            return DefaultResponse.from(responses);
        }
    }
}
