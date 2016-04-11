package io.pivotal.singapore.marvin.commands.default_response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.core.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultResponse {
    private Map<String, String> responses = new HashMap<>();

    public DefaultResponse() {
    }

    public static DefaultResponse from(Map<String, String> responses) {
        return new DefaultResponse()
            .putAllResponses(responses);
    }

    @SuppressWarnings("unchecked")
    public static DefaultResponse from(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> hash = new HashMap<>();

        try {
            hash = mapper.readValue(json, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hash != null) {
            return DefaultResponse.from(hash);
        } else {
            return new DefaultResponse();
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

    public DefaultResponse putMessage(String key, String value) {
        this.responses.put(key, value);

        return this;
    }

    public Optional<String> getMessage(String key) {
        return Optional.ofNullable(responses.get(key));
    }

    public DefaultResponse putMessageType(String key, MessageType messageType) {
        return this.putMessageType(key, String.valueOf(messageType));
    }

    public DefaultResponse putMessageType(String key, String messageType) {
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

    private DefaultResponse putAllResponses(Map<String, String> responses) {
        this.responses.putAll(responses);

        return this;
    }
}
