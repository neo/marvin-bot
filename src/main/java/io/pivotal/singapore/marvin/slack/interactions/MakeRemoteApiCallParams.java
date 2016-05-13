package io.pivotal.singapore.marvin.slack.interactions;

public interface MakeRemoteApiCallParams {
    String getArguments();
    String getChannelName();
    String getCommand();
    String getUserName();
    String getSubCommand();
    String getText();
}
