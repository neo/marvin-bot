package io.pivotal.singapore.marvin.slack;

import com.google.common.collect.Lists;
import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.SubCommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.arguments.RegexArgument;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceRequest;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import io.pivotal.singapore.marvin.utils.FrozenTimeMachine;
import io.pivotal.singapore.marvin.utils.IntegrationBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.*;

import static io.pivotal.singapore.marvin.utils.CommandFactory.createSubCommand;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// NOTE: Is there a good reason this is nested? Seems like a violation of SRP to me (Jasonm23)

@RunWith(Enclosed.class)
public class SlackControllerTest {

    public static class SlackControllerIntegration extends IntegrationBase {
        @Test
        public void wiring() throws Exception {
            webAppContextSetup(wac)
                .build()
                .perform(get("/")
                .param("token", SLACK_TOKEN)
                .param("text", "")
            ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class CommandTesting {
        @Mock
        private CommandRepository commandRepository;

        @Mock
        private RemoteApiService remoteApiService;

        @Spy
        private FrozenTimeMachine clock;

        @Spy
        private CommandParserService commandParserService;

        @InjectMocks
        private SlackController controller;

        private HashMap<String, String> slackInputParams;

        private Command command;
        private Optional<Command> optionalCommand;

        private final String MOCK_SLACK_TOKEN = "TEST TOKEN";
        private final String MOCK_SLACK_CLIENT_ID = "TEST CLIENT ID";

        @Before
        public void setUp() {
            ReflectionTestUtils.setField(controller, "SLACK_TOKEN", MOCK_SLACK_TOKEN, String.class);

            command = new Command("time", "http://somesuch.tld/api/time/");
            optionalCommand = Optional.of(command);

            slackInputParams = new HashMap();
            slackInputParams.put("token", MOCK_SLACK_TOKEN);
            slackInputParams.put("team_id", "T0001");
            slackInputParams.put("team_domain", "example");
            slackInputParams.put("channel_id", "C2147483705");
            slackInputParams.put("channel_name", "test");
            slackInputParams.put("user_id", "U2147483697");
            slackInputParams.put("user_name", "Steve");
            slackInputParams.put("command", "/marvin");
            slackInputParams.put("text", "time");
            slackInputParams.put("response_url", "https://hooks.slack.com/commands/1234/5678");
        }

        @Test
        public void testReceiveTimeOfDay() throws Exception {
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);

            assertThat(response.getBody().getResponseType(), is(equalTo("ephemeral")));
            assertThat(response.getBody().getText(), is(equalTo("This will all end in tears.")));
        }

        @Test
        public void findsCommandAndCallsEndpoint() throws Exception {
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String australiaTime = "The time in Australia is Beer o'clock.";
            serviceResponse.put("message", australiaTime);
            when(remoteApiService.call(eq(command), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(australiaTime)));
            verify(commandRepository, atLeastOnce()).findOneByName("time");
            verify(remoteApiService, times(1)).call(eq(command), any(RemoteApiServiceRequest.class));
        }

        @Test
        public void findsCommandWhenItHasArguments() throws Exception {
            slackInputParams.put("text", "time england");

            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            serviceResponse.put("message", englandTime);
            when(remoteApiService.call(eq(command), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(englandTime)));
        }

        @Test
        public void findsSubCommandsWhenItHasThem() throws Exception {
            slackInputParams.put("text", "time in London");
            Arguments arguments = mock(Arguments.class);

            SubCommand subCommand = createSubCommand("in");
            subCommand.setArguments(arguments);

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(subCommand);
            command.setSubCommands(subCommands);

            ArgumentParsedResultList parsedArguments = new ArgumentParsedResultList();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            Map<String, String> returnParams = new TreeMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            returnParams.put("message", englandTime);
            when(remoteApiService.call(eq(subCommand), any(RemoteApiServiceRequest.class))).thenReturn(
                    new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure())
            );

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(englandTime)));
        }

        @Test
        public void invalidMessageTypeTurnsIntoEpehemeral() throws Exception {
            slackInputParams.put("text", "time in england");

            SubCommand subCommand = createSubCommand("in");
            command.setSubCommands(Lists.newArrayList(subCommand));

            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            serviceResponse.put("message", englandTime);
            serviceResponse.put("message_type", "dingDong");
            when(remoteApiService.call(eq(subCommand), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(englandTime)));
            assertThat(response.getBody().getResponseType(), is(equalTo("ephemeral")));
        }

        @Test
        public void testDefaultErrorResponse() throws Exception {
            slackInputParams.put("text", "time in London");
            Arguments arguments = mock(Arguments.class);

            SubCommand subCommand = createSubCommand("in");
            subCommand.setArguments(arguments);

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(subCommand);
            command.setSubCommands(subCommands);

            ArgumentParsedResultList parsedArguments = new ArgumentParsedResultList();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(eq(subCommand), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(false, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(subCommand.getDefaultResponseFailure())));
        }

        @Test
        public void testDefaultSuccessResponse() throws Exception {
            slackInputParams.put("text", "time in London");
            Arguments arguments = mock(Arguments.class);

            SubCommand subCommand = createSubCommand("in");
            subCommand.setArguments(arguments);

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(subCommand);
            command.setSubCommands(subCommands);

            ArgumentParsedResultList parsedArguments = new ArgumentParsedResultList();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(eq(subCommand), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo(subCommand.getDefaultResponseSuccess())));
        }

        @Test
        public void testNoDefaultSuccessResponse() throws Exception {
            slackInputParams.put("text", "time in London");
            Arguments arguments = mock(Arguments.class);

            SubCommand subCommand = createSubCommand("in");
            subCommand.setArguments(arguments);
            subCommand.setDefaultResponseSuccess(null);

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(subCommand);
            command.setSubCommands(subCommands);

            ArgumentParsedResultList parsedArguments = new ArgumentParsedResultList();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(eq(subCommand), any(RemoteApiServiceRequest.class))).thenReturn(
                    new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure())
            );

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo("{}")));
        }


        @Test
        public void ignoresRequestWhenSlackTokenIsMissing() throws Exception {
            slackInputParams.put("token", null);
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);

            assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat(response.getBody().getResponseType(), is(equalTo("ephemeral")));
            assertThat(response.getBody().getText(), is(equalTo("This will all end in tears.")));
        }

        @Test
        public void badRequestWhenSlackTokenIsIncorrect() throws Exception {
            slackInputParams.put("token", "WRONG TOKEN");
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);

            assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
            assertThat(response.getBody().getResponseType(), is(equalTo("ephemeral")));
            assertThat(response.getBody().getText(), is(equalTo("Unrecognized token")));
        }

        @Test
        public void testStringInterpolationForDefaultSuccessMessage() throws Exception {
            command.setDefaultResponseSuccess("It is time for {meal}, {name}! Have a good time, {name}!");
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> remoteServiceResponse = new HashMap<>();
            remoteServiceResponse.put("name", "Wilson");
            remoteServiceResponse.put("meal", "breakfast");
            when(remoteApiService.call(eq(command), any(RemoteApiServiceRequest.class)))
                    .thenReturn(new RemoteApiServiceResponse(true, remoteServiceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo("It is time for breakfast, Wilson! Have a good time, Wilson!")));
        }

        @Test
        public void returnsDefaultResponseIfSubCommandsAreParsedButAreNotDefined() throws Exception {
            slackInputParams.put("text", "chocolate pinkberry argsssss");
            command.setName("chocolate");

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(createSubCommand("other"));

            Optional<Command> command = Optional.of(new Command("chocolate", "http://fake-endpoint.tld") {{
                setSubCommands(subCommands);
            }});
            when(commandRepository.findOneByName("chocolate")).thenReturn(command);

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo("This sub command doesn't exist for chocolate")));
        }

        @Test
        public void returnsDefaultResponseIfSubCommandArgumentsAreNotMatched() throws Exception {
            slackInputParams.put("text", "time in");
            List<SubCommand> subCommands = new ArrayList<>();

            Arguments arguments = Arguments.of(Lists.newArrayList(new RegexArgument("location", "/.+/")));

            SubCommand subCommand = createSubCommand("in");
            subCommand.setArguments(arguments);
            subCommands.add(subCommand);

            Optional<Command> command = Optional.of(new Command("time", "http://fake-endpoint.tld") {{
                setSubCommands(subCommands);
            }});
            when(commandRepository.findOneByName("time")).thenReturn(command);

            ResponseEntity<OutgoingSlackResponse> response = controller.index(slackInputParams);
            assertThat(response.getBody().getText(), is(equalTo("`location` is not found in your command.")));
            assertThat(response.getBody().getResponseType(), is(equalTo("ephemeral")));
        }

        @Test
        public void navigatingToStartRedirectsToSlackOAuthWithGetParameters() throws Exception {
            ReflectionTestUtils.setField(controller, "SLACK_CLIENT_ID", MOCK_SLACK_CLIENT_ID, String.class);

            ResponseEntity responseEntity = controller.start();

            assertThat(responseEntity.getHeaders().getLocation(), is(equalTo(new URI("https://slack.com/oauth/authorize?client_id=TEST+CLIENT+ID&scope=chat%3Awrite%3Auser+chat%3Awrite%3Abot"))));
            assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.FOUND)));
        }
    }
}
