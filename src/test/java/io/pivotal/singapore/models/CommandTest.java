package io.pivotal.singapore.models;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CommandTest {

    @Test
    public void constructorTest() {
        Command command = new Command("name", "endpoint");
        assertThat(command.getName(), is("name"));
        assertThat(command.getEndpoint(), is("endpoint"));
        assertThat(command.getMethod(), is(RequestMethod.POST));
        assertThat(command.getSubCommands(), is(new ArrayList<>()));
    }

    @Test
    public void findSubCommandTest() {
        Command command = new Command("time", "http://exampleTimeService.com");

        SubCommand subCommand = new SubCommand();
        subCommand.setName("in");

        SubCommand otherSubCommand = new SubCommand();
        otherSubCommand.setName("at");

        List<SubCommand> subCommands = new ArrayList<>();
        subCommands.add(subCommand);
        subCommands.add(otherSubCommand);
        command.setSubCommands(subCommands);

        assertThat(command.findSubCommand("in").get(), is(subCommand));
        assertThat(command.findSubCommand("missing"), is(Optional.empty()));
        assertThat(command.findSubCommand(null), is(Optional.empty()));
    }
}
