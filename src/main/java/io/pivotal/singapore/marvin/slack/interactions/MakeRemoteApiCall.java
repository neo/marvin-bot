package io.pivotal.singapore.marvin.slack.interactions;

import com.google.common.collect.ImmutableMap;
import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import io.pivotal.singapore.marvin.slack.ValidationObject;

import javax.validation.constraints.AssertTrue;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    //      - arguments.parse() should not throw an exception (handle it on the lower level)
    //      - perform precondition check for ensuring that commands exist
    //      - create a data object for remote service params
    public MakeRemoteApiCall(MakeRemoteApiCallParams makeRemoteApiCallParams, Clock clock, RemoteApiService remoteApiService, CommandRepository commandRepository) {
        this.params = makeRemoteApiCallParams;
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.clock = clock;
    }

    public InteractionResult run() {
        ICommand command = findSubCommand().orElse(getCommand());

        Arguments arguments = command.getArguments();

        final List<ArgumentParsedResult> argumentParsedResults = arguments.parse(params.getArguments());

        if (arguments.hasParseError()) {
            ArgumentParsedResult failedParsedArgument = argumentParsedResults.get(0);
            return new InteractionResult.Builder()
                .messageType(MessageType.user)
                .message(String.format("`%s` is not found in your command.", failedParsedArgument.getArgumentName()))
                .type(InteractionResultType.VALIDATION)
                .build();
        }
        RemoteApiServiceResponse response = remoteApiService.call(command, remoteServiceParams(argumentParsedResults));

        return new InteractionResult.Builder()
            .messageType(response.getMessageType().orElse(MessageType.user))
            .message(response.getMessage())
            .type(response.isSuccessful() ? InteractionResultType.SUCCESS : InteractionResultType.ERROR)
            .build();
    }

    private Map<String, String> remoteServiceParams(List<ArgumentParsedResult> results) {
        Map<String, String> parsedArguments = results.stream()
                .collect(Collectors.toMap(ArgumentParsedResult::getArgumentName,
                                          ArgumentParsedResult::getMatchResult));
        return new ImmutableMap.Builder<String, String>()
            .put("username", String.format("%s@pivotal.io", params.getUserName()))
            .put("channel", params.getChannelName())
            .put("received_at", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            .put("command", params.getText())
            .putAll(parsedArguments)
            .build();
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
