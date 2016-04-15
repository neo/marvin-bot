package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.Pair;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexArgument extends AbstractArgument {
    private Pattern regex;

    public RegexArgument() {
    }

    // What is the name argument used for here?
    public RegexArgument(String name, String regex) {
        this.name = name;
        this.pattern = regex;
        setRegex(regex);
    }

    public static Boolean canParse(String capture) {
        return capture.startsWith("/") && capture.endsWith("/");
    }

    public Pattern getRegex() {
        if (regex == null) {
            setRegex(getPattern());
        }

        return regex;
    }

    private void setRegex(String regex) {
        this.regex = Pattern.compile(
            String.format("^%s", (String) regex.subSequence(1, regex.length() - 1))
        );
    }

    @Override
    public Pair<Integer, String> parse(String rawCommand) throws ArgumentParseException {
        Matcher m = getRegex().matcher(rawCommand);

        try {
            m.find();
            MatchResult results = m.toMatchResult();

            return new Pair<>(m.end(0), results.group(1));
        } catch (IndexOutOfBoundsException | IllegalStateException ex) {
            throw new ArgumentParseException(
                    String.format("Argument '%s' found no match with regex '%s' in '%s'", name, regex, rawCommand),
                    ex,
                    this
            );
        }
    }
}
