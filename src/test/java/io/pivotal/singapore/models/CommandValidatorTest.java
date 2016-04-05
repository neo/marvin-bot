package io.pivotal.singapore.models;

import io.pivotal.singapore.marvin.commands.arguments.ArgumentFactory;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CommandValidatorTest {

    @Test
    public void checkCommandName() {
        Command command = new Command();

        SubCommand subCommand = new SubCommand();
        List<SubCommand> subCommands = Arrays.asList(subCommand);

        command.setSubCommands(subCommands);

        Errors errors = new BeanPropertyBindingResult(command, "command");
        CommandValidator subject = new CommandValidator();

        subject.validate(command, errors);

        assertThat(errors.getFieldError("name").getCode(), is(equalTo("name.empty")));

        errors = new BeanPropertyBindingResult(command, "command");
        command.setName("I pity the fool");
        subject.validate(command, errors);

        assertThat(errors.getFieldError("name").getCode(), is(equalTo("name.nospaces")));
    }

    @Test
    public void checkEndpoint() {
        Command command = new Command();
        command.setName("hello");

        Errors errors = new BeanPropertyBindingResult(command, "command");
        CommandValidator subject = new CommandValidator();

        subject.validate(command, errors);
        assertThat(errors.getFieldError("endpoint").getCode(), is(equalTo("endpoint.invalidUrl")));

        errors = new BeanPropertyBindingResult(command, "command");
        command.setEndpoint("BLAAAARGH");

        subject.validate(command, errors);
        assertThat(errors.getFieldError("endpoint").getCode(), is(equalTo("endpoint.invalidUrl")));

        command.setEndpoint("https://hello.tld/1");
        errors = new BeanPropertyBindingResult(command, "command");

        subject.validate(command, errors);
        assertThat(errors.getAllErrors(), empty());

        command.setEndpoint("http://hello.tld/1");
        errors = new BeanPropertyBindingResult(command, "command");

        subject.validate(command, errors);
        assertThat(errors.getAllErrors(), empty());
    }

    @Test
    public void subCommandArguments() {
        Command command = new Command();
        command.setName("hello");
        Arguments arguments = Arguments.of(Arrays.asList(
            ArgumentFactory.getWithEmptyArgument("time", "/hello")
        ));

        SubCommand subCommand = new SubCommand();
        subCommand.setArguments(arguments);
        List<SubCommand> subCommands = Arrays.asList(subCommand);
        command.setSubCommands(subCommands);

        Errors errors = new DirectFieldBindingResult(command, "command");
        CommandValidator subject = new CommandValidator();

        subject.validate(command, errors);
        assertThat(
            errors.getFieldError("subCommands[0].arguments").getCode(),
            is(equalTo("arguments.time.invalidUrl"))
        );
    }

}
