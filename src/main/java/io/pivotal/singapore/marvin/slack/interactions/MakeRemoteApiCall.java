package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.slack.IncomingSlackRequest;
import io.pivotal.singapore.marvin.slack.SlackText;
import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class MakeRemoteApiCall {
    private final CommandRepository commandRepository;
    private final IncomingSlackRequest incomingSlackRequest;
    private final SlackText slackText;

    private final Validator validator;
    private Set<ConstraintViolation<MakeRemoteApiCall>> constraintViolations = Collections.emptySet();

    public MakeRemoteApiCall(@NotNull IncomingSlackRequest incomingSlackRequest, CommandRepository commandRepository) {
        assert(incomingSlackRequest.isValid());
        this.incomingSlackRequest = incomingSlackRequest;

        SlackText slackText = new SlackText(incomingSlackRequest.getText());
        assert(slackText.isValid());
        this.slackText = slackText;

        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.commandRepository = commandRepository;
    }

    @AssertTrue
    private boolean isCommandPresent() {
        return findCommand().isPresent();
    }

    @AssertTrue
    private boolean isSubCommandPresent() {
        return isCommandPresent() && findSubCommand().isPresent();
    }

    private Optional<ICommand> findSubCommand() {
        return getCommand().findSubCommand(slackText.getSubCommand());
    }

    private Command getCommand() {
        return findCommand().get();
    }

    private Optional<Command> findCommand() {
        return this.commandRepository.findOneByName(slackText.getCommand());
    }

    public boolean isInvalid() {
        constraintViolations = this.validator.validate(this);
        return constraintViolations.size() > 0;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean hasErrorFor(String field) {
        return getErrors().containsKey(field);
    }

    public Map<String, Object> getErrors() {
        List<String> fields = getErrorFields();
        List<Object> values = getErrorValues();

        assert (fields.size() == values.size());

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
