package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.core.MessageType;

import java.util.Optional;

public class ParseCommand implements Interaction {
    private final CommandRepository commandRepository;
    private final Interaction decoratedObject;

    private InteractionRequest interactionRequest;

    public ParseCommand(Interaction interaction, CommandRepository commandRepository) {
        this.decoratedObject = interaction;
        this.commandRepository = commandRepository;
    }

    @Override
    public InteractionResult run(InteractionRequest interactionRequest) {
        this.interactionRequest = interactionRequest;

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

        return decoratedObject.run(interactionRequest);
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
