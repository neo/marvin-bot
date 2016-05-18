package io.pivotal.singapore.marvin.slack;

import com.google.common.base.Preconditions;
import io.pivotal.singapore.marvin.slack.interactions.InteractionRequest;
import io.pivotal.singapore.marvin.utils.ValidationObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SlackInteractionRequest implements InteractionRequest {
    private final IncomingSlackRequest incomingSlackRequest;
    private final SlackTextParser slackTextParser;

    SlackInteractionRequest(IncomingSlackRequest incomingSlackRequest) {
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

class SlackTextParser extends ValidationObject<SlackTextParser> {
    private String subCommand = "";
    private String arguments = "";

    private String[] tokens;

    SlackTextParser(String textCommand) {
        tokens = textCommand.trim().split(" ");

        if (tokens.length > 1) {
            this.subCommand = tokens[1];
        }
        if(tokens.length > 2) {
            Queue<String> argumentTokens = new LinkedList(Arrays.asList(tokens));
            argumentTokens.poll();
            argumentTokens.poll();

            this.arguments = String.join(" ", argumentTokens);
        }
    }

    String getCommand() {
        return tokens[0];
    }

    String getSubCommand() {
        return subCommand;
    }

    String getArguments() {
        return arguments;
    }

    @Override
    public SlackTextParser self() {
        return this;
    }
}
