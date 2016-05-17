package io.pivotal.singapore.marvin.commands.arguments;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.emptyString;
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

    @Test
    public void nonMatchingCommandTextReturnsArgumentNameAndMatchedString() {
        ArgumentParsedResultList results = subject.parse("foo");
        assertTrue(results.hasErrors());
        assertThat(results.get(0).getMatchResult(), is(emptyString()));
    }

    @Test
    public void parseArgumentsShouldBeEvaluatedInOrder() {
        String rawCommand = "23rd of March at 7pm \"BBQ At the Pivotal Labs Singapore office\"";

        ArgumentParsedResultList results = subject.parse(rawCommand);

        assertThat(results.get(0).getMatchResult(), equalTo(expectedDateTimeString));
        assertThat(results.get(1).getMatchResult(), equalTo("BBQ At the Pivotal Labs Singapore office"));
    }

    @Test
    public void returnsArgumentNameAndMatchedStringWhenOnePartIsInvalid() {
        String rawCommand = "23rd of March at 7pm 'not a valid event name string'";

        ArgumentParsedResultList results = subject.parse(rawCommand);
        assertTrue(results.hasErrors());
        assertThat(results.get(1).getMatchResult(), is(emptyString()));
    }

    @Test
    public void returnsArgumentNameAndMatchedStringWhenOnePartIsMissing() {
        String rawCommand = "23rd of March at 7pm";

        ArgumentParsedResultList results = subject.parse(rawCommand);
        assertTrue(results.hasErrors());
        assertThat(results.get(1).getMatchResult(), equalTo(""));
    }

    @Test
    public void commandTextIsTrimmedFromLeadingAndTrailingWhitespace() {
        String rawCommand = "       23rd of March at 7pm        \"BBQ At the Pivotal Labs Singapore office\"        ";

        ArgumentParsedResultList result = subject.parse(rawCommand);

        assertThat(result.get(0).getMatchResult(), equalTo(expectedDateTimeString));
        assertThat(result.get(1).getMatchResult(), equalTo("BBQ At the Pivotal Labs Singapore office"));
    }
}
