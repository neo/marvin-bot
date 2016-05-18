package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.slack.SlackInteractionRequest;

public class VerifyApiToken implements Interaction {
    private final MakeRemoteApiCall decoratedObject;
    private final String apiSlackToken;

    public VerifyApiToken(MakeRemoteApiCall makeRemoteApiCall, String apiSlackToken) {
        this.decoratedObject = makeRemoteApiCall;
        this.apiSlackToken = apiSlackToken;
    }

    @Override
    public InteractionResult run(InteractionRequest request) {
        if (isInvalidToken((SlackInteractionRequest) request)) {
            return new InteractionResult.Builder()
                .message("Unrecognized token")
                .messageType(MessageType.user)
                .validationError()
                .build();
        }
        return decoratedObject.run(request);
    }

    private boolean isInvalidToken(SlackInteractionRequest request) {
        return request.getToken().isEmpty() ||
            !this.apiSlackToken.equals(request.getToken());
    }
}
