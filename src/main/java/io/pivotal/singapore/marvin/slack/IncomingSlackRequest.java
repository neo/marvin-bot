package io.pivotal.singapore.marvin.slack;

import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.AssertTrue;
import java.util.Map;

public class IncomingSlackRequest extends ValidationObject<IncomingSlackRequest> {
    @Getter @NotBlank private String token;
    @Getter @NotBlank private String text;
    @Getter @NotBlank private String channelName;
    @Getter @NotBlank private String userName;
    private String teamId;
    private String teamDomain;
    private String channelId;
    private String userId;
    private String command;
    private String responseUrl;

    private String slackToken;

    public IncomingSlackRequest(Map<String, String> params, String slackToken) {
        this.token = params.getOrDefault("token", null);
        this.teamId = params.getOrDefault("team_id", null);
        this.teamDomain = params.getOrDefault("team_domain", null);
        this.channelId = params.getOrDefault("channel_id", null);
        this.channelName = params.getOrDefault("channel_name", null);
        this.userId = params.getOrDefault("user_id", null);
        this.userName = params.getOrDefault("user_name", null);
        this.command = params.getOrDefault("command", null);
        this.text = params.getOrDefault("text", null);
        this.responseUrl = params.getOrDefault("response_url", null);
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

