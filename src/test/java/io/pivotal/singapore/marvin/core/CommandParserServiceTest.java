package io.pivotal.singapore.marvin.core;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CommandParserServiceTest {
    CommandParserService service = new CommandParserService();

    @Test
    public void testParseWhenAllTokensArePresent() {
        String textCommand = "event create \"BBQ at Pivotal labs\" at 7pm on Tuesday";

        HashMap<String, String> result = service.parse(textCommand);

        assertThat(result.get("command"), is("event"));
        assertThat(result.get("sub_command"), is("create"));
        assertThat(result.get("arguments"), is("\"BBQ at Pivotal labs\" at 7pm on Tuesday"));
    }

    @Test
    public void testParseWhenCommandIsMissing() {
        String textCommand = "";

        HashMap<String, String> result = service.parse(textCommand);

        assertThat(result.get("command"), is(""));
        assertThat(result.get("sub_command"), is(""));
        assertThat(result.get("arguments"), is(""));
    }

    @Test
    public void testParseWhenSubCommandIsMissing() {
        String textCommand = "event";

        HashMap<String, String> result = service.parse(textCommand);

        assertThat(result.get("command"), is("event"));
        assertThat(result.get("sub_command"), is(""));
        assertThat(result.get("arguments"), is(""));
    }

    @Test
    public void testParseWhenArgumentsAreMissing() {
        String textCommand = "event create";

        HashMap<String, String> result = service.parse(textCommand);

        assertThat(result.get("command"), is("event"));
        assertThat(result.get("sub_command"), is("create"));
        assertThat(result.get("arguments"), is(""));
    }
}
