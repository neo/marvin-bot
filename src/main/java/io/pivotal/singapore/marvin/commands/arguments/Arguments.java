package io.pivotal.singapore.marvin.commands.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.utils.Pair;
import lombok.Getter;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Arguments {
    @Getter private List<Argument> arguments = new ArrayList<>();

    public Arguments() {
    }

    public static Arguments of(String argumentsJson) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> parsedArguments = new ArrayList<>();
        try {
            parsedArguments = mapper.readValue(argumentsJson, ArrayList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!parsedArguments.isEmpty()) {
            return new Arguments(parsedArguments);
        }

        return new Arguments();
    }

    public static Arguments of(List<Argument> arguments) {
        Arguments args = new Arguments();
        arguments.forEach(args::addArgument);

        return args;
    }

    public Arguments(List<Map<String, String>> argumentsJson) {
        for (Map<String, String> argsMap : argumentsJson) {
            for (Map.Entry<String, String> captureGroup : argsMap.entrySet()) {
                addArgument(ArgumentFactory.getArgument(captureGroup.getKey(), captureGroup.getValue()));
            }
        }
    }

    private Arguments addArgument(Argument argument) {
        arguments.add(argument);

        return this;
    }

    public String toJson() {
        if (arguments.isEmpty()) {
            return "";
        } else {
            return "[" +
                arguments
                    .stream()
                    .map(Argument::toJson)
                    .collect(Collectors.joining(",")) +
                "]";
        }
    }

    public List<Map<String, String>> toList() {
        List<Map<String, String>> returnValue = new ArrayList<>();
        for (Argument arg : arguments) {
            returnValue.add(Collections.singletonMap(arg.getName(), arg.getPattern()));
        }

        return returnValue;
    }

    public Map<String, String> parse(String rawCommand) {
        TreeMap<String, String> returnMap = new TreeMap<>();
        rawCommand = rawCommand.trim();

        for (Argument argument : getArguments()) {
            Pair<Integer, String> match = argument.parse(rawCommand);
            rawCommand = rawCommand.subSequence(match.first, rawCommand.length()).toString().trim();

            returnMap.put(argument.getName(), match.last);
        }

        return returnMap;
    }
}
