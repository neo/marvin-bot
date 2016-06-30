package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.slack.interactions.*;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.composed.web.Get;
import org.springframework.composed.web.rest.json.GetJson;
import org.springframework.http.HttpStatus;
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

    @Value("${api.slack.token}")
    private String SLACK_TOKEN;

    @Value("${api.slack.client_id}")
    private String SLACK_CLIENT_ID;

    @GetJson("/")
    ResponseEntity<OutgoingSlackResponse> index(@RequestParam Map<String, String> params) throws Exception {
        IncomingSlackRequest incomingSlackRequest = new IncomingSlackRequest(params);

        if (incomingSlackRequest.isInvalid()) {
            return ResponseEntity.ok(new OutgoingSlackResponse("This will all end in tears."));
        }

        InteractionRequest interactionRequest = new SlackInteractionRequest(incomingSlackRequest);

        Interaction makeRemoteApiCall = new MakeRemoteApiCall(remoteApiService, commandRepository, interactionRequest);
        Interaction verifyArgumentParsing = new VerifyArgumentParsing(makeRemoteApiCall, commandRepository, interactionRequest);
        Interaction verifyApiToken = new VerifyApiToken(verifyArgumentParsing, SLACK_TOKEN, interactionRequest);

        InteractionResult result = verifyApiToken.run();

        OutgoingSlackResponse outgoingSlackResponse = new OutgoingSlackResponse(result);
        return ResponseEntity
            .status(outgoingSlackResponse.getStatus())
            .body(outgoingSlackResponse);
    }

    @Get("/start")
    public ResponseEntity start() {
        URIBuilder redirectUri = new URIBuilder()
            .setScheme("https")
            .setHost("slack.com/oauth/authorize")
            .addParameter("client_id", SLACK_CLIENT_ID)
            .addParameter("scope", "chat:write:user chat:write:bot");
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header("Location", redirectUri.toString())
            .build();
    }
}
