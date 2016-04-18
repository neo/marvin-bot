package io.pivotal.singapore.marvin.commands;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;

import io.pivotal.singapore.marvin.commands.arguments.ArgumentFactory;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;

import static io.pivotal.singapore.marvin.utils.CommandFactory.createSubCommands;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CommandValidatorTest {
    private static CommandValidator subject = new CommandValidator();

    @Test
    public void checkCommandName() {
        Command command = getInvalidCommand();
        assertError(command, "name", "name.empty");

        command.setName("");
        assertError(command, "name", "name.empty");

        command.setName("I pity the fool");
        assertError(command, "name", "name.nospaces");
    }

    @Test
    public void checkCommandHttpMethodNotNull(){
        Command command = getValidCommand();
        command.setMethod(null);
        assertError(command, "method", "method.undefined");
    }

    @Test
    public void checkCommandHttpMethodWhenNotRequired(){
        Command command = getValidCommand();
        command.setSubCommands(createSubCommands());
        command.setMethod(null);
        assertNoErrors(command);
    }

    @Test
    public void checkEndpoint() {
        Command command = getValidCommand();
        command.setEndpoint(null);
        assertError(command, "endpoint", "endpoint.invalidUrl");

        command.setEndpoint("BLAAAARGH");
        assertError(command, "endpoint", "endpoint.invalidUrl");

        command.setEndpoint("https://hello.tld/1");
        assertNoErrors(command);
    }

    @Test
    public void checkCommandHttpEndpointWhenNotRequired(){
        Command command = getValidCommand();
        command.setSubCommands(createSubCommands());
        command.setEndpoint(null);
        assertNoErrors(command);
    }

    @Test
    public void subCommandArguments() {
        Command command = getValidCommand();
        command.setSubCommands(getValidSubCommand());

        assertError(command, "subCommands[0].arguments", "arguments.time.invalidUrl");
    }

    @Test
    public void subCommandNameRequired() {
        Command command = getValidCommand();
        List<SubCommand> subCommands = getValidSubCommand();
        SubCommand subCommand = subCommands.get(0);

        subCommand.setName("");
        command.setSubCommands(subCommands);

        assertError(command, "subCommands[0].name", "name.empty");
    }

    private Command getInvalidCommand() {
        return new Command();
    }

    private Command getValidCommand() {
        return new Command("time", "http://time.tld/");
    }

    private List<SubCommand> getValidSubCommand() {
        Arguments arguments = Arguments.of(Collections.singletonList(
            ArgumentFactory.getWithInvalidArgument("time", "/hello")
        ));

        SubCommand subCommand = new SubCommand();
        subCommand.setArguments(arguments);
        return Collections.singletonList(subCommand);
    }

    private Errors validate(Command command) {
        Errors errors = new BeanPropertyBindingResult(command, "command");
        subject.validate(command, errors);

        return errors;
    }

    private void assertError(Command command, String fieldName, String errorCode) {
        Errors errors = validate(command);

        assertThat(errors.getFieldError(fieldName).getCode(), is(equalTo(errorCode)));
    }

    private void assertNoErrors(Command command) {
        Errors errors = validate(command);

        assertThat(errors.getAllErrors(), empty());
    }
}
