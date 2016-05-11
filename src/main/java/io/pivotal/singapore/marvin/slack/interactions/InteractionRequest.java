package io.pivotal.singapore.marvin.slack.interactions;

public interface InteractionRequest {
    String getArguments();
    String getChannelName();
    String getCommand();
    String getUserName();
    String getSubCommand();
    String getText();
    String getToken();
}
