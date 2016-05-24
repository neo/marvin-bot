package io.pivotal.singapore.marvin.utils;

import com.google.common.base.Preconditions;
import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ValidationObject<T> {
    private Set<ConstraintViolation<T>> constraintViolations = Collections.emptySet();
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public abstract T getTargetInstance();

    public boolean isInvalid() {
        return !isValid();
    }

    public boolean isValid() {
        constraintViolations = this.validator.validate(getTargetInstance());
        return constraintViolations.isEmpty();
    }
}
