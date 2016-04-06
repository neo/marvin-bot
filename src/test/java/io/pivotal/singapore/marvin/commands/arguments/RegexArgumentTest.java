package io.pivotal.singapore.marvin.commands.arguments;

import io.pivotal.singapore.marvin.utils.Pair;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class RegexArgumentTest {
    private String defaultName = "event_name";
    private String pattern = "/\"([^\"]+)\"/";
    private RegexArgument subject = new RegexArgument(defaultName, pattern);

    @Test
    public void charactersConsumedIsFullCaptureGroup() {
        Pair<Integer, String> result = subject.parse("\"BBQ At the Pivotal Labs Singapore office\" on the 23rd of March at 7pm");

        assertThat(result.first, equalTo(42));
        assertThat(result.last, equalTo("BBQ At the Pivotal Labs Singapore office"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsesFromBeginningOfString() {
        subject.parse("On the 23rd of March at 7pm \"BBQ At the Pivotal Labs Singapore office\"");
    }
}
