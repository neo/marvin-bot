package io.pivotal.singapore.marvin.slack;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class IncomingSlackRequestValidator implements Validator {
    @Override
    public boolean supports(Class<?> klass) {
        return IncomingSlackRequest.class.isAssignableFrom(klass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "channelName", "Channel name cannot be empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "token", "Token cannot be empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "text", "Text cannot be empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "User name cannot be empty");
    }
}
