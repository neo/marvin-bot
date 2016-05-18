package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.core.MessageType;

public class VerifyApiToken implements Interaction {
    final private MakeRemoteApiCall decoratedObject;
    final private String apiSlackToken;

    public VerifyApiToken(MakeRemoteApiCall makeRemoteApiCall, String apiSlackToken) {
        this.decoratedObject = makeRemoteApiCall;
        this.apiSlackToken = apiSlackToken;
    }

    @Override
    public InteractionResult run(InteractionRequest request) {
        if (isInvalidToken(((MakeRemoteApiCallRequest) request))) {
            return new InteractionResult.Builder()
                .message("Unrecognized token")
                .messageType(MessageType.user)
                .validationError()
                .build();
        }
        return decoratedObject.run(request);
    }

    private boolean isInvalidToken(MakeRemoteApiCallRequest request) {
        return request.getToken().isEmpty() ||
            !this.apiSlackToken.equals(request.getToken());
    }
}
