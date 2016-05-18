package io.pivotal.singapore.marvin.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.slack.interactions.InteractionResult;
import org.springframework.http.HttpStatus;

public class OutgoingSlackResponse {
    private String responseType;
    private String text;
    private InteractionResult interactionResult;

    public OutgoingSlackResponse(String text) {
        this.text = text;
        this.responseType = "ephemeral";
    }

    public OutgoingSlackResponse(InteractionResult interactionResult) {
        this.interactionResult = interactionResult;
    }

    @JsonProperty("response_type")
    public String getResponseType() {
        if (interactionResult == null) {
            return responseType;
        }

        MessageType messageType = interactionResult.getMessageType();
        switch (messageType) {
            case user:
                return "ephemeral";
            case channel:
                return "in_channel";
            default:
                throw new IllegalArgumentException(
                    String.format("MessageType '%s' is not configured for Slack", messageType)
                );
        }
    }

    @JsonProperty("text")
    public String getText() {
        if (interactionResult == null) {
            return text;
        }
        return interactionResult.getMessage();
    }

    public HttpStatus getStatus() {
        if (interactionResult == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        switch (interactionResult.getType()) {
            case ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case SUCCESS:
                return HttpStatus.OK;
            case VALIDATION:
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.I_AM_A_TEAPOT;
        }
    }
}
