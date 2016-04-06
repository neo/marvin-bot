package io.pivotal.singapore.marvin.samples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EchoConsumerController {

    @RequestMapping(value="/echo", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> handleCommand(@RequestBody Map<String, Object> params,
                                     HttpMethod method) throws JsonProcessingException {

        return buildEchoResponse(params, method);
    }

    @RequestMapping(value="/echo/{subCommand}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> handleSubcommand(@RequestBody Map<String, Object> params,
                                     HttpMethod method,
                                     @PathVariable String subCommand) throws JsonProcessingException {

        return buildEchoResponse(params, method, subCommand);
    }

    private Map<String, String> buildEchoResponse(Map<String, Object> params,
                                                  HttpMethod method,
                                                  String... subCommands) throws JsonProcessingException {
        ObjectWriter printer = new ObjectMapper().writerWithDefaultPrettyPrinter();

        final ImmutableMap.Builder<String, String> echo =
                new ImmutableMap.Builder<String, String>()
                        .put("method", method.toString())
                        .put("arguments", printer.writeValueAsString(params));

        if (subCommands.length > 0) {
            echo.put("subCommand", subCommands[0]);
        }

        final ImmutableMap<String, String> response =
                new ImmutableMap.Builder<String, String>()
                        .put("message_type", "channel")
                        .put("message", printer.writeValueAsString(echo.build()))
                        .build();

        return response;
    }

}
