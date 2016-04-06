package io.pivotal.singapore.services.commands;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(MockitoJUnitRunner.class)
public class RemoteCommandTest {
    private RestTemplate restTemplate;
    private RemoteCommand remoteCommand;

    private HashMap<String, String> params;

    private MockRestServiceServer mockServer;

    private String endpoint = "http://example.com/";

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        params = new HashMap<>();
        params.put("rawCommand", "time location Singapore");
    }

    @Test
    public void callsEndpointWithPost() throws Exception {
        setupMockServer(endpoint, HttpMethod.POST);

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
    }

    @Test
    public void callsEndpointWithGet() throws Exception {
        String baseUrl = endpoint;
        setupMockServer(baseUrl + "?rawCommand=time%20location%20Singapore", HttpMethod.GET);

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.GET, baseUrl, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
    }

    @Test
    public void callsEndpointWithPut() throws Exception {
        setupMockServer(endpoint, HttpMethod.PUT);

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.PUT, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
    }

    @Test
    public void callsEndpointWithDelete() throws Exception {
        setupMockServer(endpoint, HttpMethod.DELETE);

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.DELETE, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
    }

    @Test
    public void callsEndpointWithPatch() throws Exception {
        setupMockServer("http://example.com/", HttpMethod.PATCH);

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.PATCH, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
    }

    @Test
    public void httpBadRequestErrorsReturnedFromEndpoint() {
        setupMockServerWithPostResponse(withBadRequest()
                .body("{ \"status\" : \"FAILED!!!!!\" }")
                .contentType(MediaType.APPLICATION_JSON)
        );

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("status"), is(equalTo("FAILED!!!!!")));
    }

    @Test
    public void httpMalformedJSONErrorsReturnedFromEndpoint() {
        setupMockServerWithPostResponse(withBadRequest()
                .body("\"status\" : \"FAILED!!!!!\" }")
                .contentType(MediaType.APPLICATION_JSON)
        );

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("errorBody"), is(equalTo("\"status\" : \"FAILED!!!!!\" }")));
    }

    @Test
    public void httpErrorsReturnedFromEndpointNonJSON() {
        setupMockServerWithPostResponse(withBadRequest().body("FAILED!!!!!"));

        remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
        Map<String, String> result = remoteCommand.execute();

        assertThat(result.get("errorBody"), is(equalTo("FAILED!!!!!")));
    }

    private void setupMockServer(String endpoint, HttpMethod method) {
        mockServer.expect(requestTo(endpoint))
                .andExpect(method(method))
                .andRespond(withSuccess("{ \"status\" : \"SUCCESS!!!!!!!\" }", MediaType.APPLICATION_JSON));
    }

    private void setupMockServerWithPostResponse(DefaultResponseCreator returnResponse) {
        mockServer.expect(requestTo("http://example.com/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(returnResponse);
    }
}