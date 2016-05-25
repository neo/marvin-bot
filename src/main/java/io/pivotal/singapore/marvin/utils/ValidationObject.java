package io.pivotal.singapore.marvin.utils;

import io.pivotal.singapore.marvin.slack.IncomingSlackRequestValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

public abstract class ValidationObject<T> {
    private Validator validator = new IncomingSlackRequestValidator();

    public abstract T getTargetInstance();

    public boolean isInvalid() {
        return !isValid();
    }

    public boolean isValid() {
        BindingResult bindingResult = new BeanPropertyBindingResult(getTargetInstance(), this.getClass().getName());
        validator.validate(getTargetInstance(), bindingResult);
        return !bindingResult.hasErrors();
    }
}
