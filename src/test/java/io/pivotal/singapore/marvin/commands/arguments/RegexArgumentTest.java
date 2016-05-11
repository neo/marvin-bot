package io.pivotal.singapore.marvin.commands.arguments;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.Assert.assertThat;

public class RegexArgumentTest {
    private String defaultName = "event_name";
    private String pattern = "/\"([^\"]+)\"/";
    private RegexArgument subject = new RegexArgument(defaultName, pattern);

    @Test
    public void charactersConsumedIsFullCaptureGroup() {
        String matchResult = "BBQ At the Pivotal Labs Singapore office";
        String rawCommand = String.format("\"%s\" on the 23rd of March at 7pm", matchResult);
        ArgumentParsedResult result = subject.parse(rawCommand);

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getPattern(), equalTo(pattern));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.SUCCESS));
        assertThat(result.getMatchOffset(), equalTo(matchResult.length() + 2 /* include double quotes */));
        assertThat(result.getMatchResult(), equalTo(matchResult));
    }

    @Test
    public void parsesFromBeginningOfString() {
        ArgumentParsedResult result = subject.parse("On the 23rd of March at 7pm \"BBQ At the Pivotal Labs Singapore office\"");

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getPattern(), equalTo(pattern));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.FAILURE));
        assertThat(result.getMatchOffset(), equalTo(0));
        assertThat(result.getMatchResult(), is(emptyString()));
    }
}
