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

    public abstract T self();

    public boolean isInvalid() {
        return !isValid();
    }

    public boolean isValid() {
        constraintViolations = this.validator.validate(self());
        return constraintViolations.isEmpty();
    }

    public boolean hasErrorFor(String field) {
        return getErrors().containsKey(field);
    }

    private Map<String, Object> getErrors() {
        List<String> fields = getErrorFields();
        List<Object> values = getErrorValues();

        Preconditions.checkState(fields.size() == values.size());

        HashMap<String, Object> errors = new HashMap();
        for (int i = 0; i < fields.size(); i++) {
            errors.put(fields.get(i), values.get(i));
        }
        return errors;
    }

    private List<Object> getErrorValues() {
        return constraintViolations
            .stream()
            .map(ConstraintViolation::getInvalidValue)
            .collect(Collectors.toList());
    }

    private List<String> getErrorFields() {
        return constraintViolations
            .stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(pathImpl -> ((PathImpl) pathImpl).getLeafNode().getName())
            .collect(Collectors.toList());
    }
}
