package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.utils.Pair;
import lombok.Getter;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexArgument implements Argument {
    @Getter private String name;
    private Pattern regex;
    @Getter private String pattern;

    public RegexArgument(String name, String regex) {
        this.name = name;
        this.pattern = regex;
        this.regex = Pattern.compile(
            String.format("^%s", (String) regex.subSequence(1, regex.length() - 1))
        );
    }

    public static Boolean canParse(String capture) {
        return capture.startsWith("/") && capture.endsWith("/");
    }

    @Override
    public Pair<Integer, String> parse(String rawCommand) {
        Matcher m = regex.matcher(rawCommand);

        try {
            m.find();
            MatchResult results = m.toMatchResult();

            return new Pair<>(m.end(0), results.group(1));
        } catch (IndexOutOfBoundsException | IllegalStateException ex) {
            throw new IllegalArgumentException(
                String.format("Argument '%s' found no match with regex '%s' in '%s'", name, regex, rawCommand),
                ex
            );
        }
    }

    @Override
    public String toJson() {
        return String.format("{\"%s\":\"%s\"}", getName(), getPattern());
    }

}
