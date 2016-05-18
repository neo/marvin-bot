package io.pivotal.singapore.marvin.slack.interactions;

import io.pivotal.singapore.marvin.core.MessageType;

public class VerifyApiToken implements Interaction {
    private final Interaction decoratedObject;
    private final String apiSlackToken;
    private final InteractionRequest interactionRequest;

    public VerifyApiToken(Interaction interaction, String apiSlackToken, InteractionRequest interactionRequest) {
        this.decoratedObject = interaction;
        this.apiSlackToken = apiSlackToken;
        this.interactionRequest = interactionRequest;
    }

    @Override
    public InteractionResult run() {
        if (isInvalidToken(interactionRequest)) {
            return new InteractionResult.Builder()
                .message("Unrecognized token")
                .messageType(MessageType.user)
                .validationError()
                .build();
        }
        return decoratedObject.run();
    }

    private boolean isInvalidToken(InteractionRequest request) {
        return request.getToken().isEmpty() ||
            !this.apiSlackToken.equals(request.getToken());
    }
}
