package io.pivotal.singapore.marvin.slack;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

public class SlackText {
    private String subCommand;
    private String arguments;

    private Validator validator;
    private String[] tokens;

    private Set<ConstraintViolation<SlackText>> constraintViolations = Collections.emptySet();

    public SlackText(@NotBlank String textCommand) {
        tokens = textCommand.trim().split(" ");

        if (tokens.length > 1) {
            this.subCommand = tokens[1];
        }
        if(tokens.length > 2) {
            Queue argumentTokens = new LinkedList(Arrays.asList(tokens));
            argumentTokens.poll();
            argumentTokens.poll();

            String arguments = String.join(" ", argumentTokens);
            this.arguments = arguments;
        }

        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public boolean isInvalid() {
        constraintViolations = this.validator.validate(this);
        return constraintViolations.size() > 0;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    @NotBlank
    public String getCommand() {
        return tokens[0];
    }

    public String getSubCommand() {
        return subCommand;
    }

    public String getArguments() {
        return arguments;
    }
}
