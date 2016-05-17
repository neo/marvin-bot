package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.slack.interactions.InteractionResult;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCall;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallControllerAdapter;
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
            return errorResponse("This will all end in tears.");
        }

        MakeRemoteApiCall makeRemoteApiCall = new MakeRemoteApiCall(clock, remoteApiService, commandRepository);
        InteractionResult result = makeRemoteApiCall.run(new MakeRemoteApiCallControllerAdapter(incomingSlackRequest));

        // Compiles final response to Slack
        if (result.isSuccess()) {
            return successResponse(result);
        } else {
            return errorResponse(result.getMessage());
        }
    }

    HashMap<String, String> successResponse(InteractionResult result) {
        HashMap<String, String> response = new HashMap<>();
        response.put("response_type", getSlackResponseType(result.getMessageType()));
        response.put("text", result.getMessage());

        return response;
    }

    private HashMap<String, String> errorResponse(String message) {
        String responseType = getSlackResponseType(Optional.of(MessageType.user).orElse(MessageType.user));

        HashMap<String, String> response = new HashMap<>();
        response.put("response_type", responseType);
        response.put("text", message);

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
}

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unrecognized token")
class UnrecognizedApiToken extends Exception {
}
