package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.utils.ValidationObject;
import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.AssertTrue;
import java.util.Map;

public class IncomingSlackRequest extends ValidationObject<IncomingSlackRequest> {
    @Getter @NotBlank final private String token;
    @Getter @NotBlank final private String text;
    @Getter @NotBlank final private String channelName;
    @Getter @NotBlank final private String userName;

    private String slackToken;

    public IncomingSlackRequest(Map<String, String> params, String slackToken) {
        this.channelName = params.getOrDefault("channel_name", null);
        this.text = params.getOrDefault("text", null);
        this.token = params.getOrDefault("token", null);
        this.userName = params.getOrDefault("user_name", null);
        this.slackToken = slackToken;
    }

    @AssertTrue
    private boolean isRecognizedToken() {
        return this.token != null && this.token.equals(this.slackToken);
    }

    @Override
    public IncomingSlackRequest self() {
        return this;
    }
}

