package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.utils.a;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(MockitoJUnitRunner.class)
public class RemoteApiServiceTest {
    @Test
    public void testHitRemoteCommandAndReturn() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo("http://example.com/api/echo"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{ \"status\" : \"SUCCESS\" }", MediaType.APPLICATION_JSON));

        RemoteApiServiceRequest mockedRequest = mock(RemoteApiServiceRequest.class);
        when(mockedRequest.getChannel()).thenReturn("some channel");

        assertEquals(
            "Current status is: SUCCESS",
            a.remoteApiService.w(restTemplate).build()
                .call(
                    a.command
                        .w(a.defaultResponse
                            .w("success", "Current status is: {status}")
                            .build())
                        .build(),
                    mockedRequest
                ).getMessage()
        );
    }
}
