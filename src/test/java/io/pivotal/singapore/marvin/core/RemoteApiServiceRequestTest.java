package io.pivotal.singapore.marvin.core;

import com.google.common.collect.ImmutableMap;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallRequest;
import io.pivotal.singapore.marvin.utils.FrozenTimeMachine;
import org.junit.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoteApiServiceRequestTest {
    @Test
    public void serializesInputs() throws Exception {
        // mock collaborators
        MakeRemoteApiCallRequest mockedRequest = mock(MakeRemoteApiCallRequest.class);
        when(mockedRequest.getChannelName()).thenReturn("some channel name");
        when(mockedRequest.getCommand()).thenReturn("some command");
        when(mockedRequest.getUserName()).thenReturn("some user name");

        ArgumentParsedResultList mockedArgumentParsedResultList = mock(ArgumentParsedResultList.class);
        when(mockedArgumentParsedResultList.getArgumentAndMatchResultMap())
            .thenReturn(ImmutableMap.of("some argument name", "some matched result"));

        FrozenTimeMachine mockedClock = mock(FrozenTimeMachine.class);
        when(mockedClock.instant()).thenReturn(Instant.now());
        when(mockedClock.getZone()).thenCallRealMethod();

        // act
        RemoteApiServiceRequest request = new RemoteApiServiceRequest(mockedRequest, mockedArgumentParsedResultList, mockedClock);
        Map<String, String> actualResult = request.toMap();

        // assert
        Map<String, String> expectedResult = ImmutableMap
            .of(
                "channel", "some channel name",
                "command", "some command",
                "received_at", ZonedDateTime.now(mockedClock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                "username", "some user name",
                "some argument name", "some matched result"
            );
        for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
            assertThat(actualResult, hasEntry(entry.getKey(), entry.getValue()));
        }
    }
}
