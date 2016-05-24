package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceRequest;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;

import java.util.Optional;

public class MakeRemoteApiCall implements Interaction {
    private final CommandRepository commandRepository;
    private final RemoteApiService remoteApiService;

    private final InteractionRequest interactionRequest;

    public MakeRemoteApiCall(RemoteApiService remoteApiService, CommandRepository commandRepository, InteractionRequest interactionRequest) {
        this.commandRepository = commandRepository;
        this.remoteApiService = remoteApiService;
        this.interactionRequest = interactionRequest;
    }

    @Override
    public InteractionResult run() {
        final ICommand command = findSubCommand().orElse(getCommand());
        final Arguments arguments = command.getArguments();

        final ArgumentParsedResultList argumentParsedResults = arguments.parse(interactionRequest.getArguments());

        final RemoteApiServiceRequest request = new RemoteApiServiceRequest(interactionRequest, argumentParsedResults);
        final RemoteApiServiceResponse response = remoteApiService.call(command, request);

        return new InteractionResult.Builder()
            .messageType(response.getMessageType().orElse(MessageType.user))
            .message(response.getMessage())
            .type(response.isSuccessful() ? InteractionResultType.SUCCESS : InteractionResultType.ERROR)
            .build();
    }

    private Optional<ICommand> findSubCommand() {
        return getCommand().findSubCommand(interactionRequest.getSubCommand());
    }

    private Command getCommand() {
        return findCommand().get();
    }

    private Optional<Command> findCommand() {
        return this.commandRepository.findOneByName(interactionRequest.getCommand());
    }
}
