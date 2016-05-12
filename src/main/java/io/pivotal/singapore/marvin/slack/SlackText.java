package io.pivotal.singapore.marvin.slack;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SlackText extends ValidationObject<SlackText> {
    private String subCommand = "";
    private String arguments = "";

    private String[] tokens;

    public SlackText(@NotBlank String textCommand) {
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

    @NotBlank
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
    public SlackText self() {
        return this;
    }
}
