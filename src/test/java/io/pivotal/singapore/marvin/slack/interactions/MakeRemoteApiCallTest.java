package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceRequest;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MakeRemoteApiCallTest {

    private CommandRepository commandRepository;
    private RemoteApiService remoteApiService;
    private InteractionRequest interactionRequest;

    @Before
    public void setUp() throws Exception {
        commandRepository = mock(CommandRepository.class);
        when(commandRepository.findOneByName("time")).thenReturn(Optional.of(new Command()));

        remoteApiService = mock(RemoteApiService.class);

        interactionRequest = mock(InteractionRequest.class);
        when(interactionRequest.getCommand()).thenReturn("time");
        when(interactionRequest.getArguments()).thenReturn("");
    }

    @Test
    public void makesRemoteApiServiceCall() throws Exception {
        RemoteApiServiceResponse remoteApiServiceResponse = new RemoteApiServiceResponse(true, Collections.emptyMap(), "", "");
        when(remoteApiService.call(any(ICommand.class), any(RemoteApiServiceRequest.class))).thenReturn(remoteApiServiceResponse);

        MakeRemoteApiCall subject = new MakeRemoteApiCall(remoteApiService, commandRepository, interactionRequest);
        subject.run();

        verify(remoteApiService, times(1)).call(any(ICommand.class), any(RemoteApiServiceRequest.class));
    }

    @Test
    public void givesInteractionResultBasedOnRemoteServiceCall() throws Exception {
        // arrange
        RemoteApiServiceResponse remoteApiServiceResponse = mock(RemoteApiServiceResponse.class);
        when(remoteApiServiceResponse.getMessage()).thenReturn("some message");
        when(remoteApiServiceResponse.getMessageType()).thenReturn(Optional.of(MessageType.channel));
        when(remoteApiServiceResponse.isSuccessful()).thenReturn(true);

        when(remoteApiService.call(any(ICommand.class), any(RemoteApiServiceRequest.class))).thenReturn(remoteApiServiceResponse);

        // act
        MakeRemoteApiCall subject = new MakeRemoteApiCall(remoteApiService, commandRepository, interactionRequest);
        InteractionResult result = subject.run();

        // assert
        assertThat(result.getMessage(), is(equalTo("some message")));
        assertThat(result.getMessageType(), is(equalTo(MessageType.channel)));
        assertThat(result.getType(), is(equalTo(InteractionResultType.SUCCESS)));
    }
}
