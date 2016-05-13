package io.pivotal.singapore.marvin.commands.arguments;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RegexArgumentTest {
    private String defaultName = "event_name";
    private String pattern = "/\"([^\"]+)\"/";
    private RegexArgument subject = new RegexArgument(defaultName, pattern);

    @Test
    public void charactersConsumedIsFullCaptureGroup() {
        ArgumentParsedResult result = subject.parse("\"BBQ At the Pivotal Labs Singapore office\" on the 23rd of March at 7pm");

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getPattern(), equalTo(pattern));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.SUCCESS));
        assertThat(result.getMatchOffset(), equalTo(42));
        assertThat(result.getMatchResult(), equalTo("BBQ At the Pivotal Labs Singapore office"));
    }

    @Test
    public void parsesFromBeginningOfString() {
        ArgumentParsedResult result = subject.parse("On the 23rd of March at 7pm \"BBQ At the Pivotal Labs Singapore office\"");
        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getPattern(), equalTo(pattern));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.FAILURE));
    }
}
