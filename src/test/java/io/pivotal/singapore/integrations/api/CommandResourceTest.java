package io.pivotal.singapore.integrations.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.pivotal.singapore.MarvinApplication;
import io.pivotal.singapore.models.Command;
import io.pivotal.singapore.repositories.CommandRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MarvinApplication.class)
@WebAppConfiguration
@ActiveProfiles(profiles = "test")
@IntegrationTest("server.port:0")

public class CommandResourceTest {
    @Autowired private CommandRepository commandRepository;

    @Value("${local.server.port}")
    protected int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        commandRepository.deleteAll();
    }

    @Test
    public void testCommandsListing() throws Exception {
        commandRepository.save(new Command("cmd1", "http://localhost/1"));
        commandRepository.save(new Command("cmd2", "http://localhost/2"));

        when().
                get("/api/v1/commands").
        then().
                statusCode(SC_OK).
                contentType(MediaTypes.HAL_JSON_VALUE).
                body("_embedded.commands.size()", is(2)).
                body("_embedded.commands[0].name", is("cmd1")).
                body("_embedded.commands[0].endpoint", is("http://localhost/1")).
                body("_embedded.commands[1].name", is("cmd2")).
                body("_embedded.commands[1].endpoint", is("http://localhost/2"));
    }

    @Test
    public void testCommandsCreation() throws Exception {
        String json = new JSONObject()
                .put("name", "command")
                .put("endpoint", "http://localhost/9")
                .put("defaultResponseSuccess", "I'm a success")
                .put("defaultResponseFailure", "I'm a failure")
                .put("method", "GET")
                .toString();

        given().
                contentType(ContentType.JSON).
                content(json).
        when().
                post("/api/v1/commands/").
        then().
                statusCode(SC_CREATED);

        when().
                get("/api/v1/commands").
        then().
                body("_embedded.commands.size()", is(1)).
                body("_embedded.commands[0].name", is("command")).
                body("_embedded.commands[0].defaultResponseSuccess", is("I'm a success")).
                body("_embedded.commands[0].defaultResponseFailure", is("I'm a failure")).
                body("_embedded.commands[0].endpoint", is("http://localhost/9"));
    }

    @Test
    public void testCommandsUpdateViaName() throws Exception {
        JSONObject originalJson = new JSONObject()
                .put("name", "foobar")
                .put("endpoint", "http://localhost/9")
                .put("defaultResponseSuccess", "I'm a success")
                .put("defaultResponseFailure", "I'm a failure")
                .put("method", "GET");

        given().
                contentType(ContentType.JSON).
                content(originalJson.toString()).
        when().
                post("/api/v1/commands/").
        then().
            log().all().
                statusCode(SC_CREATED);

        JSONObject newJson = originalJson.put("method", "POST")
                            .put("defaultResponseSuccess", "Its successful")
                            .put("defaultResponseFailure", "Its failed");

        given().
                contentType(ContentType.JSON).
                content(newJson.toString()).
        when().
                post("/api/v1/commands/").
        then().
                statusCode(SC_CREATED);

        when().
                get("/api/v1/commands/").
        then().
                content("page.totalElements", equalTo(1)).
                body("_embedded.commands[0].name", is("foobar")).
                body("_embedded.commands[0].endpoint", is("http://localhost/9")).
                body("_embedded.commands[0].defaultResponseSuccess", is("Its successful")).
                body("_embedded.commands[0].defaultResponseFailure", is("Its failed")).
                body("_embedded.commands[0].method", is("POST"));
    }

    @Test
    public void testCommandCreationWithSubCommands() {
        JSONArray subCommands = new JSONArray();
        JSONArray argsArray = new JSONArray();

        HashMap<String, String> arg1 = new HashMap<>();
        arg1.put("zzz", "/form1/");
        argsArray.put(arg1);

        HashMap<String, String> arg2 = new HashMap<>();
        arg2.put("lll", "/force-json-esc\\aping/");
        argsArray.put(arg2);

        HashMap<String, String> arg3 = new HashMap<>();
        arg3.put("aaa", "/form3/");
        argsArray.put(arg3);

        JSONObject subCommandObject = new JSONObject()
                .put("name", "bar")
                .put("endpoint", "http://somewhere.tld/bar")
                .put("method", "POST")
                .put("defaultResponseSuccess", "I'm a success")
                .put("defaultResponseFailure", "I'm a failure")
                .put("arguments", argsArray);

        subCommands.put(subCommandObject);


        JSONObject json = new JSONObject()
                .put("name", "pity")
                .put("endpoint", "http://localhost/9")
                .put("method", "GET")
                .put("subCommands", subCommands);

        String commandURI = given().
                log().all().
                contentType(ContentType.JSON).
                content(json.toString()).
        when().
                log().all().
                post("/api/v1/commands/").
        then().
                log().all().
                statusCode(SC_CREATED).
        extract().
            path("_links.self.href");

        // Ensure that the object is persisted and serialized/deserialized.
        given().
                log().all().
        when().
                get(commandURI).
        then().
                statusCode(SC_OK).
                body("subCommands[0].name", is("bar")).
                body("subCommands[0].method", is("POST")).
                body("subCommands[0].endpoint", is("http://somewhere.tld/bar")).
                body("subCommands[0].defaultResponseSuccess", is("I'm a success")).
                body("subCommands[0].defaultResponseFailure", is("I'm a failure")).
                body("subCommands[0].arguments[0].zzz", is("/form1/")).
                body("subCommands[0].arguments[1].lll", is("/force-json-esc\\aping/")).
                body("subCommands[0].arguments[2].aaa", is("/form3/"));
    }

    @Test
    public void returnsErrorResponseWhenInvalidArgumentSent() {
        Command command = new Command("time", "http://localhost/9");
        commandRepository.save(command);

        JSONArray subCommands = new JSONArray();
        JSONArray argsArray = new JSONArray();

        HashMap<String, String> arg1 = new HashMap<>();
        arg1.put("zzz", "/form1");  // doesn't have finishing / to denote a regex
        argsArray.put(arg1);

        JSONObject subCommandObject = new JSONObject()
            .put("name", "bar")
            .put("endpoint", "http://somewhere.tld/bar")
            .put("method", "POST")
            .put("defaultResponseSuccess", "I'm a success")
            .put("defaultResponseFailure", "I'm a failure")
            .put("arguments", argsArray);

        subCommands.put(subCommandObject);

        JSONObject commandJson = new JSONObject()
            .put("name", "time")
            .put("endpoint", "http://localhost/9")
            .put("method", "GET")
            .put("subCommands", subCommands);
        given().
            log().all().
            contentType(ContentType.JSON).
            content(commandJson.toString()).
            when().
            log().all().
            post("/api/v1/commands/").
            then().
            log().all().
            statusCode(SC_BAD_REQUEST).
            body("errors[0].property", is("subCommands[0].arguments")).
            body("errors[0].message", is("Invalid argument type for value '/form1'"));

    }
}
