package io.pivotal.singapore.marvin.slack.interactions;

import com.google.common.base.Preconditions;
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
import io.pivotal.singapore.marvin.slack.ValidationObject;

import javax.validation.constraints.AssertTrue;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MakeRemoteApiCall extends ValidationObject<MakeRemoteApiCall> {
    private final CommandRepository commandRepository;
    private final IncomingSlackRequest incomingSlackRequest;
    private SlackText slackText;

    private Clock clock;
    private RemoteApiService remoteApiService;

    @Override
    public MakeRemoteApiCall self() {
        return this;
    }

    public MakeRemoteApiCall(IncomingSlackRequest incomingSlackRequest, Clock clock, RemoteApiService remoteApiService, CommandRepository commandRepository) {
        Preconditions.checkNotNull(incomingSlackRequest);
        Preconditions.checkArgument(incomingSlackRequest.isValid());
        this.incomingSlackRequest = incomingSlackRequest;

        SlackText slackText = new SlackText(incomingSlackRequest.getText());
        Preconditions.checkState(slackText.isValid());
        this.slackText = slackText;

        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.clock = clock;
    }

    public MakeRemoteApiCallResult run() {
        Map _params = remoteServiceParams(incomingSlackRequest);
        ICommand cmd = findSubCommand().orElse(getCommand());

        Arguments arguments = cmd.getArguments();

        if (arguments.isParsable(slackText.getArguments())) {
            try {
                _params.putAll(arguments.parse(slackText.getArguments()));
            } catch (ArgumentParseException e) { /* already verified to valid */ }
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
}
