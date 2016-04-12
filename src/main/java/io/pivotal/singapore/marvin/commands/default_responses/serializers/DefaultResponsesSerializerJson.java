package io.pivotal.singapore.marvin.commands.default_responses.serializers;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;

import java.util.Map;

public class DefaultResponsesSerializerJson extends StdConverter<DefaultResponses, Map<String, String>> {
    @Override
    public Map<String, String> convert(DefaultResponses defaultResponses) {
        return defaultResponses.toMap();
    }
}
