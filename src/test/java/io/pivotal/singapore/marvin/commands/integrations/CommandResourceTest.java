package io.pivotal.singapore.marvin.commands.integrations;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;

import java.util.Arrays;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;
import io.pivotal.singapore.marvin.commands.Command;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

public class CommandResourceTest extends IntegrationBase {

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
        String json = getCommand().toString();

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
    public void testCommandsUpsertViaName() throws Exception {
        JSONObject originalJson = getCommand();

        given().
                contentType(ContentType.JSON).
                content(originalJson.toString()).
        when().
                post("/api/v1/commands/").
        then().

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
                body("_embedded.commands[0].name", is("command")).
                body("_embedded.commands[0].endpoint", is("http://localhost/9")).
                body("_embedded.commands[0].defaultResponseSuccess", is("Its successful")).
                body("_embedded.commands[0].defaultResponseFailure", is("Its failed")).
                body("_embedded.commands[0].method", is("POST"));
    }

    @Test
    public void testCommandCreationWithSubCommands() {
        JSONArray argsArray = new JSONArray(Arrays.asList(
            map("zzz", "/form1/"),
            map("lll", "/force-json-esc\\aping/"),
            map("aaa", "/form3/")
        ));
        JSONObject json = getCommand()
                .put("subCommands", getSubCommands(argsArray));

        String commandURI = given().

                contentType(ContentType.JSON).
                content(json.toString()).
        when().
                post("/api/v1/commands/").
        then().
                statusCode(SC_CREATED).
        extract().
            path("_links.self.href");

        // Ensure that the object is persisted and serialized/deserialized.
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
        Command command = new Command("command", "http://localhost/9");
        commandRepository.save(command);

        JSONArray subCommands = getSubCommands(new JSONArray()
                .put(map("zzz", "/form1"))
        );

        JSONObject commandJson = getCommand()
            .put("subCommands", subCommands);

        given().
            contentType(ContentType.JSON).
            content(commandJson.toString()).
        when().
            post("/api/v1/commands/").
        then().
            statusCode(SC_BAD_REQUEST).
            body("errors[0].property", is("subCommands[0].arguments")).
            body("errors[0].message", is("Argument 'zzz' has an invalid argument pattern '/form1'"));

    }

    @Test
    public void returnsErrorWhenUpsertingWithInvalidJSON() {
        String badJson = "{\"regex\": \"\\/(\\d+)\\/\"}";
        String badJsonErrorMessage = "Unrecognized character";
        given().
                contentType(ContentType.JSON).
                content(badJson).
        when().
                post("/api/v1/commands/").
        then().log().all().
                statusCode(SC_BAD_REQUEST).
                body("message", containsString(badJsonErrorMessage));
    }

    @Test
    public void returnsErrorWhenSubCommandNameIsBlank() {
        JSONObject command = getCommand();
        JSONObject subCommand = getSubCommand();
        subCommand.put("name", "");
        command.put("subCommands", new JSONArray().put(subCommand));

        given().
            contentType(ContentType.JSON).
            content(command.toString()).
        when().
            post("/api/v1/commands/").
        then().
            statusCode(SC_BAD_REQUEST).
            body("errors[0].property", is("subCommands[0].name")).
            body("errors[0].message", is("Name can't be empty"));
    }

    private JSONObject getCommand() {
        return new JSONObject()
            .put("name", "command")
            .put("endpoint", "http://localhost/9")
            .put("defaultResponseSuccess", "I'm a success")
            .put("defaultResponseFailure", "I'm a failure")
            .put("method", "GET");
    }

    private JSONObject getSubCommand() {
        return new JSONObject()
            .put("name", "bar")
            .put("endpoint", "http://somewhere.tld/bar")
            .put("method", "POST")
            .put("defaultResponseSuccess", "I'm a success")
            .put("defaultResponseFailure", "I'm a failure");
    }

    private JSONArray getSubCommands(JSONArray argsArray) {
        JSONArray subCommands = new JSONArray();
        subCommands.put(
                getSubCommand()
                        .put("arguments", argsArray)
        );

        return subCommands;
    }

    private Map map(String key, String value) {
        return Collections.singletonMap(key, value);
    }

}
