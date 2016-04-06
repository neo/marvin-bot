package io.pivotal.singapore.marvin.samples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class EchoConsumerController {

    @RequestMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> handleCommand(@RequestBody Map<String, Object> params,
                                             HttpMethod method) throws JsonProcessingException {

        return buildEchoResponse(params, method);
    }

    @RequestMapping(value = "/echo/{subCommand}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> handleSubcommand(@RequestBody Map<String, Object> params,
                                                HttpMethod method,
                                                @PathVariable String subCommand) throws JsonProcessingException, SimulatedError {

        Map<String, String> response;

        switch (subCommand) {
            case "error":
                response = buildErrorResponse();
                break;
            case "empty":
                response = buildEmptyResponse();
                break;
            case "flatten":
                response = buildFlatResponse(params, method, subCommand);
                break;
            default:
                response = buildEchoResponse(params, method, subCommand);
        }

        return response;
    }

    private Map<String, String> buildErrorResponse() throws SimulatedError {
        throw new SimulatedError();
    }

    private Map<String, String> buildEmptyResponse() throws SimulatedError {
        return ImmutableMap.of("message_type", "channel");
    }

    private Map<String, String> buildFlatResponse(Map<String, Object> params,
                                                  HttpMethod method,
                                                  String... subCommands) throws JsonProcessingException {

        return buildTopLevelResponse().
                putAll(buildEcho(params, method, subCommands).build()).
                build();
    }

    private Map<String, String> buildEchoResponse(Map<String, Object> params,
                                                  HttpMethod method,
                                                  String... subCommands) throws JsonProcessingException {

        return buildTopLevelResponse().put("message",
                convertToString(buildEcho(params, method, subCommands).build())).
                build();
    }

    private ImmutableMap.Builder<String, String> buildTopLevelResponse() {
        return new ImmutableMap.Builder<String, String>()
                .put("message_type", "channel");
    }

    private ImmutableMap.Builder<String, String> buildEcho(Map<String, Object> params, HttpMethod method, String... subCommands) throws JsonProcessingException {
        final ImmutableMap.Builder<String, String> echo =
                new ImmutableMap.Builder<String, String>()
                        .put("method", method.toString())
                        .put("arguments", convertToString(params));

        if (subCommands.length > 0) {
            echo.put("subCommand", subCommands[0]);
        }
        return echo;
    }

    private String convertToString(Map hash) throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hash);
    }
}

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "SimulatedError")
class SimulatedError extends Exception {
}
