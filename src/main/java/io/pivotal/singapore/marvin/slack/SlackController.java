package io.pivotal.singapore.marvin.slack;

import com.google.common.collect.ImmutableMap;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCall;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
class SlackController {
    @Autowired
    CommandRepository commandRepository;

    @Autowired
    RemoteApiService remoteApiService;

    @Autowired
    CommandParserService commandParserService;

    @Value("${api.slack.token}")
    private String SLACK_TOKEN;

    private Clock clock = Clock.systemUTC();

    @RequestMapping(value="/", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> index(@RequestParam Map<String, String> params) throws Exception {
        IncomingSlackRequest incomingSlackRequest = new IncomingSlackRequest(params, SLACK_TOKEN);

        if (incomingSlackRequest.isInvalid()) {
            if (incomingSlackRequest.hasErrorFor("recognizedToken")) {
                throw new UnrecognizedApiToken();
            }
            return defaultResponse();
        }

        // Parses into command, sub-command, args as token strings
        HashMap<String, String> parsedCommand = commandParserService.parse(incomingSlackRequest.getText());

        // Checks if Command exists
        MakeRemoteApiCall makeRemoteApiCall = new MakeRemoteApiCall(incomingSlackRequest, clock, remoteApiService, commandRepository);
        if (makeRemoteApiCall.isInvalid()) {
            if (makeRemoteApiCall.hasErrorFor("commandPresent")) {
                return defaultResponse();
            } else if (makeRemoteApiCall.hasErrorFor("subCommandPresent")) {
                return defaultResponse(String.format("This sub command doesn't exist for %s", parsedCommand.get("command")));
            }
        }

        MakeRemoteApiCallResult result = makeRemoteApiCall.run();

        // Compiles final response to Slack
        if (result.isSuccess()) {
            return textResponse(result);
        } else {
            return ImmutableMap.of("text", result.errors().get("argument"), "response_type", getSlackResponseType(MessageType.user));
        }
    }

    HashMap<String, String> textResponse(MakeRemoteApiCallResult result) {
        HashMap<String, String> response = new HashMap<>();
        response.put("response_type", result.getMessageType());
        response.put("text", result.getMessage());

        return response;
    }

    HashMap<String, String> textResponse(Optional<MessageType> messageType, String text) {
        String responseType = getSlackResponseType(messageType.orElse(MessageType.user));

        HashMap<String, String> response = new HashMap<>();
        response.put("response_type", responseType);
        response.put("text", text);

        return response;
    }

    private String getSlackResponseType(MessageType messageType) {
        switch (messageType) {
            case user:
                return "ephemeral";
            case channel:
                return "in_channel";
            default:
                throw new IllegalArgumentException(
                    String.format("MessageType '%s' is not configured for Slack", messageType.toString())
                );
        }
    }

    private HashMap<String, String> defaultResponse() {
        return defaultResponse("This will all end in tears.");
    }

    private HashMap<String, String> defaultResponse(String message) {
        return textResponse(Optional.of(MessageType.user), message);
    }
}

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unrecognized token")
class UnrecognizedApiToken extends Exception {
}
