package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.slack.interactions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.composed.web.rest.json.GetJson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @GetJson("/")
    ResponseEntity<OutgoingSlackResponse> index(@RequestParam Map<String, String> params) throws Exception {
        IncomingSlackRequest incomingSlackRequest = new IncomingSlackRequest(params);

        if (incomingSlackRequest.isInvalid()) {
            return ResponseEntity.ok(new OutgoingSlackResponse("This will all end in tears."));
        }

        InteractionRequest interactionRequest = new SlackInteractionRequest(incomingSlackRequest);

        Interaction makeRemoteApiCall = new MakeRemoteApiCall(remoteApiService, commandRepository);
        Interaction verifyArgumentParsing = new VerifyArgumentParsing(makeRemoteApiCall, commandRepository);
        Interaction verifyApiToken = new VerifyApiToken(verifyArgumentParsing, SLACK_TOKEN);

        InteractionResult result = verifyApiToken.run(interactionRequest);

        OutgoingSlackResponse outgoingSlackResponse = new OutgoingSlackResponse(result);
        return ResponseEntity
            .status(outgoingSlackResponse.getStatus())
            .body(outgoingSlackResponse);
    }
}
