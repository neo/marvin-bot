package io.pivotal.singapore.marvin.commands.default_responses.serializers;

import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;

import javax.persistence.AttributeConverter;

public class DefaultResponsesConverter implements AttributeConverter<DefaultResponses, String> {

    @Override
    public String convertToDatabaseColumn(DefaultResponses defaultResponses) {
        return defaultResponses.toJson();
    }

    @Override
    public DefaultResponses convertToEntityAttribute(String dbData) {
        return DefaultResponses.from(dbData);
    }
}
