package io.pivotal.singapore.marvin.commands.arguments;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexArgument extends AbstractArgument {
    private Pattern regex;

    public RegexArgument() {
    }

    public RegexArgument(String name, String regex) {
        this.name = name;
        this.pattern = regex;
        setRegex(regex);
    }

    public static Boolean matches(String pattern) {
        return pattern.startsWith("/") && pattern.endsWith("/");
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
    public ArgumentParsedResult parse(String rawCommand) {
        Matcher m = getRegex().matcher(rawCommand);

        try {
            m.find();
            MatchResult results = m.toMatchResult();

            return new ArgumentParsedResult.Builder()
                .argumentName(name)
                .matchOffset(m.end(0))
                .matchResult(results.group(1))
                .pattern(pattern)
                .success()
                .build();
        } catch (IndexOutOfBoundsException | IllegalStateException ex) {
            return new ArgumentParsedResult.Builder()
                .argumentName(name)
                .matchResult(rawCommand)
                .pattern(pattern)
                .failure()
                .build();
        }
    }
}
