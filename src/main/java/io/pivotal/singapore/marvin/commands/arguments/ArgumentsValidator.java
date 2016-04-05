package io.pivotal.singapore.marvin.commands.arguments;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ArgumentsValidator implements Validator {

    public boolean supports(Class clazz) {
        return Arguments.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        Arguments arguments = (Arguments) obj;

        arguments.getArguments()
            .stream()
            .filter(argument -> argument instanceof InvalidArgument)
            .forEach(argument -> e.rejectValue(
                "arguments",
                String.format("arguments.%s.invalidUrl", argument.getName()),
                String.format("Invalid argument type for value '%s'", argument.getPattern())
            ));
    }
}
