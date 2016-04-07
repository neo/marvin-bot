package io.pivotal.singapore.marvin.commands.arguments;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ArgumentsTest {
    private Arguments subject;
    private String expectedDateTimeString = "2016-03-23T19:00:00+08:00";

    @Before
    public void setUp() throws Exception {
        subject = Arguments.of(Arrays.asList(
            new TimestampArgument("start_time"),
            new RegexArgument("event_name", "/\"([^\"]+)\"/")
        ));
    }

    @Test(expected = ArgumentParseException.class)
    public void nonMatchingCommandTextRaisesException() throws ArgumentParseException {
        subject.parse("");
    }

    @Test
    public void parseArgumentsShouldBeEvaluatedInOrder() throws ArgumentParseException {
        String rawCommand = "23rd of March at 7pm \"BBQ At the Pivotal Labs Singapore office\"";

        Map<String, String> result = subject.parse(rawCommand);

        assertThat(result.get("start_time"), equalTo(expectedDateTimeString));
        assertThat(result.get("event_name"), equalTo("BBQ At the Pivotal Labs Singapore office"));
    }

    @Test(expected = ArgumentParseException.class)
    public void raisesExceptionWhenOnePartIsInvalid() throws ArgumentParseException {
        String rawCommand = "23rd of March at 7pm 'not a valid event name string'";

        subject.parse(rawCommand);
    }

    @Test(expected = ArgumentParseException.class)
    public void raisesExceptionWhenOnePartIsMissing() throws ArgumentParseException {
        String rawCommand = "23rd of March at 7pm";

        subject.parse(rawCommand);
    }

    @Test
    public void commandTextIsTrimmedFromLeadingAndTrailingWhitespace() throws ArgumentParseException {
        String rawCommand = "       23rd of March at 7pm        \"BBQ At the Pivotal Labs Singapore office\"        ";

        Map result = subject.parse(rawCommand);

        assertThat(result.get("start_time"), equalTo(expectedDateTimeString));
        assertThat(result.get("event_name"), equalTo("BBQ At the Pivotal Labs Singapore office"));
    }
}
