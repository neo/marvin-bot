package io.pivotal.singapore.marvin.commands.default_response.serializers;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;

import java.util.Map;

public class DefaultResponseDeserializerJson extends StdConverter<Map<String, String>, DefaultResponse> {
    @Override
    public DefaultResponse convert(Map<String, String> value) {
        return DefaultResponse.from(value);
    }
}
