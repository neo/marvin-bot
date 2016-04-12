package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.utils.a;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class RemoteApiServiceResponseTest {

    @RunWith(JUnit4.class)
    public static class GetMessageTypeTest {
        @Test
        public void mapsCorrectMessageType() throws Exception {
            assertEquals(
                Optional.of(MessageType.user),
                a.remoteApiServiceResponse
                    .w("message_type", "user")
                    .build()
                    .getMessageType()
            );
        }

        @Test
        public void mapsAlternativeMessageType() throws Exception {
            assertEquals(
                Optional.of(MessageType.channel),
                a.remoteApiServiceResponse
                    .w("messageType", "channel")
                    .build()
                    .getMessageType()
            );
        }

        @Test
        public void getsInvalidMessageType() throws Exception {
            assertEquals(
                Optional.empty(),
                a.remoteApiServiceResponse
                    .w("message_type", "universe")
                    .build()
                    .getMessageType()
            );
        }
    }

    @RunWith(JUnit4.class)
    public static class GetMessageTest {
        @Test
        public void getReturnedMessage() throws Exception {
            assertEquals(
                "Marvin is depressed.",
                a.remoteApiServiceResponse
                    .w("message", "Marvin is depressed.")
                    .build()
                    .getMessage()
            );
        }

        @Test
        public void getDefaultSuccessResponse() throws Exception {
            assertEquals(
                "Marvin is meh.",
                a.remoteApiServiceResponse
                    .w(a.defaultResponse.w("success", "Marvin is meh.").build())
                    .build()
                    .getMessage()
            );
        }

        @Test
        public void getDefaultFailureResponse() throws Exception {
            assertEquals(
                "Marvin is very meh.",
                a.remoteApiServiceResponse
                    .w(false)
                    .w(a.defaultResponse.w("failure", "Marvin is very meh.").build())
                    .build()
                    .getMessage()
            );
        }

        @Test
        public void getEmptyWithNoDefaultAndNoResponseBody() {
            assertEquals(
                "",
                new RemoteApiServiceResponse(false, null, a.defaultResponse.build())
                    .getMessage()
            );
        }

        @Test
        public void getDefaultSuccessResponseFromDefaultResponsesWhenPassedIn() {
            assertEquals(
                "Hello",
                a.remoteApiServiceResponse
                    .w(a.defaultResponse
                        .w("success", "Hello").build())
                    .build()
                    .getMessage()
            );
        }

        @Test
        public void preferDefaultResponseFromCommandWhenPassed() {
            Command command = a.command
                .w(a.defaultResponse
                    .w("success", "Hello from defaultResponses").build())
                .build();
            command.setDefaultResponseSuccess("Hello from command");

            RemoteApiServiceResponse response = new RemoteApiServiceResponse(true, new HashMap<>(), command);

            assertEquals(
                "Hello from command",
                response
                    .getMessage()
            );
        }
    }

    @RunWith(JUnit4.class)
    public static class InterpolationTests {
        @Test
        public void getInterpolatedMessage() throws Exception {
            assertEquals(
                "Marvin says hi to Jarvis.",
                a.remoteApiServiceResponse
                    .w("name", "Jarvis")
                    .w(a.defaultResponse.w("success", "Marvin says hi to {name}.").build())
                    .build()
                    .getMessage()
            );
        }

        // XXX: Are the following two tests signs of us having set the wrong types (String, String)
        // for the response body map we're expecting back?

        @Test
        public void getInterpolatedMessageWithBoolean() throws Exception {
            String json = "{\"truthy\": false}";
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> responseBody = mapper.readValue(json, HashMap.class);

            assertEquals(
                "Can you handle the truth? false.",
                a.remoteApiServiceResponse
                    .w(responseBody)
                    .w(a.defaultResponse.w("success", "Can you handle the truth? {truthy}.").build())
                    .build()
                    .getMessage()
            );
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

            assertEquals(
                "{errors=[{entity=CalendarEvent, message=The Event/Date field is not valid, " +
                    "invalidValue=2016-04-20T10:33:18.619+08:00, property=calendarEventDateTimeString}]}",
                a.remoteApiServiceResponse
                    .w(responseBody)
                    .build()
                    .getMessage()
            );
        }
    }
}
