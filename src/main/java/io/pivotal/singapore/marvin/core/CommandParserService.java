package io.pivotal.singapore.marvin.core;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@Service
public class CommandParserService {

    // FIXME: Use a typed object for this...
    public HashMap<String,String> parse(@NotNull String textComamnd) {
        HashMap<String, String> result = new HashMap<>();
        result.put("command", "");
        result.put("sub_command", "");
        result.put("arguments", "");

        String[] tokens = textComamnd.trim().split(" ");
        if (tokens[0].isEmpty()) return result;

        if(tokens.length > 0)
            result.put("command", tokens[0]);
        if(tokens.length > 1)
            result.put("sub_command", tokens[1]);
        if(tokens.length > 2) {
            String arguments = textComamnd.replace(tokens[0], "").replace(tokens[1], "").trim();
            result.put("arguments", arguments);
        }

        return result;
    }
}
