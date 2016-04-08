package io.pivotal.singapore.marvin.commands;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
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

@RunWith(Enclosed.class)
public class RemoteCommandTest {

    abstract static class Base {
        RestTemplate restTemplate;
        RemoteCommand remoteCommand;
        HashMap<String, String> params;
        MockRestServiceServer mockServer;
        String endpoint = "http://example.com/";

        void setupMockServer(String endpoint, HttpMethod method) {
            mockServer.expect(requestTo(endpoint))
                .andExpect(method(method))
                .andRespond(withSuccess("{ \"status\" : \"SUCCESS!!!!!!!\" }", MediaType.APPLICATION_JSON));
        }

        void setupMockServerWithPostResponse(DefaultResponseCreator returnResponse) {
            mockServer.expect(requestTo("http://example.com/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(returnResponse);
        }

        @Before
        public void setUp() {
            restTemplate = new RestTemplate();
            mockServer = MockRestServiceServer.createServer(restTemplate);

            params = new HashMap<>();
            params.put("rawCommand", "time location Singapore");
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class InterpolateParamsInUrlsForAllMethods extends Base {

        @Test
        public void get() {
            params.remove("rawCommand");

            assertSuccessForMethod(HttpMethod.GET, "http://example.com/events/42/cancel?id=42");
        }

        @Test
        public void put() {
            assertSuccessForMethod(HttpMethod.PUT);
        }

        @Test
        public void post() {
            assertSuccessForMethod(HttpMethod.POST);
        }

        @Test
        public void delete() {
            assertSuccessForMethod(HttpMethod.DELETE);
        }

        @Test
        public void patch() {
            assertSuccessForMethod(HttpMethod.PATCH);
        }

        private void assertSuccessForMethod(HttpMethod method) {
            assertSuccessForMethod(method, "http://example.com/events/42/cancel");
        }

        private void assertSuccessForMethod(HttpMethod method, String endpoint) {
            setupMockServer(endpoint, method);

            params.put("id", "42");
            remoteCommand = new RemoteCommand(restTemplate, RequestMethod.valueOf(method.toString()), "http://example.com/events/{id}/cancel", params);

            Map<String, String> result = remoteCommand.execute();

            assertThat(result.get("status"), is(equalTo("SUCCESS!!!!!!!")));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class ErrorConditions extends Base {

        @Test
        public void validJsonResponse() {
            Map<String, String> result = mockJsonAndDoRequest("{ \"status\" : \"FAILED!!!!!\" }");

            assertThat(result.get("status"), is(equalTo("FAILED!!!!!")));
        }

        @Test
        public void malformedJsonReturnsFullResponse() {
            String jsonBody = "\"status\" : \"FAILED!!!!!\" }";
            Map<String, String> result = mockJsonAndDoRequest(jsonBody);

            assertThat(result.get("errorBody"), is(equalTo(jsonBody)));
        }

        @Test
        public void nonJsonResponseReturnsFullResponse() {
            setupMockServerWithPostResponse(withBadRequest().body("FAILED!!!!!"));

            remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
            Map<String, String> result = remoteCommand.execute();

            assertThat(result.get("errorBody"), is(equalTo("FAILED!!!!!")));
        }

        private Map<String, String> mockJsonAndDoRequest(String jsonBody) {
            setupMockServerWithPostResponse(withBadRequest()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
            );

            remoteCommand = new RemoteCommand(restTemplate, RequestMethod.POST, endpoint, params);
            return remoteCommand.execute();
        }
    }
}
