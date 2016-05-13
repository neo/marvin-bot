package io.pivotal.singapore.marvin.slack;

import com.google.common.collect.Lists;
import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.SubCommand;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.arguments.RegexArgument;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.RemoteApiService;
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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

        private Map<String, String> response;
        private Map<String, Object> apiServiceParams;

        private String MOCK_SLACK_TOKEN = "TEST TOKEN";

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

            apiServiceParams = new HashMap<>();
            apiServiceParams.put("username", "Steve@pivotal.io");
            apiServiceParams.put("channel", "test");
            apiServiceParams.put("received_at", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            apiServiceParams.put("command", "time");

            response = new HashMap<>();
            response.put("response_type", "ephemeral");
            response.put("text", "This will all end in tears.");
        }

        @Test
        public void testReceiveTimeOfDay() throws Exception {
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            assertThat(controller.index(slackInputParams), is(equalTo(response)));
        }

        @Test
        public void findsCommandAndCallsEndpoint() throws Exception {
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String australiaTime = "The time in Australia is Beer o'clock.";
            serviceResponse.put("message", australiaTime);
            when(remoteApiService.call(command, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(australiaTime)));
            verify(commandRepository, atLeastOnce()).findOneByName("time");
            verify(remoteApiService, times(1)).call(command, apiServiceParams);
        }

        @Test
        public void findsCommandWhenItHasArguments() throws Exception {
            slackInputParams.put("text", "time england");
            apiServiceParams.put("command", "time england");

            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            serviceResponse.put("message", englandTime);
            when(remoteApiService.call(command, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(englandTime)));
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

            List<ArgumentParsedResult> parsedArguments = new ArrayList<>();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            apiServiceParams.putAll(parsedArguments.stream().collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult)));
            apiServiceParams.put("command", "time in London");
            Map<String, String> returnParams = new TreeMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            returnParams.put("message", englandTime);
            when(remoteApiService.call(subCommand, apiServiceParams)).thenReturn(
                    new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure())
            );

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(englandTime)));
        }

//        @Test
//        public void testResponseMapping() throws Exception {
//            Optional<MessageType> responseType = Optional.of(MessageType.channel);
//            String text = "some example";
//
//            HashMap<String, String> response = controller.successResponse(responseType, text);
//            assertThat(response.get("response_type"), is("in_channel"));
//
//            responseType = Optional.of(MessageType.user);
//            response = controller.successResponse(responseType, text);
//
//            assertThat(response.get("response_type"), is("ephemeral"));
//
//            responseType = Optional.empty();
//            response = controller.successResponse(responseType, text);
//
//            assertThat(response.get("response_type"), is("ephemeral"));
//        }

        @Test
        public void invalidMessageTypeTurnsIntoEpehemeral() throws Exception {
            slackInputParams.put("text", "time in england");
            apiServiceParams.put("command", "time in england");

            SubCommand subCommand = createSubCommand("in");
            command.setSubCommands(Lists.newArrayList(subCommand));

            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> serviceResponse = new HashMap<>();
            String englandTime = "The time in England is Tea o'clock.";
            serviceResponse.put("message", englandTime);
            serviceResponse.put("message_type", "dingDong");
            when(remoteApiService.call(subCommand, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(true, serviceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(englandTime)));
            assertThat(response.get("response_type"), is(equalTo("ephemeral")));
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

            List<ArgumentParsedResult> parsedArguments = new ArrayList<>();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            apiServiceParams.putAll(parsedArguments.stream().collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult)));
            apiServiceParams.put("command", "time in London");
            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(subCommand, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(false, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(subCommand.getDefaultResponseFailure())));
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

            List<ArgumentParsedResult> parsedArguments = new ArrayList<>();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            apiServiceParams.putAll(parsedArguments.stream().collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult)));
            apiServiceParams.put("command", "time in London");
            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(subCommand, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo(subCommand.getDefaultResponseSuccess())));
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

            List<ArgumentParsedResult> parsedArguments = new ArrayList<>();
            parsedArguments.add(
                new ArgumentParsedResult.Builder()
                    .argumentName("location")
                    .matchResult("London")
                    .success()
                    .build()
            );

            when(arguments.parse("London")).thenReturn(parsedArguments);
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            apiServiceParams.putAll(parsedArguments.stream().collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult)));
            apiServiceParams.put("command", "time in London");
            Map<String, String> returnParams = new TreeMap<>();
            when(remoteApiService.call(subCommand, apiServiceParams)).thenReturn(
                    new RemoteApiServiceResponse(true, returnParams, subCommand.getDefaultResponseSuccess(), subCommand.getDefaultResponseFailure())
            );

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo("{}")));
        }


        @Test(expected = UnrecognizedApiToken.class)
        public void ignoresRequestWhenSlackTokenIsMissing() throws Exception {
            slackInputParams.put("token", null);
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            controller.index(slackInputParams);
        }

        @Test(expected = UnrecognizedApiToken.class)
        public void ignoresRequestWhenSlackTokenIsIncorrect() throws Exception {
            slackInputParams.put("token", "WRONG TOKEN");
            when(commandRepository.findOneByName("time")).thenReturn(Optional.empty());

            controller.index(slackInputParams);
        }

        @Test
        public void testStringInterpolationForDefaultSuccessMessage() throws Exception {
            command.setDefaultResponseSuccess("It is time for {meal}, {name}! Have a good time, {name}!");
            when(commandRepository.findOneByName("time")).thenReturn(optionalCommand);

            HashMap<String, String> remoteServiceResponse = new HashMap<>();
            remoteServiceResponse.put("name", "Wilson");
            remoteServiceResponse.put("meal", "breakfast");
            when(remoteApiService.call(command, apiServiceParams))
                    .thenReturn(new RemoteApiServiceResponse(true, remoteServiceResponse, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure()));

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo("It is time for breakfast, Wilson! Have a good time, Wilson!")));
        }

        @Test
        public void returnsDefaultResponseIfSubCommandsAreParsedButAreNotDefined() throws Exception {
            slackInputParams.put("text", "chocolate pinkberry argsssss");
            apiServiceParams.put("command", "chocolate pinkberry argsssss");
            command.setName("chocolate");

            List<SubCommand> subCommands = new ArrayList<>();
            subCommands.add(createSubCommand("other"));

            Optional<Command> command = Optional.of(new Command("chocolate", "http://fake-endpoint.tld") {{
                setSubCommands(subCommands);
            }});
            when(commandRepository.findOneByName("chocolate")).thenReturn(command);

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo("This sub command doesn't exist for chocolate")));
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

            Map<String, String> response = controller.index(slackInputParams);
            assertThat(response.get("text"), is(equalTo("`location` is not found in your command.")));
            assertThat(response.get("response_type"), is(equalTo("ephemeral")));
        }

    }
}
