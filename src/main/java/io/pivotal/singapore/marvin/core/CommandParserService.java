package io.pivotal.singapore.marvin.core;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class CommandParserService {

    // FIXME: Use a typed object for this...
    public HashMap<String, String> parse(@NotNull String textCommand) {
        HashMap<String, String> result = new HashMap<>();
        result.put("command", "");
        result.put("sub_command", "");
        result.put("arguments", "");

        String[] tokens = textCommand.trim().split(" ");
        if (tokens[0].isEmpty()) return result;

        if(tokens.length > 0)
            result.put("command", tokens[0]);
        if(tokens.length > 1)
            result.put("sub_command", tokens[1]);
        if(tokens.length > 2) {
            Queue argumentTokens = new LinkedList<>(Arrays.asList(tokens));
            argumentTokens.poll();
            argumentTokens.poll();

            String arguments = String.join(" ", argumentTokens);
            result.put("arguments", arguments);
        }

        return result;
    }
}
