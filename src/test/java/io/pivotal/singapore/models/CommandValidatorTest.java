package io.pivotal.singapore.models;

import io.pivotal.singapore.marvin.commands.arguments.ArgumentFactory;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CommandValidatorTest {
    private static CommandValidator subject = new CommandValidator();

    @Test
    public void checkCommandName() {
        Command command = getInvalidCommand();
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
    public void subCommandArguments() {
        Command command = getValidCommand();
        command.setSubCommands(getValidSubCommand());

        assertError(command, "subCommands[0].arguments", "arguments.time.invalidUrl");
    }

    private Command getInvalidCommand() {
        return new Command();
    }

    private Command getValidCommand() {
        return new Command("time", "http://time.tld/");
    }

    private List<SubCommand> getValidSubCommand() {
        Arguments arguments = Arguments.of(Collections.singletonList(
            ArgumentFactory.getWithEmptyArgument("time", "/hello")
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
