package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.core.MessageType;

public class VerifyApiToken implements Interaction {
    private final Interaction decoratedObject;
    private final String apiSlackToken;

    public VerifyApiToken(Interaction interaction, String apiSlackToken) {
        this.decoratedObject = interaction;
        this.apiSlackToken = apiSlackToken;
    }

    @Override
    public InteractionResult run(InteractionRequest request) {
        if (isInvalidToken(request)) {
            return new InteractionResult.Builder()
                .message("Unrecognized token")
                .messageType(MessageType.user)
                .validationError()
                .build();
        }
        return decoratedObject.run(request);
    }

    private boolean isInvalidToken(InteractionRequest request) {
        return request.getToken().isEmpty() ||
            !this.apiSlackToken.equals(request.getToken());
    }
}
