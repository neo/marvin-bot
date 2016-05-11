package io.pivotal.singapore.marvin.slack;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

public class SlackRequest {
    @Getter @Setter @NotBlank private String token;
    @Getter @Setter private String teamId;
    @Getter @Setter private String teamDomain;
    @Getter @Setter private String channelId;
    @Getter @Setter private String channelName;
    @Getter @Setter private String userId;
    @Getter @Setter private String userName;
    @Getter @Setter private String command;
    @Getter @Setter @NotBlank private String text;
    @Getter @Setter private String responseUrl;

    private String slackToken;

    public SlackRequest(Map<String, String> params, String slackToken) {
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
    public boolean isRecognizedToken() {
        return this.token != null && this.token.equals(this.slackToken);
    }
}

