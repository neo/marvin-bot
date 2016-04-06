package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.commands.arguments.Arguments;

import javax.persistence.AttributeConverter;

public class ArgumentListConverter implements AttributeConverter<Arguments, String> {

    @Override
    public String convertToDatabaseColumn(Arguments arguments) {
        return arguments.toJson();
    }

    @Override
    public Arguments convertToEntityAttribute(String dbData) {
        return Arguments.of(dbData);
    }
}
