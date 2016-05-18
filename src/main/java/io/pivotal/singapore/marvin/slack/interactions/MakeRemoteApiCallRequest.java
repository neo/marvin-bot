package io.pivotal.singapore.marvin.slack.interactions;

public interface MakeRemoteApiCallRequest extends InteractionRequest {
    String getArguments();
    String getChannelName();
    String getCommand();
    String getUserName();
    String getSubCommand();
    String getText();
    String getToken();
}
