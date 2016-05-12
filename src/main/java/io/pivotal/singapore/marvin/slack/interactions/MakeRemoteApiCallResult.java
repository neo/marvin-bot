package io.pivotal.singapore.marvin.slack.interactions;

import java.util.Map;

public class MakeRemoteApiCallResult {
    private InteractionResult result;

    public MakeRemoteApiCallResult(InteractionResult result) {
        this.result = result;
    }

    public String getMessageType() {
        return this.result.body.get("messageType");
    }

    public String getMessage() {
        return this.result.body.get("message");
    }

    public boolean isSuccess() {
        return result.isSuccess();
    }

    public Map<String, String> errors() {
        return result.errors;
    }
}
