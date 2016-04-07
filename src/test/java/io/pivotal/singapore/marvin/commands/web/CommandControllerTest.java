package io.pivotal.singapore.marvin.commands.web;

import org.junit.Test;
import org.springframework.web.HttpMediaTypeNotSupportedException;

public class CommandControllerTest {

    @Test(expected=HttpMediaTypeNotSupportedException.class)
    public void testPUTisNotSupported() throws Exception {
        CommandController subject = new CommandController();
        subject.handleDisallowedMethods();
    }
}
