package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.utils.ValidationObject;
import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Map;

class IncomingSlackRequest extends ValidationObject<IncomingSlackRequest> {
    @Getter @NotBlank final private String token;
    @Getter @NotBlank final private String text;
    @Getter @NotBlank final private String channelName;
    @Getter @NotBlank final private String userName;

    IncomingSlackRequest(Map<String, String> params) {
        this.channelName = params.getOrDefault("channel_name", null);
        this.text = params.getOrDefault("text", null);
        this.token = params.getOrDefault("token", null);
        this.userName = params.getOrDefault("user_name", null);
    }

    @Override
    public IncomingSlackRequest self() {
        return this;
    }
}

