package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceRequest;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import io.pivotal.singapore.marvin.slack.ValidationObject;

import javax.validation.constraints.AssertTrue;
import java.util.Optional;

public class MakeRemoteApiCall extends ValidationObject<MakeRemoteApiCall> {
    private final CommandRepository commandRepository;
    private RemoteApiService remoteApiService;

    private MakeRemoteApiCallRequest params;


    @Override
    public MakeRemoteApiCall self() {
        return this;
    }

    // TODO: 12/5/16
    //      x create adapter (rename SlackTextParser to be that) to hide irrelevant fields in IncomingSlackRequest
    //      x pass this object to run() method
    //      x MakeRemoteApiCallResult should not return messageType
    //      x arguments.parse() should not throw an exception (handle it on the lower level)
    //      x create a data object for remote service params
    //      - perform precondition check for ensuring that commands exist and arguments can be parsed
    public MakeRemoteApiCall(RemoteApiService remoteApiService, CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
    }

    public InteractionResult run(MakeRemoteApiCallRequest makeRemoteApiCallRequest) {
        this.params = makeRemoteApiCallRequest;

        if (isInvalid()) {
            if (hasErrorFor("commandPresent")) {
                return getValidationErrorResult("This will all end in tears.");
            } else if (hasErrorFor("subCommandPresent")) {
                String message = String.format("This sub command doesn't exist for %s", params.getCommand());
                return getValidationErrorResult(message);
            }
        }

        ICommand command = findSubCommand().orElse(getCommand());
        Arguments arguments = command.getArguments();

        final ArgumentParsedResultList argumentParsedResults = arguments.parse(params.getArguments());
        if (argumentParsedResults.hasErrors()) {
            ArgumentParsedResult failedParsedArgument = argumentParsedResults.getFirst();
            String message = String.format("`%s` is not found in your command.", failedParsedArgument.getArgumentName());
            return getValidationErrorResult(message);
        }

        RemoteApiServiceRequest request = new RemoteApiServiceRequest(makeRemoteApiCallRequest, argumentParsedResults);
        RemoteApiServiceResponse response = remoteApiService.call(command, request);

        return new InteractionResult.Builder()
            .messageType(response.getMessageType().orElse(MessageType.user))
            .message(response.getMessage())
            .type(response.isSuccessful() ? InteractionResultType.SUCCESS : InteractionResultType.ERROR)
            .build();
    }

    private InteractionResult getValidationErrorResult(String message) {
        return new InteractionResult.Builder()
            .messageType(MessageType.user)
            .message(message)
            .validationError()
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
