package io.pivotal.singapore.marvin.commands.web;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandRepositoryEventHandlerTest {
    @Mock
    CommandRepository repository;

    @InjectMocks
    private CommandRepositoryEventHandler commandRepositoryEventHandler = new CommandRepositoryEventHandler();

    @Test
    public void testReplaceIdIfCommandNameExists() throws Exception {
        Command firstCommand = new Command("name", "endpoint");
        firstCommand.setId(500);
        when(repository.findOneByName("name")).thenReturn(Optional.of(firstCommand));

        Command secondCommand = new Command("name", "endpoint2");
        secondCommand.setId(600);

        commandRepositoryEventHandler.replaceIdIfCommandNameExists(secondCommand);

        assertThat(secondCommand.getId(), is(500L));
    }
}
