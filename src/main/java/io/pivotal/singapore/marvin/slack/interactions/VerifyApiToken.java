package io.pivotal.singapore.marvin.slack.interactions;

public class VerifyApiToken {
    final private MakeRemoteApiCall decoratedObject;

    public VerifyApiToken(MakeRemoteApiCall makeRemoteApiCall) {
       this.decoratedObject = makeRemoteApiCall;
    }

    public InteractionResult run(MakeRemoteApiCallRequest request) {
        return decoratedObject.run(request);
    }
}
