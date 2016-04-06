package io.pivotal.singapore.marvin.samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class EchoConsumerControllerTest {

    @InjectMocks
    private EchoConsumerController controller;

    @Test
    public void commandHandlerReturnsArgumentsAndMethod() throws IOException {
        Map<String, String> actual = controller.handleCommand(ImmutableMap.of("a", 1), HttpMethod.DELETE);
        Map<String, String> message = jsonToMap(actual.get("message"));
        Map<String, String> arguments = jsonToMap(message.get("arguments"));

        assertThat(message, hasEntry("method", "DELETE"));
        assertThat(arguments, hasEntry("a", 1));
        assertThat(actual, hasEntry("message_type", "channel"));
    }

    @Test
    public void subCommandHandlerReturnsSubCommand() throws IOException {
        Map<String, String> actual = controller.handleSubcommand(ImmutableMap.of("a", 1), HttpMethod.DELETE, "sub_command");
        Map<String, String> message = jsonToMap(actual.get("message"));
        Map<String, String> arguments = jsonToMap(message.get("arguments"));

        assertThat(message, allOf(hasEntry("method", "DELETE"),
                hasEntry("subCommand", "sub_command")));
        assertThat(arguments, hasEntry("a", 1));
        assertThat(actual, hasEntry("message_type", "channel"));
    }

    private Map jsonToMap(String mes) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(mes, Map.class);
    }
}
