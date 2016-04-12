package io.pivotal.singapore.marvin.commands;

import io.pivotal.singapore.marvin.utils.a;
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

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(Enclosed.class)
public class RemoteCommandTest {

    abstract static class Base {
        RestTemplate restTemplate;
        HashMap<String, String> params;
        MockRestServiceServer mockServer;
        String endpoint = "http://example.com/api/echo";

        void setupMockServer(String endpoint, HttpMethod method) {
            mockServer.expect(requestTo(endpoint))
                .andExpect(method(method))
                .andRespond(withSuccess("{ \"status\" : \"SUCCESS!!!!!!!\" }", MediaType.APPLICATION_JSON));
        }

        void setupMockServerWithPostResponse(DefaultResponseCreator returnResponse) {
            mockServer.expect(requestTo(endpoint))
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

            assertEquals(
                "SUCCESS!!!!!!!",
                a.remoteCommand
                    .w(restTemplate)
                    .w(RequestMethod.valueOf(method.toString()))
                    .w("http://example.com/events/{id}/cancel")
                    .w(params)
                    .build()
                    .execute()
                    .get("status")
            );
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class ErrorConditions extends Base {

        @Test
        public void validJsonResponse() {
            assertEquals(
                "FAILED!!!!!",
                mockJsonAndDoRequest("{ \"status\" : \"FAILED!!!!!\" }").get("status")
            );
        }

        @Test
        public void malformedJsonReturnsFullResponse() {
            String jsonBody = "\"status\" : \"FAILED!!!!!\" }";

            assertEquals(
                jsonBody,
                mockJsonAndDoRequest(jsonBody)
                    .get("errorBody")
            );
        }

        @Test
        public void nonJsonResponseReturnsFullResponse() {
            setupMockServerWithPostResponse(withBadRequest().body("FAILED!!!!!"));

            assertEquals(
                "FAILED!!!!!",
                a.remoteCommand.w(restTemplate).w(params).build()
                    .execute()
                    .get("errorBody")
            );
        }


            assertThat(result.get("errorBody"), is(equalTo("FAILED!!!!!")));
        }

        private Map<String, String> mockJsonAndDoRequest(String jsonBody) {
            setupMockServerWithPostResponse(withBadRequest()
                .body(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
            );

            return a.remoteCommand.w(restTemplate).w(params).build()
                .execute();
        }
    }
}
