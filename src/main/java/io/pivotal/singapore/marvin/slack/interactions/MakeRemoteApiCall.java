package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.Argument;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParseException;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import io.pivotal.singapore.marvin.slack.IncomingSlackRequest;
import io.pivotal.singapore.marvin.slack.SlackText;
import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MakeRemoteApiCall {
    private final CommandRepository commandRepository;
    private final IncomingSlackRequest incomingSlackRequest;
    private final SlackText slackText;

    private final Validator validator;
    private Set<ConstraintViolation<MakeRemoteApiCall>> constraintViolations = Collections.emptySet();
    private Clock clock;
    private RemoteApiService remoteApiService;

    public MakeRemoteApiCall(@NotNull IncomingSlackRequest incomingSlackRequest, Clock clock, RemoteApiService remoteApiService, CommandRepository commandRepository) {
        assert(incomingSlackRequest.isValid());
        this.incomingSlackRequest = incomingSlackRequest;

        SlackText slackText = new SlackText(incomingSlackRequest.getText());
        assert(slackText.isValid());
        this.slackText = slackText;

        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.clock = clock;
    }

    public MakeRemoteApiCallResult run() {
        Map _params = remoteServiceParams(incomingSlackRequest);
        ICommand cmd = findSubCommand().orElse(getCommand());

        Arguments arguments = cmd.getArguments();

        if (arguments.isParsable(slackText.getArguments())) {
            try { _params.putAll(arguments.parse(slackText.getArguments())); }
            catch (ArgumentParseException e) { /* already verified to valid */ }
        } else {
            Argument argument = arguments.getUnparseableArgument();
            String text = String.format("`%s` is not found in your command.", argument.getName());

            return new MakeRemoteApiCallResult(new InteractionResult.Builder()
                .isSuccess(false)
                .body("messageType", getSlackResponseType(MessageType.user))
                .body("message", text)
                .build());
        }
        RemoteApiServiceResponse response = remoteApiService.call(cmd, _params);

        return new MakeRemoteApiCallResult(new InteractionResult.Builder()
            .isSuccess(true)
            .body("messageType", getSlackResponseType(response.getMessageType().orElse(MessageType.user)))
            .body("message", response.getMessage())
            .build());
    }

    private String getSlackResponseType(MessageType messageType) {
        switch (messageType) {
            case user:
                return "ephemeral";
            case channel:
                return "in_channel";
            default:
                throw new IllegalArgumentException(
                    String.format("MessageType '%s' is not configured for Slack", messageType.toString())
                );
        }
    }

    private HashMap<String, Object> remoteServiceParams(IncomingSlackRequest params) {
        HashMap<String, Object> serviceParams = new HashMap<>();
        serviceParams.put("username", String.format("%s@pivotal.io", params.getUserName()));
        serviceParams.put("channel", params.getChannelName());
        serviceParams.put("received_at", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        serviceParams.put("command", params.getText());

        return serviceParams;
    }

    @AssertTrue
    private boolean isCommandPresent() {
        return findCommand().isPresent();
    }

    @AssertTrue
    private boolean isSubCommandPresent() {
        return isCommandPresent() && (findSubCommand().isPresent() || getCommand().getSubCommands().isEmpty());
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
