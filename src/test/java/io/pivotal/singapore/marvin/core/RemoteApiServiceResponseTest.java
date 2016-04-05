package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class RemoteApiServiceResponseTest {
    protected final Map<String, String> responseBody = new HashMap<>();
    protected RemoteApiServiceResponse subject;

    protected String defaultResponseSuccess;
    protected String defaultResponseFailure;

    @Before
    public void setUp() throws Exception {
        defaultResponseSuccess = "I succeeded! :-)";
        defaultResponseFailure = "I failed :-(";
        subject = new RemoteApiServiceResponse(true, responseBody, defaultResponseSuccess, defaultResponseFailure);
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
            String defaultResponseSuccess = "Marvin is meh.";
            subject.setDefaultResponseSuccess(defaultResponseSuccess);

            assertThat(subject.getMessage(), is(equalTo(defaultResponseSuccess)));
        }

        @Test
        public void getDefaultFailureResponse() throws Exception {
            defaultResponseFailure = "Marvin is very meh.";
            subject = new RemoteApiServiceResponse(false, responseBody, defaultResponseSuccess, defaultResponseFailure);

            assertThat(subject.getMessage(), is(equalTo(defaultResponseFailure)));
        }

        @Test
        public void getInterpolatedMessage() throws Exception {
            subject.setDefaultResponseSuccess("Marvin says hi to {name}.");

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

            subject = new RemoteApiServiceResponse(true, responseBody);

            assertThat(subject.getMessage(), is(equalTo(
                "{errors=[{entity=CalendarEvent, message=The Event/Date field is not valid, " +
                    "invalidValue=2016-04-20T10:33:18.619+08:00, property=calendarEventDateTimeString}]}"
            )));
        }
    }
}
