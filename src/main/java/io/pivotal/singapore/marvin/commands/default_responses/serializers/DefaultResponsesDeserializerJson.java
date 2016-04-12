package io.pivotal.singapore.marvin.commands.default_responses.serializers;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;

import java.util.Map;

public class DefaultResponsesDeserializerJson extends StdConverter<Map<String, String>, DefaultResponses> {
    @Override
    public DefaultResponses convert(Map<String, String> value) {
        return DefaultResponses.from(value);
    }
}
