package io.pivotal.singapore.marvin.slack;

import io.pivotal.singapore.marvin.utils.ValidationObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SlackTextParser extends ValidationObject<SlackTextParser> {
    private String subCommand = "";
    private String arguments = "";

    private String[] tokens;

    public SlackTextParser(String textCommand) {
        tokens = textCommand.trim().split(" ");

        if (tokens.length > 1) {
            this.subCommand = tokens[1];
        }
        if(tokens.length > 2) {
            Queue argumentTokens = new LinkedList(Arrays.asList(tokens));
            argumentTokens.poll();
            argumentTokens.poll();

            this.arguments = String.join(" ", argumentTokens);
        }
    }

    public String getCommand() {
        return tokens[0];
    }

    public String getSubCommand() {
        return subCommand;
    }

    public String getArguments() {
        return arguments;
    }

    @Override
    public SlackTextParser self() {
        return this;
    }
}
