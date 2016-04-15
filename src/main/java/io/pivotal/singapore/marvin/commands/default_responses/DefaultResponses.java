package io.pivotal.singapore.marvin.commands.default_responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.core.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultResponses {
    private Map<String, String> responses = new HashMap<>();

    public DefaultResponses() {
    }

    public static DefaultResponses from(Map<String, String> responses) {
        return new DefaultResponses()
            .putAllResponses(responses);
    }

    // This is a double edged sword so use with caution.
    // You are in essence ignoring the type system.
    // This is a result of Java's slightly "broken" type system.
    // It can come back to bite you sometimes.
    @SuppressWarnings("unchecked")
    public static DefaultResponses from(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> hash = new HashMap<>();

        try {
            hash = mapper.readValue(json, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hash != null) {
            return DefaultResponses.from(hash);
        } else {
            return new DefaultResponses();
        }
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(responses);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public Map<String, String> toMap() {
        return responses;
    }

    public DefaultResponses putMessage(String key, String value) {
        this.responses.put(key, value);

        return this;
    }

    public Optional<String> getMessage(String key) {
        return Optional.ofNullable(responses.get(key));
    }

    public DefaultResponses putMessageType(String key, MessageType messageType) {
        return this.putMessageType(key, String.valueOf(messageType));
    }

    public DefaultResponses putMessageType(String key, String messageType) {
        this.responses.put(String.format("%sType", key), messageType);

        return this;
    }

    public Optional<MessageType> getMessageType(String key) {
        try {
            return Optional.of(MessageType.valueOf(responses.get(String.format("%sType", key))));
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    private DefaultResponses putAllResponses(Map<String, String> responses) {
        this.responses.putAll(responses);

        return this;
    }
}
