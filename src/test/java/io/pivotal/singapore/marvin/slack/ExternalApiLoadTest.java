package io.pivotal.singapore.marvin.slack;


import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.pivotal.singapore.marvin.utils.IntegrationBase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.IntStream;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class ExternalApiLoadTest extends IntegrationBase {
    private String counterEndpoint;
    private String slackApiEndpoint;
    private String dummyEndpoint;

    @Before
    public void setUp() {
        String baseUrl = "http://localhost:" + port + "/";
        counterEndpoint = baseUrl + "counter/?endpoint=dummy";
        dummyEndpoint = baseUrl + "dummy";
        slackApiEndpoint = baseUrl + "?text=dummy&token=" + SLACK_TOKEN;

        RestAssured.port = port;
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
                post(commandApiPath).
                then().
                statusCode(SC_CREATED);
    }

    private void makeMultipleFailedRequests() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        IntStream.range(0, 100)
            .forEach((int i) -> {
                HttpGet httpGet = new HttpGet(slackApiEndpoint);
                try {
                    try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        assertThat(response.getStatusLine().getStatusCode(), is(200));
                        EntityUtils.consume(response.getEntity());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Some problem accessing webserver");
                }
            });
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
