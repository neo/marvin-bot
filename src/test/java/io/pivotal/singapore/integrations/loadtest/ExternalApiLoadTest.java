package io.pivotal.singapore.integrations.loadtest;


import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.pivotal.singapore.MarvinApplication;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MarvinApplication.class)
@WebAppConfiguration
@ActiveProfiles(profiles = "test")
@IntegrationTest("server.port:0")
public class ExternalApiLoadTest {
    @Value("${local.server.port}")
    private String portNumber;

    @Value("${api.slack.token}")
    private String slackToken;

    private String baseUrl;

    private String counterEndpoint;
    private String slackApiEndpoint;
    private String dummyEndpoint;

    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + portNumber + "/";
        counterEndpoint = baseUrl + "counter/?endpoint=dummy";
        dummyEndpoint = baseUrl + "dummy";
        slackApiEndpoint = baseUrl + "?text=dummy&token=" + slackToken;

        RestAssured.port = Integer.parseInt(portNumber);
    }

    @Test
    public void testFailedRequestsDoNotOverloadExternalService() throws Exception {
        createCommand();

        makeMultipleFailedRequests();

        when().
                get(counterEndpoint).
                then().
                body("counter", lessThan(100));
    }

    private void createCommand() {
        given().
                contentType(ContentType.JSON).
                content(getCommand().toString()).
                when().
                post("/api/v1/commands/").
                then().
                statusCode(SC_CREATED);
    }

    private void makeMultipleFailedRequests() throws Exception {
        Process process = Runtime.getRuntime().exec(new String[]{"ab", "-n", "100", slackApiEndpoint});
        int exitCode = process.waitFor();
        assertThat(exitCode, is(0));
    }

    private JSONObject getCommand() {
        return new JSONObject()
                .put("name", "dummy")
                .put("endpoint", dummyEndpoint)
                .put("defaultResponseSuccess", "I'm a success")
                .put("defaultResponseFailure", "I'm a failure")
                .put("method", "POST");
    }
}
