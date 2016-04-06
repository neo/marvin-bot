package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.Command;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class RemoteApiServiceResponseTest {
    protected final Map<String, String> responseBody = new HashMap<>();
    protected RemoteApiServiceResponse subject;
    protected Command command = new Command();

    @Before
    public void setUp() throws Exception {
        subject = new RemoteApiServiceResponse(true, responseBody, command);
    }

    @RunWith(JUnit4.class)
    public static class GetMessageTypeTest extends RemoteApiServiceResponseTest {
        @Test
        public void mapsCorrectMessageType() throws Exception {
            responseBody.put("message_type", "user");

            assertThat(subject.getMessageType().get(), is(equalTo(MessageType.user)));
        }

        @Test
        public void mapsAlternativeMessageType() throws Exception {
            responseBody.put("messageType", "channel");

            assertThat(subject.getMessageType().get(), is(equalTo(MessageType.channel)));
        }

        @Test
        public void getsInvalidMessageType() throws Exception {
            responseBody.put("message_type", "universe");

            assertThat(subject.getMessageType(), is(Optional.empty()));
        }
    }

    @RunWith(JUnit4.class)
    public static class GetMessageTest extends RemoteApiServiceResponseTest {
        @Test
        public void getReturnedMessage() throws Exception {
            String returnedMessage = "Marvin is depressed.";
            responseBody.put("message", returnedMessage);

            assertThat(subject.getMessage(), is(equalTo(returnedMessage)));
        }

        @Test
        public void getDefaultSuccessResponse() throws Exception {
            String defaultSuccessMessage = "Marvin is meh.";
            command.setDefaultResponseSuccess(defaultSuccessMessage);

            assertThat(subject.getMessage(), is(equalTo(defaultSuccessMessage)));
        }

        @Test
        public void getDefaultFailureResponse() throws Exception {
            String defaultFailureMessage = "Marvin is very meh.";
            command.setDefaultResponseFailure(defaultFailureMessage);
            subject = new RemoteApiServiceResponse(false, responseBody, command);

            assertThat(subject.getMessage(), is(equalTo(defaultFailureMessage)));
        }

        @Test
        public void getInterpolatedMessage() throws Exception {
            String defaultSuccessMessage = "Marvin says hi to {name}.";
            command.setDefaultResponseSuccess(defaultSuccessMessage);

            String userName = "Jarvis";
            responseBody.put("name", userName);

            assertThat(subject.getMessage(), is(equalTo("Marvin says hi to Jarvis.")));
        }

        @Test
        public void getInterpolatedMessageWhenKeyHasNestedValues() throws Exception {
            String json = "{" +
                "    \"errors\": [" +
                "        {" +
                "            \"entity\": \"CalendarEvent\"," +
                "            \"message\": \"The Event/Date field is not valid\"," +
                "            \"invalidValue\": \"2016-04-20T10:33:18.619+08:00\"," +
                "            \"property\": \"calendarEventDateTimeString\"" +
                "        }" +
                "    ]" +
                "}";
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> responseBody = mapper.readValue(json, HashMap.class);

            subject = new RemoteApiServiceResponse(true, responseBody, command);

            assertThat(subject.getMessage(), is(equalTo(
                "{errors=[{entity=CalendarEvent, message=The Event/Date field is not valid, " +
                    "invalidValue=2016-04-20T10:33:18.619+08:00, property=calendarEventDateTimeString}]}"
            )));
        }
    }
}
