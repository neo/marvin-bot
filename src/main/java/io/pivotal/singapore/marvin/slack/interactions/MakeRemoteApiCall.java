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
    //      x MakeRemoteApiCallResult should not return messageType
    public MakeRemoteApiCall(MakeRemoteApiCallParams makeRemoteApiCallParams, Clock clock, RemoteApiService remoteApiService, CommandRepository commandRepository) {
        this.params = makeRemoteApiCallParams;
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.clock = clock;
    }

    public InteractionResult run() {
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

            return new InteractionResult.Builder()
                .messageType(MessageType.user)
                .message(text)
                .type(InteractionResultType.VALIDATION)
                .build();
        }
        RemoteApiServiceResponse response = remoteApiService.call(command, remoteServiceParams);

        return new InteractionResult.Builder()
            .messageType(response.getMessageType().orElse(MessageType.user))
            .message(response.getMessage())
            .type(response.isSuccessful() ? InteractionResultType.SUCCESS : InteractionResultType.ERROR)
            .build();
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
