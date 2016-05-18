package io.pivotal.singapore.marvin.slack.interactions;

import com.google.common.base.Preconditions;
import io.pivotal.singapore.marvin.slack.IncomingSlackRequest;
import io.pivotal.singapore.marvin.slack.SlackTextParser;

public class MakeRemoteApiCallSlackRequest implements MakeRemoteApiCallRequest {
    private final IncomingSlackRequest incomingSlackRequest;
    private final SlackTextParser slackTextParser;

    public MakeRemoteApiCallSlackRequest(IncomingSlackRequest incomingSlackRequest) {
        Preconditions.checkArgument(incomingSlackRequest.isValid());
        this.incomingSlackRequest = incomingSlackRequest;
        this.slackTextParser = new SlackTextParser(incomingSlackRequest.getText());
    }

    @Override
    public String getArguments() {
        return slackTextParser.getArguments();
    }

    @Override
    public String getChannelName() {
        return incomingSlackRequest.getChannelName();
    }

    @Override
    public String getCommand() {
        return slackTextParser.getCommand();
    }

    @Override
    public String getUserName() {
        return incomingSlackRequest.getUserName();
    }

    @Override
    public String getSubCommand() {
        return slackTextParser.getSubCommand();
    }

    @Override
    public String getText() {
        return incomingSlackRequest.getText();
    }

    @Override
    public String getToken() {
        return incomingSlackRequest.getToken();
    }
}
