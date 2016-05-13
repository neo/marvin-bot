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
    private Clock clock;
    private RemoteApiService remoteApiService;

    private MakeRemoteApiCallParams params;

    @Override
    public MakeRemoteApiCall self() {
        return this;
    }

    // TODO: 12/5/16
    //      x create adapter (rename SlackTextParser to be that) to hide irrelevant fields in IncomingSlackRequest
    //      - pass this object to run() method
    //      - MakeRemoteApiCallResult should not return messageType
    public MakeRemoteApiCall(MakeRemoteApiCallParams makeRemoteApiCallParams, Clock clock, RemoteApiService remoteApiService, CommandRepository commandRepository) {
        this.params = makeRemoteApiCallParams;
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.clock = clock;
    }

    public MakeRemoteApiCallResult run() {
        Map remoteServiceParams = remoteServiceParams();
        ICommand command = findSubCommand().orElse(getCommand());

        Arguments arguments = command.getArguments();

        if (arguments.isParsable(params.getArguments())) {
            try {
                remoteServiceParams.putAll(arguments.parse(params.getArguments()));
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
        RemoteApiServiceResponse response = remoteApiService.call(command, remoteServiceParams);

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

    private HashMap<String, Object> remoteServiceParams() {
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
        return getCommand().findSubCommand(params.getSubCommand());
    }

    private Command getCommand() {
        return findCommand().get();
    }

    private Optional<Command> findCommand() {
        return this.commandRepository.findOneByName(params.getCommand());
    }
}
