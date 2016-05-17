package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.slack.interactions.InteractionResult;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCall;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallSlackRequest;
import io.pivotal.singapore.marvin.slack.interactions.VerifyApiToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value="/", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OutgoingSlackResponse> index(@RequestParam Map<String, String> params) throws Exception {
        IncomingSlackRequest incomingSlackRequest = new IncomingSlackRequest(params, SLACK_TOKEN);

        if (incomingSlackRequest.isInvalid()) {
            if (incomingSlackRequest.hasErrorFor("recognizedToken")) {
                return ResponseEntity.badRequest().body(new OutgoingSlackResponse("Unrecognized token"));
            }
            return ResponseEntity.ok(new OutgoingSlackResponse("This will all end in tears."));
        }

        MakeRemoteApiCall makeRemoteApiCall = new MakeRemoteApiCall(remoteApiService, commandRepository);
        VerifyApiToken verifyApiToken = new VerifyApiToken(makeRemoteApiCall);
        InteractionResult result = verifyApiToken.run(new MakeRemoteApiCallSlackRequest(incomingSlackRequest));

        return ResponseEntity.ok(new OutgoingSlackResponse(result));
    }
}
