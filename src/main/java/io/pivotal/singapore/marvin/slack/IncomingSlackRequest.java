package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.utils.ValidationObject;
import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Map;

class IncomingSlackRequest extends ValidationObject<IncomingSlackRequest> {
    @Getter
    @NotBlank(message = "Channel name cannot be empty")
    final private String channelName;

    @Getter
    @NotBlank(message = "User name cannot be empty")
    final private String userName;

    @Getter
    @NotBlank(message = "Token cannot be empty")
    final private String token;

    @Getter
    @NotBlank(message = "Text cannot be empty")
    final private String text;

    IncomingSlackRequest(Map<String, String> params) {
        this.channelName = params.getOrDefault("channel_name", null);
        this.text = params.getOrDefault("text", null);
        this.token = params.getOrDefault("token", null);
        this.userName = params.getOrDefault("user_name", null);
    }

    @Override
    public IncomingSlackRequest getTargetInstance() {
        return this;
    }
}

