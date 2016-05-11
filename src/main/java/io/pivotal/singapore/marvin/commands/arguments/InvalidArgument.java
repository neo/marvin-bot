package io.pivotal.singapore.marvin.commands.arguments;

public class InvalidArgument extends AbstractArgument {

    public InvalidArgument() {
    }

    public InvalidArgument(String name, String pattern) {
        setName(name);
        setPattern(pattern);
    }

    @Override
    public ArgumentParsedResult parse(String rawCommand) {
        return new ArgumentParsedResult.Builder()
            .matchResult(rawCommand)
            .failure()
            .build();
    }
}
