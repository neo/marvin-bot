package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.ThrowableCatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class TimestampArgumentTest {
    private String defaultName = "timestamp";
    private TimestampArgument subject = new TimestampArgument(defaultName);
    private String expectedDateTimeString = "2016-03-23T19:00:00+08:00";

    @Test
    public void parseTimestringOnItsOwn() {
        String rawCommand = "23rd of March at 7pm";
        ArgumentParsedResult result = subject.parse(rawCommand);

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getMatchOffset(), equalTo(rawCommand.length()));
        assertThat(result.getMatchResult(), equalTo(expectedDateTimeString));
        assertThat(result.getPattern(), equalTo("TIMESTAMP"));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.SUCCESS));
    }

    @Test
    public void noValidTimeString() {
        String rawCommand = "I'm a fluffy ballonicorn!";
        ArgumentParsedResult result = subject.parse(rawCommand);

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getMatchOffset(), equalTo(0));
        assertThat(result.getMatchResult(), is(emptyString()));
        assertThat(result.getPattern(), equalTo("TIMESTAMP"));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.FAILURE));
    }

    @Test
    public void parseValidTimeStringWithOtherStuff() {
        String rawCommand = "BBQ At the Pivotal Labs Singapore office on the 23rd of March at 7pm";
        ArgumentParsedResult result = subject.parse(rawCommand);

        assertThat(result.getArgumentName(), equalTo(defaultName));
        assertThat(result.getMatchOffset(), equalTo(rawCommand.length()));
        assertThat(result.getMatchResult(), equalTo(expectedDateTimeString));
        assertThat(result.getPattern(), equalTo("TIMESTAMP"));
        assertThat(result.getType(), equalTo(ArgumentParsedResultType.SUCCESS));
    }

    @Test
    public void parseAllSupportedTimestrings() {
        List<String> validStrings = Arrays.asList(
            "on the 23rd of March at 7pm",
            "at 7pm on the 23rd of March",
            "23 March 7pm",
            "2016-03-23 7pm",
            "at 19:00 March 23",
            "next thursday at 9pm"
        );

        for (String validString : validStrings) {
            Throwable result = ThrowableCatcher.capture(() -> subject.parse(validString));
            assertThat(result, is(nullValue()));
        }
    }

    @Test
    public void noNanosecondsInOutput() {
        String s = "tomorrow";

        ArgumentParsedResult result  = subject.parse(s);

        String timestamp = result.getMatchResult();
        String timePart = timestamp.split("T")[1].split("\\+")[0];
        String seconds = timePart.split(":")[2];

        assertThat(
            String.format("Second part seems to contain nanoseconds, %s", timestamp),
            seconds,
            not(containsString("."))
        );
    }
}
