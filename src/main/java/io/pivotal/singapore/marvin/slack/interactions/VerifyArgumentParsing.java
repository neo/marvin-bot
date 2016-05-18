package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;

import java.util.Optional;

public class VerifyArgumentParsing implements Interaction {
    private final CommandRepository commandRepository;
    private final Interaction decoratedObject;
    private final InteractionRequest interactionRequest;

    public VerifyArgumentParsing(Interaction interaction, CommandRepository commandRepository, InteractionRequest interactionRequest) {
        this.decoratedObject = interaction;
        this.commandRepository = commandRepository;
        this.interactionRequest = interactionRequest;
    }

    @Override
    public InteractionResult run() {
        if (!isCommandPresent()) {
            return new InteractionResult.Builder()
                .messageType(MessageType.user)
                .message("This will all end in tears.")
                .validationError()
                .build();
        }

        if (!isSubCommandPresent()) {
            String message = String.format("This sub command doesn't exist for %s", interactionRequest.getCommand());
            return new InteractionResult.Builder()
                .messageType(MessageType.user)
                .message(message)
                .validationError()
                .build();
        }

        ICommand command = findSubCommand().orElse(getCommand());
        Arguments arguments = command.getArguments();

        final ArgumentParsedResultList argumentParsedResults = arguments.parse(interactionRequest.getArguments());
        if (argumentParsedResults.hasErrors()) {
            ArgumentParsedResult failedParsedArgument = argumentParsedResults.getFirst();
            String message = String.format("`%s` is not found in your command.", failedParsedArgument.getArgumentName());
            return new InteractionResult.Builder()
                .messageType(MessageType.user)
                .message(message)
                .validationError()
                .build();
        }

        return decoratedObject.run();
    }

    private boolean isCommandPresent() {
        return findCommand().isPresent();
    }

    private boolean isSubCommandPresent() {
        return isCommandPresent() && (findSubCommand().isPresent() || getCommand().getSubCommands().isEmpty());
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
