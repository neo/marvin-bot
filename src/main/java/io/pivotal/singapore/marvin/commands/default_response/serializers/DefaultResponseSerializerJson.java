package io.pivotal.singapore.marvin.commands.default_response.serializers;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;

import java.util.List;
import java.util.Map;

public class DefaultResponseSerializerJson extends StdConverter<DefaultResponse, Map<String, String>> {
    @Override
    public Map<String, String> convert(DefaultResponse defaultResponse) {
        return defaultResponse.toMap();
    }
}
