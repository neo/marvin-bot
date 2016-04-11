package io.pivotal.singapore.marvin.commands.default_response.serializers;

import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;

import javax.persistence.AttributeConverter;

public class DefaultResponseConverter implements AttributeConverter<DefaultResponse, String> {

    @Override
    public String convertToDatabaseColumn(DefaultResponse defaultResponse) {
        return defaultResponse.toJson();
    }

    @Override
    public DefaultResponse convertToEntityAttribute(String dbData) {
        return DefaultResponse.from(dbData);
    }
}
