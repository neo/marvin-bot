package io.pivotal.singapore.marvin.commands.default_responses;

import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.utils.a;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


public class DefaultResponsesTest {

    @Test
    public void messagePutCanBeReadBack() {
        assertEquals(
            Optional.of("I'm a huge success!"),
            a.defaultResponse.build()
                .putMessage("success", "I'm a huge success!")
                .getMessage("success")
        );
    }

    @Test
    public void messageTypePutCanBeReadBack() {
        assertEquals(
            Optional.of(MessageType.channel),
            a.defaultResponse.build()
                .putMessageType("success", MessageType.channel)
                .getMessageType("success")
        );
    }

    @Test
    public void messageTypeStringPutCanBeReadBack() {
        assertEquals(
            Optional.of(MessageType.channel),
            a.defaultResponse.build()
                .putMessageType("success", "channel")
                .getMessageType("success")
        );
    }

    @Test
    public void messageTypeDefaultValueIsUser() {
        assertEquals(
            Optional.empty(),
            a.defaultResponse.build()
                .getMessageType("slartibartfast")
        );
    }

    @Test
    public void createFromMap() {
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("success", "I'm a success message");
        inputMap.put("successType", MessageType.channel.toString());

        DefaultResponses defaultResponses = DefaultResponses.from(inputMap);

        assertEquals(Optional.of("I'm a success message"), defaultResponses.getMessage("success"));
        assertEquals(Optional.of(MessageType.channel), defaultResponses.getMessageType("success"));
    }

    @Test
    public void toJson() {
        DefaultResponses defaultResponses = a
            .defaultResponse
            .w("success", "I'm a success message")
            .w("successType", "channel")
            .build();

        assertEquals(
            "{\"success\":\"I'm a success message\",\"successType\":\"channel\"}",
            defaultResponses.toJson()
        );
    }

    @Test
    public void fromJson() {
        String json = "{\"success\":\"I'm a success message\",\"successType\":\"channel\"}";

        assertEquals(
            Optional.of("I'm a success message"),
            DefaultResponses
                .from(json)
                .getMessage("success")
        );
    }

    @Test
    public void toMap() {
        DefaultResponses defaultResponses = a
            .defaultResponse
            .w("success", "I'm a success message")
            .build();

        Map expected = Collections.singletonMap("success", "I'm a success message");

        assertEquals(
            expected,
            defaultResponses.toMap()
        );
    }
}
