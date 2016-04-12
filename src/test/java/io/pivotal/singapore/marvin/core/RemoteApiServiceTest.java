package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.utils.a;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
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

        assertEquals(
            "Current status is: SUCCESS",
            a.remoteApiService.w(restTemplate).build()
                .call(
                    a.command
                        .w(a.defaultResponse
                            .w("success", "Current status is: {status}")
                            .build())
                        .build(),
                    new HashMap<String, String>()
                ).getMessage()
        );
    }
}
