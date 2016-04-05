package io.pivotal.singapore.models;

import io.pivotal.singapore.marvin.commands.arguments.ArgumentsValidator;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class CommandValidator implements Validator {

    public boolean supports(Class clazz) {
        return ICommand.class.isAssignableFrom(clazz);
    }

    public void validate(Object obj, Errors e) {
        ICommand command = (ICommand) obj;
        ValidationUtils.rejectIfEmptyOrWhitespace(e, "name", "name.empty", "Name can't be empty");

        if (command.getName() != null && command.getName().contains(" ")) {
            e.rejectValue("name", "name.nospaces", "Name can't contain spaces");
        }

        if (command.getMethod() == null) {
            e.rejectValue("method", "method.undefined", "HTTP Method MUST be provided");
        }

        if (command.getEndpoint() == null ||
            (!command.getEndpoint().startsWith("http://") && !command.getEndpoint().startsWith("https://"))) {
            e.rejectValue("endpoint", "endpoint.invalidUrl", "Endpoint isn't a valid URL");
        }

        if (!command.getArguments().getArguments().isEmpty()) {
            ValidationUtils.invokeValidator(new ArgumentsValidator(), command.getArguments(), e);
        }

        // Validate sub commands the same way as commands, just as a key under it
        if (command.getClass().equals(Command.class)) {
            AtomicInteger idx = new AtomicInteger();
            Command cmd = (Command) command;
            cmd.getSubCommands()
                .stream()
                .forEach(subCommand -> {
                    e.pushNestedPath("subCommands[" + idx.getAndIncrement() + "]");
                    ValidationUtils.invokeValidator(new CommandValidator(), subCommand, e);
                    e.popNestedPath();
                });
        }
    }
}
